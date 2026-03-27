const PRODUCT_NAME_MAP = {
  'Greencity Milk': '绿城新鲜牛奶',
  'Greencity Whole Wheat Bread': '绿城全麦面包',
  'Greencity Apple Slices': '绿城苹果果切',
  'SunGarden Yogurt': '阳光原味酸奶',
  'SunGarden Cake Roll': '阳光蛋糕卷',
  'SunGarden Orange Juice': '阳光鲜橙汁'
}

const MERCHANT_NAME_MAP = {
  'Greencity Demo Store': '绿城小区社区便利店',
  'SunGarden Demo Store': '阳光花园社区便利店'
}

const CATEGORY_NAME_MAP = {
  Bakery: '烘焙',
  Dairy: '乳品',
  Fruits: '水果',
  Vegetables: '蔬菜',
  Meat: '肉类',
  Drink: '饮品',
  Fresh: '生鲜',
  Food: '食品'
}

export function normalizeCategoryName(name) {
  if (!name) return name
  return CATEGORY_NAME_MAP[name] || name
}

export function normalizeProductName(name) {
  if (!name) return name
  return PRODUCT_NAME_MAP[name] || name
}

export function normalizeMerchantName(name) {
  if (!name) return name
  return MERCHANT_NAME_MAP[name] || name
}

export function normalizeProductRecord(record) {
  if (!record) return record
  return {
    ...record,
    productName: normalizeProductName(record.productName),
    categoryName: normalizeCategoryName(record.categoryName),
    merchantName: normalizeMerchantName(record.merchantName)
  }
}

