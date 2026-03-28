const COMMUNITY_NAME_FALLBACK = {
  3: '绿城小区',
  4: '阳光花园'
}

/** 阳光花园 SG001 / id 1、4 */
const SUN_GARDEN = {
  communityName: '阳光花园',
  province: '北京市',
  city: '北京市',
  district: '朝阳区',
  address: '朝阳区建国路88号'
}

/** 绿城小区 GC002 / id 2、3 */
const GREEN_CITY = {
  communityName: '绿城小区',
  province: '上海市',
  city: '上海市',
  district: '浦东新区',
  address: '浦东新区世纪大道100号'
}

const BY_CODE = {
  SG001: SUN_GARDEN,
  GC002: GREEN_CITY
}

const BY_ID = {
  1: SUN_GARDEN,
  2: GREEN_CITY,
  3: GREEN_CITY,
  4: SUN_GARDEN
}

export function isLikelyGarbled(text) {
  const value = String(text || '')
  if (!value) return false
  return /\?{2,}|�|[ÃÂÐÑÖØ]/.test(value)
}

export function fixCommunityName(communityId, rawName) {
  if (!isLikelyGarbled(rawName)) return rawName
  return COMMUNITY_NAME_FALLBACK[communityId] || rawName
}

/**
 * 管理端/居民端列表：修复演示社区省市区与详细地址乱码
 */
export function fixCommunityRecord(row) {
  if (!row) return row
  const code = row.communityCode
  if (code && BY_CODE[code]) {
    return { ...row, ...BY_CODE[code] }
  }
  const id = row.communityId
  if (id != null && BY_ID[id]) {
    return { ...row, ...BY_ID[id] }
  }
  const need =
    isLikelyGarbled(row.province) ||
    isLikelyGarbled(row.city) ||
    isLikelyGarbled(row.district) ||
    isLikelyGarbled(row.address)
  if (!need) return row
  const name = String(row.communityName || '')
  if (/阳光|Sun|花园/i.test(name)) {
    return { ...row, ...SUN_GARDEN }
  }
  if (/绿城|Green/i.test(name)) {
    return { ...row, ...GREEN_CITY }
  }
  return row
}

