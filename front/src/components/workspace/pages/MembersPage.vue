<script setup>
import { computed, ref, watch } from 'vue'
import ActionModal from '../ActionModal.vue'
import PaginationControls from '../PaginationControls.vue'

const props = defineProps({
  collections: { type: Object, required: true },
  editing: { type: Object, required: true },
  editor: { type: Object, required: true },
  isAdmin: { type: Boolean, default: false },
  modalOpen: { type: Boolean, default: false },
  roleLabel: { type: Function, required: true },
  saving: { type: Boolean, default: false },
})

defineEmits([
  'close-member-modal',
  'edit-member',
  'open-member-create',
  'remove-member',
  'submit-member',
])

const page = ref(1)
const pageSize = ref(5)
const pageSizeOptions = [5, 10, 12]

const totalPages = computed(() => Math.max(1, Math.ceil(props.collections.members.length / pageSize.value)))
const pagedMembers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return props.collections.members.slice(start, start + pageSize.value)
})

watch([() => props.collections.members.length, pageSize], () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})
</script>

<template>
  <section class="page-grid">
    <article class="panel wide">
      <div class="panel-head">
        <h3>成员列表</h3>
        <div class="actions">
          <button v-if="isAdmin" class="primary-btn" @click="$emit('open-member-create')">
            新增成员
          </button>
        </div>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>用户名</th>
            <th>角色</th>
            <th>邮箱</th>
            <th>电话</th>
            <th v-if="isAdmin">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedMembers" :key="item.id">
            <td>{{ item.realName }}</td>
            <td>{{ item.username }}</td>
            <td>{{ roleLabel(item.role) }}</td>
            <td>{{ item.email || '-' }}</td>
            <td>{{ item.phone || '-' }}</td>
            <td v-if="isAdmin" class="actions">
              <button class="text-btn" @click="$emit('edit-member', item)">编辑</button>
              <button class="text-btn danger" @click="$emit('remove-member', item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <PaginationControls
        :page="page"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total="collections.members.length"
        :total-pages="totalPages"
        @update:page="page = $event"
        @update:page-size="pageSize = $event; page = 1"
      />
    </article>

    <ActionModal
      :open="(isAdmin || editing.memberId) && modalOpen"
      :title="editing.memberId ? '编辑成员' : '新增成员'"
      @close="$emit('close-member-modal')"
    >
      <form class="form-grid" @submit.prevent="$emit('submit-member')">
        <label>
          用户名
          <input v-model="editor.member.username" required />
        </label>

        <label>
          姓名
          <input v-model="editor.member.realName" required />
        </label>

        <label v-if="editing.memberId">
          角色
          <input :value="roleLabel(editor.member.role)" readonly />
        </label>

        <label v-else>
          角色
          <select v-model.number="editor.member.role">
            <option :value="0">管理员</option>
            <option :value="1">项目负责人</option>
            <option :value="2">成员</option>
          </select>
        </label>

        <label v-if="!editing.memberId">
          密码
          <input
            v-model="editor.member.password"
            type="password"
            placeholder="请输入密码"
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
    </ActionModal>
  </section>
</template>
