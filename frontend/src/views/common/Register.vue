<template>
  <div class="register-page">
    <div class="bg-decoration" aria-hidden="true">
      <div class="leaf leaf-1">🍃</div>
      <div class="leaf leaf-2">🌿</div>
      <div class="leaf leaf-3">🌱</div>
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="register-card" role="main" aria-label="注册">
      <div class="brand">
        <div class="brand-logo" aria-hidden="true">
          <span class="logo-leaf">🍃</span>
        </div>
        <p class="welcome-en">CREATE ACCOUNT</p>
        <h1 class="title">社区内临期食品流转及减损系统</h1>
        <p class="subtitle">创建账号，立即开始低碳生活</p>
      </div>

      <form class="form" @submit.prevent="handleRegister">
        <div class="grid-row">
          <div class="field" :class="{ focused: focusedField === 'username' }">
            <input
              v-model.trim="registerForm.username"
              type="text"
              class="input"
              placeholder="请输入用户名"
              autocomplete="username"
              @focus="focusedField = 'username'"
              @blur="focusedField = ''"
              required
            />
          </div>
          <div class="field" :class="{ focused: focusedField === 'nickName' }">
            <input
              v-model.trim="registerForm.nickName"
              type="text"
              class="input"
              placeholder="请输入昵称"
              @focus="focusedField = 'nickName'"
              @blur="focusedField = ''"
              required
            />
          </div>
        </div>

        <div class="grid-row">
          <div class="field" :class="{ focused: focusedField === 'password' }">
            <input
              v-model="registerForm.password"
              :type="showPassword ? 'text' : 'password'"
              class="input"
              placeholder="请输入密码（至少6位）"
              autocomplete="new-password"
              @focus="focusedField = 'password'"
              @blur="focusedField = ''"
              required
            />
            <button type="button" class="eye-btn" @click="showPassword = !showPassword" aria-label="显示或隐藏密码">
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
          <div class="field" :class="{ focused: focusedField === 'confirmPassword' }">
            <input
              v-model="registerForm.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="input"
              placeholder="请确认密码"
              autocomplete="new-password"
              @focus="focusedField = 'confirmPassword'"
              @blur="focusedField = ''"
              required
            />
            <button
              type="button"
              class="eye-btn"
              @click="showConfirmPassword = !showConfirmPassword"
              aria-label="显示或隐藏确认密码"
            >
              <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>

        <p v-if="registerForm.confirmPassword && registerForm.confirmPassword !== registerForm.password" class="inline-error">
          两次输入密码不一致
        </p>

        <div class="grid-row">
          <div class="field" :class="{ focused: focusedField === 'phonenumber' }">
            <input
              v-model.trim="registerForm.phonenumber"
              type="text"
              class="input"
              placeholder="请输入手机号（可选）"
              @focus="focusedField = 'phonenumber'"
              @blur="focusedField = ''"
            />
          </div>
          <div class="type-selector">
            <button type="button" class="type-btn" :class="{ active: registerForm.userType === 1 }" @click="registerForm.userType = 1">
              居民
            </button>
            <button type="button" class="type-btn" :class="{ active: registerForm.userType === 2 }" @click="registerForm.userType = 2">
              商户
            </button>
          </div>
        </div>

        <div v-if="registerForm.userType === 2" class="community-row">
          <span class="community-label">所属社区</span>
          <select v-model="registerForm.merchantCommunityId" class="community-select">
            <option :value="null" disabled>请选择社区</option>
            <option v-for="c in communities" :key="c.communityId" :value="c.communityId">
              {{ c.communityName }}
            </option>
          </select>
        </div>

        <button class="register-btn" type="submit" :disabled="loading">
          <span v-if="loading">注册中...</span>
          <span v-else-if="registerSuccess">注册成功，跳转登录...</span>
          <span v-else>注册</span>
        </button>
      </form>

      <div class="login-row">
        <span class="muted">已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>

    <transition name="fade">
      <div v-if="errorMsg" class="error-toast" role="alert">
        <span aria-hidden="true">❌</span>
        <span>{{ errorMsg }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getRegisterCommunities } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const errorMsg = ref('')
const registerSuccess = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const focusedField = ref('')
const communities = ref([])

const registerForm = reactive({
  username: '',
  nickName: '',
  password: '',
  confirmPassword: '',
  phonenumber: '',
  userType: 1,
  merchantCommunityId: null,
  merchantCommunityCode: ''
})

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.nickName || !registerForm.password || !registerForm.confirmPassword) {
    errorMsg.value = '请填写必填项'
    setTimeout(() => { errorMsg.value = '' }, 2200)
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    errorMsg.value = '两次输入密码不一致'
    setTimeout(() => { errorMsg.value = '' }, 2200)
    return
  }
  if (registerForm.password.length < 6) {
    errorMsg.value = '密码长度至少6位'
    setTimeout(() => { errorMsg.value = '' }, 2200)
    return
  }
  if (registerForm.userType === 2 && !registerForm.merchantCommunityId) {
    errorMsg.value = '请选择商户所属社区'
    setTimeout(() => { errorMsg.value = '' }, 2200)
    return
  }

  loading.value = true
  errorMsg.value = ''
  try {
    const payload = { ...registerForm }
    if (payload.userType === 2) {
      const selected = communities.value.find(c => Number(c.communityId) === Number(payload.merchantCommunityId))
      payload.merchantCommunityCode = selected?.communityCode || ''
    } else {
      payload.merchantCommunityCode = ''
      payload.merchantCommunityId = null
    }
    await userStore.registerAction(payload)
    registerSuccess.value = true
    setTimeout(() => router.push('/login'), 800)
  } catch (error) {
    errorMsg.value = error?.message || '注册失败，请稍后重试'
    setTimeout(() => { errorMsg.value = '' }, 2600)
  } finally {
    loading.value = false
  }
}

const loadCommunities = async () => {
  try {
    const res = await getRegisterCommunities()
    const raw = res.data || []
    const list = raw
      .map((c) => ({
        communityId: c?.communityId ?? c?.community_id ?? c?.id ?? null,
        communityName: c?.communityName ?? c?.community_name ?? '',
        communityCode: c?.communityCode ?? c?.community_code ?? ''
      }))
      .filter(c => c.communityId && c.communityName)
    communities.value = list
    if (!registerForm.merchantCommunityId && list.length > 0) {
      registerForm.merchantCommunityId = list[0].communityId
    }
  } catch (e) {
    communities.value = []
  }
}

onMounted(() => {
  loadCommunities()
})
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 50%, #fff8e1 100%);
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.leaf {
  position: absolute;
  font-size: 2.2em;
  opacity: 0.2;
  animation: float 6s ease-in-out infinite;
}
.leaf-1 { top: 10%; left: 6%; animation-delay: 0s; }
.leaf-2 { top: 60%; right: 12%; animation-delay: 1s; }
.leaf-3 { bottom: 20%; left: 16%; animation-delay: 2s; }

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}
.circle-1 { width: 420px; height: 420px; background: #4caf50; top: -160px; right: -120px; }
.circle-2 { width: 300px; height: 300px; background: #8bc34a; bottom: -130px; left: 22%; }
.circle-3 { width: 220px; height: 220px; background: #f39c12; top: 16%; left: -70px; opacity: 0.06; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-18px) rotate(5deg); }
}

.register-card {
  width: min(700px, 95vw);
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(214, 229, 216, 0.9);
  border-radius: 22px;
  padding: 50px 40px;
  backdrop-filter: blur(14px);
  box-shadow:
    0 22px 60px rgba(18, 58, 37, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.72);
  position: relative;
  z-index: 1;
}

.brand {
  display: grid;
  justify-items: center;
  text-align: center;
  gap: 8px;
  margin-bottom: 18px;
}

.brand-logo {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: linear-gradient(145deg, rgba(26, 109, 69, 0.18), rgba(15, 79, 49, 0.08));
  display: grid;
  place-items: center;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.6),
    0 10px 22px rgba(15, 73, 41, 0.12);
}

.logo-leaf { font-size: 1.25rem; }

.welcome-en {
  margin: 0;
  font-size: 0.78rem;
  font-weight: 900;
  letter-spacing: 0.34em;
  color: #66bb6a;
  text-indent: 0.34em;
}

.title {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 900;
  color: #2e7d32;
  line-height: 1.25;
}

.subtitle {
  margin: 0;
  font-size: 0.95rem;
  color: #4b6a58;
}

.form {
  display: grid;
  gap: 14px;
  margin-top: 18px;
}

.grid-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.field {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(220, 231, 223, 1);
  background: rgba(250, 252, 250, 0.75);
  transition: all 0.22s ease;
}

.field.focused {
  border-color: rgba(103, 171, 120, 1);
  box-shadow: 0 0 0 4px rgba(103, 171, 120, 0.12);
  background: #fff;
}

.input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 1.1rem;
  color: #27392e;
}

.input::placeholder { color: #9db0a2; }

.eye-btn {
  border: none;
  background: transparent;
  color: #5f7768;
  cursor: pointer;
  padding: 0 6px;
  transition: transform 120ms ease;
}
.eye-btn:hover { transform: translateY(-1px); }

.inline-error {
  margin: -2px 4px 2px;
  color: #c62828;
  font-size: 0.9rem;
}

.type-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.type-btn {
  border: 1px solid rgba(220, 231, 223, 1);
  border-radius: 14px;
  background: rgba(250, 252, 250, 0.75);
  color: #4b6a58;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.type-btn.active {
  background: linear-gradient(135deg, #56b36c, #2f7d41);
  color: #fff;
  border-color: transparent;
}

.community-row {
  display: grid;
  gap: 10px;
}

.community-label {
  color: #4b6a58;
  font-size: 0.9rem;
}

.community-select {
  height: 48px;
  border: 1px solid rgba(220, 231, 223, 1);
  border-radius: 14px;
  background: rgba(250, 252, 250, 0.75);
  color: #27392e;
  font-size: 1rem;
  padding: 0 14px;
  outline: none;
}

.community-select:focus {
  border-color: rgba(103, 171, 120, 1);
  box-shadow: 0 0 0 4px rgba(103, 171, 120, 0.12);
}

.register-btn {
  margin-top: 14px;
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  font-size: 1.1rem;
  font-weight: 900;
  cursor: pointer;
  background: linear-gradient(135deg, #56b36c, #2f7d41);
  color: #fff;
  box-shadow:
    0 14px 28px rgba(45, 122, 65, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.register-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow:
    0 18px 34px rgba(45, 122, 65, 0.34),
    inset 0 1px 0 rgba(255, 255, 255, 0.48);
}

.register-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: inset 0 4px 12px rgba(0, 0, 0, 0.12);
}

.login-row {
  margin-top: 20px;
  text-align: center;
  font-size: 0.95rem;
  color: #4b6a58;
}

.muted { margin-right: 6px; }

.login-link {
  color: #2e7d32;
  font-weight: 900;
  text-decoration: none;
}

.login-link:hover { text-decoration: underline; }

.error-toast {
  position: fixed;
  top: 28px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 12px 18px;
  border-radius: 12px;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.14);
  z-index: 2000;
  display: flex;
  gap: 10px;
  align-items: center;
  color: #c62828;
  font-weight: 800;
}

.fade-enter-active,
.fade-leave-active { transition: all 0.25s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; transform: translateX(-50%) translateY(-10px); }

@media (max-width: 540px) {
  .register-card { padding: 32px 20px; }
  .title { font-size: 1.2rem; }
  .grid-row { grid-template-columns: 1fr; }
}
</style>
