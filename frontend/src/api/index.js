import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const projectApi = {
  list: () => api.get('/projects'),
  get: (id) => api.get(`/projects/${id}`),
  create: (data) => api.post('/projects', data),
  update: (id, data) => api.put(`/projects/${id}`, data),
  delete: (id) => api.delete(`/projects/${id}`)
}

export const flowApi = {
  list: (projectId) => api.get(`/flows/project/${projectId}`),
  get: (id) => api.get(`/flows/${id}`),
  getOrCreate: (projectId) => api.get(`/flows/project/${projectId}/main`),
  create: (data) => api.post('/flows', data),
  update: (id, data) => api.put(`/flows/${id}`, data),
  generate: (id) => api.post(`/flows/${id}/generate`),
  execute: (id) => api.post(`/flows/${id}/execute`),
  delete: (id) => api.delete(`/flows/${id}`)
}

export const moduleApi = {
  list: () => api.get('/modules'),
  get: (moduleId) => api.get(`/modules/${moduleId}`),
  listByCategory: (category) => api.get(`/modules/category/${category}`)
}

export const scheduleApi = {
  list: (flowId) => api.get(`/schedules/flow/${flowId}`),
  get: (id) => api.get(`/schedules/${id}`),
  create: (data) => api.post('/schedules', data),
  update: (id, data) => api.put(`/schedules/${id}`, data),
  delete: (id) => api.delete(`/schedules/${id}`)
}

export const executionLogApi = {
  list: (flowId) => api.get(`/execution-logs/flow/${flowId}`),
  get: (id) => api.get(`/execution-logs/${id}`)
}

export const settingsApi = {
  getAll: () => api.get('/settings'),
  getGroup: (group) => api.get(`/settings/${group}`),
  saveGroup: (group, data) => api.put(`/settings/${group}`, data)
}

export default api
