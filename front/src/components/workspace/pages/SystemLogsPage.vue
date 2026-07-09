<script setup>
import PaginationControls from '../PaginationControls.vue'

defineProps({
  loading: { type: Boolean, default: false },
  systemLogState: { type: Object, required: true },
})

defineEmits(['reload', 'update:page', 'update:page-size'])

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <div>
          <h3>日志管理</h3>
          <p class="muted">仅管理员可查看操作审计日志，支持分页浏览最近的关键操作记录。</p>
        </div>
        <div class="actions">
          <span>{{ systemLogState.total }} 条</span>
          <button class="ghost-btn" :disabled="loading" @click="$emit('reload')">
            {{ loading ? '加载中...' : '刷新日志' }}
          </button>
        </div>
      </div>

      <div v-if="systemLogState.items.length" class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>操作人</th>
              <th>操作时间</th>
              <th>类名</th>
              <th>方法名</th>
              <th>参数</th>
              <th>返回值</th>
              <th>耗时</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in systemLogState.items" :key="item.id">
              <td>{{ item.operateEmpName || `#${item.operateEmpId ?? '-'}` }}</td>
              <td>{{ formatTime(item.operateTime) }}</td>
              <td class="log-cell">{{ item.className || '-' }}</td>
              <td class="log-cell">{{ item.methodName || '-' }}</td>
              <td class="log-cell log-cell--multi">{{ item.methodParams || '-' }}</td>
              <td class="log-cell log-cell--multi">{{ item.returnValue || '-' }}</td>
              <td>{{ item.costTime ?? 0 }} ms</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-state">
        <p>{{ loading ? '正在加载日志...' : '暂无日志内容' }}</p>
      </div>

      <PaginationControls
        :page="systemLogState.page"
        :page-size="systemLogState.pageSize"
        :page-size-options="[10, 20, 50]"
        :total="systemLogState.total"
        :total-pages="systemLogState.totalPages"
        @update:page="$emit('update:page', $event)"
        @update:page-size="$emit('update:page-size', $event)"
      />
    </article>
  </section>
</template>

<style scoped>
.table-wrap {
  overflow-x: auto;
}

.log-cell {
  max-width: 180px;
  word-break: break-all;
}

.log-cell--multi {
  white-space: pre-wrap;
}
</style>
