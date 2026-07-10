<script setup>
import { computed, ref, watch } from 'vue'
import ActionModal from '../ActionModal.vue'
import PaginationControls from '../PaginationControls.vue'

const props = defineProps({
  availableLogTasks: { type: Array, required: true },
  collections: { type: Object, required: true },
  currentUser: { type: Object, default: null },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  isManager: { type: Boolean, default: false },
  modalOpen: { type: Boolean, default: false },
  saving: { type: Boolean, default: false },
  taskTitleById: { type: Function, required: true },
  userNameById: { type: Function, required: true },
})

defineEmits(['close-log-modal', 'edit-log', 'open-log-create', 'remove-log', 'submit-log'])

const page = ref(1)
const pageSize = ref(5)
const pageSizeOptions = [5, 10, 12]

const totalPages = computed(() => Math.max(1, Math.ceil(props.collections.logs.length / pageSize.value)))
const pagedLogs = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return props.collections.logs.slice(start, start + pageSize.value)
})

watch([() => props.collections.logs.length, pageSize], () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <h3>进度记录</h3>
        <div class="actions">
          <button v-if="Number(currentUser?.role) !== 0" class="primary-btn" @click="$emit('open-log-create')">新增记录</button>
        </div>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>任务</th>
            <th>记录人</th>
            <th>进度</th>
            <th>内容</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedLogs" :key="item.id">
            <td>{{ taskTitleById(item.taskId) }}</td>
            <td>{{ userNameById(item.operatorId) }}</td>
            <td>{{ item.progressPercent }}%</td>
            <td>{{ item.content }}</td>
            <td class="actions">
              <button class="text-btn" @click="$emit('edit-log', item)">编辑</button>
              <button class="text-btn danger" @click="$emit('remove-log', item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <PaginationControls
        :page="page"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total="collections.logs.length"
        :total-pages="totalPages"
        @update:page="page = $event"
        @update:page-size="pageSize = $event; page = 1"
      />
    </article>

    <ActionModal
      :open="modalOpen"
      :title="editing.logId ? '编辑记录' : '新增记录'"
      @close="$emit('close-log-modal')"
    >
      <form class="form-grid" @submit.prevent="$emit('submit-log')">
        <label v-if="editing.logId">
          对应任务
          <input :value="taskTitleById(editor.log.taskId)" readonly />
        </label>

        <label v-else>
          对应任务
          <select v-model.number="editor.log.taskId" required>
            <option v-for="item in availableLogTasks" :key="item.id" :value="item.id">
              {{ item.title }}
            </option>
          </select>
        </label>

        <label>
          进度百分比
          <input v-model.number="editor.log.progressPercent" type="number" min="0" max="100" required />
        </label>

        <label class="full">
          内容
          <textarea v-model="editor.log.content" rows="4" required />
        </label>

        <button class="primary-btn full" :disabled="saving">
          {{ saving ? '提交中...' : editing.logId ? '保存记录' : '新增记录' }}
        </button>
      </form>
    </ActionModal>
  </section>
</template>
