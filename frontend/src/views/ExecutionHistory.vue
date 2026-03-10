<template>
  <div class="execution-history">
    <div class="page-header">
      <div>
        <h2>执行历史</h2>
        <p class="description">查看所有脚本执行记录</p>
      </div>
      <el-button type="primary" @click="refreshLogs" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-card class="history-card glass">
      <el-table :data="executionLogStore.logs" stripe style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" class="status-tag">
              {{ row.status === 'success' ? '✓ 成功' : '✗ 失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionTimeMs" label="耗时(ms)" width="120" />
        <el-table-column prop="startedAt" label="开始时间" width="180" />
        <el-table-column prop="completedAt" label="完成时间" width="180" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewLog(row)" class="action-btn">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showLogDialog" title="执行日志详情" width="900px" class="custom-dialog">
      <div v-if="selectedLog">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="执行状态">
            <el-tag :type="selectedLog.status === 'success' ? 'success' : 'danger'">
              {{ selectedLog.status === 'success' ? '✓ 成功' : '✗ 失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行耗时">
            {{ selectedLog.executionTimeMs }} ms
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ selectedLog.startedAt }}
          </el-descriptions-item>
          <el-descriptions-item label="完成时间">
            {{ selectedLog.completedAt }}
          </el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div v-if="selectedLog.output" class="log-content">
          <h4>📤 输出信息</h4>
          <pre>{{ selectedLog.output }}</pre>
        </div>
        <div v-if="selectedLog.errorMessage" class="log-content error">
          <h4>❌ 错误信息</h4>
          <pre>{{ selectedLog.errorMessage }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useExecutionLogStore } from '../store'
import { Refresh } from '@element-plus/icons-vue'

const route = useRoute()
const executionLogStore = useExecutionLogStore()
const showLogDialog = ref(false)
const selectedLog = ref(null)
const loading = ref(false)

onMounted(() => {
  const flowId = route.params.flowId
  if (flowId) {
    executionLogStore.fetchLogs(flowId)
  }
})

const refreshLogs = async () => {
  loading.value = true
  try {
    const flowId = route.params.flowId
    if (flowId) {
      await executionLogStore.fetchLogs(flowId)
    }
  } finally {
    loading.value = false
  }
}

const viewLog = (log) => {
  selectedLog.value = log
  showLogDialog.value = true
}
</script>

<style scoped>
.execution-history {
  animation: fadeIn 0.5s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 600;
  color: #1d1d1d;
}

.description {
  margin: 0;
  font-size: 14px;
  color: #6e6e73;
}

.history-card {
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.custom-table) {
  --el-table-border-color: rgba(0, 0, 0, 0.06);
  --el-table-header-bg-color: rgba(0, 113, 227, 0.05);
}

:deep(.custom-table th) {
  background: rgba(0, 113, 227, 0.05);
  font-weight: 600;
}

:deep(.custom-table tbody tr:hover) {
  background: rgba(0, 113, 227, 0.05);
}

.status-tag {
  font-weight: 500;
}

.action-btn {
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.log-content {
  margin-top: 16px;
}

.log-content h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1d;
}

.log-content pre {
  background: rgba(0, 113, 227, 0.05);
  border: 1px solid rgba(0, 113, 227, 0.2);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  max-height: 400px;
  font-size: 12px;
  color: #1d1d1d;
  line-height: 1.5;
}

.log-content.error pre {
  background: rgba(239, 68, 68, 0.05);
  border-color: rgba(239, 68, 68, 0.2);
  color: #dc2626;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

