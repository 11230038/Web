<script setup>
import { computed } from 'vue'

const props = defineProps({
  dismissible: { type: Boolean, default: false },
  message: { type: String, default: '' },
  open: { type: Boolean, default: true },
  title: { type: String, default: '' },
  type: { type: String, default: 'success' },
})

defineEmits(['close'])

const resolvedTitle = computed(() => {
  if (props.title) {
    return props.title
  }
  return props.type === 'error' ? '操作失败' : '操作成功'
})

const resolvedIcon = computed(() => (props.type === 'error' ? '!' : '✓'))
</script>

<template>
  <Transition name="toast">
    <section v-if="open && message" class="workspace-alert-shell" :aria-live="type === 'error' ? 'assertive' : 'polite'">
      <div class="workspace-alert" :class="`workspace-alert--${type}`" role="alert">
        <div class="workspace-alert__icon" aria-hidden="true">{{ resolvedIcon }}</div>

        <div class="workspace-alert__body">
          <strong class="workspace-alert__title">{{ resolvedTitle }}</strong>
          <p class="workspace-alert__message">{{ message }}</p>
        </div>

        <button v-if="dismissible" class="workspace-alert__close" type="button" aria-label="关闭提示" @click="$emit('close')">
          ×
        </button>
      </div>
    </section>
  </Transition>
</template>
