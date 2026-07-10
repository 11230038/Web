<script setup>
defineProps({
  myTasks: { type: Array, required: true },
  overviewCards: { type: Array, required: true },
  priorityLabel: { type: Function, required: true },
  projectNameById: { type: Function, required: true },
  projectStatusLabel: { type: Function, required: true },
})
</script>

<template>
  <section class="page-grid">
    <article
      v-for="card in overviewCards"
      :key="card.title"
      class="stat-card"
      :data-accent="card.accent"
    >
      <span>{{ card.title }}</span>
      <strong>{{ card.value }}</strong>
    </article>

    <article class="panel wide">
      <div class="panel-head">
        <h3>我的待办</h3>
      </div>

      <div v-if="myTasks.length" class="list-stack">
        <div v-for="item in myTasks" :key="item.id" class="list-row">
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ projectNameById(item.projectId) }}</p>
          </div>
          <div class="tag-group">
            <span class="tag">{{ priorityLabel(item.priority) }}</span>
            <span class="tag">{{ projectStatusLabel(item.status) }}</span>
          </div>
        </div>
      </div>
      <p v-else class="empty">当前没有分配给你的任务。</p>
    </article>
  </section>
</template>
