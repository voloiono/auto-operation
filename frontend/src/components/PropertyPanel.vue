<template>
  <div class="property-panel glass">
    <div class="panel-header">
      <h3>参数配置</h3>
      <p class="panel-subtitle">编辑选中模块的参数</p>
    </div>

    <div v-if="!selectedNode" class="empty-state">
      <div class="empty-icon">⚙️</div>
      <p>选择一个模块来编辑参数</p>
    </div>
    <div v-else class="property-form">
      <el-form :model="selectedNode" label-width="100px">
        <el-form-item label="模块类型">
          <el-input v-model="selectedNode.type" disabled />
        </el-form-item>
        <el-form-item label="模块名称">
          <el-input v-model="selectedNode.name" disabled />
        </el-form-item>
        <el-divider />

        <div v-if="Object.keys(selectedNode.params).length === 0" class="no-params">
          <p>此模块无需配置参数</p>
        </div>
        <div v-else>
          <!-- 邮件模块：可用变量提示 -->
          <div v-if="availableVariables.length > 0" class="variable-hint-box">
            <div class="variable-hint-label">可用变量（点击插入到文本字段）</div>
            <div class="variable-tags">
              <el-tag
                v-for="v in availableVariables"
                :key="v"
                class="variable-tag"
                type="success"
                effect="plain"
                @click="insertVariable(v, 'body')"
              >
                {<span>{{ v }}</span>}
              </el-tag>
            </div>
          </div>

          <!-- ===== 邮件模块专用布局：折叠分组 ===== -->
          <template v-if="selectedNode?.type === 'send_email'">
            <!-- 邮件内容（始终显示） -->
            <div v-for="key in emailContentKeys" :key="key">
              <el-form-item :label="getParamLabel(key)">
                <el-input
                  v-model="selectedNode.params[key]"
                  :type="getInputType(key)"
                  :placeholder="`请输入${getParamLabel(key)}`"
                  @input="updateParam(key, $event)"
                />
                <div v-if="availableVariables.length > 0 && isVariableInsertable(key)" class="field-variable-tags">
                  <span class="field-variable-label">插入变量：</span>
                  <el-tag
                    v-for="v in availableVariables"
                    :key="v"
                    size="small"
                    class="variable-tag"
                    type="success"
                    effect="plain"
                    @click="insertVariable(v, key)"
                  >
                    {<span>{{ v }}</span>}
                  </el-tag>
                </div>
                <div class="param-hint">{{ getParamHint(key) }}</div>
              </el-form-item>
            </div>

            <!-- 服务器 & 代理配置（可折叠） -->
            <el-collapse v-model="emailCollapse" class="email-collapse">
              <el-collapse-item title="服务器配置" name="smtp">
                <div v-for="key in emailSmtpKeys" :key="key">
                  <el-form-item :label="getParamLabel(key)">
                    <el-select
                      v-if="getSelectOptions(key)"
                      v-model="selectedNode.params[key]"
                      :placeholder="`请选择${getParamLabel(key)}`"
                      @change="updateParam(key, $event)"
                    >
                      <el-option
                        v-for="option in getSelectOptions(key)"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                      />
                    </el-select>
                    <el-input
                      v-else
                      v-model="selectedNode.params[key]"
                      :type="getInputType(key)"
                      :placeholder="`请输入${getParamLabel(key)}`"
                      @input="updateParam(key, $event)"
                    />
                    <div class="param-hint">{{ getParamHint(key) }}</div>
                  </el-form-item>
                </div>
              </el-collapse-item>
              <el-collapse-item title="代理配置（可选）" name="proxy">
                <div v-for="key in emailProxyKeys" :key="key">
                  <el-form-item :label="getParamLabel(key)">
                    <el-input
                      v-model="selectedNode.params[key]"
                      :type="getInputType(key)"
                      :placeholder="`请输入${getParamLabel(key)}`"
                      @input="updateParam(key, $event)"
                    />
                    <div class="param-hint">{{ getParamHint(key) }}</div>
                  </el-form-item>
                </div>
              </el-collapse-item>
            </el-collapse>
          </template>

          <!-- ===== 非邮件模块：通用渲染 ===== -->
          <template v-else>
          <div v-for="key in sortedParamKeys" :key="key">
            <el-form-item
              v-if="isParamVisible(key)"
              :label="getParamLabel(key)"
            >
              <el-select
                v-if="getSelectOptions(key)"
                v-model="selectedNode.params[key]"
                :placeholder="`请选择${getParamLabel(key)}`"
                @change="updateParam(key, $event)"
              >
                <el-option
                  v-for="option in getSelectOptions(key)"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <!-- CSS选择器字段：输入框 + 拾取按钮 -->
              <div v-else-if="isSelectorField(key)" class="selector-input-row">
                <el-input
                  v-model="selectedNode.params[key]"
                  :type="getInputType(key)"
                  :placeholder="`请输入${getParamLabel(key)}`"
                  @input="updateParam(key, $event)"
                />
                <el-tooltip
                  v-if="currentLocateBy === 'css'"
                  content="打开选择器助手"
                  placement="top"
                >
                  <el-button
                    class="picker-btn"
                    @click="openSelectorHelper(key)"
                  >
                    <el-icon><Aim /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
              <!-- 定时执行：多时间点选择器 -->
              <div v-else-if="key === 'schedule_times'" class="schedule-times-picker">
                <div class="schedule-times-tags">
                  <el-tag
                    v-for="(t, idx) in parsedScheduleTimes"
                    :key="idx"
                    closable
                    @close="removeScheduleTime(idx)"
                    type="primary"
                    effect="plain"
                    size="default"
                  >
                    {{ t }}
                  </el-tag>
                  <span v-if="parsedScheduleTimes.length === 0" class="schedule-times-empty">未添加时间点</span>
                </div>
                <div class="schedule-times-add">
                  <el-time-picker
                    v-model="newScheduleTimeValue"
                    format="HH:mm"
                    value-format="HH:mm"
                    placeholder="选择时间"
                    size="small"
                    style="width: 120px"
                  />
                  <el-button type="primary" size="small" @click="addScheduleTime" :disabled="!newScheduleTimeValue">
                    添加
                  </el-button>
                </div>
              </div>
              <!-- 定时执行：自定义星期选择器 -->
              <div v-else-if="key === 'repeat_days'" class="repeat-days-picker">
                <el-checkbox-group v-model="parsedRepeatDays" @change="onRepeatDaysChange">
                  <el-checkbox :label="0">周一</el-checkbox>
                  <el-checkbox :label="1">周二</el-checkbox>
                  <el-checkbox :label="2">周三</el-checkbox>
                  <el-checkbox :label="3">周四</el-checkbox>
                  <el-checkbox :label="4">周五</el-checkbox>
                  <el-checkbox :label="5">周六</el-checkbox>
                  <el-checkbox :label="6">周日</el-checkbox>
                </el-checkbox-group>
              </div>
              <el-input
                v-else
                v-model="selectedNode.params[key]"
                :type="getInputType(key)"
                :placeholder="`请输入${getParamLabel(key)}`"
                @input="updateParam(key, $event)"
              />
              <!-- 邮件 subject/body 字段的变量插入按钮 -->
              <div v-if="availableVariables.length > 0 && isVariableInsertable(key)" class="field-variable-tags">
                <span class="field-variable-label">插入变量：</span>
                <el-tag
                  v-for="v in availableVariables"
                  :key="v"
                  size="small"
                  class="variable-tag"
                  type="success"
                  effect="plain"
                  @click="insertVariable(v, key)"
                >
                  {<span>{{ v }}</span>}
                </el-tag>
              </div>
              <div class="param-hint">{{ getParamHint(key) }}</div>
            </el-form-item>
          </div>
          </template>
        </div>

        <!-- 批量输入专用 UI：动态输入项列表 -->
        <template v-if="isBatchInputModule">
          <el-divider content-position="left">输入项列表</el-divider>
          <div class="batch-input-hint">
            每一项对应页面上的一个输入框，点击下方按钮添加
          </div>

          <div
            v-for="(item, index) in batchInputs"
            :key="index"
            class="batch-input-item"
          >
            <div class="batch-input-header">
              <span class="batch-input-number">{{ index + 1 }}</span>
              <span class="batch-input-title">输入项 {{ index + 1 }}</span>
              <el-button
                type="danger"
                link
                size="small"
                @click="removeInputItem(index)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
            <el-form-item :label="batchLocateByLabel" label-width="80px">
              <el-input
                v-model="item.selector"
                :placeholder="currentLocateBy === 'css' ? '例如: #name 或 .input-field' : '输入定位值'"
                size="small"
              />
            </el-form-item>
            <el-form-item label="输入内容" label-width="80px">
              <el-input
                v-model="item.text"
                placeholder="要输入的文本"
                size="small"
              />
            </el-form-item>
            <el-form-item label="先清空" label-width="80px">
              <el-switch v-model="item.clear_first" size="small" />
            </el-form-item>
          </div>

          <div v-if="batchInputs.length === 0" class="batch-input-empty">
            暂无输入项，请点击下方按钮添加
          </div>

          <el-button
            type="primary"
            plain
            style="width: 100%; margin-top: 8px"
            @click="addInputItem"
          >
            <el-icon><Plus /></el-icon>
            添加输入项
          </el-button>
        </template>
      </el-form>
    </div>

    <!-- 选择器助手对话框 -->
    <SelectorHelper
      v-model="showSelectorHelper"
      @select="onSelectorPicked"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Aim, Plus, Delete } from '@element-plus/icons-vue'
import { useFlowStore } from '../store'
import SelectorHelper from './SelectorHelper.vue'

const flowStore = useFlowStore()

const showSelectorHelper = ref(false)
const currentPickerField = ref(null)

// 邮件模块折叠面板状态（默认不展开，节省空间）
const emailCollapse = ref([])
// 邮件模块字段分组
const emailContentKeys = ['to', 'subject', 'body']
const emailSmtpKeys = ['smtp_host', 'smtp_port', 'smtp_user', 'smtp_pass', 'smtp_ssl']
const emailProxyKeys = ['proxy_host', 'proxy_port']

const selectedNode = computed(() => {
  return flowStore.currentFlow?.selectedNode || null
})

// 参数排序：locate_by 排在 selector 之前
const sortedParamKeys = computed(() => {
  if (!selectedNode.value) return []
  const keys = Object.keys(selectedNode.value.params)
  const order = ['monitor_mode', 'close_method', 'frame_action', 'frame_locator', 'target', 'action', 'extract_type', 'locate_by', 'selector', 'element_tag', 'username_selector', 'password_selector', 'smtp_host', 'smtp_port', 'smtp_user', 'smtp_pass', 'smtp_ssl', 'proxy_host', 'proxy_port', 'to', 'subject', 'body', 'repeat_mode', 'repeat_days', 'schedule_times', 'restart_delay', 'max_retries', 'auto_start', 'run_hidden', 'rdp_mode']
  return keys.sort((a, b) => {
    const ia = order.indexOf(a)
    const ib = order.indexOf(b)
    const oa = ia === -1 ? 100 + keys.indexOf(a) : ia
    const ob = ib === -1 ? 100 + keys.indexOf(b) : ib
    return oa - ob
  })
})

// 获取当前模块的 locate_by 值
const currentLocateBy = computed(() => {
  if (!selectedNode.value) return 'css'
  return selectedNode.value.params.locate_by || 'css'
})

// 是否为 selector 类字段
const isSelectorField = (key) => {
  return key === 'selector' || key === 'username_selector' || key === 'password_selector'
}

// 条件显示：element_tag 只在 locate_by=index 时显示
// close_popup 模块中 locate_by/selector/element_tag 只在 close_method=click_element 时显示
// batch_input 模块中 inputs 参数由专用 UI 渲染，从通用列表中隐藏
const isParamVisible = (key) => {
  if (key === 'inputs') return false  // 由专用 UI 渲染
  if (key === 'element_tag') {
    return currentLocateBy.value === 'index'
  }
  if (selectedNode.value?.type === 'close_popup') {
    const monitorMode = selectedNode.value.params.monitor_mode || 'single'
    const closeMethod = selectedNode.value.params.close_method || 'auto_detect'

    if (monitorMode === 'continuous') {
      // 全程监控模式：显示 monitor_mode, close_method, custom_selectors, ignore_error
      // 隐藏单次关闭专用参数
      if (key === 'locate_by' || key === 'selector' || key === 'element_tag' || key === 'wait_time') {
        return false
      }
      // custom_selectors 仅在包含 DOM 弹窗检测时显示
      if (key === 'custom_selectors') {
        return closeMethod === 'auto_detect' || closeMethod === 'dom_only'
      }
    } else {
      // 单次关闭模式：隐藏全程监控专用参数
      if (key === 'custom_selectors') {
        return false
      }
      if (key === 'locate_by' || key === 'selector' || key === 'element_tag') {
        return closeMethod === 'click_element'
      }
    }
  }
  // switch_frame: frame_locator 在"返回主页面"和"返回上级Frame"时隐藏
  if (selectedNode.value?.type === 'switch_frame' && key === 'frame_locator') {
    const action = selectedNode.value.params.frame_action || 'by_name'
    return action !== 'main' && action !== 'parent'
  }
  // press_enter: locate_by/selector/element_tag 仅在 target=element 时显示
  if (selectedNode.value?.type === 'press_enter') {
    if (key === 'locate_by' || key === 'selector' || key === 'element_tag') {
      return selectedNode.value.params.target === 'element'
    }
  }
  // scheduled_task: repeat_days 仅在 repeat_mode=weekly 时显示
  if (selectedNode.value?.type === 'scheduled_task' && key === 'repeat_days') {
    return selectedNode.value.params.repeat_mode === 'weekly'
  }
  return true
}

const getParamLabel = (key) => {
  // selector 类字段根据 locate_by 动态切换标签
  if (key === 'selector' || key === 'username_selector' || key === 'password_selector') {
    const baseLabel = {
      'selector': { css: '元素选择器', text: '元素文字', index: '元素序号' },
      'username_selector': { css: '账号选择器', text: '账号文字', index: '账号序号' },
      'password_selector': { css: '密码选择器', text: '密码文字', index: '密码序号' }
    }
    return baseLabel[key]?.[currentLocateBy.value] || baseLabel[key]?.css || key
  }

  const labels = {
    'locate_by': '定位方式',
    'element_tag': '元素类型',
    'browser_type': '浏览器类型',
    'url': '网址',
    'username': '账号',
    'password': '密码',
    'wait_time': '等待时间(秒)',
    'text': '输入文本',
    'clear_first': '先清空',
    'value': '选项值',
    'select_by': '选择方式',
    'timeout': '超时时间(秒)',
    'var_name': '变量名',
    'filename': '文件名',
    'attribute': '属性名',
    'close_method': '关闭方式',
    'monitor_mode': '监控模式',
    'ignore_error': '忽略错误',
    'inputs': '输入项列表',
    'frame_action': '切换方式',
    'frame_locator': 'Frame标识',
    'target': '回车目标',
    'extract_type': '提取方式',
    'action': '操作类型',
    'custom_selectors': '自定义选择器',
    'to': '收件人',
    'subject': '主题',
    'body': '正文',
    'smtp_host': 'SMTP服务器',
    'smtp_port': 'SMTP端口',
    'smtp_user': '发件人账号',
    'smtp_pass': '密码/授权码',
    'smtp_ssl': '加密方式',
    'proxy_host': '代理地址',
    'proxy_port': '代理端口',
    'log_folder': '日志文件夹名',
    'repeat_mode': '重复方式',
    'repeat_days': '执行星期',
    'schedule_times': '执行时间',
    'restart_delay': '重启延迟(秒)',
    'max_retries': '最大重试',
    'auto_start': '开机自启',
    'run_hidden': '后台运行',
    'rdp_mode': '远程桌面模式'
  }
  return labels[key] || key
}

const getInputType = (key) => {
  if (key.includes('password') || key === 'smtp_pass') {
    return 'password'
  }
  if (key === 'wait_time' || key === 'timeout') {
    return 'number'
  }
  if (key === 'body') {
    return 'textarea'
  }
  return 'text'
}

const getSelectOptions = (key) => {
  const options = {
    'locate_by': [
      { label: 'CSS选择器', value: 'css' },
      { label: '按文字查找', value: 'text' },
      { label: '按序号定位', value: 'index' }
    ],
    'element_tag': [
      { label: '输入框 (input)', value: 'input' },
      { label: '按钮 (button)', value: 'button' },
      { label: '链接 (a)', value: 'a' },
      { label: '任意元素 (*)', value: '*' }
    ],
    'browser_type': [
      { label: 'Chrome', value: 'chrome' },
      { label: 'Firefox', value: 'firefox' },
      { label: 'Edge', value: 'edge' },
      { label: 'Internet Explorer', value: 'ie' }
    ],
    'select_by': [
      { label: '按值选择', value: 'value' },
      { label: '按文本选择', value: 'text' }
    ],
    'clear_first': [
      { label: '是', value: true },
      { label: '否', value: false }
    ],
    'close_method': [
      { label: '自动检测（全部尝试）', value: 'auto_detect' },
      { label: '仅页面弹窗（DOM模态框）', value: 'dom_only' },
      { label: '点击指定关闭按钮', value: 'click_element' },
      { label: 'Alert弹窗 → 确认', value: 'alert_accept' },
      { label: 'Alert弹窗 → 关闭', value: 'alert_dismiss' },
      { label: 'Confirm对话框 → 确认', value: 'confirm_accept' },
      { label: 'Confirm对话框 → 取消', value: 'confirm_dismiss' }
    ],
    'monitor_mode': [
      { label: '单次关闭（流程中的一步）', value: 'single' },
      { label: '全程监控（后台持续检测）', value: 'continuous' }
    ],
    'ignore_error': [
      { label: '是（找不到弹窗则跳过）', value: true },
      { label: '否（找不到弹窗则报错）', value: false }
    ],
    'frame_action': [
      { label: '按名称/ID切入', value: 'by_name' },
      { label: '按CSS选择器切入', value: 'by_css' },
      { label: '按序号切入', value: 'by_index' },
      { label: '返回主页面', value: 'main' },
      { label: '返回上级Frame', value: 'parent' }
    ],
    'target': [
      { label: '当前焦点元素', value: 'active' },
      { label: '指定元素', value: 'element' }
    ],
    'extract_type': [
      { label: '文本内容 (text)', value: 'text' },
      { label: '内部HTML (innerHTML)', value: 'innerHTML' },
      { label: '完整HTML (outerHTML)', value: 'outerHTML' },
      { label: '表单值 (value)', value: 'value' }
    ],
    'action': [
      { label: '确认 (Accept)', value: 'accept' },
      { label: '取消 (Dismiss)', value: 'dismiss' }
    ],
    'smtp_ssl': [
      { label: 'STARTTLS (端口587)', value: 'tls' },
      { label: 'SSL (端口465)', value: 'ssl' },
      { label: '无加密 (端口25)', value: 'none' }
    ],
    'auto_start': [
      { label: '是', value: 'true' },
      { label: '否', value: 'false' }
    ],
    'run_hidden': [
      { label: '是', value: 'true' },
      { label: '否', value: 'false' }
    ],
    'rdp_mode': [
      { label: '是（远程桌面运行）', value: 'true' },
      { label: '否', value: 'false' }
    ],
    'repeat_mode': [
      { label: '每天重复', value: 'daily' },
      { label: '工作日重复（周一至周五）', value: 'weekdays' },
      { label: '自定义星期', value: 'weekly' },
      { label: '不重复（仅执行一次）', value: 'once' }
    ]
  }

  // close_method: 全程监控模式下不显示"点击指定关闭按钮"
  if (key === 'close_method' && selectedNode.value?.type === 'close_popup') {
    const monitorMode = selectedNode.value.params.monitor_mode || 'single'
    if (monitorMode === 'continuous') {
      return [
        { label: '自动检测（全部尝试）', value: 'auto_detect' },
        { label: '仅页面弹窗（DOM模态框）', value: 'dom_only' },
        { label: 'Alert弹窗 → 确认', value: 'alert_accept' },
        { label: 'Alert弹窗 → 关闭', value: 'alert_dismiss' },
        { label: 'Confirm对话框 → 确认', value: 'confirm_accept' },
        { label: 'Confirm对话框 → 取消', value: 'confirm_dismiss' }
      ]
    }
  }

  return options[key] || null
}

const getParamHint = (key) => {
  // selector 类字段根据 locate_by 动态切换提示
  if (key === 'selector') {
    const hints = {
      css: 'CSS选择器，例如: #button 或 .submit-btn',
      text: "按钮上的文字，如'登录'、'提交'",
      index: '第几个元素（从1开始），例如: 3'
    }
    return hints[currentLocateBy.value] || hints.css
  }
  if (key === 'username_selector') {
    const hints = {
      css: '例如: #username 或 .login-input',
      text: "账号输入框附近的文字，如'用户名'",
      index: '第几个输入框（从1开始），例如: 1'
    }
    return hints[currentLocateBy.value] || hints.css
  }
  if (key === 'password_selector') {
    const hints = {
      css: '例如: #password 或 input[type="password"]',
      text: "密码输入框附近的文字，如'密码'",
      index: '第几个输入框（从1开始），例如: 2'
    }
    return hints[currentLocateBy.value] || hints.css
  }

  const hints = {
    'locate_by': '选择元素的定位方式',
    'element_tag': '用于序号定位时指定元素类型',
    'browser_type': '支持 Chrome、Firefox、Edge、Internet Explorer',
    'url': '例如: https://www.example.com',
    'username': '输入用户名或邮箱',
    'password': '输入密码',
    'wait_time': '单位为秒，例如: 2',
    'text': '要输入的文本内容',
    'clear_first': '是否先清空输入框',
    'value': '下拉框选项的值',
    'select_by': '选择方式: value 或 text',
    'timeout': '最多等待秒数，例如: 10',
    'var_name': '保存结果的变量名',
    'filename': '保存文件的名称，例如: screenshot.png',
    'attribute': '要获取的属性名，例如: href, src',
    'close_method': '自动检测会依次尝试浏览器弹窗和页面弹窗；选择"仅页面弹窗"则只关闭DOM模态框',
    'monitor_mode': '单次关闭：作为流程中的一步执行；全程监控：启动后台线程在整个流程期间持续检测弹窗',
    'ignore_error': '开启后，如果未找到弹窗将静默跳过不中断流程',
    'frame_action': '选择器拾取器会显示 [Frame: xxx] 前缀，xxx 就是这里要填的名称',
    'frame_locator': '拾取器复制的 [Frame: TRAK_main] 中的 TRAK_main',
    'target': '当前焦点：对刚刚输入过的输入框按回车；指定元素：先定位再按回车',
    'extract_type': '选择要提取的内容类型：文本、HTML或表单值',
    'action': '确认：点击OK按钮；取消：点击Cancel按钮',
    'custom_selectors': '自定义关闭按钮的CSS选择器，多个用逗号分隔，例如: .my-close-btn, #dismiss',
    'to': '收件人邮箱地址，多个用逗号分隔',
    'subject': '邮件主题',
    'body': '邮件正文内容',
    'smtp_host': 'SMTP服务器地址，例如: smtp.gmail.com',
    'smtp_port': '端口号，TLS一般587，SSL一般465',
    'smtp_user': '发件人邮箱地址',
    'smtp_pass': 'SMTP密码或应用专用授权码',
    'smtp_ssl': '选择加密方式，需与端口匹配',
    'proxy_host': '代理服务器地址（留空则直连），例如: proxy.example.com',
    'proxy_port': '代理端口号，例如: 8080',
    'log_folder': '桌面上保存错误日志的文件夹名称',
    'repeat_mode': '选择执行的重复周期',
    'repeat_days': '选择每周哪几天执行',
    'schedule_times': '执行时间点，逗号分隔，例如: 09:00,12:00,18:00',
    'restart_delay': '执行失败后等待多少秒再重试，默认60',
    'max_retries': '最大重试次数，0表示无限重试',
    'auto_start': '开机时自动启动程序（仅Windows）',
    'run_hidden': '运行时隐藏控制台窗口，在后台静默执行',
    'rdp_mode': '开启后浏览器以无头模式运行，断开远程桌面后程序仍可正常执行（支持JumpServer）'
  }
  return hints[key] || ''
}

const updateParam = (key, value) => {
  if (selectedNode.value) {
    selectedNode.value.params[key] = value
  }
}

// 选择器助手
const openSelectorHelper = (key) => {
  currentPickerField.value = key
  showSelectorHelper.value = true
}

const onSelectorPicked = (selector) => {
  if (selectedNode.value && currentPickerField.value) {
    selectedNode.value.params[currentPickerField.value] = selector
  }
}

// ==================== 批量输入（batch_input）动态列表 ====================
const isBatchInputModule = computed(() => {
  return selectedNode.value?.type === 'batch_input'
})

const batchInputs = computed(() => {
  if (!isBatchInputModule.value) return []
  const raw = selectedNode.value.params.inputs
  if (Array.isArray(raw)) return raw
  if (typeof raw === 'string' && raw) {
    try { return JSON.parse(raw) } catch { return [] }
  }
  return []
})

const ensureInputsArray = () => {
  if (!selectedNode.value) return
  if (!Array.isArray(selectedNode.value.params.inputs)) {
    selectedNode.value.params.inputs = []
  }
}

const addInputItem = () => {
  ensureInputsArray()
  selectedNode.value.params.inputs.push({ selector: '', text: '', clear_first: true })
}

const removeInputItem = (index) => {
  ensureInputsArray()
  selectedNode.value.params.inputs.splice(index, 1)
}

const batchLocateByLabel = computed(() => {
  const labels = { css: '选择器', text: '元素文字', index: '元素序号' }
  return labels[currentLocateBy.value] || '选择器'
})

// ==================== 可用变量（所有模块通用） ====================
const availableVariables = computed(() => {
  const flow = flowStore.currentFlow
  if (!flow?.nodes || flow.selectedNodeIndex == null) return []
  const vars = []
  for (let i = 0; i < flow.selectedNodeIndex; i++) {
    const node = flow.nodes[i]
    if (node.type === 'extract_content' && node.params?.var_name) {
      vars.push(node.params.var_name)
    }
  }
  return vars
})

const insertVariable = (varName, paramKey) => {
  if (!selectedNode.value) return
  const current = selectedNode.value.params[paramKey] || ''
  selectedNode.value.params[paramKey] = current + `{${varName}}`
}

// 判断字段是否支持变量插入（纯文本输入字段）
const isVariableInsertable = (key) => {
  // 排除：下拉框、密码、数字、内部标识字段
  if (getSelectOptions(key)) return false
  const excluded = ['var_name', 'locate_by', 'element_tag', 'filename', 'log_folder',
    'wait_time', 'timeout', 'restart_delay', 'max_retries', 'smtp_port', 'proxy_port',
    'schedule_times', 'repeat_days']
  if (excluded.includes(key)) return false
  const inputType = getInputType(key)
  if (inputType === 'password' || inputType === 'number') return false
  return true
}

// ==================== 定时执行：多时间点管理 ====================
const newScheduleTimeValue = ref(null)

const parsedScheduleTimes = computed(() => {
  if (!selectedNode.value || selectedNode.value.type !== 'scheduled_task') return []
  const raw = selectedNode.value.params.schedule_times || ''
  return raw.split(',').map(s => s.trim()).filter(Boolean).sort()
})

const addScheduleTime = () => {
  if (!newScheduleTimeValue.value || !selectedNode.value) return
  const times = parsedScheduleTimes.value.slice()
  if (!times.includes(newScheduleTimeValue.value)) {
    times.push(newScheduleTimeValue.value)
    times.sort()
    selectedNode.value.params.schedule_times = times.join(',')
  }
  newScheduleTimeValue.value = null
}

const removeScheduleTime = (idx) => {
  if (!selectedNode.value) return
  const times = parsedScheduleTimes.value.slice()
  times.splice(idx, 1)
  selectedNode.value.params.schedule_times = times.join(',')
}

// ==================== 定时执行：自定义星期管理 ====================
const parsedRepeatDays = computed({
  get() {
    if (!selectedNode.value || selectedNode.value.type !== 'scheduled_task') return []
    const raw = selectedNode.value.params.repeat_days || ''
    return raw.split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n))
  },
  set() { /* handled by onRepeatDaysChange */ }
})

const onRepeatDaysChange = (val) => {
  if (!selectedNode.value) return
  const sorted = [...val].sort()
  selectedNode.value.params.repeat_days = sorted.join(',')
}
</script>

<style scoped>
.property-panel {
  height: 100%;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.panel-header {
  margin-bottom: 20px;
  flex-shrink: 0;
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #6e6e73;
  text-align: center;
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 13px;
}

.property-form {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding-right: 4px;
}

/* 滚动条美化 */
.property-form::-webkit-scrollbar {
  width: 6px;
}

.property-form::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.property-form::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}

.property-form::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #1d1d1d;
  font-size: 13px;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 113, 227, 0.2);
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #0071e3;
  background: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #0071e3;
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.1);
  background: white;
}

:deep(.el-input__inner) {
  font-size: 13px;
}

.param-hint {
  font-size: 11px;
  color: #a1a1a6;
  margin-top: 6px;
  padding: 0 12px;
  line-height: 1.4;
}

.no-params {
  text-align: center;
  padding: 30px 20px;
  color: #6e6e73;
  font-size: 13px;
}

:deep(.el-divider) {
  margin: 16px 0;
  border-color: rgba(0, 0, 0, 0.06);
}

:deep(.el-form-item__content) {
  line-height: 1;
}

/* 选择器输入行：输入框 + 拾取按钮 */
.selector-input-row {
  display: flex;
  gap: 6px;
  width: 100%;
}

.selector-input-row .el-input {
  flex: 1;
}

.picker-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 8px;
  border: 1px solid rgba(0, 113, 227, 0.3);
  background: rgba(0, 113, 227, 0.06);
  color: #0071e3;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.picker-btn:hover {
  background: #0071e3;
  color: white;
  border-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.3);
}

/* 批量输入动态列表 */
.batch-input-hint {
  font-size: 12px;
  color: #6e6e73;
  margin-bottom: 12px;
  padding: 0 4px;
}

.batch-input-item {
  background: rgba(0, 113, 227, 0.03);
  border: 1px solid rgba(0, 113, 227, 0.15);
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}

.batch-input-item:hover {
  border-color: rgba(0, 113, 227, 0.3);
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.08);
}

.batch-input-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.batch-input-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  background: linear-gradient(135deg, #0071e3, #0077ed);
  color: white;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.batch-input-title {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
  color: #1d1d1d;
}

.batch-input-item :deep(.el-form-item) {
  margin-bottom: 8px;
}

.batch-input-item :deep(.el-form-item__label) {
  font-size: 12px;
  font-weight: 500;
}

.batch-input-empty {
  text-align: center;
  padding: 20px;
  color: #a1a1a6;
  font-size: 13px;
}

/* 邮件模块：可用变量提示 */
.variable-hint-box {
  background: rgba(52, 199, 89, 0.06);
  border: 1px solid rgba(52, 199, 89, 0.2);
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 16px;
}

.variable-hint-label {
  font-size: 12px;
  color: #34c759;
  font-weight: 600;
  margin-bottom: 8px;
}

.variable-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.variable-tag {
  cursor: pointer;
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 12px;
  transition: all 0.2s ease;
}

.variable-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(52, 199, 89, 0.25);
}

.field-variable-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.field-variable-label {
  font-size: 11px;
  color: #6e6e73;
}

/* 邮件模块折叠面板 */
.email-collapse {
  border: none;
  margin-top: 4px;
}

.email-collapse :deep(.el-collapse-item__header) {
  height: 36px;
  line-height: 36px;
  font-size: 13px;
  font-weight: 600;
  color: #555;
  background: rgba(0, 113, 227, 0.04);
  border-radius: 8px;
  padding: 0 12px;
  border: 1px solid rgba(0, 113, 227, 0.1);
  margin-bottom: 4px;
}

.email-collapse :deep(.el-collapse-item__header:hover) {
  color: #0071e3;
  background: rgba(0, 113, 227, 0.08);
}

.email-collapse :deep(.el-collapse-item__header.is-active) {
  color: #0071e3;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  margin-bottom: 0;
}

.email-collapse :deep(.el-collapse-item__wrap) {
  border: 1px solid rgba(0, 113, 227, 0.1);
  border-top: none;
  border-radius: 0 0 8px 8px;
  margin-bottom: 8px;
  background: rgba(0, 113, 227, 0.02);
}

.email-collapse :deep(.el-collapse-item__content) {
  padding: 12px 8px 4px;
}

.email-collapse :deep(.el-collapse-item__arrow) {
  font-size: 12px;
}

.email-collapse :deep(.el-form-item) {
  margin-bottom: 14px;
}

.email-collapse :deep(.el-form-item__label) {
  font-size: 12px;
}

/* 定时执行：多时间点选择器 */
.schedule-times-picker {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.schedule-times-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 32px;
  align-items: center;
}

.schedule-times-empty {
  font-size: 12px;
  color: #a1a1a6;
}

.schedule-times-add {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 自定义星期选择器 */
.repeat-days-picker {
  width: 100%;
}

.repeat-days-picker :deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 8px;
}

.repeat-days-picker :deep(.el-checkbox) {
  margin-right: 0;
}
</style>
