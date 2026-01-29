# LabLink Backend API Documentation

Dokumentasi teknis untuk komunikasi antara Frontend dan Backend developer.

---

## Table of Contents
1. [Physical Data Model (ERD)](#1-physical-data-model-erd)
2. [State Machine Diagrams](#2-state-machine-diagrams)
3. [Sequence Diagrams](#3-sequence-diagrams)

---

## 1. Physical Data Model (ERD)

```mermaid
erDiagram
    %% Core Entities
    RESEARCH_ASSISTANT {
        string id PK
        string username UK
        string password
        string full_name
        string email
        string phone
        string role "ADMIN|HEAD_LAB|RESEARCH_COORD|DIVISION_HEAD|TREASURER|SECRETARY|MEMBER"
        string expert_division
        boolean is_password_changed
        timestamp created_at
        timestamp updated_at
    }

    ACADEMIC_PERIOD {
        string id PK
        string code UK
        string name
        date start_date
        date end_date
        boolean is_active
        boolean is_archived
        timestamp created_at
        timestamp updated_at
    }

    MEMBER_PERIOD {
        string member_id PK,FK
        string period_id PK,FK
        string position
        string status "ACTIVE|ALUMNI"
        timestamp graduated_at
        timestamp joined_at
    }

    %% Project Module
    PROJECT {
        string id PK
        string project_code UK
        string name
        text description
        string leader_id FK
        string period_id FK
        string status "NOT_STARTED|IN_PROGRESS|COMPLETED|CANCELLED"
        string approval_status "PENDING|APPROVED|REJECTED"
        string approved_by
        string rejection_reason
        date start_date
        date end_date
        timestamp created_at
        timestamp updated_at
    }

    PROJECT_MEMBER {
        string project_id PK,FK
        string member_id PK,FK
        string role
        timestamp joined_at
    }

    %% Event Module
    EVENT {
        string id PK
        string event_code UK
        string name
        text description
        string pic_id FK
        string period_id FK
        string status "PLANNED|ONGOING|COMPLETED|CANCELLED"
        string approval_status "PENDING|APPROVED|REJECTED"
        string approved_by
        string rejection_reason
        date start_date
        date end_date
        timestamp created_at
        timestamp updated_at
    }

    EVENT_COMMITTEE {
        string event_id PK,FK
        string member_id PK,FK
        string role
        timestamp joined_at
    }

    EVENT_SCHEDULE {
        string id PK
        string event_id FK
        date activity_date
        string title
        text description
        time start_time
        time end_time
        string location
    }

    %% Archive Module
    ARCHIVE {
        string id PK
        string archive_code UK
        string title
        text description
        string archive_type "PUBLIKASI|HKI|PKM|LAPORAN|SERTIFIKAT"
        string source_type "PROJECT|EVENT"
        string department "INTERNAL|EKSTERNAL"
        string project_id FK
        string event_id FK
        string period_id FK
        string publish_location
        string reference_number
        date publish_date
        timestamp created_at
        timestamp updated_at
    }

    %% Administration Module
    LETTER {
        string id PK
        string letter_number UK
        string letter_type "SURAT_PEMINJAMAN|SURAT_TUGAS|SURAT_KETERANGAN"
        string category "INTERNAL|EKSTERNAL"
        string subject
        string recipient
        text content
        string attachment
        string requester_id FK
        string requester_name
        string requester_nim
        string event_id FK
        string period_id FK
        date borrow_date
        date borrow_return_date
        date issue_date
        string status "PENDING|APPROVED|REJECTED|DOWNLOADED"
        string approved_by
        string rejection_reason
        timestamp created_at
        timestamp updated_at
    }

    INCOMING_LETTER {
        string id PK
        string reference_number
        string sender
        string subject
        date received_date
        text notes
        string attachment_path
        timestamp created_at
        timestamp updated_at
    }

    %% Finance Module
    FINANCE_CATEGORY {
        string id PK
        string name UK
        string type "INCOME|EXPENSE|BOTH"
        text description
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }

    DUES_PAYMENT {
        string id PK
        string member_id FK
        string period_id FK
        int payment_month
        int payment_year
        decimal amount
        date paid_at
        string payment_proof_path
        string status "UNPAID|PENDING|VERIFIED|REJECTED"
        string verified_by
        timestamp created_at
        timestamp updated_at
    }

    FINANCE_TRANSACTION {
        string id PK
        string type "INCOME|EXPENSE"
        string category_id FK
        string period_id FK
        string event_id FK
        string project_id FK
        decimal amount
        date transaction_date
        text description
        string receipt_path
        string created_by
        timestamp created_at
        timestamp updated_at
    }

    PROCUREMENT_REQUEST {
        string id PK
        string requester_id FK
        string item_name
        text description
        string reason
        decimal estimated_price
        string priority "LOW|MEDIUM|HIGH|URGENT"
        string purchase_link
        string status "PENDING|APPROVED|REJECTED|PURCHASED"
        string processed_by
        string rejection_reason
        date processed_at
        string transaction_id FK
        timestamp created_at
        timestamp updated_at
    }

    %% Audit Log
    ACTIVITY_LOG {
        string id PK
        string entity_type
        string entity_id
        string entity_name
        string action "CREATE|UPDATE|DELETE"
        text details
        string performed_by
        timestamp created_at
    }

    %% Relationships
    RESEARCH_ASSISTANT ||--o{ MEMBER_PERIOD : "belongs to"
    ACADEMIC_PERIOD ||--o{ MEMBER_PERIOD : "has"
    
    RESEARCH_ASSISTANT ||--o{ PROJECT : "leads"
    ACADEMIC_PERIOD ||--o{ PROJECT : "contains"
    PROJECT ||--o{ PROJECT_MEMBER : "has"
    RESEARCH_ASSISTANT ||--o{ PROJECT_MEMBER : "participates"
    
    RESEARCH_ASSISTANT ||--o{ EVENT : "is PIC of"
    ACADEMIC_PERIOD ||--o{ EVENT : "contains"
    EVENT ||--o{ EVENT_COMMITTEE : "has"
    RESEARCH_ASSISTANT ||--o{ EVENT_COMMITTEE : "participates"
    EVENT ||--o{ EVENT_SCHEDULE : "has"
    
    PROJECT ||--o{ ARCHIVE : "produces"
    EVENT ||--o{ ARCHIVE : "produces"
    ACADEMIC_PERIOD ||--o{ ARCHIVE : "contains"
    
    RESEARCH_ASSISTANT ||--o{ LETTER : "requests"
    EVENT ||--o{ LETTER : "related to"
    ACADEMIC_PERIOD ||--o{ LETTER : "contains"
    
    RESEARCH_ASSISTANT ||--o{ DUES_PAYMENT : "pays"
    ACADEMIC_PERIOD ||--o{ DUES_PAYMENT : "contains"
    
    FINANCE_CATEGORY ||--o{ FINANCE_TRANSACTION : "categorizes"
    ACADEMIC_PERIOD ||--o{ FINANCE_TRANSACTION : "contains"
    EVENT ||--o{ FINANCE_TRANSACTION : "cost center"
    PROJECT ||--o{ FINANCE_TRANSACTION : "cost center"
    
    RESEARCH_ASSISTANT ||--o{ PROCUREMENT_REQUEST : "requests"
    FINANCE_TRANSACTION ||--o| PROCUREMENT_REQUEST : "fulfills"
```

---

## 2. State Machine Diagrams

### 2.1 Project Approval State

```mermaid
stateDiagram-v2
    [*] --> PENDING: Create Project
    
    PENDING --> APPROVED: Admin/Coord/DivHead Approves
    PENDING --> REJECTED: Admin/Coord/DivHead Rejects
    
    APPROVED --> [*]
    REJECTED --> PENDING: Resubmit
    REJECTED --> [*]
    
    note right of PENDING
        Division Head can only approve
        projects in their division
    end note
```

### 2.2 Project Status State

```mermaid
stateDiagram-v2
    [*] --> NOT_STARTED: Create (after approval)
    
    NOT_STARTED --> IN_PROGRESS: Start Work
    IN_PROGRESS --> ON_HOLD: Pause
    ON_HOLD --> IN_PROGRESS: Resume
    IN_PROGRESS --> COMPLETED: Finish
    
    NOT_STARTED --> CANCELLED: Cancel
    IN_PROGRESS --> CANCELLED: Cancel
    ON_HOLD --> CANCELLED: Cancel
    
    COMPLETED --> [*]: Can create Archive
    CANCELLED --> [*]
```

### 2.3 Event Approval State

```mermaid
stateDiagram-v2
    [*] --> PENDING: Create Event
    
    PENDING --> APPROVED: Admin Approves
    PENDING --> REJECTED: Admin Rejects
    
    APPROVED --> [*]
    REJECTED --> PENDING: Resubmit
    REJECTED --> [*]
```

### 2.4 Event Status State

```mermaid
stateDiagram-v2
    [*] --> PLANNED: Create (after approval)
    
    PLANNED --> ONGOING: Event Starts
    ONGOING --> COMPLETED: Event Ends
    
    PLANNED --> CANCELLED: Cancel
    
    COMPLETED --> [*]: Can create Archive
    CANCELLED --> [*]
```

### 2.5 Letter Status State

```mermaid
stateDiagram-v2
    [*] --> PENDING: Submit Request
    
    PENDING --> APPROVED: Admin Approves
    PENDING --> REJECTED: Admin Rejects
    
    APPROVED --> DOWNLOADED: User Downloads
    
    REJECTED --> PENDING: Resubmit
    
    DOWNLOADED --> [*]
    REJECTED --> [*]
    
    note right of APPROVED
        Letter number generated
        Issue date = approval date
    end note
```

### 2.6 Dues Payment Status State

```mermaid
stateDiagram-v2
    [*] --> UNPAID: Member assigned to period
    
    UNPAID --> PENDING: Submit Payment + Proof
    
    PENDING --> VERIFIED: Treasurer Verifies
    PENDING --> REJECTED: Treasurer Rejects
    
    REJECTED --> PENDING: Resubmit
    
    VERIFIED --> [*]
    REJECTED --> [*]
```

### 2.7 Procurement Request Status State

```mermaid
stateDiagram-v2
    [*] --> PENDING: Submit Request
    
    PENDING --> APPROVED: Admin/Treasurer Approves
    PENDING --> REJECTED: Admin/Treasurer Rejects
    
    APPROVED --> PURCHASED: Mark as Purchased
    
    REJECTED --> [*]
    PURCHASED --> [*]
    
    note right of PURCHASED
        Linked to Finance Transaction
    end note
```

---

## 3. Sequence Diagrams

### 3.1 Authentication Flow

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant AC as AuthController
    participant AS as AuthService
    participant JWT as JwtService
    participant DB as Database
    
    FE->>AC: POST /api/auth/login {username, password}
    AC->>AS: login(request)
    AS->>DB: findByUsername(username)
    DB-->>AS: ResearchAssistant
    AS->>AS: passwordEncoder.matches()
    alt Password Valid
        AS->>JWT: generateToken(username, role, id)
        JWT-->>AS: JWT Token
        AS-->>AC: LoginResponse
        AC-->>FE: 200 OK {token, user}
    else Password Invalid
        AS-->>AC: AuthenticationException
        AC-->>FE: 401 Unauthorized
    end
```

### 3.2 Project Approval Flow

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant PC as ProjectController
    participant PS as ProjectService
    participant PAS as ProjectApprovalService
    participant DB as Database
    
    Note over FE,DB: Create Project
    FE->>PC: POST /api/projects {name, leaderId, ...}
    PC->>PS: createProject(request)
    PS->>DB: save(project) [status=PENDING]
    DB-->>PS: Project
    PS-->>PC: ProjectResponse
    PC-->>FE: 201 Created
    
    Note over FE,DB: Get Pending Projects
    FE->>PC: GET /api/projects/pending
    PC->>PS: getPendingProjects()
    PS->>PAS: getPending()
    PAS->>DB: findByApprovalStatus("PENDING")
    DB-->>PAS: List of Project
    PAS-->>PS: List of ProjectResponse
    PS-->>PC: List of ProjectResponse
    PC-->>FE: 200 OK
    
    Note over FE,DB: Approve Project
    FE->>PC: PUT /api/projects/{id}/approve
    PC->>PS: approveProject(id, username)
    PS->>PAS: approve(id, username)
    PAS->>DB: findById(id)
    DB-->>PAS: Project
    PAS->>PAS: validateAccess() [RBAC check]
    PAS->>DB: save(project) [status=APPROVED]
    DB-->>PAS: Project
    PAS-->>PS: ProjectResponse
    PS-->>PC: ProjectResponse
    PC-->>FE: 200 OK
```

### 3.3 Event with Schedule Flow

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant EC as EventController
    participant ES as EventService
    participant DB as Database
    
    Note over FE,DB: Create Event with Schedules
    FE->>EC: POST /api/events {name, schedules: [...]}
    EC->>ES: createEvent(request)
    ES->>DB: save(event)
    DB-->>ES: Event
    loop For each schedule
        ES->>ES: validate date in event range
        ES->>DB: save(schedule)
    end
    ES-->>EC: EventResponse
    EC-->>FE: 201 Created
    
    Note over FE,DB: Add Committee Member
    FE->>EC: POST /api/events/{id}/committee
    EC->>ES: addCommitteeMember(eventId, request)
    ES->>DB: findEvent(eventId)
    ES->>DB: findMember(memberId)
    ES->>DB: save(EventCommittee)
    ES-->>EC: EventResponse
    EC-->>FE: 200 OK
```

### 3.4 Finance Transaction Flow

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant FC as FinanceController
    participant FS as FinanceService
    participant FSS as FileStorageService
    participant DB as Database
    
    Note over FE,DB: Create Transaction with Receipt
    FE->>FC: POST /api/finance/transactions (multipart)
    FC->>FSS: storeFile(receipt)
    FSS-->>FC: filename
    FC->>FS: createTransaction(request, receiptPath, username)
    FS->>DB: findCategory(categoryId)
    FS->>DB: findActivePeriod()
    alt Has Event Cost Center
        FS->>DB: findEvent(eventId)
    end
    alt Has Project Cost Center
        FS->>DB: findProject(projectId)
    end
    FS->>DB: save(transaction)
    DB-->>FS: FinanceTransaction
    FS-->>FC: TransactionResponse
    FC-->>FE: 201 Created
```

### 3.5 Letter Request & Download Flow

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant LC as LetterController
    participant LS as LetterService
    participant LNG as LetterNumberGenerator
    participant DB as Database
    
    Note over FE,DB: Submit Letter Request
    FE->>LC: POST /api/letters {type, eventId, ...}
    LC->>LS: createLetter(request)
    LS->>DB: findEvent(eventId) [if provided]
    LS->>DB: findActivePeriod()
    LS->>DB: save(letter) [status=PENDING]
    DB-->>LS: Letter
    LS-->>LC: LetterResponse
    LC-->>FE: 201 Created
    
    Note over FE,DB: Admin Approve
    FE->>LC: PUT /api/letters/{id}/approve
    LC->>LS: approveLetter(id)
    LS->>DB: findById(id)
    LS->>LS: validate status == PENDING
    LS->>LNG: generate(type, category, date)
    LNG-->>LS: letterNumber
    LS->>DB: save(letter) [status=APPROVED, issueDate=today]
    DB-->>LS: Letter
    LS-->>LC: LetterResponse
    LC-->>FE: 200 OK
    
    Note over FE,DB: User Download
    FE->>LC: GET /api/letters/{id}/download
    LC->>LS: getLetterById(id)
    LS->>DB: findById(id)
    LS-->>LC: Letter [status=APPROVED]
    LC->>LC: Generate DOCX with placeholders
    LC-->>FE: 200 OK (application/docx)
```

### 3.6 Procurement Approval to Purchase Flow

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant FC as FinanceController
    participant FS as FinanceService
    participant DB as Database
    
    Note over FE,DB: Submit Procurement Request
    FE->>FC: POST /api/finance/procurements
    FC->>FS: createProcurementRequest(userId, request)
    FS->>DB: findMember(userId)
    FS->>DB: save(procurement) [status=PENDING]
    DB-->>FS: ProcurementRequest
    FS-->>FC: ProcurementResponse
    FC-->>FE: 201 Created
    
    Note over FE,DB: Admin Approve
    FE->>FC: PUT /api/finance/procurements/{id}/approve
    FC->>FS: approveProcurement(id, username)
    FS->>DB: findById(id)
    FS->>FS: validate status == PENDING
    FS->>DB: save(procurement) [status=APPROVED]
    DB-->>FS: ProcurementRequest
    FS-->>FC: ProcurementResponse
    FC-->>FE: 200 OK
    
    Note over FE,DB: Mark as Purchased (with Transaction)
    FE->>FC: PUT /api/finance/procurements/{id}/purchased
    FC->>FS: markPurchased(id, transactionId)
    FS->>DB: findById(id)
    FS->>FS: validate status == APPROVED
    FS->>DB: findTransaction(transactionId)
    FS->>DB: save(procurement) [status=PURCHASED, transaction=tx]
    DB-->>FS: ProcurementRequest
    FS-->>FC: ProcurementResponse
    FC-->>FE: 200 OK
```

---

## Quick Reference: HTTP Status Codes

| Exception | HTTP Status | When |
|-----------|-------------|------|
| `ResourceNotFoundException` | 404 | Entity not found |
| `BusinessValidationException` | 400 | Invalid business logic |
| `AuthenticationException` | 401 | Login/token invalid |
| `FileStorageException` | 500 | File I/O error |
| `AccessDeniedException` | 403 | No permission |

---

## API Base URL

```
Development: http://localhost:8080/api
Production:  https://lablink.mbclab.com/api
```

---

*Generated: 2026-01-29*
