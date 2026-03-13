import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { projectApi, flowApi, moduleApi, scheduleApi, executionLogApi, createExecutionStream, marketplaceApi } from '../api'

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
   * SSE 实时日志流
   * @param {number} logId
   * @param {function} onLine - 每行输出回调 (line: string)
   * @param {function} onComplete - 完成回调 (status: string)
   * @returns {EventSource} 可用于手动关闭
   */
  const streamExecutionLog = (logId, onLine, onComplete) => {
    const es = createExecutionStream(logId)
    let hasReceivedData = false

    es.addEventListener('output', (e) => {
      hasReceivedData = true
      onLine(e.data)
    })

    es.addEventListener('catchup', (e) => {
      hasReceivedData = true
      // 补全历史日志
      const lines = e.data.split('\n').filter(l => l.trim())
      lines.forEach(line => onLine(line))
    })

    es.addEventListener('complete', (e) => {
      onComplete(e.data)
      es.close()
    })

    es.onerror = (err) => {
      console.error('SSE connection error:', err)
      es.close()

      // 如果从未收到数据，可能是连接失败
      if (!hasReceivedData) {
        onLine('[系统] SSE 连接失败，正在尝试获取执行日志...')
        // 尝试获取最终状态
        executionLogApi.get(logId).then(response => {
          const log = response.data
          if (log.output) {
            log.output.split('\n').forEach(line => onLine(line))
          }
          onComplete(log.status)
        }).catch(() => {
          onComplete('disconnected')
        })
      } else {
        // 已收到部分数据，尝试获取最终状态
        onComplete('disconnected')
      }
    }

    return es
  }

  /**
   * 停止正在执行的脚本
   */
  const stopExecution = async (logId) => {
    await executionLogApi.stop(logId)
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
    streamExecutionLog,
    stopExecution,
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

  const createModule = async (data) => {
    const response = await moduleApi.create(data)
    modules.value.push(response.data)
    return response.data
  }

  const updateModule = async (id, data) => {
    const response = await moduleApi.update(id, data)
    const index = modules.value.findIndex(m => m.id === id)
    if (index !== -1) {
      modules.value[index] = response.data
    }
    return response.data
  }

  const deleteModule = async (id) => {
    await moduleApi.delete(id)
    modules.value = modules.value.filter(m => m.id !== id)
  }

  return {
    modules,
    loading,
    fetchModules,
    getModulesByCategory,
    createModule,
    updateModule,
    deleteModule
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

export const useMarketplaceStore = defineStore('marketplace', () => {
  const items = ref([])
  const loading = ref(false)

  const fetchItems = async (params = {}) => {
    loading.value = true
    try {
      const response = await marketplaceApi.list(params)
      items.value = response.data
    } finally {
      loading.value = false
    }
  }

  const publishModule = async (moduleId) => {
    const response = await marketplaceApi.publish(moduleId)
    return response.data
  }

  const installModule = async (moduleId) => {
    const response = await marketplaceApi.install(moduleId)
    return response.data
  }

  const unpublishModule = async (moduleId) => {
    await marketplaceApi.unpublish(moduleId)
    items.value = items.value.filter(i => i.moduleId !== moduleId)
  }

  return {
    items,
    loading,
    fetchItems,
    publishModule,
    installModule,
    unpublishModule
  }
})
