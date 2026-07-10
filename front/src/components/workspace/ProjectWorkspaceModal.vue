<script setup>
defineProps({
  canCreateProject: { type: Boolean, default: false },
  canViewProjectForm: { type: Boolean, default: false },
  createProjectNeedsBreakdown: { type: Boolean, default: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  open: { type: Boolean, default: false },
  saving: { type: Boolean, default: false },
  userNameById: { type: Function, required: true },
})

defineEmits([
  'close',
  'reset-project',
  'submit-project',
  'update:create-project-needs-breakdown',
])
</script>

<template>
  <div v-if="open" class="workspace-modal">
    <div class="workspace-backdrop" @click="$emit('close')" />
    <section class="workspace-dialog action-dialog">
      <div class="panel-head workspace-header">
        <div>
          <h3>{{ editing.projectId ? '编辑项目工作台' : '新增项目工作台' }}</h3>
          <span>填写项目基础信息，按需在创建完成后进入 AI 拆解。</span>
        </div>
        <button class="ghost-btn" @click="$emit('close')">关闭</button>
      </div>

      <article
        v-if="canViewProjectForm && (canCreateProject || editing.projectId)"
        class="panel workspace-panel wide"
      >
        <div class="panel-head">
          <h3>{{ editing.projectId ? '编辑项目' : '新建项目' }}</h3>
          <button class="text-btn" @click="$emit('reset-project')">清空</button>
        </div>

        <form class="form-grid" @submit.prevent="$emit('submit-project')">
          <label>
            项目名称
            <input v-model="editor.project.name" required />
          </label>

          <label v-if="editing.projectId">
            项目负责人
            <input :value="userNameById(editor.project.ownerId)" readonly />
          </label>

          <label class="full">
            项目描述
            <textarea v-model="editor.project.description" rows="3" />
          </label>

          <label>
            优先级
            <select v-model.number="editor.project.priority">
              <option :value="0">低</option>
              <option :value="1">中</option>
              <option :value="2">高</option>
            </select>
          </label>

          <label>
            状态
            <select v-model.number="editor.project.status">
              <option :value="0">未开始</option>
              <option :value="1">进行中</option>
              <option :value="2">已完成</option>
            </select>
          </label>

          <label>
            开始日期
            <input v-model="editor.project.startDate" type="date" required />
          </label>

          <label>
            结束日期
            <input v-model="editor.project.endDate" type="date" required />
          </label>

          <label v-if="!editing.projectId" class="full check-card">
            <span class="check-row">
              <input
                :checked="createProjectNeedsBreakdown"
                type="checkbox"
                @change="$emit('update:create-project-needs-breakdown', $event.target.checked)"
              />
              <span>创建后立即进入 AI 拆解</span>
            </span>
            <small>创建成功后会自动弹出独立的 AI 拆解弹窗。</small>
          </label>

          <button class="primary-btn full" :disabled="saving">
            {{ saving ? '提交中...' : editing.projectId ? '保存项目' : '创建项目' }}
          </button>
        </form>
      </article>
    </section>
  </div>
</template>
