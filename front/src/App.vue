<script setup>
import { computed } from 'vue'
import LoginView from './components/workspace/LoginView.vue'
import AppSidebar from './components/workspace/AppSidebar.vue'
import ProjectWorkspaceModal from './components/workspace/ProjectWorkspaceModal.vue'
import LogsPage from './components/workspace/pages/LogsPage.vue'
import MembersPage from './components/workspace/pages/MembersPage.vue'
import OverviewPage from './components/workspace/pages/OverviewPage.vue'
import ProfilePage from './components/workspace/pages/ProfilePage.vue'
import ProjectsPage from './components/workspace/pages/ProjectsPage.vue'
import SummariesPage from './components/workspace/pages/SummariesPage.vue'
import TasksPage from './components/workspace/pages/TasksPage.vue'
import { useWorkspaceApp } from './composables/useWorkspaceApp'

const app = useWorkspaceApp()

const pageComponent = computed(() => ({
  overview: OverviewPage,
  projects: ProjectsPage,
  tasks: TasksPage,
  logs: LogsPage,
  summaries: SummariesPage,
  members: MembersPage,
  profile: ProfilePage,
}[app.activeMenu.value] || OverviewPage))

const pageProps = computed(() => {
  const common = {
    collections: app.collections,
    currentUser: app.currentUser.value,
    editing: app.editing,
    editor: app.editor,
    isManager: app.isManager.value,
    saving: app.saving.value,
  }

  return {
    overview: {
      myTasks: app.myTasks.value,
      overviewCards: app.overviewCards.value,
      priorityLabel: app.priorityLabel,
      projectNameById: app.projectNameById,
      projectStatusLabel: app.projectStatusLabel,
    },
    projects: {
      canCreateProject: app.canCreateProject.value,
      collections: app.collections,
      isManager: app.isManager.value,
      priorityLabel: app.priorityLabel,
      projectStatusLabel: app.projectStatusLabel,
      userNameById: app.userNameById,
    },
    tasks: {
      ...common,
      priorityLabel: app.priorityLabel,
      projectNameById: app.projectNameById,
      projectStatusLabel: app.projectStatusLabel,
      userNameById: app.userNameById,
    },
    logs: {
      ...common,
      taskTitleById: app.taskTitleById,
      userNameById: app.userNameById,
    },
    summaries: {
      ...common,
      projectNameById: app.projectNameById,
      summaryTypeLabel: app.summaryTypeLabel,
      taskTitleById: app.taskTitleById,
      userNameById: app.userNameById,
    },
    members: {
      ...common,
      isAdmin: Number(app.currentUser.value?.role) === 0,
      roleLabel: app.roleLabel,
    },
    profile: {
      currentUser: app.currentUser.value,
      passwordForm: app.passwordForm,
      roleLabel: app.roleLabel,
      saving: app.saving.value,
    },
  }[app.activeMenu.value] || {}
})
</script>

<template>
  <div class="shell">
    <LoginView
      v-if="!app.isAuthed.value"
      :error-message="app.errorMessage.value"
      :loading="app.loading.value"
      :login-form="app.loginForm"
      @submit="app.login"
    />

    <template v-else>
      <AppSidebar
        :active-menu="app.activeMenu.value"
        :current-user="app.currentUser.value"
        :menus="app.visibleMenus.value"
        :role-label="app.roleLabel"
        @logout="app.logout"
        @switch-menu="app.activeMenu.value = $event"
      />

      <main class="main-panel">
        <div class="topbar">
          <div>
            <p class="eyebrow">Workspace</p>
            <h1>{{ app.activeMenuLabel.value }}</h1>
          </div>
          <div class="topbar-note">
            <span v-if="app.loading.value">正在同步数据...</span>
            <span v-else>当前账号：{{ app.currentUser.value.realName || app.currentUser.value.username }}</span>
          </div>
        </div>

        <p v-if="app.successMessage.value" class="banner success">{{ app.successMessage.value }}</p>
        <p v-if="app.errorMessage.value" class="banner error">{{ app.errorMessage.value }}</p>

        <component
          :is="pageComponent"
          v-bind="pageProps"
          @change-password="app.changePassword"
          @create-project="app.openProjectWorkspace()"
          @edit-log="app.editLog"
          @edit-member="app.editMember"
          @edit-project="app.editProject"
          @edit-summary="app.editSummary"
          @edit-task="app.editTask"
          @remove-log="app.removeLog"
          @remove-member="app.removeMember"
          @remove-project="app.removeProject"
          @remove-summary="app.removeSummary"
          @remove-task="app.removeTask"
          @reset-log="app.resetEditor('log')"
          @reset-member="app.resetEditor('member')"
          @reset-summary="app.resetEditor('summary')"
          @reset-task="app.resetEditor('task')"
          @submit-log="app.submitLog"
          @submit-member="app.submitMember"
          @submit-summary="app.submitSummary"
          @submit-task="app.submitTask"
          @update-task-status="app.updateMyTaskStatus"
        />
      </main>

      <ProjectWorkspaceModal
        :ai-form="app.aiForm"
        :breakdown-result="app.breakdownResult.value"
        :can-create-project="app.canCreateProject.value"
        :can-use-ai-workspace="app.canUseAiWorkspace.value"
        :can-view-project-form="app.canViewProjectForm.value"
        :collections="app.collections"
        :create-project-needs-breakdown="app.createProjectNeedsBreakdown.value"
        :editing="app.editing"
        :editor="app.editor"
        :open="app.projectWorkspaceOpen.value"
        :project-status-label="app.projectStatusLabel"
        :saving="app.saving.value"
        :selected-breakdown-indexes="app.selectedBreakdownIndexes.value"
        :show-breakdown-workspace="app.showBreakdownWorkspace.value"
        :user-name-by-id="app.userNameById"
        @close="app.closeProjectWorkspace"
        @generate-breakdown="app.generateProjectBreakdown"
        @import-breakdown="app.importSelectedBreakdownTasks"
        @reset-ai="app.resetAiWorkspace"
        @reset-project="app.resetEditor('project')"
        @submit-project="app.submitProject"
        @toggle-breakdown="app.toggleBreakdown"
        @update:create-project-needs-breakdown="app.createProjectNeedsBreakdown.value = $event"
      />
    </template>
  </div>
</template>
