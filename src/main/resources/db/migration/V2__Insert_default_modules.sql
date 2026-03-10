INSERT INTO modules (module_id, name, category, description, input_schema, output_schema, python_template) VALUES
('open_browser', 'Open Browser', 'basic', 'Open a web browser and navigate to URL',
 '{"browser_type": "select", "url": "text"}',
 '{"driver": "object"}',
 'self.driver = webdriver.{browser_type}()\nself.driver.get("{url}")'),

('input_text', 'Input Text', 'basic', 'Input text into an element',
 '{"selector": "text", "text": "text"}',
 '{}',
 'element = self.driver.find_element(By.CSS_SELECTOR, "{selector}")\nelement.clear()\nelement.send_keys("{text}")'),

('click_element', 'Click Element', 'basic', 'Click on an element',
 '{"selector": "text"}',
 '{}',
 'element = self.driver.find_element(By.CSS_SELECTOR, "{selector}")\nelement.click()'),

('wait_element', 'Wait for Element', 'basic', 'Wait for element to appear',
 '{"selector": "text", "timeout": "number"}',
 '{}',
 'WebDriverWait(self.driver, {timeout}).until(EC.presence_of_element_located((By.CSS_SELECTOR, "{selector}")))'),

('get_text', 'Get Text', 'advanced', 'Get text from element',
 '{"selector": "text", "var_name": "text"}',
 '{"text": "string"}',
 '{var_name} = self.driver.find_element(By.CSS_SELECTOR, "{selector}").text'),

('screenshot', 'Take Screenshot', 'advanced', 'Take a screenshot',
 '{"filename": "text"}',
 '{}',
 'self.driver.save_screenshot("{filename}")');
