<template>
  <div class="products-page">
    <div class="mb-3">
      <button class="btn btn-success" @click="handleAdd">
        <i class="fas fa-plus me-2"></i>添加商品
      </button>
    </div>

    <div class="batch-toolbar mb-3">
      <div class="left">
        <span class="selected-tip">已选 {{ selectedIds.length }} 项</span>
        <span v-if="surpriseBagCount > 0" style="margin-left: 20px; color: #d63384; font-weight: 600;">
          盲盒商品: {{ surpriseBagCount }} 件
        </span>
      </div>
      <div class="right">
        <label class="d-flex align-center me-3" style="gap: 6px; cursor: pointer;">
          <input type="checkbox" v-model="surpriseBagFilter">
          <span>仅看盲盒</span>
        </label>
        <button class="btn btn-sm btn-outline-success" :disabled="selectedIds.length === 0" @click="handleBatchStatus(1)">
          批量上架
        </button>
        <button class="btn btn-sm btn-outline-warning" :disabled="selectedIds.length === 0" @click="handleBatchStatus(0)">
          批量下架
        </button>
        <button class="btn btn-sm btn-outline-danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除
        </button>
      </div>
    </div>

    <div class="card">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th style="width: 48px;">
                  <input
                    type="checkbox"
                    :checked="isAllSelected"
                    @change="toggleSelectAll($event.target.checked)"
                  >
                </th>
                <th>商品信息</th>
                <th>价格</th>
                <th>库存</th>
                <th>过期时间</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in filteredProducts" :key="row.productId">
                <td>
                  <input
                    type="checkbox"
                    :checked="selectedIds.includes(row.productId)"
                    @change="toggleSelect(row.productId, $event.target.checked)"
                  >
                </td>
                <td>
                  <div class="d-flex align-items-center">
                    <img :src="getProductImage(row)" class="product-image me-3" />
                    <div>
                      <p class="mb-1">{{ row.productName }}</p>
                      <small class="text-muted">{{ row.categoryName }}</small>
                    </div>
                  </div>
                </td>
                <td>
                  <p class="mb-1"><small class="text-muted">原价: ¥{{ row.originalPrice }}</small></p>
                  <p class="mb-1"><small class="text-muted">底价: ¥{{ row.minPrice ?? row.discountPrice }}</small></p>
                  <p class="mb-0"><strong class="text-danger">¥{{ row.discountPrice }}</strong></p>
                </td>
                <td>{{ row.stock }}</td>
                <td>{{ formatDate(row.expireDatetime) }}</td>
                <td>
                  <span :class="['badge', getStatusClass(row.status)]">
                    {{ getStatusText(row.status) }}
                  </span>
                </td>
                <td>
                  <button
                    v-if="row.status !== 1"
                    class="btn btn-outline-success btn-sm me-1"
                    title="上架"
                    @click="handleSingleStatus(row.productId, 1)"
                  >
                    <i class="fas fa-arrow-up"></i>
                  </button>
                  <button
                    v-else
                    class="btn btn-outline-warning btn-sm me-1"
                    title="下架"
                    @click="handleSingleStatus(row.productId, 0)"
                  >
                    <i class="fas fa-arrow-down"></i>
                  </button>
                  <button class="btn btn-outline-primary btn-sm me-1" @click="handleEdit(row)">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button class="btn btn-outline-danger btn-sm" @click="handleDelete(row.productId)">
                    <i class="fas fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="filteredProducts.length === 0">
                <td colspan="7" class="text-center py-4">
                  <i class="fas fa-box-open fa-3x text-muted mb-3 d-block"></i>
                  <p class="text-muted">暂无商品</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Product Modal -->
    <div
      v-if="isProductModalOpen"
      class="modal show product-modal-backdrop"
      tabindex="-1"
      @click.self="closeModal"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ dialogTitle }}</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="handleSubmit">
              <div class="mb-3">
                <label class="form-label">商品名称 *</label>
                <input type="text" class="form-control" v-model="productForm.productName" required>
              </div>
              <div class="mb-3">
                <label class="form-label">分类</label>
                <select class="form-select" v-model="productForm.categoryId">
                  <option :value="null">请选择分类</option>
                  <option v-for="c in categories" :key="c.categoryId" :value="c.categoryId">
                    {{ c.categoryName }}
                  </option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">商品图片</label>
                <div class="d-flex align-items-center gap-3">
                  <img :src="productImagePreview" class="upload-preview" alt="商品图片预览">
                  <div class="flex-grow-1">
                    <input type="file" class="form-control" accept="image/*" @change="handleImageUpload">
                    <small class="text-muted">支持 JPG/PNG/WebP，自动压缩为 WebP（最长边 960px）</small>
                    <div class="mt-2">
                      <button
                        v-if="productForm.productImage"
                        type="button"
                        class="btn btn-sm btn-outline-danger"
                        @click="handleRemoveImage"
                      >
                        移除图片
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div class="row">
                <div class="col-md-3 mb-3">
                  <label class="form-label">原价 *</label>
                  <input type="number" class="form-control" v-model="productForm.originalPrice" min="0" step="0.01">
                </div>
                <div class="col-md-3 mb-3">
                  <label class="form-label">最低底价 *</label>
                  <input type="number" class="form-control" v-model="productForm.minPrice" min="0" step="0.01">
                </div>
                <div class="col-md-3 mb-3">
                  <label class="form-label">当前售价(自动)</label>
                  <input type="number" class="form-control" :value="autoPricingPreview" disabled>
                </div>
                <div class="col-md-3 mb-3">
                  <label class="form-label">库存 *</label>
                  <input type="number" class="form-control" v-model="productForm.stock" min="0">
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">过期日期 *</label>
                  <input type="date" class="form-control" v-model="productForm.expireDate">
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">过期时间 *</label>
                  <input type="time" class="form-control" v-model="productForm.expireDatetime">
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">预警小时数</label>
                <input type="number" class="form-control" v-model="productForm.warningHours" min="1">
              </div>
              <div class="mb-3 form-check form-switch">
                <input class="form-check-input" type="checkbox" v-model="productForm.surpriseBag" id="surpriseBagSwitch">
                <label class="form-check-label" for="surpriseBagSwitch">启用“盲盒模式”</label>
              </div>
              <div v-if="productForm.surpriseBag" class="mb-3">
                <label class="form-label">盲盒名义价值（元） *</label>
                <input type="number" class="form-control" v-model.number="productForm.bagValue" min="0" step="0.01" required>
                <div class="form-text">通常设定为大于实际折扣价的价值（如50元），可提升吸引力。</div>
              </div>
              <div class="mb-3">
                <label class="form-label">商品描述</label>
                <textarea class="form-control" v-model="productForm.description" rows="3"></textarea>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="button" class="btn btn-outline-secondary" @click="handleSubmit(0)">保存草稿</button>
            <button type="button" class="btn btn-primary" @click="handleSubmit(1)">立即发布</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import {
  getMerchantProducts,
  addProduct,
  updateProduct,
  deleteProduct,
  getCategories,
  batchUpdateProductStatus,
  batchDeleteProducts,
  uploadProductImage,
  deleteProductImage
} from '@/api/merchant'
import { useUserStore } from '@/store/user'
import { Message } from '@/utils/message'
import { resolveProductImageSrc, buildNameBasedProductImage } from '@/utils/productImage'
import { normalizeProductRecord, normalizeCategoryName } from '@/utils/demoTextNormalizer'

const userStore = useUserStore()

const products = ref([])
const selectedIds = ref([])
const categories = ref([])
const surpriseBagFilter = ref(false)
const isProductModalOpen = ref(false)
const dialogTitle = ref('添加商品')
const isEdit = ref(false)
const productImagePreview = ref(buildNameBasedProductImage({}, 120))

const productForm = reactive({
  productId: null,
  productName: '',
  categoryId: null,
  originalPrice: 0,
  discountPrice: 0,
  minPrice: 0,
  bagValue: 0,
  surpriseBag: false,
  stock: 0,
  expireDate: '',
  expireDatetime: '',
  warningHours: 24,
  description: '',
  status: 1,
  productImage: ''
})

onMounted(async () => {
  await loadProducts()
  await loadCategories()
})

const loadProducts = async () => {
  try {
    const res = await getMerchantProducts(1, 100)
    products.value = (res.data || []).map(normalizeProductRecord)
    selectedIds.value = []
  } catch (error) {
    console.error(error)
  }
}

const filteredProducts = computed(() => {
  if (!surpriseBagFilter.value) return products.value
  return products.value.filter(p => p.surpriseBag === 1 || p.surpriseBag === true)
})

const surpriseBagCount = computed(() => {
  return products.value.filter(p => p.surpriseBag === 1 || p.surpriseBag === true).length
})

const isAllSelected = computed(() => {
  return filteredProducts.value.length > 0 && selectedIds.value.length === filteredProducts.value.length
})

const toggleSelect = (id, checked) => {
  if (checked) {
    if (!selectedIds.value.includes(id)) selectedIds.value.push(id)
  } else {
    selectedIds.value = selectedIds.value.filter(x => x !== id)
  }
}

const toggleSelectAll = (checked) => {
  if (checked) {
    selectedIds.value = filteredProducts.value.map(x => x.productId)
  } else {
    selectedIds.value = []
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = (res.data || []).map((c) => ({
      ...c,
      categoryName: normalizeCategoryName(c.categoryName)
    }))
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleDateString('zh-CN')
}

const getProductImage = (row) => {
  return resolveProductImageSrc(row, { size: 120 })
}

const getStatusClass = (status) => {
  const classes = { 0: 'bg-secondary', 1: 'bg-success', 2: 'bg-warning' }
  return classes[status] || 'bg-secondary'
}

const getStatusText = (status) => {
  const texts = { 0: '草稿/下架', 1: '在售', 2: '已售罄' }
  return texts[status] || '未知'
}

const handleAdd = () => {
  dialogTitle.value = '添加商品'
  isEdit.value = false
  // 清空表单
  Object.assign(productForm, {
    productId: null,
    productName: '',
    categoryId: null,
    originalPrice: 0,
    discountPrice: 0,
    minPrice: 0,
    bagValue: 0,
    surpriseBag: false,
    stock: 0,
    expireDate: '',
    expireDatetime: '',
    warningHours: 24,
    description: '',
    status: 1,
    productImage: ''
  })
  productImagePreview.value = buildNameBasedProductImage({}, 120)
  isProductModalOpen.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑商品'
  isEdit.value = true
  Object.assign(productForm, row, {
    minPrice: row.minPrice ?? row.discountPrice ?? 0,
    bagValue: row.bagValue ?? 0,
    surpriseBag: Boolean(row.surpriseBag),
    expireDate: row.expireDate,
    expireDatetime: row.expireDatetime ? row.expireDatetime.substring(11, 19) : ''
  })
  productImagePreview.value = getProductImage(row)
  isProductModalOpen.value = true
}

const closeModal = () => {
  isProductModalOpen.value = false
}

const compressImageToWebp = (file) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.onload = () => {
    const img = new Image()
    img.onload = () => {
      const maxSide = 960
      let { width, height } = img
      if (Math.max(width, height) > maxSide) {
        const ratio = maxSide / Math.max(width, height)
        width = Math.round(width * ratio)
        height = Math.round(height * ratio)
      }
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      canvas.toBlob(
        (blob) => {
          if (!blob) {
            reject(new Error('图片压缩失败'))
            return
          }
          resolve(blob)
        },
        'image/webp',
        0.82
      )
    }
    img.onerror = () => reject(new Error('图片读取失败'))
    img.src = reader.result
  }
  reader.onerror = () => reject(new Error('文件读取失败'))
  reader.readAsDataURL(file)
})

const handleImageUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    Message.warning('请选择图片文件')
    event.target.value = ''
    return
  }
  const previous = productForm.productImage
  try {
    const compressedBlob = await compressImageToWebp(file)
    const compressedFile = new File([compressedBlob], `product-${Date.now()}.webp`, { type: 'image/webp' })
    const uploadRes = await uploadProductImage(compressedFile)
    const imageUrl = uploadRes.data
    productForm.productImage = imageUrl
    productImagePreview.value = imageUrl
    if (previous && previous !== imageUrl) {
      deleteProductImage(previous).catch(() => {})
    }
    Message.success('图片上传成功')
  } catch (e) {
    productForm.productImage = previous || ''
    productImagePreview.value = previous || getProductImage(productForm)
    Message.error(e.message || '图片处理失败')
  } finally {
    event.target.value = ''
  }
}

const handleRemoveImage = async () => {
  const current = productForm.productImage
  productForm.productImage = ''
  productImagePreview.value = getProductImage(productForm)
  if (!current) return
  try {
    await deleteProductImage(current)
    Message.success('图片已移除')
  } catch (e) {
    Message.warning('图片本地引用已移除，服务端文件删除失败可忽略')
  }
}

const handleDelete = async (productId) => {
  if (confirm('确定要删除该商品吗?')) {
    try {
      await deleteProduct(productId)
      Message.success('删除成功')
      await loadProducts()
    } catch (error) {
      console.error(error)
    }
  }
}

const handleSingleStatus = async (productId, status) => {
  try {
    await batchUpdateProductStatus([productId], status)
    Message.success(status === 1 ? '上架成功' : '下架成功')
    await loadProducts()
  } catch (error) {
    console.error(error)
  }
}

const handleBatchStatus = async (status) => {
  if (selectedIds.value.length === 0) return
  try {
    const res = await batchUpdateProductStatus(selectedIds.value, status)
    Message.success(`${status === 1 ? '上架' : '下架'}成功，共处理 ${res.data || 0} 条`)
    await loadProducts()
  } catch (error) {
    console.error(error)
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return
  if (!confirm(`确定删除已选 ${selectedIds.value.length} 个商品吗？`)) return
  try {
    const res = await batchDeleteProducts(selectedIds.value)
    Message.success(`删除成功，共处理 ${res.data || 0} 条`)
    await loadProducts()
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async (targetStatus = 1) => {
  try {
    if (!productForm.productName || !String(productForm.productName).trim()) {
      Message.warning('请填写商品名称')
      return
    }
    if (!productForm.categoryId) {
      Message.warning('请选择商品分类')
      return
    }
    if (!productForm.expireDate) {
      Message.warning('请选择过期日期')
      return
    }
    if (!productForm.expireDatetime) {
      Message.warning('请选择过期时间')
      return
    }

    const original = Number(productForm.originalPrice || 0)
    const min = Number(productForm.minPrice || 0)
    if (original <= 0) {
      Message.warning('原价必须大于0')
      return
    }
    if (min <= 0) {
      Message.warning('最低底价必须大于0')
      return
    }
    if (min > original) {
      Message.warning('最低底价不能高于原价')
      return
    }
    if (productForm.surpriseBag && (!productForm.bagValue || Number(productForm.bagValue) <= 0)) {
      Message.warning('盲盒名义价值必须大于0')
      return
    }
    const payload = {
      ...productForm,
      status: targetStatus,
      // 后端要求 LocalDateTime；将 date + time 组装为 ISO 本地时间字符串
      expireDatetime: `${productForm.expireDate}T${String(productForm.expireDatetime).slice(0, 5)}:00`
    }
    if (isEdit.value) {
      await updateProduct(productForm.productId, payload)
      Message.success(targetStatus === 1 ? '更新并发布成功' : '草稿保存成功')
    } else {
      await addProduct(payload)
      Message.success(targetStatus === 1 ? '发布成功' : '草稿创建成功')
    }
    closeModal()
    await loadProducts()
  } catch (error) {
    console.error(error)
  }
}

const autoPricingPreview = computed(() => {
  const original = Number(productForm.originalPrice || 0)
  const min = Number(productForm.minPrice || 0)
  if (original <= 0 || min <= 0 || min > original) return ''
  const now = Date.now()
  const datePart = productForm.expireDate || ''
  const timePart = productForm.expireDatetime || ''
  const expireTs = new Date(`${datePart}T${timePart || '23:59:59'}`).getTime()
  if (!Number.isFinite(expireTs)) return ''
  const startTs = expireTs - 72 * 3600 * 1000
  const ratio = Math.max(0, Math.min(1, (expireTs - now) / Math.max(1, expireTs - startTs)))
  const dynamic = min + (original - min) * ratio
  return dynamic.toFixed(2)
})

</script>

<style scoped>
.products-page {
  padding: 20px 0;
}

.product-image {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e6e6e6;
}

.upload-preview {
  width: 84px;
  height: 84px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e3e3e3;
  background: #fafafa;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.batch-toolbar .left,
.batch-toolbar .right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-tip {
  color: #517359;
  font-size: 0.92rem;
  font-weight: 600;
}

.btn-primary {
  background-color: #4e57d9;
  border-color: #4e57d9;
}

.btn-primary:hover {
  background-color: #3b47b8;
  border-color: #3b47b8;
}

.product-modal-backdrop {
  display: block;
  background: rgba(0, 0, 0, 0.5);
}
</style>
