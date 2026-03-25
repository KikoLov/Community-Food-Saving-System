<template>
  <div class="login-container" @mousemove="handleMouseMove">
    <div class="bg-decoration">
      <div class="leaf leaf-1">🍃</div>
      <div class="leaf leaf-2">🌿</div>
      <div class="leaf leaf-3">🌱</div>
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-wrapper">
      <div class="characters-section">
        <h2 class="section-title">守护者们</h2>
        <div
          class="characters-row"
          :class="{
            'characters-row--peek-account': characterPose === 'peek',
            'characters-row--hide-password': characterPose === 'hide'
          }"
        >
          <div class="character" :class="{ active: activeCharacter === 'purple' }">
            <div class="character-body purple" :style="getCharacterStyle('purple')">
              <div class="head" :style="getHeadStyle()">
                <div class="face">
                  <div class="cover-hands" aria-hidden="true">
                    <span class="cover-hand cover-hand--left"></span>
                    <span class="cover-hand cover-hand--right"></span>
                  </div>
                  <div class="eyes">
                    <div class="eye left" :style="getEyeStyle('left')">
                      <div class="pupil" :style="getPupilStyle()"></div>
                    </div>
                    <div class="eye right" :style="getEyeStyle('right')">
                      <div class="pupil" :style="getPupilStyle()"></div>
                    </div>
                  </div>
                  <div class="mouth" :class="mouthClass"></div>
                </div>
                <div class="blush" v-if="activeCharacter === 'purple'"></div>
              </div>
              <div class="body-shape"></div>
            </div>
          </div>

          <div class="character" :class="{ active: activeCharacter === 'black' }">
            <div class="character-body black" :style="getCharacterStyle('black')">
              <div class="head" :style="getHeadStyle()">
                <div class="face">
                  <div class="cover-hands" aria-hidden="true">
                    <span class="cover-hand cover-hand--left"></span>
                    <span class="cover-hand cover-hand--right"></span>
                  </div>
                  <div class="eyes">
                    <div class="eye left" :style="getEyeStyle('left')">
                      <div class="pupil" :style="getPupilStyle()"></div>
                    </div>
                    <div class="eye right" :style="getEyeStyle('right')">
                      <div class="pupil" :style="getPupilStyle()"></div>
                    </div>
                  </div>
                  <div class="mouth" :class="mouthClass"></div>
                </div>
                <div class="blush" v-if="activeCharacter === 'black'"></div>
              </div>
              <div class="body-shape"></div>
            </div>
          </div>

          <div class="character" :class="{ active: activeCharacter === 'yellow' }">
            <div class="character-body yellow" :style="getCharacterStyle('yellow')">
              <div class="head" :style="getHeadStyle()">
                <div class="face">
                  <div class="cover-hands" aria-hidden="true">
                    <span class="cover-hand cover-hand--left"></span>
                    <span class="cover-hand cover-hand--right"></span>
                  </div>
                  <div class="eyes">
                    <div class="eye left" :style="getEyeStyle('left')">
                      <div class="pupil" :style="getPupilStyle()"></div>
                    </div>
                    <div class="eye right" :style="getEyeStyle('right')">
                      <div class="pupil" :style="getPupilStyle()"></div>
                    </div>
                  </div>
                  <div class="mouth" :class="mouthClass"></div>
                </div>
                <div class="blush" v-if="activeCharacter === 'yellow'"></div>
              </div>
              <div class="body-shape"></div>
            </div>
          </div>

          <div class="character" :class="{ active: activeCharacter === 'orange' }">
            <div class="character-body orange" :style="getCharacterStyle('orange')">
              <div class="head" :style="getHeadStyle()">
                <div class="face">
                  <div class="cover-hands" aria-hidden="true">
                    <span class="cover-hand cover-hand--left"></span>
                    <span class="cover-hand cover-hand--right"></span>
                  </div>
                  <div class="eyes">
                    <div class="eye left" :style="getEyeStyle('left')">
                      <div class="pupil" :style="getPupilStyle()"></div>
                    </div>
                    <div class="eye right" :style="getEyeStyle('right')">
                      <div class="pupil" :style="getPupilStyle()"></div>
                    </div>
                  </div>
                  <div class="mouth" :class="mouthClass"></div>
                </div>
                <div class="blush" v-if="activeCharacter === 'orange'"></div>
              </div>
              <div class="body-shape"></div>
            </div>
          </div>
        </div>
      </div>

      <div class="login-section">
        <div class="login-card">
          <div class="login-header">
            <div class="logo">🌿</div>
            <h1>社区临期食品低碳减损系统</h1>
            <p class="subtitle">减少浪费，守护地球</p>
          </div>

          <form @submit.prevent="handleRegister" class="login-form">
            <div class="form-group">
              <label class="form-label">
                <span class="label-icon">👤</span>
                用户名
              </label>
              <div class="input-wrapper" :class="{ focused: focusedField === 'username' }">
                <input
                  ref="usernameInput"
                  v-model="registerForm.username"
                  type="text"
                  class="form-input"
                  placeholder="请输入用户名"
                  @focus="handleFocus('username')"
                  @blur="handleFieldBlur"
                  @input="handleInput"
                  required
                >
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">
                <span class="label-icon">😊</span>
                昵称
              </label>
              <div class="input-wrapper" :class="{ focused: focusedField === 'nickName' }">
                <input
                  ref="nickNameInput"
                  v-model="registerForm.nickName"
                  type="text"
                  class="form-input"
                  placeholder="请输入昵称"
                  @focus="handleFocus('nickName')"
                  @blur="handleFieldBlur"
                  @input="handleInput"
                  required
                >
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">
                <span class="label-icon">🔒</span>
                密码
              </label>
              <div class="input-wrapper" :class="{ focused: focusedField === 'password' }">
                <input
                  ref="passwordInput"
                  v-model="registerForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-input"
                  placeholder="请输入密码"
                  @focus="handleFocus('password')"
                  @blur="handleFieldBlur"
                  @input="handleInput"
                  required
                >
                <button
                  type="button"
                  class="password-toggle"
                  @click="togglePassword('password')"
                >
                  {{ showPassword ? '🙈' : '👁' }}
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">
                <span class="label-icon">🔐</span>
                确认密码
              </label>
              <div class="input-wrapper" :class="{ focused: focusedField === 'confirmPassword' }">
                <input
                  ref="confirmPasswordInput"
                  v-model="registerForm.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  class="form-input"
                  placeholder="请确认密码"
                  @focus="handleFocus('confirmPassword')"
                  @blur="handleFieldBlur"
                  @input="handleInput"
                  required
                >
                <button
                  type="button"
                  class="password-toggle"
                  @click="togglePassword('confirmPassword')"
                >
                  {{ showConfirmPassword ? '🙈' : '👁' }}
                </button>
              </div>
            </div>

            <p v-if="registerForm.confirmPassword && registerForm.confirmPassword !== registerForm.password" class="inline-error">
              两次输入密码不一致
            </p>

            <div class="form-group">
              <label class="form-label">
                <span class="label-icon">📱</span>
                手机号
              </label>
              <div class="input-wrapper" :class="{ focused: focusedField === 'phonenumber' }">
                <input
                  ref="phonenumberInput"
                  v-model="registerForm.phonenumber"
                  type="text"
                  class="form-input"
                  placeholder="请输入手机号"
                  @focus="handleFocus('phonenumber')"
                  @blur="handleFieldBlur"
                  @input="handleInput"
                >
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">
                <span class="label-icon">🧩</span>
                用户类型
              </label>
              <div class="type-selector">
                <button
                  type="button"
                  class="type-btn"
                  :class="{ active: registerForm.userType === 1 }"
                  @click="registerForm.userType = 1"
                >
                  居民
                </button>
                <button
                  type="button"
                  class="type-btn"
                  :class="{ active: registerForm.userType === 2 }"
                  @click="registerForm.userType = 2"
                >
                  商户
                </button>
              </div>
            </div>

            <button
              type="submit"
              class="login-btn"
              :class="{ loading: loading, success: registerSuccess }"
              :disabled="loading"
            >
              <span v-if="!loading && !registerSuccess" class="btn-text">🌱 注册</span>
              <span v-else-if="loading" class="btn-text">注册中...</span>
              <span v-else class="btn-text">😊 注册成功</span>
            </button>
          </form>

          <div class="login-footer">
            <router-link to="/login" class="register-link">
              已有账号？<span>立即登录</span>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="errorMsg" class="error-toast">
        <span>❌</span> {{ errorMsg }}
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const errorMsg = ref('')
const registerSuccess = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const focusedField = ref('')
const activeCharacter = ref('')
const mouseX = ref(50)
const mouseY = ref(50)

const usernameInput = ref(null)
const nickNameInput = ref(null)
const passwordInput = ref(null)
const confirmPasswordInput = ref(null)
const phonenumberInput = ref(null)

const registerFormInputEls = () =>
  [
    usernameInput.value,
    nickNameInput.value,
    passwordInput.value,
    confirmPasswordInput.value,
    phonenumberInput.value
  ].filter(Boolean)

/** 账号类输入：探头；密码类：捂眼 */
const characterPose = computed(() => {
  if (showPassword.value || showConfirmPassword.value) return ''
  if (focusedField.value === 'username' || focusedField.value === 'nickName') return 'peek'
  if (focusedField.value === 'password' || focusedField.value === 'confirmPassword') return 'hide'
  if (registerForm.password.length > 0 || registerForm.confirmPassword.length > 0) return 'hide'
  if (registerForm.username.length > 0 || registerForm.nickName.length > 0) return 'peek'
  return ''
})

const registerForm = reactive({
  username: '',
  nickName: '',
  password: '',
  confirmPassword: '',
  phonenumber: '',
  userType: 1
})

const handleMouseMove = (e) => {
  mouseX.value = (e.clientX / window.innerWidth) * 100
  mouseY.value = (e.clientY / window.innerHeight) * 100
}

const getCharacterStyle = (color) => {
  const colors = {
    purple: '#9B59B6',
    black: '#2C3E50',
    yellow: '#F1C40F',
    orange: '#E67E22'
  }
  return { '--character-color': colors[color] }
}

const getHeadStyle = () => {
  if (showPassword.value || showConfirmPassword.value) {
    return {
      transform: 'translateX(-6px) rotateY(-24deg)',
      transition: 'transform 0.45s ease'
    }
  }
  if (characterPose.value === 'peek') {
    const offsetX = (mouseX.value - 50) * 0.012
    const offsetY = (mouseY.value - 50) * 0.008
    return {
      transform: `translate(${offsetX + 14}px, ${offsetY}px) rotate(11deg)`,
      transition: 'transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)'
    }
  }
  if (characterPose.value === 'hide') {
    return {
      transform: 'translate(-11px, 4px) rotate(-13deg)',
      transition: 'transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)'
    }
  }
  const offsetX = (mouseX.value - 50) * 0.03
  const offsetY = (mouseY.value - 50) * 0.02
  return {
    transform: `translate(${offsetX}px, ${offsetY}px)`,
    transition: 'transform 0.1s ease-out'
  }
}

const getEyeStyle = (side) => {
  const offsetX = (mouseX.value - 50) * 0.02
  const offsetY = (mouseY.value - 50) * 0.015
  const direction = side === 'left' ? 1 : -1
  return {
    transform: `translate(${offsetX * direction}px, ${offsetY}px)`,
    transition: 'transform 0.1s ease-out'
  }
}

const getPupilStyle = () => {
  const offsetX = (mouseX.value - 50) * 0.015
  const offsetY = (mouseY.value - 50) * 0.01
  return {
    transform: `translate(${offsetX}px, ${offsetY}px)`,
    transition: 'transform 0.05s ease-out'
  }
}

const mouthClass = computed(() => {
  if (registerSuccess.value) return 'smile'
  if (characterPose.value === 'hide') return 'shy'
  if (registerForm.username && registerForm.nickName && registerForm.password && registerForm.confirmPassword) return 'happy'
  if (characterPose.value === 'peek') return 'curious'
  if (focusedField.value) return 'thinking'
  return ''
})

const handleFocus = (type) => {
  focusedField.value = type
  if (type === 'username' || type === 'nickName') {
    activeCharacter.value = 'purple'
    return
  }
  if (type === 'password' || type === 'confirmPassword') {
    activeCharacter.value = 'yellow'
    return
  }
  activeCharacter.value = 'orange'
}

const handleInput = () => {
  if (registerForm.username || registerForm.nickName) activeCharacter.value = 'purple'
  if (registerForm.password || registerForm.confirmPassword) activeCharacter.value = 'yellow'
  if (registerForm.username && registerForm.nickName && registerForm.password && registerForm.confirmPassword) {
    activeCharacter.value = 'orange'
  }
}

const handleFieldBlur = () => {
  setTimeout(() => {
    const ae = document.activeElement
    if (!registerFormInputEls().includes(ae)) {
      focusedField.value = ''
      activeCharacter.value = ''
    }
  }, 80)
}

const togglePassword = (type) => {
  if (type === 'password') {
    showPassword.value = !showPassword.value
  } else {
    showConfirmPassword.value = !showConfirmPassword.value
  }
}

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.nickName || !registerForm.password || !registerForm.confirmPassword) {
    errorMsg.value = '请填写必填项'
    setTimeout(() => { errorMsg.value = '' }, 3000)
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    errorMsg.value = '两次输入密码不一致'
    setTimeout(() => { errorMsg.value = '' }, 3000)
    return
  }
  if (registerForm.password.length < 6) {
    errorMsg.value = '密码长度至少6位'
    setTimeout(() => { errorMsg.value = '' }, 3000)
    return
  }

  loading.value = true
  errorMsg.value = ''
  try {
    await userStore.registerAction(registerForm)
    registerSuccess.value = true
    activeCharacter.value = 'orange'
    setTimeout(() => {
      router.push('/login')
    }, 800)
  } catch (error) {
    errorMsg.value = error?.message || '注册失败，请稍后重试'
    registerSuccess.value = false
    activeCharacter.value = 'black'
    setTimeout(() => {
      errorMsg.value = ''
      activeCharacter.value = ''
    }, 3000)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 50%, #fff8e1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.leaf {
  position: absolute;
  font-size: 2em;
  opacity: 0.3;
  animation: float 6s ease-in-out infinite;
}

.leaf-1 { top: 10%; left: 5%; animation-delay: 0s; }
.leaf-2 { top: 60%; left: 8%; animation-delay: 1s; }
.leaf-3 { top: 30%; right: 10%; animation-delay: 2s; }

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: #4CAF50;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: #FFC107;
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: #9C27B0;
  bottom: 20%;
  right: 10%;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
}

.login-wrapper {
  display: flex;
  align-items: center;
  gap: 60px;
  padding: 40px;
  max-width: 1000px;
  width: 100%;
}

.characters-section {
  flex: 1;
  text-align: center;
}

.section-title {
  font-size: 1.5em;
  color: #2E7D32;
  margin-bottom: 30px;
  font-weight: 600;
}

.characters-row {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.character {
  display: flex;
  flex-direction: column;
  align-items: center;
  --peek-x: 0px;
  --peek-r: 0deg;
  --hide-x: 0px;
  --hide-r: 0deg;
  transition: transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.characters-row:not(.characters-row--peek-account):not(.characters-row--hide-password) .character:hover {
  transform: translateY(-10px);
}

.characters-row--peek-account .character:nth-child(1) {
  --peek-x: 6px;
  --peek-r: 5deg;
}
.characters-row--peek-account .character:nth-child(2) {
  --peek-x: 11px;
  --peek-r: 7deg;
}
.characters-row--peek-account .character:nth-child(3) {
  --peek-x: 9px;
  --peek-r: 6deg;
}
.characters-row--peek-account .character:nth-child(4) {
  --peek-x: 13px;
  --peek-r: 8deg;
}
.characters-row--peek-account .character {
  transform: translateX(var(--peek-x)) rotate(var(--peek-r));
  transform-origin: 50% 100%;
}
.characters-row--peek-account .character.active {
  transform: translateX(var(--peek-x)) rotate(var(--peek-r)) translateY(-8px) scale(1.08);
}

.characters-row--hide-password .character:nth-child(1) {
  --hide-x: -5px;
  --hide-r: -6deg;
}
.characters-row--hide-password .character:nth-child(2) {
  --hide-x: -9px;
  --hide-r: -8deg;
}
.characters-row--hide-password .character:nth-child(3) {
  --hide-x: -7px;
  --hide-r: -7deg;
}
.characters-row--hide-password .character:nth-child(4) {
  --hide-x: -11px;
  --hide-r: -9deg;
}
.characters-row--hide-password .character {
  transform: translateX(var(--hide-x)) rotate(var(--hide-r));
  transform-origin: 50% 100%;
}
.characters-row--hide-password .character.active {
  transform: translateX(var(--hide-x)) rotate(var(--hide-r)) translateY(-6px) scale(1.06);
}

.characters-row:not(.characters-row--peek-account):not(.characters-row--hide-password) .character.active {
  transform: scale(1.1);
}

.character-body {
  position: relative;
  cursor: pointer;
}

.head {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.character-body.purple .head { background: linear-gradient(145deg, #AB47BC, #9B59B6); }
.character-body.black .head { background: linear-gradient(145deg, #34495E, #2C3E50); }
.character-body.yellow .head { background: linear-gradient(145deg, #FFEB3B, #F1C40F); }
.character-body.orange .head { background: linear-gradient(145deg, #FF9800, #E67E22); }

.face {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.cover-hands {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 6;
}

.cover-hand {
  position: absolute;
  width: 22px;
  height: 15px;
  background: linear-gradient(160deg, rgba(255, 218, 198, 0.96), rgba(235, 175, 145, 0.92));
  border-radius: 50% 50% 45% 55%;
  opacity: 0;
  transform: scale(0.35);
  transition:
    opacity 0.32s ease,
    transform 0.42s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.12);
}

.characters-row--hide-password .cover-hand {
  opacity: 1;
}

.characters-row--hide-password .cover-hand--left {
  left: 9%;
  top: 32%;
  transform: rotate(-36deg) translate(3px, 7px);
}

.characters-row--hide-password .cover-hand--right {
  right: 9%;
  top: 32%;
  transform: rotate(36deg) translate(-3px, 7px);
}

.eyes {
  display: flex;
  gap: 15px;
  margin-bottom: 8px;
}

.eye {
  width: 16px;
  height: 16px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.pupil {
  width: 8px;
  height: 8px;
  background: #333;
  border-radius: 50%;
}

.mouth {
  width: 20px;
  height: 10px;
  border: 3px solid #333;
  border-top: none;
  border-radius: 0 0 20px 20px;
  transition: all 0.3s ease;
}

.mouth.thinking {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 3px solid #333;
}

.mouth.curious {
  width: 14px;
  height: 12px;
  border-radius: 40% 40% 50% 50%;
  border: 3px solid #333;
  border-top-width: 2px;
}

.mouth.shy {
  width: 20px;
  height: 7px;
  border-radius: 0 0 20px 20px;
  border-top: none;
  transform: scaleX(0.88);
}

.mouth.happy {
  width: 25px;
  height: 12px;
  border-radius: 0 0 25px 25px;
}

.mouth.smile {
  width: 30px;
  height: 15px;
  background: #FF6B6B;
  border: none;
  border-radius: 0 0 30px 30px;
}

.blush {
  position: absolute;
  width: 15px;
  height: 8px;
  background: rgba(255, 107, 107, 0.5);
  border-radius: 50%;
  bottom: 20px;
}

.blush:first-of-type {
  left: 8px;
}

.blush:last-of-type {
  right: 8px;
}

.body-shape {
  width: 50px;
  height: 40px;
  margin-top: -5px;
  border-radius: 25px 25px 15px 15px;
  opacity: 0.8;
}

.character-body.purple .body-shape { background: linear-gradient(180deg, #9B59B6, #8E44AD); }
.character-body.black .body-shape { background: linear-gradient(180deg, #2C3E50, #1A252F); }
.character-body.yellow .body-shape { background: linear-gradient(180deg, #F1C40F, #D4AC0D); }
.character-body.orange .body-shape { background: linear-gradient(180deg, #E67E22, #D35400); }

.login-section {
  flex: 1;
  max-width: 400px;
}

.login-card {
  background: white;
  border-radius: 20px;
  padding: 40px 35px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 35px;
}

.logo {
  font-size: 3em;
  margin-bottom: 15px;
}

.login-header h1 {
  font-size: 1.4em;
  color: #2E7D32;
  margin-bottom: 8px;
  font-weight: 600;
}

.subtitle {
  color: #888;
  font-size: 0.95em;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95em;
  color: #555;
  font-weight: 500;
}

.label-icon {
  font-size: 1.1em;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 2px solid #E0E0E0;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: #FAFAFA;
}

.input-wrapper.focused {
  border-color: #4CAF50;
  background: white;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.1);
}

.form-input {
  flex: 1;
  padding: 14px 16px;
  border: none;
  background: transparent;
  font-size: 1em;
  outline: none;
  color: #333;
}

.form-input::placeholder {
  color: #BDBDBD;
}

.password-toggle {
  padding: 10px 15px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2em;
  transition: transform 0.3s ease;
}

.password-toggle:hover {
  transform: scale(1.1);
}

.inline-error {
  margin: -2px 2px 2px;
  color: #D32F2F;
  font-size: 0.88em;
}

.type-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.type-btn {
  border: 2px solid #C8E6C9;
  background: #F6FFF6;
  color: #2E7D32;
  padding: 11px 12px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.95em;
  font-weight: 600;
  transition: all 0.2s ease;
}

.type-btn:hover {
  transform: translateY(-1px);
}

.type-btn.active {
  background: linear-gradient(135deg, #66BB6A, #43A047);
  border-color: transparent;
  color: #fff;
}

.login-btn {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  font-size: 1.1em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  color: white;
  margin-top: 10px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.4);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-btn.success {
  background: linear-gradient(135deg, #66BB6A, #43A047);
}

.login-footer {
  text-align: center;
  margin-top: 25px;
}

.register-link {
  color: #888;
  text-decoration: none;
  font-size: 0.95em;
}

.register-link span {
  color: #4CAF50;
  font-weight: 600;
}

.register-link:hover span {
  text-decoration: underline;
}

.error-toast {
  position: fixed;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 15px 30px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1em;
  color: #D32F2F;
  z-index: 1000;
}

.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

@media (max-width: 900px) {
  .login-wrapper {
    flex-direction: column;
    gap: 40px;
  }

  .characters-section {
    order: 1;
  }

  .login-section {
    order: 2;
  }
}

@media (max-width: 500px) {
  .login-wrapper {
    padding: 20px;
  }

  .login-card {
    padding: 30px 20px;
  }

  .characters-row {
    gap: 8px;
  }

  .head {
    width: 55px;
    height: 55px;
  }

  .eye {
    width: 12px;
    height: 12px;
  }

  .pupil {
    width: 6px;
    height: 6px;
  }

  .body-shape {
    width: 40px;
    height: 32px;
  }

  .character-name {
    font-size: 0.7em;
  }
}
</style>
