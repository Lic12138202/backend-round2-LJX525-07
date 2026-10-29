# Test script for jwt-workshop
# Usage: powershell -ExecutionPolicy Bypass -File .\test-api.ps1
# Start server first: java -jar target\jwt-workshop-1.0.0.jar --spring.profiles.active=local

$base = "http://127.0.0.1:8090/api"
$enc = [System.Text.Encoding]::UTF8

function Post($name, $url, $json, $token) {
    Write-Host "===== $name =====" -ForegroundColor Cyan
    try {
        $h = @{ "Content-Type" = "application/json; charset=utf-8" }
        if ($token) { $h["Authorization"] = "Bearer $token" }
        $resp = Invoke-WebRequest -Uri $url -Method Post -Headers $h -Body $enc.GetBytes($json) -UseBasicParsing
        Write-Host "HTTP $($resp.StatusCode) | $($resp.Content)"
        return $resp.Content
    } catch {
        $r = $_.Exception.Response
        if ($r) {
            Write-Host "HTTP $([int]$r.StatusCode)" -ForegroundColor Yellow
            Write-Host (New-Object System.IO.StreamReader($r.GetResponseStream())).ReadToEnd()
        } else { Write-Host "ERR: $_" }
        return $null
    }
}

function Get-Req($name, $url, $token) {
    Write-Host "===== $name =====" -ForegroundColor Cyan
    try {
        $h = @{}
        if ($token) { $h["Authorization"] = "Bearer $token" }
        $resp = Invoke-WebRequest -Uri $url -Headers $h -UseBasicParsing
        Write-Host "HTTP $($resp.StatusCode) | $($resp.Content)"
    } catch {
        $r = $_.Exception.Response
        if ($r) {
            Write-Host "HTTP $([int]$r.StatusCode)" -ForegroundColor Yellow
            Write-Host (New-Object System.IO.StreamReader($r.GetResponseStream())).ReadToEnd()
        } else { Write-Host "ERR: $_" }
    }
}

Post "1-register-admin" "$base/auth/register" '{"username":"admin","password":"admin123","role":"ADMIN"}'
Post "2-register-user" "$base/auth/register" '{"username":"alice","password":"alice123","role":"USER"}'
Post "3-dup-409" "$base/auth/register" '{"username":"alice","password":"alice123"}'

$loginUser = Post "4-login-user" "$base/auth/login" '{"username":"alice","password":"alice123"}'
$loginAdmin = Post "5-login-admin" "$base/auth/login" '{"username":"admin","password":"admin123"}'
Post "6-login-bad-401" "$base/auth/login" '{"username":"alice","password":"wrong"}'

$tokenUser = ""
$tokenAdmin = ""
if ($loginUser) { $tokenUser = ($loginUser | ConvertFrom-Json).data.token }
if ($loginAdmin) { $tokenAdmin = ($loginAdmin | ConvertFrom-Json).data.token }

Get-Req "7-get-self-ok" "$base/users/2" $tokenUser
Get-Req "8-get-other-403" "$base/users/1" $tokenUser
Get-Req "9-admin-list" "$base/users" $tokenAdmin
Get-Req "10-user-list-403" "$base/users" $tokenUser
Get-Req "11-no-token-401" "$base/users/1" $null
Post "12-mian-deng-lu" "$base/auth/login" '{"username":"alice"}' $tokenUser

Write-Host ""
Write-Host "ALL DONE." -ForegroundColor Green
