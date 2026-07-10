<script setup>
import { computed, ref, watch } from 'vue'
import ActionModal from '../ActionModal.vue'
import PaginationControls from '../PaginationControls.vue'

const props = defineProps({
  collections: { type: Object, required: true },
  currentUser: { type: Object, required: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  isManager: { type: Boolean, default: false },
  modalOpen: { type: Boolean, default: false },
  priorityLabel: { type: Function, required: true },
  projectNameById: { type: Function, required: true },
  projectStatusLabel: { type: Function, required: true },
  saving: { type: Boolean, default: false },
  userNameById: { type: Function, required: true },
})

defineEmits([
  'close-task-modal',
  'edit-task',
  'open-task-create',
  'remove-task',
  'submit-task',
  'update-task-status',
])

const page = ref(1)
const pageSize = ref(5)
const pageSizeOptions = [5, 10, 12]

const totalPages = computed(() => Math.max(1, Math.ceil(props.collections.tasks.length / pageSize.value)))
const pagedTasks = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return props.collections.tasks.slice(start, start + pageSize.value)
})
const canManageTasks = computed(() => props.isManager || Number(props.currentUser?.role) === 0)
const canCreateTask = computed(() => Number(props.currentUser?.role) !== 0 && props.isManager)

function normalizeId(value) {
  const normalized = Number(value)
  return Number.isFinite(normalized) ? normalized : null
}

const assignableMembers = computed(() => {
  const currentUserId = normalizeId(props.currentUser.userId)

  return props.collections.members.filter((item) => {
    const memberId = normalizeId(item.id)
    const memberRole = normalizeId(item.role)
    return memberId === currentUserId || memberRole === 2
  })
})

watch([() => props.collections.tasks.length, pageSize], () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})

watch(
  [() => props.editor.task.projectId, assignableMembers],
  () => {
    if (!props.editor.task.assigneeId) {
      return
    }

    const allowed = assignableMembers.value.some((item) => item.id === props.editor.task.assigneeId)
    if (!allowed) {
      props.editor.task.assigneeId = ''
    }
  },
  { immediate: true },
)
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <h3>任务列表</h3>
        <div class="actions">
          <button v-if="canCreateTask" class="primary-btn" @click="$emit('open-task-create')">
            新增任务
          </button>
        </div>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>项目</th>
            <th>负责人</th>
            <th>优先级</th>
            <th>状态</th>
            <th>截止日期</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedTasks" :key="item.id">
            <td>{{ item.title }}</td>
            <td>{{ projectNameById(item.projectId) }}</td>
            <td>{{ userNameById(item.assigneeId) }}</td>
            <td>{{ priorityLabel(item.priority) }}</td>
            <td>{{ projectStatusLabel(item.status) }}</td>
            <td>{{ item.dueDate }}</td>
            <td class="actions">
              <template v-if="canManageTasks">
                <button class="text-btn" @click="$emit('edit-task', item)">编辑</button>
                <button class="text-btn danger" @click="$emit('remove-task', item.id)">删除</button>
              </template>
              <template v-else-if="item.assigneeId === currentUser.userId">
                <button class="text-btn" @click="$emit('update-task-status', item, 1)">进行中</button>
                <button class="text-btn" @click="$emit('update-task-status', item, 2)">已完成</button>
              </template>
              <span v-else class="muted">无权限</span>
            </td>
          </tr>
        </tbody>
      </table>

      <PaginationControls
        :page="page"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total="collections.tasks.length"
        :total-pages="totalPages"
        @update:page="page = $event"
        @update:page-size="pageSize = $event; page = 1"
      />
    </article>

    <ActionModal
      :open="canManageTasks && modalOpen"
      :title="editing.taskId ? '编辑任务' : '新增任务'"
      @close="$emit('close-task-modal')"
    >
      <form class="form-grid" @submit.prevent="$emit('submit-task')">
        <label>
          任务标题
          <input v-model="editor.task.title" required />
        </label>

        <label v-if="editing.taskId">
          所属项目
          <input :value="projectNameById(editor.task.projectId)" readonly />
        </label>

        <label v-else>
          所属项目
          <select v-model.number="editor.task.projectId" required>
            <option v-for="item in collections.projects" :key="item.id" :value="item.id">
              {{ item.name }}
            </option>
          </select>
        </label>

        <label>
          执行成员
          <select v-model.number="editor.task.assigneeId" required>
            <option v-for="item in assignableMembers" :key="item.id" :value="item.id">
              {{ item.realName || item.username }}
            </option>
          </select>
        </label>

        <label v-if="editing.taskId">
          父任务
          <input :value="editor.task.parentId ? collections.tasks.find((item) => item.id === editor.task.parentId)?.title || '-' : '无'" readonly />
        </label>

        <label v-else>
          父任务
          <select v-model.number="editor.task.parentId">
            <option value="">无</option>
            <option v-for="item in collections.tasks" :key="item.id" :value="item.id">
              {{ item.title }}
            </option>
          </select>
        </label>

        <label class="full">
          任务描述
          <textarea v-model="editor.task.description" rows="4" />
        </label>

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

        <label>
          截止日期
          <input v-model="editor.task.dueDate" type="date" required />
        </label>

        <label class="full">
          AI 建议
          <textarea v-model="editor.task.aiSuggestion" rows="3" />
        </label>

        <button class="primary-btn full" :disabled="saving">
          {{ saving ? '提交中...' : editing.taskId ? '保存任务' : '创建任务' }}
        </button>
      </form>
    </ActionModal>
  </section>
</template>
