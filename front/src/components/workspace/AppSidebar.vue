<script setup>
defineProps({
  activeMenu: { type: String, required: true },
  currentUser: { type: Object, required: true },
  menus: { type: Array, required: true },
  roleLabel: { type: Function, required: true },
})

defineEmits(['logout', 'switch-menu'])
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <p class="eyebrow">Workspace</p>
      <h2>项目协作台</h2>
    </div>

    <div class="user-panel">
      <strong>{{ currentUser.realName || currentUser.username }}</strong>
      <span>{{ roleLabel(currentUser.role) }}</span>
    </div>

    <nav class="menu">
      <button
        v-for="item in menus"
        :key="item.key"
        class="menu-item"
        :class="{ active: activeMenu === item.key }"
        @click="$emit('switch-menu', item.key)"
      >
        {{ item.label }}
      </button>
    </nav>

    <div class="sidebar-actions">
      <button class="ghost-btn" @click="$emit('logout')">退出登录</button>
    </div>
  </aside>
</template>
