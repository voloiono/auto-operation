import { createRouter, createWebHistory } from 'vue-router'
import ProjectList from '../views/ProjectList.vue'
import ProjectEditor from '../views/ProjectEditor.vue'
import ScheduleConfig from '../views/ScheduleConfig.vue'
import ExecutionHistory from '../views/ExecutionHistory.vue'
import Marketplace from '../views/Marketplace.vue'

const routes = [
  {
    path: '/',
    redirect: '/projects'
  },
  {
    path: '/projects',
    name: 'ProjectList',
    component: ProjectList
  },
  {
    path: '/projects/:id',
    name: 'ProjectEditor',
    component: ProjectEditor
  },
  {
    path: '/projects/:projectId/flows/:flowId/schedule',
    name: 'ScheduleConfig',
    component: ScheduleConfig
  },
  {
    path: '/projects/:projectId/flows/:flowId/history',
    name: 'ExecutionHistory',
    component: ExecutionHistory
  },
  {
    path: '/marketplace',
    name: 'Marketplace',
    component: Marketplace
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
