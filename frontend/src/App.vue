<template>
  <div id="app">
    <el-container>
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo-section">
            <h1>自动化脚本平台</h1>
            <p class="subtitle">智能流程编排 · 自动化执行</p>
          </div>
          <el-menu mode="horizontal" :default-active="activeMenu" class="header-menu">
            <el-menu-item index="1" @click="$router.push('/projects')">项目</el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import './styles/global.css'

const route = useRoute()
const activeMenu = ref('1')
</script>

<style scoped>
#app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f5f7 0%, #ffffff 100%);
}

.app-header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0 40px !important;
  display: flex;
  align-items: center;
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}

.logo-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.logo-section h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #0071e3 0%, #0077ed 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  margin: 0;
  font-size: 12px;
  color: #6e6e73;
  font-weight: 400;
}

.header-menu {
  background: transparent;
  border: none;
  flex: 1;
  justify-content: flex-end;
}

:deep(.el-menu-item) {
  color: #1d1d1d;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background: rgba(0, 113, 227, 0.08) !important;
  color: #0071e3;
}

:deep(.el-menu-item.is-active) {
  background: transparent !important;
  color: #0071e3;
  border-bottom: 2px solid #0071e3;
}

.app-main {
  flex: 1;
  overflow-y: auto;
  background: linear-gradient(135deg, #f5f5f7 0%, #ffffff 100%);
  padding: 40px;
}

:deep(.el-main) {
  padding: 40px;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>

