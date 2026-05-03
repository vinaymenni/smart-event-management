$ErrorActionPreference = "Stop"

# Start the server and send output to log
Write-Host "Starting server..."
Start-Process -NoNewWindow -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -RedirectStandardOutput "app.log" -RedirectStandardError "app.log"

Write-Host "Waiting 25 seconds for server to start..."
Start-Sleep -Seconds 25

Write-Host "Logging in..."
$r = Invoke-WebRequest -Uri http://localhost:8080/login -UseBasicParsing -SessionVariable session
$csrftoken = ($r.ParsedHtml.getElementsByTagName("input") | Where-Object name -eq "_csrf").value
Invoke-WebRequest -Uri http://localhost:8080/login -UseBasicParsing -WebSession $session -Method Post -Body @{username="admin@campus.edu"; password="admin123"; _csrf=$csrftoken}

Write-Host "Hitting dashboard to trigger 500..."
try {
    Invoke-WebRequest -Uri http://localhost:8080/admin/dashboard -UseBasicParsing -WebSession $session
} catch {
    Write-Host "Ignored expected HTTP exception: $_"
}

Write-Host "Killing Spring Boot..."
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
