<script setup>
import ActionModal from './ActionModal.vue'

defineProps({
  aiForm: { type: Object, required: true },
  breakdownResult: { type: Object, default: null },
  canUseAiWorkspace: { type: Boolean, default: false },
  collections: { type: Object, required: true },
  open: { type: Boolean, default: false },
  saving: { type: Boolean, default: false },
  selectedBreakdownIndexes: { type: Array, required: true },
  userNameById: { type: Function, required: true },
})

defineEmits([
  'close',
  'generate-breakdown',
  'import-breakdown',
  'reset-ai',
  'toggle-breakdown',
])
</script>

<template>
  <ActionModal
    v-if="canUseAiWorkspace"
    :open="open"
    title="AI 任务拆解"
    width="920px"
    @close="$emit('close')"
  >
    <div class="panel workspace-panel wide">
      <div class="panel-head">
        <div>
          <h3>AI 任务拆解</h3>
          <span>生成拆解结果后，可直接导入为项目任务。</span>
        </div>
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
    </div>
  </ActionModal>
</template>
