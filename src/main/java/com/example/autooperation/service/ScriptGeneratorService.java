package com.example.autooperation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptGeneratorService {
    private final ObjectMapper objectMapper;

    @Value("${app.python.executable:python}")
    private String pythonExecutable;

    public String generatePythonScript(String flowConfiguration) throws Exception {
        JsonNode config = objectMapper.readTree(flowConfiguration);
        StringBuilder script = new StringBuilder();

        script.append("#!/usr/bin/env python3\n");
        script.append("import sys\n");
        script.append("import os\n");
        script.append("import re\n");
        script.append("import subprocess\n");
        script.append("import logging\n");
        script.append("from selenium import webdriver\n");
        script.append("from selenium.webdriver.common.by import By\n");
        script.append("from selenium.webdriver.support.ui import WebDriverWait\n");
        script.append("from selenium.webdriver.support import expected_conditions as EC\n");
        script.append("import time\n");
        script.append("import smtplib\n");
        script.append("from email.mime.text import MIMEText\n\n");

        script.append("logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')\n");
        script.append("logger = logging.getLogger(__name__)\n\n");

        // PyInstaller 路径检测
        script.append("# 路径检测（支持 PyInstaller 打包）\n");
        script.append("if getattr(sys, 'frozen', False):\n");
        script.append("    _BUNDLE_DIR = sys._MEIPASS\n");
        script.append("else:\n");
        script.append("    _BUNDLE_DIR = os.path.dirname(os.path.abspath(__file__))\n");
        script.append("_DRIVERS_DIR = os.path.join(_BUNDLE_DIR, 'drivers')\n");
        script.append("if not os.path.exists(_DRIVERS_DIR):\n");
        script.append("    _DRIVERS_DIR = os.path.join(os.path.dirname(_BUNDLE_DIR), 'drivers')\n\n");

        // 浏览器版本检测函数
        script.append("def _get_browser_version(browser_type):\n");
        script.append("    \"\"\"检测已安装浏览器的主版本号\"\"\"\n");
        script.append("    try:\n");
        script.append("        if browser_type == 'edge':\n");
        script.append("            paths = [\n");
        script.append("                r'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',\n");
        script.append("                r'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe'\n");
        script.append("            ]\n");
        script.append("        elif browser_type == 'chrome':\n");
        script.append("            paths = [\n");
        script.append("                r'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',\n");
        script.append("                r'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe'\n");
        script.append("            ]\n");
        script.append("        else:\n");
        script.append("            return None\n");
        script.append("        for path in paths:\n");
        script.append("            if os.path.exists(path):\n");
        script.append("                result = subprocess.run(\n");
        script.append("                    ['powershell', '-command', f\"(Get-Item '{path}').VersionInfo.FileVersion\"],\n");
        script.append("                    capture_output=True, text=True, timeout=10\n");
        script.append("                )\n");
        script.append("                if result.returncode == 0:\n");
        script.append("                    version = result.stdout.strip()\n");
        script.append("                    match = re.match(r'(\\d+)', version)\n");
        script.append("                    if match:\n");
        script.append("                        return int(match.group(1))\n");
        script.append("    except Exception as e:\n");
        script.append("        logger.warning(f'Failed to detect browser version: {e}')\n");
        script.append("    return None\n\n");

        // 驱动选择函数
        script.append("def _find_matching_driver(browser_type, browser_version):\n");
        script.append("    \"\"\"根据浏览器版本选择匹配的驱动\"\"\"\n");
        script.append("    if browser_type == 'edge':\n");
        script.append("        driver_prefix = 'msedgedriver'\n");
        script.append("    elif browser_type == 'chrome':\n");
        script.append("        driver_prefix = 'chromedriver'\n");
        script.append("    elif browser_type == 'ie':\n");
        script.append("        driver_prefix = 'IEDriverServer'\n");
        script.append("    elif browser_type == 'firefox':\n");
        script.append("        driver_prefix = 'geckodriver'\n");
        script.append("    else:\n");
        script.append("        return None\n");
        script.append("    \n");
        script.append("    if not os.path.exists(_DRIVERS_DIR):\n");
        script.append("        return None\n");
        script.append("    \n");
        script.append("    # 查找所有驱动文件并提取版本号\n");
        script.append("    drivers = {}\n");
        script.append("    for f in os.listdir(_DRIVERS_DIR):\n");
        script.append("        if f.startswith(driver_prefix) and f.endswith('.exe'):\n");
        script.append("            # 格式: msedgedriver_143.exe 或 msedgedriver.exe\n");
        script.append("            match = re.search(r'_(\\d+)\\.exe$', f)\n");
        script.append("            if match:\n");
        script.append("                ver = int(match.group(1))\n");
        script.append("                drivers[ver] = os.path.join(_DRIVERS_DIR, f)\n");
        script.append("            elif f == f'{driver_prefix}.exe':\n");
        script.append("                # 无版本号的作为默认\n");
        script.append("                drivers[0] = os.path.join(_DRIVERS_DIR, f)\n");
        script.append("    \n");
        script.append("    if not drivers:\n");
        script.append("        return None\n");
        script.append("    \n");
        script.append("    # 精确匹配\n");
        script.append("    if browser_version and browser_version in drivers:\n");
        script.append("        logger.info(f'Found exact match driver for version {browser_version}')\n");
        script.append("        return drivers[browser_version]\n");
        script.append("    \n");
        script.append("    # 选择最接近的版本（不大于浏览器版本）\n");
        script.append("    if browser_version:\n");
        script.append("        compatible = [v for v in drivers.keys() if v <= browser_version and v > 0]\n");
        script.append("        if compatible:\n");
        script.append("            best = max(compatible)\n");
        script.append("            logger.info(f'Using driver version {best} for browser {browser_version}')\n");
        script.append("            return drivers[best]\n");
        script.append("    \n");
        script.append("    # 使用默认驱动或最新版本\n");
        script.append("    if 0 in drivers:\n");
        script.append("        return drivers[0]\n");
        script.append("    return drivers[max(drivers.keys())]\n\n");

        script.append("class AutomationScript:\n");
        script.append("    def __init__(self):\n");
        script.append("        self.driver = None\n\n");

        JsonNode modules = config.get("modules");

        // 收集全程监控配置（close_popup + monitor_mode=continuous）
        List<JsonNode> continuousMonitors = new ArrayList<>();
        // 收集错误监控配置
        String errorLogFolder = null;
        if (modules != null && modules.isArray()) {
            for (JsonNode module : modules) {
                String moduleType = module.get("type").asText();
                if ("close_popup".equals(moduleType)) {
                    JsonNode params = module.get("params");
                    String monitorMode = getParamText(params, "monitor_mode", "single");
                    if ("continuous".equals(monitorMode)) {
                        continuousMonitors.add(module);
                    }
                }
                if ("error_monitor".equals(moduleType)) {
                    JsonNode params = module.get("params");
                    errorLogFolder = getParamText(params, "log_folder", "自动化错误日志");
                }
            }
        }
        boolean hasErrorMonitor = errorLogFolder != null;

        // 如果有错误监控，在 __init__ 中创建日志文件夹和文件
        if (hasErrorMonitor) {
            script.append("        # 错误监控：在桌面创建日志\n");
            script.append("        _desktop = os.path.join(os.path.expanduser('~'), 'Desktop')\n");
            script.append("        _log_dir = os.path.join(_desktop, '" + errorLogFolder + "')\n");
            script.append("        os.makedirs(_log_dir, exist_ok=True)\n");
            script.append("        self._error_log_path = os.path.join(_log_dir, f'error_{time.strftime(\"%Y%m%d_%H%M%S\")}.log')\n");
            script.append("        logger.info(f'Error log: {self._error_log_path}')\n\n");
        }

        // 如果有全程监控，在类中生成 _check_popup() 辅助方法（位于 __init__ 和 run 之间）
        boolean hasMonitor = !continuousMonitors.isEmpty();
        if (hasMonitor) {
            // 取第一个全程监控的配置（通常只有一个）
            JsonNode monitorParams = continuousMonitors.get(0).get("params");
            script.append(generatePopupCheckHelper(monitorParams));
        }

        // 如果有错误监控，生成 _log_error 方法
        if (hasErrorMonitor) {
            script.append("    def _log_error(self, step_name, error):\n");
            script.append("        try:\n");
            script.append("            with open(self._error_log_path, 'a', encoding='utf-8') as f:\n");
            script.append("                f.write(f'[{time.strftime(\"%Y-%m-%d %H:%M:%S\")}] 步骤: {step_name}\\n')\n");
            script.append("                f.write(f'  错误: {type(error).__name__}: {str(error)}\\n\\n')\n");
            script.append("        except:\n");
            script.append("            pass\n\n");
        }

        script.append("    def run(self):\n");
        script.append("        try:\n");

        if (modules != null && modules.isArray()) {
            // 第一轮：生成 open_browser
            for (JsonNode module : modules) {
                if ("open_browser".equals(module.get("type").asText())) {
                    script.append(generateModuleCode(module));
                }
            }

            // 第二轮：按原顺序生成其余模块（跳过 open_browser、continuous monitor 和 error_monitor）
            for (JsonNode module : modules) {
                String type = module.get("type").asText();
                if ("open_browser".equals(type)) continue;
                if ("error_monitor".equals(type)) continue;
                if ("scheduled_task".equals(type)) continue;
                if ("close_popup".equals(type)) {
                    JsonNode params = module.get("params");
                    String monitorMode = getParamText(params, "monitor_mode", "single");
                    if ("continuous".equals(monitorMode)) continue;
                }
                // 每个业务步骤前插入弹窗检测
                if (hasMonitor) {
                    script.append("            self._check_popup()\n");
                }
                // 错误监控：用 try/except 包裹每个步骤
                if (hasErrorMonitor) {
                    String stepName = module.has("name") ? module.get("name").asText() : type;
                    script.append("            try:\n");
                    // 生成模块代码，并增加一层缩进（原来是12空格，现在需要16空格）
                    String moduleCode = generateModuleCode(module);
                    String indentedCode = moduleCode.replace("\n            ", "\n                ");
                    script.append("    ").append(indentedCode);
                    script.append("            except Exception as _step_err:\n");
                    script.append("                self._log_error('" + stepName.replace("'", "\\'") + "', _step_err)\n");
                    script.append("                raise\n");
                } else {
                    script.append(generateModuleCode(module));
                }
            }
        }

        script.append("            logger.info('Script execution completed successfully')\n");
        script.append("        except Exception as e:\n");
        script.append("            logger.error(f'Error during execution: {str(e)}')\n");
        if (hasErrorMonitor) {
            script.append("            self._log_error('脚本异常终止', e)\n");
        }
        script.append("            raise\n");
        script.append("        finally:\n");
        script.append("            if self.driver:\n");
        script.append("                logger.info('Browser will remain open for 10 seconds before closing...')\n");
        script.append("                time.sleep(10)\n");
        script.append("                self.driver.quit()\n\n");

        script.append("if __name__ == '__main__':\n");
        script.append("    script = AutomationScript()\n");
        script.append("    script.run()\n");

        return script.toString();
    }

    private String generateModuleCode(JsonNode module) {
        String type = module.get("type").asText();
        JsonNode params = module.get("params");

        return switch (type) {
            case "open_browser" -> generateOpenBrowser(params);
            case "input_account" -> generateInputAccount(params);
            case "click_button" -> generateClickButton(params);
            case "input_text" -> generateInputText(params);
            case "select_dropdown" -> generateSelectDropdown(params);
            case "wait_element" -> generateWaitElement(params);
            case "get_text" -> generateGetText(params);
            case "screenshot" -> generateScreenshot(params);
            case "hover_element" -> generateHoverElement(params);
            case "double_click" -> generateDoubleClick(params);
            case "scroll_to" -> generateScrollTo(params);
            case "get_attribute" -> generateGetAttribute(params);
            case "close_popup" -> generateClosePopup(params);
            case "batch_input" -> generateBatchInput(params);
            case "switch_frame" -> generateSwitchFrame(params);
            case "press_enter" -> generatePressEnter(params);
            case "alert_confirm" -> generateAlertConfirm(params);
            case "extract_content" -> generateExtractContent(params);
            case "confirm_dialog" -> generateConfirmDialog(params);
            case "send_email" -> generateSendEmail(params);
            case "error_monitor" -> "";
            case "scheduled_task" -> "";
            default -> "            # Unknown module type: " + type + "\n";
        };
    }

    private String generateOpenBrowser(JsonNode params) {
        String browserType = params.get("browser_type").asText("chrome");
        String url = params.get("url").asText("");

        String serviceClass = serviceClassName(browserType);
        String serviceImport = serviceImportLine(browserType);
        String optionsClass = optionsClassName(browserType);
        String optionsImport = optionsImportLine(browserType);

        StringBuilder code = new StringBuilder();
        code.append(String.format("            logger.info('Opening %s browser')\n", browserType));
        code.append(String.format("            %s\n", serviceImport));
        code.append(String.format("            %s\n", optionsImport));

        // 配置浏览器选项
        code.append(String.format("            _options = %s()\n", optionsClass));
        if (browserType.equals("chrome") || browserType.equals("edge")) {
            code.append(              "            _options.add_argument('--disable-gpu')\n");
            code.append(              "            _options.add_argument('--no-sandbox')\n");
            code.append(              "            _options.add_argument('--disable-dev-shm-usage')\n");
            code.append(              "            _options.add_argument('--disable-software-rasterizer')\n");
            code.append(              "            _options.add_argument('--disable-extensions')\n");
            code.append(              "            _options.add_argument('--log-level=3')\n");
            code.append(              "            _options.add_experimental_option('excludeSwitches', ['enable-logging', 'enable-automation'])\n");
            code.append(              "            _options.add_experimental_option('useAutomationExtension', False)\n");
        }
        if (browserType.equals("ie")) {
            // 忽略 Protected Mode 安全区域设置不一致的问题
            code.append(              "            _options.ignore_protected_mode_settings = True\n");
            // 忽略缩放比例不是 100% 的问题
            code.append(              "            _options.ignore_zoom_level = True\n");
            // 确保使用干净的会话
            code.append(              "            _options.ensure_clean_session = True\n");
            // 设置初始 URL 为 about:blank 避免启动延迟
            code.append(              "            _options.initial_browser_url = 'about:blank'\n");
        }

        // 检测浏览器版本并选择匹配的驱动
        code.append(String.format("            _browser_ver = _get_browser_version('%s')\n", browserType));
        code.append(              "            logger.info(f'Detected browser version: {_browser_ver}')\n");
        code.append(String.format("            _driver_path = _find_matching_driver('%s', _browser_ver)\n", browserType));

        code.append(              "            if _driver_path:\n");
        code.append(              "                logger.info(f'Using bundled driver: {_driver_path}')\n");
        code.append(String.format("                _service = %s(executable_path=_driver_path)\n", serviceClass));
        code.append(String.format("                self.driver = webdriver.%s(service=_service, options=_options)\n", browserClass(browserType)));
        code.append(              "            else:\n");
        code.append(              "                logger.info('No bundled driver found, using Selenium auto-management')\n");
        code.append(String.format("                self.driver = webdriver.%s(options=_options)\n", browserClass(browserType)));

        code.append(String.format("            self.driver.get('%s')\n", url));
        code.append(              "            time.sleep(2)\n");

        return code.toString();
    }

    private String optionsClassName(String browserType) {
        return switch (browserType) {
            case "chrome" -> "ChromeOptions";
            case "firefox" -> "FirefoxOptions";
            case "edge" -> "EdgeOptions";
            case "ie" -> "IeOptions";
            default -> "Options";
        };
    }

    private String optionsImportLine(String browserType) {
        return switch (browserType) {
            case "chrome" -> "from selenium.webdriver.chrome.options import Options as ChromeOptions";
            case "firefox" -> "from selenium.webdriver.firefox.options import Options as FirefoxOptions";
            case "edge" -> "from selenium.webdriver.edge.options import Options as EdgeOptions";
            case "ie" -> "from selenium.webdriver.ie.options import Options as IeOptions";
            default -> "from selenium.webdriver.chrome.options import Options";
        };
    }

    private String driverFileName(String browserType) {
        return switch (browserType) {
            case "chrome" -> "chromedriver.exe";
            case "firefox" -> "geckodriver.exe";
            case "edge" -> "msedgedriver.exe";
            case "ie" -> "IEDriverServer.exe";
            default -> browserType + "driver.exe";
        };
    }

    private String serviceClassName(String browserType) {
        return switch (browserType) {
            case "chrome" -> "ChromeService";
            case "firefox" -> "FirefoxService";
            case "edge" -> "EdgeService";
            case "ie" -> "IeService";
            default -> "Service";
        };
    }

    private String serviceImportLine(String browserType) {
        return switch (browserType) {
            case "chrome" -> "from selenium.webdriver.chrome.service import Service as ChromeService";
            case "firefox" -> "from selenium.webdriver.firefox.service import Service as FirefoxService";
            case "edge" -> "from selenium.webdriver.edge.service import Service as EdgeService";
            case "ie" -> "from selenium.webdriver.ie.service import Service as IeService";
            default -> "from selenium.webdriver.chrome.service import Service";
        };
    }

    private String browserClass(String browserType) {
        return switch (browserType) {
            case "chrome" -> "Chrome";
            case "firefox" -> "Firefox";
            case "edge" -> "Edge";
            case "ie" -> "Ie";
            default -> capitalize(browserType);
        };
    }

    private String wdmManagerClass(String browserType) {
        return switch (browserType) {
            case "chrome" -> "ChromeDriverManager";
            case "firefox" -> "GeckoDriverManager";
            case "edge" -> "EdgeChromiumDriverManager";
            case "ie" -> "IEDriverManager";
            default -> "ChromeDriverManager";
        };
    }

    private String wdmImportLine(String browserType) {
        return switch (browserType) {
            case "chrome" -> "from webdriver_manager.chrome import ChromeDriverManager";
            case "firefox" -> "from webdriver_manager.firefox import GeckoDriverManager";
            case "edge" -> "from webdriver_manager.microsoft import EdgeChromiumDriverManager";
            case "ie" -> "from webdriver_manager.microsoft import IEDriverManager";
            default -> "from webdriver_manager.chrome import ChromeDriverManager";
        };
    }

    /**
     * 统一元素定位方法：根据 locate_by 生成不同的 find_element 代码
     * locate_by=css   → By.CSS_SELECTOR
     * locate_by=text  → By.XPATH with contains(text())
     * locate_by=index → By.XPATH with positional index
     * 默认(无locate_by) → CSS_SELECTOR 兼容旧数据
     */
    private String generateFindElement(String varName, String selector, String locateBy, String elementTag) {
        if (locateBy == null || locateBy.isEmpty() || "css".equals(locateBy)) {
            return String.format("%s = self.driver.find_element(By.CSS_SELECTOR, '%s')", varName, selector);
        } else if ("text".equals(locateBy)) {
            return String.format("%s = self.driver.find_element(By.XPATH, '//*[not(self::script) and not(self::style)][contains(text(), \"%s\")]')", varName, selector);
        } else if ("index".equals(locateBy)) {
            String tag = (elementTag == null || elementTag.isEmpty()) ? "*" : elementTag;
            return String.format("%s = self.driver.find_element(By.XPATH, '(//%s)[%s]')", varName, tag, selector);
        }
        return String.format("%s = self.driver.find_element(By.CSS_SELECTOR, '%s')", varName, selector);
    }

    /**
     * 统一元素定位表达式（不赋值，用于 WebDriverWait 等场景）
     */
    private String generateLocator(String selector, String locateBy, String elementTag) {
        if (locateBy == null || locateBy.isEmpty() || "css".equals(locateBy)) {
            return String.format("(By.CSS_SELECTOR, '%s')", selector);
        } else if ("text".equals(locateBy)) {
            return String.format("(By.XPATH, '//*[not(self::script) and not(self::style)][contains(text(), \"%s\")]')", selector);
        } else if ("index".equals(locateBy)) {
            String tag = (elementTag == null || elementTag.isEmpty()) ? "*" : elementTag;
            return String.format("(By.XPATH, '(//%s)[%s]')", tag, selector);
        }
        return String.format("(By.CSS_SELECTOR, '%s')", selector);
    }

    private String getParamText(JsonNode params, String key, String defaultValue) {
        JsonNode node = params.get(key);
        return node != null ? node.asText(defaultValue) : defaultValue;
    }

    private String generateInputAccount(JsonNode params) {
        String usernameSelector = params.get("username_selector").asText("");
        String username = params.get("username").asText("");
        String passwordSelector = params.get("password_selector").asText("");
        String password = params.get("password").asText("");
        String locateBy = getParamText(params, "locate_by", "css");

        String findUsername = generateFindElement("username_elem", usernameSelector, locateBy, "input");
        String findPassword = generateFindElement("password_elem", passwordSelector, locateBy, "input");

        return String.format(
                "            logger.info('Inputting account and password')\n" +
                "            %s\n" +
                "            username_elem.send_keys('%s')\n" +
                "            %s\n" +
                "            password_elem.send_keys('%s')\n" +
                "            time.sleep(1)\n",
                findUsername, username, findPassword, password
        );
    }

    private String generateClickButton(JsonNode params) {
        String selector = params.get("selector").asText("");
        int waitTime = params.get("wait_time").asInt(1);
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String findElement = generateFindElement("element", selector, locateBy, elementTag);

        return String.format(
                "            logger.info('Clicking button')\n" +
                "            %s\n" +
                "            try:\n" +
                "                element.click()\n" +
                "            except Exception as _click_err:\n" +
                "                if 'obscures it' in str(_click_err) or 'not clickable' in str(_click_err) or 'not interactable' in str(_click_err):\n" +
                "                    logger.warning(f'Click blocked by overlay, trying to close popup first: {_click_err}')\n" +
                "                    if hasattr(self, '_check_popup'):\n" +
                "                        self._check_popup()\n" +
                "                        time.sleep(0.5)\n" +
                "                    try:\n" +
                "                        %s\n" +
                "                        element.click()\n" +
                "                        logger.info('Retry click succeeded after closing popup')\n" +
                "                    except Exception:\n" +
                "                        logger.warning('Retry click still blocked, falling back to JS click')\n" +
                "                        %s\n" +
                "                        self.driver.execute_script(\"\"\"\n" +
                "                            var el = arguments[0];\n" +
                "                            // 1. jQuery trigger\n" +
                "                            if (typeof jQuery !== 'undefined') {\n" +
                "                                jQuery(el).trigger('click');\n" +
                "                                // 检查是否有 data-ilink（HiSUI菜单），手动触发导航\n" +
                "                                var ilink = el.getAttribute('data-ilink');\n" +
                "                                var itarget = el.getAttribute('data-itarget');\n" +
                "                                if (ilink && itarget) {\n" +
                "                                    var frame = document.querySelector('iframe[name=\"' + itarget + '\"]') || document.querySelector('frame[name=\"' + itarget + '\"]');\n" +
                "                                    if (frame) { frame.src = ilink; return; }\n" +
                "                                }\n" +
                "                                return;\n" +
                "                            }\n" +
                "                            // 2. 原生 click\n" +
                "                            el.click();\n" +
                "                        \"\"\", element)\n" +
                "                else:\n" +
                "                    raise\n" +
                "            time.sleep(%d)\n",
                findElement, findElement, findElement, waitTime
        );
    }

    private String generateSelectDropdown(JsonNode params) {
        String selector = params.get("selector").asText("");
        String value = params.get("value").asText("");
        String selectBy = params.get("select_by").asText("value");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "select");

        String findElement = generateFindElement("element", selector, locateBy, elementTag);

        String selectCode = "value".equals(selectBy)
            ? String.format("select.select_by_value('%s')", value)
            : String.format("select.select_by_visible_text('%s')", value);

        return String.format(
                "            logger.info('Selecting dropdown option')\n" +
                "            from selenium.webdriver.support.ui import Select\n" +
                "            %s\n" +
                "            select = Select(element)\n" +
                "            %s\n" +
                "            time.sleep(1)\n",
                findElement, selectCode
        );
    }

    private String generateHoverElement(JsonNode params) {
        String selector = params.get("selector").asText("");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String findElement = generateFindElement("element", selector, locateBy, elementTag);

        return String.format(
                "            logger.info('Hovering over element')\n" +
                "            from selenium.webdriver.common.action_chains import ActionChains\n" +
                "            %s\n" +
                "            ActionChains(self.driver).move_to_element(element).perform()\n" +
                "            time.sleep(1)\n",
                findElement
        );
    }

    private String generateDoubleClick(JsonNode params) {
        String selector = params.get("selector").asText("");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String findElement = generateFindElement("element", selector, locateBy, elementTag);

        return String.format(
                "            logger.info('Double clicking element')\n" +
                "            from selenium.webdriver.common.action_chains import ActionChains\n" +
                "            %s\n" +
                "            ActionChains(self.driver).double_click(element).perform()\n" +
                "            time.sleep(1)\n",
                findElement
        );
    }

    private String generateScrollTo(JsonNode params) {
        String selector = params.get("selector").asText("");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String findElement = generateFindElement("element", selector, locateBy, elementTag);

        return String.format(
                "            logger.info('Scrolling to element')\n" +
                "            %s\n" +
                "            self.driver.execute_script('arguments[0].scrollIntoView(true);', element)\n" +
                "            time.sleep(1)\n",
                findElement
        );
    }

    private String generateGetAttribute(JsonNode params) {
        String selector = params.get("selector").asText("");
        String attribute = params.get("attribute").asText("");
        String varName = params.get("var_name").asText("attr_value");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String findElement = generateFindElement("_elem", selector, locateBy, elementTag);

        return String.format(
                "            logger.info('Getting attribute %s from element')\n" +
                "            %s\n" +
                "            %s = _elem.get_attribute('%s')\n" +
                "            logger.info(f'Attribute value: {%s}')\n",
                attribute, findElement, varName, attribute, varName
        );
    }

    private String generateInputText(JsonNode params) {
        String selector = params.get("selector").asText("");
        String text = params.get("text").asText("");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String findElement = generateFindElement("element", selector, locateBy, elementTag);

        return "            logger.info('Inputting text')\n" +
                "            from selenium.webdriver.common.action_chains import ActionChains\n" +
                "            from selenium.webdriver.common.keys import Keys\n" +
                String.format("            %s\n", findElement) +
                "            # 智能输入：先尝试标准方法，失败则用 ActionChains 模拟真实键盘输入\n" +
                String.format("            _input_text = f'%s'\n", text.replace("'", "\\'")) +
                "            try:\n" +
                "                element.clear()\n" +
                "                element.send_keys(_input_text)\n" +
                "            except Exception:\n" +
                "                # contenteditable div 兜底：点击聚焦 → 全选清空 → 模拟打字\n" +
                "                ActionChains(self.driver).click(element).key_down(Keys.CONTROL).send_keys('a').key_up(Keys.CONTROL).send_keys(Keys.DELETE).perform()\n" +
                "                time.sleep(0.3)\n" +
                "                ActionChains(self.driver).click(element).send_keys(_input_text).perform()\n";
    }

    private String generateClickElement(JsonNode params) {
        String selector = params.get("selector").asText("");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String findElement = generateFindElement("element", selector, locateBy, elementTag);

        return String.format(
                "            logger.info('Clicking element')\n" +
                "            %s\n" +
                "            element.click()\n" +
                "            time.sleep(1)\n",
                findElement
        );
    }

    private String generateWaitElement(JsonNode params) {
        String selector = params.get("selector").asText("");
        int timeout = params.get("timeout").asInt(10);
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String locator = generateLocator(selector, locateBy, elementTag);

        return String.format(
                "            logger.info('Waiting for element')\n" +
                "            WebDriverWait(self.driver, %d).until(\n" +
                "                EC.presence_of_element_located(%s)\n" +
                "            )\n",
                timeout, locator
        );
    }

    private String generateGetText(JsonNode params) {
        String selector = params.get("selector").asText("");
        String varName = params.get("var_name").asText("text_content");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");

        String findElement = generateFindElement("_elem", selector, locateBy, elementTag);

        return String.format(
                "            logger.info('Getting text from element')\n" +
                "            %s\n" +
                "            %s = _elem.text\n" +
                "            logger.info(f'Text: {%s}')\n",
                findElement, varName, varName
        );
    }

    private String generateScreenshot(JsonNode params) {
        String filename = params.get("filename").asText("screenshot.png");
        return String.format(
                "            logger.info('Taking screenshot')\n" +
                "            self.driver.save_screenshot('%s')\n",
                filename
        );
    }

    private String generateBatchInput(JsonNode params) {
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "input");

        JsonNode inputs = params.get("inputs");
        if (inputs == null || !inputs.isArray() || inputs.size() == 0) {
            return "            logger.info('Batch input: no inputs configured')\n";
        }

        StringBuilder code = new StringBuilder();
        code.append("            from selenium.webdriver.common.action_chains import ActionChains\n");
        code.append("            from selenium.webdriver.common.keys import Keys\n");
        code.append(String.format("            logger.info('Batch input: filling %d fields')\n", inputs.size()));

        for (int i = 0; i < inputs.size(); i++) {
            JsonNode item = inputs.get(i);
            String selector = item.has("selector") ? item.get("selector").asText("") : "";
            String text = item.has("text") ? item.get("text").asText("") : "";
            boolean clearFirst = item.has("clear_first") && item.get("clear_first").asBoolean(true);

            if (selector.isEmpty()) continue;

            String varName = "_input_" + i;
            String findElement = generateFindElement(varName, selector, locateBy, elementTag);

            code.append(String.format("            # Input %d: %s\n", i + 1, selector));
            code.append(String.format("            %s\n", findElement));

            if (clearFirst) {
                code.append(String.format("            try:\n"));
                code.append(String.format("                %s.clear()\n", varName));
                code.append(String.format("                %s.send_keys('%s')\n", varName, text));
                code.append(String.format("            except Exception:\n"));
                code.append(String.format("                ActionChains(self.driver).click(%s).key_down(Keys.CONTROL).send_keys('a').key_up(Keys.CONTROL).send_keys(Keys.DELETE).perform()\n", varName));
                code.append(              "                time.sleep(0.3)\n");
                code.append(String.format("                ActionChains(self.driver).click(%s).send_keys('%s').perform()\n", varName, text));
            } else {
                code.append(String.format("            try:\n"));
                code.append(String.format("                %s.send_keys('%s')\n", varName, text));
                code.append(String.format("            except Exception:\n"));
                code.append(String.format("                ActionChains(self.driver).click(%s).send_keys('%s').perform()\n", varName, text));
            }
            code.append(              "            time.sleep(0.3)\n");
        }

        return code.toString();
    }

    private String generatePressEnter(JsonNode params) {
        String target = getParamText(params, "target", "active");

        StringBuilder code = new StringBuilder();
        code.append("            from selenium.webdriver.common.keys import Keys\n");

        if ("active".equals(target)) {
            code.append("            logger.info('Pressing Enter on active element')\n");
            code.append("            self.driver.switch_to.active_element.send_keys(Keys.ENTER)\n");
        } else {
            String selector = getParamText(params, "selector", "");
            String locateBy = getParamText(params, "locate_by", "css");
            String elementTag = getParamText(params, "element_tag", "*");
            String findElement = generateFindElement("_enter_el", selector, locateBy, elementTag);

            code.append("            logger.info('Pressing Enter on specified element')\n");
            code.append(String.format("            %s\n", findElement));
            code.append("            _enter_el.send_keys(Keys.ENTER)\n");
        }

        code.append("            time.sleep(1)\n");
        return code.toString();
    }

    private String generateSwitchFrame(JsonNode params) {
        String action = getParamText(params, "frame_action", "by_name");
        String locator = getParamText(params, "frame_locator", "");

        StringBuilder code = new StringBuilder();

        switch (action) {
            case "main" -> {
                code.append("            logger.info('Switching to main content (top level)')\n");
                code.append("            self.driver.switch_to.default_content()\n");
            }
            case "parent" -> {
                code.append("            logger.info('Switching to parent frame')\n");
                code.append("            self.driver.switch_to.parent_frame()\n");
            }
            case "by_index" -> {
                code.append(String.format("            logger.info('Switching to frame by index: %s')\n", locator));
                code.append(String.format("            self.driver.switch_to.frame(%s)\n", locator));
            }
            default -> {
                // by_name (name/id) or by_css
                if ("by_css".equals(action)) {
                    code.append(String.format("            logger.info('Switching to frame by CSS: %s')\n", locator));
                    code.append(String.format("            _frame_el = self.driver.find_element(By.CSS_SELECTOR, '%s')\n", locator));
                    code.append("            self.driver.switch_to.frame(_frame_el)\n");
                } else {
                    code.append(String.format("            logger.info('Switching to frame: %s')\n", locator));
                    code.append("            # 优先用 CSS 选择器精确匹配 iframe/frame，避免匹配到同名 div\n");
                    code.append(String.format("            try:\n"));
                    code.append(String.format("                _frame_el = self.driver.find_element(By.CSS_SELECTOR, 'iframe#%s, frame#%s')\n", locator, locator));
                    code.append(String.format("            except Exception:\n"));
                    code.append(String.format("                try:\n"));
                    code.append(String.format("                    _frame_el = self.driver.find_element(By.CSS_SELECTOR, 'iframe[name=\"%s\"], frame[name=\"%s\"]')\n", locator, locator));
                    code.append(String.format("                except Exception:\n"));
                    code.append(String.format("                    # By.NAME 可能匹配到同名 div，再用 XPath 精确匹配 iframe/frame\n"));
                    code.append(String.format("                    try:\n"));
                    code.append(String.format("                        _frame_el = self.driver.find_element(By.XPATH, '//iframe[@name=\"%s\" or @id=\"%s\"] | //frame[@name=\"%s\" or @id=\"%s\"]')\n", locator, locator, locator, locator));
                    code.append(String.format("                    except Exception:\n"));
                    code.append(String.format("                        _frame_el = self.driver.find_element(By.NAME, '%s')\n", locator));
                    code.append("            self.driver.switch_to.frame(_frame_el)\n");
                }
            }
        }

        code.append("            time.sleep(0.5)\n");
        return code.toString();
    }

    private String generateClosePopup(JsonNode params) {
        String monitorMode = getParamText(params, "monitor_mode", "single");

        // 全程监控模式由 generatePopupCheckHelper 处理，此处不生成代码
        if ("continuous".equals(monitorMode)) {
            return "";
        }

        // 单次关闭模式
        String closeMethod = getParamText(params, "close_method", "auto_detect");
        String selector = getParamText(params, "selector", "");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");
        int waitTime = params.has("wait_time") ? params.get("wait_time").asInt(2) : 2;
        boolean ignoreError = params.has("ignore_error") && params.get("ignore_error").asBoolean(true);

        StringBuilder code = new StringBuilder();
        code.append(String.format("            logger.info('Attempting to close popup (method: %s)')\n", closeMethod));
        code.append(String.format("            time.sleep(%d)\n", waitTime));

        switch (closeMethod) {
            case "alert_accept" -> {
                // 仅处理浏览器 alert 弹窗 → 点击确认
                code.append("            try:\n");
                code.append("                _alert = self.driver.switch_to.alert\n");
                code.append("                logger.info(f'Alert text: {_alert.text}')\n");
                code.append("                _alert.accept()\n");
                code.append("                logger.info('Alert accepted')\n");
                code.append("            except Exception as _e:\n");
                if (ignoreError) {
                    code.append("                logger.warning(f'No alert found or failed to accept: {_e}')\n");
                } else {
                    code.append("                logger.error(f'Failed to accept alert: {_e}')\n");
                    code.append("                raise\n");
                }
            }
            case "alert_dismiss" -> {
                // 仅处理浏览器 alert 弹窗 → 点击取消/关闭
                code.append("            try:\n");
                code.append("                _alert = self.driver.switch_to.alert\n");
                code.append("                logger.info(f'Alert text: {_alert.text}')\n");
                code.append("                _alert.dismiss()\n");
                code.append("                logger.info('Alert dismissed')\n");
                code.append("            except Exception as _e:\n");
                if (ignoreError) {
                    code.append("                logger.warning(f'No alert found or failed to dismiss: {_e}')\n");
                } else {
                    code.append("                logger.error(f'Failed to dismiss alert: {_e}')\n");
                    code.append("                raise\n");
                }
            }
            case "confirm_accept" -> {
                // 仅处理浏览器 confirm 对话框 → 点击确认
                code.append("            try:\n");
                code.append("                _confirm = self.driver.switch_to.alert\n");
                code.append("                logger.info(f'Confirm dialog text: {_confirm.text}')\n");
                code.append("                _confirm.accept()\n");
                code.append("                logger.info('Confirm dialog accepted')\n");
                code.append("            except Exception as _e:\n");
                if (ignoreError) {
                    code.append("                logger.warning(f'No confirm dialog found or failed to accept: {_e}')\n");
                } else {
                    code.append("                logger.error(f'Failed to accept confirm dialog: {_e}')\n");
                    code.append("                raise\n");
                }
            }
            case "confirm_dismiss" -> {
                // 仅处理浏览器 confirm 对话框 → 点击取消
                code.append("            try:\n");
                code.append("                _confirm = self.driver.switch_to.alert\n");
                code.append("                logger.info(f'Confirm dialog text: {_confirm.text}')\n");
                code.append("                _confirm.dismiss()\n");
                code.append("                logger.info('Confirm dialog dismissed')\n");
                code.append("            except Exception as _e:\n");
                if (ignoreError) {
                    code.append("                logger.warning(f'No confirm dialog found or failed to dismiss: {_e}')\n");
                } else {
                    code.append("                logger.error(f'Failed to dismiss confirm dialog: {_e}')\n");
                    code.append("                raise\n");
                }
            }
            case "dom_only" -> {
                // 仅关闭页面上的 DOM 弹窗（模态框等），不处理浏览器 alert/confirm
                code.append("            _popup_closed = False\n");
                code.append("            _close_selectors = [\n");
                appendDomCloseSelectors(code);
                code.append("            ]\n");
                code.append("            for _by, _sel in _close_selectors:\n");
                code.append("                try:\n");
                code.append("                    _btn = self.driver.find_element(_by, _sel)\n");
                code.append("                    self.driver.execute_script('if(typeof jQuery!==\"undefined\"){jQuery(arguments[0]).trigger(\"click\")}else{arguments[0].click()}', _btn)\n");
                code.append("                    _popup_closed = True\n");
                code.append("                    logger.info(f'DOM popup closed using selector: {_sel}')\n");
                code.append("                    break\n");
                code.append("                except Exception:\n");
                code.append("                    continue\n");
                code.append("            if not _popup_closed:\n");
                if (ignoreError) {
                    code.append("                logger.warning('No DOM popup detected or could not close')\n");
                } else {
                    code.append("                raise Exception('No DOM popup detected or could not close')\n");
                }
            }
            case "click_element" -> {
                // 点击指定的关闭按钮（多种策略关闭 EasyUI/HiSUI 弹窗）
                String findElement = generateFindElement("_close_btn", selector, locateBy, elementTag);
                code.append("            try:\n");
                code.append(String.format("                %s\n", findElement));
                code.append("                # 策略1: 尝试 EasyUI/HiSUI window('close') API\n");
                code.append("                _closed = self.driver.execute_script(\"\"\"\n");
                code.append("                    var el = arguments[0];\n");
                code.append("                    if (typeof jQuery === 'undefined') return false;\n");
                code.append("                    // 从关闭按钮向上找到 .panel.window 容器\n");
                code.append("                    var panel = el.closest('.panel.window') || el.closest('.panel');\n");
                code.append("                    if (panel) {\n");
                code.append("                        // 找 window-body / panel-body 并调用 window('close') 或 panel('close')\n");
                code.append("                        var body = panel.querySelector('.window-body') || panel.querySelector('.panel-body');\n");
                code.append("                        if (body) {\n");
                code.append("                            try { jQuery(body).window('close'); return true; } catch(e1) {\n");
                code.append("                                try { jQuery(body).panel('close'); return true; } catch(e2) {\n");
                code.append("                                    try { jQuery(body).dialog('close'); return true; } catch(e3) {}\n");
                code.append("                                }\n");
                code.append("                            }\n");
                code.append("                        }\n");
                code.append("                        // 如果 API 都失败，直接隐藏面板\n");
                code.append("                        panel.style.display = 'none';\n");
                code.append("                        var mask = document.querySelector('.window-mask');\n");
                code.append("                        if (mask) mask.style.display = 'none';\n");
                code.append("                        return true;\n");
                code.append("                    }\n");
                code.append("                    return false;\n");
                code.append("                \"\"\", _close_btn)\n");
                code.append("                if not _closed:\n");
                code.append("                    # 策略2: 普通点击 / jQuery 触发\n");
                code.append("                    try:\n");
                code.append("                        _close_btn.click()\n");
                code.append("                    except Exception:\n");
                code.append("                        self.driver.execute_script(\n");
                code.append("                            'if(typeof jQuery!==\"undefined\"){jQuery(arguments[0]).trigger(\"click\")}else{arguments[0].click()}',\n");
                code.append("                            _close_btn)\n");
                code.append("                logger.info('Popup closed by clicking element')\n");
                code.append("                time.sleep(0.5)\n");
                code.append("            except Exception as _e:\n");
                if (ignoreError) {
                    code.append("                logger.warning(f'Close button not found or click failed: {_e}')\n");
                } else {
                    code.append("                logger.error(f'Failed to click close button: {_e}')\n");
                    code.append("                raise\n");
                }
            }
            default -> {
                // auto_detect: 依次尝试 alert/confirm → DOM 弹窗
                code.append("            _popup_closed = False\n");
                code.append("            # Step 1: 尝试处理浏览器 alert/confirm\n");
                code.append("            try:\n");
                code.append("                _alert = self.driver.switch_to.alert\n");
                code.append("                logger.info(f'Found browser dialog: {_alert.text}')\n");
                code.append("                _alert.accept()\n");
                code.append("                _popup_closed = True\n");
                code.append("                logger.info('Browser dialog accepted')\n");
                code.append("            except Exception:\n");
                code.append("                pass\n");
                code.append("            # Step 2: 尝试关闭页面 DOM 弹窗\n");
                code.append("            if not _popup_closed:\n");
                code.append("                _close_selectors = [\n");
                appendDomCloseSelectors(code);
                code.append("                ]\n");
                code.append("                for _by, _sel in _close_selectors:\n");
                code.append("                    try:\n");
                code.append("                        _btn = self.driver.find_element(_by, _sel)\n");
                code.append("                        self.driver.execute_script('if(typeof jQuery!==\"undefined\"){jQuery(arguments[0]).trigger(\"click\")}else{arguments[0].click()}', _btn)\n");
                code.append("                        _popup_closed = True\n");
                code.append("                        logger.info(f'DOM popup closed using selector: {_sel}')\n");
                code.append("                        break\n");
                code.append("                    except Exception:\n");
                code.append("                        continue\n");
                code.append("            if not _popup_closed:\n");
                if (ignoreError) {
                    code.append("                logger.warning('No popup detected or could not close automatically')\n");
                } else {
                    code.append("                raise Exception('No popup detected or could not close automatically')\n");
                }
            }
        }

        return code.toString();
    }

    /**
     * 公共方法：追加常见 DOM 弹窗关闭按钮选择器列表
     */
    private void appendDomCloseSelectors(StringBuilder code) {
        code.append("                (By.CSS_SELECTOR, '.close'),\n");
        code.append("                (By.CSS_SELECTOR, '.modal-close'),\n");
        code.append("                (By.CSS_SELECTOR, '[aria-label=\"Close\"]'),\n");
        code.append("                (By.CSS_SELECTOR, '[aria-label=\"close\"]'),\n");
        code.append("                (By.CSS_SELECTOR, 'button.close'),\n");
        code.append("                (By.CSS_SELECTOR, '.el-dialog__headerbtn'),\n");
        code.append("                (By.CSS_SELECTOR, '.ant-modal-close'),\n");
        code.append("                (By.CSS_SELECTOR, '.layui-layer-close'),\n");
        code.append("                (By.CSS_SELECTOR, '.ivu-modal-close'),\n");
        code.append("                (By.XPATH, '//*[contains(@class,\"close\") and (self::button or self::a or self::span or self::i)]'),\n");
        code.append("                (By.XPATH, '//button[text()=\"\\u00d7\"]'),\n");
        code.append("                (By.XPATH, '//span[text()=\"\\u00d7\"]'),\n");
        code.append("                (By.XPATH, '//*[text()=\"\\u5173\\u95ed\"]'),\n");
    }

    /**
     * 生成 _check_popup() 实例方法，在每个业务步骤前调用
     * 无多线程，无连接池冲突，完全在主线程中同步执行
     */
    private String generatePopupCheckHelper(JsonNode params) {
        String closeMethod = getParamText(params, "close_method", "auto_detect");
        String customSelectors = getParamText(params, "custom_selectors", "");

        boolean handleAlert = closeMethod.equals("auto_detect") || closeMethod.equals("alert_accept")
                || closeMethod.equals("alert_dismiss") || closeMethod.equals("confirm_accept")
                || closeMethod.equals("confirm_dismiss");
        boolean handleDom = closeMethod.equals("auto_detect") || closeMethod.equals("dom_only");
        boolean dismissMode = closeMethod.equals("alert_dismiss") || closeMethod.equals("confirm_dismiss");

        StringBuilder code = new StringBuilder();
        code.append("    def _check_popup(self):\n");
        code.append("        \"\"\"检测并关闭弹窗（每步骤前自动调用，循环关闭直到没有弹窗）\"\"\"\n");
        code.append("        for _round in range(5):  # 最多尝试5轮\n");
        code.append("            _found = False\n");

        if (handleAlert) {
            code.append("            try:\n");
            code.append("                _alert = self.driver.switch_to.alert\n");
            code.append("                logger.info(f'Auto-close: found dialog: {_alert.text}')\n");
            if (dismissMode) {
                code.append("                _alert.dismiss()\n");
            } else {
                code.append("                _alert.accept()\n");
            }
            code.append("                logger.info('Auto-close: dialog handled')\n");
            code.append("                time.sleep(0.5)\n");
            code.append("                _found = True\n");
            code.append("                continue\n");
            code.append("            except Exception:\n");
            code.append("                pass\n");
        }

        if (handleDom) {
            code.append("            _close_selectors = [\n");
            if (!customSelectors.isEmpty()) {
                String[] parts = customSelectors.split(",");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        code.append(String.format("                (By.CSS_SELECTOR, '%s'),\n", trimmed));
                    }
                }
            }
            code.append("                (By.CSS_SELECTOR, '.close'),\n");
            code.append("                (By.CSS_SELECTOR, '.modal-close'),\n");
            code.append("                (By.CSS_SELECTOR, '[aria-label=\"Close\"]'),\n");
            code.append("                (By.CSS_SELECTOR, '[aria-label=\"close\"]'),\n");
            code.append("                (By.CSS_SELECTOR, 'button.close'),\n");
            code.append("                (By.CSS_SELECTOR, '.el-dialog__headerbtn'),\n");
            code.append("                (By.CSS_SELECTOR, '.ant-modal-close'),\n");
            code.append("                (By.CSS_SELECTOR, '.layui-layer-close'),\n");
            code.append("                (By.CSS_SELECTOR, '.ivu-modal-close'),\n");
            code.append("                (By.XPATH, '//*[contains(@class,\"close\") and (self::button or self::a or self::span or self::i)]'),\n");
            code.append("                (By.XPATH, '//button[text()=\"\\u00d7\"]'),\n");
            code.append("                (By.XPATH, '//span[text()=\"\\u00d7\"]'),\n");
            code.append("                (By.XPATH, '//*[text()=\"\\u5173\\u95ed\"]'),\n");
            code.append("            ]\n");
            code.append("            for _by, _sel in _close_selectors:\n");
            code.append("                try:\n");
            code.append("                    _btns = self.driver.find_elements(_by, _sel)\n");
            code.append("                    for _btn in _btns:\n");
            code.append("                        try:\n");
            code.append("                            _visible = self.driver.execute_script(\n");
            code.append("                                'var r = arguments[0].getBoundingClientRect();'\n");
            code.append("                                'return r.width > 0 || r.height > 0 || arguments[0].offsetParent !== null;',\n");
            code.append("                                _btn)\n");
            code.append("                            if not _visible:\n");
            code.append("                                continue\n");
            code.append("                            # 多种方式尝试关闭（EasyUI API → 隐藏面板 → jQuery → 原生click）\n");
            code.append("                            _done = self.driver.execute_script(\"\"\"\n");
            code.append("                                var el = arguments[0];\n");
            code.append("                                if (typeof jQuery !== 'undefined') {\n");
            code.append("                                    var panel = el.closest('.panel.window') || el.closest('.panel');\n");
            code.append("                                    if (panel) {\n");
            code.append("                                        var body = panel.querySelector('.window-body') || panel.querySelector('.panel-body');\n");
            code.append("                                        if (body) {\n");
            code.append("                                            try { jQuery(body).window('close'); return true; } catch(e1) {\n");
            code.append("                                                try { jQuery(body).panel('close'); return true; } catch(e2) {\n");
            code.append("                                                    try { jQuery(body).dialog('close'); return true; } catch(e3) {}\n");
            code.append("                                                }\n");
            code.append("                                            }\n");
            code.append("                                        }\n");
            code.append("                                        panel.style.display = 'none';\n");
            code.append("                                        var mask = document.querySelector('.window-mask');\n");
            code.append("                                        if (mask) mask.style.display = 'none';\n");
            code.append("                                        return true;\n");
            code.append("                                    }\n");
            code.append("                                }\n");
            code.append("                                return false;\n");
            code.append("                            \"\"\", _btn)\n");
            code.append("                            if not _done:\n");
            code.append("                                try:\n");
            code.append("                                    _btn.click()\n");
            code.append("                                except Exception:\n");
            code.append("                                    self.driver.execute_script('arguments[0].click()', _btn)\n");
            code.append("                            logger.info(f'Auto-close: closed popup via {_sel}')\n");
            code.append("                            time.sleep(0.5)\n");
            code.append("                            _found = True\n");
            code.append("                            break\n");
            code.append("                        except Exception:\n");
            code.append("                            continue\n");
            code.append("                except Exception:\n");
            code.append("                    continue\n");
            code.append("            if _found:\n");
            code.append("                continue\n");
        }

        code.append("            break  # 本轮没找到弹窗，退出循环\n");
        code.append("\n");
        return code.toString();
    }

    private String generateAlertConfirm(JsonNode params) {
        int waitTime = params.has("wait_time") ? params.get("wait_time").asInt(2) : 2;
        boolean ignoreError = params.has("ignore_error") && params.get("ignore_error").asBoolean(true);

        StringBuilder code = new StringBuilder();
        code.append("            logger.info('Waiting for alert dialog...')\n");
        code.append(String.format("            time.sleep(%d)\n", waitTime));
        code.append("            try:\n");
        code.append("                _alert = self.driver.switch_to.alert\n");
        code.append("                logger.info(f'Alert detected, text: {_alert.text}')\n");
        code.append("                _alert.accept()\n");
        code.append("                logger.info('Alert confirmed (accepted)')\n");
        code.append("            except Exception as _e:\n");
        if (ignoreError) {
            code.append("                logger.warning(f'No alert found or failed to accept: {_e}')\n");
        } else {
            code.append("                logger.error(f'Failed to accept alert: {_e}')\n");
            code.append("                raise\n");
        }
        return code.toString();
    }

    private String generateExtractContent(JsonNode params) {
        String selector = getParamText(params, "selector", "");
        String varName = getParamText(params, "var_name", "extracted_content");
        String locateBy = getParamText(params, "locate_by", "css");
        String elementTag = getParamText(params, "element_tag", "*");
        String extractType = getParamText(params, "extract_type", "text");

        String findElement = generateFindElement("_extract_elem", selector, locateBy, elementTag);

        StringBuilder code = new StringBuilder();
        code.append(String.format("            logger.info('Extracting content from element: %s')\n", selector));
        code.append(String.format("            %s\n", findElement));

        switch (extractType) {
            case "innerHTML" -> {
                code.append(String.format("            %s = _extract_elem.get_attribute('innerHTML')\n", varName));
            }
            case "outerHTML" -> {
                code.append(String.format("            %s = _extract_elem.get_attribute('outerHTML')\n", varName));
            }
            case "value" -> {
                code.append(String.format("            %s = _extract_elem.get_attribute('value')\n", varName));
            }
            default -> {
                // text
                code.append(String.format("            %s = _extract_elem.text\n", varName));
            }
        }

        code.append(String.format("            logger.info(f'Extracted content: {%s}')\n", varName));
        return code.toString();
    }

    private String generateConfirmDialog(JsonNode params) {
        String action = getParamText(params, "action", "accept");
        int waitTime = params.has("wait_time") ? params.get("wait_time").asInt(2) : 2;
        boolean ignoreError = params.has("ignore_error") && params.get("ignore_error").asBoolean(true);

        StringBuilder code = new StringBuilder();
        code.append(String.format("            logger.info('Handling confirm dialog (action: %s)')\n", action));
        code.append(String.format("            time.sleep(%d)\n", waitTime));
        code.append("            try:\n");
        code.append("                _confirm = self.driver.switch_to.alert\n");
        code.append("                logger.info(f'Confirm dialog detected, text: {_confirm.text}')\n");

        if ("dismiss".equals(action)) {
            code.append("                _confirm.dismiss()\n");
            code.append("                logger.info('Confirm dialog dismissed (cancelled)')\n");
        } else {
            code.append("                _confirm.accept()\n");
            code.append("                logger.info('Confirm dialog accepted (confirmed)')\n");
        }

        code.append("            except Exception as _e:\n");
        if (ignoreError) {
            code.append("                logger.warning(f'No confirm dialog found or operation failed: {_e}')\n");
        } else {
            code.append("                logger.error(f'Failed to handle confirm dialog: {_e}')\n");
            code.append("                raise\n");
        }
        return code.toString();
    }

    private String generateSendEmail(JsonNode params) {
        String to = getParamText(params, "to", "");
        String subject = getParamText(params, "subject", "");
        String body = getParamText(params, "body", "");
        String smtpHost = getParamText(params, "smtp_host", "smtp.gmail.com");
        String smtpPort = getParamText(params, "smtp_port", "587");
        String smtpUser = getParamText(params, "smtp_user", "");
        String smtpPass = getParamText(params, "smtp_pass", "");
        String smtpSsl = getParamText(params, "smtp_ssl", "tls");
        String proxyHost = getParamText(params, "proxy_host", "");
        String proxyPort = getParamText(params, "proxy_port", "");
        boolean useProxy = !proxyHost.isEmpty() && !proxyPort.isEmpty();

        StringBuilder code = new StringBuilder();
        code.append("            logger.info('Sending email...')\n");
        code.append(String.format("            _smtp_host = '%s'\n", smtpHost.replace("'", "\\'")));
        code.append(String.format("            _smtp_port = %s\n", smtpPort));
        code.append(String.format("            _smtp_user = '%s'\n", smtpUser.replace("'", "\\'")));
        code.append(String.format("            _smtp_pass = '%s'\n", smtpPass.replace("'", "\\'")));
        code.append("            logger.info(f'SMTP config: host={_smtp_host}, port={_smtp_port}, user={_smtp_user}')\n");
        // 支持在 body 和 subject 中用 {变量名} 引用前面提取的变量
        code.append(String.format("            _email_body = f'%s'\n", body.replace("'", "\\'")));
        code.append(String.format("            _email_subject = f'%s'\n", subject.replace("'", "\\'")));
        code.append(String.format("            _email_to = '%s'\n", to.replace("'", "\\'")));
        code.append("            logger.info(f'Email: to={_email_to}, subject={_email_subject}')\n");
        code.append("            _msg = MIMEText(_email_body, 'plain', 'utf-8')\n");
        code.append("            _msg['Subject'] = _email_subject\n");
        code.append("            _msg['From'] = _smtp_user\n");
        code.append("            _msg['To'] = _email_to\n");
        code.append("            try:\n");
        code.append("                import socket\n");
        code.append("                import ssl\n");

        if (useProxy) {
            // 通过代理发送
            code.append(String.format("                _proxy_host = '%s'\n", proxyHost.replace("'", "\\'")));
            code.append(String.format("                _proxy_port = %s\n", proxyPort));
            code.append("                logger.info(f'Using proxy: {_proxy_host}:{_proxy_port}')\n");
            code.append("                logger.info('Connecting to proxy...')\n");
            code.append("                _proxy_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)\n");
            code.append("                _proxy_sock.settimeout(30)\n");
            code.append("                _proxy_sock.connect((_proxy_host, _proxy_port))\n");
            code.append("                logger.info('Proxy connected, sending CONNECT request...')\n");
            code.append("                _connect_req = f'CONNECT {_smtp_host}:{_smtp_port} HTTP/1.1\\r\\nHost: {_smtp_host}:{_smtp_port}\\r\\n\\r\\n'\n");
            code.append("                _proxy_sock.sendall(_connect_req.encode())\n");
            code.append("                _proxy_resp = b''\n");
            code.append("                while b'\\r\\n\\r\\n' not in _proxy_resp:\n");
            code.append("                    _chunk = _proxy_sock.recv(4096)\n");
            code.append("                    if not _chunk:\n");
            code.append("                        break\n");
            code.append("                    _proxy_resp += _chunk\n");
            code.append("                _resp_line = _proxy_resp.split(b'\\r\\n')[0].decode()\n");
            code.append("                logger.info(f'Proxy response: {_resp_line}')\n");
            code.append("                if '200' not in _resp_line:\n");
            code.append("                    raise Exception(f'Proxy CONNECT failed: {_resp_line}')\n");
            code.append("                logger.info('Proxy tunnel established, starting SSL...')\n");
            code.append("                _ssl_context = ssl.create_default_context()\n");
            code.append("                _ssl_sock = _ssl_context.wrap_socket(_proxy_sock, server_hostname=_smtp_host)\n");
            code.append("                logger.info('SSL handshake completed')\n");
            code.append("                _server = smtplib.SMTP()\n");
            code.append("                _server.sock = _ssl_sock\n");
            code.append("                _server.file = _ssl_sock.makefile('rb')\n");
            code.append("                _code, _greeting = _server.getreply()\n");
            code.append("                logger.info(f'SMTP greeting: {_code} {_greeting}')\n");
            code.append("                _server.ehlo()\n");
            code.append("                logger.info('SMTP EHLO successful')\n");
        } else {
            // 直连模式
            if ("ssl".equals(smtpSsl)) {
                code.append("                _server = smtplib.SMTP_SSL(_smtp_host, _smtp_port, timeout=30)\n");
                code.append("                logger.info('Connected via SSL')\n");
            } else if ("tls".equals(smtpSsl)) {
                code.append("                _server = smtplib.SMTP(_smtp_host, _smtp_port, timeout=30)\n");
                code.append("                _server.ehlo()\n");
                code.append("                _server.starttls()\n");
                code.append("                _server.ehlo()\n");
                code.append("                logger.info('Connected via STARTTLS')\n");
            } else {
                code.append("                _server = smtplib.SMTP(_smtp_host, _smtp_port, timeout=30)\n");
                code.append("                _server.ehlo()\n");
                code.append("                logger.info('Connected (no encryption)')\n");
            }
        }

        code.append("                _server.login(_smtp_user, _smtp_pass)\n");
        code.append("                logger.info('SMTP login successful')\n");
        code.append("                _server.sendmail(_smtp_user, [_x.strip() for _x in _email_to.split(',')], _msg.as_string())\n");
        code.append("                _server.quit()\n");
        code.append("                logger.info('Email sent successfully')\n");
        code.append("            except smtplib.SMTPAuthenticationError as _e:\n");
        code.append("                logger.error(f'SMTP authentication failed: {_e}')\n");
        code.append("                raise\n");
        code.append("            except Exception as _e:\n");
        code.append("                logger.error(f'Failed to send email: {type(_e).__name__}: {_e}')\n");
        code.append("                raise\n");
        return code.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 将脚本的 if __name__ == '__main__' 部分替换为指定运行模式的代码。
     * 仅在打包时调用，"生成脚本"按钮始终生成单次模式。
     */
    public String wrapWithExecutionMode(String script, String mode, int restartDelay, int maxRetries,
                                         List<String> scheduleTimes, int intervalMinutes,
                                         boolean autoStart, boolean runHidden,
                                         String repeatMode, List<Integer> repeatDays,
                                         boolean rdpMode) {
        // 找到 if __name__ 块并替换
        String marker = "if __name__ == '__main__':";
        int idx = script.lastIndexOf(marker);
        if (idx < 0) {
            return script;
        }
        String prefix = script.substring(0, idx);
        String newMain;

        switch (mode) {
            case "daemon":
                newMain = generateDaemonMain(restartDelay, maxRetries);
                break;
            case "scheduled":
                if (scheduleTimes != null && !scheduleTimes.isEmpty()) {
                    newMain = generateScheduledTimesMain(scheduleTimes, restartDelay);
                } else {
                    newMain = generateScheduledIntervalMain(intervalMinutes);
                }
                break;
            case "scheduled_task":
                newMain = generateScheduledTaskMain(scheduleTimes, restartDelay, maxRetries, autoStart, runHidden, repeatMode, repeatDays, rdpMode);
                break;
            default:
                return script;
        }

        // 远程桌面模式：注入无头浏览器参数，确保不依赖 GUI 桌面会话
        if (rdpMode) {
            prefix = injectHeadlessMode(prefix);
        }

        return prefix + newMain;
    }

    /**
     * 在已生成的脚本中注入无头浏览器参数。
     * 将 Chrome/Edge 的 --disable-gpu 前插入 --headless=new，
     * 为 Firefox 插入 --headless，IE 不支持无头模式。
     */
    private String injectHeadlessMode(String script) {
        // Chrome / Edge：在 --disable-gpu 前插入 --headless=new
        script = script.replace(
                "_options.add_argument('--disable-gpu')",
                "_options.add_argument('--headless=new')\n            _options.add_argument('--disable-gpu')"
        );
        // Firefox：在 FirefoxOptions() 后插入 --headless
        script = script.replace(
                "_options = FirefoxOptions()\n",
                "_options = FirefoxOptions()\n            _options.add_argument('--headless')\n"
        );
        return script;
    }

    private String generateDaemonMain(int restartDelay, int maxRetries) {
        StringBuilder sb = new StringBuilder();
        sb.append("if __name__ == '__main__':\n");
        sb.append("    import signal\n");
        sb.append("    _MAX_RETRIES = ").append(maxRetries).append("\n");
        sb.append("    _RESTART_DELAY = ").append(restartDelay).append("\n");
        sb.append("    _retry_count = 0\n");
        sb.append("    _running = True\n");
        sb.append("    def _signal_handler(sig, frame):\n");
        sb.append("        global _running\n");
        sb.append("        logger.info('收到终止信号，正在退出...')\n");
        sb.append("        _running = False\n");
        sb.append("    signal.signal(signal.SIGINT, _signal_handler)\n");
        sb.append("    signal.signal(signal.SIGTERM, _signal_handler)\n");
        sb.append("    logger.info(f'守护模式启动 (重启延迟={_RESTART_DELAY}s, 最大重试={_MAX_RETRIES or \"无限\"})')\n");
        sb.append("    while _running:\n");
        sb.append("        try:\n");
        sb.append("            script = AutomationScript()\n");
        sb.append("            script.run()\n");
        sb.append("            logger.info('执行完成，等待后重新执行...')\n");
        sb.append("            _retry_count = 0\n");
        sb.append("        except Exception as e:\n");
        sb.append("            _retry_count += 1\n");
        sb.append("            logger.error(f'执行失败 (第{_retry_count}次): {e}')\n");
        sb.append("            if _MAX_RETRIES > 0 and _retry_count >= _MAX_RETRIES:\n");
        sb.append("                logger.error('已达最大重试次数，退出')\n");
        sb.append("                break\n");
        sb.append("        if _running:\n");
        sb.append("            time.sleep(_RESTART_DELAY)\n");
        return sb.toString();
    }

    private String generateScheduledTimesMain(List<String> times, int restartDelay) {
        StringBuilder sb = new StringBuilder();
        // 构建时间列表字符串
        StringBuilder timesList = new StringBuilder("[");
        for (int i = 0; i < times.size(); i++) {
            if (i > 0) timesList.append(", ");
            timesList.append("'").append(times.get(i)).append("'");
        }
        timesList.append("]");

        sb.append("if __name__ == '__main__':\n");
        sb.append("    import datetime\n");
        sb.append("    import signal\n");
        sb.append("    _SCHEDULE_TIMES = ").append(timesList).append("\n");
        sb.append("    _RESTART_DELAY = ").append(restartDelay).append("\n");
        sb.append("    _running = True\n");
        sb.append("    def _signal_handler(sig, frame):\n");
        sb.append("        global _running\n");
        sb.append("        logger.info('收到终止信号，正在退出...')\n");
        sb.append("        _running = False\n");
        sb.append("    signal.signal(signal.SIGINT, _signal_handler)\n");
        sb.append("    signal.signal(signal.SIGTERM, _signal_handler)\n");
        sb.append("    def _next_run_time():\n");
        sb.append("        now = datetime.datetime.now()\n");
        sb.append("        candidates = []\n");
        sb.append("        for t in _SCHEDULE_TIMES:\n");
        sb.append("            h, m = map(int, t.split(':'))\n");
        sb.append("            s = now.replace(hour=h, minute=m, second=0, microsecond=0)\n");
        sb.append("            if s > now:\n");
        sb.append("                candidates.append(s)\n");
        sb.append("        if not candidates:\n");
        sb.append("            h, m = map(int, sorted(_SCHEDULE_TIMES)[0].split(':'))\n");
        sb.append("            return (now + datetime.timedelta(days=1)).replace(hour=h, minute=m, second=0, microsecond=0)\n");
        sb.append("        return min(candidates)\n");
        sb.append("    logger.info(f'定时模式启动，执行时间: {_SCHEDULE_TIMES}')\n");
        sb.append("    while _running:\n");
        sb.append("        nxt = _next_run_time()\n");
        sb.append("        logger.info(f'下次执行: {nxt.strftime(\"%Y-%m-%d %H:%M\")}')\n");
        sb.append("        while _running and datetime.datetime.now() < nxt:\n");
        sb.append("            time.sleep(30)\n");
        sb.append("        if not _running: break\n");
        sb.append("        try:\n");
        sb.append("            script = AutomationScript()\n");
        sb.append("            script.run()\n");
        sb.append("        except Exception as e:\n");
        sb.append("            logger.error(f'定时执行失败: {e}')\n");
        sb.append("            time.sleep(_RESTART_DELAY)\n");
        return sb.toString();
    }

    private String generateScheduledIntervalMain(int intervalMinutes) {
        int intervalSeconds = intervalMinutes * 60;
        StringBuilder sb = new StringBuilder();
        sb.append("if __name__ == '__main__':\n");
        sb.append("    import signal\n");
        sb.append("    _INTERVAL_SECONDS = ").append(intervalSeconds).append("\n");
        sb.append("    _running = True\n");
        sb.append("    def _signal_handler(sig, frame):\n");
        sb.append("        global _running\n");
        sb.append("        logger.info('收到终止信号，正在退出...')\n");
        sb.append("        _running = False\n");
        sb.append("    signal.signal(signal.SIGINT, _signal_handler)\n");
        sb.append("    signal.signal(signal.SIGTERM, _signal_handler)\n");
        sb.append("    logger.info(f'间隔模式启动，每{_INTERVAL_SECONDS // 60}分钟执行一次')\n");
        sb.append("    while _running:\n");
        sb.append("        try:\n");
        sb.append("            script = AutomationScript()\n");
        sb.append("            script.run()\n");
        sb.append("        except Exception as e:\n");
        sb.append("            logger.error(f'执行失败: {e}')\n");
        sb.append("        for _ in range(_INTERVAL_SECONDS // 30):\n");
        sb.append("            if not _running: break\n");
        sb.append("            time.sleep(30)\n");
        return sb.toString();
    }

    private String generateScheduledTaskMain(List<String> scheduleTimes, int restartDelay, int maxRetries,
                                              boolean autoStart, boolean runHidden,
                                              String repeatMode, List<Integer> repeatDays,
                                              boolean rdpMode) {
        StringBuilder sb = new StringBuilder();

        // 构建时间列表字符串
        StringBuilder timesList = new StringBuilder("[");
        if (scheduleTimes != null) {
            for (int i = 0; i < scheduleTimes.size(); i++) {
                if (i > 0) timesList.append(", ");
                timesList.append("'").append(scheduleTimes.get(i)).append("'");
            }
        }
        timesList.append("]");

        sb.append("if __name__ == '__main__':\n");
        sb.append("    import datetime\n");
        sb.append("    import signal\n");

        // 隐藏控制台窗口
        if (runHidden) {
            sb.append("    # 隐藏控制台窗口\n");
            sb.append("    try:\n");
            sb.append("        import ctypes\n");
            sb.append("        ctypes.windll.user32.ShowWindow(ctypes.windll.kernel32.GetConsoleWindow(), 0)\n");
            sb.append("    except Exception:\n");
            sb.append("        pass\n");
        }

        // 开机自启动注册
        if (autoStart) {
            sb.append("    # 注册开机自启动\n");
            sb.append("    try:\n");
            sb.append("        import winreg\n");
            sb.append("        _exe_path = os.path.abspath(sys.argv[0])\n");
            sb.append("        _app_name = os.path.splitext(os.path.basename(_exe_path))[0]\n");
            sb.append("        _reg_key = winreg.OpenKey(winreg.HKEY_CURRENT_USER,\n");
            sb.append("            r'Software\\Microsoft\\Windows\\CurrentVersion\\Run', 0, winreg.KEY_SET_VALUE)\n");
            sb.append("        winreg.SetValueEx(_reg_key, _app_name, 0, winreg.REG_SZ, _exe_path)\n");
            sb.append("        winreg.CloseKey(_reg_key)\n");
            sb.append("        logger.info(f'已注册开机自启动: {_app_name}')\n");
            sb.append("    except Exception as e:\n");
            sb.append("        logger.warning(f'注册开机自启动失败: {e}')\n");
        }

        // 远程桌面模式：保持 RDP 断开后会话存活
        if (rdpMode) {
            sb.append("\n    # ====== 远程桌面保活 ======\n");
            // 1. 阻止屏幕锁定和休眠（SetThreadExecutionState）
            sb.append("    try:\n");
            sb.append("        import ctypes\n");
            sb.append("        # ES_CONTINUOUS | ES_SYSTEM_REQUIRED | ES_DISPLAY_REQUIRED\n");
            sb.append("        ctypes.windll.kernel32.SetThreadExecutionState(0x80000002 | 0x00000002 | 0x00000001)\n");
            sb.append("        logger.info('已设置系统保活：阻止休眠和屏幕关闭')\n");
            sb.append("    except Exception as _e:\n");
            sb.append("        logger.warning(f'设置系统保活失败: {_e}')\n");

            // 2. 禁用锁屏（注册表）
            sb.append("    try:\n");
            sb.append("        import winreg\n");
            sb.append("        _pol_key = winreg.CreateKeyEx(winreg.HKEY_CURRENT_USER,\n");
            sb.append("            r'Software\\Policies\\Microsoft\\Windows\\Personalization', 0, winreg.KEY_SET_VALUE)\n");
            sb.append("        winreg.SetValueEx(_pol_key, 'NoLockScreen', 0, winreg.REG_DWORD, 1)\n");
            sb.append("        winreg.CloseKey(_pol_key)\n");
            sb.append("        logger.info('已禁用锁屏')\n");
            sb.append("    except Exception as _e:\n");
            sb.append("        logger.warning(f'禁用锁屏失败（可能需要管理员权限）: {_e}')\n");

            // 3. 生成安全断开脚本
            sb.append("    try:\n");
            sb.append("        _desktop = os.path.join(os.path.expanduser('~'), 'Desktop')\n");
            sb.append("        _bat_path = os.path.join(_desktop, '安全断开远程桌面.bat')\n");
            sb.append("        if not os.path.exists(_bat_path):\n");
            sb.append("            with open(_bat_path, 'w', encoding='gbk') as _f:\n");
            sb.append("                _f.write('@echo off\\n')\n");
            sb.append("                _f.write('echo 正在安全断开远程桌面（保持后台程序运行）...\\n')\n");
            sb.append("                _f.write('for /f \"skip=1 tokens=3\" %%s in (\\'query user\\') do (\\n')\n");
            sb.append("                _f.write('    tscon %%s /dest:console\\n')\n");
            sb.append("                _f.write('    goto :done\\n')\n");
            sb.append("                _f.write(')\\n')\n");
            sb.append("                _f.write(':done\\n')\n");
            sb.append("                _f.write('echo 已断开，后台程序将继续运行。\\n')\n");
            sb.append("            logger.info(f'已生成安全断开脚本: {_bat_path}')\n");
            sb.append("    except Exception as _e:\n");
            sb.append("        logger.warning(f'生成断开脚本失败: {_e}')\n");

            // 4. 后台线程定期防止会话超时
            sb.append("    import threading\n");
            sb.append("    def _rdp_keep_alive():\n");
            sb.append("        while _running:\n");
            sb.append("            try:\n");
            sb.append("                ctypes.windll.kernel32.SetThreadExecutionState(0x80000002 | 0x00000002 | 0x00000001)\n");
            sb.append("            except Exception:\n");
            sb.append("                pass\n");
            sb.append("            for _ in range(12):\n");
            sb.append("                if not _running: break\n");
            sb.append("                time.sleep(5)\n");
            sb.append("    _keep_alive_thread = threading.Thread(target=_rdp_keep_alive, daemon=True)\n");
            sb.append("    _keep_alive_thread.start()\n");
            sb.append("    logger.info('远程桌面保活线程已启动')\n");
            sb.append("    # ====== 远程桌面保活结束 ======\n\n");
        }

        sb.append("    _SCHEDULE_TIMES = ").append(timesList).append("\n");
        sb.append("    _RESTART_DELAY = ").append(restartDelay).append("\n");
        sb.append("    _MAX_RETRIES = ").append(maxRetries).append("\n");

        // 重复模式: once=不重复, daily=每天, weekdays=工作日, weekly=自定义星期
        String safeRepeatMode = (repeatMode == null || repeatMode.isEmpty()) ? "daily" : repeatMode;
        sb.append("    _REPEAT_MODE = '").append(safeRepeatMode).append("'\n");

        // 自定义星期列表 (Python weekday: 0=周一, 6=周日)
        StringBuilder daysList = new StringBuilder("[");
        if (repeatDays != null && !repeatDays.isEmpty()) {
            for (int i = 0; i < repeatDays.size(); i++) {
                if (i > 0) daysList.append(", ");
                daysList.append(repeatDays.get(i));
            }
        }
        daysList.append("]");
        sb.append("    _REPEAT_DAYS = ").append(daysList).append("\n");

        sb.append("    _running = True\n");
        sb.append("    _retry_count = 0\n");
        sb.append("    if not _SCHEDULE_TIMES:\n");
        sb.append("        logger.error('未配置执行时间点，退出')\n");
        sb.append("        sys.exit(1)\n");
        sb.append("    def _signal_handler(sig, frame):\n");
        sb.append("        global _running\n");
        sb.append("        logger.info('收到终止信号，正在退出...')\n");
        sb.append("        _running = False\n");
        sb.append("    signal.signal(signal.SIGINT, _signal_handler)\n");
        sb.append("    signal.signal(signal.SIGTERM, _signal_handler)\n");

        // _is_run_day(): 判断指定日期是否应该执行
        sb.append("    def _is_run_day(dt):\n");
        sb.append("        wd = dt.weekday()\n");
        sb.append("        if _REPEAT_MODE == 'daily': return True\n");
        sb.append("        if _REPEAT_MODE == 'weekdays': return wd < 5\n");
        sb.append("        if _REPEAT_MODE == 'weekly': return wd in _REPEAT_DAYS\n");
        sb.append("        return True\n");

        // _next_run_time(): 找到下一个符合条件的执行时间
        sb.append("    def _next_run_time():\n");
        sb.append("        now = datetime.datetime.now()\n");
        sb.append("        # 检查今天剩余的时间点\n");
        sb.append("        if _is_run_day(now):\n");
        sb.append("            for t in sorted(_SCHEDULE_TIMES):\n");
        sb.append("                h, m = map(int, t.split(':'))\n");
        sb.append("                s = now.replace(hour=h, minute=m, second=0, microsecond=0)\n");
        sb.append("                if s > now:\n");
        sb.append("                    return s\n");
        sb.append("        # 向后查找最多7天\n");
        sb.append("        for d in range(1, 8):\n");
        sb.append("            nxt_day = now + datetime.timedelta(days=d)\n");
        sb.append("            if _is_run_day(nxt_day):\n");
        sb.append("                h, m = map(int, sorted(_SCHEDULE_TIMES)[0].split(':'))\n");
        sb.append("                return nxt_day.replace(hour=h, minute=m, second=0, microsecond=0)\n");
        sb.append("        # 兜底：明天最早时间\n");
        sb.append("        h, m = map(int, sorted(_SCHEDULE_TIMES)[0].split(':'))\n");
        sb.append("        return (now + datetime.timedelta(days=1)).replace(hour=h, minute=m, second=0, microsecond=0)\n");

        // once 模式标签
        sb.append("    _WEEKDAY_NAMES = ['周一','周二','周三','周四','周五','周六','周日']\n");
        sb.append("    if _REPEAT_MODE == 'once':\n");
        sb.append("        logger.info(f'单次定时模式，执行时间: {_SCHEDULE_TIMES}')\n");
        sb.append("    elif _REPEAT_MODE == 'weekdays':\n");
        sb.append("        logger.info(f'工作日定时模式，执行时间: {_SCHEDULE_TIMES}')\n");
        sb.append("    elif _REPEAT_MODE == 'weekly':\n");
        sb.append("        _day_names = [_WEEKDAY_NAMES[d] for d in _REPEAT_DAYS]\n");
        sb.append("        logger.info(f'每周 {\",\".join(_day_names)} 定时执行: {_SCHEDULE_TIMES}')\n");
        sb.append("    else:\n");
        sb.append("        logger.info(f'每天定时模式，执行时间: {_SCHEDULE_TIMES}')\n");

        sb.append("    while _running:\n");
        sb.append("        nxt = _next_run_time()\n");
        sb.append("        logger.info(f'下次执行: {nxt.strftime(\"%Y-%m-%d %H:%M %A\")}')\n");
        sb.append("        while _running and datetime.datetime.now() < nxt:\n");
        sb.append("            time.sleep(30)\n");
        sb.append("        if not _running: break\n");
        sb.append("        try:\n");
        sb.append("            script = AutomationScript()\n");
        sb.append("            script.run()\n");
        sb.append("            _retry_count = 0\n");
        sb.append("        except Exception as e:\n");
        sb.append("            _retry_count += 1\n");
        sb.append("            logger.error(f'定时执行失败 (第{_retry_count}次): {e}')\n");
        sb.append("            if _MAX_RETRIES > 0 and _retry_count >= _MAX_RETRIES:\n");
        sb.append("                logger.error('已达最大重试次数，退出')\n");
        sb.append("                break\n");
        sb.append("            time.sleep(_RESTART_DELAY)\n");
        // once 模式：执行完一轮后退出
        sb.append("        if _REPEAT_MODE == 'once':\n");
        sb.append("            logger.info('单次模式执行完毕，退出')\n");
        sb.append("            break\n");

        return sb.toString();
    }
}
