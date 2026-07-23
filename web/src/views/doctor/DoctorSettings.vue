<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateProfile, changePassword } from '@/api/auth'

const form = ref({ realName: '', phone: '', email: '', avatarUrl: '' })
const saving = ref(false)

// 头像预览
const avatarPreview = ref('')

// 修改密码
const showPwdDialog = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdChanging = ref(false)

const router = useRouter()

onMounted(async () => {
  try {
    const res = await getUserInfo()
    if (res.data) {
      form.value.realName = res.data.realName || ''
      form.value.phone = res.data.phone || ''
      form.value.email = res.data.email || ''
      form.value.avatarUrl = res.data.avatarUrl || ''
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
    // 刷新 localStorage
    const res = await getUserInfo()
    if (res.data) {
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
    ElMessage.success('个人资料已更新')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleAvatarUrl() {
  // 输入图片URL
  ElMessageBox.prompt('请输入头像图片URL', '更换头像', {
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
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (pwdForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度至少6位')
    return
  }
  pwdChanging.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码已修改，请重新登录')
    showPwdDialog.value = false
    setTimeout(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }, 500)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  } finally {
    pwdChanging.value = false
  }
}

// 需要引入 ElMessageBox
import { ElMessageBox } from 'element-plus'
</script>

<template>
  <div class="page">
    <h2>⚙️ 个人设置</h2>

    <!-- 个人资料编辑 -->
    <div class="section">
      <h3>个人资料</h3>
      <el-form :model="form" label-width="80px" style="max-width:480px">
        <el-form-item label="头像">
          <div class="avatar-preview" v-if="avatarPreview">
            <img :src="avatarPreview" alt="avatar" class="avatar-img" @error="avatarPreview = ''" />
          </div>
          <div class="avatar-preview" v-else>
            <span>{{ form.realName?.charAt(0) || '👤' }}</span>
          </div>
          <el-button size="small" text type="primary" style="margin-left:12px" @click="handleAvatarUrl">输入图片链接</el-button>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 账号安全 -->
    <div class="section">
      <h3>账号安全</h3>
      <div class="security-item">
        <span>登录密码</span>
        <el-button size="small" text type="primary" @click="handleChangePwd">修改密码</el-button>
      </div>
      <div class="security-item">
        <span>绑定手机</span>
        <span class="sec-status">{{ form.phone || '未绑定' }}</span>
      </div>
      <div class="security-item">
        <span>绑定邮箱</span>
        <span class="sec-status">{{ form.email || '未绑定' }}</span>
      </div>
    </div>

    <!-- 改密码弹窗 -->
    <el-dialog v-model="showPwdDialog" title="修改密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
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
.page { padding: 24px; max-width: 720px; }
.page h2 { color: #1A3C6E; margin-bottom: 20px; }
.section { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 20px; }
.section h3 { color: #1A3C6E; font-size: 16px; margin: 0 0 16px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; }
.avatar-preview {
  width: 48px; height: 48px; border-radius: 50%;
  background: #1A3C6E; color: #fff;
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 20px; overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.security-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 0; border-bottom: 1px solid #f5f5f5;
}
.security-item:last-child { border-bottom: none; }
.sec-status { color: #999; font-size: 13px; }
</style>
