<template>
  <div class="module-panel glass">
    <div class="panel-header">
      <h3>功能模块</h3>
      <p class="panel-subtitle">点击或拖拽添加到流程</p>
    </div>

    <el-tabs class="module-tabs">
      <el-tab-pane label="基础模块">
        <div class="module-list">
          <div
            v-for="module in basicModules"
            :key="module.id"
            class="module-item"
            draggable="true"
            @dragstart="dragStart($event, module)"
            @click="clickAdd($event, module)"
            :title="module.description"
          >
            <div class="module-icon">{{ getModuleIcon(module) }}</div>
            <div class="module-info">
              <div class="module-name">{{ module.name }}</div>
              <div class="module-desc">{{ module.description }}</div>
            </div>
            <div class="module-drag-hint">⋮⋮</div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="高级模块">
        <div class="module-list">
          <div
            v-for="module in advancedModules"
            :key="module.id"
            class="module-item"
            draggable="true"
            @dragstart="dragStart($event, module)"
            @click="clickAdd($event, module)"
            :title="module.description"
          >
            <div class="module-icon">{{ getModuleIcon(module) }}</div>
            <div class="module-info">
              <div class="module-name">{{ module.name }}</div>
              <div class="module-desc">{{ module.description }}</div>
            </div>
            <div class="module-drag-hint">⋮⋮</div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="自定义模块">
        <div class="custom-header">
          <el-button type="primary" size="small" @click="showModuleManager = true">
            + 新建模块
          </el-button>
        </div>
        <div class="module-list">
          <div v-if="customModules.length === 0" class="empty-hint">
            暂无自定义模块，点击上方按钮创建
          </div>
          <div
            v-for="module in customModules"
            :key="module.id"
            class="module-item"
            draggable="true"
            @dragstart="dragStart($event, module)"
            @click="clickAdd($event, module)"
            :title="module.description"
          >
            <div class="module-icon">{{ getModuleIcon(module) }}</div>
            <div class="module-info">
              <div class="module-name">{{ module.name }}</div>
              <div class="module-desc">{{ module.description }}</div>
            </div>
            <div class="module-actions">
              <el-icon class="edit-icon" @click.stop="editModule(module)"><Edit /></el-icon>
            </div>
            <div class="module-drag-hint">⋮⋮</div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <ModuleManager
      v-model="showModuleManager"
      :edit-module="editingModule"
      @saved="onModuleSaved"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Edit } from '@element-plus/icons-vue'
import { useModuleStore } from '../store'
import ModuleManager from './ModuleManager.vue'

const emit = defineEmits(['add-module'])

const moduleStore = useModuleStore()
const showModuleManager = ref(false)
const editingModule = ref(null)

onMounted(() => {
  moduleStore.fetchModules()
})

const basicModules = computed(() => moduleStore.getModulesByCategory('basic'))
const advancedModules = computed(() => moduleStore.getModulesByCategory('advanced'))
const customModules = computed(() => moduleStore.getModulesByCategory('custom'))

const defaultIcons = {
  'open_browser': '🌐',
  'input_account': '🔐',
  'click_button': '🖱️',
  'input_text': '⌨️',
  'select_dropdown': '📋',
  'wait_element': '⏳',
  'get_text': '📄',
  'screenshot': '📸',
  'hover_element': '👆',
  'double_click': '🖱️🖱️',
  'scroll_to': '📜',
  'get_attribute': '🏷️',
  'close_popup': '❌',
  'batch_input': '📝',
  'switch_frame': '🔲',
  'press_enter': '↵',
  'alert_confirm': '🔔',
  'extract_content': '🔍',
  'confirm_dialog': '❓',
  'send_email': '📧',
  'error_monitor': '🛡️',
  'scheduled_task': '⏰'
}

const getModuleIcon = (module) => {
  if (module.icon) return module.icon
  return defaultIcons[module.moduleId] || '📦'
}

const dragStart = (event, module) => {
  event.dataTransfer.effectAllowed = 'copy'
  event.dataTransfer.setData('module', JSON.stringify(module))
}

const clickAdd = (event, module) => {
  const el = event.currentTarget
  el.classList.add('fly-out')
  setTimeout(() => el.classList.remove('fly-out'), 500)
  emit('add-module', module)
}

const editModule = (module) => {
  editingModule.value = module
  showModuleManager.value = true
}

const onModuleSaved = () => {
  editingModule.value = null
  moduleStore.fetchModules()
}
</script>

<style scoped>
.module-panel {
  height: 100%;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.panel-header {
  margin-bottom: 20px;
}

.panel-header h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1d;
}

.panel-subtitle {
  margin: 0;
  font-size: 12px;
  color: #6e6e73;
}

.module-tabs {
  flex: 1;
  overflow-y: auto;
}

:deep(.el-tabs__nav) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #0071e3 0%, #0077ed 100%);
}

:deep(.el-tabs__content) {
  overflow-y: auto;
  max-height: calc(100vh - 300px);
}

.custom-header {
  padding: 8px 0 12px;
  display: flex;
  justify-content: flex-end;
}

.empty-hint {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 24px 0;
}

.module-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px 0;
}

.module-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 113, 227, 0.2);
  border-radius: 10px;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  position: relative;
}

.module-item:hover {
  background: rgba(0, 113, 227, 0.08);
  border-color: #0071e3;
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.15);
  transform: translateY(-2px);
}

.module-item:active {
  transform: scale(0.95);
  opacity: 0.85;
}

/* 点击后飞出动画 */
.module-item.fly-out {
  animation: flyOutBounce 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes flyOutBounce {
  0%   { transform: scale(1); opacity: 1; }
  20%  { transform: scale(0.9); opacity: 0.7; }
  50%  { transform: scale(1.05) translateX(30px); opacity: 0.5; }
  70%  { transform: scale(1) translateX(10px); opacity: 0.8; }
  100% { transform: scale(1) translateX(0); opacity: 1; }
}

.module-icon {
  font-size: 24px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.module-info {
  flex: 1;
  min-width: 0;
}

.module-name {
  font-size: 13px;
  font-weight: 600;
  color: #1d1d1d;
  margin-bottom: 2px;
}

.module-desc {
  font-size: 11px;
  color: #6e6e73;
  white-space: normal;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.module-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.module-item:hover .module-actions {
  opacity: 1;
}

.edit-icon {
  font-size: 14px;
  color: #0071e3;
  cursor: pointer;
  padding: 4px;
}

.edit-icon:hover {
  color: #0077ed;
}

.module-drag-hint {
  font-size: 12px;
  color: #a1a1a6;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.module-item:hover .module-drag-hint {
  opacity: 1;
}
</style>
