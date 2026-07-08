<script setup>
defineProps({
  collections: { type: Object, required: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  isManager: { type: Boolean, default: false },
  projectNameById: { type: Function, required: true },
  saving: { type: Boolean, default: false },
  summaryTypeLabel: { type: Function, required: true },
  taskTitleById: { type: Function, required: true },
  userNameById: { type: Function, required: true },
})

defineEmits(['edit-summary', 'remove-summary', 'reset-summary', 'submit-summary'])
</script>

<template>
  <section class="page-grid">
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
              <button class="text-btn" @click="$emit('edit-summary', item)">编辑</button>
              <button class="text-btn danger" @click="$emit('remove-summary', item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </article>

    <article class="panel">
      <div class="panel-head">
        <h3>{{ editing.summaryId ? '编辑总结' : '新增总结' }}</h3>
        <button class="text-btn" @click="$emit('reset-summary')">清空</button>
      </div>

      <form class="form-grid" @submit.prevent="$emit('submit-summary')">
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

        <label class="full">
          内容
          <textarea v-model="editor.summary.content" rows="5" required />
        </label>

        <button class="primary-btn full" :disabled="saving">
          {{ saving ? '提交中...' : editing.summaryId ? '保存总结' : '新增总结' }}
        </button>
      </form>
    </article>
  </section>
</template>
