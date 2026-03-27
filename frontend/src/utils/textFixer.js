const COMMUNITY_NAME_FALLBACK = {
  3: '绿城小区',
  4: '阳光花园'
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

