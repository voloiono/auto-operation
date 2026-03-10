package com.example.autooperation.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;

@RestController
@RequestMapping("/api/tools")
@CrossOrigin(origins = "*")
public class ToolsController {

    /**
     * 下载浏览器版本检测工具
     */
    @GetMapping("/detect-browser")
    public ResponseEntity<byte[]> downloadDetectTool() {
        String script = generateDetectScript();

        // 使用 GBK 编码，Windows 批处理默认编码
        byte[] content = script.getBytes(Charset.forName("GBK"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"detect_browser_version.bat\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    /**
     * 下载 CSS 选择器提取工具（独立 HTML 文件）
     */
    @GetMapping("/css-extractor")
    public ResponseEntity<byte[]> downloadCssExtractor() {
        String html = generateCssExtractorHtml();
        byte[] content = html.getBytes(Charset.forName("UTF-8"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"css_selector_extractor.html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }

    /**
     * 下载 CSS 选择器提取工具（HTA 版本，内嵌浏览器，可直接在页面中选取元素）
     */
    @GetMapping("/css-extractor-hta")
    public ResponseEntity<byte[]> downloadCssExtractorHta() {
        String hta = generateCssExtractorHta();
        byte[] content = hta.getBytes(Charset.forName("UTF-8"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"css_selector_picker.hta\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    private String generateCssExtractorHtml() {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CSS 选择器提取工具</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { height: 100%; }
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background: #667eea url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:%23667eea"/><stop offset="100%" style="stop-color:%23764ba2"/></linearGradient></defs><rect width="100" height="100" fill="url(%23grad)"/></svg>');
            background-size: cover;
            min-height: 100vh;
            padding: 20px;
            text-align: center;
            vertical-align: middle;
        }
        .container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            max-width: 600px;
            width: 100%;
            padding: 40px;
            margin: 50px auto;
        }
        h1 { color: #333; margin-bottom: 10px; font-size: 28px; }
        .subtitle { color: #666; margin-bottom: 30px; font-size: 14px; }
        .status {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            display: none;
        }
        .status.active {
            display: block;
            background: #e3f2fd;
            color: #1976d2;
            border-left: 4px solid #1976d2;
        }
        .status.success {
            display: block;
            background: #e8f5e9;
            color: #388e3c;
            border-left-color: #388e3c;
        }
        .button-group {
            margin-bottom: 20px;
            overflow: auto;
        }
        .button-group button {
            width: 48%;
            margin-right: 2%;
            float: left;
        }
        .button-group button:last-child {
            margin-right: 0;
        }
        button {
            padding: 12px 20px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: background-color 0.3s ease;
        }
        .btn-start {
            background: #667eea;
            color: white;
        }
        .btn-start:hover {
            background: #5568d3;
        }
        .btn-start:disabled {
            background: #ccc;
            cursor: not-allowed;
        }
        .btn-stop {
            background: #f44336;
            color: white;
        }
        .btn-stop:hover {
            background: #da190b;
        }
        .btn-copy {
            background: #4caf50;
            color: white;
            font-size: 12px;
            width: 60px;
            padding: 6px 12px;
        }
        .btn-copy:hover {
            background: #45a049;
        }
        .selectors-list {
            background: #f5f5f5;
            border-radius: 8px;
            padding: 15px;
            max-height: 300px;
            overflow-y: auto;
            margin-bottom: 20px;
        }
        .selector-item {
            background: white;
            padding: 10px 12px;
            margin-bottom: 8px;
            border-radius: 6px;
            font-size: 13px;
            border-left: 3px solid #667eea;
            overflow: auto;
        }
        .selector-item code {
            background: #f0f0f0;
            padding: 6px 8px;
            border-radius: 4px;
            font-family: 'Courier New', monospace;
            word-break: break-all;
            display: block;
            margin-bottom: 8px;
            margin-right: 0;
        }
        .selector-item button {
            float: right;
        }
        .instructions {
            background: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 15px;
            border-radius: 6px;
            font-size: 13px;
            line-height: 1.6;
            color: #856404;
            text-align: left;
        }
        .instructions strong {
            display: block;
            margin-bottom: 8px;
        }
        .instructions ol {
            margin-left: 20px;
        }
        .instructions li {
            margin-bottom: 6px;
        }
        .clear { clear: both; }
    </style>
</head>
<body>
    <div class="container">
        <h1>CSS 选择器提取工具</h1>
        <p class="subtitle">用于远程桌面上快速提取网页元素的 CSS 选择器</p>

        <div id="status" class="status"></div>

        <div class="button-group">
            <button class="btn-start" id="startBtn" type="button" onclick="return startPicker();">在当前页面提取</button>
            <button class="btn-stop" id="stopBtn" type="button" onclick="return stopPicker();" style="display:none;">停止提取</button>
            <div class="clear"></div>
        </div>

        <div style="margin-bottom: 20px; padding: 15px; background: #f0f7ff; border-radius: 8px; border: 1px solid #c8ddf5;">
            <div style="margin-bottom: 10px; font-size: 14px; color: #333;">
                <strong>在其他页面上提取 CSS 选择器：</strong>
            </div>

            <div style="margin-bottom: 12px; font-size: 13px; color: #555; line-height: 1.8;">
                <strong>步骤1：</strong>点击下方按钮复制提取代码<br>
                <strong>步骤2：</strong>打开目标网页<br>
                <strong>步骤3：</strong>在目标网页地址栏中粘贴并按回车<br>
                <strong>步骤4：</strong>鼠标悬停在元素上查看选择器，点击元素复制
            </div>

            <div style="margin-bottom: 12px;">
                <button type="button" id="copyScriptBtn" style="width: 100%; padding: 12px 20px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 600;">复制提取代码到剪贴板</button>
            </div>

            <div style="overflow: auto;">
                <input type="text" id="targetUrl" placeholder="输入目标网址后点击打开（可选）" style="width: 70%; float: left; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; box-sizing: border-box;">
                <button type="button" id="openTargetBtn" style="width: 28%; float: right; padding: 8px 16px; background: #4caf50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; font-weight: 600; box-sizing: border-box;">复制并打开网页</button>
            </div>
            <div style="clear: both;"></div>

            <div style="margin-top: 12px; padding: 10px; background: #fff; border: 1px solid #e0e0e0; border-radius: 4px;">
                <div style="font-size: 12px; color: #888; margin-bottom: 4px;">提取代码（也可手动全选复制）：</div>
                <textarea id="scriptArea" readonly style="width: 100%; height: 50px; font-family: Courier New, monospace; font-size: 11px; border: none; background: #f8f8f8; resize: vertical; box-sizing: border-box; color: #333;"></textarea>
            </div>
        </div>

        <div class="selectors-list" id="selectorsList" style="display:none;">
            <div id="selectorsContent"></div>
        </div>

        <div class="instructions">
            <strong>使用说明：</strong>
            <ol>
                <li>点击 <strong>"启动提取模式"</strong> 按钮</li>
                <li>鼠标悬停在网页元素上，会显示该元素的 CSS 选择器</li>
                <li>点击元素即可复制选择器到剪贴板</li>
                <li>按 <strong>Esc</strong> 键或点击 <strong>"停止提取"</strong> 退出模式</li>
                <li>将复制的选择器粘贴到自动化配置中</li>
            </ol>
        </div>
    </div>

    <script>
        // ============ 全局变量 ============
        var isPickerActive = false;
        var selectedElements = [];
        var highlightedElement = null;

        // ============ 所有函数必须在最前面定义 ============

        function startPicker() {
            try {
                isPickerActive = true;
                selectedElements = [];
                var startBtn = document.getElementById('startBtn');
                var stopBtn = document.getElementById('stopBtn');
                var selectorsList = document.getElementById('selectorsList');
                var selectorsContent = document.getElementById('selectorsContent');

                if (startBtn) startBtn.style.display = 'none';
                if (stopBtn) stopBtn.style.display = 'inline-block';
                if (selectorsList) selectorsList.style.display = 'none';
                if (selectorsContent) selectorsContent.innerHTML = '';

                showStatus('提取模式已启动，请在网页上悬停和点击元素', 'active');

                document.addEventListener('mouseover', onMouseOver, false);
                document.addEventListener('click', onClick, true);
                document.addEventListener('keydown', onKeyDown, false);
            } catch(e) {
                alert('启动提取模式失败: ' + e.message);
            }
            return false;
        }

        function stopPicker() {
            try {
                isPickerActive = false;
                var startBtn = document.getElementById('startBtn');
                var stopBtn = document.getElementById('stopBtn');

                if (startBtn) startBtn.style.display = 'inline-block';
                if (stopBtn) stopBtn.style.display = 'none';

                if (highlightedElement) {
                    highlightedElement.style.outline = '';
                    highlightedElement = null;
                }

                document.removeEventListener('mouseover', onMouseOver, false);
                document.removeEventListener('click', onClick, true);
                document.removeEventListener('keydown', onKeyDown, false);

                showStatus('提取模式已停止', 'active');
            } catch(e) {
                alert('停止提取模式失败: ' + e.message);
            }
            return false;
        }

        function openTargetPage() {
            var url = document.getElementById('targetUrl').value;
            if (!url) {
                alert('请输入目标网址');
                return false;
            }

            // 确保URL有协议
            if (url.indexOf('http://') !== 0 && url.indexOf('https://') !== 0) {
                url = 'https://' + url;
            }

            // 先复制提取代码到剪贴板
            copyPickerToClipboard();

            // 打开目标页面
            window.open(url, '_blank');

            return false;
        }

        function copyPickerToClipboard() {
            var code = getPickerScript();
            // 直接复制，不通过 copyToClipboard 避免显示过长内容
            if (window.clipboardData && window.clipboardData.setData) {
                window.clipboardData.setData('Text', code);
            } else if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(code);
            } else {
                fallbackCopy(code);
            }
            showStatus('提取代码已复制！请到目标页面地址栏粘贴并按回车', 'success');
            return false;
        }

        function onMouseOver(e) {
            if (!isPickerActive) return;

            var target = e.target || e.srcElement;
            if (!target) return;

            if (highlightedElement) {
                highlightedElement.style.outline = '';
            }

            highlightedElement = target;
            try {
                highlightedElement.style.outline = '2px solid #667eea';
            } catch(e) {
                // 某些元素可能不允许修改outline
            }
        }

        function onClick(e) {
            if (!isPickerActive) return;

            try {
                if (e.preventDefault) {
                    e.preventDefault();
                }
                if (e.stopPropagation) {
                    e.stopPropagation();
                }
            } catch(e) {
                // IE中可能不支持这些方法
            }

            var target = e.target || e.srcElement;
            if (!target) return;

            var selector = generateSelector(target);
            selectedElements.push(selector);
            updateSelectorsList();
            showStatus('已捕获: ' + selector, 'success');
        }

        function onKeyDown(e) {
            var key = e.key || String.fromCharCode(e.keyCode);
            if ((key === 'Escape' || e.keyCode === 27) && isPickerActive) {
                stopPicker();
            }
        }

        function generateSelector(element) {
            if (element.id) {
                return '#' + element.id;
            }

            if (element.className) {
                var classes = element.className.split(' ');
                var filtered = [];
                for (var i = 0; i < classes.length; i++) {
                    if (classes[i]) {
                        filtered.push(classes[i]);
                    }
                }
                return element.tagName.toLowerCase() + '.' + filtered.join('.');
            }

            var path = [];
            while (element && element !== document.body) {
                var selector = element.tagName.toLowerCase();
                if (element.id) {
                    selector += '#' + element.id;
                    path.unshift(selector);
                    break;
                } else {
                    var siblings = element.parentNode.querySelectorAll(selector);
                    if (siblings.length > 1) {
                        var index = 0;
                        for (var i = 0; i < siblings.length; i++) {
                            if (siblings[i] === element) {
                                index = i + 1;
                                break;
                            }
                        }
                        selector += ':nth-of-type(' + index + ')';
                    }
                    path.unshift(selector);
                }
                element = element.parentNode;
            }

            return path.join(' > ');
        }

        function updateSelectorsList() {
            var content = document.getElementById('selectorsContent');
            var html = '';
            for (var i = 0; i < selectedElements.length; i++) {
                html += '<div class="selector-item"><code>' + escapeHtml(selectedElements[i]) + '</code><button class="btn-copy" type="button" onclick="return copySelectorByIndex(' + i + ');">复制</button><div class="clear"></div></div>';
            }
            content.innerHTML = html;
            document.getElementById('selectorsList').style.display = 'block';
        }

        function copySelectorByIndex(index) {
            return copyToClipboard(selectedElements[index]);
        }

        function escapeHtml(text) {
            var map = {
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#039;'
            };
            return text.replace(/[&<>"']/g, function(c) { return map[c]; });
        }

        function copyToClipboard(text) {
            if (window.clipboardData && window.clipboardData.setData) {
                // IE 10-11
                window.clipboardData.setData('Text', text);
                showStatus('已复制到剪贴板: ' + text, 'success');
            } else if (navigator.clipboard && navigator.clipboard.writeText) {
                // 现代浏览器
                navigator.clipboard.writeText(text).then(function() {
                    showStatus('已复制到剪贴板: ' + text, 'success');
                }).catch(function() {
                    fallbackCopy(text);
                });
            } else {
                // 备选方案
                fallbackCopy(text);
            }
            return false;
        }

        function fallbackCopy(text) {
            var textArea = document.createElement('textarea');
            textArea.value = text;
            textArea.style.position = 'fixed';
            textArea.style.left = '-999999px';
            textArea.style.top = '-999999px';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();
            try {
                document.execCommand('copy');
                showStatus('已复制到剪贴板: ' + text, 'success');
            } catch (err) {
                alert('复制失败，请手动复制: ' + text);
            }
            document.body.removeChild(textArea);
        }

        function showStatus(message, type) {
            var status = document.getElementById('status');
            if (status) {
                status.textContent = message;
                status.className = 'status ' + type;
            }
        }

        function getPickerScript() {
            var s = 'javascript:';
            s += 'void(function(){';
            s += 'if(window.__selectorPicker){window.__selectorPicker.destroy();delete window.__selectorPicker;return;}';
            s += 'var overlay=document.createElement("div");';
            s += 'overlay.id="__sp_overlay";';
            s += 'overlay.style.cssText="position:fixed;top:0;left:0;right:0;z-index:2147483646;background:rgba(0,113,227,0.95);color:#fff;font:14px/40px -apple-system,sans-serif;text-align:center;padding:0 16px;box-shadow:0 2px 12px rgba(0,0,0,0.3);";';
            s += 'var sp1=document.createElement("span");sp1.style.marginRight="12px";sp1.appendChild(document.createTextNode("CSS选择器提取"));overlay.appendChild(sp1);';
            s += 'var sp2=document.createElement("span");sp2.id="__sp_display";sp2.style.cssText="font-family:monospace;background:rgba(255,255,255,0.2);padding:2px 12px;border-radius:4px;font-size:13px;";sp2.appendChild(document.createTextNode("悬停在元素上..."));overlay.appendChild(sp2);';
            s += 'var sp3=document.createElement("span");sp3.id="__sp_close";sp3.style.cssText="position:absolute;right:16px;cursor:pointer;font-size:18px;";sp3.innerHTML="&times;";overlay.appendChild(sp3);';
            s += 'document.body.appendChild(overlay);';
            s += 'var highlight=document.createElement("div");';
            s += 'highlight.id="__sp_highlight";';
            s += 'highlight.style.cssText="position:fixed;pointer-events:none;z-index:2147483645;border:2px solid #0071e3;background:rgba(0,113,227,0.1);transition:all 0.1s;display:none;";';
            s += 'document.body.appendChild(highlight);';
            s += 'var display=document.getElementById("__sp_display");';
            s += 'var lastEl=null;';
            s += 'function getSelector(el){';
            s += '  if(el.id)return"#"+el.id;';
            s += '  if(el===document.body)return"body";';
            s += '  var parent=el.parentElement;';
            s += '  if(!parent)return el.tagName.toLowerCase();';
            s += '  var tag=el.tagName.toLowerCase();';
            s += '  if(el.className){var cls=el.className.split(" ");var f=[];for(var i=0;i<cls.length;i++){if(cls[i]&&cls[i].indexOf("__sp")!==0)f.push(cls[i]);}if(f.length){var sel=tag+"."+f.join(".");try{if(document.querySelectorAll(sel).length===1)return sel;}catch(e){}}}';
            s += '  var siblings=parent.children;var sameTag=[];for(var i=0;i<siblings.length;i++){if(siblings[i].tagName===el.tagName)sameTag.push(siblings[i]);}';
            s += '  var idx=0;for(var i=0;i<sameTag.length;i++){if(sameTag[i]===el){idx=i+1;break;}}';
            s += '  var parentSel=getSelector(parent);';
            s += '  if(sameTag.length===1)return parentSel+" > "+tag;';
            s += '  return parentSel+" > "+tag+":nth-child("+idx+")";';
            s += '}';
            s += 'function onMove(e){';
            s += '  var t=e.target||e.srcElement;';
            s += '  if(t.id&&t.id.indexOf("__sp")===0)return;';
            s += '  lastEl=t;';
            s += '  var r=t.getBoundingClientRect();';
            s += '  highlight.style.display="block";';
            s += '  highlight.style.left=r.left+"px";';
            s += '  highlight.style.top=r.top+"px";';
            s += '  highlight.style.width=r.width+"px";';
            s += '  highlight.style.height=r.height+"px";';
            s += '  display.textContent=getSelector(t);';
            s += '}';
            s += 'function onClick(e){';
            s += '  if(e.preventDefault)e.preventDefault();if(e.stopPropagation)e.stopPropagation();';
            s += '  if(!lastEl)return;';
            s += '  var sel=getSelector(lastEl);';
            s += '  if(window.clipboardData){window.clipboardData.setData("Text",sel);display.textContent="已复制: "+sel;}';
            s += '  else if(navigator.clipboard){navigator.clipboard.writeText(sel).then(function(){display.textContent="已复制: "+sel;});}';
            s += '  else{display.textContent=sel;window.prompt("复制选择器:",sel);}';
            s += '}';
            s += 'function onKey(e){if(e.key==="Escape"||e.keyCode===27)destroy();}';
            s += 'function destroy(){';
            s += '  document.removeEventListener("mousemove",onMove,true);';
            s += '  document.removeEventListener("click",onClick,true);';
            s += '  document.removeEventListener("keydown",onKey,true);';
            s += '  var o=document.getElementById("__sp_overlay");if(o&&o.parentNode)o.parentNode.removeChild(o);';
            s += '  var h=document.getElementById("__sp_highlight");if(h&&h.parentNode)h.parentNode.removeChild(h);';
            s += '  delete window.__selectorPicker;';
            s += '}';
            s += 'document.addEventListener("mousemove",onMove,true);';
            s += 'document.addEventListener("click",onClick,true);';
            s += 'document.addEventListener("keydown",onKey,true);';
            s += 'document.getElementById("__sp_close").addEventListener("click",destroy);';
            s += 'window.__selectorPicker={destroy:destroy};';
            s += '})());';
            return s;
        }

        // 页面加载完毕后绑定事件处理器
        function initializePageEvents() {
            var openTargetBtn = document.getElementById('openTargetBtn');
            if (openTargetBtn) {
                openTargetBtn.onclick = function() {
                    return openTargetPage();
                };
            }
            var copyScriptBtn = document.getElementById('copyScriptBtn');
            if (copyScriptBtn) {
                copyScriptBtn.onclick = function() {
                    return copyPickerToClipboard();
                };
            }
            // 填充提取代码到textarea
            var scriptArea = document.getElementById('scriptArea');
            if (scriptArea) {
                scriptArea.value = getPickerScript();
            }
        }

        // IE 兼容：确保函数在任何时间点都被执行
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initializePageEvents, false);
        } else {
            // 页面已加载
            initializePageEvents();
        }
    </script>
</body>
</html>
""";
    }

    private String generateCssExtractorHta() {
        return """
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>CSS 选择器提取工具</title>
<HTA:APPLICATION ID="cssPicker" APPLICATIONNAME="CssSelectorPicker" BORDER="thick" SCROLL="no" SINGLEINSTANCE="yes" SHOWINTASKBAR="yes" />
<style>
html,body{margin:0;padding:0;width:100%;height:100%;overflow:hidden;font-family:Segoe UI,Arial,sans-serif;}
#toolbar{height:40px;background:#f0f0f0;border-bottom:1px solid #ccc;padding:6px 10px;}
#urlBar{width:62%;height:26px;padding:2px 8px;border:1px solid #aaa;font-size:13px;vertical-align:top;}
.tb{height:26px;padding:0 12px;border:none;color:#fff;font-size:12px;font-weight:bold;cursor:pointer;vertical-align:top;}
#btnGo{background:#4a7cf6;}
#btnPick{background:#4caf50;}
#statusBar{height:24px;line-height:24px;padding:0 10px;font-size:12px;color:#1b5e20;background:#e8f5e9;border-bottom:1px solid #c8e6c9;display:none;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;}
#browserFrame{position:absolute;left:0;right:0;border:none;width:100%;}
#resultPanel{position:absolute;left:0;right:0;bottom:0;height:120px;background:#fafafa;border-top:2px solid #4a7cf6;overflow-y:auto;padding:6px 10px;font-size:12px;}
#resultPanel .hdr{font-weight:bold;margin-bottom:4px;color:#333;}
#resultPanel .hdr button{float:right;font-size:11px;padding:1px 8px;cursor:pointer;}
.ri{padding:3px 6px;margin-bottom:3px;background:#fff;border:1px solid #ddd;border-left:3px solid #4a7cf6;cursor:pointer;}
.ri:hover{background:#f0f7ff;}
.ri code{font-family:Consolas,Courier New,monospace;font-size:12px;color:#222;}
.ri .cb{float:right;font-size:11px;padding:1px 8px;background:#4caf50;color:#fff;border:none;cursor:pointer;}
</style>
</head>
<body>
<div id="toolbar">
  <input type="text" id="urlBar" value="https://www.baidu.com" />
  <button class="tb" id="btnGo" onclick="doGo()">前往</button>
  <button class="tb" id="btnPick" onclick="doPick()">开始提取</button>
</div>
<div id="statusBar"></div>
<iframe id="browserFrame" src="about:blank"></iframe>
<div id="resultPanel">
  <div class="hdr"><span>已捕获的选择器</span><button onclick="doClear()">清空</button></div>
  <div id="resultList"></div>
</div>
<script language="javascript">
var fr=document.getElementById('browserFrame');
var picking=false, lastHL=null, captured=[];

window.resizeTo(1050,720);

window.onload=function(){doLayout();};
window.onresize=function(){doLayout();};

document.getElementById('urlBar').onkeydown=function(){if(window.event.keyCode===13)doGo();};

function doLayout(){
  var tH=40, sEl=document.getElementById('statusBar');
  var sH=(sEl.style.display==='none')?0:24;
  var rH=120, top=tH+sH;
  fr.style.top=top+'px';
  fr.style.bottom=rH+'px';
  fr.style.height=(document.body.clientHeight-top-rH)+'px';
}

function doGo(){
  var url=document.getElementById('urlBar').value;
  if(!url)return;
  if(url.indexOf('://')<0)url='http://'+url;
  document.getElementById('urlBar').value=url;
  // 停止之前的提取
  if(picking)doPick();
  fr.src=url;
}

function doPick(){
  if(picking){doStop();return;}
  var doc=getDoc();
  if(!doc||!doc.body){alert('请先输入网址并点击前往加载页面');return;}
  picking=true;
  document.getElementById('btnPick').innerText='停止提取';
  document.getElementById('btnPick').style.background='#e53935';
  showSt('提取模式已启动 - 在下方页面中悬停查看选择器，点击元素捕获，按Esc退出');
  doc.onmouseover=hOver;
  doc.onclick=hClick;
  doc.onkeydown=hKey;
}

function doStop(){
  picking=false;
  document.getElementById('btnPick').innerText='开始提取';
  document.getElementById('btnPick').style.background='#4caf50';
  hideSt();
  clrHL();
  var doc=getDoc();
  if(doc){doc.onmouseover=null;doc.onclick=null;doc.onkeydown=null;}
}

function getDoc(){try{return fr.contentWindow.document;}catch(e){return null;}}

function clrHL(){if(lastHL){try{lastHL.style.outline='';lastHL.style.outlineOffset='';}catch(e){}lastHL=null;}}

function hOver(){
  if(!picking)return;
  try{
    var doc=getDoc();var el=doc.parentWindow.event.srcElement;
    if(!el||!el.tagName)return;
    clrHL();lastHL=el;
    el.style.outline='2px solid #4a7cf6';
    showSt(mkSel(el)+'    <'+el.tagName.toLowerCase()+'>    点击捕获');
  }catch(e){}
}

function hClick(){
  if(!picking)return;
  try{
    var doc=getDoc();var ev=doc.parentWindow.event;
    ev.cancelBubble=true;ev.returnValue=false;
    var el=ev.srcElement;if(!el||!el.tagName)return;
    var sel=mkSel(el);
    var txt='';try{txt=(el.innerText||'').replace(/[\\r\\n]+/g,' ');if(txt.length>40)txt=txt.substring(0,40)+'...';}catch(e){}
    captured.push({s:sel,t:el.tagName.toLowerCase(),x:txt});
    render();
    showSt('已捕获并复制: '+sel);
    try{window.clipboardData.setData('Text',sel);}catch(e){}
  }catch(e){}
}

function hKey(){try{var ev=getDoc().parentWindow.event;if(ev.keyCode===27)doStop();}catch(e){}}

function mkSel(el){
  try{
    if(el.id)return'#'+el.id;
    if(el.className&&typeof el.className==='string'){
      var c=el.className.replace(/\\s+/g,' ').split(' ');
      var f=[];for(var i=0;i<c.length;i++)if(c[i])f.push(c[i]);
      if(f.length)return el.tagName.toLowerCase()+'.'+f.join('.');
    }
    var p=[],cur=el;
    while(cur&&cur.tagName){
      var tn=cur.tagName.toUpperCase();
      if(tn==='HTML'||tn==='BODY')break;
      var tag=cur.tagName.toLowerCase();
      if(cur.id){p.unshift('#'+cur.id);break;}
      var par=cur.parentElement;
      if(par){var ch=par.children,sm=[];for(var j=0;j<ch.length;j++)if(ch[j].tagName===cur.tagName)sm.push(ch[j]);
        if(sm.length>1){for(var k=0;k<sm.length;k++)if(sm[k]===cur){tag+=':nth-child('+(k+1)+')';break;}}
      }
      p.unshift(tag);cur=par;
    }
    return p.join(' > ')||el.tagName.toLowerCase();
  }catch(e){return'?';}
}

function render(){
  var h='';
  for(var i=captured.length-1;i>=0;i--){
    var c=captured[i];
    h+='<div class="ri" onclick="cpIdx('+i+')"><code>'+esc(c.s)+'</code>';
    h+=' <span style="color:#888;font-size:11px">&lt;'+esc(c.t)+'&gt;</span>';
    if(c.x)h+=' <span style="color:#aaa;font-size:11px">'+esc(c.x)+'</span>';
    h+='<button class="cb" onclick="cpIdx('+i+')">复制</button><div style="clear:both"></div></div>';
  }
  document.getElementById('resultList').innerHTML=h;
}

function cpIdx(i){
  try{window.clipboardData.setData('Text',captured[i].s);showSt('已复制: '+captured[i].s);}catch(e){alert(captured[i].s);}
}

function doClear(){captured=[];document.getElementById('resultList').innerHTML='';}

function esc(s){return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');}

function showSt(m){var b=document.getElementById('statusBar');b.innerText=m;b.style.display='block';doLayout();}
function hideSt(){document.getElementById('statusBar').style.display='none';doLayout();}
</script>
</body>
</html>
""";
    }

    private String generateDetectScript() {
        // 使用简单的英文+数字，避免编码问题
        return """
@echo off
echo ========================================
echo    Browser Version Detection Tool
echo ========================================
echo.

echo [Edge Browser]
for %%p in (
    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe"
    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe"
) do (
    if exist %%p (
        echo   Path: %%~p
        for /f "tokens=*" %%v in ('powershell -command "(Get-Item '%%~p').VersionInfo.FileVersion"') do (
            echo   Full Version: %%v
            for /f "tokens=1 delims=." %%m in ("%%v") do (
                echo   *** Major Version: %%m ***
            )
        )
        goto :edge_done
    )
)
echo   Not installed
:edge_done
echo.

echo [Chrome Browser]
for %%p in (
    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"
    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
) do (
    if exist %%p (
        echo   Path: %%~p
        for /f "tokens=*" %%v in ('powershell -command "(Get-Item '%%~p').VersionInfo.FileVersion"') do (
            echo   Full Version: %%v
            for /f "tokens=1 delims=." %%m in ("%%v") do (
                echo   *** Major Version: %%m ***
            )
        )
        goto :chrome_done
    )
)
echo   Not installed
:chrome_done
echo.

echo [Firefox Browser]
for %%p in (
    "C:\\Program Files\\Mozilla Firefox\\firefox.exe"
    "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe"
) do (
    if exist %%p (
        echo   Path: %%~p
        for /f "tokens=*" %%v in ('powershell -command "(Get-Item '%%~p').VersionInfo.FileVersion"') do (
            echo   Full Version: %%v
            for /f "tokens=1 delims=." %%m in ("%%v") do (
                echo   *** Major Version: %%m ***
            )
        )
        goto :firefox_done
    )
)
echo   Not installed
:firefox_done
echo.

echo [Internet Explorer]
for %%p in (
    "C:\\Program Files\\Internet Explorer\\iexplore.exe"
    "C:\\Program Files (x86)\\Internet Explorer\\iexplore.exe"
) do (
    if exist %%p (
        echo   Path: %%~p
        for /f "tokens=*" %%v in ('powershell -command "(Get-Item '%%~p').VersionInfo.FileVersion"') do (
            echo   Full Version: %%v
            for /f "tokens=1 delims=." %%m in ("%%v") do (
                echo   *** Major Version: %%m ***
            )
        )
        goto :ie_done
    )
)
echo   Not installed
:ie_done
echo.

echo ========================================
echo Please note the Major Version numbers
echo and use them when packaging offline exe
echo ========================================
echo.
pause
""";
    }
}

