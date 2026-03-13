package com.example.autooperation.init;

import com.example.autooperation.model.Module;
import com.example.autooperation.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModuleInitializer implements CommandLineRunner {
    private final ModuleRepository moduleRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Module> allModules = Arrays.asList(
                createModule("open_browser", "打开浏览器", "basic", "打开指定浏览器并导航到URL",
                        "{\"browser_type\": \"select\", \"url\": \"text\"}",
                        "{\"driver\": \"object\"}",
                        "if \"{browser_type}\" == \"ie\":\n    from selenium.webdriver.ie.service import Service\n    self.driver = webdriver.Ie()\nelse:\n    self.driver = webdriver.{browser_type}()\nself.driver.get(\"{url}\")"),

                createModule("input_account", "输入账号密码", "basic", "在登录表单中输入账号和密码",
                        "{\"locate_by\": \"select\", \"username_selector\": \"text\", \"username\": \"text\", \"password_selector\": \"text\", \"password\": \"text\"}",
                        "{}",
                        "username_elem = self.driver.find_element(By.CSS_SELECTOR, \"{username_selector}\")\nusername_elem.send_keys(\"{username}\")\npassword_elem = self.driver.find_element(By.CSS_SELECTOR, \"{password_selector}\")\npassword_elem.send_keys(\"{password}\")"),

                createModule("click_button", "点击按钮", "basic", "点击页面上的按钮或链接",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"wait_time\": \"number\"}",
                        "{}",
                        "element = self.driver.find_element(By.CSS_SELECTOR, \"{selector}\")\nelement.click()\ntime.sleep({wait_time})"),

                createModule("navigate_to_url", "跳转页面", "basic", "导航到指定URL地址",
                        "{\"url\": \"text\", \"wait_time\": \"number\"}",
                        "{}",
                        "self.driver.get(\"{url}\")\ntime.sleep({wait_time})"),

                createModule("input_text", "输入内容", "basic", "在输入框中输入文本内容",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"text\": \"text\", \"clear_first\": \"boolean\"}",
                        "{}",
                        "element = self.driver.find_element(By.CSS_SELECTOR, \"{selector}\")\nif {clear_first}:\n    element.clear()\nelement.send_keys(\"{text}\")"),

                createModule("select_dropdown", "下拉框选择", "basic", "从下拉框中选择指定选项",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"value\": \"text\", \"select_by\": \"select\"}",
                        "{}",
                        "element = self.driver.find_element(By.CSS_SELECTOR, \"{selector}\")\nselect = Select(element)\nif \"{select_by}\" == \"value\":\n    select.select_by_value(\"{value}\")\nelse:\n    select.select_by_visible_text(\"{value}\")"),

                createModule("wait_element", "等待元素", "basic", "等待页面元素出现",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"timeout\": \"number\"}",
                        "{}",
                        "WebDriverWait(self.driver, {timeout}).until(\n    EC.presence_of_element_located((By.CSS_SELECTOR, \"{selector}\"))\n)"),

                createModule("get_text", "获取文本", "advanced", "获取页面元素的文本内容",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"var_name\": \"text\"}",
                        "{\"text\": \"string\"}",
                        "{var_name} = self.driver.find_element(By.CSS_SELECTOR, \"{selector}\").text"),

                createModule("screenshot", "截图", "advanced", "保存当前页面的截图",
                        "{\"filename\": \"text\"}",
                        "{}",
                        "self.driver.save_screenshot(\"{filename}\")"),

                createModule("hover_element", "悬停元素", "advanced", "鼠标悬停在元素上",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\"}",
                        "{}",
                        "element = self.driver.find_element(By.CSS_SELECTOR, \"{selector}\")\nActionChains(self.driver).move_to_element(element).perform()"),

                createModule("double_click", "双击元素", "advanced", "双击页面元素",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\"}",
                        "{}",
                        "element = self.driver.find_element(By.CSS_SELECTOR, \"{selector}\")\nActionChains(self.driver).double_click(element).perform()"),

                createModule("scroll_to", "滚动到元素", "advanced", "滚动页面使元素可见",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\"}",
                        "{}",
                        "element = self.driver.find_element(By.CSS_SELECTOR, \"{selector}\")\nself.driver.execute_script(\"arguments[0].scrollIntoView(true);\", element)"),

                createModule("get_attribute", "获取属性", "advanced", "获取元素的属性值",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"attribute\": \"text\", \"var_name\": \"text\"}",
                        "{\"value\": \"string\"}",
                        "{var_name} = self.driver.find_element(By.CSS_SELECTOR, \"{selector}\").get_attribute(\"{attribute}\")"),

                createModule("close_popup", "关闭弹窗", "basic", "关闭页面弹窗或对话框，支持单次关闭和全程监控两种模式",
                        "{\"monitor_mode\": \"select\", \"close_method\": \"select\", \"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"wait_time\": \"number\", \"ignore_error\": \"boolean\", \"custom_selectors\": \"text\"}",
                        "{}",
                        "# close popup"),

                createModule("batch_input", "批量输入", "basic", "在一个页面上同时填写多个输入框（可动态添加输入项）",
                        "{\"locate_by\": \"select\", \"element_tag\": \"select\", \"inputs\": \"input_list\"}",
                        "{}",
                        "# batch input"),

                createModule("switch_frame", "切换Frame", "basic", "切换到iframe/frame内部操作元素，或切回主页面",
                        "{\"frame_action\": \"select\", \"frame_locator\": \"text\"}",
                        "{}",
                        "# switch frame"),

                createModule("press_enter", "按回车", "basic", "在当前焦点元素或指定元素上按一次回车键",
                        "{\"target\": \"select\", \"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\"}",
                        "{}",
                        "# press enter"),

                createModule("alert_confirm", "Alert确认", "basic", "点击浏览器Alert弹窗的确认按钮",
                        "{\"wait_time\": \"number\", \"ignore_error\": \"boolean\"}",
                        "{}",
                        "# alert confirm"),

                createModule("extract_content", "提取内容", "advanced", "通过CSS选择器提取页面元素中显示的文本内容",
                        "{\"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"var_name\": \"text\", \"extract_type\": \"select\"}",
                        "{\"content\": \"string\"}",
                        "# extract content"),

                createModule("confirm_dialog", "Confirm对话框", "basic", "处理浏览器Confirm对话框，支持点击确认或取消按钮",
                        "{\"action\": \"select\", \"wait_time\": \"number\", \"ignore_error\": \"boolean\"}",
                        "{}",
                        "# confirm dialog"),

                createModule("send_email", "发送邮件", "advanced", "通过SMTP发送邮件，支持配置收件人、主题和正文，可设置SMTP服务器和代理",
                        "{\"smtp_host\": \"text\", \"smtp_port\": \"text\", \"smtp_user\": \"text\", \"smtp_pass\": \"text\", \"smtp_ssl\": \"select\", \"proxy_host\": \"text\", \"proxy_port\": \"text\", \"to\": \"text\", \"subject\": \"text\", \"body\": \"text\"}",
                        "{}",
                        "# send email"),

                createModule("error_monitor", "错误监控", "basic", "全程监控脚本执行，捕获每一步异常并记录到桌面日志文件",
                        "{\"log_folder\": \"text\"}",
                        "{}",
                        "# error monitor"),

                createModule("scheduled_task", "定时执行", "advanced", "将程序设为定时运行的守护进程，支持开机自启和后台运行",
                        "{\"repeat_mode\": \"select\", \"repeat_days\": \"text\", \"schedule_times\": \"text\", \"restart_delay\": \"text\", \"max_retries\": \"text\", \"auto_start\": \"select\", \"run_hidden\": \"select\", \"rdp_mode\": \"select\"}",
                        "{}",
                        "# scheduled task")
        );

        if (moduleRepository.count() == 0) {
            log.info("初始化功能模块...");
            moduleRepository.saveAll(allModules);
            log.info("功能模块初始化完成，共 {} 个模块", allModules.size());
        } else {
            // 增量补充：检查是否有新模块需要添加
            int added = 0;
            for (Module module : allModules) {
                if (moduleRepository.findByModuleId(module.getModuleId()).isEmpty()) {
                    moduleRepository.save(module);
                    log.info("新增功能模块: {}", module.getModuleId());
                    added++;
                }
            }
            if (added > 0) {
                log.info("增量补充完成，新增 {} 个模块", added);
            }

            // 清理已废弃的模块
            List<String> deprecatedModules = List.of("auto_dismiss_popup");
            for (String deprecatedId : deprecatedModules) {
                moduleRepository.findByModuleId(deprecatedId).ifPresent(m -> {
                    moduleRepository.delete(m);
                    log.info("已移除废弃模块: {}", deprecatedId);
                });
            }

            // 更新已有模块的 schema（close_popup 合并了全程监控功能）
            moduleRepository.findByModuleId("close_popup").ifPresent(m -> {
                String newSchema = "{\"monitor_mode\": \"select\", \"close_method\": \"select\", \"locate_by\": \"select\", \"selector\": \"text\", \"element_tag\": \"select\", \"wait_time\": \"number\", \"ignore_error\": \"boolean\", \"custom_selectors\": \"text\"}";
                if (!newSchema.equals(m.getInputSchema())) {
                    m.setInputSchema(newSchema);
                    m.setDescription("关闭页面弹窗或对话框，支持单次关闭和全程监控两种模式");
                    moduleRepository.save(m);
                    log.info("已更新模块 schema: close_popup");
                }
            });

            // 更新 send_email 模块：新增 SMTP 和代理配置字段
            moduleRepository.findByModuleId("send_email").ifPresent(m -> {
                String newSchema = "{\"smtp_host\": \"text\", \"smtp_port\": \"text\", \"smtp_user\": \"text\", \"smtp_pass\": \"text\", \"smtp_ssl\": \"select\", \"proxy_host\": \"text\", \"proxy_port\": \"text\", \"to\": \"text\", \"subject\": \"text\", \"body\": \"text\"}";
                if (!newSchema.equals(m.getInputSchema())) {
                    m.setInputSchema(newSchema);
                    m.setDescription("通过SMTP发送邮件，支持配置收件人、主题和正文，可设置SMTP服务器和代理");
                    moduleRepository.save(m);
                    log.info("已更新模块 schema: send_email");
                }
            });

            // 更新 scheduled_task 模块 schema
            moduleRepository.findByModuleId("scheduled_task").ifPresent(m -> {
                String newSchema = "{\"repeat_mode\": \"select\", \"repeat_days\": \"text\", \"schedule_times\": \"text\", \"restart_delay\": \"text\", \"max_retries\": \"text\", \"auto_start\": \"select\", \"run_hidden\": \"select\", \"rdp_mode\": \"select\"}";
                if (!newSchema.equals(m.getInputSchema())) {
                    m.setInputSchema(newSchema);
                    moduleRepository.save(m);
                    log.info("已更新模块 schema: scheduled_task");
                }
            });
        }
    }

    private Module createModule(String moduleId, String name, String category, String description,
                               String inputSchema, String outputSchema, String pythonTemplate) {
        Module module = new Module();
        module.setModuleId(moduleId);
        module.setName(name);
        module.setCategory(category);
        module.setDescription(description);
        module.setInputSchema(inputSchema);
        module.setOutputSchema(outputSchema);
        module.setPythonTemplate(pythonTemplate);
        module.setBuiltIn(true);
        return module;
    }
}
