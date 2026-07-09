<script setup>
import ActionModal from '../ActionModal.vue'

defineProps({
  currentUser: { type: Object, required: true },
  passwordForm: { type: Object, required: true },
  passwordModalOpen: { type: Boolean, default: false },
  profileForm: { type: Object, required: true },
  profileModalOpen: { type: Boolean, default: false },
  roleLabel: { type: Function, required: true },
  saving: { type: Boolean, default: false },
})

defineEmits([
  'change-password',
  'close-password-modal',
  'close-profile-modal',
  'open-password-modal',
  'open-profile-modal',
  'submit-profile',
])
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <h3>个人信息</h3>
        <div class="actions">
          <button class="primary-btn" @click="$emit('open-profile-modal')">编辑个人信息</button>
          <button class="ghost-btn" @click="$emit('open-password-modal')">修改密码</button>
        </div>
      </div>

      <div class="profile-card">
        <p><strong>姓名：</strong>{{ currentUser.realName || '-' }}</p>
        <p><strong>用户名：</strong>{{ currentUser.username || '-' }}</p>
        <p><strong>角色：</strong>{{ roleLabel(currentUser.role) }}</p>
        <p><strong>邮箱：</strong>{{ currentUser.email || '-' }}</p>
        <p><strong>电话：</strong>{{ currentUser.phone || '-' }}</p>
      </div>
    </article>

    <ActionModal :open="profileModalOpen" title="编辑个人信息" @close="$emit('close-profile-modal')">
      <form class="form-grid" @submit.prevent="$emit('submit-profile')">
        <label>
          姓名
          <input v-model="profileForm.realName" required />
        </label>

        <label>
          用户名
          <input v-model="profileForm.username" required />
        </label>

        <label>
          邮箱
          <input v-model="profileForm.email" type="email" />
        </label>

        <label>
          电话
          <input v-model="profileForm.phone" />
        </label>

        <button class="primary-btn full" :disabled="saving">
          {{ saving ? '提交中...' : '保存个人信息' }}
        </button>
      </form>
    </ActionModal>

    <ActionModal :open="passwordModalOpen" title="修改密码" @close="$emit('close-password-modal')">
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
    </ActionModal>
  </section>
</template>
