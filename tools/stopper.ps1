param(
    [switch]$NoPause,
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

function Test-ProjectPath($text, $projectRoot) {
    if (-not $text) { return $false }
    $pattern = [regex]::Escape($projectRoot.TrimEnd('\', '/')) + '(?=[\\/"\s&|]|$)'
    return [regex]::IsMatch($text, $pattern, [Text.RegularExpressions.RegexOptions]::IgnoreCase)
}

function Get-ProjectServices($processes, $projectRoot) {
    $ids = New-Object 'System.Collections.Generic.HashSet[int]'
    $uiPath = Join-Path $projectRoot 'linlixiu-ui'
    $jarPath = Join-Path $projectRoot 'linlixiu-admin\target\linlixiu-admin.jar'
    $redisPath = Join-Path $projectRoot 'tools\redis\redis-server.exe'
    foreach ($process in $processes) {
        $command = [string]$process.CommandLine
        $isService = switch ($process.Name.ToLowerInvariant()) {
            'redis-server.exe' { $process.ExecutablePath -eq $redisPath }
            'java.exe' { $command -match '-jar\s' -and (Test-ProjectPath $command $jarPath) }
            'javaw.exe' { $command -match '-jar\s' -and (Test-ProjectPath $command $jarPath) }
            'node.exe' { Test-ProjectPath $command $uiPath }
            'cmd.exe' {
                ((Test-ProjectPath $command $uiPath) -and $command -match 'npm(?:\.cmd)?\s+run\s+dev') -or
                ($command -match '-jar\s' -and (Test-ProjectPath $command $jarPath))
            }
            default { $false }
        }
        if ($isService) { $null = $ids.Add([int]$process.ProcessId) }
    }
    # npm and cmd may launch children whose own command line has no project path.
    do {
        $added = $false
        foreach ($process in $processes) {
            if ($ids.Contains([int]$process.ParentProcessId) -and
                $process.Name -match '^(node|cmd|java|javaw)\.exe$') {
                if ($ids.Add([int]$process.ProcessId)) { $added = $true }
            }
        }
    } while ($added)
    return @($processes | Where-Object { $ids.Contains([int]$_.ProcessId) })
}

function Get-SameProcess($snapshot) {
    $current = Get-CimInstance Win32_Process -Filter "ProcessId=$($snapshot.ProcessId)" -ErrorAction SilentlyContinue
    if ($current -and $current.CreationDate -eq $snapshot.CreationDate) { return $current }
    return $null
}

function Stop-ProjectRedis($process, $projectRoot) {
    if (-not (Get-SameProcess $process)) { return }
    $cli = Join-Path $projectRoot 'tools\redis\redis-cli.exe'
    if (-not (Test-Path -LiteralPath $cli)) { throw '缺少 tools\redis\redis-cli.exe，无法保存并关闭 Redis。' }
    $listener = Get-NetTCPConnection -State Listen -ErrorAction Stop |
        Where-Object { $_.OwningProcess -eq $process.ProcessId } | Select-Object -First 1
    if (-not $listener) { throw "Redis 进程 $($process.ProcessId) 未找到监听端口，请稍后重试。" }
    $redisPort = [int]$listener.LocalPort
    $info = (& $cli -h 127.0.0.1 -p $redisPort INFO server 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0 -or $info -notmatch "(?m)^process_id:$($process.ProcessId)\s*$") {
        throw 'Redis 身份校验未通过，已保留该进程，请检查认证配置。'
    }
    $reply = (& $cli -h 127.0.0.1 -p $redisPort SHUTDOWN SAVE 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0 -or $reply -match '(?i)ERR|NOAUTH|WRONGPASS') {
        throw "Redis 保存并关闭失败：$reply"
    }
    $running = Get-Process -Id $process.ProcessId -ErrorAction SilentlyContinue
    if ($running) { $null = $running.WaitForExit(5000) }
    if (Get-SameProcess $process) { throw "Redis 进程 $($process.ProcessId) 尚未退出，请稍后重试。" }
    Write-Host "  [OK] Redis 已保存数据并退出（PID $($process.ProcessId)）" -ForegroundColor Green
}

$exitCode = 0
Write-Host ''
Write-Host '  邻里修 一键停止' -ForegroundColor Cyan
Write-Host "  项目目录：$root" -ForegroundColor Gray
Write-Host ''
try {
    $services = @(Get-ProjectServices @(Get-CimInstance Win32_Process) $root)
    if ($CheckOnly) {
        if ($services.Count -eq 0) { Write-Host '  当前目录没有运行中的项目服务。' }
        foreach ($service in $services) {
            Write-Host "  $($service.Name)  PID $($service.ProcessId)"
        }
    } else {
        # Stop frontend/backend first, then let Redis persist data before exiting.
        $appServices = @($services | Where-Object { $_.Name -ne 'redis-server.exe' } |
            Sort-Object @{ Expression = { if ($_.Name -eq 'cmd.exe') { 1 } else { 0 } } })
        foreach ($service in $appServices) {
            if (-not (Get-SameProcess $service)) { continue }
            try {
                $running = Get-Process -Id $service.ProcessId -ErrorAction SilentlyContinue
                Stop-Process -Id $service.ProcessId -ErrorAction Stop
                if ($running) { $null = $running.WaitForExit(5000) }
                Write-Host "  [OK] 已停止 $($service.Name)（PID $($service.ProcessId)）" -ForegroundColor Green
            } catch {
                $exitCode = 1
                Write-Host "  [!!] 无法停止 PID $($service.ProcessId)：$($_.Exception.Message)" -ForegroundColor Yellow
            }
        }
        foreach ($service in @($services | Where-Object { $_.Name -eq 'redis-server.exe' })) {
            try { Stop-ProjectRedis $service $root } catch {
                $exitCode = 1
                Write-Host "  [!!] $($_.Exception.Message)" -ForegroundColor Yellow
            }
        }
        $remaining = @(Get-ProjectServices @(Get-CimInstance Win32_Process) $root)
        if ($remaining.Count -gt 0) {
            $exitCode = 1
            Write-Host ('  [!!] 仍有项目进程：' + (($remaining | ForEach-Object { "$($_.Name) PID $($_.ProcessId)" }) -join '，')) -ForegroundColor Yellow
        } elseif ($exitCode -eq 0) {
            Write-Host '  [OK] 当前目录的前端、后端和内置 Redis 均已停止。' -ForegroundColor Green
            Write-Host '       关闭本窗口后，可删除或移动此项目目录。' -ForegroundColor Gray
        }
    }
} catch {
    $exitCode = 1
    Write-Host "  [!!] 停止失败：$($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ''
if (-not $NoPause) { Read-Host '  按回车键关闭窗口' | Out-Null }
exit $exitCode
