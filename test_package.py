#!/usr/bin/env python3
import sys
import os
import logging
from selenium import webdriver
from selenium.webdriver.common.by import By
import time

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Auto-detect paths (supports PyInstaller bundled exe)
if getattr(sys, 'frozen', False):
    _BUNDLE_DIR = sys._MEIPASS  # PyInstaller extract temp dir
    _EXE_DIR = os.path.dirname(sys.executable)
else:
    _BUNDLE_DIR = os.path.dirname(os.path.abspath(__file__))
    _EXE_DIR = _BUNDLE_DIR
_BUNDLED_DRIVERS = os.path.join(_BUNDLE_DIR, 'drivers')
_EXTERNAL_DRIVERS = os.path.join(_EXE_DIR, 'drivers')

def _find_driver(filename):
    """Search driver: bundled first, then external"""
    for d in [_BUNDLED_DRIVERS, _EXTERNAL_DRIVERS, _EXE_DIR]:
        p = os.path.join(d, filename)
        if os.path.exists(p):
            logger.info(f'Found driver: {p}')
            return p
    return None

class AutomationScript:
    def __init__(self):
        self.driver = None

    def run(self):
        try:
            logger.info('Opening Edge browser')
            from selenium.webdriver.edge.service import Service as EdgeService
            _driver_path = _find_driver('msedgedriver.exe')
            if _driver_path:
                logger.info(f'Using local driver: {_driver_path}')
                _service = EdgeService(executable_path=_driver_path)
                self.driver = webdriver.Edge(service=_service)
            else:
                logger.info('No local driver, using Selenium Manager')
                self.driver = webdriver.Edge()

            self.driver.get('https://www.baidu.com')
            time.sleep(3)
            logger.info('Script execution completed successfully')
        except Exception as e:
            logger.error(f'Error: {str(e)}')
            raise
        finally:
            if self.driver:
                logger.info('Closing browser in 5 seconds...')
                time.sleep(5)
                self.driver.quit()

if __name__ == '__main__':
    script = AutomationScript()
    script.run()
