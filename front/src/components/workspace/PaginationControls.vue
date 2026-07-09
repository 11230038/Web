<script setup>
const props = defineProps({
  page: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  pageSizeOptions: { type: Array, default: () => [5, 10, 12] },
  total: { type: Number, required: true },
  totalPages: { type: Number, required: true },
})

defineEmits(['update:page', 'update:page-size'])
</script>

<template>
  <div v-if="total > 0" class="pagination-bar">
    <div class="pagination-meta">
      <span>共 {{ total }} 条</span>
      <label class="pagination-size">
        <span>每页</span>
        <select
          :value="pageSize"
          @change="$emit('update:page-size', Number($event.target.value))"
        >
          <option v-for="item in pageSizeOptions" :key="item" :value="item">
            {{ item }}
          </option>
        </select>
        <span>条</span>
      </label>
    </div>

    <div class="pagination-actions">
      <button
        class="ghost-btn"
        :disabled="page <= 1"
        @click="$emit('update:page', page - 1)"
      >
        上一页
      </button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button
        class="ghost-btn"
        :disabled="page >= totalPages"
        @click="$emit('update:page', page + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>
