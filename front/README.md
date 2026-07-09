# Frontend

前端基于 Vue 3 + Vite，负责登录、项目管理、任务管理、进度跟踪、总结中心、成员列表、个人信息，以及弹窗式新增/编辑/删除交互。

## 常用命令

```bash
npm install
npm run dev
npm run build
```

## 关键目录

- `src/components/workspace/`：业务页面与弹窗组件
- `src/composables/useWorkspaceApp.js`：全局业务状态与交互逻辑
- `src/components/workspace/PaginationControls.vue`：分页组件
- `src/components/workspace/ActionModal.vue`：统一确认/操作弹窗

更完整的项目说明见根目录 [README.md](/C:/Users/鄭昊/Desktop/Web/README.md)。
