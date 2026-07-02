<template>
  <div class="login-page">
    <div class="bg-layer" aria-hidden="true">
      <div class="bg-mesh"></div>
      <div class="bg-grid"></div>
      <svg class="bg-scene" viewBox="0 0 1440 900" preserveAspectRatio="xMidYMid slice">
        <defs>
          <linearGradient id="flowGrad" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#3d9a5c" stop-opacity="0" />
            <stop offset="50%" stop-color="#52b788" stop-opacity="0.45" />
            <stop offset="100%" stop-color="#3d9a5c" stop-opacity="0" />
          </linearGradient>
          <radialGradient id="glowA" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stop-color="#7bc492" stop-opacity="0.35" />
            <stop offset="100%" stop-color="#7bc492" stop-opacity="0" />
          </radialGradient>
        </defs>
        <!-- 社区节点与流转路径 -->
        <circle cx="180" cy="220" r="6" fill="#2d8c54" fill-opacity="0.25" />
        <circle cx="320" cy="180" r="5" fill="#2d8c54" fill-opacity="0.2" />
        <circle cx="260" cy="320" r="7" fill="#2d8c54" fill-opacity="0.22" />
        <circle cx="1180" cy="260" r="6" fill="#2d8c54" fill-opacity="0.2" />
        <circle cx="1260" cy="400" r="5" fill="#2d8c54" fill-opacity="0.18" />
        <circle cx="1100" cy="620" r="7" fill="#2d8c54" fill-opacity="0.2" />
        <path
          d="M180 220 Q520 120 720 450 T1180 260"
          fill="none"
          stroke="url(#flowGrad)"
          stroke-width="2"
          stroke-dasharray="8 10"
          class="flow-path"
        />
        <path
          d="M260 320 Q600 480 720 450 T1100 620"
          fill="none"
          stroke="url(#flowGrad)"
          stroke-width="1.5"
          stroke-dasharray="6 12"
          class="flow-path flow-path-delay"
        />
        <!-- 社区楼宇剪影 -->
        <g fill="#2d7d47" fill-opacity="0.08" class="buildings-left">
          <rect x="80" y="520" width="48" height="120" rx="4" />
          <rect x="140" y="480" width="56" height="160" rx="4" />
          <rect x="210" y="540" width="42" height="100" rx="4" />
          <rect x="110" y="500" width="24" height="24" rx="2" fill-opacity="0.12" />
        </g>
        <g fill="#2d7d47" fill-opacity="0.07" class="buildings-right">
          <rect x="1150" y="500" width="52" height="140" rx="4" />
          <rect x="1220" y="460" width="44" height="180" rx="4" />
          <rect x="1288" y="530" width="60" height="110" rx="4" />
        </g>
        <!-- 食品/绿叶装饰 -->
        <ellipse cx="120" cy="140" rx="90" ry="70" fill="url(#glowA)" />
        <ellipse cx="1320" cy="720" rx="110" ry="85" fill="url(#glowA)" />
        <path
          d="M1050 150c-20 35-55 48-85 42 8-28 42-52 85-42z"
          fill="#52b788"
          fill-opacity="0.12"
          class="float-leaf"
        />
        <path
          d="M340 680c-18 30-48 40-72 34 6-24 36-44 72-34z"
          fill="#3d9a5c"
          fill-opacity="0.1"
          class="float-leaf float-leaf-alt"
        />
        <!-- 购物袋/临期品意象 -->
        <g transform="translate(200 120)" fill="#2d8c54" fill-opacity="0.1" class="float-bag">
          <path d="M8 14h24l-3 28H11L8 14z" />
          <path d="M14 14V10a6 6 0 0 1 12 0v4" stroke="#2d8c54" stroke-width="2" fill="none" stroke-opacity="0.15" />
        </g>
        <g transform="translate(1180 640) scale(1.2)" fill="#2d8c54" fill-opacity="0.09" class="float-bag float-bag-alt">
          <path d="M8 14h24l-3 28H11L8 14z" />
          <path d="M14 14V10a6 6 0 0 1 12 0v4" stroke="#2d8c54" stroke-width="2" fill="none" stroke-opacity="0.12" />
        </g>
      </svg>
    </div>

    <div class="login-card" role="main" aria-label="登录">
      <div class="brand">
        <div class="brand-logo" aria-hidden="true">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M10 22h28v16a4 4 0 0 1-4 4H14a4 4 0 0 1-4-4V22z"
              fill="white"
              fill-opacity="0.95"
            />
            <path
              d="M8 22l16-10 16 10"
              stroke="white"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M30 12c2 4 6 5 8 8-4 1-7-3-8-8z"
              fill="white"
              fill-opacity="0.9"
            />
          </svg>
        </div>
        <p class="welcome-en">WELCOME BACK</p>
        <h1 class="title">社区内临期食品流转及减损系统</h1>
        <span class="title-leaf" aria-hidden="true">🍃</span>
        <p class="subtitle">减少浪费，守护地球</p>
      </div>

      <form class="form" @submit.prevent="handleLogin">
        <div class="field" :class="{ focused: isUsernameFocused }">
          <input
            ref="usernameInput"
            v-model="loginForm.username"
            type="text"
            class="input"
            placeholder="用户名"
            autocomplete="username"
            @focus="isUsernameFocused = true"
            @blur="isUsernameFocused = false"
            required
          />
        </div>

        <div class="field" :class="{ focused: isPasswordFocused }">
          <input
            ref="passwordInput"
            v-model="loginForm.password"
            :type="showPassword ? 'text' : 'password'"
            class="input"
            placeholder="密码"
            autocomplete="current-password"
            @focus="isPasswordFocused = true"
            @blur="isPasswordFocused = false"
            required
          />
          <button type="button" class="eye-btn" @click="togglePassword" aria-label="显示/隐藏密码">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8">
              <template v-if="!showPassword">
                <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" />
                <circle cx="12" cy="12" r="3" />
              </template>
              <template v-else>
                <path d="M3 3l18 18M10.5 10.7A3 3 0 0 0 12 15a3 3 0 0 0 2.3-1M6.2 6.2C4.2 7.8 2.8 9.9 2 12s3.5 7 10 7c2 0 3.8-.6 5.3-1.6M17.8 17.8C19.8 16.2 21.2 14.1 22 12s-3.5-7-10-7c-2 0-3.8.6-5.3 1.6" />
              </template>
            </svg>
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
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.bg-mesh {
  position: absolute;
  inset: -40%;
  background:
    radial-gradient(ellipse 55% 45% at 15% 25%, rgba(125, 196, 148, 0.45) 0%, transparent 55%),
    radial-gradient(ellipse 50% 40% at 85% 15%, rgba(180, 220, 165, 0.4) 0%, transparent 50%),
    radial-gradient(ellipse 60% 50% at 75% 85%, rgba(220, 200, 170, 0.35) 0%, transparent 55%),
    radial-gradient(ellipse 45% 40% at 10% 75%, rgba(160, 210, 175, 0.38) 0%, transparent 50%),
    linear-gradient(155deg, #e4efe6 0%, #dce9d5 35%, #ebe8df 68%, #e8f0ea 100%);
  animation: mesh-drift 18s ease-in-out infinite alternate;
}

@keyframes mesh-drift {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(-2%, 1.5%) scale(1.04); }
}

.bg-grid {
  position: absolute;
  inset: 0;
  opacity: 0.4;
  background-image:
    linear-gradient(rgba(45, 125, 65, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(45, 125, 65, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 70% 65% at 50% 45%, black 20%, transparent 75%);
}

.bg-scene {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.flow-path {
  animation: flow-dash 22s linear infinite;
}

.flow-path-delay {
  animation-duration: 28s;
  animation-direction: reverse;
}

@keyframes flow-dash {
  to { stroke-dashoffset: -120; }
}

.float-leaf,
.float-bag,
.buildings-left,
.buildings-right {
  animation: float-y 9s ease-in-out infinite;
}

.float-leaf-alt { animation-delay: -3s; }
.float-bag { animation-delay: -1.5s; }
.float-bag-alt { animation-delay: -4.5s; }
.buildings-right { animation-delay: -2s; }

@keyframes float-y {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.login-card {
  width: min(620px, 94vw);
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24px;
  padding: 52px 48px 44px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow:
    0 24px 56px rgba(25, 70, 45, 0.14),
    0 0 0 1px rgba(82, 160, 106, 0.08);
  position: relative;
  z-index: 2;
  backdrop-filter: blur(6px);
}

.brand {
  display: grid;
  justify-items: center;
  text-align: center;
  gap: 8px;
  margin-bottom: 36px;
}

.brand-logo {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(145deg, #3d9a5c, #2d7d47);
  display: grid;
  place-items: center;
  margin-bottom: 8px;
  box-shadow: 0 8px 20px rgba(45, 125, 65, 0.25);
}

.brand-logo svg {
  width: 36px;
  height: 36px;
}

.welcome-en {
  margin: 0;
  font-size: 0.82rem;
  font-weight: 600;
  letter-spacing: 0.28em;
  color: #9aa89e;
  text-indent: 0.28em;
}

.title {
  margin: 4px 0 0;
  font-size: 1.75rem;
  font-weight: 800;
  color: #2d7d47;
  line-height: 1.4;
  letter-spacing: 0.02em;
}

.title-leaf {
  font-size: 1.05rem;
  line-height: 1;
  margin: 4px 0;
  opacity: 0.85;
}

.subtitle {
  margin: 0;
  font-size: 1.02rem;
  color: #52a06a;
  font-weight: 500;
}

.form {
  display: grid;
  gap: 18px;
}

.field {
  display: flex;
  align-items: center;
  padding: 0 18px;
  height: 58px;
  border-radius: 14px;
  border: 1px solid #e8ece9;
  background: #f5f7f5;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.field.focused {
  border-color: #7bc492;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(82, 160, 106, 0.15);
}

.input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 1.08rem;
  color: #2c3e30;
}

.input::placeholder {
  color: #a8b5ac;
}

.eye-btn {
  border: none;
  background: transparent;
  color: #8a9a90;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.eye-btn:hover {
  color: #2d7d47;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 2px;
  gap: 12px;
}

.remember {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: #5c6d62;
  font-size: 0.96rem;
  cursor: pointer;
}

.remember input {
  width: 16px;
  height: 16px;
  accent-color: #2d7d47;
  cursor: pointer;
}

.forgot {
  color: #2d7d47;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.96rem;
}

.forgot:hover {
  text-decoration: underline;
}

.login-btn {
  margin-top: 10px;
  width: 100%;
  height: 56px;
  border: none;
  border-radius: 14px;
  font-size: 1.12rem;
  font-weight: 700;
  cursor: pointer;
  background: #2d8c54;
  color: #fff;
  box-shadow: 0 10px 22px rgba(45, 140, 84, 0.32);
  transition: background 0.2s ease, transform 0.15s ease, box-shadow 0.2s ease;
}

.login-btn:hover:not(:disabled) {
  background: #267348;
  transform: translateY(-1px);
  box-shadow: 0 12px 26px rgba(45, 140, 84, 0.38);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.register-row {
  margin-top: 28px;
  text-align: center;
  font-size: 1rem;
  color: #8a9690;
}

.muted {
  margin-right: 4px;
}

.register-link {
  color: #2d8c54;
  font-weight: 700;
  text-decoration: none;
}

.register-link:hover {
  text-decoration: underline;
}

.error-toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  padding: 12px 20px;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 2000;
  color: #c62828;
  font-weight: 600;
  font-size: 0.9rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}

@media (max-width: 900px) {
  .buildings-left,
  .buildings-right {
    opacity: 0.5;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 36px 24px 32px;
  }

  .title {
    font-size: 1.35rem;
  }

  .field {
    height: 54px;
  }

  .bg-scene {
    opacity: 0.65;
  }
}

@media (prefers-reduced-motion: reduce) {
  .bg-mesh,
  .flow-path,
  .float-leaf,
  .float-bag,
  .buildings-left,
  .buildings-right {
    animation: none;
  }
}
</style>
