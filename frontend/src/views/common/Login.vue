<template>
  <div class="login-page">
    <div class="bg-decoration" aria-hidden="true">
      <div class="leaf leaf-1">🍃</div>
      <div class="leaf leaf-2">🌿</div>
      <div class="leaf leaf-3">🌱</div>
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-card" role="main" aria-label="登录">
      <div class="brand">
        <div class="brand-logo" aria-hidden="true">
          <span class="logo-leaf">🍃</span>
        </div>
        <p class="welcome-en">WELCOME BACK</p>
        <h1 class="title">社区临期食品低碳减损系统</h1>
        <p class="subtitle">减少浪费，守护地球</p>
      </div>

      <form class="form" @submit.prevent="handleLogin">
        <div class="field" :class="{ focused: isUsernameFocused }">
          <i class="fas fa-user input-icon" aria-hidden="true"></i>
          <input
            ref="usernameInput"
            v-model="loginForm.username"
            type="text"
            class="input"
            placeholder="请输入用户名"
            autocomplete="username"
            @focus="isUsernameFocused = true"
            @blur="isUsernameFocused = false"
            required
          />
        </div>

        <div class="field" :class="{ focused: isPasswordFocused }">
          <i class="fas fa-lock input-icon" aria-hidden="true"></i>
          <input
            ref="passwordInput"
            v-model="loginForm.password"
            :type="showPassword ? 'text' : 'password'"
            class="input"
            placeholder="请输入密码"
            autocomplete="current-password"
            @focus="isPasswordFocused = true"
            @blur="isPasswordFocused = false"
            required
          />
          <button type="button" class="eye-btn" @click="togglePassword" aria-label="显示/隐藏密码">
            <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
          </button>
        </div>

        <div class="meta">
          <label class="remember">
            <input type="checkbox" v-model="rememberMe" />
            <span>记住我</span>
          </label>
          <a href="javascript:void(0)" class="forgot" @click.prevent="mockForgot">忘记密码</a>
        </div>

        <button class="login-btn" type="submit" :disabled="loading">
          <span v-if="loading">登录中...</span>
          <span v-else>登录</span>
        </button>
      </form>

      <div class="register-row">
        <span class="muted">还没有账号？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>

    <transition name="fade">
      <div v-if="errorMsg" class="error-toast" role="alert">
        <span class="x" aria-hidden="true">❌</span>
        <span>{{ errorMsg }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const errorMsg = ref('')
const showPassword = ref(false)
const rememberMe = ref(true)
const isUsernameFocused = ref(false)
const isPasswordFocused = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const usernameInput = ref(null)
const passwordInput = ref(null)

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const mockForgot = () => {
  errorMsg.value = '演示版本：请联系管理员重置密码'
  setTimeout(() => { errorMsg.value = '' }, 2200)
}

const handleLogin = async () => {
  if (!String(loginForm.username).trim() || !loginForm.password) {
    errorMsg.value = '请输入用户名和密码'
    setTimeout(() => { errorMsg.value = '' }, 2200)
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const username = String(loginForm.username).trim()
    const password = String(loginForm.password)
    await userStore.loginAction(username, password)

    const userType = userStore.userType
    if (userType === 1) router.push('/consumer/products')
    else if (userType === 2) router.push('/merchant/dashboard')
    else if (userType === 3) router.push('/admin/dashboard')
    else router.push('/consumer/products')
  } catch (e) {
    errorMsg.value = e?.message || '登录失败，请检查用户名和密码'
    setTimeout(() => { errorMsg.value = '' }, 2600)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
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

.login-card {
  width: min(520px, 92vw);
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(214, 229, 216, 0.9);
  border-radius: 22px;
  padding: 42px 34px;
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
  font-size: 1.4rem;
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

.field {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
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

.input-icon {
  color: #4b6a58;
  opacity: 0.9;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 1rem;
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

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 2px;
  gap: 12px;
}

.remember {
  display: inline-flex;
  gap: 10px;
  align-items: center;
  color: #51655a;
  font-size: 0.92rem;
}
.remember input { accent-color: #2e7d32; }

.forgot {
  color: #3f9155;
  text-decoration: none;
  font-weight: 700;
  font-size: 0.92rem;
}
.forgot:hover { text-decoration: underline; }

.login-btn {
  margin-top: 4px;
  width: 100%;
  border: none;
  border-radius: 14px;
  padding: 14px 16px;
  font-size: 1.05rem;
  font-weight: 900;
  cursor: pointer;
  background: linear-gradient(135deg, #5ab46f, #2f7d41);
  color: #fff;
  box-shadow:
    0 14px 28px rgba(45, 122, 65, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  transition: all 0.22s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow:
    0 18px 34px rgba(45, 122, 65, 0.34),
    inset 0 1px 0 rgba(255, 255, 255, 0.48);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: inset 0 4px 12px rgba(0, 0, 0, 0.12);
}

.login-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.register-row {
  margin-top: 22px;
  text-align: center;
  font-size: 0.95rem;
  color: #51655a;
}

.muted { opacity: 0.9; margin-right: 6px; }
.register-link {
  color: #3f9155;
  font-weight: 900;
  text-decoration: none;
}
.register-link:hover { text-decoration: underline; }

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
  .login-card { padding: 32px 20px; }
  .title { font-size: 1.2rem; }
}
</style>

