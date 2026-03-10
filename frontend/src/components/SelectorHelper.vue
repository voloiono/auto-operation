<template>
  <el-dialog
    v-model="visible"
    title="CSS 选择器助手"
    width="640px"
    class="selector-helper-dialog"
    append-to-body
    @close="onClose"
  >
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab">
      <!-- Tab 1: 书签栏工具 -->
      <el-tab-pane label="书签栏工具" name="bookmarklet">
        <div class="helper-section">
          <div class="step">
            <span class="step-num">1</span>
            <span>将下面的按钮 <strong>拖拽到浏览器书签栏</strong>：</span>
          </div>
          <div class="bookmarklet-container">
            <a
              class="bookmarklet-btn"
              :href="bookmarkletCode"
              @click.prevent
              draggable="true"
            >
              选择器拾取器
            </a>
            <span class="drag-hint">拖我到书签栏</span>
          </div>

          <div class="step">
            <span class="step-num">2</span>
            <span>打开目标网页，点击书签栏中的 <strong>"选择器拾取器"</strong></span>
          </div>

          <div class="step">
            <span class="step-num">3</span>
            <span>鼠标悬停在元素上查看选择器，<strong>点击元素</strong> 即可复制</span>
          </div>

          <div class="step">
            <span class="step-num">4</span>
            <span>回到此页面，将复制的选择器 <strong>粘贴</strong> 到参数输入框中</span>
          </div>

          <el-alert type="info" :closable="false" style="margin-top: 16px">
            <template #title>
              <strong>提示：</strong>按 <kbd>Esc</kbd> 可退出拾取模式。拾取器会自动为元素生成最优选择器（优先 #id > .class > 标签路径）。兼容 IE11 / Chrome / Firefox / Edge。
            </template>
          </el-alert>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 在线捕获 -->
      <el-tab-pane label="在线捕获" name="online">
        <div class="helper-section">
          <el-form label-width="80px">
            <el-form-item label="目标网址">
              <el-input
                v-model="targetUrl"
                placeholder="https://www.example.com"
                clearable
              >
                <template #append>
                  <el-button @click="launchPicker" :loading="launching">
                    启动捕获
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-form>

          <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
            <template #title>
              此功能会通过后端 Selenium 打开一个浏览器窗口，注入选择器拾取脚本。点击页面元素后选择器会显示在下方。
            </template>
          </el-alert>

          <div v-if="capturedSelectors.length > 0" class="captured-list">
            <div class="captured-header">
              <strong>已捕获的选择器：</strong>
              <el-button text size="small" @click="capturedSelectors = []">清空</el-button>
            </div>
            <div
              v-for="(item, i) in capturedSelectors"
              :key="i"
              class="captured-item"
              @click="useSelector(item.selector)"
            >
              <span class="captured-tag">{{ item.tag }}</span>
              <code class="captured-selector">{{ item.selector }}</code>
              <span class="captured-text" v-if="item.text">{{ item.text }}</span>
              <el-button text size="small" type="primary">使用</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 3: 下载独立工具 -->
      <el-tab-pane label="下载工具" name="download">
        <div class="helper-section">
          <div class="step">
            <span class="step-num">1</span>
            <span>选择适合你的工具版本：</span>
          </div>

          <div style="margin: 16px 0 24px 36px;">
            <div style="margin-bottom: 16px; padding: 16px; background: rgba(0,113,227,0.04); border: 1px solid rgba(0,113,227,0.15); border-radius: 10px;">
              <div style="font-weight: 600; margin-bottom: 8px; color: #1d1d1d;">HTA 版本（推荐）</div>
              <div style="font-size: 13px; color: #6e6e73; margin-bottom: 10px;">内嵌浏览器，输入网址后直接在页面上点击元素提取选择器。无需额外操作，体验最佳。仅支持 Windows。</div>
              <el-button type="primary" @click="downloadCssExtractorHta">
                下载 HTA 提取工具
              </el-button>
            </div>

            <div style="padding: 16px; background: rgba(0,0,0,0.02); border: 1px solid rgba(0,0,0,0.08); border-radius: 10px;">
              <div style="font-weight: 600; margin-bottom: 8px; color: #1d1d1d;">HTML 版本</div>
              <div style="font-size: 13px; color: #6e6e73; margin-bottom: 10px;">独立 HTML 文件，兼容所有浏览器。需要手动复制提取代码到目标页面地址栏。</div>
              <el-button @click="downloadCssExtractor">
                下载 HTML 提取工具
              </el-button>
            </div>
          </div>

          <div class="step">
            <span class="step-num">2</span>
            <span><strong>HTA 版本：</strong>双击打开 → 输入网址 → 点击"开始提取" → 鼠标选取元素</span>
          </div>

          <div class="step">
            <span class="step-num">3</span>
            <span>点击元素后选择器自动复制到剪贴板，粘贴到参数输入框即可</span>
          </div>

          <el-alert type="success" :closable="false" style="margin-top: 16px">
            <template #title>
              <strong>推荐 HTA 版本：</strong>内嵌浏览器控件，直接在目标页面上悬停和点击元素即可提取选择器，无需任何额外步骤。底部面板实时显示已捕获的选择器列表。
            </template>
          </el-alert>
        </div>
      </el-tab-pane>

      <!-- Tab 3: 手动输入测试 -->
      <el-tab-pane label="选择器速查" name="cheatsheet">
        <div class="helper-section cheatsheet">
          <table class="cheatsheet-table">
            <thead>
              <tr><th>选择器</th><th>说明</th><th>示例</th></tr>
            </thead>
            <tbody>
              <tr><td><code>#id</code></td><td>按 ID 选择</td><td><code>#login-btn</code></td></tr>
              <tr><td><code>.class</code></td><td>按 class 选择</td><td><code>.submit-btn</code></td></tr>
              <tr><td><code>tag</code></td><td>按标签选择</td><td><code>button</code></td></tr>
              <tr><td><code>[attr=val]</code></td><td>按属性选择</td><td><code>input[type="email"]</code></td></tr>
              <tr><td><code>A B</code></td><td>后代选择器</td><td><code>.form input</code></td></tr>
              <tr><td><code>A > B</code></td><td>直接子元素</td><td><code>ul > li</code></td></tr>
              <tr><td><code>A + B</code></td><td>紧邻兄弟</td><td><code>h2 + p</code></td></tr>
              <tr><td><code>:nth-child(n)</code></td><td>第 n 个子元素</td><td><code>tr:nth-child(2)</code></td></tr>
              <tr><td><code>:first-child</code></td><td>第一个子元素</td><td><code>li:first-child</code></td></tr>
              <tr><td><code>:not(sel)</code></td><td>排除选择器</td><td><code>input:not([disabled])</code></td></tr>
            </tbody>
          </table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'select'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const activeTab = ref('bookmarklet')
const targetUrl = ref('')
const launching = ref(false)
const capturedSelectors = ref([])

const onClose = () => {
  emit('update:modelValue', false)
}

// ==================== 书签栏工具代码（支持 iframe） ====================
const pickerScript = `
(function(){
  if(window.__selectorPicker){window.__selectorPicker.destroy();delete window.__selectorPicker;return;}

  /* ---- 顶层 overlay & highlight ---- */
  var overlay=document.createElement('div');
  overlay.id='__sp_overlay';
  overlay.style.cssText='position:fixed;top:0;left:0;right:0;z-index:2147483646;background:rgba(0,113,227,0.95);color:#fff;font:13px/36px -apple-system,sans-serif;text-align:center;padding:0 16px;box-shadow:0 2px 12px rgba(0,0,0,0.3);';
  overlay.innerHTML='<span style="margin-right:8px">选择器拾取器</span>'
    +'<span id="__sp_frame" style="font-family:monospace;background:rgba(255,200,0,0.3);padding:2px 8px;border-radius:4px;font-size:12px;margin-right:6px;display:none;"></span>'
    +'<span id="__sp_display" style="font-family:monospace;background:rgba(255,255,255,0.2);padding:2px 12px;border-radius:4px;font-size:12px;">悬停在元素上...</span>'
    +'<span id="__sp_close" style="position:absolute;right:16px;cursor:pointer;font-size:18px;">&times;</span>';
  document.body.appendChild(overlay);
  var highlight=document.createElement('div');
  highlight.id='__sp_highlight';
  highlight.style.cssText='position:fixed;pointer-events:none;z-index:2147483645;border:2px solid #0071e3;background:rgba(0,113,227,0.1);transition:all 0.05s;display:none;';
  document.body.appendChild(highlight);
  var display=document.getElementById('__sp_display');
  var frameDisplay=document.getElementById('__sp_frame');

  var lastEl=null;
  var lastFrameInfo=null;

  /* ---- 选择器生成 ---- */
  function getSelector(el,doc){
    if(!el||el===doc.body)return'body';
    if(el.id)return'#'+cssEsc(el.id);
    var tag=el.tagName.toLowerCase();
    /* name 属性（表单元素常用） */
    var nm=el.getAttribute('name');
    if(nm){var s=tag+'[name="'+nm+'"]';try{if(doc.querySelectorAll(s).length===1)return s;}catch(e){}}
    /* class */
    var cls=[];
    if(el.classList){for(var i=0;i<el.classList.length;i++){var c=el.classList[i];if(c.indexOf('__sp')!==0)cls.push(c);}}
    if(cls.length){var s2=tag+'.'+cls.map(cssEsc).join('.');try{if(doc.querySelectorAll(s2).length===1)return s2;}catch(e){}}
    /* 递归父级 */
    var parent=el.parentElement;
    if(!parent)return tag;
    var siblings=[];for(var j=0;j<parent.children.length;j++){if(parent.children[j].tagName===el.tagName)siblings.push(parent.children[j]);}
    var idx=siblings.indexOf(el)+1;
    var ps=getSelector(parent,doc);
    if(siblings.length===1)return ps+' > '+tag;
    return ps+' > '+tag+':nth-child('+idx+')';
  }
  function cssEsc(s){if(window.CSS&&CSS.escape)return CSS.escape(s);return s.replace(/([^\\w-])/g,'\\\\$1');}

  /* ---- 获取 iframe 标识（name 或 id 或 index） ---- */
  function getFrameId(iframe){
    if(iframe.name)return iframe.name;
    if(iframe.id)return iframe.id;
    var frames=iframe.ownerDocument.querySelectorAll('iframe,frame');
    for(var i=0;i<frames.length;i++){if(frames[i]===iframe)return'index:'+i;}
    return'unknown';
  }

  /* ---- 计算 iframe 内元素相对于顶层窗口的位置 ---- */
  function getAbsoluteRect(el,win){
    var r=el.getBoundingClientRect();
    var t=r.top,l=r.left,w=r.width,h=r.height;
    var cur=win;
    while(cur!==window.top){
      try{
        var fe=cur.frameElement;
        if(!fe)break;
        var fr=fe.getBoundingClientRect();
        t+=fr.top;l+=fr.left;
        cur=cur.parent;
      }catch(e){break;}
    }
    return{top:t,left:l,width:w,height:h};
  }

  /* ---- 在单个 document 上绑定事件 ---- */
  var cleanups=[];
  function attach(doc,win,frameChain){
    function onMove(e){
      var t=e.target;
      if(t.id&&t.id.indexOf('__sp')===0)return;
      if(t.tagName==='IFRAME'||t.tagName==='FRAME')return;
      lastEl=t;
      lastFrameInfo=frameChain.length?frameChain:null;
      var ar=getAbsoluteRect(t,win);
      highlight.style.display='block';
      highlight.style.left=ar.left+'px';
      highlight.style.top=ar.top+'px';
      highlight.style.width=ar.width+'px';
      highlight.style.height=ar.height+'px';
      var sel=getSelector(t,doc);
      display.textContent=sel;
      if(frameChain.length){
        frameDisplay.style.display='inline';
        frameDisplay.textContent='Frame: '+frameChain.join(' > ');
      }else{
        frameDisplay.style.display='none';
      }
    }
    function onClick(e){
      e.preventDefault();e.stopPropagation();
      if(!lastEl)return;
      var sel=getSelector(lastEl,lastEl.ownerDocument);
      var copyText=sel;
      if(lastFrameInfo&&lastFrameInfo.length){
        copyText='[Frame: '+lastFrameInfo.join(' > ')+'] '+sel;
      }
      try{
        if(window.top.navigator.clipboard){
          window.top.navigator.clipboard.writeText(copyText).then(function(){display.textContent='已复制: '+sel;});
        }else{
          display.textContent=sel;window.top.prompt('复制选择器:',copyText);
        }
      }catch(ex){display.textContent=sel;window.top.prompt('复制选择器:',copyText);}
    }
    doc.addEventListener('mousemove',onMove,true);
    doc.addEventListener('click',onClick,true);
    cleanups.push(function(){
      doc.removeEventListener('mousemove',onMove,true);
      doc.removeEventListener('click',onClick,true);
    });
  }

  /* ---- 递归注入所有同源 iframe / frame ---- */
  function injectFrames(doc,win,frameChain){
    attach(doc,win,frameChain);
    var frames=doc.querySelectorAll('iframe,frame');
    for(var i=0;i<frames.length;i++){
      try{
        var fd=frames[i].contentDocument||frames[i].contentWindow.document;
        var fw=frames[i].contentWindow;
        if(fd){
          var fid=getFrameId(frames[i]);
          injectFrames(fd,fw,frameChain.concat(fid));
        }
      }catch(e){/* 跨域 iframe 无法访问，跳过 */}
    }
  }

  /* ---- 全局键盘 & 销毁 ---- */
  function onKey(e){if(e.key==='Escape')destroy();}
  document.addEventListener('keydown',onKey,true);

  function destroy(){
    for(var i=0;i<cleanups.length;i++)cleanups[i]();
    cleanups=[];
    document.removeEventListener('keydown',onKey,true);
    var o=document.getElementById('__sp_overlay');if(o)o.remove();
    var h=document.getElementById('__sp_highlight');if(h)h.remove();
    delete window.__selectorPicker;
  }
  document.getElementById('__sp_close').addEventListener('click',destroy);
  window.__selectorPicker={destroy:destroy};

  /* ---- 启动：注入顶层 + 所有 iframe ---- */
  injectFrames(document,window,[]);
})();
`.replace(/\n/g, '').replace(/\s{2,}/g, ' ')

const bookmarkletCode = computed(() => 'javascript:' + encodeURIComponent(pickerScript))

// ==================== 在线捕获 ====================
const launchPicker = async () => {
  if (!targetUrl.value) {
    ElMessage.warning('请输入目标网址')
    return
  }
  launching.value = true
  try {
    const resp = await fetch('/api/selector-helper/launch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ url: targetUrl.value })
    })
    if (!resp.ok) throw new Error('启动失败')
    const data = await resp.json()
    capturedSelectors.value = data.selectors || []
    ElMessage.success('浏览器已启动，请在弹出的窗口中操作')
  } catch (e) {
    ElMessage.info('在线捕获需要后端支持，请先使用书签栏工具方式')
  } finally {
    launching.value = false
  }
}

const useSelector = (selector) => {
  emit('select', selector)
  visible.value = false
  ElMessage.success('选择器已应用')
}

// ==================== 下载CSS提取工具 ====================
const downloadCssExtractor = () => {
  window.location.href = '/api/tools/css-extractor'
}

const downloadCssExtractorHta = () => {
  window.location.href = '/api/tools/css-extractor-hta'
}
</script>

<style scoped>
.helper-section {
  padding: 8px 0;
}

.step {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #1d1d1d;
  line-height: 1.6;
}

.step-num {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  background: linear-gradient(135deg, #0071e3, #0077ed);
  color: white;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.bookmarklet-container {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 20px 36px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(0,113,227,0.05), rgba(0,113,227,0.1));
  border-radius: 12px;
  border: 1px dashed rgba(0,113,227,0.3);
}

.bookmarklet-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 20px;
  background: linear-gradient(135deg, #0071e3, #005bb5);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: grab;
  box-shadow: 0 2px 8px rgba(0,113,227,0.3);
  transition: transform 0.2s, box-shadow 0.2s;
  user-select: none;
}

.bookmarklet-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0,113,227,0.4);
}

.bookmarklet-btn:active {
  cursor: grabbing;
}

.drag-hint {
  font-size: 12px;
  color: #6e6e73;
  animation: pulse-hint 2s ease-in-out infinite;
}

@keyframes pulse-hint {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

kbd {
  display: inline-block;
  padding: 2px 6px;
  background: #f0f0f0;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

/* 已捕获列表 */
.captured-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.captured-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(0,113,227,0.04);
  border: 1px solid rgba(0,113,227,0.15);
  border-radius: 8px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.captured-item:hover {
  background: rgba(0,113,227,0.08);
  border-color: #0071e3;
}

.captured-tag {
  background: #0071e3;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
}

.captured-selector {
  flex: 1;
  font-size: 13px;
  color: #1d1d1d;
}

.captured-text {
  font-size: 11px;
  color: #6e6e73;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 速查表 */
.cheatsheet-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.cheatsheet-table th {
  text-align: left;
  padding: 10px 12px;
  background: rgba(0,113,227,0.06);
  color: #1d1d1d;
  font-weight: 600;
  border-bottom: 2px solid rgba(0,113,227,0.15);
}

.cheatsheet-table td {
  padding: 8px 12px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
  color: #333;
}

.cheatsheet-table code {
  background: rgba(0,113,227,0.08);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  color: #0071e3;
}
</style>
