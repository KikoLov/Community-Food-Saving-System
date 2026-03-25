/**
 * 简单的消息提示工具
 */

class Message {
  static show(message, type = 'info', duration = 3000) {
    // Create toast container if not exists
    let container = document.getElementById('toast-container')
    if (!container) {
      container = document.createElement('div')
      container.id = 'toast-container'
      container.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999;'
      document.body.appendChild(container)
    }

    // Create toast element
    const toast = document.createElement('div')
    const colors = {
      success: '#27ae60',
      error: '#e74c3c',
      warning: '#f39c12',
      info: '#3498db'
    }
    const icons = {
      success: '✓',
      error: '✕',
      warning: '⚠',
      info: 'ℹ'
    }

    const color = colors[type] || colors.info
    const icon = icons[type] || icons.info

    toast.style.cssText = `
      background: ${color};
      color: white;
      padding: 15px 20px;
      border-radius: 4px;
      margin-bottom: 10px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.2);
      min-width: 250px;
      animation: slideIn 0.3s ease-out;
    `

    toast.innerHTML = `
      <div style="display: flex; align-items: center;">
        <span style="font-size: 1.2em; margin-right: 10px;">${icon}</span>
        <span>${message}</span>
      </div>
    `

    container.appendChild(toast)

    // Auto remove
    setTimeout(() => {
      toast.style.animation = 'slideOut 0.3s ease-out'
      setTimeout(() => toast.remove(), 300)
    }, duration)
  }

  static success(message, duration = 3000) {
    this.show(message, 'success', duration)
  }

  static error(message, duration = 3000) {
    this.show(message, 'error', duration)
  }

  static warning(message, duration = 3000) {
    this.show(message, 'warning', duration)
  }

  static info(message, duration = 3000) {
    this.show(message, 'info', duration)
  }
}

// Add animations
const style = document.createElement('style')
style.textContent = `
  @keyframes slideIn {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
  @keyframes slideOut {
    from {
      transform: translateX(0);
      opacity: 1;
    }
    to {
      transform: translateX(100%);
      opacity: 0;
    }
  }
`
document.head.appendChild(style)

export { Message }
