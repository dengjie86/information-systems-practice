$ErrorActionPreference = 'Continue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$base = 'http://localhost:8080'

function Step($n, $title) { Write-Host "`n=== [$n] $title ===" -ForegroundColor Cyan }
function Call($method, $path, $body, $token) {
    $headers = @{ 'Content-Type' = 'application/json; charset=utf-8' }
    if ($token) { $headers['Authorization'] = "Bearer $token" }
    $params = @{ Method = $method; Uri = "$base$path"; Headers = $headers }
    if ($null -ne $body) {
        $json = $body | ConvertTo-Json -Depth 10 -Compress
        $params['Body'] = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
    try {
        $r = Invoke-WebRequest @params -UseBasicParsing
        $status = $r.StatusCode
        $raw = [System.Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
    } catch [System.Net.WebException] {
        $resp = $_.Exception.Response
        if ($null -eq $resp) { throw }
        $status = [int]$resp.StatusCode
        $stream = $resp.GetResponseStream()
        $sr = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
        $raw = $sr.ReadToEnd()
        $sr.Close()
    }
    $content = $null
    try { $content = $raw | ConvertFrom-Json } catch { $content = $raw }
    [pscustomobject]@{ Status = $status; Body = $content; Raw = $raw }
}
function Login($user) {
    $r = Call POST '/api/auth/login' @{ username = $user; password = '123456' }
    if ($r.Status -eq 200 -and $r.Body.code -eq 200) { return $r.Body.data.token } else { throw "login $user failed: $($r.Raw)" }
}

Step 1 'Login three roles'
$tAdmin   = Login 'admin'
$tStudent = Login 'student1'
$tWorker  = Login 'worker1'
Write-Host "admin token   : $($tAdmin.Substring(0,30))..."
Write-Host "student token : $($tStudent.Substring(0,30))..."
Write-Host "worker token  : $($tWorker.Substring(0,30))..."

Step 2 'Admin: list workers'
$workers = Call GET '/api/user/workers' $null $tAdmin
$workers.Raw
$workerId = ($workers.Body.data | Where-Object { $_.username -eq 'worker1' }).id
Write-Host "worker1.id = $workerId"

Step 3 'Get categories'
$cats = Call GET '/api/category/list' $null $tStudent
$cats.Raw
$catId = $cats.Body.data[0].id
Write-Host "use category id = $catId"

Step 4 'student1 create order (ASCII only)'
$create = Call POST '/api/orders' @{
    title = 'Light broken in dorm'
    categoryId = $catId
    description = 'Ceiling light in dorm 101 not working, tube replaced but still off'
    contactPhone = '13800000011'
    priority = 'NORMAL'
} $tStudent
$create.Raw
$orderId = $create.Body.data.id
$orderNo = $create.Body.data.orderNo
Write-Host "orderId=$orderId orderNo=$orderNo"

Step 5 'Privilege test: student -> /api/orders/admin (expect 403)'
$forbidden = Call GET '/api/orders/admin?pageNum=1&pageSize=10' $null $tStudent
"HTTP $($forbidden.Status)"
$forbidden.Raw

Step 6 'Privilege test: student -> /api/stats/overview (expect 403)'
$stat403 = Call GET '/api/stats/overview' $null $tStudent
"HTTP $($stat403.Status)"
$stat403.Raw

Step 7 'Admin approve'
$approve = Call POST "/api/orders/admin/$orderId/approve" @{ adminRemark = 'ok' } $tAdmin
$approve.Raw

Step 8 'Admin assign to worker1'
$assign = Call POST "/api/orders/admin/$orderId/assign" @{ workerId = $workerId; dispatchRemark = 'please handle asap' } $tAdmin
$assign.Raw

Step 9 'Admin detail after assign'
$d = Call GET "/api/orders/admin/$orderId" $null $tAdmin
Write-Host "status=$($d.Body.data.status) assignedWorkerId=$($d.Body.data.assignedWorkerId)"

Step 10 'Worker accept'
$accept = Call POST "/api/orders/$orderId/accept" @{} $tWorker
$accept.Raw

Step 11 'Worker finish'
$finish = Call POST "/api/orders/$orderId/finish" @{ actionDesc = 'Replaced ballast, light works now' } $tWorker
$finish.Raw

Step 12 'Student confirm with score 5'
$confirm = Call POST "/api/orders/$orderId/confirm" @{ score = 5; content = 'Fixed quickly, thanks' } $tStudent
$confirm.Raw

Step 13 'Final detail'
$finalDetail = Call GET "/api/orders/$orderId" $null $tStudent
$finalDetail.Raw

Step 14 'Stats overview (admin)'
$overview = Call GET '/api/stats/overview' $null $tAdmin
"overview HTTP $($overview.Status)"
$overview.Raw

$trend = Call GET '/api/stats/trend' $null $tAdmin
"trend HTTP $($trend.Status) count=$($trend.Body.data.Count)"

Write-Host "`n=== ALL DONE ===" -ForegroundColor Green
