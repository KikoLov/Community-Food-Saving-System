<template>
  <div class="poster-page">
    <div class="page-head">
      <h4 class="mb-1">
        <i class="fas fa-image me-2"></i>成就分享海报
      </h4>
      <p class="text-muted mb-0 small">
        活泼风海报：贴纸、星星、波浪与俏皮文案，一键下载或复制分享。
      </p>
    </div>

    <div class="toolbar card mb-3">
      <div class="card-body d-flex flex-wrap align-items-center gap-2">
        <button type="button" class="btn btn-success" :disabled="loading || drawing" @click="regenerate">
          <i class="fas fa-sync-alt me-1"></i>{{ drawing ? '绘制中…' : '重新生成' }}
        </button>
        <button type="button" class="btn btn-outline-primary" :disabled="!ready || drawing" @click="downloadPng">
          <i class="fas fa-download me-1"></i>下载 PNG
        </button>
        <button type="button" class="btn btn-outline-secondary" :disabled="!ready || drawing" @click="copyImage">
          <i class="fas fa-copy me-1"></i>复制图片
        </button>
        <div class="ms-auto small text-muted">
          扫码访问：<code class="user-select-all">{{ shareUrl }}</code>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-success" role="status"></div>
      <p class="mt-2 text-muted">加载低碳数据…</p>
    </div>

    <div v-else class="preview-shell">
      <div class="preview-inner">
        <canvas
          ref="canvasRef"
          class="poster-canvas"
          :width="pixelW"
          :height="pixelH"
          :style="{ width: cssW + 'px', height: cssH + 'px' }"
        />
      </div>
      <p v-if="copyTip" class="small text-success mt-2 mb-0">{{ copyTip }}</p>
      <p v-if="errMsg" class="small text-danger mt-2 mb-0">{{ errMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import QRCode from 'qrcode'
import { useUserStore } from '@/store/user'
import { getCarbonCenter } from '@/api/consumer'
import { Message } from '@/utils/message'

const userStore = useUserStore()
const canvasRef = ref(null)
const loading = ref(true)
const drawing = ref(false)
const ready = ref(false)
const copyTip = ref('')
const errMsg = ref('')

/** 逻辑尺寸（绘制坐标系） */
const CSS_W = 720
const CSS_H = 1200
const DPR = Math.min(2.5, typeof window !== 'undefined' ? window.devicePixelRatio || 1 : 2)

const cssW = CSS_W
const cssH = CSS_H
const pixelW = Math.round(CSS_W * DPR)
const pixelH = Math.round(CSS_H * DPR)

const shareUrl = computed(() => {
  const env = import.meta.env.VITE_SHARE_QR_URL
  if (env && String(env).trim()) return String(env).trim()
  if (typeof window !== 'undefined') {
    return `${window.location.origin}/register`
  }
  return 'https://example.com/register'
})

function formatNum(v, digits = 2) {
  const n = Number(v)
  if (!Number.isFinite(n)) return '0'
  if (Number.isInteger(n)) return String(n)
  return n.toFixed(digits)
}

/**
 * 挽救食品：接口 totalFoodSaved 为千克（kg，与订单 quantity 累计口径一致），海报换算 g = kg×1000
 */
function foodKgToGramsStr(kg) {
  const g = Math.round((Number(kg) || 0) * 1000)
  return g.toLocaleString('zh-CN')
}

/**
 * 碳减排：接口 totalCarbonSaved 为 CO₂ 当量（kg），海报换算 g = kg×1000；小数值保留合理精度
 */
function carbonKgToGramsStr(kg) {
  const raw = (Number(kg) || 0) * 1000
  if (!(raw > 0)) return '0'
  const g = Number(raw.toPrecision(10))
  if (g >= 100) return Math.round(g).toLocaleString('zh-CN')
  if (g >= 1) return (Math.round(g * 10) / 10).toLocaleString('zh-CN')
  return (Math.round(g * 1000) / 1000).toLocaleString('zh-CN')
}

/** 粗略：1kg CO₂ ≈ 私家车行驶 4.2 km（必须用 kg 口径，不能用 g） */
function equivDriveKm(kgCo2) {
  const n = Number(kgCo2)
  if (!Number.isFinite(n) || n <= 0) return '0'
  return (n * 4.2).toFixed(1)
}

async function loadProfile() {
  loading.value = true
  errMsg.value = ''
  try {
    if (!userStore.userInfo) {
      await userStore.getUserInfoAction()
    }
    const res = await getCarbonCenter()
    return res.data?.profile || {}
  } catch (e) {
    errMsg.value = e?.message || '加载失败'
    Message.error(errMsg.value)
    return {}
  } finally {
    loading.value = false
  }
}

function drawRoundedRect(ctx, x, y, w, h, r) {
  const radius = Math.min(r, w / 2, h / 2)
  ctx.beginPath()
  if (ctx.roundRect) {
    ctx.roundRect(x, y, w, h, radius)
  } else {
    ctx.moveTo(x + radius, y)
    ctx.arcTo(x + w, y, x + w, y + h, radius)
    ctx.arcTo(x + w, y + h, x, y + h, radius)
    ctx.arcTo(x, y + h, x, y, radius)
    ctx.arcTo(x, y, x + w, y, radius)
    ctx.closePath()
  }
}

/** 五角星装饰 */
function drawStar(ctx, cx, cy, outerR, innerR, rot, fill) {
  ctx.save()
  ctx.translate(cx, cy)
  ctx.rotate(rot)
  ctx.beginPath()
  for (let i = 0; i < 10; i++) {
    const r = i % 2 === 0 ? outerR : innerR
    const a = (Math.PI / 5) * i - Math.PI / 2
    const x = Math.cos(a) * r
    const y = Math.sin(a) * r
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  }
  ctx.closePath()
  ctx.fillStyle = fill
  ctx.fill()
  ctx.restore()
}

/** 小彩纸屑 */
function drawConfetti(ctx, seed) {
  const colors = ['#ffeb3b', '#ff8a65', '#81c784', '#4dd0e1', '#ce93d8', '#fff176']
  let s = seed % 9973
  const rnd = () => {
    s = (s * 48271 + 1) % 2147483647
    return s / 2147483647
  }
  for (let i = 0; i < 42; i++) {
    const x = rnd() * CSS_W
    const y = rnd() * (CSS_H * 0.72)
    const w = 5 + rnd() * 9
    const h = 4 + rnd() * 7
    const rot = rnd() * Math.PI
    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(rot)
    ctx.globalAlpha = 0.35 + rnd() * 0.35
    ctx.fillStyle = colors[i % colors.length]
    ctx.fillRect(-w / 2, -h / 2, w, h)
    ctx.restore()
  }
  ctx.globalAlpha = 1
}

/** 底部波浪条 */
function drawWaveFooter(ctx, y0) {
  ctx.save()
  ctx.beginPath()
  ctx.moveTo(0, y0 + 18)
  for (let x = 0; x <= CSS_W; x += 36) {
    const y = y0 + Math.sin(x * 0.045) * 10 + 8
    ctx.lineTo(x, y)
  }
  ctx.lineTo(CSS_W, CSS_H)
  ctx.lineTo(0, CSS_H)
  ctx.closePath()
  const g = ctx.createLinearGradient(0, y0, 0, CSS_H)
  g.addColorStop(0, 'rgba(255,213,79,0.35)')
  g.addColorStop(1, 'rgba(129,199,132,0.25)')
  ctx.fillStyle = g
  ctx.fill()
  ctx.restore()
}

/** 斜放 emoji 贴纸 */
function drawEmojiSticker(ctx, text, x, y, size, deg) {
  ctx.save()
  ctx.translate(x, y)
  ctx.rotate((deg * Math.PI) / 180)
  ctx.font = `${size}px "Segoe UI Emoji","Apple Color Emoji","Noto Color Emoji",sans-serif`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.shadowColor = 'rgba(0,0,0,0.15)'
  ctx.shadowBlur = 6
  ctx.shadowOffsetY = 3
  ctx.fillText(text, 0, 0)
  ctx.restore()
}

/** 手绘感对话气泡 */
function drawSpeechBubble(ctx, x, y, w, h, r) {
  ctx.save()
  ctx.fillStyle = 'rgba(255,255,255,0.92)'
  drawRoundedRect(ctx, x, y, w, h, r)
  ctx.fill()
  ctx.beginPath()
  ctx.moveTo(x + 32, y + h)
  ctx.lineTo(x + 48, y + h + 18)
  ctx.lineTo(x + 58, y + h)
  ctx.closePath()
  ctx.fill()
  ctx.strokeStyle = 'rgba(255,193,7,0.5)'
  ctx.lineWidth = 2
  drawRoundedRect(ctx, x, y, w, h, r)
  ctx.stroke()
  ctx.restore()
}

async function drawPoster(profile) {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  drawing.value = true
  ready.value = false
  copyTip.value = ''

  try {
    if (document.fonts?.ready) await document.fonts.ready
  } catch {
    /* ignore */
  }

  ctx.setTransform(DPR, 0, 0, DPR, 0, 0)

  const u = userStore.userInfo || {}
  const nick = (u.nickName || u.userName || u.username || '居民用户').trim() || '居民用户'
  const seed =
    nick.split('').reduce((a, c) => a + c.charCodeAt(0), 0) +
    Math.floor(Number(profile.totalFoodSaved) || 0)

  // 更活泼的渐变背景（青绿 → 嫩绿 → 暖黄点缀）
  const bg = ctx.createLinearGradient(0, 0, CSS_W, CSS_H * 1.1)
  bg.addColorStop(0, '#087f5b')
  bg.addColorStop(0.35, '#2b8a3e')
  bg.addColorStop(0.65, '#37b24d')
  bg.addColorStop(1, '#5c940d')
  ctx.fillStyle = bg
  ctx.fillRect(0, 0, CSS_W, CSS_H)

  // 大块柔色泡泡
  ctx.save()
  ctx.globalAlpha = 0.22
  const b1 = ctx.createRadialGradient(120, 100, 0, 120, 100, 200)
  b1.addColorStop(0, '#fff9c4')
  b1.addColorStop(1, 'transparent')
  ctx.fillStyle = b1
  ctx.fillRect(0, 0, 400, 400)
  const b2 = ctx.createRadialGradient(CSS_W - 80, 280, 0, CSS_W - 80, 280, 180)
  b2.addColorStop(0, '#b2f2bb')
  b2.addColorStop(1, 'transparent')
  ctx.fillStyle = b2
  ctx.fillRect(CSS_W - 360, 100, 360, 400)
  ctx.restore()

  drawConfetti(ctx, seed)

  // 星星点点
  const starColors = ['#fff9c4', '#ffffff', '#e8f5e9']
  drawStar(ctx, 620, 118, 14, 6, 0.2, starColors[0])
  drawStar(ctx, 88, 420, 11, 4, -0.4, starColors[1])
  drawStar(ctx, 660, 520, 10, 4, 0.6, starColors[2])
  drawStar(ctx, 52, 680, 12, 5, 0.1, '#fff59d')

  // 贴纸 emoji
  drawEmojiSticker(ctx, '🌱', CSS_W - 72, 168, 56, -12)
  drawEmojiSticker(ctx, '✨', 48, 128, 40, 18)
  drawEmojiSticker(ctx, '🥐', CSS_W - 56, 380, 48, 22)
  drawEmojiSticker(ctx, '🍎', 70, 320, 44, -8)
  drawEmojiSticker(ctx, '💚', 640, 640, 42, -15)
  drawEmojiSticker(ctx, '🎉', 100, 560, 46, 10)

  // 顶部小标签
  ctx.fillStyle = 'rgba(255,255,255,0.92)'
  ctx.font = '600 18px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
  ctx.fillText('🌿 临期不浪费 · 好吃又减碳', 40, 46)

  // 主标题 + 俏皮副标题
  ctx.fillStyle = '#fffde7'
  ctx.font = 'bold 38px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
  ctx.shadowColor = 'rgba(0,0,0,0.2)'
  ctx.shadowBlur = 8
  ctx.shadowOffsetY = 2
  ctx.fillText('耶～这是我的绿色成绩单！', 40, 108)
  ctx.shadowBlur = 0
  ctx.shadowOffsetY = 0

  ctx.fillStyle = 'rgba(255,255,255,0.88)'
  ctx.font = '22px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
  ctx.fillText(`嗨 ${nick}，今天也在认真拯救地球胃 ✌️`, 40, 148)

  const foodN = Number(profile.totalFoodSaved) || 0
  const cheer =
    foodN >= 5
      ? '干饭减碳两不误，你超棒的！'
      : foodN > 0
        ? '每一口都算数，继续加油呀～'
        : '从这里开始，一起点亮第一颗星！'
  drawSpeechBubble(ctx, 400, 96, 292, 64, 18)
  ctx.fillStyle = '#33691e'
  ctx.font = '600 17px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
  ctx.fillText(cheer, 412, 136)

  const foodKgNum = Number(profile.totalFoodSaved) || 0
  const carbonKgNum = Number(profile.totalCarbonSaved) || 0
  const foodGramsStr = foodKgToGramsStr(foodKgNum)
  const carbonGramsStr = carbonKgToGramsStr(carbonKgNum)
  const coins = formatNum(profile.carbonPoints, 2)
  const driveKm = equivDriveKm(carbonKgNum)

  // 数据卡片（奶油色 + 彩边）
  const cardX = 32
  const cardY = 228
  const cardW = CSS_W - 64
  const cardH = 418
  ctx.save()
  ctx.shadowColor = 'rgba(255, 193, 7, 0.45)'
  ctx.shadowBlur = 28
  ctx.shadowOffsetY = 10
  ctx.fillStyle = '#fffef7'
  drawRoundedRect(ctx, cardX, cardY, cardW, cardH, 28)
  ctx.fill()
  ctx.restore()

  // 左侧彩虹条装饰
  const stripColors = ['#66bb6a', '#ffca28', '#42a5f5', '#ab47bc']
  stripColors.forEach((c, i) => {
    ctx.fillStyle = c
    ctx.globalAlpha = 0.85
    drawRoundedRect(ctx, cardX + 14, cardY + 24 + i * 22, 8, 18, 4)
    ctx.fill()
  })
  ctx.globalAlpha = 1

  ctx.fillStyle = '#33691e'
  ctx.font = 'bold 22px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
  ctx.fillText('✨ 我的高光数据', cardX + 36, cardY + 46)

  const rowColors = ['#2e7d32', '#1565c0', '#ef6c00', '#6a1b9a']
  const rows = [
    {
      label: '🍱 拯救的好吃的',
      value: `${foodGramsStr} g`,
      sub: '没进垃圾桶的美味，通通算我的功劳～'
    },
    {
      label: '🌍 帮地球减了碳',
      value: `${carbonGramsStr} g CO₂`,
      sub: '大气层：谢谢你，今天少喘一口气啦'
    },
    { label: '🚗 约等于少开车', value: `${driveKm} km`, sub: '仅供参考的趣味换算啦' },
    { label: '🪙 碳积分小金库', value: coins, sub: '商城里换树换券换惊喜' }
  ]

  let y = cardY + 78
  const rowH = 88
  ctx.textAlign = 'left'
  rows.forEach((row, idx) => {
    const pillX = cardX + 28
    const pillW = cardW - 56
    const pillH = rowH - 10
    ctx.save()
    ctx.fillStyle = idx % 2 === 0 ? 'rgba(129, 199, 132, 0.12)' : 'rgba(255, 213, 79, 0.14)'
    drawRoundedRect(ctx, pillX, y - 6, pillW, pillH, 16)
    ctx.fill()
    ctx.restore()

    ctx.fillStyle = rowColors[idx % rowColors.length]
    ctx.font = '600 16px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
    ctx.fillText(row.label, cardX + 44, y + 14)
    ctx.fillStyle = '#1b5e20'
    ctx.font = 'bold 30px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
    ctx.fillText(row.value, cardX + 44, y + 48)
    ctx.fillStyle = '#78909c'
    ctx.font = '13px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
    ctx.fillText(row.sub, cardX + 44, y + 68)
    y += rowH
  })

  // 二维码区（果冻底）
  const qrSize = 196
  const qrX = (CSS_W - qrSize) / 2
  const qrY = cardY + cardH + 28

  ctx.save()
  ctx.fillStyle = 'rgba(255,255,255,0.22)'
  drawRoundedRect(ctx, qrX - 22, qrY - 20, qrSize + 44, qrSize + 88, 22)
  ctx.fill()
  ctx.strokeStyle = 'rgba(255,255,255,0.45)'
  ctx.lineWidth = 2
  drawRoundedRect(ctx, qrX - 22, qrY - 20, qrSize + 44, qrSize + 88, 22)
  ctx.stroke()
  ctx.restore()

  try {
    const qrDataUrl = await QRCode.toDataURL(shareUrl.value, {
      width: qrSize,
      margin: 1,
      color: { dark: '#1b5e20ff', light: '#ffffffff' }
    })
    await new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => {
        ctx.save()
        drawRoundedRect(ctx, qrX - 4, qrY - 4, qrSize + 8, qrSize + 8, 12)
        ctx.clip()
        ctx.drawImage(img, qrX, qrY, qrSize, qrSize)
        ctx.restore()
        resolve()
      }
      img.onerror = reject
      img.src = qrDataUrl
    })
  } catch (e) {
    console.error(e)
    ctx.fillStyle = '#fff'
    ctx.font = '14px sans-serif'
    ctx.fillText('二维码生成失败', qrX, qrY + qrSize / 2)
  }

  ctx.textAlign = 'center'
  ctx.fillStyle = '#fffde7'
  ctx.font = 'bold 17px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
  ctx.fillText('👆 扫我！喊朋友一起来玩～', CSS_W / 2, qrY + qrSize + 30)

  ctx.fillStyle = 'rgba(255,255,255,0.65)'
  ctx.font = '11px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
  const shortUrl =
    shareUrl.value.length > 42 ? `${shareUrl.value.slice(0, 40)}…` : shareUrl.value
  ctx.fillText(shortUrl, CSS_W / 2, qrY + qrSize + 52)

  // 底部波浪 +  footer
  drawWaveFooter(ctx, CSS_H - 120)

  ctx.textAlign = 'center'
  ctx.fillStyle = 'rgba(46,125,50,0.9)'
  ctx.font = '600 13px "Segoe UI","PingFang SC","Microsoft YaHei",sans-serif'
  const now = new Date().toLocaleString('zh-CN')
  ctx.fillText(`🕐 ${now}  ·  数据来自平台记录`, CSS_W / 2, CSS_H - 28)

  ready.value = true
  drawing.value = false
}

async function regenerate() {
  const profile = await loadProfile()
  await drawPoster(profile)
}

function downloadPng() {
  const canvas = canvasRef.value
  if (!canvas || !ready.value) return
  const name = `低碳成就海报-${new Date().toISOString().slice(0, 10)}.png`
  const link = document.createElement('a')
  link.download = name
  link.href = canvas.toDataURL('image/png')
  link.click()
  Message.success('已开始下载')
}

async function copyImage() {
  const canvas = canvasRef.value
  if (!canvas || !ready.value) return
  copyTip.value = ''
  try {
    const blob = await new Promise((resolve, reject) => {
      canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('toBlob failed'))), 'image/png')
    })
    if (!navigator.clipboard || !window.ClipboardItem) {
      Message.warning('当前浏览器不支持复制图片，请使用「下载 PNG」')
      return
    }
    await navigator.clipboard.write([new ClipboardItem({ 'image/png': blob })])
    copyTip.value = '已复制到剪贴板，可在微信等应用中粘贴图片。'
    Message.success('图片已复制')
  } catch (e) {
    Message.warning('复制失败，请改用下载')
  }
}

onMounted(async () => {
  await regenerate()
})
</script>

<style scoped>
.poster-page {
  padding: 20px 0 48px;
  max-width: 920px;
  margin: 0 auto;
}

.page-head {
  margin-bottom: 16px;
}

.preview-shell {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-inner {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 16px 48px rgba(27, 94, 32, 0.22);
  background: #0d3d26;
  line-height: 0;
}

.poster-canvas {
  display: block;
  max-width: 100%;
  height: auto;
}

.toolbar code {
  font-size: 0.78rem;
  word-break: break-all;
  max-width: 280px;
  display: inline-block;
  vertical-align: bottom;
}

@media (max-width: 768px) {
  .toolbar .ms-auto {
    margin-left: 0 !important;
    width: 100%;
  }
}
</style>
