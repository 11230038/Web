<script setup>
import { computed, ref, watch } from 'vue'
import ActionModal from '../ActionModal.vue'
import PaginationControls from '../PaginationControls.vue'
import { ROLE_OWNER } from '../../../constants/workspace'

const props = defineProps({
  collections: { type: Object, required: true },
  currentUser: { type: Object, required: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  isManager: { type: Boolean, default: false },
  modalOpen: { type: Boolean, default: false },
  projectNameById: { type: Function, required: true },
  saving: { type: Boolean, default: false },
  summaryTypeLabel: { type: Function, required: true },
  taskTitleById: { type: Function, required: true },
  userNameById: { type: Function, required: true },
})

defineEmits([
  'close-summary-modal',
  'edit-summary',
  'open-summary-create',
  'remove-summary',
  'submit-summary',
])

const page = ref(1)
const pageSize = ref(5)
const pageSizeOptions = [5, 10, 12]

const totalPages = computed(() => Math.max(1, Math.ceil(props.collections.summaries.length / pageSize.value)))
const pagedSummaries = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return props.collections.summaries.slice(start, start + pageSize.value)
})

const availableProjects = computed(() => {
  if (Number(props.currentUser?.role) !== ROLE_OWNER) {
    return props.collections.projects
  }

  return props.collections.projects.filter(
    (item) => Number(item.ownerId) === Number(props.currentUser.userId),
  )
})

watch([() => props.collections.summaries.length, pageSize], () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})

watch(
  availableProjects,
  (projects) => {
    if (props.editing.summaryId) {
      return
    }

    const currentProjectId = Number(props.editor.summary.projectId)
    const exists = projects.some((item) => Number(item.id) === currentProjectId)
    if (!exists) {
      props.editor.summary.projectId = projects[0]?.id ?? ''
    }
  },
  { immediate: true },
)
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <h3>总结列表</h3>
        <div class="actions">
          <button v-if="Number(currentUser?.role) !== 0" class="primary-btn" @click="$emit('open-summary-create')">新增总结</button>
        </div>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>项目</th>
            <th>任务</th>
            <th>创建人</th>
            <th>类型</th>
            <th>内容</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedSummaries" :key="item.id">
            <td>{{ projectNameById(item.projectId) }}</td>
            <td>{{ taskTitleById(item.taskId) }}</td>
            <td>{{ userNameById(item.creatorId) }}</td>
            <td>{{ summaryTypeLabel(item.summaryType) }}</td>
            <td>{{ item.content }}</td>
            <td class="actions">
                <button class="text-btn danger" @click="$emit('remove-summary', item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <PaginationControls
        :page="page"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total="collections.summaries.length"
        :total-pages="totalPages"
        @update:page="page = $event"
        @update:page-size="pageSize = $event; page = 1"
      />
    </article>

    <ActionModal
      :open="modalOpen"
      :title="editing.summaryId ? '编辑总结' : '新增总结'"
      @close="$emit('close-summary-modal')"
    >
      <form class="form-grid" @submit.prevent="$emit('submit-summary')">
        <label v-if="editing.summaryId">
          所属项目
          <input :value="projectNameById(editor.summary.projectId)" readonly />
        </label>

        <label v-else>
          所属项目
          <select v-model.number="editor.summary.projectId" required>
            <option v-for="item in availableProjects" :key="item.id" :value="item.id">
              {{ item.name }}
            </option>
          </select>
        </label>

        <label v-if="editing.summaryId">
          关联任务
          <input :value="taskTitleById(editor.summary.taskId)" readonly />
        </label>

        <label v-else>
          关联任务
          <select v-model.number="editor.summary.taskId" required>
            <option v-for="item in collections.tasks" :key="item.id" :value="item.id">
              {{ item.title }}
            </option>
          </select>
        </label>

        <label v-if="editing.summaryId">
          总结类型
          <input :value="summaryTypeLabel(editor.summary.summaryType)" readonly />
        </label>

        <label v-else>
          总结类型
          <select v-model.number="editor.summary.summaryType">
            <option :value="0">阶段总结</option>
            <option :value="1">最终总结</option>
          </select>
        </label>

        <label class="full">
          内容
          <textarea v-model="editor.summary.content" rows="5" required />
        </label>

        <button class="primary-btn full" :disabled="saving">
          {{ saving ? '提交中...' : editing.summaryId ? '保存总结' : '新增总结' }}
        </button>
      </form>
    </ActionModal>
  </section>
</template>
