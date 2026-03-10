# IE浏览器驱动配置指南

## 下载IE驱动

1. 访问 https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver
2. 下载对应版本的 IEDriverServer.exe
3. 将驱动放在系统PATH中或项目目录

## Windows 配置

### 方式1：添加到系统PATH
1. 下载 IEDriverServer.exe
2. 放在 `C:\Windows\System32` 或其他PATH目录
3. 重启系统

### 方式2：指定驱动路径
在生成的Python脚本中指定驱动路径：
```python
from selenium.webdriver.ie.service import Service
service = Service('path/to/IEDriverServer.exe')
self.driver = webdriver.Ie(service=service)
```

## IE浏览器要求

- Windows 7 及以上
- IE 6 及以上
- 需要禁用保护模式（可选）

## 常见问题

### 问题1：找不到IEDriverServer
**解决方案：** 确保驱动在PATH中或指定完整路径

### 问题2：权限不足
**解决方案：** 以管理员身份运行Python脚本

### 问题3：IE版本不兼容
**解决方案：** 下载与IE版本匹配的驱动

## 驱动下载链接

- [IEDriverServer 32位](https://github.com/SeleniumHQ/selenium/releases)
- [IEDriverServer 64位](https://github.com/SeleniumHQ/selenium/releases)

## 验证安装

运行以下Python代码验证：
```python
from selenium import webdriver
driver = webdriver.Ie()
driver.get('https://www.google.com')
driver.quit()
```
