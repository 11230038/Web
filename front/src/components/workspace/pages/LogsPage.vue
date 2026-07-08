<script setup>
defineProps({
  collections: { type: Object, required: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  isManager: { type: Boolean, default: false },
  saving: { type: Boolean, default: false },
  taskTitleById: { type: Function, required: true },
  userNameById: { type: Function, required: true },
})

defineEmits(['edit-log', 'remove-log', 'reset-log', 'submit-log'])
</script>

<template>
  <section class="page-grid">
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
              <button class="text-btn" @click="$emit('edit-log', item)">编辑</button>
              <button class="text-btn danger" @click="$emit('remove-log', item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </article>

    <article class="panel">
      <div class="panel-head">
        <h3>{{ editing.logId ? '编辑记录' : '新增记录' }}</h3>
        <button class="text-btn" @click="$emit('reset-log')">清空</button>
      </div>

      <form class="form-grid" @submit.prevent="$emit('submit-log')">
        <label>
          对应任务
          <select v-model.number="editor.log.taskId" required>
            <option value="">请选择任务</option>
            <option v-for="item in collections.tasks" :key="item.id" :value="item.id">
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
    </article>
  </section>
</template>
