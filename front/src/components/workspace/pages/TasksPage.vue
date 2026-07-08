<script setup>
defineProps({
  collections: { type: Object, required: true },
  currentUser: { type: Object, required: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  isManager: { type: Boolean, default: false },
  priorityLabel: { type: Function, required: true },
  projectNameById: { type: Function, required: true },
  projectStatusLabel: { type: Function, required: true },
  saving: { type: Boolean, default: false },
  userNameById: { type: Function, required: true },
})

defineEmits(['edit-task', 'remove-task', 'reset-task', 'submit-task', 'update-task-status'])
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <h3>任务列表</h3>
        <span>{{ collections.tasks.length }} 项</span>
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
          <tr v-for="item in collections.tasks" :key="item.id">
            <td>{{ item.title }}</td>
            <td>{{ projectNameById(item.projectId) }}</td>
            <td>{{ userNameById(item.assigneeId) }}</td>
            <td>{{ priorityLabel(item.priority) }}</td>
            <td>{{ projectStatusLabel(item.status) }}</td>
            <td>{{ item.dueDate }}</td>
            <td class="actions">
              <template v-if="isManager">
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
    </article>

    <article v-if="isManager" class="panel">
      <div class="panel-head">
        <h3>{{ editing.taskId ? '编辑任务' : '新建任务' }}</h3>
        <button class="text-btn" @click="$emit('reset-task')">清空</button>
      </div>

      <form class="form-grid" @submit.prevent="$emit('submit-task')">
        <label>
          任务标题
          <input v-model="editor.task.title" required />
        </label>

        <label>
          所属项目
          <select v-model.number="editor.task.projectId" required>
            <option value="">请选择项目</option>
            <option v-for="item in collections.projects" :key="item.id" :value="item.id">
              {{ item.name }}
            </option>
          </select>
        </label>

        <label>
          执行成员
          <select v-model.number="editor.task.assigneeId" required>
            <option value="">请选择成员</option>
            <option v-for="item in collections.members" :key="item.id" :value="item.id">
              {{ item.realName || item.username }}
            </option>
          </select>
        </label>

        <label>
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
    </article>
  </section>
</template>
