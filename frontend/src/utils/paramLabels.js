/**
 * 共享参数标签 & 显示值映射
 * FlowEditor 和 PropertyPanel 共用
 */

// 参数名 → 中文标签（静态，不依赖 locate_by）
export const paramLabels = {
  'locate_by': '定位方式',
  'element_tag': '元素类型',
  'browser_type': '浏览器类型',
  'url': '网址',
  'username_selector': '账号选择器',
  'username': '账号',
  'password_selector': '密码选择器',
  'password': '密码',
  'selector': '元素选择器',
  'wait_time': '等待时间(秒)',
  'text': '输入文本',
  'clear_first': '先清空',
  'value': '选项值',
  'select_by': '选择方式',
  'timeout': '超时时间(秒)',
  'var_name': '变量名',
  'filename': '文件名',
  'attribute': '属性名',
  'monitor_mode': '监控模式',
  'close_method': '关闭方式',
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
  'repeat_mode': '重复方式',
  'repeat_days': '执行星期',
  'schedule_times': '执行时间',
  'restart_delay': '重启延迟(秒)',
  'max_retries': '最大重试',
  'auto_start': '开机自启',
  'run_hidden': '后台运行',
  'rdp_mode': '远程桌面模式'
}

// 下拉选项值 → 中文显示文本
const selectDisplayValues = {
  'locate_by': { 'css': 'CSS选择器', 'text': '按文字查找', 'index': '按序号定位' },
  'element_tag': { 'input': '输入框', 'button': '按钮', 'a': '链接', '*': '任意元素' },
  'browser_type': { 'chrome': 'Chrome', 'firefox': 'Firefox', 'edge': 'Edge', 'ie': 'IE' },
  'select_by': { 'value': '按值选择', 'text': '按文本选择' },
  'clear_first': { 'true': '是', 'false': '否', true: '是', false: '否' },
  'monitor_mode': { 'single': '单次关闭', 'continuous': '全程监控' },
  'close_method': { 'auto_detect': '自动检测', 'dom_only': '仅页面弹窗', 'click_element': '点击关闭按钮', 'alert_accept': 'Alert确认', 'alert_dismiss': 'Alert关闭', 'confirm_accept': 'Confirm确认', 'confirm_dismiss': 'Confirm取消' },
  'ignore_error': { 'true': '是', 'false': '否', true: '是', false: '否' },
  'frame_action': { 'by_name': '按名称/ID', 'by_css': '按CSS选择器', 'by_index': '按序号', 'main': '返回主页面', 'parent': '返回上级Frame' },
  'target': { 'active': '当前焦点元素', 'element': '指定元素' },
  'extract_type': { 'text': '文本内容', 'innerHTML': '内部HTML', 'outerHTML': '完整HTML', 'value': '表单值' },
  'action': { 'accept': '确认', 'dismiss': '取消' },
  'smtp_ssl': { 'tls': 'STARTTLS', 'ssl': 'SSL', 'none': '无加密' },
  'auto_start': { 'true': '是', 'false': '否', true: '是', false: '否' },
  'run_hidden': { 'true': '是', 'false': '否', true: '是', false: '否' },
  'rdp_mode': { 'true': '是', 'false': '否', true: '是', false: '否' },
  'repeat_mode': { 'daily': '每天重复', 'weekdays': '工作日', 'weekly': '自定义星期', 'once': '不重复' }
}

/**
 * 获取参数的中文标签（静态版，不随 locate_by 变化）
 */
export function getStaticParamLabel(key) {
  return paramLabels[key] || key
}

/**
 * 获取参数值的友好显示文本
 * 对 select 类型的值翻译为中文，其他原样返回
 */
export function getDisplayValue(key, value) {
  if (value === '' || value === null || value === undefined) return '(未设置)'
  // inputs 数组：显示条目数
  if (key === 'inputs') {
    if (Array.isArray(value)) return `${value.length} 项`
    return '(未设置)'
  }
  const mapping = selectDisplayValues[key]
  if (mapping) {
    return mapping[value] || String(value)
  }
  // 密码类字段打码
  if (key === 'password' || key === 'smtp_pass') return '••••••'
  return String(value)
}
