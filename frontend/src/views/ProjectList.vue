<template>
  <div class="project-list">
    <div class="page-header">
      <div>
        <h2>项目管理</h2>
        <p class="description">创建和管理您的自动化脚本项目</p>
      </div>
      <el-button type="primary" @click="showCreateDialog = true" class="create-btn">
        <el-icon><Plus /></el-icon>
        新建项目
      </el-button>
    </div>

    <el-card class="projects-card glass">
      <el-table :data="projectStore.projects" stripe style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="项目名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '活跃' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="editProject(row)" class="action-btn">编辑</el-button>
            <el-button link type="primary" @click="openProject(row)" class="action-btn">打开</el-button>
            <el-popconfirm title="确定删除此项目？" @confirm="deleteProject(row.id)">
              <template #reference>
                <el-button link type="danger" class="action-btn">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="新建项目" width="500px" class="custom-dialog">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="项目名称">
          <el-input v-model="formData.name" placeholder="输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="formData.description" type="textarea" placeholder="输入项目描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createProject">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '../store'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const projectStore = useProjectStore()
const showCreateDialog = ref(false)
const formData = ref({
  name: '',
  description: ''
})

onMounted(() => {
  projectStore.fetchProjects()
})

const createProject = async () => {
  try {
    await projectStore.createProject(formData.value)
    showCreateDialog.value = false
    formData.value = { name: '', description: '' }
    ElMessage.success('项目创建成功')
  } catch (error) {
    ElMessage.error('项目创建失败')
  }
}

const editProject = (project) => {
  router.push(`/projects/${project.id}/edit`)
}

const openProject = (project) => {
  projectStore.setCurrentProject(project)
  router.push(`/projects/${project.id}`)
}

const deleteProject = async (id) => {
  try {
    await projectStore.deleteProject(id)
    ElMessage.success('项目删除成功')
  } catch (error) {
    ElMessage.error('项目删除失败')
  }
}
</script>

<style scoped>
.project-list {
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

.projects-card {
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.projects-card:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

:deep(.custom-table) {
  --el-table-border-color: rgba(0, 0, 0, 0.06);
  --el-table-header-bg-color: rgba(0, 113, 227, 0.05);
}

:deep(.custom-table th) {
  background: rgba(0, 113, 227, 0.05);
  font-weight: 600;
  color: #1d1d1d;
}

:deep(.custom-table tbody tr:hover) {
  background: rgba(0, 113, 227, 0.05);
}

.action-btn {
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
}

:deep(.custom-dialog .el-dialog__header) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
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

