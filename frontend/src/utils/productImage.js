function pickStyleByText(text) {
  const s = String(text || '').toLowerCase()
  if (/牛奶|酸奶|奶|milk|yogurt|dairy/.test(s)) return { icon: '🥛', bg1: '#e8f3ff', bg2: '#cfe6ff', label: '乳品' }
  if (/面包|烘焙|蛋糕|bread|cake|bakery/.test(s)) return { icon: '🥖', bg1: '#fff1df', bg2: '#ffe0bb', label: '烘焙' }
  if (/果|橙|苹果|fruit|orange|apple|juice/.test(s)) return { icon: '🍊', bg1: '#fff3dc', bg2: '#ffd99c', label: '水果' }
  if (/肉|鸡|牛|meat|beef|chicken/.test(s)) return { icon: '🥩', bg1: '#ffe5e5', bg2: '#ffc8c8', label: '肉类' }
  if (/蔬|菜|vegetable/.test(s)) return { icon: '🥬', bg1: '#e3f8e7', bg2: '#c5efcf', label: '蔬菜' }
  if (/饮|水|茶|drink|water|tea/.test(s)) return { icon: '🧃', bg1: '#e9f6ff', bg2: '#cdeaff', label: '饮品' }
  return { icon: '🥗', bg1: '#d9f6df', bg2: '#b5ebc2', label: '社区鲜食' }
}

function hashString(input) {
  const str = String(input || '')
  let h = 2166136261
  for (let i = 0; i < str.length; i++) {
    h ^= str.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return h >>> 0
}

function hslToHex(h, s, l) {
  const c = (1 - Math.abs(2 * l - 1)) * s
  const hp = h / 60
  const x = c * (1 - Math.abs((hp % 2) - 1))
  let r = 0; let g = 0; let b = 0
  if (hp >= 0 && hp < 1) [r, g, b] = [c, x, 0]
  else if (hp < 2) [r, g, b] = [x, c, 0]
  else if (hp < 3) [r, g, b] = [0, c, x]
  else if (hp < 4) [r, g, b] = [0, x, c]
  else if (hp < 5) [r, g, b] = [x, 0, c]
  else [r, g, b] = [c, 0, x]
  const m = l - c / 2
  const toHex = (v) => Math.round((v + m) * 255).toString(16).padStart(2, '0')
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`
}

function stableGradientByName(productName) {
  const h = hashString(productName)
  const hue = h % 360
  const hue2 = (hue + 26) % 360
  return {
    bg1: hslToHex(hue, 0.55, 0.92),
    bg2: hslToHex(hue2, 0.62, 0.80)
  }
}

export function buildNameBasedProductImage(product, size = 88) {
  const text = `${product?.productName || ''} ${product?.description || ''}`
  const style = pickStyleByText(text)
  const fixed = stableGradientByName(product?.productName || text)
  const bg1 = fixed.bg1
  const bg2 = fixed.bg2
  const radius = Math.round(size * 0.14)
  const iconSize = Math.round(size * 0.32)
  const labelFont = Math.max(10, Math.round(size * 0.12))
  const badgeY = Math.round(size * 0.7)
  const badgeH = Math.round(size * 0.18)
  const badgeW = Math.round(size * 0.78)
  const badgeX = Math.round((size - badgeW) / 2)
  const textY = badgeY + Math.round(badgeH * 0.74)
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}">
    <defs>
      <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="${bg1}"/>
        <stop offset="100%" stop-color="${bg2}"/>
      </linearGradient>
    </defs>
    <rect width="${size}" height="${size}" rx="${radius}" fill="url(#g)"/>
    <text x="${size / 2}" y="${Math.round(size * 0.46)}" text-anchor="middle" dominant-baseline="middle" font-size="${iconSize}">${style.icon}</text>
    <rect x="${badgeX}" y="${badgeY}" width="${badgeW}" height="${badgeH}" rx="${Math.round(badgeH / 2)}" fill="rgba(255,255,255,0.72)"/>
    <text x="${size / 2}" y="${textY}" text-anchor="middle" font-size="${labelFont}" fill="#2f4f3a">${style.label}</text>
  </svg>`
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`
}

export function resolveProductImageSrc(product, options = {}) {
  const size = options.size || 88
  const raw = product?.productImage
  const fallback = buildNameBasedProductImage(product, size)
  if (!raw) return fallback
  if (raw.startsWith('data:image') || /^https?:\/\//.test(raw)) return raw
  if (raw.startsWith('/uploads/')) return `${window.location.protocol}//${window.location.hostname}:8080${raw}`
  return fallback
}

