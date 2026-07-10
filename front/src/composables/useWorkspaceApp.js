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
  const message = ref(null)
  const successMessage = computed(() =>
    message.value?.type === 'success' ? message.value.text : '',
  )
  const errorMessage = computed(() =>
    message.value?.type === 'error' ? message.value.text : '',
  )
  const MESSAGE_DURATION = 5000
  let messageTimer = null
  const taskTitles = reactive({})

  const loginForm = reactive({
    username: '系统管理员',
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
  const breakdownModalOpen = ref(false)
  const createProjectNeedsBreakdown = ref(true)

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

  const modalState = reactive({
    task: false,
    log: false,
    summary: false,
    member: false,
    profile: false,
    password: false,
  })

  const deleteDialog = reactive({
    open: false,
    type: '',
    id: null,
    title: '',
    message: '',
  })

  const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
  })

  const profileForm = reactive({
    id: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
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
  const operateLogState = reactive({
    page: 1,
    pageSize: 10,
    total: 0,
    totalPages: 1,
    items: [],
  })
  const visibleMenus = computed(() =>
    MENUS.filter((item) => (!item.managerOnly || isManager.value) && (!item.adminOnly || isAdmin.value)),
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

  const availableLogTasks = computed(() => {
    if (isAdmin.value) {
      return collections.tasks
    }

    const currentUserId = currentUser.value?.userId
    const baseTasks = collections.tasks.filter(
      (item) => item.assigneeId === currentUserId || item.creatorId === currentUserId,
    )

    if (!editor.log.taskId) {
      return baseTasks
    }

    const editingTask = collections.tasks.find((item) => item.id === editor.log.taskId)
    if (editingTask && !baseTasks.some((item) => item.id === editingTask.id)) {
      return [...baseTasks, editingTask]
    }

    return baseTasks
  })

  function clearMessage() {
    if (messageTimer) {
      clearTimeout(messageTimer)
      messageTimer = null
    }
    message.value = null
  }

  function setMessage(type, text) {
    if (!type || !text) {
      clearMessage()
      return
    }

    clearMessage()
    message.value = {
      type,
      text,
      title: type === 'error' ? '操作失败' : '操作成功',
      key: `${type}-${Date.now()}`,
    }

    messageTimer = setTimeout(() => {
      clearMessage()
    }, MESSAGE_DURATION)
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

  function prepareAiFormFromProject(project) {
    if (!project) return
    aiForm.importProjectId = project.id || ''
    aiForm.projectName = project.name || aiForm.projectName
    aiForm.description = project.description || aiForm.description
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

  function resetPasswordForm() {
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
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

  function syncProfileForm() {
    profileForm.id = currentUser.value?.userId || null
    profileForm.username = currentUser.value?.username || ''
    profileForm.realName = currentUser.value?.realName || ''
    profileForm.email = currentUser.value?.email || ''
    profileForm.phone = currentUser.value?.phone || ''
  }

  async function loadCurrentUserProfile() {
    if (!currentUser.value?.userId) return
    try {
      const profile = await api(`/sysUsers/${currentUser.value.userId}`)
      currentUser.value = {
        ...currentUser.value,
        userId: profile.id,
        username: profile.username,
        realName: profile.realName,
        role: profile.role,
        email: profile.email,
        phone: profile.phone,
      }
      localStorage.setItem('currentUser', JSON.stringify(currentUser.value))
    } catch {
      // Ignore profile refresh failures and keep the current auth state.
    }
  }

  function syncTaskTitles(tasks = []) {
    Object.keys(taskTitles).forEach((key) => {
      delete taskTitles[key]
    })
    tasks.forEach((item) => {
      if (item?.id != null && item?.title) {
        taskTitles[item.id] = item.title
      }
    })
  }

  async function loadMissingTaskTitles(summaryList = []) {
    const missingIds = [...new Set(summaryList.map((item) => item?.taskId).filter((id) => id != null))]
      .filter((id) => !taskTitles[id])

    if (!missingIds.length) return

    const fetchedTasks = await Promise.all(
      missingIds.map(async (id) => {
        try {
          return await api(`/taskInfos/${id}`)
        } catch {
          return null
        }
      }),
    )

    fetchedTasks.forEach((item) => {
      if (item?.id != null && item?.title) {
        taskTitles[item.id] = item.title
      }
    })
  }

  async function loadOperateLogs(page = operateLogState.page, pageSize = operateLogState.pageSize) {
    if (!isAuthed.value || !isAdmin.value) {
      operateLogState.page = 1
      operateLogState.pageSize = pageSize
      operateLogState.total = 0
      operateLogState.totalPages = 1
      operateLogState.items = []
      return
    }

    const data = await api(`/operateLogs?page=${page}&pageSize=${pageSize}`)
    operateLogState.page = data.page || 1
    operateLogState.pageSize = data.pageSize || pageSize
    operateLogState.total = data.total || 0
    operateLogState.totalPages = data.totalPages || 1
    operateLogState.items = data.items || []
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
      syncTaskTitles(tasks)
      await loadMissingTaskTitles(summaries)
      await loadCurrentUserProfile()
      syncCurrentUserRole()
      syncProfileForm()
      await loadOperateLogs()
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
      setMessage('success', `欢迎回来，${data.realName || data.username}。工作台已为你准备就绪。`)
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

  function normalizeId(value) {
    const normalized = Number(value)
    return Number.isFinite(normalized) ? normalized : value
  }

  function projectNameById(id) {
    const normalizedId = normalizeId(id)
    return (
      collections.projects.find((item) => normalizeId(item.id) === normalizedId)?.name ||
      `#${id ?? '-'}`
    )
  }

  function taskTitleById(id) {
    return taskTitles[id] || `#${id ?? '-'}`
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
    breakdownModalOpen.value = false
    if (mode === 'edit' && item) {
      editor.project = { ...item }
      editing.projectId = item.id
      prepareAiFormFromProject(item)
    } else {
      resetEditor('project')
      resetAiWorkspace()
      createProjectNeedsBreakdown.value = true
    }
    projectWorkspaceOpen.value = true
  }

  function closeProjectWorkspace() {
    projectWorkspaceOpen.value = false
    resetEditor('project')
    createProjectNeedsBreakdown.value = true
  }

  function openBreakdownModal(project = null) {
    if (project) {
      prepareAiFormFromProject(project)
    }
    breakdownModalOpen.value = true
  }

  function closeBreakdownModal() {
    breakdownModalOpen.value = false
    resetAiWorkspace()
  }

  function editProject(item) {
    openProjectWorkspace('edit', item)
  }

  function editTask(item) {
    editor.task = {
      ...emptyTask(),
      ...item,
      parentId: item.parentId ?? '',
    }
    editing.taskId = item.id
    modalState.task = true
  }

  function editLog(item) {
    editor.log = {
      ...emptyLog(),
      ...item,
    }
    editing.logId = item.id
    modalState.log = true
  }

  function editSummary(item) {
    setMessage('error', '总结不支持编辑')
    return
    const currentUserId = currentUser.value?.userId
    if (!isManager.value && item.creatorId !== currentUserId) {
      setMessage('error', '只有总结创建者本人和管理员可操作')
      return
    }
    editor.summary = {
      ...emptySummary(),
      ...item,
    }
    editing.summaryId = item.id
    modalState.summary = true
  }

  function editMember(item) {
    if (!isAdmin.value) {
      setMessage('error', '只有管理员能操作成员列表')
      return
    }
    editor.member = {
      ...emptyMember(),
      ...item,
      password: '',
    }
    editing.memberId = item.id
    modalState.member = true
  }

  function openTaskCreate() {
    if (isAdmin.value) {
      setMessage('error', '管理员不显示新增任务功能')
      return
    }
    resetEditor('task')
    modalState.task = true
  }

  function closeTaskModal() {
    modalState.task = false
    resetEditor('task')
  }

  function openLogCreate() {
    if (isAdmin.value) {
      setMessage('error', '管理员不显示新增记录功能')
      return
    }
    resetEditor('log')
    modalState.log = true
  }

  function closeLogModal() {
    modalState.log = false
    resetEditor('log')
  }

  function openSummaryCreate() {
    if (isAdmin.value) {
      setMessage('error', '管理员不显示新增总结功能')
      return
    }
    resetEditor('summary')
    modalState.summary = true
  }

  function closeSummaryModal() {
    modalState.summary = false
    resetEditor('summary')
  }

  function openMemberCreate() {
    if (!isAdmin.value) {
      setMessage('error', '只有管理员能操作成员列表')
      return
    }
    resetEditor('member')
    modalState.member = true
  }

  function closeMemberModal() {
    modalState.member = false
    resetEditor('member')
  }

  function openProfileModal() {
    syncProfileForm()
    modalState.profile = true
  }

  function closeProfileModal() {
    modalState.profile = false
    syncProfileForm()
  }

  function openPasswordModal() {
    resetPasswordForm()
    modalState.password = true
  }

  function closePasswordModal() {
    modalState.password = false
    resetPasswordForm()
  }

  function openDeleteDialog(type, id, title, message) {
    deleteDialog.open = true
    deleteDialog.type = type
    deleteDialog.id = id
    deleteDialog.title = title
    deleteDialog.message = message
  }

  function closeDeleteDialog() {
    deleteDialog.open = false
    deleteDialog.type = ''
    deleteDialog.id = null
    deleteDialog.title = ''
    deleteDialog.message = ''
  }

  function requestRemoveProject(id) {
    openDeleteDialog('project', id, '删除项目', '确认删除这个项目吗？')
  }

  function requestRemoveTask(id) {
    openDeleteDialog('task', id, '删除任务', '确认删除这个任务吗？')
  }

  function requestRemoveLog(id) {
    openDeleteDialog('log', id, '删除记录', '确认删除这条进度记录吗？')
  }

  function requestRemoveSummary(id) {
    openDeleteDialog('summary', id, '删除总结', '确认删除这条总结吗？')
  }

  function requestRemoveMember(id) {
    openDeleteDialog('member', id, '删除成员', '确认删除这个成员吗？')
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
        prepareAiFormFromProject(editor.project)
        await loadDashboard()
        closeProjectWorkspace()
        setMessage('success', '项目内容已更新。')
      } else {
        if (!canCreateProject.value) {
          throw new Error('只有成员和项目负责人可以新增项目')
        }
        const shouldOpenBreakdown = createProjectNeedsBreakdown.value
        const payload = { ...editor.project, ownerId: currentUser.value?.userId || null }
        const createdProject = await api('/projectInfos', {
          method: 'POST',
          body: JSON.stringify(payload),
        })
        editor.project = { ...createdProject }
        editing.projectId = createdProject.id
        prepareAiFormFromProject(createdProject)
        await loadDashboard()
        closeProjectWorkspace()

        if (shouldOpenBreakdown) {
          if (canUseAiWorkspace.value) {
            openBreakdownModal(createdProject)
            setMessage('success', '项目创建成功，已为你打开 AI 拆解工作台。')
          } else {
            setMessage('success', '项目创建成功，但当前账号暂未开通 AI 拆解权限。')
          }
        } else {
          setMessage('success', '项目创建成功。')
        }
      }
    } catch (error) {
      setMessage('error', error.message)
    } finally {
      saving.value = false
    }
  }

  async function removeProject(id) {
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
      closeTaskModal()
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
      closeLogModal()
      await loadDashboard()
    } catch (error) {
      setMessage('error', error.message)
    } finally {
      saving.value = false
    }
  }

  async function removeLog(id) {
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
      const currentUserId = currentUser.value?.userId
      const payload = {
        ...editor.summary,
        creatorId: editor.summary.creatorId || currentUserId || null,
        projectId: editor.summary.projectId || null,
        taskId: editor.summary.taskId || null,
      }
      if (editing.summaryId) {
        throw new Error('总结不支持编辑')
        if (!isManager.value && payload.creatorId !== currentUserId) {
          throw new Error('只有总结创建者本人和管理员可操作')
        }
        await api('/taskSummaries', { method: 'PUT', body: JSON.stringify(payload) })
        setMessage('success', '总结已更新')
      } else {
        await api('/taskSummaries', { method: 'POST', body: JSON.stringify(payload) })
        setMessage('success', '总结已新增')
      }
      closeSummaryModal()
      await loadDashboard()
    } catch (error) {
      setMessage('error', error.message)
    } finally {
      saving.value = false
    }
  }

  async function removeSummary(id) {
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
      if (!isAdmin.value) {
        throw new Error('只有管理员能操作成员列表')
      }
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
      closeMemberModal()
      await loadDashboard()
    } catch (error) {
      setMessage('error', error.message)
    } finally {
      saving.value = false
    }
  }

  async function submitProfile() {
    saving.value = true
    setMessage('', '')
    try {
      const payload = {
        id: profileForm.id || currentUser.value?.userId || null,
        username: profileForm.username,
        realName: profileForm.realName,
        email: profileForm.email,
        phone: profileForm.phone,
      }
      await api('/sysUsers', { method: 'PUT', body: JSON.stringify(payload) })
      const latestUser = await api(`/sysUsers/${payload.id}`)
      currentUser.value = {
        ...currentUser.value,
        userId: latestUser.id,
        username: latestUser.username,
        realName: latestUser.realName,
        role: latestUser.role,
        email: latestUser.email,
        phone: latestUser.phone,
      }
      localStorage.setItem('currentUser', JSON.stringify(currentUser.value))
      closeProfileModal()
      await loadDashboard()
      setMessage('success', '个人信息已更新')
    } catch (error) {
      setMessage('error', error.message)
    } finally {
      saving.value = false
    }
  }

  async function removeMember(id) {
    try {
      if (!isAdmin.value) {
        throw new Error('只有管理员能操作成员列表')
      }
      await api(`/sysUsers/${id}`, { method: 'DELETE' })
      setMessage('success', '成员已删除')
      await loadDashboard()
    } catch (error) {
      setMessage('error', error.message)
    }
  }

  async function confirmDelete() {
    const { type, id } = deleteDialog
    if (!type || id == null) {
      closeDeleteDialog()
      return
    }

    closeDeleteDialog()

    if (type === 'project') {
      await removeProject(id)
      return
    }
    if (type === 'task') {
      await removeTask(id)
      return
    }
    if (type === 'log') {
      await removeLog(id)
      return
    }
    if (type === 'summary') {
      await removeSummary(id)
      return
    }
    if (type === 'member') {
      await removeMember(id)
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
      activeMenu.value = 'tasks'
      closeBreakdownModal()
      editor.task.projectId = importProjectId
      await loadDashboard()
      setMessage('success', `已导入 ${selectedItems.length} 条任务`)
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
      closePasswordModal()
      setMessage('success', '密码已更新')
    } catch (error) {
      setMessage('error', error.message)
    } finally {
      saving.value = false
    }
  }

  async function refreshOperateLogs() {
    loading.value = true
    try {
      await loadOperateLogs()
    } catch (error) {
      setMessage('error', error.message)
    } finally {
      loading.value = false
    }
  }

  async function updateOperateLogPage(page) {
    operateLogState.page = page
    await refreshOperateLogs()
  }

  async function updateOperateLogPageSize(pageSize) {
    operateLogState.pageSize = pageSize
    operateLogState.page = 1
    await refreshOperateLogs()
  }

  if (isAuthed.value) {
    loadDashboard()
  }

  return {
    activeMenu,
    activeMenuLabel,
    aiForm,
    availableLogTasks,
    breakdownResult,
    canCreateProject,
    canUseAiWorkspace,
    canViewProjectForm,
    clearMessage,
    closeBreakdownModal,
    closeDeleteDialog,
    closeLogModal,
    closeMemberModal,
    closePasswordModal,
    closeProfileModal,
    closeProjectWorkspace,
    closeSummaryModal,
    closeTaskModal,
    changePassword,
    confirmDelete,
    collections,
    createProjectNeedsBreakdown,
    currentUser,
    deleteDialog,
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
    message,
    modalState,
    myTasks,
    operateLogState,
    openBreakdownModal,
    openLogCreate,
    openMemberCreate,
    openPasswordModal,
    openProfileModal,
    openProjectWorkspace,
    openSummaryCreate,
    openTaskCreate,
    overviewCards,
    passwordForm,
    profileForm,
    priorityLabel,
    projectNameById,
    projectStatusLabel,
    breakdownModalOpen,
    projectWorkspaceOpen,
    requestRemoveLog,
    requestRemoveMember,
    requestRemoveProject,
    requestRemoveSummary,
    requestRemoveTask,
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
    submitLog,
    submitMember,
    submitProfile,
    submitProject,
    submitSummary,
    submitTask,
    successMessage,
    summaryTypeLabel,
    taskTitleById,
    toggleBreakdown,
    updateMyTaskStatus,
    updateOperateLogPage,
    updateOperateLogPageSize,
    refreshOperateLogs,
    userNameById,
    visibleMenus,
  }
}
