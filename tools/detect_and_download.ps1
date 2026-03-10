# 浏览器版本检测与驱动下载工具
# 运行方式: powershell -ExecutionPolicy Bypass -File detect_and_download.ps1

param(
    [switch]$Download,  # 加上此参数会自动下载驱动
    [string]$OutputDir = ".\drivers"  # 驱动保存目录
)

$Host.UI.RawUI.WindowTitle = "浏览器版本检测工具"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       浏览器版本检测工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 存储检测结果
$detected = @{}

# 检测 Edge
Write-Host "[Edge 浏览器]" -ForegroundColor Yellow
$edgePaths = @(
    "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    "C:\Program Files\Microsoft\Edge\Application\msedge.exe"
)
$edgeFound = $false
foreach ($path in $edgePaths) {
    if (Test-Path $path) {
        $version = (Get-Item $path).VersionInfo.FileVersion
        $majorVer = $version.Split('.')[0]
        Write-Host "  路径: $path" -ForegroundColor Gray
        Write-Host "  完整版本: $version" -ForegroundColor White
        Write-Host "  主版本号: $majorVer" -ForegroundColor Green
        $detected["edge"] = @{ Version = $version; Major = $majorVer }
        $edgeFound = $true
        break
    }
}
if (-not $edgeFound) { Write-Host "  未安装" -ForegroundColor Red }
Write-Host ""

# 检测 Chrome
Write-Host "[Chrome 浏览器]" -ForegroundColor Yellow
$chromePaths = @(
    "C:\Program Files\Google\Chrome\Application\chrome.exe",
    "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
)
$chromeFound = $false
foreach ($path in $chromePaths) {
    if (Test-Path $path) {
        $version = (Get-Item $path).VersionInfo.FileVersion
        $majorVer = $version.Split('.')[0]
        Write-Host "  路径: $path" -ForegroundColor Gray
        Write-Host "  完整版本: $version" -ForegroundColor White
        Write-Host "  主版本号: $majorVer" -ForegroundColor Green
        $detected["chrome"] = @{ Version = $version; Major = $majorVer }
        $chromeFound = $true
        break
    }
}
if (-not $chromeFound) { Write-Host "  未安装" -ForegroundColor Red }
Write-Host ""

# 检测 Firefox
Write-Host "[Firefox 浏览器]" -ForegroundColor Yellow
$firefoxPaths = @(
    "C:\Program Files\Mozilla Firefox\firefox.exe",
    "C:\Program Files (x86)\Mozilla Firefox\firefox.exe"
)
$firefoxFound = $false
foreach ($path in $firefoxPaths) {
    if (Test-Path $path) {
        $version = (Get-Item $path).VersionInfo.FileVersion
        $majorVer = $version.Split('.')[0]
        Write-Host "  路径: $path" -ForegroundColor Gray
        Write-Host "  完整版本: $version" -ForegroundColor White
        Write-Host "  主版本号: $majorVer" -ForegroundColor Green
        $detected["firefox"] = @{ Version = $version; Major = $majorVer }
        $firefoxFound = $true
        break
    }
}
if (-not $firefoxFound) { Write-Host "  未安装" -ForegroundColor Red }
Write-Host ""

# 检测 IE
Write-Host "[Internet Explorer]" -ForegroundColor Yellow
$iePaths = @(
    "C:\Program Files\Internet Explorer\iexplore.exe",
    "C:\Program Files (x86)\Internet Explorer\iexplore.exe"
)
$ieFound = $false
foreach ($path in $iePaths) {
    if (Test-Path $path) {
        $version = (Get-Item $path).VersionInfo.FileVersion
        $majorVer = $version.Split('.')[0]
        Write-Host "  路径: $path" -ForegroundColor Gray
        Write-Host "  完整版本: $version" -ForegroundColor White
        Write-Host "  主版本号: $majorVer" -ForegroundColor Green
        $detected["ie"] = @{ Version = $version; Major = $majorVer }
        $ieFound = $true
        break
    }
}
if (-not $ieFound) { Write-Host "  未安装" -ForegroundColor Red }
Write-Host ""

# 显示驱动下载信息
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       驱动下载命令" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($detected.ContainsKey("edge")) {
    $ver = $detected["edge"].Version
    $major = $detected["edge"].Major
    Write-Host "[Edge $major 驱动]" -ForegroundColor Yellow
    Write-Host "  下载地址: https://msedgedriver.azureedge.net/$ver/edgedriver_win64.zip" -ForegroundColor White
    Write-Host "  保存为: msedgedriver_$major.exe" -ForegroundColor Gray
    Write-Host ""
}

if ($detected.ContainsKey("chrome")) {
    $ver = $detected["chrome"].Version
    $major = $detected["chrome"].Major
    Write-Host "[Chrome $major 驱动]" -ForegroundColor Yellow
    Write-Host "  查询地址: https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json" -ForegroundColor White
    Write-Host "  保存为: chromedriver_$major.exe" -ForegroundColor Gray
    Write-Host ""
}

if ($detected.ContainsKey("firefox")) {
    Write-Host "[Firefox 驱动 (geckodriver)]" -ForegroundColor Yellow
    Write-Host "  下载地址: https://github.com/mozilla/geckodriver/releases" -ForegroundColor White
    Write-Host "  保存为: geckodriver.exe" -ForegroundColor Gray
    Write-Host ""
}

if ($detected.ContainsKey("ie")) {
    Write-Host "[IE 驱动]" -ForegroundColor Yellow
    Write-Host "  下载地址: https://www.selenium.dev/downloads/" -ForegroundColor White
    Write-Host "  保存为: IEDriverServer.exe" -ForegroundColor Gray
    Write-Host ""
}

# 自动下载功能
if ($Download) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "       开始下载驱动" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""

    if (-not (Test-Path $OutputDir)) {
        New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
    }

    # 下载 Edge 驱动
    if ($detected.ContainsKey("edge")) {
        $ver = $detected["edge"].Version
        $major = $detected["edge"].Major
        $url = "https://msedgedriver.azureedge.net/$ver/edgedriver_win64.zip"
        $zipFile = Join-Path $OutputDir "edgedriver_$major.zip"
        $exeFile = Join-Path $OutputDir "msedgedriver_$major.exe"

        if (Test-Path $exeFile) {
            Write-Host "[Edge] 驱动已存在: $exeFile" -ForegroundColor Yellow
        } else {
            Write-Host "[Edge] 下载中: $url" -ForegroundColor Green
            try {
                Invoke-WebRequest -Uri $url -OutFile $zipFile -UseBasicParsing
                $tempDir = Join-Path $OutputDir "temp_edge"
                Expand-Archive -Path $zipFile -DestinationPath $tempDir -Force
                Move-Item -Path (Join-Path $tempDir "msedgedriver.exe") -Destination $exeFile -Force
                Remove-Item -Path $tempDir -Recurse -Force
                Remove-Item -Path $zipFile -Force
                Write-Host "[Edge] 完成: $exeFile" -ForegroundColor Green
            } catch {
                Write-Host "[Edge] 下载失败: $_" -ForegroundColor Red
            }
        }
    }

    # 下载 Chrome 驱动 (使用 Chrome for Testing)
    if ($detected.ContainsKey("chrome")) {
        $ver = $detected["chrome"].Version
        $major = $detected["chrome"].Major
        $exeFile = Join-Path $OutputDir "chromedriver_$major.exe"

        if (Test-Path $exeFile) {
            Write-Host "[Chrome] 驱动已存在: $exeFile" -ForegroundColor Yellow
        } else {
            Write-Host "[Chrome] 查询下载地址..." -ForegroundColor Green
            try {
                # 查询 Chrome for Testing API
                $json = Invoke-RestMethod -Uri "https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json"
                $matching = $json.versions | Where-Object { $_.version -like "$major.*" } | Select-Object -Last 1
                if ($matching -and $matching.downloads.chromedriver) {
                    $url = ($matching.downloads.chromedriver | Where-Object { $_.platform -eq "win64" }).url
                    $zipFile = Join-Path $OutputDir "chromedriver_$major.zip"
                    Write-Host "[Chrome] 下载中: $url" -ForegroundColor Green
                    Invoke-WebRequest -Uri $url -OutFile $zipFile -UseBasicParsing
                    $tempDir = Join-Path $OutputDir "temp_chrome"
                    Expand-Archive -Path $zipFile -DestinationPath $tempDir -Force
                    $chromeDriverPath = Get-ChildItem -Path $tempDir -Recurse -Filter "chromedriver.exe" | Select-Object -First 1
                    Move-Item -Path $chromeDriverPath.FullName -Destination $exeFile -Force
                    Remove-Item -Path $tempDir -Recurse -Force
                    Remove-Item -Path $zipFile -Force
                    Write-Host "[Chrome] 完成: $exeFile" -ForegroundColor Green
                } else {
                    Write-Host "[Chrome] 未找到匹配版本 $major 的驱动" -ForegroundColor Red
                }
            } catch {
                Write-Host "[Chrome] 下载失败: $_" -ForegroundColor Red
            }
        }
    }

    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "提示: 将驱动文件放到 python/drivers/ 目录" -ForegroundColor White
Write-Host "命名格式: msedgedriver_版本号.exe" -ForegroundColor White
Write-Host "例如: msedgedriver_143.exe, chromedriver_120.exe" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if (-not $Download) {
    Write-Host "如需自动下载驱动，请运行:" -ForegroundColor Gray
    Write-Host "  .\detect_and_download.ps1 -Download" -ForegroundColor White
    Write-Host "  .\detect_and_download.ps1 -Download -OutputDir 'C:\path\to\drivers'" -ForegroundColor White
    Write-Host ""
}

Read-Host "按 Enter 键退出"
