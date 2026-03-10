# Auto Operation Platform - API Documentation

## Base URL

```
http://localhost:8080/api
```

## Projects API

### List Projects
```
GET /projects
```

Response:
```json
[
  {
    "id": 1,
    "name": "My Project",
    "description": "Project description",
    "status": "active",
    "createdAt": "2024-03-02T10:00:00",
    "updatedAt": "2024-03-02T10:00:00"
  }
]
```

### Create Project
```
POST /projects
Content-Type: application/json

{
  "name": "My Project",
  "description": "Project description"
}
```

### Get Project
```
GET /projects/{id}
```

### Update Project
```
PUT /projects/{id}
Content-Type: application/json

{
  "name": "Updated Name",
  "description": "Updated description",
  "status": "active"
}
```

### Delete Project
```
DELETE /projects/{id}
```

## Flows API

### List Flows
```
GET /flows/project/{projectId}
```

### Create Flow
```
POST /flows
Content-Type: application/json

{
  "projectId": 1,
  "name": "My Flow",
  "configuration": "{...}"
}
```

### Get Flow
```
GET /flows/{id}
```

### Update Flow
```
PUT /flows/{id}
Content-Type: application/json

{
  "name": "Updated Flow",
  "configuration": "{...}",
  "status": "draft"
}
```

### Generate Script
```
POST /flows/{id}/generate
```

Response:
```json
{
  "id": 1,
  "generatedScript": "#!/usr/bin/env python3\n...",
  "status": "draft"
}
```

### Execute Flow
```
POST /flows/{id}/execute
```

Response:
```json
{
  "success": true,
  "message": "Script executed successfully",
  "output": "...",
  "executionTimeMs": 1234
}
```

### Delete Flow
```
DELETE /flows/{id}
```

## Modules API

### List All Modules
```
GET /modules
```

Response:
```json
[
  {
    "id": 1,
    "moduleId": "open_browser",
    "name": "Open Browser",
    "category": "basic",
    "description": "Open a web browser",
    "inputSchema": "{...}",
    "outputSchema": "{...}"
  }
]
```

### Get Module
```
GET /modules/{moduleId}
```

### List Modules by Category
```
GET /modules/category/{category}
```

## Schedules API

### List Schedules
```
GET /schedules/flow/{flowId}
```

### Create Schedule
```
POST /schedules
Content-Type: application/json

{
  "flowId": 1,
  "cronExpression": "0 9 * * *",
  "enabled": true,
  "notifyEmail": "user@example.com"
}
```

### Get Schedule
```
GET /schedules/{id}
```

### Update Schedule
```
PUT /schedules/{id}
Content-Type: application/json

{
  "cronExpression": "0 10 * * *",
  "enabled": true,
  "notifyEmail": "user@example.com"
}
```

### Delete Schedule
```
DELETE /schedules/{id}
```

## Execution Logs API

### List Logs
```
GET /execution-logs/flow/{flowId}
```

Response:
```json
[
  {
    "id": 1,
    "flowId": 1,
    "status": "success",
    "output": "...",
    "errorMessage": null,
    "executionTimeMs": 1234,
    "startedAt": "2024-03-02T10:00:00",
    "completedAt": "2024-03-02T10:00:05"
  }
]
```

### Get Log
```
GET /execution-logs/{id}
```

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid request",
  "message": "Field validation failed"
}
```

### 404 Not Found
```json
{
  "error": "Not found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred"
}
```

## Cron Expression Examples

- `0 0 * * *` - Every day at midnight
- `0 9 * * *` - Every day at 9 AM
- `0 9 * * MON` - Every Monday at 9 AM
- `0 */6 * * *` - Every 6 hours
- `0 0 1 * *` - First day of every month
- `0 0 * * 0` - Every Sunday at midnight

## Rate Limiting

No rate limiting is currently implemented. For production, consider adding rate limiting middleware.

## Authentication

Currently, no authentication is implemented. For production, add JWT or OAuth2 authentication.

## CORS

CORS is enabled for all origins. For production, restrict to specific domains in `CorsConfig.java`.
