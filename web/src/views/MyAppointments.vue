<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getUserAppointments, cancelAppointment, payAppointment } from '@/api/appointment'

const list = ref([])
const total = ref(0)
const page = ref(1)

onMounted(fetchData)

async function fetchData() {
  const res = await getUserAppointments({ page: page.value, size: 10 })
  list.value = res.data.records
  total.value = res.data.total
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(
      `确定取消 ${row.doctorName}（${row.appointmentDate} ${row.timeSlot}）的预约吗？取消后号源将退回。`,
      '取消预约',
      { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '再想想' }
    )
  } catch {
    return
  }
  try {
    await cancelAppointment(row.id)
    ElMessage.success('已取消预约，号源已退回')
    fetchData()
  } catch {}
}

async function handlePay(row) {
  try {
    await ElMessageBox.confirm(
      `应付挂号费 ¥${row.amount}（${row.doctorName} · ${row.department}）`,
      '支付挂号费',
      { confirmButtonText: '模拟支付', cancelButtonText: '稍后再说' }
    )
  } catch {
    return
  }
  try {
    await payAppointment(row.id)
    ElMessage.success('支付成功，等待医生确认')
    fetchData()
  } catch {}
}

const statusMap = { UNPAID: '待支付', PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }
const statusType = { UNPAID: 'danger', PENDING: 'warning', CONFIRMED: 'success', CANCELLED: 'info', COMPLETED: '' }
</script>

<template>
  <div class="page">
    <h2>我的预约</h2>
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="hospitalName" label="医院" width="150" />
      <el-table-column prop="doctorName" label="医生" width="100" />
      <el-table-column prop="department" label="科室" width="100" />
      <el-table-column prop="appointmentDate" label="日期" width="120" />
      <el-table-column prop="timeSlot" label="时间段" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType[row.status]">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" min-width="160" />
      <el-table-column label="挂号费" width="100" align="center">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column label="操作" width="170" align="center">
        <template #default="{ row }">
          <el-button v-if="row.status === 'UNPAID'" type="primary" link @click="handlePay(row)">去支付</el-button>
          <el-button
            v-if="row.status === 'UNPAID' || row.status === 'PENDING' || row.status === 'CONFIRMED'"
            type="danger" link @click="handleCancel(row)"
          >取消预约</el-button>
          <span v-if="row.status === 'CANCELLED' || row.status === 'COMPLETED'" style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="page-bar" v-if="total > 10">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="page" @current-change="fetchData" />
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; }
.page h2 { color: #1A3C6E; margin-bottom: 16px; }
.page-bar { margin-top: 16px; display: flex; justify-content: center; }
</style>
