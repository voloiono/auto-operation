# Auto Operation Platform

A complete automation script generation platform with drag-and-drop flow editor, Python script generation, and scheduled execution.

## Features

- **Drag-and-Drop Flow Editor**: Visually design automation workflows
- **Python Script Generation**: Automatically generate executable Python scripts
- **Multiple Browser Support**: Chrome, Firefox, Edge, IE via Selenium
- **Scheduled Execution**: Run scripts on a schedule with cron expressions
- **Email Notifications**: Get notified when scripts complete
- **Execution History**: Track all script executions with logs
- **Web UI**: Modern Vue 3 interface for easy management

## Architecture

### Backend (Java Spring Boot)
- REST API for project, flow, and schedule management
- Python script generation engine
- Script execution via ProcessBuilder
- Quartz-based job scheduling
- Email notification service

### Frontend (Vue 3)
- Drag-and-drop flow editor
- Module library with 6+ built-in modules
- Property configuration panel
- Execution history viewer
- Schedule management interface

### Python Scripts
- Selenium-based browser automation
- Support for common web interactions
- Error handling and logging
- APScheduler integration for local scheduling

## Quick Start

### Backend

```bash
cd /c/work/brid-recognition/auto-operation
./gradlew build
java -jar build/libs/auto-operation-0.0.1-SNAPSHOT.jar
```

Backend runs on `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`

### Create Your First Flow

1. Open http://localhost:5173
2. Create a new project
3. Add a flow to the project
4. Drag modules from the left panel to create your workflow
5. Configure each module's properties
6. Click "Generate" to create the Python script
7. Click "Execute" to run the script

## Project Structure

```
auto-operation/
├── src/main/java/com/example/autooperation/
│   ├── controller/          # REST API endpoints
│   ├── service/             # Business logic
│   ├── model/               # JPA entities
│   ├── repository/          # Data access
│   ├── dto/                 # Data transfer objects
│   ├── config/              # Spring configuration
│   ├── job/                 # Quartz jobs
│   └── listener/            # Event listeners
├── src/main/resources/
│   ├── application.yml      # Application configuration
│   └── db/migration/        # Flyway migrations
├── frontend/                # Vue 3 frontend
│   ├── src/
│   │   ├── components/      # Vue components
│   │   ├── views/           # Page components
│   │   ├── api/             # API clients
│   │   ├── store/           # Pinia state management
│   │   └── router/          # Vue Router
│   └── package.json
├── scripts/                 # Python templates
│   ├── templates/           # Base templates
│   └── requirements.txt     # Python dependencies
└── build.gradle             # Gradle configuration
```

## Available Modules

### Basic
- **Open Browser**: Launch a web browser
- **Input Text**: Type text into an element
- **Click Element**: Click on an element
- **Wait for Element**: Wait for element to appear
- **Get Text**: Extract text from element
- **Take Screenshot**: Capture screen

### Advanced
- Select Dropdown
- Hover Element
- Double Click
- Right Click
- Scroll to Element
- Get Attribute

## API Endpoints

### Projects
- `GET /api/projects` - List all projects
- `POST /api/projects` - Create project
- `GET /api/projects/{id}` - Get project
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Flows
- `GET /api/flows/project/{projectId}` - List flows
- `POST /api/flows` - Create flow
- `GET /api/flows/{id}` - Get flow
- `PUT /api/flows/{id}` - Update flow
- `POST /api/flows/{id}/generate` - Generate script
- `POST /api/flows/{id}/execute` - Execute flow
- `DELETE /api/flows/{id}` - Delete flow

### Schedules
- `GET /api/schedules/flow/{flowId}` - List schedules
- `POST /api/schedules` - Create schedule
- `PUT /api/schedules/{id}` - Update schedule
- `DELETE /api/schedules/{id}` - Delete schedule

### Execution Logs
- `GET /api/execution-logs/flow/{flowId}` - Get logs
- `GET /api/execution-logs/{id}` - Get log details

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

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions for Windows, Mac, and Linux.

## Technology Stack

- **Backend**: Java 25, Spring Boot 4.0.3, Quartz, JPA/Hibernate
- **Frontend**: Vue 3, Vite, Pinia, Element Plus
- **Database**: H2 (dev), SQLite/MySQL (prod)
- **Automation**: Selenium, Playwright
- **Build**: Gradle, npm

## License

MIT

## Support

For issues and questions, please refer to the documentation or create an issue in the repository.
