"""
Playwright helper functions for automation scripts
"""
import logging

logger = logging.getLogger(__name__)


class PlaywrightHelper:
    @staticmethod
    async def select_dropdown(page, selector, value):
        """Select option from dropdown"""
        try:
            await page.select_option(selector, value)
            logger.info(f"Selected dropdown value: {value}")
        except Exception as e:
            logger.error(f"Failed to select dropdown: {str(e)}")
            raise

    @staticmethod
    async def hover_element(page, selector):
        """Hover over element"""
        try:
            await page.hover(selector)
            logger.info(f"Hovered over element: {selector}")
        except Exception as e:
            logger.error(f"Failed to hover element: {str(e)}")
            raise

    @staticmethod
    async def double_click(page, selector):
        """Double click element"""
        try:
            await page.dblclick(selector)
            logger.info(f"Double clicked element: {selector}")
        except Exception as e:
            logger.error(f"Failed to double click: {str(e)}")
            raise

    @staticmethod
    async def right_click(page, selector):
        """Right click element"""
        try:
            await page.click(selector, button="right")
            logger.info(f"Right clicked element: {selector}")
        except Exception as e:
            logger.error(f"Failed to right click: {str(e)}")
            raise

    @staticmethod
    async def scroll_to_element(page, selector):
        """Scroll to element"""
        try:
            await page.locator(selector).scroll_into_view_if_needed()
            logger.info(f"Scrolled to element: {selector}")
        except Exception as e:
            logger.error(f"Failed to scroll to element: {str(e)}")
            raise

    @staticmethod
    async def get_attribute(page, selector, attribute):
        """Get element attribute"""
        try:
            value = await page.get_attribute(selector, attribute)
            logger.info(f"Got attribute {attribute}: {value}")
            return value
        except Exception as e:
            logger.error(f"Failed to get attribute: {str(e)}")
            raise

    @staticmethod
    async def is_element_visible(page, selector):
        """Check if element is visible"""
        try:
            return await page.is_visible(selector)
        except Exception:
            return False

    @staticmethod
    async def wait_for_element(page, selector, timeout=10000):
        """Wait for element to appear"""
        try:
            await page.wait_for_selector(selector, timeout=timeout)
            logger.info(f"Element appeared: {selector}")
        except Exception as e:
            logger.error(f"Timeout waiting for element {selector}: {str(e)}")
            raise

    @staticmethod
    async def get_text(page, selector):
        """Get text from element"""
        try:
            text = await page.text_content(selector)
            logger.info(f"Got text: {text}")
            return text
        except Exception as e:
            logger.error(f"Failed to get text: {str(e)}")
            raise

    @staticmethod
    async def fill_input(page, selector, text):
        """Fill input field"""
        try:
            await page.fill(selector, text)
            logger.info(f"Filled input: {selector}")
        except Exception as e:
            logger.error(f"Failed to fill input: {str(e)}")
            raise
