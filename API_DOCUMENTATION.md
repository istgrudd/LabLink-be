# LabLink API Documentation

**Base URL:** `http://localhost:8080`

**Authentication:** JWT Bearer Token (except `/api/auth/login` and `/error`)

---

## 🔐 Authentication

### Login
```http
POST /api/auth/login
```

**Public endpoint** - No authentication required.

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": "uuid",
    "username": "admin",
    "fullName": "Administrator",
    "role": "ADMIN",
    "isPasswordChanged": true
  }
}
```

### Get Current User
```http
GET /api/auth/me
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": "uuid",
  "username": "admin",
  "fullName": "Administrator",
  "role": "ADMIN",
  "isPasswordChanged": true
}
```

---

## 👥 Members

### Get All Members
```http
GET /api/members
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "id": "uuid",
    "username": "2021001",
    "fullName": "John Doe",
    "role": "ASSISTANT",
    "expertDivision": "BIG_DATA",
    "department": "INTERNAL",
    "email": "john@example.com",
    "phoneNumber": "08123456789",
    "socialMediaLink": "https://instagram.com/john",
    "isActive": true,
    "isPasswordChanged": false,
    "createdAt": "2026-01-15T10:00:00",
    "updatedAt": "2026-01-15T10:00:00"
  }
]
```

### Get Member by ID
```http
GET /api/members/{id}
Authorization: Bearer {token}
```

### Create Member
```http
POST /api/members
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "nim": "2021001",
  "fullName": "John Doe",
  "expertDivision": "BIG_DATA",
  "department": "INTERNAL",
  "role": "ASSISTANT"
}
```

**Response:** Same as Get Member by ID

**Notes:**
- Default password = NIM
- `role` is optional (defaults to `ASSISTANT`)
- Expert divisions: `CYBER_SECURITY`, `BIG_DATA`, `GIS`, `GAME_TECH`, `CROSS_DIVISION`
- Departments: `INTERNAL`, `EKSTERNAL`

### Update Member
```http
PUT /api/members/{id}
Authorization: Bearer {token}
```

**Request Body (all fields optional):**
```json
{
  "fullName": "John Doe Updated",
  "expertDivision": "CYBER_SECURITY",
  "department": "INTERNAL",
  "email": "john.new@example.com",
  "phoneNumber": "08198765432",
  "socialMediaLink": "https://linkedin.com/in/john"
}
```

### Delete Member
```http
DELETE /api/members/{id}
Authorization: Bearer {token}
Role: ADMIN
```

**Response:** `204 No Content`

**Error:** `400 Bad Request` if member is still a project leader

---

## 📁 Projects

### Get All Projects
```http
GET /api/projects?periodId={periodId}
Authorization: Bearer {token}
```

**Query Parameters:**
- `periodId` (optional): Filter by academic period

### Get Project by ID
```http
GET /api/projects/{id}
Authorization: Bearer {token}
```

### Get Project by Code
```http
GET /api/projects/code/{projectCode}
Authorization: Bearer {token}
```

### Create Project
```http
POST /api/projects
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "name": "AI Research Project",
  "division": "BIG_DATA",
  "activityType": "RISET",
  "leaderId": "member-uuid",
  "periodId": "period-uuid",
  "startDate": "2026-01-15",
  "endDate": "2026-12-31",
  "description": "Research on machine learning applications"
}
```

**Activity Types:** `RISET`, `HKI`, `PENGABDIAN`

### Update Project
```http
PUT /api/projects/{id}
Authorization: Bearer {token}
Role: ADMIN
```

### Delete Project
```http
DELETE /api/projects/{id}
Authorization: Bearer {token}
Role: ADMIN
```

### Add Project Member
```http
POST /api/projects/{projectId}/members
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "memberId": "member-uuid"
}
```

### Get Project Members
```http
GET /api/projects/{projectId}/members
Authorization: Bearer {token}
```

### Remove Project Member
```http
DELETE /api/projects/{projectId}/members/{memberId}
Authorization: Bearer {token}
Role: ADMIN
```

---

## 📅 Events

### Get All Events
```http
GET /api/events?periodId={periodId}
Authorization: Bearer {token}
```

### Get Event by ID
```http
GET /api/events/{id}
Authorization: Bearer {token}
```

### Get Event by Code
```http
GET /api/events/code/{eventCode}
Authorization: Bearer {token}
```

### Create Event
```http
POST /api/events
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "name": "Tech Workshop 2026",
  "eventType": "WORKSHOP",
  "periodId": "period-uuid",
  "eventDate": "2026-03-15",
  "location": "Lab Building",
  "description": "Workshop on cloud computing"
}
```

**Event Types:** `WORKSHOP`, `SEMINAR`, `COMPETITION`, `TRAINING`, `OTHER`

### Update Event
```http
PUT /api/events/{id}
Authorization: Bearer {token}
Role: ADMIN
```

### Delete Event
```http
DELETE /api/events/{id}
Authorization: Bearer {token}
Role: ADMIN
```

### Add Committee Member
```http
POST /api/events/{id}/committee
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "memberId": "member-uuid",
  "role": "COORDINATOR"
}
```

### Get Committee Members
```http
GET /api/events/{id}/committee
Authorization: Bearer {token}
```

### Update Committee Role
```http
PUT /api/events/{id}/committee/{memberId}
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "role": "HEAD_COORDINATOR"
}
```

### Remove Committee Member
```http
DELETE /api/events/{id}/committee/{memberId}
Authorization: Bearer {token}
Role: ADMIN
```

---

## 📦 Archives

### Get All Archives
```http
GET /api/archives?periodId={periodId}
Authorization: Bearer {token}
```

### Get Archive by ID
```http
GET /api/archives/{id}
Authorization: Bearer {token}
```

### Get Archive by Code
```http
GET /api/archives/code/{archiveCode}
Authorization: Bearer {token}
```

### Get Archives by Project
```http
GET /api/archives/project/{projectId}
Authorization: Bearer {token}
```

### Get Archives by Event
```http
GET /api/archives/event/{eventId}
Authorization: Bearer {token}
```

### Get Archives by Department
```http
GET /api/archives/department/{department}
Authorization: Bearer {token}
```

### Create Archive
```http
POST /api/archives
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "title": "Research Publication",
  "archiveType": "PUBLICATION",
  "outputType": "JOURNAL",
  "department": "INTERNAL",
  "projectId": "project-uuid",
  "periodId": "period-uuid",
  "publicationDate": "2026-06-15",
  "url": "https://doi.org/...",
  "description": "Published in IEEE Transactions"
}
```

**Archive Types:** `PUBLICATION`, `HKI`, `DOCUMENTATION`
**Output Types:** `JOURNAL`, `CONFERENCE`, `PATENT`, `COPYRIGHT`, `VIDEO`, `PHOTO`, `REPORT`

### Update Archive
```http
PUT /api/archives/{id}
Authorization: Bearer {token}
Role: ADMIN
```

### Delete Archive
```http
DELETE /api/archives/{id}
Authorization: Bearer {token}
Role: ADMIN
```

---

## 📆 Academic Periods

### Get All Periods
```http
GET /api/periods
Authorization: Bearer {token}
```

### Get Active Period
```http
GET /api/periods/active
Authorization: Bearer {token}
```

### Get Period by ID
```http
GET /api/periods/{id}
Authorization: Bearer {token}
```

### Create Period
```http
POST /api/periods
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "periodName": "2026/2027",
  "startDate": "2026-08-01",
  "endDate": "2027-07-31"
}
```

### Activate Period
```http
POST /api/periods/{id}/activate
Authorization: Bearer {token}
Role: ADMIN
```

### Close Period
```http
POST /api/periods/{id}/close
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "closingNotes": "Successfully completed all projects and events"
}
```

### Add Member to Period
```http
POST /api/periods/{id}/members
Authorization: Bearer {token}
Role: ADMIN
```

**Request Body:**
```json
{
  "memberId": "member-uuid",
  "roleInPeriod": "KETUA_LAB"
}
```

**Roles:** `KETUA_LAB`, `WAKIL_KETUA`, `SEKRETARIS`, `BENDAHARA`, `ANGGOTA`

### Get Members by Period
```http
GET /api/periods/{id}/members
Authorization: Bearer {token}
```

---

## 📊 Dashboard

### Get Dashboard Summary
```http
GET /api/dashboard/summary
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalMembers": 15,
  "totalActiveMembers": 12,
  "totalProjects": 8,
  "totalActiveProjects": 5,
  "totalEvents": 3,
  "totalPublications": 10,
  "totalHKI": 2,
  "currentPeriod": {
    "id": "uuid",
    "periodName": "2026/2027",
    "isActive": true
  }
}
```

---

## 📝 Activity Logs

### Get All Logs (Paginated)
```http
GET /api/activity-logs?page=0&size=50
Authorization: Bearer {token}
Role: ADMIN
```

### Get Recent Logs
```http
GET /api/activity-logs/recent?limit=100
Authorization: Bearer {token}
Role: ADMIN
```

### Get Logs by Target Type
```http
GET /api/activity-logs/target/{targetType}
Authorization: Bearer {token}
Role: ADMIN
```

**Target Types:** `MEMBER`, `PROJECT`, `EVENT`, `ARCHIVE`, `PERIOD`

### Get Logs by User
```http
GET /api/activity-logs/user/{username}
Authorization: Bearer {token}
Role: ADMIN
```

### Get Logs by Action
```http
GET /api/activity-logs/action/{action}
Authorization: Bearer {token}
Role: ADMIN
```

**Actions:** `CREATE`, `UPDATE`, `DELETE`

**Log Response:**
```json
{
  "id": "uuid",
  "action": "CREATE",
  "targetType": "MEMBER",
  "targetId": "member-uuid",
  "targetName": "John Doe",
  "description": "Created member: 2021001",
  "userName": "admin",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

## 🔒 Authorization

**Roles:**
- `ADMIN` - Full access to all endpoints
- `ASSISTANT` - Read-only access to most endpoints

**Authentication Flow:**
1. Login with `/api/auth/login` → Get JWT token
2. Include token in all subsequent requests: `Authorization: Bearer {token}`
3. Token expires after 24 hours (configurable)

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions
- `400 Bad Request` - Validation errors
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server errors
