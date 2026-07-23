<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo, updateProfile, changePassword } from '@/api/auth'

const form = ref({ realName: '', phone: '', email: '', avatarUrl: '', gender: '', birthDate: '' })
const saving = ref(false)
const avatarPreview = ref('')

const router = useRouter()

// 修改密码
const showPwdDialog = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdChanging = ref(false)

const userInfo = computed(() => JSON.parse(localStorage.getItem('userInfo') || '{}'))

onMounted(async () => {
  try {
    const res = await getUserInfo()
    if (res.data) {
      form.value.realName = res.data.realName || ''
      form.value.phone = res.data.phone || ''
      form.value.email = res.data.email || ''
      form.value.avatarUrl = res.data.avatarUrl || ''
      form.value.gender = res.data.gender || ''
      form.value.birthDate = res.data.birthDate || ''
      avatarPreview.value = res.data.avatarUrl || ''
    }
  } catch (e) { /* */ }
})

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({
      realName: form.value.realName,
      phone: form.value.phone,
      email: form.value.email,
      avatarUrl: form.value.avatarUrl
    })
    const res = await getUserInfo()
    if (res.data) localStorage.setItem('userInfo', JSON.stringify(res.data))
    ElMessage.success('个人资料已更新')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally { saving.value = false }
}

function handleAvatarUrl() {
  ElMessageBox.prompt('请输入头像图片链接地址', '更换头像', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: form.value.avatarUrl || '',
  }).then(({ value }) => {
    if (value) {
      form.value.avatarUrl = value
      avatarPreview.value = value
    }
  }).catch(() => {})
}

function handleChangePwd() {
  pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  showPwdDialog.value = true
}

async function doChangePwd() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) { ElMessage.warning('请填写完整'); return }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) { ElMessage.warning('两次输入的新密码不一致'); return }
  if (pwdForm.value.newPassword.length < 6) { ElMessage.warning('新密码长度至少6位'); return }
  pwdChanging.value = true
  try {
    await changePassword({ oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    ElMessage.success('密码已修改，请重新登录')
    showPwdDialog.value = false
    setTimeout(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }, 500)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  } finally { pwdChanging.value = false }
}

const genderOptions = [
  { value: 1, label: '男' },
  { value: 2, label: '女' },
  { value: 0, label: '保密' },
]
</script>

<template>
  <div class="page">
    <h2>⚙️ 个人设置</h2>

    <!-- 头像 + 基本信息卡片 -->
    <div class="profile-card">
      <div class="avatar-section">
        <div class="avatar-preview" v-if="avatarPreview">
          <img :src="avatarPreview" alt="avatar" @error="avatarPreview = ''" />
        </div>
        <div class="avatar-preview" v-else>
          <span>{{ form.realName?.charAt(0) || userInfo.username?.charAt(0) || '👤' }}</span>
        </div>
        <el-button size="small" text type="primary" @click="handleAvatarUrl">更换头像</el-button>
      </div>
      <div class="profile-summary">
        <h3>{{ form.realName || userInfo.username }}</h3>
        <p>{{ userInfo.role === 'USER' ? '普通用户' : userInfo.role === 'DOCTOR' ? '医生' : '管理员' }}</p>
        <p class="summary-meta">账号：{{ userInfo.username }}</p>
      </div>
    </div>

    <!-- 个人资料编辑 -->
    <div class="section">
      <h3>📝 个人资料</h3>
      <el-form :model="form" label-width="80px" style="max-width:480px">
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="请选择">
            <el-option v-for="g in genderOptions" :key="g.value" :label="g.label" :value="g.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthDate" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 账号安全 -->
    <div class="section">
      <h3>🔐 账号安全</h3>
      <div class="security-item">
        <div>
          <strong>登录密码</strong>
          <p>定期更换密码保障账号安全</p>
        </div>
        <el-button text type="primary" @click="handleChangePwd">修改</el-button>
      </div>
      <div class="security-item">
        <div>
          <strong>绑定手机</strong>
          <p>{{ form.phone || '未绑定' }}</p>
        </div>
        <span class="sec-status">{{ form.phone ? '已绑定' : '待绑定' }}</span>
      </div>
      <div class="security-item">
        <div>
          <strong>绑定邮箱</strong>
          <p>{{ form.email || '未绑定' }}</p>
        </div>
        <span class="sec-status">{{ form.email ? '已绑定' : '待绑定' }}</span>
      </div>
    </div>

    <!-- 改密码弹窗 -->
    <el-dialog v-model="showPwdDialog" title="修改密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位字符" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false">取消</el-button>
        <el-button type="primary" @click="doChangePwd" :loading="pwdChanging">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 720px; margin: 0 auto; }
.page h2 { color: #1A3C6E; margin-bottom: 20px; }

/* 个人信息卡片 */
.profile-card {
  background: linear-gradient(135deg, #1A3C6E, #3B6FB6);
  border-radius: 16px; padding: 28px 32px;
  display: flex; align-items: center; gap: 24px;
  color: #fff; margin-bottom: 24px;
}
.avatar-section { text-align: center; }
.avatar-preview {
  width: 72px; height: 72px; border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  font-size: 30px; font-weight: 700; overflow: hidden;
  margin-bottom: 8px;
}
.avatar-preview img { width: 100%; height: 100%; object-fit: cover; }
.profile-summary h3 { margin: 0 0 4px; font-size: 22px; }
.profile-summary p { margin: 2px 0; opacity: 0.8; font-size: 14px; }
.summary-meta { font-size: 13px !important; opacity: 0.6 !important; }

/* 通用卡片 */
.section {
  background: #fff; border-radius: 12px; padding: 24px;
  margin-bottom: 20px; border: 1px solid #EEF0F4;
}
.section h3 { color: #1A3C6E; font-size: 16px; margin: 0 0 18px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; }

/* 安全选项 */
.security-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 0; border-bottom: 1px solid #f5f5f5;
}
.security-item:last-child { border-bottom: none; }
.security-item strong { font-size: 14px; color: #333; }
.security-item p { margin: 2px 0 0; font-size: 12px; color: #999; }
.sec-status { font-size: 13px; color: #67C23A; }
</style>
