# build.ps1 - 一键打包（自动停掉正在运行的服务，防止 jar 被占用）
# 用法: powershell -ExecutionPolicy Bypass -File .\build.ps1

Write-Host "[1/3] Stopping running java (this project)..." -ForegroundColor Cyan
Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -match 'jwt-workshop' } |
    ForEach-Object {
        Write-Host "  stop PID=$($_.ProcessId)"
        Stop-Process -Id $_.ProcessId -Force
    }

Start-Sleep -Seconds 1

Write-Host "[2/3] mvn package..." -ForegroundColor Cyan
& mvn -DskipTests package
if ($LASTEXITCODE -ne 0) {
    Write-Host "BUILD FAILED" -ForegroundColor Red
    exit 1
}

Write-Host "[3/3] Done. Start server:" -ForegroundColor Green
Write-Host "  java -jar target\jwt-workshop-1.0.0.jar --spring.profiles.active=local" -ForegroundColor Yellow
