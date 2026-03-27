export function resolveAccentClass(accent) {
  return accent ? `is-${accent}` : ''
}

export function resolveTagType(status) {
  const map = {
    正常: 'success',
    在线: 'success',
    已完成: 'success',
    低风险: 'success',
    告警: 'warning',
    处理中: 'warning',
    重要: 'warning',
    中风险: 'warning',
    故障: 'danger',
    严重: 'danger',
    高风险: 'danger',
    离线: 'info',
    一般: 'info',
    提示: 'info',
    偏差超阈: 'danger'
  }

  return map[status] || ''
}
