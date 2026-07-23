<script setup>
import { ref, onMounted } from 'vue'
import { getUserAppointments } from '@/api/appointment'

const list = ref([])
const total = ref(0)
const page = ref(1)

onMounted(fetchData)

async function fetchData() {
  const res = await getUserAppointments({ page: page.value, size: 10 })
  list.value = res.data.records
  total.value = res.data.total
}

const statusMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }
const statusType = { PENDING: 'warning', CONFIRMED: 'success', CANCELLED: 'info', COMPLETED: '' }
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
