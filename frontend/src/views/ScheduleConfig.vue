<template>
  <div class="schedule-config">
    <div class="page-header">
      <div>
        <h2>定时配置</h2>
        <p class="description">设置流程的自动执行计划</p>
      </div>
      <el-button type="primary" @click="showCreateDialog = true" class="create-btn">
        <el-icon><Plus /></el-icon>
        新建计划
      </el-button>
    </div>

    <el-card class="schedule-card glass">
      <el-table :data="scheduleStore.schedules" stripe style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="cronExpression" label="Cron表达式" />
        <el-table-column prop="enabled" label="启用状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="updateSchedule(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="notifyEmail" label="通知邮箱" />
        <el-table-column prop="lastExecution" label="最后执行" width="180" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="editSchedule(row)" class="action-btn">编辑</el-button>
            <el-popconfirm title="确定删除此计划？" @confirm="deleteSchedule(row.id)">
              <template #reference>
                <el-button link type="danger" class="action-btn">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="新建定时计划" width="500px" class="custom-dialog">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="Cron表达式">
          <el-input v-model="formData.cronExpression" placeholder="例如: 0 9 * * *" />
          <div class="cron-hint">
            <p>常用表达式:</p>
            <ul>
              <li>0 0 * * * - 每天午夜</li>
              <li>0 9 * * * - 每天上午9点</li>
              <li>0 */6 * * * - 每6小时</li>
              <li>0 0 1 * * - 每月1号</li>
            </ul>
          </div>
        </el-form-item>
        <el-form-item label="通知邮箱">
          <el-input v-model="formData.notifyEmail" type="email" placeholder="输入邮箱地址" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createSchedule">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useScheduleStore } from '../store'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const scheduleStore = useScheduleStore()
const showCreateDialog = ref(false)
const formData = ref({
  cronExpression: '',
  notifyEmail: '',
  enabled: false
})

onMounted(() => {
  const flowId = route.params.flowId
  if (flowId) {
    scheduleStore.fetchSchedules(flowId)
  }
})

const createSchedule = async () => {
  try {
    const flowId = route.params.flowId
    await scheduleStore.createSchedule({
      flowId,
      ...formData.value
    })
    showCreateDialog.value = false
    formData.value = { cronExpression: '', notifyEmail: '', enabled: false }
    ElMessage.success('计划创建成功')
  } catch (error) {
    ElMessage.error('计划创建失败')
  }
}

const updateSchedule = async (schedule) => {
  try {
    await scheduleStore.updateSchedule(schedule.id, schedule)
    ElMessage.success('计划更新成功')
  } catch (error) {
    ElMessage.error('计划更新失败')
  }
}

const editSchedule = (schedule) => {
  formData.value = { ...schedule }
  showCreateDialog.value = true
}

const deleteSchedule = async (id) => {
  try {
    await scheduleStore.deleteSchedule(id)
    ElMessage.success('计划删除成功')
  } catch (error) {
    ElMessage.error('计划删除失败')
  }
}
</script>

<style scoped>
.schedule-config {
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

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.schedule-card {
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

.action-btn {
  font-weight: 500;
}

.cron-hint {
  margin-top: 12px;
  padding: 12px;
  background: rgba(0, 113, 227, 0.05);
  border-radius: 8px;
  font-size: 12px;
  color: #6e6e73;
}

.cron-hint p {
  margin: 0 0 8px 0;
  font-weight: 500;
  color: #1d1d1d;
}

.cron-hint ul {
  margin: 0;
  padding-left: 20px;
}

.cron-hint li {
  margin: 4px 0;
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

