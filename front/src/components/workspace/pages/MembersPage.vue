<script setup>
defineProps({
  collections: { type: Object, required: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  isAdmin: { type: Boolean, default: false },
  isManager: { type: Boolean, default: false },
  roleLabel: { type: Function, required: true },
  saving: { type: Boolean, default: false },
})

defineEmits(['edit-member', 'remove-member', 'reset-member', 'submit-member'])
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <h3>成员列表</h3>
        <span>{{ collections.members.length }} 人</span>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>用户名</th>
            <th>角色</th>
            <th>邮箱</th>
            <th>电话</th>
            <th v-if="isManager">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in collections.members" :key="item.id">
            <td>{{ item.realName }}</td>
            <td>{{ item.username }}</td>
            <td>{{ roleLabel(item.role) }}</td>
            <td>{{ item.email || '-' }}</td>
            <td>{{ item.phone || '-' }}</td>
            <td v-if="isManager" class="actions">
              <button class="text-btn" @click="$emit('edit-member', item)">编辑</button>
              <button class="text-btn danger" @click="$emit('remove-member', item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </article>

    <article v-if="isAdmin || editing.memberId" class="panel">
      <div class="panel-head">
        <h3>{{ editing.memberId ? '编辑成员' : '新增成员' }}</h3>
        <button class="text-btn" @click="$emit('reset-member')">清空</button>
      </div>

      <form class="form-grid" @submit.prevent="$emit('submit-member')">
        <label>
          用户名
          <input v-model="editor.member.username" required />
        </label>

        <label>
          姓名
          <input v-model="editor.member.realName" required />
        </label>

        <label>
          角色
          <select v-model.number="editor.member.role">
            <option :value="0">管理员</option>
            <option :value="1">项目负责人</option>
            <option :value="2">成员</option>
          </select>
        </label>

        <label>
          密码
          <input
            v-model="editor.member.password"
            type="password"
            :placeholder="editing.memberId ? '留空表示不修改密码' : '请输入密码'"
          />
        </label>

        <label>
          邮箱
          <input v-model="editor.member.email" type="email" />
        </label>

        <label>
          电话
          <input v-model="editor.member.phone" />
        </label>

        <button class="primary-btn full" :disabled="saving">
          {{ saving ? '提交中...' : editing.memberId ? '保存成员' : '新增成员' }}
        </button>
      </form>
    </article>
  </section>
</template>
