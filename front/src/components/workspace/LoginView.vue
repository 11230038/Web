<script setup>
import WorkspaceAlert from './WorkspaceAlert.vue'

defineProps({
  errorMessage: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  loginForm: { type: Object, required: true },
})

defineEmits(['submit'])
</script>

<template>
  <section class="login-screen">
    <div class="login-card">
      <p class="eyebrow">Mini Semester Workspace</p>
      <h1>任务协作控制台</h1>
      <p class="intro">登录后可以按角色查看项目、任务、进度、总结和成员信息。</p>

      <form class="login-form" @submit.prevent="$emit('submit')">
        <label>
          用户名
          <input v-model="loginForm.username" placeholder="请输入用户名" />
        </label>
        <label>
          密码
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </label>
        <button class="primary-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录系统' }}
        </button>
      </form>

      <WorkspaceAlert
        v-if="errorMessage"
        :message="errorMessage"
        title="登录失败"
        type="error"
      />
    </div>
  </section>
</template>
