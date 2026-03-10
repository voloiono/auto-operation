# Auto Operation Platform - Quick Start Guide

## Prerequisites

- Java 25+
- Python 3.8+
- Node.js 18+
- npm or yarn

## Installation

### Linux/Mac

```bash
chmod +x install.sh build.sh
./install.sh
```

### Windows

```cmd
install.bat
```

## Running the Application

### Option 1: Development Mode

**Terminal 1 - Backend:**
```bash
java -jar build/libs/auto-operation-0.0.1-SNAPSHOT.jar
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

Access at: http://localhost:5173

### Option 2: Docker

```bash
docker-compose up
```

Access at: http://localhost

## Creating Your First Automation

1. Open the web interface
2. Create a new project
3. Add a flow to the project
4. Drag modules from the left panel to build your workflow
5. Configure each module's properties
6. Click "Generate" to create the Python script
7. Click "Execute" to run the script

## Scheduling Scripts

1. Go to the flow's schedule tab
2. Click "New Schedule"
3. Enter cron expression (e.g., `0 9 * * *` for 9 AM daily)
4. Add email for notifications
5. Enable the schedule

## Packaging Scripts as Executables

### Linux/Mac

```bash
./package_script.sh scripts/generated/flow_1.py my_automation
```

### Windows

```cmd
package_script.bat scripts\generated\flow_1.py my_automation
```

The executable will be in `dist/` directory.

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:autooperation
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

app:
  scripts:
    dir: ./scripts
```

## Troubleshooting

### Backend won't start
- Check Java version: `java -version`
- Check port 8080 is available
- Review logs in console

### Frontend won't load
- Check Node.js version: `node -v`
- Clear npm cache: `npm cache clean --force`
- Reinstall dependencies: `npm install`

### Scripts won't execute
- Verify Python is installed: `python --version`
- Install dependencies: `pip install -r scripts/requirements.txt`
- Check browser drivers are installed

## Support

For detailed documentation, see:
- [README.md](README.md) - Project overview
- [DEPLOYMENT.md](DEPLOYMENT.md) - Deployment guide
- [API Documentation](API.md) - REST API reference

## License

MIT
