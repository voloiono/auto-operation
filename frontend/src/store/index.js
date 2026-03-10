import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { projectApi, flowApi, moduleApi, scheduleApi, executionLogApi } from '../api'

export const useProjectStore = defineStore('project', () => {
  const projects = ref([])
  const currentProject = ref(null)
  const loading = ref(false)

  const fetchProjects = async () => {
    loading.value = true
    try {
      const response = await projectApi.list()
      projects.value = response.data
    } finally {
      loading.value = false
    }
  }

  const createProject = async (data) => {
    const response = await projectApi.create(data)
    projects.value.push(response.data)
    return response.data
  }

  const updateProject = async (id, data) => {
    const response = await projectApi.update(id, data)
    const index = projects.value.findIndex(p => p.id === id)
    if (index !== -1) {
      projects.value[index] = response.data
    }
    return response.data
  }

  const deleteProject = async (id) => {
    await projectApi.delete(id)
    projects.value = projects.value.filter(p => p.id !== id)
  }

  const setCurrentProject = (project) => {
    currentProject.value = project
  }

  return {
    projects,
    currentProject,
    loading,
    fetchProjects,
    createProject,
    updateProject,
    deleteProject,
    setCurrentProject
  }
})

export const useFlowStore = defineStore('flow', () => {
  const flows = ref([])
  const currentFlow = ref(null)
  const loading = ref(false)

  const fetchFlows = async (projectId) => {
    loading.value = true
    try {
      const response = await flowApi.list(projectId)
      flows.value = response.data
    } finally {
      loading.value = false
    }
  }

  const createFlow = async (data) => {
    const response = await flowApi.create(data)
    flows.value.push(response.data)
    return response.data
  }

  const updateFlow = async (id, data) => {
    const response = await flowApi.update(id, data)
    const index = flows.value.findIndex(f => f.id === id)
    if (index !== -1) {
      flows.value[index] = response.data
    }
    return response.data
  }

  const generateScript = async (id) => {
    const response = await flowApi.generate(id)
    const index = flows.value.findIndex(f => f.id === id)
    if (index !== -1) {
      flows.value[index] = response.data
    }
    return response.data
  }

  const executeFlow = async (id) => {
    const response = await flowApi.execute(id)
    return response.data // 返回 ExecutionLogDTO（含 id、status="running"）
  }

  /**
   * 轮询执行日志直到完成
   */
  const pollExecutionLog = (logId, onUpdate, intervalMs = 2000) => {
    const timer = setInterval(async () => {
      try {
        const response = await executionLogApi.get(logId)
        const log = response.data
        onUpdate(log)
        if (log.status !== 'running') {
          clearInterval(timer)
        }
      } catch (e) {
        clearInterval(timer)
        onUpdate({ status: 'failed', errorMessage: '轮询失败: ' + e.message })
      }
    }, intervalMs)
    return timer // 返回 timer 以便外部可手动取消
  }

  const deleteFlow = async (id) => {
    await flowApi.delete(id)
    flows.value = flows.value.filter(f => f.id !== id)
  }

  const setCurrentFlow = (flow) => {
    currentFlow.value = flow
  }

  return {
    flows,
    currentFlow,
    loading,
    fetchFlows,
    createFlow,
    updateFlow,
    generateScript,
    executeFlow,
    pollExecutionLog,
    deleteFlow,
    setCurrentFlow
  }
})

export const useModuleStore = defineStore('module', () => {
  const modules = ref([])
  const loading = ref(false)

  const fetchModules = async () => {
    loading.value = true
    try {
      const response = await moduleApi.list()
      modules.value = response.data
    } finally {
      loading.value = false
    }
  }

  const getModulesByCategory = (category) => {
    return modules.value.filter(m => m.category === category)
  }

  return {
    modules,
    loading,
    fetchModules,
    getModulesByCategory
  }
})

export const useScheduleStore = defineStore('schedule', () => {
  const schedules = ref([])
  const loading = ref(false)

  const fetchSchedules = async (flowId) => {
    loading.value = true
    try {
      const response = await scheduleApi.list(flowId)
      schedules.value = response.data
    } finally {
      loading.value = false
    }
  }

  const createSchedule = async (data) => {
    const response = await scheduleApi.create(data)
    schedules.value.push(response.data)
    return response.data
  }

  const updateSchedule = async (id, data) => {
    const response = await scheduleApi.update(id, data)
    const index = schedules.value.findIndex(s => s.id === id)
    if (index !== -1) {
      schedules.value[index] = response.data
    }
    return response.data
  }

  const deleteSchedule = async (id) => {
    await scheduleApi.delete(id)
    schedules.value = schedules.value.filter(s => s.id !== id)
  }

  return {
    schedules,
    loading,
    fetchSchedules,
    createSchedule,
    updateSchedule,
    deleteSchedule
  }
})

export const useExecutionLogStore = defineStore('executionLog', () => {
  const logs = ref([])
  const loading = ref(false)

  const fetchLogs = async (flowId) => {
    loading.value = true
    try {
      const response = await executionLogApi.list(flowId)
      logs.value = response.data
    } finally {
      loading.value = false
    }
  }

  return {
    logs,
    loading,
    fetchLogs
  }
})
