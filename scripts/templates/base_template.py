#!/usr/bin/env python3
"""
Base template for generated automation scripts
"""
import sys
import logging
import time
from datetime import datetime
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.firefox.options import Options as FirefoxOptions

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class AutomationScript:
    def __init__(self):
        self.driver = None
        self.wait = None
        self.execution_start = datetime.now()

    def setup_driver(self, browser_type='chrome', headless=False):
        """Initialize webdriver based on browser type"""
        try:
            if browser_type.lower() == 'chrome':
                options = ChromeOptions()
                if headless:
                    options.add_argument('--headless')
                options.add_argument('--no-sandbox')
                options.add_argument('--disable-dev-shm-usage')
                self.driver = webdriver.Chrome(options=options)
            elif browser_type.lower() == 'firefox':
                options = FirefoxOptions()
                if headless:
                    options.add_argument('--headless')
                self.driver = webdriver.Firefox(options=options)
            else:
                raise ValueError(f"Unsupported browser: {browser_type}")

            self.wait = WebDriverWait(self.driver, 10)
            logger.info(f"Browser {browser_type} initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize browser: {str(e)}")
            raise

    def navigate(self, url):
        """Navigate to URL"""
        try:
            logger.info(f"Navigating to {url}")
            self.driver.get(url)
            time.sleep(2)
        except Exception as e:
            logger.error(f"Failed to navigate to {url}: {str(e)}")
            raise

    def find_element(self, selector, by=By.CSS_SELECTOR, timeout=10):
        """Find element with wait"""
        try:
            element = WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located((by, selector))
            )
            return element
        except Exception as e:
            logger.error(f"Failed to find element {selector}: {str(e)}")
            raise

    def click_element(self, selector, by=By.CSS_SELECTOR):
        """Click on element"""
        try:
            element = self.find_element(selector, by)
            element.click()
            logger.info(f"Clicked element: {selector}")
            time.sleep(1)
        except Exception as e:
            logger.error(f"Failed to click element {selector}: {str(e)}")
            raise

    def input_text(self, selector, text, by=By.CSS_SELECTOR):
        """Input text into element"""
        try:
            element = self.find_element(selector, by)
            element.clear()
            element.send_keys(text)
            logger.info(f"Inputted text to {selector}")
        except Exception as e:
            logger.error(f"Failed to input text to {selector}: {str(e)}")
            raise

    def get_text(self, selector, by=By.CSS_SELECTOR):
        """Get text from element"""
        try:
            element = self.find_element(selector, by)
            text = element.text
            logger.info(f"Got text from {selector}: {text}")
            return text
        except Exception as e:
            logger.error(f"Failed to get text from {selector}: {str(e)}")
            raise

    def take_screenshot(self, filename='screenshot.png'):
        """Take screenshot"""
        try:
            self.driver.save_screenshot(filename)
            logger.info(f"Screenshot saved: {filename}")
        except Exception as e:
            logger.error(f"Failed to take screenshot: {str(e)}")
            raise

    def wait_for_element(self, selector, timeout=10, by=By.CSS_SELECTOR):
        """Wait for element to appear"""
        try:
            WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located((by, selector))
            )
            logger.info(f"Element appeared: {selector}")
        except Exception as e:
            logger.error(f"Timeout waiting for element {selector}: {str(e)}")
            raise

    def execute(self):
        """Main execution method - override in subclass"""
        raise NotImplementedError("Subclass must implement execute method")

    def run(self):
        """Run the automation script"""
        try:
            logger.info("Starting automation script")
            self.execute()
            logger.info("Script execution completed successfully")
            return True
        except Exception as e:
            logger.error(f"Script execution failed: {str(e)}")
            return False
        finally:
            if self.driver:
                self.driver.quit()
                logger.info("Browser closed")


if __name__ == '__main__':
    script = AutomationScript()
    success = script.run()
    sys.exit(0 if success else 1)
