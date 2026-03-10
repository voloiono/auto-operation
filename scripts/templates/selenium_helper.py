"""
Selenium helper functions for automation scripts
"""
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.support.ui import Select
import logging

logger = logging.getLogger(__name__)


class SeleniumHelper:
    @staticmethod
    def select_dropdown(driver, selector, value, by=By.CSS_SELECTOR):
        """Select option from dropdown"""
        try:
            element = driver.find_element(by, selector)
            select = Select(element)
            select.select_by_value(value)
            logger.info(f"Selected dropdown value: {value}")
        except Exception as e:
            logger.error(f"Failed to select dropdown: {str(e)}")
            raise

    @staticmethod
    def select_dropdown_by_text(driver, selector, text, by=By.CSS_SELECTOR):
        """Select option from dropdown by visible text"""
        try:
            element = driver.find_element(by, selector)
            select = Select(element)
            select.select_by_visible_text(text)
            logger.info(f"Selected dropdown text: {text}")
        except Exception as e:
            logger.error(f"Failed to select dropdown by text: {str(e)}")
            raise

    @staticmethod
    def hover_element(driver, selector, by=By.CSS_SELECTOR):
        """Hover over element"""
        try:
            element = driver.find_element(by, selector)
            ActionChains(driver).move_to_element(element).perform()
            logger.info(f"Hovered over element: {selector}")
        except Exception as e:
            logger.error(f"Failed to hover element: {str(e)}")
            raise

    @staticmethod
    def double_click(driver, selector, by=By.CSS_SELECTOR):
        """Double click element"""
        try:
            element = driver.find_element(by, selector)
            ActionChains(driver).double_click(element).perform()
            logger.info(f"Double clicked element: {selector}")
        except Exception as e:
            logger.error(f"Failed to double click: {str(e)}")
            raise

    @staticmethod
    def right_click(driver, selector, by=By.CSS_SELECTOR):
        """Right click element"""
        try:
            element = driver.find_element(by, selector)
            ActionChains(driver).context_click(element).perform()
            logger.info(f"Right clicked element: {selector}")
        except Exception as e:
            logger.error(f"Failed to right click: {str(e)}")
            raise

    @staticmethod
    def scroll_to_element(driver, selector, by=By.CSS_SELECTOR):
        """Scroll to element"""
        try:
            element = driver.find_element(by, selector)
            driver.execute_script("arguments[0].scrollIntoView(true);", element)
            logger.info(f"Scrolled to element: {selector}")
        except Exception as e:
            logger.error(f"Failed to scroll to element: {str(e)}")
            raise

    @staticmethod
    def get_attribute(driver, selector, attribute, by=By.CSS_SELECTOR):
        """Get element attribute"""
        try:
            element = driver.find_element(by, selector)
            value = element.get_attribute(attribute)
            logger.info(f"Got attribute {attribute}: {value}")
            return value
        except Exception as e:
            logger.error(f"Failed to get attribute: {str(e)}")
            raise

    @staticmethod
    def is_element_visible(driver, selector, by=By.CSS_SELECTOR):
        """Check if element is visible"""
        try:
            element = driver.find_element(by, selector)
            return element.is_displayed()
        except Exception:
            return False

    @staticmethod
    def switch_to_frame(driver, selector, by=By.CSS_SELECTOR):
        """Switch to iframe"""
        try:
            frame = driver.find_element(by, selector)
            driver.switch_to.frame(frame)
            logger.info(f"Switched to frame: {selector}")
        except Exception as e:
            logger.error(f"Failed to switch to frame: {str(e)}")
            raise

    @staticmethod
    def switch_to_default_content(driver):
        """Switch back to default content"""
        try:
            driver.switch_to.default_content()
            logger.info("Switched to default content")
        except Exception as e:
            logger.error(f"Failed to switch to default content: {str(e)}")
            raise
