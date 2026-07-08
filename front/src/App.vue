<script setup>
import { computed, reactive, ref } from 'vue'

const ROLE_ADMIN = 0
const ROLE_OWNER = 1
const ROLE_MEMBER = 2

const menus = [
  { key: 'overview', label: '总览', managerOnly: false },
  { key: 'projects', label: '项目管理', managerOnly: false },
  { key: 'ai', label: '任务拆解', managerOnly: true },
  { key: 'tasks', label: '任务管理', managerOnly: false },
  { key: 'logs', label: '进度跟踪', managerOnly: false },
  { key: 'summaries', label: '总结中心', managerOnly: false },
  { key: 'members', label: '成员列表', managerOnly: false },
  { key: 'profile', label: '个人信息', managerOnly: false },
]

const token = ref(localStorage.getItem('token') || '')
const currentUser = ref(readJson('currentUser'))
const activeMenu = ref(currentUser.value ? 'overview' : 'login')
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const loginForm = reactive({
  username: 'alice',
  password: '123456',
})

const aiForm = reactive({
  projectName: '',
  goal: '',
  description: '',
})

const breakdownResult = ref(null)
const selectedBreakdownIndexes = ref([])

const collections = reactive({
  projects: [],
  tasks: [],
  logs: [],
  summaries: [],
  members: [],
})

const editor = reactive({
  project: emptyProject(),
  task: emptyTask(),
  log: emptyLog(),
  summary: emptySummary(),
  member: emptyMember(),
})

const editing = reactive({
  projectId: null,
  taskId: null,
  logId: null,
  summaryId: null,
  memberId: null,
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
})

const isAuthed = computed(() => Boolean(token.value && currentUser.value))
const isManager = computed(() =>
  currentUser.value && [ROLE_ADMIN, ROLE_OWNER].includes(currentUser.value.role),
)
const visibleMenus = computed(() =>
  menus.filter((item) => !item.managerOnly || isManager.value),
)

const overviewCards = computed(() => {
  const taskList = collections.tasks
  const projectList = collections.projects
  const myId = currentUser.value?.userId

  return [
    { title: '项目总数', value: projectList.length, accent: 'amber' },
    { title: '任务总数', value: taskList.length, accent: 'blue' },
    { title: '进行中任务', value: taskList.filter((item) => item.status === 1).length, accent: 'green' },
    { title: '我的任务', value: taskList.filter((item) => item.assigneeId === myId).length, accent: 'rose' },
  ]
})

const myTasks = computed(() =>
  collections.tasks.filter((item) => item.assigneeId === currentUser.value?.userId),
)

function readJson(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function emptyProject() {
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

function emptyTask() {
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

function emptyLog() {
  return {
    id: null,
    operatorId: '',
    taskId: '',
    progressPercent: 0,
    content: '',
  }
}

function emptySummary() {
  return {
    id: null,
    creatorId: '',
    projectId: '',
    taskId: '',
    summaryType: 0,
    content: '',
  }
}

function emptyMember() {
  return {
    id: null,
    username: '',
    password: '',
    realName: '',
    role: ROLE_MEMBER,
    email: '',
    phone: '',
  }
}

function setMessage(type, text) {
  errorMessage.value = type === 'error' ? text : ''
  successMessage.value = type === 'success' ? text : ''
}

async function api(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }

  if (token.value && path !== '/auth/login') {
    headers.token = token.value
  }

  const response = await fetch(path, {
    ...options,
    headers,
  })

  let body = null
  try {
    body = await response.json()
  } catch {
    body = null
  }

  if (!response.ok || (body && body.code && body.code !== 200)) {
    const message = body?.message || `请求失败 (${response.status})`
    if (response.status === 401) {
      logout()
    }
    throw new Error(message)
  }

  return body?.data ?? body
}

async function login() {
  loading.value = true
  setMessage('', '')
  try {
    const data = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify(loginForm),
    })
    token.value = data.token
    currentUser.value = data
    localStorage.setItem('token', data.token)
    localStorage.setItem('currentUser', JSON.stringify(data))
    activeMenu.value = 'overview'
    await loadDashboard()
    setMessage('success', `欢迎回来，${data.realName || data.username}`)
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    loading.value = false
  }
}

function logout() {
  token.value = ''
  currentUser.value = null
  localStorage.removeItem('token')
  localStorage.removeItem('currentUser')
  activeMenu.value = 'login'
}

async function loadDashboard() {
  if (!isAuthed.value) return
  loading.value = true
  try {
    const [projects, tasks, logs, summaries, members] = await Promise.all([
      api('/projectInfos'),
      api('/taskInfos'),
      api('/taskLogs'),
      api('/taskSummaries'),
      api('/sysUsers'),
    ])
    collections.projects = projects
    collections.tasks = tasks
    collections.logs = logs
    collections.summaries = summaries
    collections.members = members
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    loading.value = false
  }
}

function roleLabel(role) {
  return { 0: '管理员', 1: '负责人', 2: '成员' }[role] || '未知'
}

function projectStatusLabel(status) {
  return { 0: '未开始', 1: '进行中', 2: '已完成' }[status] || '未知'
}

function priorityLabel(priority) {
  return { 0: '低', 1: '中', 2: '高' }[priority] || '未知'
}

function summaryTypeLabel(type) {
  return { 0: '阶段总结', 1: '最终总结' }[type] || '未分类'
}

function mapPriority(priority) {
  const normalized = String(priority || '').trim().toLowerCase()
  if (normalized.includes('high') || normalized.includes('高')) return 2
  if (normalized.includes('low') || normalized.includes('低')) return 0
  return 1
}

function userNameById(id) {
  const found = collections.members.find((item) => item.id === id)
  return found?.realName || found?.username || `#${id ?? '-'}`
}

function projectNameById(id) {
  return collections.projects.find((item) => item.id === id)?.name || `#${id ?? '-'}`
}

function taskTitleById(id) {
  return collections.tasks.find((item) => item.id === id)?.title || `#${id ?? '-'}`
}

function resetEditor(type) {
  if (type === 'project') {
    editor.project = emptyProject()
    editing.projectId = null
  }
  if (type === 'task') {
    editor.task = emptyTask()
    editing.taskId = null
  }
  if (type === 'log') {
    editor.log = emptyLog()
    editing.logId = null
  }
  if (type === 'summary') {
    editor.summary = emptySummary()
    editing.summaryId = null
  }
  if (type === 'member') {
    editor.member = emptyMember()
    editing.memberId = null
  }
}

function editProject(item) {
  editor.project = { ...item }
  editing.projectId = item.id
}

function editTask(item) {
  editor.task = { ...item, parentId: item.parentId ?? '' }
  editing.taskId = item.id
}

function editLog(item) {
  editor.log = { ...item }
  editing.logId = item.id
}

function editSummary(item) {
  editor.summary = { ...item }
  editing.summaryId = item.id
}

function editMember(item) {
  editor.member = { ...item, password: '' }
  editing.memberId = item.id
}

async function submitProject() {
  saving.value = true
  setMessage('', '')
  try {
    const payload = { ...editor.project, ownerId: editor.project.ownerId || null }
    if (editing.projectId) {
      await api('/projectInfos', { method: 'PUT', body: JSON.stringify(payload) })
      setMessage('success', '项目已更新')
    } else {
      await api('/projectInfos', { method: 'POST', body: JSON.stringify(payload) })
      setMessage('success', '项目已创建')
    }
    resetEditor('project')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    saving.value = false
  }
}

async function removeProject(id) {
  if (!confirm('确认删除这个项目吗？')) return
  try {
    await api(`/projectInfos/${id}`, { method: 'DELETE' })
    setMessage('success', '项目已删除')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  }
}

async function submitTask() {
  saving.value = true
  setMessage('', '')
  try {
    const payload = {
      ...editor.task,
      creatorId: editor.task.creatorId || null,
      assigneeId: editor.task.assigneeId || null,
      projectId: editor.task.projectId || null,
      parentId: editor.task.parentId || null,
    }
    if (editing.taskId) {
      await api('/taskInfos', { method: 'PUT', body: JSON.stringify(payload) })
      setMessage('success', '任务已更新')
    } else {
      await api('/taskInfos', { method: 'POST', body: JSON.stringify(payload) })
      setMessage('success', '任务已创建')
    }
    resetEditor('task')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    saving.value = false
  }
}

async function updateMyTaskStatus(task, status) {
  try {
    await api('/taskInfos', {
      method: 'PUT',
      body: JSON.stringify({ ...task, status }),
    })
    setMessage('success', '任务状态已更新')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  }
}

async function removeTask(id) {
  if (!confirm('确认删除这个任务吗？')) return
  try {
    await api(`/taskInfos/${id}`, { method: 'DELETE' })
    setMessage('success', '任务已删除')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  }
}

async function submitLog() {
  saving.value = true
  setMessage('', '')
  try {
    const payload = {
      ...editor.log,
      operatorId: editor.log.operatorId || null,
      taskId: editor.log.taskId || null,
    }
    if (editing.logId) {
      await api('/taskLogs', { method: 'PUT', body: JSON.stringify(payload) })
      setMessage('success', '进度记录已更新')
    } else {
      await api('/taskLogs', { method: 'POST', body: JSON.stringify(payload) })
      setMessage('success', '进度记录已新增')
    }
    resetEditor('log')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    saving.value = false
  }
}

async function removeLog(id) {
  if (!confirm('确认删除这条进度记录吗？')) return
  try {
    await api(`/taskLogs/${id}`, { method: 'DELETE' })
    setMessage('success', '进度记录已删除')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  }
}

async function submitSummary() {
  saving.value = true
  setMessage('', '')
  try {
    const payload = {
      ...editor.summary,
      creatorId: editor.summary.creatorId || null,
      projectId: editor.summary.projectId || null,
      taskId: editor.summary.taskId || null,
    }
    if (editing.summaryId) {
      await api('/taskSummaries', { method: 'PUT', body: JSON.stringify(payload) })
      setMessage('success', '总结已更新')
    } else {
      await api('/taskSummaries', { method: 'POST', body: JSON.stringify(payload) })
      setMessage('success', '总结已新增')
    }
    resetEditor('summary')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    saving.value = false
  }
}

async function removeSummary(id) {
  if (!confirm('确认删除这条总结吗？')) return
  try {
    await api(`/taskSummaries/${id}`, { method: 'DELETE' })
    setMessage('success', '总结已删除')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  }
}

async function submitMember() {
  saving.value = true
  setMessage('', '')
  try {
    const payload = { ...editor.member }
    if (!payload.password) {
      delete payload.password
    }
    if (editing.memberId) {
      await api('/sysUsers', { method: 'PUT', body: JSON.stringify(payload) })
      setMessage('success', '成员已更新')
    } else {
      await api('/sysUsers', { method: 'POST', body: JSON.stringify(payload) })
      setMessage('success', '成员已新增')
    }
    resetEditor('member')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    saving.value = false
  }
}

async function removeMember(id) {
  if (!confirm('确认删除这个成员吗？')) return
  try {
    await api(`/sysUsers/${id}`, { method: 'DELETE' })
    setMessage('success', '成员已删除')
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  }
}

async function generateProjectBreakdown() {
  saving.value = true
  setMessage('', '')
  try {
    const data = await api('/api/ai/project-breakdown', {
      method: 'POST',
      body: JSON.stringify(aiForm),
    })
    breakdownResult.value = data
    selectedBreakdownIndexes.value = (data.items || []).map((_, index) => index)
    setMessage('success', 'AI 拆解结果已生成')
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    saving.value = false
  }
}

async function importSelectedBreakdownTasks() {
  if (!breakdownResult.value?.items?.length) {
    setMessage('error', '没有可导入的拆解任务')
    return
  }
  if (!editor.task.projectId) {
    setMessage('error', '请先在任务管理里选择一个项目作为导入目标')
    activeMenu.value = 'tasks'
    return
  }

  const selectedItems = breakdownResult.value.items
    .map((item, index) => ({ item, index }))
    .filter(({ index }) => selectedBreakdownIndexes.value.includes(index))

  if (!selectedItems.length) {
    setMessage('error', '请至少选择一条拆解任务')
    return
  }

  saving.value = true
  setMessage('', '')
  try {
    await Promise.all(
      selectedItems.map(({ item, index }) =>
        api('/taskInfos', {
          method: 'POST',
          body: JSON.stringify({
            creatorId: currentUser.value.userId,
            assigneeId: item.assigneeId || null,
            projectId: editor.task.projectId || null,
            parentId: null,
            title: item.title || `拆解任务 ${index + 1}`,
            description: item.description || '',
            priority: mapPriority(item.priority),
            status: 0,
            dueDate: editor.task.dueDate || null,
            aiSuggestion: breakdownResult.value.summary || '',
          }),
        }),
      ),
    )
    setMessage('success', `已导入 ${selectedItems.length} 条任务`)
    activeMenu.value = 'tasks'
    await loadDashboard()
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    saving.value = false
  }
}

function toggleBreakdown(index) {
  if (selectedBreakdownIndexes.value.includes(index)) {
    selectedBreakdownIndexes.value = selectedBreakdownIndexes.value.filter((item) => item !== index)
  } else {
    selectedBreakdownIndexes.value = [...selectedBreakdownIndexes.value, index]
  }
}

async function changePassword() {
  saving.value = true
  setMessage('', '')
  try {
    await api('/sysUsers/change-password', {
      method: 'POST',
      body: JSON.stringify(passwordForm),
    })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    setMessage('success', '密码已更新')
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    saving.value = false
  }
}

if (isAuthed.value) {
  loadDashboard()
}
</script>

<template>
  <div class="shell">
    <section v-if="!isAuthed" class="login-screen">
      <div class="login-card">
        <p class="eyebrow">Mini Semester Workspace</p>
        <h1>任务协作控制台</h1>
        <p class="intro">登录后可以按角色查看项目、任务、进度、总结和成员信息。</p>
        <form class="login-form" @submit.prevent="login">
          <label>
            用户名
            <input v-model="loginForm.username" placeholder="请输入用户名" />
          </label>
          <label>
            密码
            <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
          </label>
          <button class="primary-btn" :disabled="loading">{{ loading ? '登录中...' : '登录系统' }}</button>
        </form>
        <p v-if="errorMessage" class="banner error">{{ errorMessage }}</p>
      </div>
    </section>

    <template v-else>
      <aside class="sidebar">
        <div class="brand">
          <p class="eyebrow">Workspace</p>
          <h2>项目协作台</h2>
        </div>
        <div class="user-panel">
          <strong>{{ currentUser.realName || currentUser.username }}</strong>
          <span>{{ roleLabel(currentUser.role) }}</span>
        </div>
        <nav class="menu">
          <button
            v-for="item in visibleMenus"
            :key="item.key"
            class="menu-item"
            :class="{ active: activeMenu === item.key }"
            @click="activeMenu = item.key"
          >
            {{ item.label }}
          </button>
        </nav>
        <div class="sidebar-actions">
          <button class="ghost-btn" @click="loadDashboard">刷新数据</button>
          <button class="ghost-btn" @click="logout">退出登录</button>
        </div>
      </aside>

      <main class="main-panel">
        <header class="topbar">
          <div>
            <p class="eyebrow">Role Based Access</p>
            <h1>{{ visibleMenus.find((item) => item.key === activeMenu)?.label || '控制台' }}</h1>
          </div>
          <div class="topbar-note"><span>接口鉴权：</span><code>token</code></div>
        </header>

        <p v-if="successMessage" class="banner success">{{ successMessage }}</p>
        <p v-if="errorMessage" class="banner error">{{ errorMessage }}</p>

        <section v-if="activeMenu === 'overview'" class="page-grid">
          <div v-for="card in overviewCards" :key="card.title" class="stat-card" :data-accent="card.accent">
            <span>{{ card.title }}</span>
            <strong>{{ card.value }}</strong>
          </div>
          <article class="panel wide">
            <div class="panel-head">
              <h3>我的任务</h3>
              <span>{{ myTasks.length }} 项</span>
            </div>
            <div class="list-stack">
              <div v-for="task in myTasks" :key="task.id" class="list-row">
                <div>
                  <strong>{{ task.title }}</strong>
                  <p>{{ task.description }}</p>
                </div>
                <div class="tag-group">
                  <span class="tag">{{ projectNameById(task.projectId) }}</span>
                  <span class="tag">{{ projectStatusLabel(task.status) }}</span>
                </div>
              </div>
              <p v-if="!myTasks.length" class="empty">你当前没有被分配的任务。</p>
            </div>
          </article>
        </section>

        <section v-if="activeMenu === 'projects'" class="page-grid">
          <article class="panel wide">
            <div class="panel-head">
              <h3>项目列表</h3>
              <span>{{ collections.projects.length }} 项</span>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>名称</th>
                  <th>负责人</th>
                  <th>优先级</th>
                  <th>状态</th>
                  <th>周期</th>
                  <th v-if="isManager">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in collections.projects" :key="item.id">
                  <td>{{ item.name }}</td>
                  <td>{{ userNameById(item.ownerId) }}</td>
                  <td>{{ priorityLabel(item.priority) }}</td>
                  <td>{{ projectStatusLabel(item.status) }}</td>
                  <td>{{ item.startDate }} ~ {{ item.endDate }}</td>
                  <td v-if="isManager" class="actions">
                    <button class="text-btn" @click="editProject(item)">编辑</button>
                    <button class="text-btn danger" @click="removeProject(item.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </article>
          <article v-if="isManager" class="panel">
            <div class="panel-head">
              <h3>{{ editing.projectId ? '编辑项目' : '新建项目' }}</h3>
              <button class="text-btn" @click="resetEditor('project')">清空</button>
            </div>
            <form class="form-grid" @submit.prevent="submitProject">
              <label>项目名称<input v-model="editor.project.name" required /></label>
              <label>
                负责人
                <select v-model.number="editor.project.ownerId">
                  <option value="">默认当前登录人</option>
                  <option v-for="item in collections.members" :key="item.id" :value="item.id">
                    {{ item.realName || item.username }}
                  </option>
                </select>
              </label>
              <label class="full">项目描述<textarea v-model="editor.project.description" rows="3" /></label>
              <label>
                优先级
                <select v-model.number="editor.project.priority">
                  <option :value="0">低</option>
                  <option :value="1">中</option>
                  <option :value="2">高</option>
                </select>
              </label>
              <label>
                状态
                <select v-model.number="editor.project.status">
                  <option :value="0">未开始</option>
                  <option :value="1">进行中</option>
                  <option :value="2">已完成</option>
                </select>
              </label>
              <label>开始日期<input v-model="editor.project.startDate" type="date" required /></label>
              <label>结束日期<input v-model="editor.project.endDate" type="date" required /></label>
              <button class="primary-btn full" :disabled="saving">
                {{ saving ? '提交中...' : editing.projectId ? '保存项目' : '创建项目' }}
              </button>
            </form>
          </article>
        </section>

        <section v-if="activeMenu === 'ai' && isManager" class="page-grid">
          <article class="panel">
            <div class="panel-head">
              <h3>AI 项目拆解</h3>
              <span>已对齐后端项目拆解接口</span>
            </div>
            <form class="form-grid" @submit.prevent="generateProjectBreakdown">
              <label>项目名称<input v-model="aiForm.projectName" required /></label>
              <label>项目目标<input v-model="aiForm.goal" required /></label>
              <label class="full">项目描述<textarea v-model="aiForm.description" rows="5" required /></label>
              <button class="primary-btn full" :disabled="saving">{{ saving ? '生成中...' : '生成拆解结果' }}</button>
            </form>
          </article>

          <article class="panel wide">
            <div class="panel-head">
              <h3>拆解结果</h3>
              <button class="text-btn" @click="importSelectedBreakdownTasks">导入选中任务</button>
            </div>
            <div v-if="breakdownResult?.summary" class="banner success">{{ breakdownResult.summary }}</div>
            <div v-if="breakdownResult?.items?.length" class="list-stack">
              <label
                v-for="(item, index) in breakdownResult.items"
                :key="`${index}-${item.title || 'untitled'}`"
                class="breakdown-card"
              >
                <input
                  :checked="selectedBreakdownIndexes.includes(index)"
                  type="checkbox"
                  @change="toggleBreakdown(index)"
                />
                <div>
                  <strong>{{ item.title || `拆解任务 ${index + 1}` }}</strong>
                  <p>{{ item.description || '暂无描述' }}</p>
                  <div class="tag-group">
                    <span class="tag">成员：{{ userNameById(item.assigneeId) }}</span>
                    <span class="tag">优先级：{{ item.priority || '中' }}</span>
                    <span class="tag">建议天数：{{ item.suggestedDays || '-' }}</span>
                  </div>
                </div>
              </label>
            </div>
            <pre v-else class="suggestion-box">生成后会在这里显示可导入的项目拆解结果。</pre>
          </article>
        </section>

        <section v-if="activeMenu === 'tasks'" class="page-grid">
          <article class="panel wide">
            <div class="panel-head">
              <h3>任务列表</h3>
              <span>{{ collections.tasks.length }} 项</span>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>标题</th>
                  <th>项目</th>
                  <th>负责人</th>
                  <th>优先级</th>
                  <th>状态</th>
                  <th>截止时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in collections.tasks" :key="item.id">
                  <td>{{ item.title }}</td>
                  <td>{{ projectNameById(item.projectId) }}</td>
                  <td>{{ userNameById(item.assigneeId) }}</td>
                  <td>{{ priorityLabel(item.priority) }}</td>
                  <td>{{ projectStatusLabel(item.status) }}</td>
                  <td>{{ item.dueDate }}</td>
                  <td class="actions">
                    <template v-if="isManager">
                      <button class="text-btn" @click="editTask(item)">编辑</button>
                      <button class="text-btn danger" @click="removeTask(item.id)">删除</button>
                    </template>
                    <template v-else-if="item.assigneeId === currentUser.userId">
                      <button class="text-btn" @click="updateMyTaskStatus(item, 1)">进行中</button>
                      <button class="text-btn" @click="updateMyTaskStatus(item, 2)">已完成</button>
                    </template>
                    <span v-else class="muted">无权限</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </article>

          <article v-if="isManager" class="panel">
            <div class="panel-head">
              <h3>{{ editing.taskId ? '编辑任务' : '新建任务' }}</h3>
              <button class="text-btn" @click="resetEditor('task')">清空</button>
            </div>
            <form class="form-grid" @submit.prevent="submitTask">
              <label>任务标题<input v-model="editor.task.title" required /></label>
              <label>
                所属项目
                <select v-model.number="editor.task.projectId" required>
                  <option value="">请选择项目</option>
                  <option v-for="item in collections.projects" :key="item.id" :value="item.id">
                    {{ item.name }}
                  </option>
                </select>
              </label>
              <label>
                执行成员
                <select v-model.number="editor.task.assigneeId" required>
                  <option value="">请选择成员</option>
                  <option v-for="item in collections.members" :key="item.id" :value="item.id">
                    {{ item.realName || item.username }}
                  </option>
                </select>
              </label>
              <label>
                父任务
                <select v-model.number="editor.task.parentId">
                  <option value="">无</option>
                  <option v-for="item in collections.tasks" :key="item.id" :value="item.id">
                    {{ item.title }}
                  </option>
                </select>
              </label>
              <label class="full">任务描述<textarea v-model="editor.task.description" rows="4" /></label>
              <label>
                优先级
                <select v-model.number="editor.task.priority">
                  <option :value="0">低</option>
                  <option :value="1">中</option>
                  <option :value="2">高</option>
                </select>
              </label>
              <label>
                状态
                <select v-model.number="editor.task.status">
                  <option :value="0">未开始</option>
                  <option :value="1">进行中</option>
                  <option :value="2">已完成</option>
                </select>
              </label>
              <label>截止时间<input v-model="editor.task.dueDate" type="date" required /></label>
              <label class="full">AI 建议<textarea v-model="editor.task.aiSuggestion" rows="3" /></label>
              <button class="primary-btn full" :disabled="saving">
                {{ saving ? '提交中...' : editing.taskId ? '保存任务' : '创建任务' }}
              </button>
            </form>
          </article>
        </section>

        <section v-if="activeMenu === 'logs'" class="page-grid">
          <article class="panel wide">
            <div class="panel-head">
              <h3>进度记录</h3>
              <span>{{ collections.logs.length }} 条</span>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>任务</th>
                  <th>记录人</th>
                  <th>进度</th>
                  <th>内容</th>
                  <th v-if="isManager">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in collections.logs" :key="item.id">
                  <td>{{ taskTitleById(item.taskId) }}</td>
                  <td>{{ userNameById(item.operatorId) }}</td>
                  <td>{{ item.progressPercent }}%</td>
                  <td>{{ item.content }}</td>
                  <td v-if="isManager" class="actions">
                    <button class="text-btn" @click="editLog(item)">编辑</button>
                    <button class="text-btn danger" @click="removeLog(item.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </article>
          <article class="panel">
            <div class="panel-head">
              <h3>{{ editing.logId ? '编辑记录' : '新增记录' }}</h3>
              <button class="text-btn" @click="resetEditor('log')">清空</button>
            </div>
            <form class="form-grid" @submit.prevent="submitLog">
              <label>
                对应任务
                <select v-model.number="editor.log.taskId" required>
                  <option value="">请选择任务</option>
                  <option v-for="item in collections.tasks" :key="item.id" :value="item.id">
                    {{ item.title }}
                  </option>
                </select>
              </label>
              <label>进度百分比<input v-model.number="editor.log.progressPercent" type="number" min="0" max="100" required /></label>
              <label class="full">内容<textarea v-model="editor.log.content" rows="4" required /></label>
              <button class="primary-btn full" :disabled="saving">
                {{ saving ? '提交中...' : editing.logId ? '保存记录' : '新增记录' }}
              </button>
            </form>
          </article>
        </section>

        <section v-if="activeMenu === 'summaries'" class="page-grid">
          <article class="panel wide">
            <div class="panel-head">
              <h3>总结列表</h3>
              <span>{{ collections.summaries.length }} 条</span>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>项目</th>
                  <th>任务</th>
                  <th>创建人</th>
                  <th>类型</th>
                  <th>内容</th>
                  <th v-if="isManager">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in collections.summaries" :key="item.id">
                  <td>{{ projectNameById(item.projectId) }}</td>
                  <td>{{ taskTitleById(item.taskId) }}</td>
                  <td>{{ userNameById(item.creatorId) }}</td>
                  <td>{{ summaryTypeLabel(item.summaryType) }}</td>
                  <td>{{ item.content }}</td>
                  <td v-if="isManager" class="actions">
                    <button class="text-btn" @click="editSummary(item)">编辑</button>
                    <button class="text-btn danger" @click="removeSummary(item.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </article>
          <article class="panel">
            <div class="panel-head">
              <h3>{{ editing.summaryId ? '编辑总结' : '新增总结' }}</h3>
              <button class="text-btn" @click="resetEditor('summary')">清空</button>
            </div>
            <form class="form-grid" @submit.prevent="submitSummary">
              <label>
                所属项目
                <select v-model.number="editor.summary.projectId" required>
                  <option value="">请选择项目</option>
                  <option v-for="item in collections.projects" :key="item.id" :value="item.id">
                    {{ item.name }}
                  </option>
                </select>
              </label>
              <label>
                关联任务
                <select v-model.number="editor.summary.taskId" required>
                  <option value="">请选择任务</option>
                  <option v-for="item in collections.tasks" :key="item.id" :value="item.id">
                    {{ item.title }}
                  </option>
                </select>
              </label>
              <label>
                总结类型
                <select v-model.number="editor.summary.summaryType">
                  <option :value="0">阶段总结</option>
                  <option :value="1">最终总结</option>
                </select>
              </label>
              <label class="full">内容<textarea v-model="editor.summary.content" rows="5" required /></label>
              <button class="primary-btn full" :disabled="saving">
                {{ saving ? '提交中...' : editing.summaryId ? '保存总结' : '新增总结' }}
              </button>
            </form>
          </article>
        </section>

        <section v-if="activeMenu === 'members'" class="page-grid">
          <article class="panel wide">
            <div class="panel-head">
              <h3>成员列表</h3>
              <span>{{ collections.members.length }} 人</span>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>姓名</th>
                  <th>用户名</th>
                  <th>角色</th>
                  <th>邮箱</th>
                  <th>电话</th>
                  <th v-if="isManager">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in collections.members" :key="item.id">
                  <td>{{ item.realName }}</td>
                  <td>{{ item.username }}</td>
                  <td>{{ roleLabel(item.role) }}</td>
                  <td>{{ item.email || '-' }}</td>
                  <td>{{ item.phone || '-' }}</td>
                  <td v-if="isManager" class="actions">
                    <button class="text-btn" @click="editMember(item)">编辑</button>
                    <button class="text-btn danger" @click="removeMember(item.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </article>
          <article v-if="isManager" class="panel">
            <div class="panel-head">
              <h3>{{ editing.memberId ? '编辑成员' : '新增成员' }}</h3>
              <button class="text-btn" @click="resetEditor('member')">清空</button>
            </div>
            <form class="form-grid" @submit.prevent="submitMember">
              <label>用户名<input v-model="editor.member.username" required /></label>
              <label>姓名<input v-model="editor.member.realName" required /></label>
              <label>
                角色
                <select v-model.number="editor.member.role">
                  <option :value="0">管理员</option>
                  <option :value="1">负责人</option>
                  <option :value="2">成员</option>
                </select>
              </label>
              <label>
                密码
                <input
                  v-model="editor.member.password"
                  type="password"
                  :placeholder="editing.memberId ? '留空表示不改密码' : '请输入密码'"
                />
              </label>
              <label>邮箱<input v-model="editor.member.email" type="email" /></label>
              <label>电话<input v-model="editor.member.phone" /></label>
              <button class="primary-btn full" :disabled="saving">
                {{ saving ? '提交中...' : editing.memberId ? '保存成员' : '新增成员' }}
              </button>
            </form>
          </article>
        </section>

        <section v-if="activeMenu === 'profile'" class="page-grid">
          <article class="panel">
            <div class="panel-head"><h3>账号信息</h3></div>
            <div class="profile-card">
              <p><strong>姓名：</strong>{{ currentUser.realName || '-' }}</p>
              <p><strong>用户名：</strong>{{ currentUser.username }}</p>
              <p><strong>角色：</strong>{{ roleLabel(currentUser.role) }}</p>
            </div>
          </article>
          <article class="panel">
            <div class="panel-head"><h3>修改密码</h3></div>
            <form class="form-grid" @submit.prevent="changePassword">
              <label>当前密码<input v-model="passwordForm.oldPassword" type="password" required /></label>
              <label>新密码<input v-model="passwordForm.newPassword" type="password" required /></label>
              <button class="primary-btn full" :disabled="saving">{{ saving ? '提交中...' : '更新密码' }}</button>
            </form>
          </article>
        </section>
      </main>
    </template>
  </div>
</template>
