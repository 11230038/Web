<script setup>
import { computed, ref, watch } from 'vue'
import PaginationControls from '../PaginationControls.vue'

const props = defineProps({
  canCreateProject: { type: Boolean, default: false },
  collections: { type: Object, required: true },
  isManager: { type: Boolean, default: false },
  priorityLabel: { type: Function, required: true },
  projectStatusLabel: { type: Function, required: true },
  userNameById: { type: Function, required: true },
})

defineEmits(['create-project', 'edit-project', 'remove-project'])

const page = ref(1)
const pageSize = ref(5)
const pageSizeOptions = [5, 10, 12]

const totalPages = computed(() => Math.max(1, Math.ceil(props.collections.projects.length / pageSize.value)))
const pagedProjects = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return props.collections.projects.slice(start, start + pageSize.value)
})

watch([() => props.collections.projects.length, pageSize], () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <h3>项目列表</h3>
        <div class="actions">
          <span>{{ collections.projects.length }} 项</span>
          <button v-if="canCreateProject" class="primary-btn" @click="$emit('create-project')">
            新建项目
          </button>
        </div>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>项目名</th>
            <th>负责人</th>
            <th>优先级</th>
            <th>状态</th>
            <th>周期</th>
            <th v-if="isManager">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedProjects" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ userNameById(item.ownerId) }}</td>
            <td>{{ priorityLabel(item.priority) }}</td>
            <td>{{ projectStatusLabel(item.status) }}</td>
            <td>{{ item.startDate }} ~ {{ item.endDate }}</td>
            <td v-if="isManager" class="actions">
              <button class="text-btn" @click="$emit('edit-project', item)">编辑</button>
              <button class="text-btn danger" @click="$emit('remove-project', item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <PaginationControls
        :page="page"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total="collections.projects.length"
        :total-pages="totalPages"
        @update:page="page = $event"
        @update:page-size="pageSize = $event; page = 1"
      />
    </article>
  </section>
</template>
