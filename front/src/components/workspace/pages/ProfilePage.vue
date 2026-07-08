<script setup>
defineProps({
  currentUser: { type: Object, required: true },
  passwordForm: { type: Object, required: true },
  roleLabel: { type: Function, required: true },
  saving: { type: Boolean, default: false },
})

defineEmits(['change-password'])
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <div class="panel-head">
        <h3>账号信息</h3>
      </div>

      <div class="profile-card">
        <p><strong>姓名：</strong>{{ currentUser.realName || '-' }}</p>
        <p><strong>用户名：</strong>{{ currentUser.username }}</p>
        <p><strong>角色：</strong>{{ roleLabel(currentUser.role) }}</p>
      </div>
    </article>

    <article class="panel">
      <div class="panel-head">
        <h3>修改密码</h3>
      </div>

      <form class="form-grid" @submit.prevent="$emit('change-password')">
        <label>
          当前密码
          <input v-model="passwordForm.oldPassword" type="password" required />
        </label>

        <label>
          新密码
          <input v-model="passwordForm.newPassword" type="password" required />
        </label>

        <button class="primary-btn full" :disabled="saving">
          {{ saving ? '提交中...' : '更新密码' }}
        </button>
      </form>
    </article>
  </section>
</template>
