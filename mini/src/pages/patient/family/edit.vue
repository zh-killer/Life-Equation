<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFamilyMembers, addFamilyMember, updateFamilyMember } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const id = ref('')
const isEdit = ref(false)
const loading = ref(false)

const form = reactive({
  name: '', idCard: '', phone: '',
  relationship: '', gender: '', birthDate: '',
})

const relations = ['本人', '配偶', '子女', '父母', '其他']
const genders = ['男', '女']

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const opts = (page.$page && page.$page.options) || page.options || {}
  if (opts.id) {
    id.value = opts.id
    isEdit.value = true
    uni.setNavigationBarTitle({ title: '编辑就诊人' })
    loadMember(opts.id)
  }
})

async function loadMember(mid) {
  try {
    const res = await getFamilyMembers()
    const member = (res.data || []).find(m => m.id == mid)
    if (member) Object.assign(form, {
      name: member.name || '', idCard: member.idCard || '',
      phone: member.phone || '', relationship: member.relationship || '',
      gender: member.gender || '', birthDate: member.birthDate || '',
    })
  } catch {}
}

function handleSave() {
  if (!form.name.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' }); return
  }
  loading.value = true
  const save = isEdit.value ? updateFamilyMember(id.value, form) : addFamilyMember(form)
  save.then(() => {
    uni.showToast({ title: isEdit.value ? '已更新' : '已添加' })
    setTimeout(() => uni.navigateBack(), 800)
  }).catch(() => {}).finally(() => { loading.value = false })
}
</script>

<template>
  <view class="page">
    <view class="form-card">
      <view class="input-row">
        <text class="label">姓名<text class="required">*</text></text>
        <input v-model="form.name" placeholder="请输入姓名" placeholder-class="ph" class="input" />
      </view>
      <view class="input-row">
        <text class="label">关系</text>
        <picker mode="selector" :range="relations" @change="e => form.relationship = relations[e.detail.value]">
          <view class="picker-item">
            <text :class="{ ph: !form.relationship }">{{ form.relationship || '请选择关系' }}</text>
            <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
          </view>
        </picker>
      </view>
      <view class="input-row">
        <text class="label">性别</text>
        <picker mode="selector" :range="genders" @change="e => form.gender = genders[e.detail.value]">
          <view class="picker-item">
            <text :class="{ ph: !form.gender }">{{ form.gender || '请选择' }}</text>
            <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
          </view>
        </picker>
      </view>
      <view class="input-row">
        <text class="label">出生日期</text>
        <picker mode="date" :value="form.birthDate" @change="e => form.birthDate = e.detail.value">
          <view class="picker-item">
            <text :class="{ ph: !form.birthDate }">{{ form.birthDate || '请选择' }}</text>
            <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
          </view>
        </picker>
      </view>
      <view class="input-row">
        <text class="label">身份证</text>
        <input v-model="form.idCard" placeholder="选填，挂号时可用" placeholder-class="ph" class="input" maxlength="18" />
      </view>
      <view class="input-row">
        <text class="label">手机号</text>
        <input v-model="form.phone" placeholder="选填" placeholder-class="ph" class="input" maxlength="11" type="number" />
      </view>
    </view>

    <button class="btn-save" :loading="loading" @click="handleSave">
      {{ isEdit ? '保存修改' : '添加就诊人' }}
    </button>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding: 16px; }
.form-card { background: #fff; border-radius: 14px; padding: 4px 16px; border: 1px solid #F2F2F7; }
.input-row { display: flex; align-items: center; gap: 12px; padding: 14px 0; border-bottom: 1px solid #F2F2F7; }
.input-row:last-child { border-bottom: none; }
.label { font-size: 15px; color: #1C1C1E; width: 70px; flex-shrink: 0; }
.required { color: #FF3B30; }
.input { flex: 1; font-size: 14px; color: #1C1C1E; text-align: right; }
.ph { color: #C7C7CC; }
.picker-item { display: flex; align-items: center; justify-content: flex-end; flex: 1; }
.picker-item text { font-size: 14px; color: #1C1C1E; }
.picker-item text.ph { color: #C7C7CC; }

.btn-save {
  width: 100%; height: 48px; line-height: 48px;
  background: #2DD4BF; color: #fff; border: none;
  border-radius: 12px; font-size: 16px; font-weight: 600;
  margin-top: 24px;
}
</style>
