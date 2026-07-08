<script setup>
defineProps({
  aiForm: { type: Object, required: true },
  breakdownResult: { type: Object, default: null },
  canCreateProject: { type: Boolean, default: false },
  canUseAiWorkspace: { type: Boolean, default: false },
  canViewProjectForm: { type: Boolean, default: false },
  collections: { type: Object, required: true },
  createProjectNeedsBreakdown: { type: Boolean, default: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  open: { type: Boolean, default: false },
  projectStatusLabel: { type: Function, required: true },
  saving: { type: Boolean, default: false },
  selectedBreakdownIndexes: { type: Array, required: true },
  showBreakdownWorkspace: { type: Boolean, default: false },
  userNameById: { type: Function, required: true },
})

defineEmits([
  'close',
  'generate-breakdown',
  'import-breakdown',
  'reset-ai',
  'reset-project',
  'submit-project',
  'toggle-breakdown',
  'update:create-project-needs-breakdown',
])
</script>

<template>
  <div v-if="open" class="workspace-modal">
    <div class="workspace-backdrop" @click="$emit('close')" />
    <section class="workspace-dialog">
      <div class="panel-head workspace-header">
        <div>
          <h3>{{ editing.projectId ? '编辑项目工作台' : '新增项目工作台' }}</h3>
          <span>先建项目，再按需要进入 AI 任务拆解。</span>
        </div>
        <button class="ghost-btn" @click="$emit('close')">关闭</button>
      </div>

      <div class="workspace-grid">
        <article
          v-if="canViewProjectForm && (canCreateProject || editing.projectId)"
          class="panel workspace-panel"
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
              <select v-model.number="editor.project.ownerId">
                <option value="">默认当前负责人</option>
                <option v-for="item in collections.members" :key="item.id" :value="item.id">
                  {{ item.realName || item.username }}
                </option>
              </select>
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
              <small>只有新增项目时才会触发这个步骤。</small>
            </label>

            <button class="primary-btn full" :disabled="saving">
              {{ saving ? '提交中...' : editing.projectId ? '保存项目' : '创建项目' }}
            </button>
          </form>
        </article>

        <article
          v-if="canUseAiWorkspace && showBreakdownWorkspace"
          class="panel workspace-panel"
        >
          <div class="panel-head">
            <h3>AI 任务拆解</h3>
            <button class="text-btn" @click="$emit('reset-ai')">重置</button>
          </div>

          <form class="form-grid" @submit.prevent="$emit('generate-breakdown')">
            <label>
              项目名称
              <input v-model="aiForm.projectName" required />
            </label>

            <label>
              项目目标
              <input v-model="aiForm.goal" required />
            </label>

            <label>
              导入目标项目
              <select v-model.number="aiForm.importProjectId">
                <option value="">自动匹配新建项目</option>
                <option v-for="item in collections.projects" :key="item.id" :value="item.id">
                  {{ item.name }}
                </option>
              </select>
            </label>

            <label class="full">
              项目描述
              <textarea v-model="aiForm.description" rows="5" required />
            </label>

            <button class="primary-btn full" :disabled="saving">
              {{ saving ? '生成中...' : '生成拆解结果' }}
            </button>
          </form>

          <div class="workspace-results">
            <div class="panel-head">
              <h3>拆解结果</h3>
              <button class="text-btn" @click="$emit('import-breakdown')">导入选中任务</button>
            </div>

            <div v-if="breakdownResult?.summary" class="banner success">
              {{ breakdownResult.summary }}
            </div>

            <div v-if="breakdownResult?.items?.length" class="list-stack">
              <label
                v-for="(item, index) in breakdownResult.items"
                :key="`${index}-${item.title || 'untitled'}`"
                class="breakdown-card"
              >
                <input
                  :checked="selectedBreakdownIndexes.includes(index)"
                  type="checkbox"
                  @change="$emit('toggle-breakdown', index)"
                />
                <div>
                  <strong>{{ item.title || `拆解任务 ${index + 1}` }}</strong>
                  <p>{{ item.description || '暂无描述' }}</p>
                  <div class="tag-group">
                    <span class="tag">成员：{{ userNameById(item.assigneeId) }}</span>
                    <span class="tag">优先级：{{ item.priority || '中' }}</span>
                    <span class="tag">建议天数：{{ item.suggestedDays || '-' }}</span>
                  </div>
                </div>
              </label>
            </div>

            <pre v-else class="suggestion-box">生成后会在这里显示可导入的项目拆解结果。</pre>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>
