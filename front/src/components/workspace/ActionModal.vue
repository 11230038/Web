<script setup>
defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, required: true },
  width: { type: String, default: '720px' },
})

defineEmits(['close'])
</script>

<template>
  <div v-if="open" class="workspace-modal">
    <div class="workspace-backdrop" @click="$emit('close')" />
    <section class="workspace-dialog action-dialog" :style="{ maxWidth: width }">
      <div class="panel-head workspace-header">
        <h3>{{ title }}</h3>
        <button class="ghost-btn" @click="$emit('close')">关闭</button>
      </div>
      <slot />
    </section>
  </div>
</template>
