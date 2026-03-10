<template>
  <div class="flow-editor">
    <el-row :gutter="20" style="height: 100vh">
      <el-col :span="4" class="module-panel">
        <ModulePanel @add-module="onAddModule" />
      </el-col>
      <el-col :span="16" class="editor-area">
        <FlowEditor v-if="flowId" ref="flowEditorRef" :flow-id="flowId" />
      </el-col>
      <el-col :span="4" class="property-panel">
        <PropertyPanel />
      </el-col>
    </el-row>
    <AiChatDrawer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useFlowStore } from '../store'
import { flowApi } from '../api'
import { ElMessage } from 'element-plus'
import ModulePanel from '../components/ModulePanel.vue'
import FlowEditor from '../components/FlowEditor.vue'
import PropertyPanel from '../components/PropertyPanel.vue'
import AiChatDrawer from '../components/AiChatDrawer.vue'

const route = useRoute()
const flowStore = useFlowStore()
const flowId = ref(null)
const flowEditorRef = ref(null)

const onAddModule = (module) => {
  if (flowEditorRef.value) {
    flowEditorRef.value.addModule(module)
  }
}

onMounted(async () => {
  try {
    const projectId = route.params.id
    if (!projectId) {
      ElMessage.error('项目ID不存在')
      return
    }

    // 获取或创建项目的唯一流程
    const response = await flowApi.getOrCreate(projectId)
    const flow = response.data
    flowId.value = flow.id
    flowStore.currentFlow = flow
  } catch (error) {
    console.error('初始化流程失败:', error)
    ElMessage.error('初始化流程失败: ' + error.message)
  }
})
</script>

<style scoped>
.flow-editor {
  height: 100vh;
  overflow: hidden;
}

.module-panel {
  border: 1px solid #ddd;
  overflow-y: auto;
  padding: 10px;
  height: 100%;
}

.property-panel {
  border: 1px solid #ddd;
  overflow: hidden;
  padding: 10px;
  height: 100%;
}

.editor-area {
  border: 1px solid #ddd;
  overflow: hidden;
  height: 100%;
}

:deep(.el-row) {
  height: 100%;
}

:deep(.el-col) {
  height: 100%;
}
</style>
