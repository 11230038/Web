export function readJson(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function emptyProject() {
  return {
    id: null,
    ownerId: '',
    name: '',
    description: '',
    priority: 1,
    status: 0,
    startDate: '',
    endDate: '',
  }
}

export function emptyTask() {
  return {
    id: null,
    creatorId: '',
    assigneeId: '',
    projectId: '',
    parentId: '',
    title: '',
    description: '',
    priority: 1,
    status: 0,
    dueDate: '',
    aiSuggestion: '',
  }
}

export function emptyLog() {
  return {
    id: null,
    operatorId: '',
    taskId: '',
    progressPercent: 0,
    content: '',
  }
}

export function emptySummary() {
  return {
    id: null,
    creatorId: '',
    projectId: '',
    taskId: '',
    summaryType: 0,
    content: '',
  }
}

export function emptyMember() {
  return {
    id: null,
    username: '',
    password: '',
    realName: '',
    role: 2,
    email: '',
    phone: '',
  }
}

export function roleLabel(role) {
  return { 0: '管理员', 1: '项目负责人', 2: '成员' }[role] || '未知'
}

export function projectStatusLabel(status) {
  return { 0: '未开始', 1: '进行中', 2: '已完成' }[status] || '未知'
}

export function priorityLabel(priority) {
  return { 0: '低', 1: '中', 2: '高' }[priority] || '未知'
}

export function summaryTypeLabel(type) {
  return { 0: '阶段总结', 1: '最终总结' }[type] || '未分类'
}

export function mapPriority(priority) {
  const normalized = String(priority || '').trim().toLowerCase()
  if (normalized.includes('high') || normalized.includes('高')) return 2
  if (normalized.includes('low') || normalized.includes('低')) return 0
  return 1
}
