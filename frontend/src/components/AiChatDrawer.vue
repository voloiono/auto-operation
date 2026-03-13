<template>
  <div class="ai-chat-wrapper">
    <!-- 悬浮按钮 -->
    <div class="ai-fab" @click="drawerVisible = true" v-show="!drawerVisible">
      <el-icon :size="26"><ChatDotRound /></el-icon>
    </div>

    <!-- 抽屉对话窗口 -->
    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      :size="420"
      :show-close="false"
      :with-header="false"
      class="ai-drawer"
      :modal="false"
      :append-to-body="true"
    >
      <div class="ai-drawer-content">
        <!-- 顶部标题栏 -->
        <div class="ai-header">
          <div class="ai-header-left">
            <el-icon :size="20" color="#7c3aed"><ChatDotRound /></el-icon>
            <span class="ai-title">AI 助手</span>
            <el-tag v-if="aiConfig.mode === 'backend'" size="small" type="success" effect="plain">Tool Use</el-tag>
          </div>
          <div class="ai-header-right">
            <el-button text size="small" @click="showSettings = !showSettings" :type="showSettings ? 'primary' : ''">
              <el-icon><Setting /></el-icon>
              设置
            </el-button>
            <el-button text size="small" @click="clearMessages" :disabled="messages.length === 0">
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
            <el-button text size="small" @click="drawerVisible = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 设置面板 -->
        <Transition name="settings-slide">
        <div v-if="showSettings" class="ai-settings-panel">
          <el-form label-width="80px" size="small">
            <el-form-item label="AI 模式">
              <el-radio-group v-model="aiConfig.mode">
                <el-radio-button value="backend">后端AI (Tool Use)</el-radio-button>
                <el-radio-button value="direct">直连API</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="提供商">
              <el-select v-model="aiConfig.provider" style="width: 100%">
                <el-option label="Anthropic (Claude)" value="anthropic" />
                <el-option label="OpenAI (GPT)" value="openai" />
              </el-select>
            </el-form-item>
            <el-form-item label="Base URL">
              <el-input v-model="aiConfig.baseUrl" placeholder="https://www.dmxapi.cn" />
              <div class="form-tip">
                代理地址只填域名，如 https://www.dmxapi.cn<br>
                系统会自动拼接 /v1/messages 或 /v1/chat/completions
              </div>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="aiConfig.apiKey" type="password" show-password placeholder="sk-..." />
            </el-form-item>
            <el-form-item label="模型">
              <el-select v-model="aiConfig.model" filterable allow-create default-first-option style="width: 100%">
                <el-option-group label="Claude 4.6 (代理格式)">
                  <el-option label="claude-opus-4-6-cc" value="claude-opus-4-6-cc" />
                  <el-option label="claude-sonnet-4-6-cc" value="claude-sonnet-4-6-cc" />
                </el-option-group>
                <el-option-group label="Claude 4.5 (代理格式)">
                  <el-option label="claude-sonnet-4-5-20250929-cc" value="claude-sonnet-4-5-20250929-cc" />
                  <el-option label="claude-haiku-4-5-20251001-cc" value="claude-haiku-4-5-20251001-cc" />
                </el-option-group>
                <el-option-group label="Claude 4.6 (官方格式)">
                  <el-option label="claude-opus-4-6-20250929" value="claude-opus-4-6-20250929" />
                  <el-option label="claude-sonnet-4-6-20250929" value="claude-sonnet-4-6-20250929" />
                </el-option-group>
                <el-option-group label="Claude 4 / 4.5 (官方格式)">
                  <el-option label="claude-sonnet-4-20250514" value="claude-sonnet-4-20250514" />
                  <el-option label="claude-opus-4-20250514" value="claude-opus-4-20250514" />
                  <el-option label="claude-sonnet-4-5-20250514" value="claude-sonnet-4-5-20250514" />
                  <el-option label="claude-haiku-4-5-20251001" value="claude-haiku-4-5-20251001" />
                </el-option-group>
                <el-option-group label="OpenAI">
                  <el-option label="gpt-4o" value="gpt-4o" />
                  <el-option label="gpt-4o-mini" value="gpt-4o-mini" />
                  <el-option label="gpt-4.1" value="gpt-4.1" />
                </el-option-group>
                <el-option-group label="其他">
                  <el-option label="glm-4.7-cc" value="glm-4.7-cc" />
                </el-option-group>
              </el-select>
              <div class="form-tip">代理商模型名可能不同，支持手动输入</div>
            </el-form-item>
            <el-form-item label="最大Token">
              <el-input-number v-model="aiConfig.maxTokens" :min="256" :max="32768" :step="256" style="width: 100%" />
            </el-form-item>
            <template v-if="aiConfig.mode === 'direct'">
              <el-form-item label="提示词">
                <el-input v-model="aiConfig.systemPrompt" type="textarea" :rows="2" placeholder="系统提示词..." />
              </el-form-item>
            </template>
            <el-form-item>
              <el-button type="primary" size="small" @click="saveSettings" :loading="savingSettings">保存配置</el-button>
              <el-button size="small" @click="testConnection" :loading="testingConnection">测试连接</el-button>
            </el-form-item>
          </el-form>
        </div>
        </Transition>

        <!-- 消息列表 -->
        <div class="ai-messages" ref="messagesContainer">
          <!-- 空状态：快捷问题 -->
          <div v-if="messages.length === 0" class="ai-welcome">
            <div class="welcome-icon">
              <el-icon :size="48" color="#7c3aed"><ChatDotRound /></el-icon>
            </div>
            <p class="welcome-text">
              {{ aiConfig.mode === 'backend'
                ? '你好！我是 AI 助手，可以帮你创建模块、修改流程、调试脚本。'
                : '你好！我是 AI 助手，可以帮助你调试脚本、解释模块用法。' }}
            </p>
            <div v-if="!configValid" class="config-banner" @click="showSettings = true">
              <el-icon><WarningFilled /></el-icon>
              请先配置 Base URL 和 API Key
            </div>
            <div class="quick-questions">
              <el-button
                v-for="q in quickQuestions"
                :key="q.text"
                size="small"
                round
                class="quick-btn"
                @click="sendQuickQuestion(q.text)"
              >
                {{ q.label }}
              </el-button>
            </div>
          </div>

          <!-- 消息气泡 -->
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', msg.role === 'user' ? 'message-user' : 'message-ai']"
          >
            <div class="message-bubble">
              <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
              <!-- 工具执行结果 -->
              <div v-if="msg.actions && msg.actions.length > 0" class="message-actions">
                <div v-for="(action, i) in msg.actions" :key="i" class="action-item">
                  <el-icon color="#10b981"><CircleCheck /></el-icon>
                  <span>{{ action }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载指示器 -->
          <div v-if="loading && !streamingContent" class="message-item message-ai">
            <div class="message-bubble">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="ai-input-area">
          <div class="input-wrapper">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入你的问题..."
              resize="none"
              @keydown.enter.exact="handleEnter"
              :disabled="loading"
            />
            <el-button
              class="send-btn"
              type="primary"
              :icon="Promotion"
              circle
              size="small"
              @click="sendMessage"
              :disabled="!inputText.trim() || loading"
            />
          </div>
          <div class="input-hint">
            <span class="model-info">{{ aiConfig.mode === 'backend' ? 'Backend' : 'Direct' }} / {{ aiConfig.model }}</span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, computed, onMounted, watch } from 'vue'
import { ChatDotRound, Delete, Close, Promotion, WarningFilled, Setting, CircleCheck } from '@element-plus/icons-vue'
import { useFlowStore, useModuleStore } from '../store'
import { ElMessage } from 'element-plus'
import { settingsApi, aiChatApi } from '../api'
import defaultAiConfig from '../config/ai-config.json'

// --- State ---
const drawerVisible = ref(false)
const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const streamingContent = ref('')
const messagesContainer = ref(null)
const flowStore = useFlowStore()
const moduleStore = useModuleStore()
const showSettings = ref(false)
const savingSettings = ref(false)
const testingConnection = ref(false)

// AI 配置
const aiConfig = reactive({ mode: 'backend', ...defaultAiConfig })

const configValid = computed(() => !!aiConfig.apiKey && !!aiConfig.baseUrl)

/**
 * 根据 provider 和 baseUrl 拼接出完整的 API 请求地址
 */
function getFullApiUrl() {
  let base = (aiConfig.baseUrl || '').replace(/\/+$/, '') // 去掉尾部斜杠
  if (aiConfig.provider === 'anthropic') {
    return base + '/v1/messages'
  } else {
    return base + '/v1/chat/completions'
  }
}

const quickQuestions = computed(() => {
  if (aiConfig.mode === 'backend') {
    return [
      { label: '创建模块', text: '请帮我创建一个自定义模块，用于在页面上执行JavaScript代码并获取返回值。' },
      { label: '查看模块', text: '请列出所有可用的模块。' },
      { label: '分析流程', text: '请分析当前流程配置，给出优化建议。' },
      { label: '调试脚本', text: '请帮我分析当前流程可能出现的常见报错及解决方法。' }
    ]
  }
  return [
    { label: '分析报错', text: '请帮我分析当前流程可能出现的常见报错及解决方法。' },
    { label: '解释模块', text: '请解释当前流程中各个模块的功能和参数含义。' },
    { label: 'Frame用法', text: '请详细说明 Selenium 中 iframe/frame 的切换方法。' },
    { label: '优化脚本', text: '请分析当前流程配置，给出脚本优化建议。' }
  ]
})

// --- Methods ---

function handleEnter(e) {
  if (!e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function clearMessages() {
  messages.value = []
  streamingContent.value = ''
}

function getFlowContext() {
  const flow = flowStore.currentFlow
  if (!flow) return ''
  let context = ''
  if (flow.name) context += `当前流程名称: ${flow.name}\n`
  let nodes = []
  try {
    if (flow.nodesJson) nodes = JSON.parse(flow.nodesJson)
  } catch (e) { /* ignore */ }
  if (nodes.length > 0) {
    context += `\n流程节点列表 (${nodes.length} 个):\n`
    nodes.forEach((node, i) => {
      context += `${i + 1}. [${node.moduleType || node.type}] ${node.label || node.name || ''}`
      if (node.params) {
        const params = typeof node.params === 'string' ? JSON.parse(node.params) : node.params
        const paramStr = Object.entries(params)
          .filter(([_, v]) => v !== '' && v !== null && v !== undefined)
          .map(([k, v]) => `${k}=${v}`).join(', ')
        if (paramStr) context += ` (${paramStr})`
      }
      context += '\n'
    })
  }
  if (flow.generatedScript) {
    context += `\n已生成的Python脚本:\n\`\`\`python\n${flow.generatedScript}\n\`\`\`\n`
  }
  return context
}

function buildSystemPrompt() {
  let prompt = aiConfig.systemPrompt || ''
  const flowContext = getFlowContext()
  if (flowContext) prompt += `\n\n--- 当前流程上下文 ---\n${flowContext}`
  return prompt
}

function renderMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre class="code-block"><code>$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

async function sendQuickQuestion(text) {
  inputText.value = text
  await sendMessage()
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return
  if (!configValid.value) {
    ElMessage.warning('请先在设置中配置 Base URL 和 API Key')
    showSettings.value = true
    return
  }
  if (aiConfig.mode === 'backend') {
    await sendBackendMessage(text)
  } else {
    await sendDirectMessage(text)
  }
}

async function sendBackendMessage(text) {
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  await scrollToBottom()
  try {
    const chatMessages = messages.value
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .map(m => ({ role: m.role, content: m.content }))
    const flow = flowStore.currentFlow
    const response = await aiChatApi.chat({
      messages: chatMessages,
      flowId: flow?.id || null
    })
    const { content, actions } = response.data
    messages.value.push({ role: 'assistant', content, actions })
    if (actions && actions.length > 0) {
      const hasModuleAction = actions.some(a => a.toLowerCase().includes('module'))
      if (hasModuleAction) moduleStore.fetchModules()
    }
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: `调用失败: ${error.response?.data?.content || error.message}`
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function sendDirectMessage(text) {
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  streamingContent.value = ''
  await scrollToBottom()
  const aiMessageIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  try {
    if (aiConfig.provider === 'anthropic') {
      await callAnthropicAPI(aiMessageIndex)
    } else {
      await callOpenAIAPI(aiMessageIndex)
    }
  } catch (error) {
    messages.value[aiMessageIndex].content = `调用失败: ${error.message}`
  } finally {
    loading.value = false
    streamingContent.value = ''
  }
}

async function callAnthropicAPI(aiMessageIndex) {
  const systemPrompt = buildSystemPrompt()
  const apiMessages = messages.value.slice(0, -1)
    .filter(m => m.role === 'user' || m.role === 'assistant')
    .map(m => ({ role: m.role, content: m.content }))

  const body = {
    model: aiConfig.model,
    max_tokens: aiConfig.maxTokens || 4096,
    system: systemPrompt,
    messages: apiMessages,
    stream: true
  }

  const response = await fetch(getFullApiUrl(), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': aiConfig.apiKey,
      'anthropic-version': '2023-06-01',
      'anthropic-dangerous-direct-browser-access': 'true'
    },
    body: JSON.stringify(body)
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(`API ${response.status}: ${errorText}`)
  }
  await readSSEStream(response, aiMessageIndex, 'anthropic')
}

async function callOpenAIAPI(aiMessageIndex) {
  const systemPrompt = buildSystemPrompt()
  const apiMessages = [
    { role: 'system', content: systemPrompt },
    ...messages.value.slice(0, -1)
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .map(m => ({ role: m.role, content: m.content }))
  ]

  const body = {
    model: aiConfig.model,
    max_tokens: aiConfig.maxTokens || 4096,
    messages: apiMessages,
    stream: true
  }

  const response = await fetch(getFullApiUrl(), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${aiConfig.apiKey}`
    },
    body: JSON.stringify(body)
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(`API ${response.status}: ${errorText}`)
  }
  await readSSEStream(response, aiMessageIndex, 'openai')
}

async function readSSEStream(response, aiMessageIndex, provider) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      if (!line.startsWith('data: ')) continue
      const data = line.slice(6).trim()
      if (data === '[DONE]') return
      try {
        const parsed = JSON.parse(data)
        let text = ''
        if (provider === 'anthropic') {
          if (parsed.type === 'content_block_delta' && parsed.delta?.text) text = parsed.delta.text
        } else {
          if (parsed.choices?.[0]?.delta?.content) text = parsed.choices[0].delta.content
        }
        if (text) {
          streamingContent.value += text
          messages.value[aiMessageIndex].content = streamingContent.value
          await scrollToBottom()
        }
      } catch (e) { /* skip */ }
    }
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
}

watch(drawerVisible, (val) => { if (val) scrollToBottom() })

async function testConnection() {
  if (!configValid.value) {
    ElMessage.warning('请先填写 Base URL 和 API Key')
    return
  }
  testingConnection.value = true
  try {
    const url = getFullApiUrl()
    const headers = { 'Content-Type': 'application/json' }
    let body

    if (aiConfig.provider === 'anthropic') {
      headers['x-api-key'] = aiConfig.apiKey
      headers['anthropic-version'] = '2023-06-01'
      headers['anthropic-dangerous-direct-browser-access'] = 'true'
      body = JSON.stringify({
        model: aiConfig.model,
        max_tokens: 32,
        messages: [{ role: 'user', content: 'Hi' }]
      })
    } else {
      headers['Authorization'] = `Bearer ${aiConfig.apiKey}`
      body = JSON.stringify({
        model: aiConfig.model,
        max_tokens: 32,
        messages: [{ role: 'user', content: 'Hi' }]
      })
    }

    const response = await fetch(url, { method: 'POST', headers, body })
    if (response.ok) {
      ElMessage.success(`连接成功！模型: ${aiConfig.model}`)
    } else {
      const errText = await response.text()
      ElMessage.error(`连接失败 (${response.status}): ${errText.substring(0, 200)}`)
    }
  } catch (e) {
    ElMessage.error('连接失败: ' + e.message)
  } finally {
    testingConnection.value = false
  }
}

async function loadSettings() {
  try {
    const response = await settingsApi.getGroup('ai')
    const ai = response.data
    if (ai && Object.keys(ai).length > 0) {
      if (ai['ai.mode']) aiConfig.mode = ai['ai.mode']
      if (ai['ai.provider']) aiConfig.provider = ai['ai.provider']
      if (ai['ai.baseUrl']) aiConfig.baseUrl = ai['ai.baseUrl']
      // 兼容旧配置：如果只存了 apiUrl 没存 baseUrl
      if (!ai['ai.baseUrl'] && ai['ai.apiUrl']) {
        aiConfig.baseUrl = ai['ai.apiUrl'].replace(/\/v1\/(messages|chat\/completions)$/, '')
      }
      if (ai['ai.apiKey']) aiConfig.apiKey = ai['ai.apiKey']
      if (ai['ai.model']) aiConfig.model = ai['ai.model']
      if (ai['ai.maxTokens']) aiConfig.maxTokens = parseInt(ai['ai.maxTokens']) || 4096
      if (ai['ai.systemPrompt']) aiConfig.systemPrompt = ai['ai.systemPrompt']
    }
  } catch (e) {
    console.debug('Using default AI config:', e.message)
  }
}

async function saveSettings() {
  try {
    savingSettings.value = true
    await settingsApi.saveGroup('ai', {
      'ai.mode': aiConfig.mode,
      'ai.provider': aiConfig.provider,
      'ai.baseUrl': aiConfig.baseUrl,
      'ai.apiKey': aiConfig.apiKey,
      'ai.model': aiConfig.model,
      'ai.maxTokens': String(aiConfig.maxTokens),
      'ai.systemPrompt': aiConfig.systemPrompt || ''
    })
    ElMessage.success('AI 配置已保存')
    showSettings.value = false
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.message || '未知错误'))
  } finally {
    savingSettings.value = false
  }
}

onMounted(() => { loadSettings() })
</script>

<style scoped>
.ai-fab {
  position: fixed;
  right: 28px;
  bottom: 28px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.4);
  z-index: 2000;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease;
  animation: fabPulse 2s ease-in-out infinite;
}
.ai-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 28px rgba(124, 58, 237, 0.55);
}
@keyframes fabPulse {
  0%, 100% { box-shadow: 0 4px 20px rgba(124, 58, 237, 0.4); }
  50% { box-shadow: 0 4px 28px rgba(124, 58, 237, 0.65); }
}
.ai-drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}
.ai-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ai-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}
.ai-header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  scroll-behavior: smooth;
}
.ai-settings-panel {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(124, 58, 237, 0.03);
  flex-shrink: 0;
  max-height: 500px;
  overflow-y: auto;
}
.ai-settings-panel :deep(.el-form-item) {
  margin-bottom: 12px;
}
.ai-settings-panel :deep(.el-form-item__label) {
  font-size: 12px;
  font-weight: 500;
  color: #555;
}
.form-tip {
  font-size: 11px;
  color: #909399;
  line-height: 1.4;
  margin-top: 4px;
}
.settings-slide-enter-active {
  animation: settingsIn 0.3s ease-out;
}
.settings-slide-leave-active {
  animation: settingsOut 0.2s ease-in;
}
@keyframes settingsIn {
  0% { max-height: 0; opacity: 0; padding-top: 0; padding-bottom: 0; }
  100% { max-height: 500px; opacity: 1; padding-top: 16px; padding-bottom: 16px; }
}
@keyframes settingsOut {
  0% { max-height: 500px; opacity: 1; }
  100% { max-height: 0; opacity: 0; padding-top: 0; padding-bottom: 0; }
}
.ai-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
}
.welcome-icon {
  margin-bottom: 16px;
  opacity: 0.8;
}
.welcome-text {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
}
.config-banner {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: #fef3c7;
  border: 1px solid #fcd34d;
  border-radius: 10px;
  color: #92400e;
  font-size: 13px;
  cursor: pointer;
  margin-bottom: 16px;
  transition: background 0.2s;
}
.config-banner:hover {
  background: #fde68a;
}
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.quick-btn {
  border-color: #7c3aed;
  color: #7c3aed;
}
.quick-btn:hover {
  background: rgba(124, 58, 237, 0.08);
  border-color: #7c3aed;
  color: #7c3aed;
}
.message-item {
  display: flex;
  margin-bottom: 12px;
}
.message-user {
  justify-content: flex-end;
}
.message-ai {
  justify-content: flex-start;
}
.message-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.message-user .message-bubble {
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.message-ai .message-bubble {
  background: #f3f4f6;
  color: #1d1d1f;
  border-bottom-left-radius: 4px;
}
.message-actions {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}
.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #10b981;
  margin-bottom: 4px;
}
.message-content :deep(pre.code-block) {
  background: rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  padding: 10px 12px;
  margin: 8px 0;
  overflow-x: auto;
  font-size: 13px;
}
.message-content :deep(code.inline-code) {
  background: rgba(0, 0, 0, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.message-user .message-content :deep(pre.code-block) {
  background: rgba(255, 255, 255, 0.15);
}
.message-user .message-content :deep(code.inline-code) {
  background: rgba(255, 255, 255, 0.2);
}
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}
.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #9ca3af;
  animation: typingBounce 1.4s ease-in-out infinite;
}
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}
.ai-input-area {
  padding: 12px 20px 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}
.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}
.input-wrapper :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 8px 14px;
  font-size: 14px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: none;
}
.input-wrapper :deep(.el-textarea__inner:focus) {
  border-color: #7c3aed;
  box-shadow: 0 0 0 2px rgba(124, 58, 237, 0.15);
}
.send-btn {
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  border: none;
  flex-shrink: 0;
}
.send-btn:hover {
  background: linear-gradient(135deg, #6d28d9, #9333ea);
}
.send-btn:disabled {
  opacity: 0.5;
}
.input-hint {
  margin-top: 6px;
  font-size: 11px;
  color: #9ca3af;
  padding-left: 4px;
}
.model-info {
  color: #9ca3af;
}
:deep(.el-drawer) {
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.1) !important;
}
:deep(.el-drawer__body) {
  padding: 0 !important;
  height: 100%;
}
</style>
