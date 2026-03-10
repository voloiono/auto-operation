<template>
  <div class="flow-editor">
    <el-card class="editor-card glass">
      <template #header>
        <div class="editor-header">
          <div>
            <h3>流程编辑器</h3>
            <p class="subtitle">拖拽模块构建自动化流程</p>
          </div>
          <div class="editor-actions">
            <el-button type="primary" @click="generateScript" :loading="generating">
              <el-icon><DocumentCopy /></el-icon>
              生成脚本
            </el-button>
            <el-button type="info" @click="showScriptPreview" :disabled="!generatedScript">
              <el-icon><View /></el-icon>
              查看脚本
            </el-button>
            <el-button type="warning" @click="downloadScript" :disabled="!generatedScript">
              <el-icon><Download /></el-icon>
              下载脚本
            </el-button>
            <el-button type="success" @click="showPackageDialog" :loading="generating">
              <el-icon><Box /></el-icon>
              打包程序
            </el-button>
            <el-button type="success" @click="executeFlow" :loading="executing">
              <el-icon><VideoPlay /></el-icon>
              执行
            </el-button>
          </div>
        </div>
      </template>

      <div
        class="editor-canvas"
        ref="canvasRef"
        @drop.prevent="handleCanvasDrop($event)"
        @dragover.prevent="handleCanvasDragOver($event)"
        @dragleave="handleCanvasDragLeave"
      >
        <div v-if="nodes.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <p>从左侧拖拽模块到此处</p>
          <p class="empty-hint">开始构建您的自动化流程</p>
        </div>
        <div v-else class="canvas-layout">
          <!-- 左侧：全程监控弹窗条 -->
          <Transition name="sidebar-slide">
          <TransitionGroup name="monitor-enter" tag="div" class="monitor-sidebar" v-if="monitorNodes.length > 0">
              <div
                v-for="(node, mIdx) in monitorNodes"
                :key="node.id"
                class="monitor-bar"
                :class="{ selected: selectedNodeId === node.id, 'error-type': node.type === 'error_monitor', 'scheduled-type': node.type === 'scheduled_task' }"
                @click="selectNode(node, getOriginalIndex(node))"
              >
                <div class="monitor-bar-header">
                  <span class="monitor-bar-icon">{{ node.type === 'error_monitor' ? '📋' : node.type === 'scheduled_task' ? '⏰' : '🛡️' }}</span>
                  <span class="monitor-bar-title">{{ node.name }}</span>
                  <div class="node-btn remove-btn" @click.stop="removeNode(getOriginalIndex(node))">
                    <el-icon><Close /></el-icon>
                  </div>
                </div>
                <div class="monitor-bar-body">
                  <div class="monitor-bar-label">{{ node.type === 'error_monitor' ? '错误监控' : node.type === 'scheduled_task' ? '定时执行' : '全程监控' }}</div>
                  <div class="monitor-bar-info">
                    <span v-if="node.type === 'error_monitor' && node.params.log_folder">{{ node.params.log_folder }}</span>
                    <span v-else-if="node.type === 'scheduled_task' && node.params.schedule_times">{{ node.params.schedule_times }}</span>
                    <span v-else-if="node.params.close_method">{{ getDisplayValue('close_method', node.params.close_method) }}</span>
                  </div>
                  <div class="monitor-bar-track"></div>
                </div>
              </div>
          </TransitionGroup>
          </Transition>

          <!-- 右侧：普通流程节点 -->
          <div class="nodes-container">
          <template v-for="(node, index) in nodes" :key="node.id">
            <Transition name="node-to-flow">
            <div v-if="!isMonitorNode(node)" class="node-wrapper">
            <!-- 节点前方的插入指示器（纯视觉） -->
            <div
              class="drop-indicator"
              :class="{ active: dropTargetIndex === index }"
            >
              <div class="drop-indicator-line">
                <span class="drop-indicator-label">放置到此处</span>
              </div>
            </div>

            <!-- 节点卡片 -->
            <div
              class="node"
              :class="{
                selected: selectedNodeId === node.id,
                dragging: dragSourceIndex === index,
                'fly-in': node._justAdded
              }"
              draggable="true"
              @dragstart="handleNodeDragStart(index, $event)"
              @dragend="handleNodeDragEnd"
              @click="selectNode(node, index)"
            >
              <div class="node-header">
                <span class="node-number">{{ getFlowStepNumber(index) }}</span>
                <span class="node-title">{{ node.name }}</span>
                <div class="node-actions">
                  <div
                    class="node-btn move-btn"
                    :class="{ disabled: index === 0 }"
                    @click.stop="moveNode(index, -1)"
                    title="上移"
                  >
                    <el-icon><Top /></el-icon>
                  </div>
                  <div
                    class="node-btn move-btn"
                    :class="{ disabled: index === nodes.length - 1 }"
                    @click.stop="moveNode(index, 1)"
                    title="下移"
                  >
                    <el-icon><Bottom /></el-icon>
                  </div>
                  <div class="node-btn remove-btn" @click.stop="removeNode(index)">
                    <el-icon><Close /></el-icon>
                  </div>
                </div>
              </div>
              <div class="node-body">
                <div v-if="Object.keys(node.params).length > 0" class="params-preview">
                  <div v-for="(value, key) in node.params" :key="key" class="param-item">
                    <span class="param-key">{{ getStaticParamLabel(key) }}:</span>
                    <span class="param-value">{{ getDisplayValue(key, value) }}</span>
                  </div>
                </div>
                <div v-else class="no-params">点击右侧编辑参数</div>
              </div>
            </div>
            </div>
            </Transition>
          </template>

          <!-- 末尾插入指示器（纯视觉） -->
          <div
            class="drop-indicator"
            :class="{ active: dropTargetIndex === nodes.length }"
          >
            <div class="drop-indicator-line">
              <span class="drop-indicator-label">放置到末尾</span>
            </div>
          </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 脚本预览对话框 -->
    <el-dialog v-model="showScriptDialog" title="生成的脚本" width="900px" class="custom-dialog">
      <div class="script-preview">
        <pre>{{ generatedScript }}</pre>
      </div>
      <template #footer>
        <el-button @click="showScriptDialog = false">关闭</el-button>
        <el-button type="primary" @click="downloadScript">下载脚本</el-button>
      </template>
    </el-dialog>

    <!-- 打包对话框 -->
    <el-dialog v-model="showPackageDialogFlag" title="打包为可执行程序" width="600px" class="custom-dialog">
      <el-form label-width="120px">
        <el-form-item label="选择平台">
          <el-radio-group v-model="packagePlatform">
            <el-radio label="windows">Windows (.exe)</el-radio>
            <el-radio label="macos">macOS (.app)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="运行环境">
          <el-radio-group v-model="packageNetworkMode">
            <el-radio label="online">
              <span>在线模式</span>
              <el-tag size="small" type="success" style="margin-left: 8px">推荐</el-tag>
            </el-radio>
            <el-radio label="offline">
              <span>离线模式</span>
              <el-tag size="small" type="warning" style="margin-left: 8px">内置驱动</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 离线模式：浏览器版本选择 -->
        <template v-if="packageNetworkMode === 'offline'">
          <el-divider content-position="left">目标环境浏览器版本</el-divider>

          <el-alert
            type="info"
            :closable="false"
            style="margin-bottom: 15px"
          >
            <template #title>
              <span style="display: flex; align-items: center; gap: 8px;">
                <span>如何获取目标机器的浏览器版本？</span>
                <el-button size="small" type="primary" link @click="downloadDetectTool">
                  下载检测工具
                </el-button>
              </span>
            </template>
          </el-alert>

          <!-- 根据流程中使用的浏览器动态显示 -->
          <!-- IE/Firefox 驱动版本与浏览器版本无关，自动处理，无需用户填写 -->
          <template v-for="browser in detectedBrowsers" :key="browser.type">
            <el-form-item
              v-if="browser.type !== 'ie' && browser.type !== 'firefox'"
              :label="browser.label + '版本'"
            >
              <el-select
                v-model="browserVersions[browser.type]"
                filterable
                allow-create
                default-first-option
                placeholder="输入或选择版本号"
                style="width: 200px"
              >
                <el-option
                  v-for="ver in getVersionHistory(browser.type)"
                  :key="ver"
                  :label="ver"
                  :value="ver"
                />
              </el-select>
              <span style="margin-left: 10px; color: #909399; font-size: 12px">
                例如: 143、144、145
              </span>
            </el-form-item>
            <el-alert
              v-else-if="browser.type === 'ie'"
              type="info"
              :closable="false"
              style="margin-bottom: 15px"
            >
              <template #title>
                IE 驱动（IEDriverServer 4.14.0）将自动下载
              </template>
            </el-alert>
            <el-alert
              v-else-if="browser.type === 'firefox'"
              type="info"
              :closable="false"
              style="margin-bottom: 15px"
            >
              <template #title>
                Firefox 驱动（geckodriver 0.34.0）将自动下载
              </template>
            </el-alert>
          </template>

          <el-alert
            title="离线模式说明"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px"
          >
            <template #default>
              <ul style="margin: 0; padding-left: 20px; line-height: 1.8">
                <li>请确保 <code>python/drivers/</code> 目录下有对应版本的驱动</li>
                <li>驱动命名格式：<code>msedgedriver_143.exe</code>、<code>chromedriver_120.exe</code></li>
                <li>程序会自动选择匹配版本的驱动打包</li>
              </ul>
            </template>
          </el-alert>
        </template>

        <el-alert
          v-if="packageNetworkMode === 'online'"
          title="在线模式说明"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #default>
            <ul style="margin: 0; padding-left: 20px; line-height: 1.8">
              <li>程序体积较小（约 10MB）</li>
              <li>首次运行需要联网下载浏览器驱动</li>
              <li>自动匹配目标机器的浏览器版本</li>
            </ul>
          </template>
        </el-alert>

      </el-form>
      <template #footer>
        <el-button @click="showPackageDialogFlag = false">取消</el-button>
        <el-button type="primary" @click="packageScript" :loading="packaging">打包</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useFlowStore } from '../store'
import { DocumentCopy, VideoPlay, Close, View, Download, Box, Top, Bottom } from '@element-plus/icons-vue'
import { getStaticParamLabel, getDisplayValue } from '../utils/paramLabels'

const props = defineProps({
  flowId: {
    type: Number,
    required: false
  }
})

const flowStore = useFlowStore()
const nodes = ref([])
const selectedNodeId = ref(null)
const selectedNodeIndex = ref(null)
const generating = ref(false)
const executing = ref(false)
const generatedScript = ref(null)
const showScriptDialog = ref(false)
const showPackageDialogFlag = ref(false)
const packagePlatform = ref('windows')
const packageNetworkMode = ref('online')
const packaging = ref(false)
const canvasRef = ref(null)

// 浏览器版本相关
const browserVersions = ref({})  // { edge: '143', chrome: '120' }
const BROWSER_VERSION_STORAGE_KEY = 'auto_operation_browser_versions'

// 浏览器类型映射
const browserLabels = {
  edge: 'Edge',
  chrome: 'Chrome',
  firefox: 'Firefox',
  ie: 'IE'
}

// 从流程中检测使用的浏览器类型
const detectedBrowsers = computed(() => {
  const browsers = new Set()
  nodes.value.forEach(node => {
    if (node.type === 'open_browser' && node.params?.browser_type) {
      browsers.add(node.params.browser_type)
    }
  })
  // 如果没有检测到，默认显示 edge
  if (browsers.size === 0) {
    browsers.add('edge')
  }
  return Array.from(browsers).map(type => ({
    type,
    label: browserLabels[type] || type
  }))
})

// 获取浏览器版本历史（从 localStorage）
const getVersionHistory = (browserType) => {
  try {
    const stored = localStorage.getItem(BROWSER_VERSION_STORAGE_KEY)
    if (stored) {
      const data = JSON.parse(stored)
      return data[browserType] || []
    }
  } catch (e) {
    console.error('Failed to load version history:', e)
  }
  return []
}

// 保存浏览器版本到历史
const saveVersionHistory = (browserType, version) => {
  if (!version) return
  try {
    const stored = localStorage.getItem(BROWSER_VERSION_STORAGE_KEY)
    const data = stored ? JSON.parse(stored) : {}
    if (!data[browserType]) {
      data[browserType] = []
    }
    // 添加到列表开头，去重，最多保留10个
    data[browserType] = [version, ...data[browserType].filter(v => v !== version)].slice(0, 10)
    localStorage.setItem(BROWSER_VERSION_STORAGE_KEY, JSON.stringify(data))
  } catch (e) {
    console.error('Failed to save version history:', e)
  }
}

// 下载浏览器版本检测工具
const downloadDetectTool = async () => {
  try {
    const response = await fetch('/api/tools/detect-browser')
    if (!response.ok) throw new Error('下载失败')
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'detect_browser_version.bat'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    ElMessage.success('检测工具已下载，请在目标机器上运行')
  } catch (error) {
    ElMessage.error('下载失败: ' + error.message)
  }
}
let nodeCounter = 0
let saveTimer = null
let saving = false

// 拖拽状态
const dragSourceIndex = ref(null)
const dropTargetIndex = ref(null)

// ==================== 全程监控节点检测 ====================
const isMonitorNode = (node) => {
  return (node.type === 'close_popup' && node.params?.monitor_mode === 'continuous') || node.type === 'error_monitor' || node.type === 'scheduled_task'
}

const monitorNodes = computed(() => {
  return nodes.value.filter(n => isMonitorNode(n))
})

const getOriginalIndex = (node) => {
  return nodes.value.findIndex(n => n.id === node.id)
}

// 流程步骤编号（跳过监控节点）
const getFlowStepNumber = (index) => {
  let step = 0
  for (let i = 0; i <= index; i++) {
    if (!isMonitorNode(nodes.value[i])) {
      step++
    }
  }
  return step
}

// ==================== 构建/解析流程配置 ====================
const buildFlowConfig = () => {
  return {
    modules: nodes.value.map((node, index) => ({
      id: node.id,
      type: node.type,
      name: node.name,
      params: node.params
    })),
    connections: []
  }
}

const restoreNodesFromConfig = (configStr) => {
  try {
    const config = JSON.parse(configStr)
    if (!config.modules || !Array.isArray(config.modules)) return
    nodes.value = config.modules.map((mod, index) => {
      nodeCounter = Math.max(nodeCounter, index + 1)
      return {
        id: mod.id || `node_${index}`,
        type: mod.type,
        name: mod.name,
        params: mod.params || {}
      }
    })
  } catch (e) {
    // 配置为空或解析失败，保持空数组
  }
}

// ==================== 自动保存（防抖 800ms） ====================
const saveFlowConfig = () => {
  if (!props.flowId || saving) return
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(async () => {
    saving = true
    try {
      const updateData = {
        name: flowStore.currentFlow?.name || '默认流程',
        configuration: JSON.stringify(buildFlowConfig()),
        status: 'draft'
      }
      await flowStore.updateFlow(props.flowId, updateData)
    } catch (e) {
      console.error('自动保存失败:', e)
    } finally {
      saving = false
    }
  }, 800)
}

// 深度监听 nodes 变化 → 自动保存（覆盖参数编辑、增删排序所有场景）
watch(nodes, () => {
  saveFlowConfig()
}, { deep: true })

// ==================== 页面加载 → 从数据库恢复 ====================
onMounted(async () => {
  if (props.flowId) {
    try {
      const { flowApi } = await import('../api')
      const response = await flowApi.get(props.flowId)
      const flowData = response.data
      if (flowData.configuration) {
        restoreNodesFromConfig(flowData.configuration)
      }
      if (flowData.generatedScript) {
        generatedScript.value = flowData.generatedScript
      }
      flowStore.currentFlow = {
        ...flowStore.currentFlow,
        name: flowData.name,
        nodes: nodes.value
      }
    } catch (e) {
      console.error('加载流程失败:', e)
    }
  }
})

onUnmounted(() => {
  if (saveTimer) clearTimeout(saveTimer)
})

const parseInputSchema = (schema) => {
  try {
    const parsed = JSON.parse(schema)
    const params = {}
    for (const key in parsed) {
      // input_list 类型初始化为空数组（用于 batch_input 的动态输入列表）
      params[key] = parsed[key] === 'input_list' ? [] : ''
    }
    return params
  } catch (e) {
    return {}
  }
}

// ==================== 计算插入位置 ====================
const calcDropIndex = (clientY) => {
  if (!canvasRef.value) return nodes.value.length
  const nodeEls = canvasRef.value.querySelectorAll('.node')
  if (nodeEls.length === 0) return 0

  for (let i = 0; i < nodeEls.length; i++) {
    const rect = nodeEls[i].getBoundingClientRect()
    const midY = rect.top + rect.height / 2
    if (clientY < midY) return i
  }
  return nodes.value.length
}

// ==================== 画布级拖放处理 ====================
const handleCanvasDragOver = (event) => {
  event.dataTransfer.dropEffect = dragSourceIndex.value !== null ? 'move' : 'copy'
  dropTargetIndex.value = calcDropIndex(event.clientY)
}

const handleCanvasDragLeave = (event) => {
  // 只有真正离开画布时才清除（忽略进入子元素触发的 dragleave）
  if (!canvasRef.value.contains(event.relatedTarget)) {
    dropTargetIndex.value = null
  }
}

const handleCanvasDrop = (event) => {
  const targetIndex = calcDropIndex(event.clientY)
  dropTargetIndex.value = null

  // 情况 1：从画布内拖拽排序
  const nodeIndexStr = event.dataTransfer.getData('node-index')
  if (nodeIndexStr !== '') {
    const fromIndex = parseInt(nodeIndexStr)
    if (fromIndex !== targetIndex && fromIndex !== targetIndex - 1) {
      reorderNode(fromIndex, targetIndex)
    }
    return
  }

  // 情况 2：从模块面板拖入
  insertModuleFromEvent(event, targetIndex)
}

// ==================== 节点拖拽排序 ====================
const handleNodeDragStart = (index, event) => {
  dragSourceIndex.value = index
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('node-index', String(index))
}

const handleNodeDragEnd = () => {
  dragSourceIndex.value = null
  dropTargetIndex.value = null
}

// ==================== 节点操作 ====================
const createNodeFromModule = (module) => {
  return {
    id: `node_${nodeCounter++}`,
    type: module.moduleId,
    name: module.name,
    description: module.description,
    inputSchema: module.inputSchema,
    params: parseInputSchema(module.inputSchema),
    _justAdded: true  // 标记用于飞入动画
  }
}

const insertModuleFromEvent = (event, targetIndex) => {
  const moduleData = event.dataTransfer.getData('module')
  if (!moduleData) return

  const module = JSON.parse(moduleData)
  const node = createNodeFromModule(module)
  nodes.value.splice(targetIndex, 0, node)
  selectNode(node, targetIndex)
  clearJustAdded(node)
  ElMessage.success(`已添加模块: ${module.name}`)
}

/**
 * 点击添加模块（供父组件通过 ref 调用）
 */
const addModule = (module) => {
  const node = createNodeFromModule(module)
  nodes.value.push(node)
  selectNode(node, nodes.value.length - 1)
  clearJustAdded(node)
  ElMessage.success(`已添加模块: ${module.name}`)

  // 滚动到底部让新节点可见
  nextTick(() => {
    if (canvasRef.value) {
      canvasRef.value.scrollTo({ top: canvasRef.value.scrollHeight, behavior: 'smooth' })
    }
  })
}

const clearJustAdded = (node) => {
  setTimeout(() => { delete node._justAdded }, 600)
}

const reorderNode = (fromIndex, toIndex) => {
  const node = nodes.value.splice(fromIndex, 1)[0]
  // splice 后数组缩短，需要调整目标索引
  const adjustedIndex = toIndex > fromIndex ? toIndex - 1 : toIndex
  nodes.value.splice(adjustedIndex, 0, node)
  selectNode(node, adjustedIndex)
}

const moveNode = (index, direction) => {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= nodes.value.length) return

  const node = nodes.value[index]
  nodes.value.splice(index, 1)
  nodes.value.splice(newIndex, 0, node)
  selectNode(node, newIndex)
}

const selectNode = (node, index) => {
  selectedNodeId.value = node.id
  selectedNodeIndex.value = index
  flowStore.currentFlow = {
    ...flowStore.currentFlow,
    selectedNode: node,
    selectedNodeIndex: index,
    nodes: nodes.value
  }
}

const updateNodeParam = (key, value) => {
  if (selectedNodeIndex.value !== null && nodes.value[selectedNodeIndex.value]) {
    nodes.value[selectedNodeIndex.value].params[key] = value
  }
}

const removeNode = (index) => {
  const removed = nodes.value.splice(index, 1)
  ElMessage.success(`已移除模块: ${removed[0].name}`)
  if (selectedNodeIndex.value === index) {
    selectedNodeId.value = null
    selectedNodeIndex.value = null
  }
}

// ==================== 确保保存完成后再执行操作 ====================
const flushSave = () => {
  return new Promise((resolve) => {
    if (saveTimer) clearTimeout(saveTimer)
    if (!props.flowId) { resolve(); return }
    const updateData = {
      name: flowStore.currentFlow?.name || '默认流程',
      configuration: JSON.stringify(buildFlowConfig()),
      status: 'draft'
    }
    flowStore.updateFlow(props.flowId, updateData).then(resolve).catch(resolve)
  })
}

const generateScript = async () => {
  try {
    generating.value = true
    if (!props.flowId) {
      ElMessage.error('流程ID不存在，请刷新页面重试')
      return
    }
    await flushSave()
    const result = await flowStore.generateScript(props.flowId)
    generatedScript.value = result.generatedScript
    ElMessage.success('脚本生成成功')
  } catch (error) {
    console.error('生成脚本失败:', error)
    ElMessage.error('脚本生成失败: ' + (error.message || '未知错误'))
  } finally {
    generating.value = false
  }
}

const showScriptPreview = () => {
  showScriptDialog.value = true
}

const downloadScript = () => {
  if (!generatedScript.value) {
    ElMessage.error('没有生成脚本')
    return
  }

  const element = document.createElement('a')
  const file = new Blob([generatedScript.value], { type: 'text/plain' })
  element.href = URL.createObjectURL(file)
  element.download = `automation_script_${Date.now()}.py`
  document.body.appendChild(element)
  element.click()
  document.body.removeChild(element)
  ElMessage.success('脚本已下载')
}

const showPackageDialog = async () => {
  // 打包前自动重新生成脚本，确保使用最新的流程配置
  if (nodes.value.length === 0) {
    ElMessage.warning('请先添加功能模块到流程中')
    return
  }
  try {
    generating.value = true
    if (!props.flowId) {
      ElMessage.error('流程ID不存在，请刷新页面重试')
      return
    }
    await flushSave()
    const result = await flowStore.generateScript(props.flowId)
    generatedScript.value = result.generatedScript
  } catch (error) {
    console.error('生成脚本失败:', error)
    ElMessage.error('脚本生成失败: ' + (error.message || '未知错误'))
    return
  } finally {
    generating.value = false
  }

  // 重置 browserVersions，只保留当前流程中实际使用的浏览器
  // IE/Firefox 驱动版本由后端固定，前端传空字符串
  const newVersions = {}
  detectedBrowsers.value.forEach(browser => {
    if (browser.type === 'ie' || browser.type === 'firefox') {
      newVersions[browser.type] = ''
    } else if (browserVersions.value[browser.type]) {
      newVersions[browser.type] = browserVersions.value[browser.type]
    } else {
      const history = getVersionHistory(browser.type)
      newVersions[browser.type] = history[0] || ''
    }
  })
  browserVersions.value = newVersions

  showPackageDialogFlag.value = true
}

const packageScript = async () => {
  try {
    packaging.value = true

    if (!generatedScript.value) {
      ElMessage.error('没有生成脚本')
      return
    }

    // 定时执行模块验证：必须配置至少一个时间点
    const scheduledNode = nodes.value.find(n => n.type === 'scheduled_task')
    if (scheduledNode) {
      const times = (scheduledNode.params?.schedule_times || '').split(',').map(s => s.trim()).filter(Boolean)
      if (times.length === 0) {
        ElMessage.error('定时执行模块未配置执行时间点，请先添加至少一个时间')
        return
      }
    }

    // 离线模式下验证版本号（IE/Firefox 允许为空，后端使用固定版本）
    if (packageNetworkMode.value === 'offline') {
      for (const browser of detectedBrowsers.value) {
        const version = browserVersions.value[browser.type]
        if (!version && browser.type !== 'ie' && browser.type !== 'firefox') {
          ElMessage.error(`请输入 ${browser.label} 浏览器版本`)
          return
        }
        // 保存到历史记录
        if (version) {
          saveVersionHistory(browser.type, version)
        }
      }
    }

    // 调用后端打包API
    const response = await fetch('/api/scripts/package', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify((() => {
        // 检测流程中是否存在 scheduled_task 模块，自动覆盖打包参数
        const scheduledNode = nodes.value.find(n => n.type === 'scheduled_task')
        if (scheduledNode) {
          const p = scheduledNode.params || {}
          const times = (p.schedule_times || '').split(',').map(s => s.trim()).filter(Boolean)
          return {
            script: generatedScript.value,
            platform: packagePlatform.value,
            networkMode: packageNetworkMode.value,
            browserVersions: packageNetworkMode.value === 'offline' ? browserVersions.value : {},
            name: `automation_${Date.now()}`,
            executionMode: 'scheduled_task',
            restartDelay: parseInt(p.restart_delay) || 60,
            maxRetries: parseInt(p.max_retries) || 0,
            scheduleTimes: times,
            intervalMinutes: 0,
            autoStart: p.auto_start === 'true' || p.auto_start === true,
            runHidden: p.run_hidden === 'true' || p.run_hidden === true,
            repeatMode: p.repeat_mode || 'daily',
            repeatDays: (p.repeat_days || '').split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n)),
            rdpMode: p.rdp_mode === 'true' || p.rdp_mode === true
          }
        }
        return {
          script: generatedScript.value,
          platform: packagePlatform.value,
          networkMode: packageNetworkMode.value,
          browserVersions: packageNetworkMode.value === 'offline' ? browserVersions.value : {},
          name: `automation_${Date.now()}`,
          executionMode: 'once',
          restartDelay: 30,
          maxRetries: 0,
          scheduleTimes: [],
          intervalMinutes: 0,
          autoStart: false,
          runHidden: false
        }
      })())
    })

    if (!response.ok) {
      const text = await response.text()
      throw new Error(text || '打包失败')
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `automation_${packagePlatform.value}.exe`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)

    const modeText = packageNetworkMode.value === 'offline' ? '（含内置驱动）' : '（首次运行需联网）'
    ElMessage.success(`打包完成！${modeText}`)
    showPackageDialogFlag.value = false
  } catch (error) {
    console.error('打包失败:', error)
    ElMessage.error('打包失败: ' + error.message)
  } finally {
    packaging.value = false
  }
}

const executeFlow = async () => {
  try {
    executing.value = true

    if (!props.flowId) {
      ElMessage.error('流程ID不存在，请刷新页面重试')
      return
    }

    await flushSave()

    // 立即返回执行日志（status=running）
    const logDTO = await flowStore.executeFlow(props.flowId)
    ElMessage.info('脚本已开始执行...')

    // 轮询等待执行完成
    flowStore.pollExecutionLog(logDTO.id, (log) => {
      if (log.status === 'success') {
        executing.value = false
        ElMessage.success(`执行成功，耗时 ${(log.executionTimeMs / 1000).toFixed(1)} 秒`)
      } else if (log.status === 'failed') {
        executing.value = false
        ElMessage.error('执行失败: ' + (log.errorMessage || '未知错误'))
      }
    })
  } catch (error) {
    console.error('执行流程失败:', error)
    ElMessage.error('执行流程失败: ' + (error.message || '未知错误'))
    executing.value = false
  }
}

defineExpose({
  updateNodeParam,
  addModule
})
</script>

<style scoped>
.flow-editor {
  height: 100%;
}

.editor-card {
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-card__header) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 20px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}

.editor-header h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1d;
}

.subtitle {
  margin: 0;
  font-size: 12px;
  color: #6e6e73;
}

.editor-actions {
  display: flex;
  gap: 12px;
}

.editor-canvas {
  min-height: 500px;
  border: 2px dashed rgba(0, 113, 227, 0.3);
  border-radius: 12px;
  padding: 30px;
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.02) 0%, rgba(0, 113, 227, 0.05) 100%);
  transition: all 0.3s ease;
  overflow-y: auto;
  max-height: calc(100vh - 300px);
}

.editor-canvas:hover {
  border-color: rgba(0, 113, 227, 0.5);
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.05) 0%, rgba(0, 113, 227, 0.08) 100%);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #6e6e73;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.empty-hint {
  font-size: 12px;
  color: #a1a1a6;
  margin-top: 8px;
}

.nodes-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

/* ==================== 画布布局：左侧监控条 + 右侧流程 ==================== */
.canvas-layout {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

/* ==================== 全程监控侧边栏 ==================== */
.monitor-sidebar {
  display: flex;
  flex-direction: row;
  gap: 8px;
  flex-shrink: 0;
}

.monitor-bar {
  width: 56px;
  flex: 1;
  min-height: 120px;
  background: linear-gradient(180deg, rgba(139, 92, 246, 0.08) 0%, rgba(139, 92, 246, 0.03) 100%);
  border: 2px solid rgba(139, 92, 246, 0.25);
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 4px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.monitor-bar:hover {
  border-color: #8b5cf6;
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.2);
}

.monitor-bar.selected {
  border-color: #8b5cf6;
  background: linear-gradient(180deg, rgba(139, 92, 246, 0.15) 0%, rgba(139, 92, 246, 0.05) 100%);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.25);
}

.monitor-bar-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 100%;
}

.monitor-bar-header .remove-btn {
  width: 18px;
  height: 18px;
  font-size: 10px;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.monitor-bar:hover .monitor-bar-header .remove-btn {
  opacity: 1;
}

.monitor-bar-icon {
  font-size: 20px;
}

.monitor-bar-title {
  font-size: 10px;
  font-weight: 600;
  color: #6d28d9;
  text-align: center;
  line-height: 1.2;
  writing-mode: horizontal-tb;
}

.monitor-bar-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 100%;
  margin-top: 6px;
}

.monitor-bar-label {
  font-size: 9px;
  color: #8b5cf6;
  font-weight: 600;
  letter-spacing: 0.5px;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  flex: 1;
  display: flex;
  align-items: center;
}

.monitor-bar-info {
  font-size: 8px;
  color: #7c3aed;
  text-align: center;
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

.monitor-bar-track {
  position: absolute;
  left: 50%;
  top: 60px;
  bottom: 10px;
  width: 2px;
  background: linear-gradient(180deg, rgba(139, 92, 246, 0.4), rgba(139, 92, 246, 0.1));
  border-radius: 1px;
  transform: translateX(-50%);
}

.monitor-bar-track::before {
  content: '';
  position: absolute;
  top: 0;
  left: -3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #8b5cf6;
  animation: monitorPulse 2s ease-in-out infinite;
}

@keyframes monitorPulse {
  0%, 100% { top: 0; opacity: 1; }
  50% { top: calc(100% - 8px); opacity: 0.4; }
}

/* ==================== 错误监控条样式 ==================== */
.monitor-bar.error-type {
  background: linear-gradient(180deg, rgba(234, 88, 12, 0.08) 0%, rgba(234, 88, 12, 0.03) 100%);
  border-color: rgba(234, 88, 12, 0.25);
}

.monitor-bar.error-type:hover {
  border-color: #ea580c;
  box-shadow: 0 4px 16px rgba(234, 88, 12, 0.2);
}

.monitor-bar.error-type.selected {
  border-color: #ea580c;
  background: linear-gradient(180deg, rgba(234, 88, 12, 0.15) 0%, rgba(234, 88, 12, 0.05) 100%);
  box-shadow: 0 4px 16px rgba(234, 88, 12, 0.25);
}

.monitor-bar.error-type .monitor-bar-title {
  color: #c2410c;
}

.monitor-bar.error-type .monitor-bar-label {
  color: #ea580c;
}

.monitor-bar.error-type .monitor-bar-info {
  color: #c2410c;
}

.monitor-bar.error-type .monitor-bar-track {
  background: linear-gradient(180deg, rgba(234, 88, 12, 0.4), rgba(234, 88, 12, 0.1));
}

.monitor-bar.error-type .monitor-bar-track::before {
  background: #ea580c;
  animation: errorPulse 2s ease-in-out infinite;
}

@keyframes errorPulse {
  0%, 100% { top: 0; opacity: 1; }
  50% { top: calc(100% - 8px); opacity: 0.4; }
}

/* ==================== 定时执行条样式 ==================== */
.monitor-bar.scheduled-type {
  background: linear-gradient(180deg, rgba(6, 182, 212, 0.08) 0%, rgba(6, 182, 212, 0.03) 100%);
  border-color: rgba(6, 182, 212, 0.25);
}

.monitor-bar.scheduled-type:hover {
  border-color: #06b6d4;
  box-shadow: 0 4px 16px rgba(6, 182, 212, 0.2);
}

.monitor-bar.scheduled-type.selected {
  border-color: #06b6d4;
  background: linear-gradient(180deg, rgba(6, 182, 212, 0.15) 0%, rgba(6, 182, 212, 0.05) 100%);
  box-shadow: 0 4px 16px rgba(6, 182, 212, 0.25);
}

.monitor-bar.scheduled-type .monitor-bar-title {
  color: #0e7490;
}

.monitor-bar.scheduled-type .monitor-bar-label {
  color: #06b6d4;
}

.monitor-bar.scheduled-type .monitor-bar-info {
  color: #0e7490;
}

.monitor-bar.scheduled-type .monitor-bar-track {
  background: linear-gradient(180deg, rgba(6, 182, 212, 0.4), rgba(6, 182, 212, 0.1));
}

.monitor-bar.scheduled-type .monitor-bar-track::before {
  background: #06b6d4;
  animation: scheduledPulse 3s ease-in-out infinite;
}

@keyframes scheduledPulse {
  0%, 100% { top: 0; opacity: 1; }
  50% { top: calc(100% - 8px); opacity: 0.4; }
}

/* ==================== 监控条进出动画 ==================== */
/* 节点从流程飞到左侧监控条 */
.monitor-enter-enter-active {
  animation: nodeToMonitor 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.monitor-enter-leave-active {
  animation: monitorToNode 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes nodeToMonitor {
  0% {
    opacity: 0;
    transform: translateX(120px) scaleX(3) scaleY(0.3);
    border-radius: 12px;
  }
  40% {
    opacity: 0.7;
    transform: translateX(20px) scaleX(1.5) scaleY(0.6);
  }
  70% {
    opacity: 0.9;
    transform: translateX(-5px) scaleX(0.95) scaleY(1.05);
  }
  100% {
    opacity: 1;
    transform: translateX(0) scaleX(1) scaleY(1);
  }
}

@keyframes monitorToNode {
  0% {
    opacity: 1;
    transform: translateX(0) scaleX(1) scaleY(1);
  }
  30% {
    opacity: 0.7;
    transform: translateX(30px) scaleX(1.8) scaleY(0.5);
  }
  100% {
    opacity: 0;
    transform: translateX(120px) scaleX(3) scaleY(0.2);
  }
}

/* 节点从监控条回到流程 */
.node-to-flow-enter-active {
  animation: monitorBackToFlow 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.node-to-flow-leave-active {
  animation: flowToMonitor 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes monitorBackToFlow {
  0% {
    opacity: 0;
    transform: translateX(-80px) scaleY(0.4);
    max-height: 0;
  }
  30% {
    max-height: 200px;
  }
  50% {
    opacity: 0.8;
    transform: translateX(8px) scaleY(1.03);
  }
  70% {
    transform: translateX(-3px) scaleY(0.99);
  }
  100% {
    opacity: 1;
    transform: translateX(0) scaleY(1);
    max-height: 200px;
  }
}

@keyframes flowToMonitor {
  0% {
    opacity: 1;
    transform: translateX(0) scaleY(1);
    max-height: 200px;
  }
  40% {
    opacity: 0.5;
    transform: translateX(-40px) scaleY(0.7);
  }
  100% {
    opacity: 0;
    transform: translateX(-100px) scaleY(0.2);
    max-height: 0;
  }
}

/* 监控侧边栏本身的展开/收起 */
.monitor-sidebar {
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.4s ease;
}

/* 侧边栏整体滑入/滑出 */
.sidebar-slide-enter-active {
  animation: sidebarIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.sidebar-slide-leave-active {
  animation: sidebarOut 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes sidebarIn {
  0% { opacity: 0; width: 0; min-width: 0; margin-right: -16px; }
  100% { opacity: 1; width: auto; min-width: auto; margin-right: 0; }
}

@keyframes sidebarOut {
  0% { opacity: 1; width: auto; min-width: auto; margin-right: 0; }
  100% { opacity: 0; width: 0; min-width: 0; margin-right: -16px; }
}

/* ==================== 节点卡片 ==================== */
.node-wrapper {
  /* Transition 需要的包裹层，不影响布局 */
}

.node {
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  border: 2px solid rgba(0, 113, 227, 0.2);
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  user-select: none;
}

/* 新节点飞入动画 */
.node.fly-in {
  animation: nodeFlyIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes nodeFlyIn {
  0%   { opacity: 0; transform: translateX(-80px) scale(0.8); }
  50%  { opacity: 0.8; transform: translateX(8px) scale(1.03); }
  70%  { transform: translateX(-3px) scale(0.99); }
  100% { opacity: 1; transform: translateX(0) scale(1); }
}

.node:hover {
  border-color: #0071e3;
  box-shadow: 0 8px 24px rgba(0, 113, 227, 0.15);
  transform: translateY(-2px);
}

.node.selected {
  border-color: #0071e3;
  background: rgba(0, 113, 227, 0.05);
  box-shadow: 0 8px 24px rgba(0, 113, 227, 0.2);
}

.node.dragging {
  opacity: 0.4;
  transform: scale(0.98);
}

.node-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.node-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #0071e3 0%, #0077ed 100%);
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.node-title {
  font-weight: 600;
  color: #1d1d1d;
  flex: 1;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.node-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 12px;
}

.move-btn {
  background: rgba(0, 113, 227, 0.08);
  color: #0071e3;
}

.move-btn:hover:not(.disabled) {
  background: #0071e3;
  color: white;
}

.move-btn.disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.remove-btn {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.remove-btn:hover {
  background: #dc2626;
  color: white;
}

.node-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 40px;
}

.params-preview {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.param-item {
  display: flex;
  gap: 8px;
  font-size: 12px;
}

.param-key {
  color: #6e6e73;
  font-weight: 500;
  min-width: 80px;
}

.param-value {
  color: #0071e3;
  word-break: break-all;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-params {
  font-size: 12px;
  color: #a1a1a6;
  font-style: italic;
}

/* ==================== 拖拽插入指示器 ==================== */
.drop-indicator {
  height: 8px;
  position: relative;
  transition: height 0.2s ease;
}

.drop-indicator.active {
  height: 40px;
}

.drop-indicator-line {
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 3px;
  background: #0071e3;
  border-radius: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drop-indicator.active .drop-indicator-line {
  opacity: 1;
}

.drop-indicator-label {
  position: absolute;
  top: -10px;
  background: #0071e3;
  color: white;
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 10px;
  white-space: nowrap;
}

/* ==================== 对话框 ==================== */
.script-preview {
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;
}

.script-preview pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #333;
  white-space: pre-wrap;
  word-wrap: break-word;
}

:deep(.custom-dialog .el-dialog__header) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
</style>
