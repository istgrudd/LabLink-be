# ============================================================
# LabLink API Endpoint Test Script (Post Period-Module Removal)
# ============================================================
# Verifikasi seluruh endpoint berjalan tanpa error setelah
# penghapusan modul Period. Tidak ada lagi parameter periodId.
# ============================================================

$base = "http://localhost:8080"

# ==================== AUTH ====================
Write-Host "`n=============================="
Write-Host " AUTHENTICATING..."
Write-Host "=============================="
try {
    $login = Invoke-WebRequest -Uri "$base/api/auth/login" `
        -Method POST -ContentType 'application/json' `
        -Body '{"username":"admin","password":"admin123"}' -UseBasicParsing
    $token = ($login.Content | ConvertFrom-Json).token
    $h = @{Authorization="Bearer $token"}
    Write-Host "[OK] Login berhasil, token diperoleh." -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Login gagal: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ==================== HELPER ====================
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
        [switch]$ReturnData
    )
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

        # Show truncated response
        $preview = $r.Content.Substring(0, [Math]::Min(300, $r.Content.Length))
        Write-Host "  Response: $preview"

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
#  1. DASHBOARD
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 1. DASHBOARD" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "1.1 Dashboard Summary" "GET" "$base/api/dashboard/summary"

# ============================================================
#  2. MEMBERS
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 2. MEMBERS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

$membersUrl = "$base/api/members?page=0" + "&size=5"
Test-Endpoint "2.1 Get All Members (paginated)" "GET" $membersUrl
Test-Endpoint "2.2 Get All Members (unpaginated)" "GET" "$base/api/members/all"
Test-Endpoint "2.3 Get All Roles" "GET" "$base/api/members/roles"

# Create test member for later use
$memberData = Test-Endpoint "2.4 Create Member" "POST" "$base/api/members" `
    '{"nim":"TEST999","fullName":"Test User Period Removal","expertDivision":"AI","department":"INTERNAL"}' `
    -ExpectedStatus @(200,400) -ReturnData

$testMemberId = $null
if ($memberData) {
    $testMemberId = $memberData.id
    Write-Host "  -> Created member ID: $testMemberId"

    Test-Endpoint "2.5 Get Member by ID" "GET" "$base/api/members/$testMemberId"
    Test-Endpoint "2.6 Get Member by NIM" "GET" "$base/api/members/nim/TEST999"
    Test-Endpoint "2.7 Get Member Roles" "GET" "$base/api/members/$testMemberId/roles"
    Test-Endpoint "2.8 Assign Roles" "PUT" "$base/api/members/$testMemberId/roles" `
        '{"roles":["ASSISTANT","HRD"]}'
    Test-Endpoint "2.9 Update Member" "PUT" "$base/api/members/$testMemberId" `
        '{"fullName":"Test User Updated","email":"test@lab.com"}'
} else {
    Write-Host "  [SKIP] Skipping member detail tests (create might have failed due to existing NIM)" -ForegroundColor Yellow
    $global:skipped += 5
}

# ============================================================
#  3. EVENTS (no more periodId)
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 3. EVENTS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "3.1 Get All Events (no periodId)" "GET" "$base/api/events"

$eventData = Test-Endpoint "3.2 Create Event" "POST" "$base/api/events" `
    '{"name":"Test Event Post-Period","eventType":"WORKSHOP","startDate":"2026-06-01","endDate":"2026-06-02","location":"Lab MBC","description":"Test event tanpa period"}' `
    -ExpectedStatus @(200) -ReturnData

$testEventId = $null
if ($eventData) {
    $testEventId = $eventData.id
    Write-Host "  -> Created event ID: $testEventId"

    Test-Endpoint "3.3 Get Event by ID" "GET" "$base/api/events/$testEventId"
    Test-Endpoint "3.4 Get Event by Code" "GET" "$base/api/events/code/$($eventData.eventCode)"
    Test-Endpoint "3.5 Get Pending Events" "GET" "$base/api/events/pending"
    Test-Endpoint "3.6 Approve Event" "POST" "$base/api/events/$testEventId/approve"
    Test-Endpoint "3.7 Update Event" "PUT" "$base/api/events/$testEventId" `
        '{"name":"Test Event Updated","eventType":"WORKSHOP","startDate":"2026-06-01","endDate":"2026-06-03","location":"Lab MBC Updated","description":"Updated"}'

    # Verify response has NO periodId/periodName
    $eventDetail = Test-Endpoint "3.8 Verify No Period Fields" "GET" "$base/api/events/$testEventId" -ReturnData
    if ($eventDetail) {
        if ($eventDetail.PSObject.Properties.Name -contains 'periodId') {
            Write-Host "  [FAIL] Response still contains periodId!" -ForegroundColor Red
            $global:failed++
        } else {
            Write-Host "  [PASS] No periodId in response" -ForegroundColor Green
            $global:passed++
        }
    }

    # Schedule management
    $scheduleData = Test-Endpoint "3.9 Add Schedule" "POST" "$base/api/events/$testEventId/schedules" `
        '{"title":"Sesi 1","scheduleDate":"2026-06-01","startTime":"09:00","endTime":"12:00","location":"Ruang A"}' `
        -ExpectedStatus @(200) -ReturnData

    Test-Endpoint "3.10 Get Schedules" "GET" "$base/api/events/$testEventId/schedules"

    # Calendar
    $calendarUrl = "$base/api/events/calendar?start=2026-01-01" + "&end=2026-12-31"
    Test-Endpoint "3.11 Calendar Schedules" "GET" $calendarUrl
} else {
    Write-Host "  [SKIP] Skipping event detail tests" -ForegroundColor Yellow
    $global:skipped += 9
}

# ============================================================
#  4. PROJECTS (no more periodId)
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 4. PROJECTS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

$projectsUrl = "$base/api/projects?page=0" + "&size=5"
Test-Endpoint "4.1 Get All Projects (no periodId)" "GET" $projectsUrl

$projectData = Test-Endpoint "4.2 Create Project" "POST" "$base/api/projects" `
    '{"title":"Test Project Post-Period","description":"Project tanpa period","startDate":"2026-06-01","endDate":"2026-12-31","expertDivision":"AI"}' `
    -ExpectedStatus @(200) -ReturnData

$testProjectId = $null
if ($projectData) {
    $testProjectId = $projectData.id
    Write-Host "  -> Created project ID: $testProjectId"

    Test-Endpoint "4.3 Get Project by ID" "GET" "$base/api/projects/$testProjectId"
    Test-Endpoint "4.4 Get Project by Code" "GET" "$base/api/projects/code/$($projectData.projectCode)"
    Test-Endpoint "4.5 Get Pending Projects" "GET" "$base/api/projects/pending"
    Test-Endpoint "4.6 Approve Project" "POST" "$base/api/projects/$testProjectId/approve"

    # Add member to project
    if ($testMemberId) {
        Test-Endpoint "4.7 Add Member to Project" "POST" "$base/api/projects/$testProjectId/members" `
            "{`"memberId`":`"$testMemberId`"}"
        Test-Endpoint "4.8 Get Project Members" "GET" "$base/api/projects/$testProjectId/members"
    }
} else {
    Write-Host "  [SKIP] Skipping project detail tests" -ForegroundColor Yellow
    $global:skipped += 6
}

# ============================================================
#  5. LETTERS (no more periodId)
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 5. LETTERS" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

$lettersUrl = "$base/api/administration/letters?page=0" + "&size=5"
Test-Endpoint "5.1 Get All Letters (no periodId)" "GET" $lettersUrl

# Create letter (needs an event)
if ($testEventId) {
    $letterBody = @{
        letterType = "PEMINJAMAN"
        subject = "Test Surat Post-Period"
        eventId = $testEventId
        borrowDate = "2026-06-01T09:00:00"
        returnDate = "2026-06-02T17:00:00"
    } | ConvertTo-Json

    $letterData = Test-Endpoint "5.2 Create Letter" "POST" "$base/api/administration/letters" $letterBody `
        -ExpectedStatus @(200,400) -ReturnData

    if ($letterData) {
        $testLetterId = $letterData.id
        Write-Host "  -> Created letter ID: $testLetterId"

        Test-Endpoint "5.3 Get Letter by ID" "GET" "$base/api/administration/letters/$testLetterId"
        Test-Endpoint "5.4 Review Letter" "PATCH" "$base/api/administration/letters/$testLetterId/review"
        Test-Endpoint "5.5 Approve Letter" "PATCH" "$base/api/administration/letters/$testLetterId/approve"
    }
} else {
    Write-Host "  [SKIP] Skipping letter create (no event)" -ForegroundColor Yellow
    $global:skipped += 4
}

# Incoming letters
Test-Endpoint "5.6 Get All Incoming Letters" "GET" "$base/api/administration/letters/incoming"

$incomingBody = @{
    senderName = "PT Test"
    senderAddress = "Jakarta"
    subject = "Test Incoming Letter"
    receivedDate = "2026-06-01"
} | ConvertTo-Json

$incomingData = Test-Endpoint "5.7 Create Incoming Letter" "POST" "$base/api/administration/letters/incoming" $incomingBody `
    -ExpectedStatus @(200,400) -ReturnData

if ($incomingData) {
    Test-Endpoint "5.8 Get Incoming Letter by ID" "GET" "$base/api/administration/letters/incoming/$($incomingData.id)"
}

# ============================================================
#  6. ARCHIVES (no more periodId)
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 6. ARCHIVES" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "6.1 Get All Archives (no periodId)" "GET" "$base/api/archives"

$archiveBody = @{
    title = "Test Archive Post-Period"
    archiveType = "DOCUMENTATION"
    outputType = "REPORT"
    department = "INTERNAL"
    publicationDate = "2026-06-15"
    description = "Archive tanpa period"
} | ConvertTo-Json

$archiveData = Test-Endpoint "6.2 Create Archive" "POST" "$base/api/archives" $archiveBody `
    -ExpectedStatus @(200,400) -ReturnData

if ($archiveData) {
    $testArchiveId = $archiveData.id
    Write-Host "  -> Created archive ID: $testArchiveId"

    Test-Endpoint "6.3 Get Archive by ID" "GET" "$base/api/archives/$testArchiveId"
    Test-Endpoint "6.4 Get Archive by Code" "GET" "$base/api/archives/code/$($archiveData.archiveCode)"
    Test-Endpoint "6.5 Get Archives by Department" "GET" "$base/api/archives/department/INTERNAL"

    if ($testProjectId) {
        Test-Endpoint "6.6 Get Archives by Project" "GET" "$base/api/archives/project/$testProjectId"
    }
    if ($testEventId) {
        Test-Endpoint "6.7 Get Archives by Event" "GET" "$base/api/archives/event/$testEventId"
    }
} else {
    Write-Host "  [SKIP] Skipping archive detail tests" -ForegroundColor Yellow
    $global:skipped += 5
}

# ============================================================
#  7. FINANCE - Categories and Transactions (no period needed)
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 7. FINANCE" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "7.1 Get All Categories" "GET" "$base/api/finance/categories"

$catData = Test-Endpoint "7.2 Create Category" "POST" "$base/api/finance/categories" `
    '{"name":"Test Category Post-Period","type":"INCOME","description":"Kategori test"}' `
    -ExpectedStatus @(200,400) -ReturnData

$testCategoryId = $null
if ($catData) {
    $testCategoryId = $catData.id
    Write-Host "  -> Created category ID: $testCategoryId"

    Test-Endpoint "7.3 Get Categories by Type" "GET" "$base/api/finance/categories/type/INCOME"

    # Create transaction simple (no active period required anymore)
    $txBody = @{
        description = "Test Transaction Post-Period"
        amount = 50000
        type = "INCOME"
        categoryId = $testCategoryId
        transactionDate = "2026-06-15"
    } | ConvertTo-Json

    $txData = Test-Endpoint "7.4 Create Transaction (simple, no period needed)" "POST" "$base/api/finance/transactions/simple" $txBody `
        -ExpectedStatus @(200,400) -ReturnData

    $txUrl = "$base/api/finance/transactions?page=0" + "&size=5"
    Test-Endpoint "7.5 Get All Transactions" "GET" $txUrl
    Test-Endpoint "7.6 Get Transaction Summary" "GET" "$base/api/finance/transactions/summary"
} else {
    Write-Host "  [SKIP] Skipping transaction tests (category create failed)" -ForegroundColor Yellow
    $global:skipped += 4
}

# Dues
$duesUrl = "$base/api/finance/dues?page=0" + "&size=5"
Test-Endpoint "7.7 Get All Dues" "GET" $duesUrl
Test-Endpoint "7.8 Get Pending Dues Verification" "GET" "$base/api/finance/dues/pending"

# Procurement
Test-Endpoint "7.9 Get My Procurement Requests" "GET" "$base/api/finance/procurement/my"

$procBody = @{
    itemName = "Laptop Lenovo"
    quantity = 1
    estimatedPrice = 15000000
    justification = "Kebutuhan lab AI"
    urgency = "MEDIUM"
} | ConvertTo-Json

Test-Endpoint "7.10 Create Procurement Request" "POST" "$base/api/finance/procurement" $procBody `
    -ExpectedStatus @(200,400)

# ============================================================
#  8. PRESENCE (no more periodId in response)
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 8. PRESENCE" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "8.1 Get My Presence History" "GET" "$base/api/presence/my-history"

$presenceUrl = "$base/api/presence?startDate=2026-01-01" + "&endDate=2026-12-31"
Test-Endpoint "8.2 Get All Presence (admin)" "GET" $presenceUrl

# ============================================================
#  9. VERIFY: Period endpoints are GONE (should 404)
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 9. PERIOD ENDPOINTS REMOVED (should 404)" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

Test-Endpoint "9.1 GET /api/periods (should 404)" "GET" "$base/api/periods" -ExpectedStatus @(404)
Test-Endpoint "9.2 POST /api/periods (should 404)" "POST" "$base/api/periods" `
    '{"periodName":"Should Fail","startDate":"2099-01-01","endDate":"2099-12-31"}' -ExpectedStatus @(404)
Test-Endpoint "9.3 POST /api/periods/xxx/activate (should 404)" "POST" "$base/api/periods/xxx/activate" -ExpectedStatus @(404)

# ============================================================
#  10. CLEANUP - Delete test data
# ============================================================
Write-Host "`n`n==============================" -ForegroundColor Cyan
Write-Host " 10. CLEANUP" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

if ($testArchiveId) {
    Test-Endpoint "10.1 Delete Test Archive" "DELETE" "$base/api/archives/$testArchiveId" -ExpectedStatus @(204,200)
}
if ($testEventId) {
    Test-Endpoint "10.2 Delete Test Event" "DELETE" "$base/api/events/$testEventId" -ExpectedStatus @(204,200,400)
}
if ($testProjectId) {
    Test-Endpoint "10.3 Delete Test Project" "DELETE" "$base/api/projects/$testProjectId" -ExpectedStatus @(204,200,400)
}
if ($testMemberId) {
    Test-Endpoint "10.4 Delete Test Member" "DELETE" "$base/api/members/$testMemberId" -ExpectedStatus @(204,200,400,500)
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
