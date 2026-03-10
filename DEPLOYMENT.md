# Auto Operation Platform - Deployment Guide

## Backend Deployment

### Prerequisites
- Java 25 or higher
- Gradle 8.0 or higher

### Build

```bash
cd /c/work/brid-recognition/auto-operation
./gradlew build
```

### Run

```bash
java -jar build/libs/auto-operation-0.0.1-SNAPSHOT.jar
```

The backend will be available at `http://localhost:8080`

### Configuration

Edit `src/main/resources/application.yml` to configure:
- Database connection
- Email settings
- Script execution directory

## Frontend Deployment

### Prerequisites
- Node.js 18 or higher
- npm or yarn

### Build

```bash
cd frontend
npm install
npm run build
```

### Run Development Server

```bash
npm run dev
```

The frontend will be available at `http://localhost:5173`

### Production Build

The built files will be in `frontend/dist/`. Deploy to any static hosting service.

## Python Scripts

### Setup

```bash
cd scripts
pip install -r requirements.txt
```

### Browser Drivers

For Selenium:
- Chrome: Download ChromeDriver from https://chromedriver.chromium.org/
- Firefox: Download GeckoDriver from https://github.com/mozilla/geckodriver/releases

For Playwright:
```bash
playwright install
```

## Docker Deployment (Optional)

### Backend Dockerfile

```dockerfile
FROM openjdk:25-slim
WORKDIR /app
COPY build/libs/auto-operation-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend Dockerfile

```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/src ./src
COPY frontend/public ./public
COPY frontend/vite.config.js ./
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## Windows Executable Packaging

### Using PyInstaller

```bash
pip install pyinstaller
pyinstaller --onefile --windowed scripts/generated/flow_1.py
```

The executable will be in `dist/flow_1.exe`

## Mac App Packaging

### Using PyInstaller

```bash
pip install pyinstaller
pyinstaller --onefile --windowed scripts/generated/flow_1.py
```

The app will be in `dist/flow_1.app`

## Scheduled Execution

### Windows Task Scheduler

1. Open Task Scheduler
2. Create Basic Task
3. Set trigger (daily, weekly, etc.)
4. Set action to run the executable

### Linux Cron

```bash
0 9 * * * /path/to/flow_1
```

### Mac LaunchAgent

Create `~/Library/LaunchAgents/com.autooperation.flow1.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.autooperation.flow1</string>
    <key>ProgramArguments</key>
    <array>
        <string>/path/to/flow_1</string>
    </array>
    <key>StartCalendarInterval</key>
    <dict>
        <key>Hour</key>
        <integer>9</integer>
        <key>Minute</key>
        <integer>0</integer>
    </dict>
</dict>
</plist>
```

Then load it:
```bash
launchctl load ~/Library/LaunchAgents/com.autooperation.flow1.plist
```

## Troubleshooting

### Backend Issues

- Check logs: `tail -f logs/application.log`
- Verify database connection in `application.yml`
- Ensure Python is installed and in PATH

### Frontend Issues

- Clear browser cache
- Check browser console for errors
- Verify API endpoint in `src/api/index.js`

### Script Execution Issues

- Verify browser drivers are installed
- Check script permissions: `chmod +x flow_1`
- Review execution logs in the UI

## Support

For issues and questions, check the project documentation or create an issue in the repository.
