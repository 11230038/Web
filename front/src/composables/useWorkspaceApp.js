import { computed, reactive, ref } from 'vue'
import { MENUS, ROLE_ADMIN, ROLE_MEMBER, ROLE_OWNER } from '../constants/workspace'
import {
  emptyLog,
  emptyMember,
  emptyProject,
  emptySummary,
  emptyTask,
  mapPriority,
  priorityLabel,
  projectStatusLabel,
  readJson,
  roleLabel,
  summaryTypeLabel,
} from '../utils/workspace'

export function useWorkspaceApp() {
  const token = ref(localStorage.getItem('token') || '')
  const currentUser = ref(readJson('currentUser'))
  const activeMenu = ref(currentUser.value ? 'overview' : 'login')
  const loading = ref(false)
  const saving = ref(false)
  const errorMessage = ref('')
  const successMessage = ref('')

  const loginForm = reactive({
    username: '1',
    password: '123456',
  })

  const aiForm = reactive({
    projectName: '',
    goal: '',
    description: '',
    importProjectId: '',
  })

  const breakdownResult = ref(null)
  const selectedBreakdownIndexes = ref([])
  const projectWorkspaceOpen = ref(false)
  const createProjectNeedsBreakdown = ref(true)
  const showBreakdownWorkspace = ref(false)

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
    Boolean(currentUser.value && [ROLE_ADMIN, ROLE_OWNER].includes(Number(currentUser.value.role))),
  )
  const isAdmin = computed(() => Number(currentUser.value?.role) === ROLE_ADMIN)
  const canCreateProject = computed(() =>
    [ROLE_MEMBER, ROLE_OWNER].includes(Number(currentUser.value?.role)),
  )
  const canViewProjectForm = computed(() => Boolean(isManager.value || canCreateProject.value))
  const canUseAiWorkspace = computed(() => Boolean(isManager.value))
  const visibleMenus = computed(() =>
    MENUS.filter((item) => !item.managerOnly || isManager.value),
  )
  const activeMenuLabel = computed(() =>
    MENUS.find((item) => item.key === activeMenu.value)?.label || '工作台',
  )

  const overviewCards = computed(() => {
    const myId = currentUser.value?.userId
    return [
      { title: '项目总数', value: collections.projects.length, accent: 'amber' },
      { title: '任务总数', value: collections.tasks.length, accent: 'blue' },
      {
        title: '进行中任务',
        value: collections.tasks.filter((item) => item.status === 1).length,
        accent: 'green',
      },
      {
        title: '我的任务',
        value: collections.tasks.filter((item) => item.assigneeId === myId).length,
        accent: 'rose',
      },
    ]
  })

  const myTasks = computed(() =>
    collections.tasks.filter((item) => item.assigneeId === currentUser.value?.userId),
  )

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

  function resetAiWorkspace() {
    aiForm.projectName = ''
    aiForm.goal = ''
    aiForm.description = ''
    aiForm.importProjectId = ''
    breakdownResult.value = null
    selectedBreakdownIndexes.value = []
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

  function logout() {
    token.value = ''
    currentUser.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('currentUser')
    activeMenu.value = 'login'
  }

  function syncCurrentUserRole() {
    if (!currentUser.value?.userId) return
    const latestUser = collections.members.find((item) => item.id === currentUser.value.userId)
    if (!latestUser) return
    currentUser.value = {
      ...currentUser.value,
      username: latestUser.username,
      realName: latestUser.realName,
      role: latestUser.role,
    }
    localStorage.setItem('currentUser', JSON.stringify(currentUser.value))
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
      syncCurrentUserRole()
    } catch (error) {
      setMessage('error', error.message)
    } finally {
      loading.value = false
    }
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

  function findProjectIdForAiImport() {
    if (aiForm.importProjectId) return aiForm.importProjectId
    if (editor.task.projectId) return editor.task.projectId

    const normalizedName = aiForm.projectName?.trim()
    if (!normalizedName) return null

    return collections.projects.find((item) => item.name?.trim() === normalizedName)?.id || null
  }

  function resolveBreakdownAssigneeId(rawAssigneeId) {
    const normalizedId = Number(rawAssigneeId)
    if (Number.isFinite(normalizedId)) {
      const matchedUser = collections.members.find((item) => item.id === normalizedId)
      if (matchedUser) {
        return matchedUser.id
      }
    }
    return currentUser.value?.userId || collections.members[0]?.id || null
  }

  function resolveBreakdownDueDate(item, importProjectId) {
    if (editor.task.dueDate) {
      return editor.task.dueDate
    }

    const targetProject = collections.projects.find((project) => project.id === importProjectId)
    if (targetProject?.endDate) {
      return targetProject.endDate
    }

    const suggestedDays = Number(item?.suggestedDays)
    const dueDate = new Date()
    dueDate.setHours(0, 0, 0, 0)
    dueDate.setDate(dueDate.getDate() + (Number.isFinite(suggestedDays) && suggestedDays > 0 ? suggestedDays : 7))
    return dueDate.toISOString().slice(0, 10)
  }

  function openProjectWorkspace(mode = 'create', item = null) {
    if (mode === 'edit' && item) {
      editor.project = { ...item }
      editing.projectId = item.id
      aiForm.projectName = item.name || ''
      aiForm.description = item.description || ''
      aiForm.importProjectId = item.id
      showBreakdownWorkspace.value = false
    } else {
      resetEditor('project')
      resetAiWorkspace()
      createProjectNeedsBreakdown.value = true
      showBreakdownWorkspace.value = false
    }
    projectWorkspaceOpen.value = true
  }

  function closeProjectWorkspace() {
    projectWorkspaceOpen.value = false
    resetEditor('project')
    resetAiWorkspace()
    createProjectNeedsBreakdown.value = true
    showBreakdownWorkspace.value = false
  }

  function editProject(item) {
    openProjectWorkspace('edit', item)
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
      if (editing.projectId) {
        if (!isManager.value) {
          throw new Error('只有项目管理员可以编辑项目')
        }
        const payload = { ...editor.project, ownerId: editor.project.ownerId || null }
        await api('/projectInfos', { method: 'PUT', body: JSON.stringify(payload) })
        aiForm.importProjectId = editor.project.id
        aiForm.projectName = editor.project.name || aiForm.projectName
        aiForm.description = editor.project.description || aiForm.description
        showBreakdownWorkspace.value = false
        setMessage('success', '项目已更新')
      } else {
        if (!canCreateProject.value) {
          throw new Error('只有成员和项目负责人可以新增项目')
        }
        const payload = { ...editor.project, ownerId: currentUser.value?.userId || null }
        const createdProject = await api('/projectInfos', {
          method: 'POST',
          body: JSON.stringify(payload),
        })
        editor.project = { ...createdProject }
        editing.projectId = createdProject.id
        aiForm.importProjectId = createdProject.id
        aiForm.projectName = createdProject.name || aiForm.projectName
        aiForm.description = createdProject.description || aiForm.description
        showBreakdownWorkspace.value = createProjectNeedsBreakdown.value
        setMessage('success', '项目已创建')
      }
      await loadDashboard()
      if (!showBreakdownWorkspace.value) {
        closeProjectWorkspace()
      }
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
        operatorId: editor.log.operatorId || currentUser.value?.userId || null,
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
        creatorId: editor.summary.creatorId || currentUser.value?.userId || null,
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
      if (!editing.memberId && !isAdmin.value) {
        throw new Error('只有管理员可以新增成员')
      }
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
      if (!aiForm.projectName && editor.project.name) {
        aiForm.projectName = editor.project.name
      }
      if (!aiForm.description && editor.project.description) {
        aiForm.description = editor.project.description
      }
      const data = await api('/api/ai/project-breakdown', {
        method: 'POST',
        body: JSON.stringify({
          ...aiForm,
          projectId: aiForm.importProjectId || editor.task.projectId || null,
        }),
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

    const importProjectId = findProjectIdForAiImport()
    if (!importProjectId) {
      setMessage('error', '请先完成项目创建，系统才能把拆解任务导入到新项目中')
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
              assigneeId: resolveBreakdownAssigneeId(item.assigneeId),
              projectId: importProjectId || null,
              parentId: null,
              title: item.title || `拆解任务 ${index + 1}`,
              description: item.description || '',
              priority: mapPriority(item.priority),
              status: 0,
              dueDate: resolveBreakdownDueDate(item, importProjectId),
              aiSuggestion: breakdownResult.value.summary || '',
            }),
          }),
        ),
      )
      setMessage('success', `已导入 ${selectedItems.length} 条任务`)
      activeMenu.value = 'tasks'
      closeProjectWorkspace()
      editor.task.projectId = importProjectId
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

  return {
    activeMenu,
    activeMenuLabel,
    aiForm,
    breakdownResult,
    canCreateProject,
    canUseAiWorkspace,
    canViewProjectForm,
    closeProjectWorkspace,
    changePassword,
    collections,
    createProjectNeedsBreakdown,
    currentUser,
    editLog,
    editMember,
    editProject,
    editSummary,
    editTask,
    editing,
    editor,
    errorMessage,
    generateProjectBreakdown,
    importSelectedBreakdownTasks,
    isAdmin,
    isAuthed,
    isManager,
    loadDashboard,
    loading,
    login,
    loginForm,
    logout,
    myTasks,
    openProjectWorkspace,
    overviewCards,
    passwordForm,
    priorityLabel,
    projectNameById,
    projectStatusLabel,
    projectWorkspaceOpen,
    removeLog,
    removeMember,
    removeProject,
    removeSummary,
    removeTask,
    resetAiWorkspace,
    resetEditor,
    roleLabel,
    saving,
    selectedBreakdownIndexes,
    setMessage,
    showBreakdownWorkspace,
    submitLog,
    submitMember,
    submitProject,
    submitSummary,
    submitTask,
    successMessage,
    summaryTypeLabel,
    taskTitleById,
    toggleBreakdown,
    updateMyTaskStatus,
    userNameById,
    visibleMenus,
  }
}
