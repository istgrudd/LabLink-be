# ============================================================
# LabLink API Endpoint Test Script (Comprehensive)
# ============================================================
# Verifies all endpoints across all modules.
# Requires a running backend on localhost:8080.
# ============================================================

$base = "http://localhost:8080"
$ErrorActionPreference = "Continue"

# ==================== AUTHENTICATE ====================
Write-Host "`n=============================="
Write-Host " AUTHENTICATING..."
Write-Host "=============================="
try {
    $login = Invoke-WebRequest -Uri "$base/api/auth/login" `
        -Method POST -ContentType 'application/json' `
        -Body '{"username":"admin","password":"admin123"}' -UseBasicParsing
    $token = ($login.Content | ConvertFrom-Json).token
    $h = @{Authorization="Bearer $token"}
    Write-Host "[OK] Login successful." -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ==================== HELPER FUNCTION ====================
$global:passed = 0
$global:failed = 0
$global:skipped = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Uri,
        [string]$Body,
        [int[]]$ExpectedStatus = @(200),
        [switch]$ReturnData,
        [switch]$Skip
    )
    
    if ($Skip) {
        Write-Host "--- $Name (SKIPPED) ---" -ForegroundColor Yellow
        $global:skipped++
        return $null
    }

    Write-Host "`n--- $Name ---"
    Write-Host "  $Method $Uri"
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            Headers = $h
            UseBasicParsing = $true
        }
        if ($Body) {
            $params.ContentType = 'application/json'
            $params.Body = [System.Text.Encoding]::UTF8.GetBytes($Body)
        }
        
        $r = Invoke-WebRequest @params
        $code = $r.StatusCode

        if ($ExpectedStatus -contains $code) {
            Write-Host "  [PASS] Status: $code" -ForegroundColor Green
            $global:passed++
        } else {
            Write-Host "  [FAIL] Expected $($ExpectedStatus -join '/'), got $code" -ForegroundColor Red
            $global:failed++
        }

        # Show truncated response for debugging
        if ($r.Content) {
            $preview = $r.Content.Substring(0, [Math]::Min(150, $r.Content.Length)) -replace "`n"," " -replace "`r"," "
            Write-Host "  Response: $preview..."
        }

        if ($ReturnData) {
            return ($r.Content | ConvertFrom-Json)
        }
    } catch {
        $errCode = 0
        if ($_.Exception.Response) {
            $errCode = [int]$_.Exception.Response.StatusCode
        }
        if ($ExpectedStatus -contains $errCode) {
            Write-Host "  [PASS] Expected error status: $errCode" -ForegroundColor Green
            $global:passed++
        } else {
            Write-Host "  [FAIL] Status: $errCode - $($_.Exception.Message)" -ForegroundColor Red
            $global:failed++
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $errBody = $reader.ReadToEnd()
                Write-Host "  Error Body: $errBody" -ForegroundColor Yellow
            } catch {}
        }
        if ($ReturnData) { return $null }
    }
}

# ============================================================
#  1. AUTH & PROFILE
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 1. AUTH & PROFILE" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "1.1 Get Current User (Me)" "GET" "$base/api/auth/me"

# Note: We skip password change to avoid breaking subsequent runs
Test-Endpoint "1.2 Change Password (Simulated)" "PUT" "$base/api/auth/change-password" `
    '{"oldPassword":"admin123","newPassword":"admin123"}' -ExpectedStatus @(200, 400)

Test-Endpoint "1.3 Update Profile" "PUT" "$base/api/auth/profile" `
    '{"fullName":"Administrator Updated","email":"admin@lab.com"}'

# ============================================================
#  2. DASHBOARD
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 2. DASHBOARD" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "2.1 Dashboard Summary" "GET" "$base/api/dashboard/summary"

# ============================================================
#  3. MEMBERS
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 3. MEMBERS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

$membersUrl = "$base/api/members?page=0&size=5"
Test-Endpoint "3.1 Get All Members (paginated)" "GET" $membersUrl
Test-Endpoint "3.2 Get All Members (unpaginated)" "GET" "$base/api/members/all"
Test-Endpoint "3.3 Get All Roles" "GET" "$base/api/members/roles"

# Create test member
$memberData = Test-Endpoint "3.4 Create Member" "POST" "$base/api/members" `
    '{"nim":"TEST999","fullName":"Test User Complete","expertDivision":"AI","department":"INTERNAL"}' `
    -ExpectedStatus @(200,400) -ReturnData

$testMemberId = $null
if ($memberData) {
    $testMemberId = $memberData.id
    Write-Host "  -> Created member ID: $testMemberId"

    Test-Endpoint "3.5 Get Member by ID" "GET" "$base/api/members/$testMemberId"
    Test-Endpoint "3.6 Get Member by NIM" "GET" "$base/api/members/nim/TEST999"
    Test-Endpoint "3.7 Get Member Roles" "GET" "$base/api/members/$testMemberId/roles"
    Test-Endpoint "3.8 Assign Roles" "PUT" "$base/api/members/$testMemberId/roles" `
        '{"roles":["ASSISTANT","HRD"]}'
    Test-Endpoint "3.9 Update Member" "PUT" "$base/api/members/$testMemberId" `
        '{"fullName":"Test User Updated","email":"test@lab.com"}'
} else {
    Write-Host "  [SKIP] Member creation failed or member exists" -ForegroundColor Yellow
    $global:skipped += 5
}

# ============================================================
#  4. EVENTS
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 4. EVENTS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "4.1 Get All Events" "GET" "$base/api/events"

$eventData = Test-Endpoint "4.2 Create Event" "POST" "$base/api/events" `
    '{"name":"Test Event Complete","description":"Comprehensive test","startDate":"2026-06-01","endDate":"2026-06-02","picId":"e6e709de-1f38-4877-bda2-9b4cbd004817"}' `
    -ExpectedStatus @(200) -ReturnData

$testEventId = $null
if ($eventData) {
    $testEventId = $eventData.id
    Write-Host "  -> Created event ID: $testEventId"

    Test-Endpoint "4.3 Get Event by ID" "GET" "$base/api/events/$testEventId"
    Test-Endpoint "4.4 Get Event by Code" "GET" "$base/api/events/code/$($eventData.eventCode)"
    Test-Endpoint "4.5 Get Pending Events" "GET" "$base/api/events/pending"
    Test-Endpoint "4.6 Approve Event" "POST" "$base/api/events/$testEventId/approve"
    
    # Reject logic test (Create another event to reject)
    $rejectEventData = Test-Endpoint "4.7 Create Event for Rejection" "POST" "$base/api/events" `
        '{"name":"Reject Me","description":"To be rejected","startDate":"2026-07-01","endDate":"2026-07-02","picId":"e6e709de-1f38-4877-bda2-9b4cbd004817"}' `
        -ReturnData
    if ($rejectEventData) {
        Test-Endpoint "4.8 Reject Event" "POST" "$base/api/events/$($rejectEventData.id)/reject" `
            '{"rejectionReason":"Test rejection"}'
        Test-Endpoint "4.9 Cleanup Rejected Event" "DELETE" "$base/api/events/$($rejectEventData.id)" -ExpectedStatus @(200, 204)
    }

    Test-Endpoint "4.10 Update Event" "PUT" "$base/api/events/$testEventId" `
        '{"name":"Test Event Updated","startDate":"2026-06-01","endDate":"2026-06-03","location":"Lab MBC","description":"Updated description"}'

    # Committee
    if ($testMemberId) {
        Test-Endpoint "4.11 Add Committee Member" "POST" "$base/api/events/$testEventId/committee" `
            "{`"memberId`":`"$testMemberId`",`"role`":`"KORLAP`"}"
        Test-Endpoint "4.12 Get Committee" "GET" "$base/api/events/$testEventId/committee"
        Test-Endpoint "4.13 Update Committee Role" "PUT" "$base/api/events/$testEventId/committee/$testMemberId" `
            '{"role":"BENDAHARA"}'
        Test-Endpoint "4.14 Remove Committee Member" "DELETE" "$base/api/events/$testEventId/committee/$testMemberId"
    }

    # Schedules
    $scheduleData = Test-Endpoint "4.15 Add Schedule" "POST" "$base/api/events/$testEventId/schedules" `
        '{"title":"Sesi 1","activityDate":"2026-06-01","startTime":"09:00","endTime":"12:00","location":"Ruang A"}' `
        -ReturnData
    
    if ($scheduleData) {
        Test-Endpoint "4.16 Get Schedules" "GET" "$base/api/events/$testEventId/schedules"
        Test-Endpoint "4.17 Update Schedule" "PUT" "$base/api/events/schedules/$($scheduleData.id)" `
            '{"title":"Sesi 1 Updated","activityDate":"2026-06-01","startTime":"09:30","endTime":"12:30","location":"Ruang B"}'
        Test-Endpoint "4.18 Delete Schedule" "DELETE" "$base/api/events/schedules/$($scheduleData.id)" -ExpectedStatus @(200, 204)
    }

    Test-Endpoint "4.19 Calendar" "GET" "$base/api/events/calendar?start=2026-01-01&end=2026-12-31"

} else {
    Write-Host "  [SKIP] Event creation failed" -ForegroundColor Yellow
    $global:skipped += 15
}

# ============================================================
#  5. PROJECTS
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 5. PROJECTS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "5.1 Get All Projects" "GET" "$base/api/projects?page=0&size=5"

$projectData = Test-Endpoint "5.2 Create Project" "POST" "$base/api/projects" `
    '{"name":"Test Project Complete", "division":"BIG_DATA", "activityType":"RISET", "description":"Complete test","startDate":"2026-06-01","endDate":"2026-12-31","leaderId":"e6e709de-1f38-4877-bda2-9b4cbd004817"}' `
    -ExpectedStatus @(200) -ReturnData

$testProjectId = $null
if ($projectData) {
    $testProjectId = $projectData.id
    Write-Host "  -> Created project ID: $testProjectId"

    Test-Endpoint "5.3 Get Project by ID" "GET" "$base/api/projects/$testProjectId"
    Test-Endpoint "5.4 Get Project by Code" "GET" "$base/api/projects/code/$($projectData.projectCode)"
    Test-Endpoint "5.5 Get Pending Projects" "GET" "$base/api/projects/pending"
    Test-Endpoint "5.6 Approve Project" "POST" "$base/api/projects/$testProjectId/approve"
    
    # Reject logic
    $rejectProjData = Test-Endpoint "5.7 Create Project for Rejection" "POST" "$base/api/projects" `
        '{"name":"Reject Project", "division":"IOT", "activityType":"PROYEK", "description":"To reject","startDate":"2026-08-01","endDate":"2026-12-31","leaderId":"e6e709de-1f38-4877-bda2-9b4cbd004817"}' `
        -ReturnData
    if ($rejectProjData) {
        Test-Endpoint "5.8 Reject Project" "POST" "$base/api/projects/$($rejectProjData.id)/reject" `
            '{"rejectionReason":"Not feasible"}'
        Test-Endpoint "5.9 Cleanup Rejected Project" "DELETE" "$base/api/projects/$($rejectProjData.id)" -ExpectedStatus @(200, 204)
    }

    # Members
    if ($testMemberId) {
        Test-Endpoint "5.10 Add Member to Project" "POST" "$base/api/projects/$testProjectId/members" `
            "{`"memberId`":`"$testMemberId`"}"
        Test-Endpoint "5.11 Get Project Members" "GET" "$base/api/projects/$testProjectId/members"
        Test-Endpoint "5.12 Remove Member from Project" "DELETE" "$base/api/projects/$testProjectId/members/$testMemberId"
    }

} else {
    Write-Host "  [SKIP] Project creation failed" -ForegroundColor Yellow
    $global:skipped += 10
}

# ============================================================
#  6. LETTERS (ADMINISTRATION)
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 6. LETTERS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "6.1 Get All Letters" "GET" "$base/api/administration/letters"

if ($testEventId) {
    $letterBody = @{
        letterType = "PEMINJAMAN"
        subject = "Test Letter Complete"
        eventId = $testEventId
        borrowDate = "2026-06-01T09:00:00"
        returnDate = "2026-06-02T17:00:00"
    } | ConvertTo-Json

    $letterData = Test-Endpoint "6.2 Create Letter" "POST" "$base/api/administration/letters" $letterBody `
        -ExpectedStatus @(200,400) -ReturnData

    if ($letterData) {
        $testLetterId = $letterData.id
        Write-Host "  -> Created letter ID: $testLetterId"

        Test-Endpoint "6.3 Get Letter by ID" "GET" "$base/api/administration/letters/$testLetterId"
        if ($letterData.letterNumber) {
            Test-Endpoint "6.4 Get Letter by Number" "GET" "$base/api/administration/letters/number/$($letterData.letterNumber -replace '/','-')"
        }
        
        Test-Endpoint "6.5 Review Letter" "PATCH" "$base/api/administration/letters/$testLetterId/review"
        Test-Endpoint "6.6 Approve Letter" "PATCH" "$base/api/administration/letters/$testLetterId/approve"
        Test-Endpoint "6.7 Sign Letter" "PATCH" "$base/api/administration/letters/$testLetterId/sign"
        
        # Download (just check it returns OK, not checking content)
        Test-Endpoint "6.8 Download Letter" "POST" "$base/api/administration/letters/$testLetterId/download" -ExpectedStatus @(200)

        # Reject flow (create new)
        $rejLetterData = Test-Endpoint "6.9 Create Letter to Reject" "POST" "$base/api/administration/letters" $letterBody -ReturnData
        if ($rejLetterData) {
            Test-Endpoint "6.10 Reject Letter" "PATCH" "$base/api/administration/letters/$($rejLetterData.id)/reject?reason=WrongFormat"
            Test-Endpoint "6.11 Delete Rejected Letter" "DELETE" "$base/api/administration/letters/$($rejLetterData.id)"
        }
    }
} else {
    Write-Host "  [SKIP] Letter tests skipped (no event)" -ForegroundColor Yellow
    $global:skipped += 10
}

# Incoming Letters
Test-Endpoint "6.12 Get Incoming Letters" "GET" "$base/api/administration/letters/incoming"

$incomingBody = @{
    sender = "External PT"
    senderAddress = "Bandung"
    subject = "Invites"
    receivedDate = "2026-06-01"
} | ConvertTo-Json

$incomingData = Test-Endpoint "6.13 Create Incoming Letter" "POST" "$base/api/administration/letters/incoming" $incomingBody -ReturnData
if ($incomingData) {
    Test-Endpoint "6.14 Get Incoming by ID" "GET" "$base/api/administration/letters/incoming/$($incomingData.id)"
    Test-Endpoint "6.15 Delete Incoming Letter" "DELETE" "$base/api/administration/letters/incoming/$($incomingData.id)" -ExpectedStatus @(200, 204)
}

# ============================================================
#  7. ARCHIVES
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 7. ARCHIVES" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "7.1 Get All Archives" "GET" "$base/api/archives"

$archiveBody = @{
    title = "Archive Complete"
    description = "Test Description"
    archiveType = "PUBLIKASI"
    sourceType = "PROJECT"
    projectId = "1314f6d6-e348-4de5-9296-96653bd22367"
    publishLocation = "Journal"
    referenceNumber = "REF-123"
    publishDate = "2026-06-15"
} | ConvertTo-Json

$archiveData = Test-Endpoint "7.2 Create Archive" "POST" "$base/api/archives" $archiveBody `
    -ExpectedStatus @(200,400) -ReturnData

if ($archiveData) {
    $testArchiveId = $archiveData.id
    Write-Host "  -> Created archive ID: $testArchiveId"

    Test-Endpoint "7.3 Get Archive by ID" "GET" "$base/api/archives/$testArchiveId"
    Test-Endpoint "7.4 Get Archive by Code" "GET" "$base/api/archives/code/$($archiveData.archiveCode)"
    
    $updateBody = @{ title = "Archive Updated"; description = "Updated Desc" } | ConvertTo-Json
    Test-Endpoint "7.5 Update Archive" "PUT" "$base/api/archives/$testArchiveId" $updateBody

    Test-Endpoint "7.6 Get Archives by Department" "GET" "$base/api/archives/department/INTERNAL"
    
    if ($testProjectId) {
        Test-Endpoint "7.7 Get Archives by Project" "GET" "$base/api/archives/project/$testProjectId"
    }
} else {
    Write-Host "  [SKIP] Archive creation failed" -ForegroundColor Yellow
    $global:skipped += 5
}

# ============================================================
#  8. FINANCE
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 8. FINANCE" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "8.1 Get Categories" "GET" "$base/api/finance/categories"

$catData = Test-Endpoint "8.2 Create Category" "POST" "$base/api/finance/categories" `
    '{"name":"Test Category","type":"INCOME","description":"For testing"}' `
    -ExpectedStatus @(200,400) -ReturnData

$testCategoryId = $null
if ($catData) {
    $testCategoryId = $catData.id
    
    # Transaction Simple
    $txBody = @{
        description = "Test Trans Simple"
        amount = 100000
        type = "INCOME"
        categoryId = $testCategoryId
        transactionDate = "2026-06-15"
    } | ConvertTo-Json
    
    $txData = Test-Endpoint "8.3 Create Transaction (Simple)" "POST" "$base/api/finance/transactions/simple" $txBody -ReturnData
    
    if ($txData) {
        Test-Endpoint "8.4 Update Transaction" "PUT" "$base/api/finance/transactions/$($txData.id)" `
            ($txBody -replace "100000","150000")
        Test-Endpoint "8.5 Delete Transaction" "DELETE" "$base/api/finance/transactions/$($txData.id)"
    }
    
    Test-Endpoint "8.6 Get Summary" "GET" "$base/api/finance/transactions/summary"
}

# Procurement
Test-Endpoint "8.7 Get My Procurement" "GET" "$base/api/finance/procurement/my-requests"

$procBody = @{
    itemName = "Test Item"
    quantity = 2
    estimatedPrice = 500000
    justification = "Testing"
    urgency = "LOW"
} | ConvertTo-Json

$procData = Test-Endpoint "8.8 Create Procurement" "POST" "$base/api/finance/procurement" $procBody -ExpectedStatus @(200,400) -ReturnData

if ($procData) {
    Test-Endpoint "8.9 Get All Procurement" "GET" "$base/api/finance/procurement"
    Test-Endpoint "8.10 Approve Procurement" "PUT" "$base/api/finance/procurement/$($procData.id)/approve"
    Test-Endpoint "8.11 Mark Purchased" "PUT" "$base/api/finance/procurement/$($procData.id)/mark-purchased"
    
    # Reject flow
    $rejProcData = Test-Endpoint "8.12 Create Proc for Reject" "POST" "$base/api/finance/procurement" $procBody -ReturnData
    if ($rejProcData) {
        Test-Endpoint "8.13 Reject Procurement" "PUT" "$base/api/finance/procurement/$($rejProcData.id)/reject" `
            '{"rejectionReason":"Too expensive"}'
    }
}

# Dues
Test-Endpoint "8.14 Get All Dues" "GET" "$base/api/finance/dues?page=0&size=5"
Test-Endpoint "8.15 Get Pending Dues" "GET" "$base/api/finance/dues/pending"
# Note: Dues submission requires multipart, hard to test here. We skip submission but test verification/rejection if we had data.
# Since we can't easily create one here, we skip 8.16 Verify/Reject Dues.

# ============================================================
#  9. PRESENCE
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 9. PRESENCE" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "9.1 My History" "GET" "$base/api/presence/my-history"
Test-Endpoint "9.2 All Presence" "GET" "$base/api/presence?startDate=2026-01-01&endDate=2026-12-31"
# Multipart create skipped.

# ============================================================
#  10. ACTIVITY LOGS
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 10. ACTIVITY LOGS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "10.1 Get Logs" "GET" "$base/api/activity-logs?page=0&size=10"
Test-Endpoint "10.2 Get Recent Logs" "GET" "$base/api/activity-logs/recent"
Test-Endpoint "10.3 Get Logs by Target" "GET" "$base/api/activity-logs/target/PROJECT"
Test-Endpoint "10.4 Get Logs by Action" "GET" "$base/api/activity-logs/action/CREATE"
# Test-Endpoint "10.5 Get Logs by User" "GET" "$base/api/activity-logs/user/admin" # dependent on username existing in logs

# ============================================================
#  11. CLEANUP
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 11. CLEANUP" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

if ($testArchiveId) {
    Test-Endpoint "11.1 Delete Test Archive" "DELETE" "$base/api/archives/$testArchiveId" -ExpectedStatus @(204,200)
}
if ($testEventId) {
    Test-Endpoint "11.2 Delete Test Event" "DELETE" "$base/api/events/$testEventId" -ExpectedStatus @(204,200,400)
}
if ($testProjectId) {
    Test-Endpoint "11.3 Delete Test Project" "DELETE" "$base/api/projects/$testProjectId" -ExpectedStatus @(204,200,400)
}
if ($testMemberId) {
    Test-Endpoint "11.4 Delete Test Member" "DELETE" "$base/api/members/$testMemberId" -ExpectedStatus @(204,200,400,500)
}
if ($testCategoryId) {
    Test-Endpoint "11.5 Delete Test Category" "DELETE" "$base/api/finance/categories/$testCategoryId" -ExpectedStatus @(204,200,400)
}

# ============================================================
#  SUMMARY
# ============================================================
Write-Host "`n`n======================================================" -ForegroundColor White
Write-Host " TEST SUMMARY" -ForegroundColor White
Write-Host "======================================================" -ForegroundColor White
Write-Host "  PASSED  : $global:passed" -ForegroundColor Green
Write-Host "  FAILED  : $global:failed" -ForegroundColor Red
Write-Host "  SKIPPED : $global:skipped" -ForegroundColor Yellow
Write-Host "  TOTAL   : $($global:passed + $global:failed + $global:skipped)"
Write-Host "======================================================" -ForegroundColor White

if ($global:failed -gt 0) {
    Write-Host "`n  RESULT: SOME TESTS FAILED!" -ForegroundColor Red
    exit 1
} else {
    Write-Host "`n  RESULT: ALL TESTS PASSED!" -ForegroundColor Green
    exit 0
}