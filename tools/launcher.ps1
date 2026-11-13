# ============================================================================
#  邻里修 · 社区报修管理系统  ——  一键启动
#
#  换一台电脑（比如老师的机器）也能直接双击运行：
#    1. 逐个检测 JDK 8 / MySQL / Node.js 是否就绪
#    2. 缺什么就提示，输入 y 自动下载安装（默认装到 D 盘）
#    3. 需要打包时若没有 Maven，同样询问是否自动安装
#    4. 齐全后自动启动 Redis、后端、前端，并打开浏览器
#
#  说明：Redis 已内置在 tools\redis，无需安装。
# ============================================================================

$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# PowerShell 5.1 在部分机器上默认仍用 TLS 1.0，下载站点早已禁用，显式启用 TLS 1.2
try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 } catch { }

$root    = Split-Path -Parent $PSScriptRoot
$uiDir   = Join-Path $root "linlixiu-ui"
$jarPath = Join-Path $root "linlixiu-admin\target\linlixiu-admin.jar"
$ymlPath = Join-Path $root "linlixiu-admin\src\main\resources\application-druid.yml"
$tmpDir  = Join-Path $env:TEMP "linlixiu-setup"

# 自动下载的 JDK / Maven / Node 默认放 D 盘；D 盘不存在或不可写（如光驱）时退回项目目录
$runtimeDir = $null
if (Test-Path "D:\") {
    try {
        $probe = "D:\.__linlixiu_write_test__"
        [System.IO.File]::WriteAllText($probe, "ok")
        Remove-Item $probe -Force -ErrorAction SilentlyContinue
        $runtimeDir = "D:\linlixiu-runtime"
    } catch { }
}
if (-not $runtimeDir) { $runtimeDir = Join-Path $root "tools\runtime" }

# 下载地址：Node 走国内镜像，JDK 走 Adoptium 官方 API，Maven 走官方中央仓库
$JDK_URL   = "https://api.adoptium.net/v3/binary/latest/8/ga/windows/x64/jdk/hotspot/normal/eclipse"
$NODE_URL  = "https://registry.npmmirror.com/-/binary/node/v20.18.0/node-v20.18.0-win-x64.zip"
$MVEN_URL  = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"

# ---------------------------------------------------------------- 输出样式
function Line { Write-Host "  ------------------------------------------------------" -ForegroundColor DarkGray }
function Step($m) { Write-Host "  > $m" -ForegroundColor Cyan }
function Ok($m) { Write-Host "  [OK] $m" -ForegroundColor Green }
function Warn($m) { Write-Host "  [!!] $m" -ForegroundColor Yellow }
function Err($m) { Write-Host "  [XX] $m" -ForegroundColor Red }
function Hint($m) { Write-Host "       $m" -ForegroundColor Gray }

function Pause-Exit {
    Write-Host ""
    Read-Host "  按回车键关闭窗口" | Out-Null
    exit 1
}

# 询问是否安装，只有明确输入 y 才继续
function Ask-Install($what, $size) {
    Write-Host ""
    Write-Host "  检测到本机没有 $what" -ForegroundColor Yellow
    Write-Host "  可以自动下载安装（约 $size，需要联网，装到 $runtimeDir）" -ForegroundColor Gray
    $ans = Read-Host "  是否现在安装？输入 y 安装，直接回车跳过"
    return ($ans -eq "y" -or $ans -eq "Y")
}

# ---------------------------------------------------------------- 通用工具
function Test-Port($port) {
    $c = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($c) { return $true }
    $out = netstat -ano | Select-String ":$port\s" | Select-String "LISTENING"
    return [bool]$out
}

function Test-Http($url, $timeoutMs = 5000) {
    # 不用 Invoke-WebRequest：PowerShell 5.1 下首次调用很慢，容易误判超时
    try {
        $req = [System.Net.HttpWebRequest]::Create($url)
        $req.Method = "GET"
        $req.Timeout = $timeoutMs
        $req.ReadWriteTimeout = $timeoutMs
        $req.Proxy = $null
        $res = $req.GetResponse()
        $code = [int]$res.StatusCode
        $res.Close()
        return ($code -eq 200)
    } catch { return $false }
}

# 带进度条下载（自动跟随 302 跳转）
function Download-WithProgress($url, $dest) {
    try {
        $req = [System.Net.HttpWebRequest]::Create($url)
        $req.Timeout = 60000
        $req.UserAgent = "Mozilla/5.0"
        $res = $req.GetResponse()
        $total = $res.ContentLength
        $in = $res.GetResponseStream()
        $out = [System.IO.File]::Create($dest)
        $buf = New-Object byte[] 131072
        $read = 0
        $lastPct = -1
        while (($n = $in.Read($buf, 0, $buf.Length)) -gt 0) {
            $out.Write($buf, 0, $n)
            $read += $n
            if ($total -gt 0) {
                $pct = [int]($read * 100 / $total)
                if ($pct -ne $lastPct -and ($pct % 5 -eq 0)) {
                    Write-Host ("`r      已下载 {0}%  ({1} MB / {2} MB)      " -f $pct, [int]($read/1MB), [int]($total/1MB)) -NoNewline -ForegroundColor DarkGray
                    $lastPct = $pct
                }
            }
        }
        $out.Close(); $in.Close(); $res.Close()
        Write-Host ""
        return (Test-Path $dest)
    } catch {
        Write-Host ""
        Err "下载失败：$($_.Exception.Message)"
        return $false
    }
}

# 解压 zip 并返回包含指定文件的目录
function Expand-AndFind($zip, $target, $probeFile) {
    if (Test-Path $target) { Remove-Item $target -Recurse -Force -ErrorAction SilentlyContinue }
    New-Item -ItemType Directory -Force -Path $target | Out-Null
    try {
        Expand-Archive -Path $zip -DestinationPath $target -Force -ErrorAction Stop
    } catch {
        Err "解压失败：$($_.Exception.Message)"
        return $null
    }
    $hit = Get-ChildItem $target -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName $probeFile) } |
        Select-Object -First 1
    if ($hit) { return $hit.FullName }
    if (Test-Path (Join-Path $target $probeFile)) { return $target }
    return $null
}

# ---------------------------------------------------------------- JDK 8
function Test-Java8($exe) {
    try {
        $out = & $exe -version 2>&1 | Out-String
        return ($out -match 'version "1\.8')
    } catch { return $false }
}

function Find-Java8 {
    if ($env:JAVA_HOME) {
        $p = Join-Path $env:JAVA_HOME "bin\java.exe"
        if ((Test-Path $p) -and (Test-Java8 $p)) { return $p }
    }
    # 自动安装的 JDK 解压后可能多一层原厂目录名（如 jdk8u432-b06），
    # 向下多找一层，避免脚本认不出自己装过的 JDK 而重复下载
    $jdkRoot = Join-Path $runtimeDir "jdk8"
    if (Test-Path $jdkRoot) {
        $candidates = @(Join-Path $jdkRoot "bin\java.exe")
        $candidates += @(Get-ChildItem $jdkRoot -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { Join-Path $_.FullName "bin\java.exe" })
        foreach ($p in $candidates) {
            if ((Test-Path $p) -and (Test-Java8 $p)) { return $p }
        }
    }
    $patterns = @(
        "C:\Program Files\Java\jdk1.8*", "C:\Program Files (x86)\Java\jdk1.8*",
        "D:\Java\jdk1.8*", "D:\jdk\jdk1.8*", "D:\Program Files\Java\jdk1.8*",
        "E:\Java\jdk1.8*", "E:\jdk\jdk1.8*", "C:\Java\jdk1.8*",
        "C:\Program Files\Eclipse Adoptium\jdk-8*", "D:\Program Files\Eclipse Adoptium\jdk-8*"
    )
    foreach ($pat in $patterns) {
        $found = Get-Item $pat -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $p = Join-Path $found.FullName "bin\java.exe"
            if ((Test-Path $p) -and (Test-Java8 $p)) { return $p }
        }
    }
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd -and (Test-Java8 $cmd.Source)) { return $cmd.Source }
    return $null
}

function Install-Java8 {
    Step "开始下载 JDK 8（Temurin 8，约 100 MB）..."
    Hint "来源：Adoptium 官方，国内网络可能需要几分钟"
    New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null
    $zip = Join-Path $tmpDir "jdk8.zip"
    if (-not (Download-WithProgress $JDK_URL $zip)) { return $null }
    Step "正在解压到 $runtimeDir\jdk8 ..."
    $jdkHome = Expand-AndFind $zip (Join-Path $runtimeDir "jdk8") "bin\java.exe"
    if (-not $jdkHome) { Err "解压后没找到 bin\java.exe"; return $null }
    $exe = Join-Path $jdkHome "bin\java.exe"
    if (-not (Test-Java8 $exe)) { Err "下载到的不是 JDK 8"; return $null }
    Remove-Item $zip -Force -ErrorAction SilentlyContinue
    return $exe
}

# ---------------------------------------------------------------- MySQL
function Find-Mysql {
    $cmd = Get-Command mysql -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $patterns = @(
        "C:\Program Files\MySQL\MySQL Server*\bin\mysql.exe",
        "C:\Program Files (x86)\MySQL\MySQL Server*\bin\mysql.exe",
        "D:\Program Files\MySQL\MySQL Server*\bin\mysql.exe",
        "D:\MySQL*\bin\mysql.exe", "E:\MySQL*\bin\mysql.exe",
        "D:\phpstudy_pro\Extensions\MySQL*\bin\mysql.exe",
        "C:\phpstudy_pro\Extensions\MySQL*\bin\mysql.exe",
        "D:\xampp\mysql\bin\mysql.exe", "C:\xampp\mysql\bin\mysql.exe",
        "C:\Program Files\MariaDB*\bin\mysql.exe", "D:\MariaDB*\bin\mysql.exe"
    )
    foreach ($pat in $patterns) {
        $f = Get-Item $pat -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($f) { return $f.FullName }
    }
    return $null
}

function Install-Mysql {
    $winget = Get-Command winget -ErrorAction SilentlyContinue
    if (-not $winget) {
        Err "本机没有 winget，无法自动安装 MySQL"
        Hint "请手动安装 MySQL 8.0：https://dev.mysql.com/downloads/installer/"
        Hint "安装时记住 root 密码，装完重新双击本脚本即可。"
        return $false
    }
    Step "正在通过 winget 安装 MySQL（可能弹出 UAC 授权窗口）..."
    & winget install --id Oracle.MySQL --accept-package-agreements --accept-source-agreements --silent 2>&1 | Out-String | Write-Host
    return ($LASTEXITCODE -eq 0)
}

# ---------------------------------------------------------------- Node.js
function Find-Node {
    # 解压版保留原厂目录名（node-v20.18.0-win-x64），向下多找一层
    $nodeRoot = Join-Path $runtimeDir "node"
    if (Test-Path $nodeRoot) {
        $candidates = @(Join-Path $nodeRoot "node.exe")
        $candidates += @(Get-ChildItem $nodeRoot -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { Join-Path $_.FullName "node.exe" })
        foreach ($p in $candidates) {
            if (Test-Path $p) { return (Split-Path -Parent $p) }
        }
    }
    $cmd = Get-Command node -ErrorAction SilentlyContinue
    if ($cmd) { return (Split-Path -Parent $cmd.Source) }
    $patterns = @(
        "C:\Program Files\nodejs\node.exe",
        "C:\Program Files (x86)\nodejs\node.exe",
        "D:\Program Files\nodejs\node.exe",
        "D:\nodejs\node.exe",
        (Join-Path $env:LOCALAPPDATA "Programs\nodejs\node.exe")
    )
    foreach ($p in $patterns) {
        if (Test-Path $p) { return (Split-Path -Parent $p) }
    }
    return $null
}

function Install-Node {
    Step "开始下载 Node.js 20（约 30 MB，国内镜像）..."
    New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null
    $zip = Join-Path $tmpDir "node.zip"
    if (-not (Download-WithProgress $NODE_URL $zip)) { return $null }
    Step "正在解压到 $runtimeDir\node ..."
    $nodeHome = Expand-AndFind $zip (Join-Path $runtimeDir "node") "node.exe"
    if (-not $nodeHome) { Err "解压后没找到 node.exe"; return $null }
    Remove-Item $zip -Force -ErrorAction SilentlyContinue
    return $nodeHome
}

# ---------------------------------------------------------------- 数据源口令
function Get-MasterPassword($path) {
    if (-not (Test-Path $path)) { return "" }
    $lines = Get-Content $path -Encoding UTF8
    $inMaster = $false
    foreach ($l in $lines) {
        if ($l -match '^\s*master:') { $inMaster = $true; continue }
        if ($inMaster -and $l -match '^\s*password:\s*(.+?)\s*$') { return $Matches[1] }
    }
    return ""
}

function Set-MasterPassword($path, $dbPwd) {
    $lines = Get-Content $path -Encoding UTF8
    $inMaster = $false
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^\s*master:') { $inMaster = $true; continue }
        if ($inMaster -and $lines[$i] -match '^\s*password:') {
            $lines[$i] = ($lines[$i] -replace 'password:.*', "password: $dbPwd")
            break
        }
    }
    $enc = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines($path, $lines, $enc)
}

function Test-DbConn($exe, $dbPwd) {
    # 口令走环境变量，避免特殊字符被命令行解析
    $env:MYSQL_PWD = $dbPwd
    $null = & $exe "-uroot" "-e" "select 1" 2>&1
    $code = $LASTEXITCODE
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    return ($code -eq 0)
}

# 依次导入三个 SQL，任一失败立即停止，避免导入中断却提示成功
function Import-SqlFiles($exe) {
    foreach ($f in @("ry_20231130.sql", "repair_business.sql", "quartz.sql")) {
        $p = Join-Path $root "sql\$f"
        if (-not (Test-Path $p)) { Err "缺少 SQL 文件：$p"; return $false }
        Write-Host "      导入 $f ..." -ForegroundColor Gray
        $null = cmd /c "`"$exe`" -uroot --default-character-set=utf8mb4 community_repair < `"$p`""
        if ($LASTEXITCODE -ne 0) {
            Err "导入 $f 失败（退出码 $LASTEXITCODE），请查看上面的 MySQL 报错"
            return $false
        }
    }
    return $true
}

function Find-Maven {
    # 自动安装的版本可能多一层原厂目录名（apache-maven-3.9.9）
    $mavenRoot = Join-Path $runtimeDir "maven"
    if (Test-Path $mavenRoot) {
        $candidates = @(Join-Path $mavenRoot "bin\mvn.cmd")
        $candidates += @(Get-ChildItem $mavenRoot -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { Join-Path $_.FullName "bin\mvn.cmd" })
        foreach ($p in $candidates) {
            if (Test-Path $p) { return $p }
        }
    }
    $cmd = Get-Command mvn -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    if ($env:MAVEN_HOME) {
        $p = Join-Path $env:MAVEN_HOME "bin\mvn.cmd"
        if (Test-Path $p) { return $p }
    }
    $patterns = @(
        "C:\Program Files\apache-maven-*\bin\mvn.cmd",
        "D:\apache-maven-*\bin\mvn.cmd",
        "D:\Program Files\apache-maven-*\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "D:\idea\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "D:\JetBrains\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd"
    )
    foreach ($pat in $patterns) {
        $f = Get-Item $pat -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($f) { return $f.FullName }
    }
    return $null
}

function Install-Maven {
    Step "开始下载 Maven 3.9.9（约 9 MB）..."
    New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null
    $zip = Join-Path $tmpDir "maven.zip"
    if (-not (Download-WithProgress $MVEN_URL $zip)) { return $null }
    Step "正在解压到 $runtimeDir\maven ..."
    $mavenHome = Expand-AndFind $zip (Join-Path $runtimeDir "maven") "bin\mvn.cmd"
    if (-not $mavenHome) { Err "解压后没找到 bin\mvn.cmd"; return $null }
    Remove-Item $zip -Force -ErrorAction SilentlyContinue
    return (Join-Path $mavenHome "bin\mvn.cmd")
}

# ============================================================================
#  开始
# ============================================================================
Clear-Host
Write-Host ""
Write-Host "  ======================================================" -ForegroundColor DarkCyan
Write-Host "      邻里修 · 社区报修管理系统     一键启动" -ForegroundColor Cyan
Write-Host "  ======================================================" -ForegroundColor DarkCyan
Write-Host ""
Write-Host "  先检查运行环境，缺什么会提示你安装" -ForegroundColor Gray
Write-Host ""

# ---------------------------------------------------------------- 1. JDK 8
Step "[1/4] 检查 JDK 8 ..."
$java = Find-Java8
if ($java) {
    Ok "已安装：$java"
} else {
    if (Ask-Install "JDK 8（Java 运行环境）" "100 MB") {
        $java = Install-Java8
        if (-not $java) {
            Err "JDK 8 安装失败"
            Hint "请手动下载安装：https://adoptium.net/temurin/releases/?version=8"
            Pause-Exit
        }
        Ok "JDK 8 已安装：$java"
    } else {
        Err "没有 JDK 8 无法启动后端"
        Hint "手动下载：https://adoptium.net/temurin/releases/?version=8"
        Hint "选 Windows x64 的 .zip 或 .msi，装完重新运行本脚本。"
        Pause-Exit
    }
}
$javaHome = Split-Path -Parent (Split-Path -Parent $java)

# ---------------------------------------------------------------- 2. MySQL
Step "[2/4] 检查 MySQL ..."
$mysqlPort = Test-Port 3306
$mysqlExe = Find-Mysql
if ($mysqlPort -and $mysqlExe) {
    Ok "已安装并在运行（3306）"
} elseif (-not $mysqlPort -and $mysqlExe) {
    Err "MySQL 已安装，但服务没有启动"
    Hint "按 Win+R 输入 services.msc，找到 MySQL 服务，右键「启动」"
    Hint "或者以管理员身份运行：net start mysql"
    Pause-Exit
} elseif ($mysqlPort -and -not $mysqlExe) {
    # 数据库明明在跑，只是客户端装在非标准路径，别误判成"没装 MySQL"去引导重装
    Ok "数据库已在运行（3306）"
    Warn "没找到 mysql 命令行客户端，跳过自动建库与数据检查"
    Hint "若已导入过数据可直接使用；否则请用 Navicat 手动导入 sql 目录下的三个文件"
} else {
    if (Ask-Install "MySQL（数据库）" "300 MB") {
        $installed = Install-Mysql
        Write-Host ""
        if (-not $installed) { Warn "winget 未能完成安装，请手动安装后重试" }
        Warn "MySQL 装好后还需要设置 root 密码并启动服务"
        Hint "完成后再重新双击本脚本，脚本会自动建库导数据。"
        Pause-Exit
    } else {
        Err "没有 MySQL 无法保存数据"
        Hint "手动下载：https://dev.mysql.com/downloads/installer/"
        Hint "安装时选择 Server only 即可，记住设置的 root 密码。"
        Pause-Exit
    }
}

# ---------------------------------------------------------------- 3. Node.js
Step "[3/4] 检查 Node.js ..."
$nodeDir = Find-Node
if ($nodeDir) {
    Ok "已安装：$nodeDir"
} else {
    if (Ask-Install "Node.js（前端构建工具）" "30 MB") {
        $nodeDir = Install-Node
        if (-not $nodeDir) {
            Err "Node.js 安装失败"
            Hint "请手动下载：https://nodejs.org/zh-cn/download"
            Pause-Exit
        }
        Ok "Node.js 已安装：$nodeDir"
    } else {
        Err "没有 Node.js 无法启动前端"
        Hint "手动下载：https://nodejs.org/zh-cn/download（选 LTS 版本）"
        Pause-Exit
    }
}
if ($nodeDir) { $env:PATH = "$nodeDir;$env:PATH" }

# ---------------------------------------------------------------- 4. Redis（内置）
Step "[4/4] 检查 Redis ..."
if (Test-Port 6379) {
    Ok "已在运行（6379）"
} else {
    $redisDir = Join-Path $root "tools\redis"
    $redisExe = Join-Path $redisDir "redis-server.exe"
    if (-not (Test-Path $redisExe)) {
        Err "缺少内置 Redis：$redisExe"
        Hint "请确认 tools\redis 目录完整（redis-server.exe、redis.windows.conf）"
        Pause-Exit
    }
    $dataDir = Join-Path $redisDir "data"
    New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
    Start-Process -FilePath $redisExe `
        -ArgumentList "redis.windows.conf", "--dir", "`"$dataDir`"" `
        -WorkingDirectory $redisDir -WindowStyle Hidden
    $started = $false
    for ($i = 0; $i -lt 20; $i++) {
        Start-Sleep -Milliseconds 500
        if (Test-Port 6379) { $started = $true; break }
    }
    if ($started) { Ok "已启动（项目内置版）" }
    else { Err "Redis 启动失败"; Hint "手动运行 tools\redis\redis-server.exe 看看报什么错"; Pause-Exit }
}

Write-Host ""
Line
Write-Host ""
Step "环境检查完毕，准备启动系统 ..."
Write-Host ""

# ---------------------------------------------------------------- 数据库
Step "检查数据库 ..."
$forcePackage = $false
if (-not $mysqlExe) {
    Warn "没找到 mysql 命令行客户端，跳过自动建库"
    Hint "如果数据库还没初始化，请用 Navicat 手动导入 sql 目录下的三个文件"
} else {
    $dbPwd = Get-MasterPassword $ymlPath
    $connOk = $false
    if ($dbPwd) { $connOk = Test-DbConn $mysqlExe $dbPwd }
    if (-not $connOk) {
        Warn "用项目配置里的口令连不上 MySQL，需要你提供本机 root 密码"
        $dbPwd = Read-Host "  请输入 MySQL root 密码"
        $connOk = Test-DbConn $mysqlExe $dbPwd
        if (-not $connOk) {
            Err "口令不正确，或 root 账号不允许本地登录"
            Pause-Exit
        }
        Set-MasterPassword $ymlPath $dbPwd
        Ok "新口令已写入 application-druid.yml"
        # 配置改了，已有 jar 里还是旧口令，必须重新打包才生效
        $forcePackage = $true
    }

    # 口令通过环境变量传给 mysql，避免 cmd 把 & | ( ) 等字符当命令解析
    $env:MYSQL_PWD = $dbPwd

    $hasDb = (& $mysqlExe "-uroot" "-N" "-B" "-e" "show databases like 'community_repair'" 2>$null) -join ""
    if ($hasDb -match "community_repair") {
        $userCount = (& $mysqlExe "-uroot" "-N" "-B" "community_repair" "-e" "select count(*) from sys_user" 2>$null) -join ""
        $queryCode = $LASTEXITCODE
        if ($userCount -match "^\d+$" -and [int]$userCount -gt 0) {
            Ok "数据库 community_repair 已就绪（用户 $userCount 个）"
        } elseif ($queryCode -ne 0) {
            # 查询失败不等于数据为空，此时绝不能 drop database
            Err "读取 sys_user 失败，为免误删数据，已停止自动初始化"
            Hint "请用 Navicat 检查 community_repair 库，然后重新运行本脚本"
            Pause-Exit
        } else {
            Warn "数据库存在但数据不完整，正在重新导入 ..."
            $null = & $mysqlExe "-uroot" "-e" "drop database if exists community_repair; create database community_repair default character set utf8mb4 collate utf8mb4_general_ci"
            if ($LASTEXITCODE -ne 0) { Err "重建数据库失败"; Pause-Exit }
            if (-not (Import-SqlFiles $mysqlExe)) { Pause-Exit }
            Ok "数据已重新导入"
        }
    } else {
        Step "首次运行，正在创建数据库并导入数据 ..."
        $null = & $mysqlExe "-uroot" "-e" "create database community_repair default character set utf8mb4 collate utf8mb4_general_ci"
        if ($LASTEXITCODE -ne 0) { Err "创建数据库失败，请查看上面的 MySQL 报错"; Pause-Exit }
        if (-not (Import-SqlFiles $mysqlExe)) { Pause-Exit }
        Ok "数据库初始化完成"
    }
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

# ---------------------------------------------------------------- 后端 jar
Step "检查后端程序 ..."
if ((Test-Path $jarPath) -and -not $forcePackage) {
    Ok "已找到 linlixiu-admin.jar"
} else {
    if ($forcePackage) { Step "数据库口令已更新，正在重新打包后端 ..." }
    else { Step "还没有打包，正在编译（首次需下载依赖，约 3~8 分钟）..." }
    $mvn = Find-Maven
    if (-not $mvn -and (Ask-Install "Maven（后端打包工具）" "9 MB")) {
        $mvn = Install-Maven
        if ($mvn) { Ok "Maven 已安装：$mvn" }
    }
    if (-not $mvn) {
        Err "没找到 Maven，无法自动打包"
        Hint "安装 Maven：https://maven.apache.org/download.cgi"
        Hint "或用 IDEA 打开项目，等依赖下载完后执行 mvn clean package -DskipTests"
        Pause-Exit
    }
    $env:JAVA_HOME = $javaHome
    Push-Location $root
    & $mvn "clean" "package" "-DskipTests" "-B" "-q"
    $mvnCode = $LASTEXITCODE
    Pop-Location
    if ($mvnCode -ne 0 -or -not (Test-Path $jarPath)) {
        Err "打包失败，请查看上面的编译错误"
        Pause-Exit
    }
    Ok "打包完成"
}

# ---------------------------------------------------------------- 前端依赖
Step "检查前端依赖 ..."
$nodeModules = Join-Path $uiDir "node_modules"
if (Test-Path $nodeModules) {
    Ok "已安装"
} else {
    Step "首次运行，正在安装前端依赖（约 3~5 分钟）..."
    Push-Location $uiDir
    & npm install --legacy-peer-deps --no-audit --no-fund
    $npmCode = $LASTEXITCODE
    Pop-Location
    if ($npmCode -ne 0 -or -not (Test-Path (Join-Path $nodeModules "vue"))) {
        Err "依赖安装失败，请查看上面的 npm 报错"
        Pause-Exit
    }
    Ok "依赖安装完成"
}

# ---------------------------------------------------------------- 启动
Write-Host ""
Step "启动后端（8081）..."
if (Test-Port 8081) {
    Warn "8081 已被占用，可能上次没关干净，跳过启动"
} else {
    $cmdLine = "title 后端-8081 && `"$java`" -jar `"$jarPath`" --server.port=8081"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k $cmdLine" -WorkingDirectory $root
    Start-Sleep -Seconds 2
}

Step "启动前端（88）..."
if (Test-Port 88) {
    Warn "88 已被占用，可能上次没关干净，跳过启动"
} else {
    $cmdLine = "title 前端-88 && set PATH=$nodeDir;%PATH% && cd /d `"$uiDir`" && npm run dev"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k $cmdLine" -WorkingDirectory $uiDir
}

# ---------------------------------------------------------------- 等待就绪
# 登录页一加载就会请求 /captchaImage，后端没预热完会弹「系统接口请求超时」。
# 这里先替页面把接口探活，都就绪后再打开浏览器，就不用手动刷新了。
Write-Host ""
Step "等待服务就绪（最多 120 秒）..."
$uiReady = $false
$apiReady = $false
for ($i = 0; $i -lt 60; $i++) {
    if (-not $uiReady) { $uiReady = Test-Http "http://localhost:88" }
    # 端口先监听再发请求，避免后端没起来时每轮都白等一个 HTTP 超时
    if (-not $apiReady -and (Test-Port 8081)) {
        $apiReady = Test-Http "http://localhost:8081/captchaImage" 3000
    }
    if ($uiReady -and $apiReady) { break }
    Start-Sleep -Seconds 2
    if ($i % 5 -eq 4) {
        $s1 = if ($uiReady) { "前端 OK" } else { "前端编译中" }
        $s2 = if ($apiReady) { "后端 OK" } else { "后端启动中" }
        Write-Host "      ... 已等待 $(($i + 1) * 2) 秒（$s1 / $s2）" -ForegroundColor DarkGray
    }
}
# 兜底：页面没返回 200，但 88 端口确实在监听，也认为前端已起来
if (-not $uiReady -and (Test-Port 88)) {
    $uiReady = $true
    Warn "页面响应较慢，但 88 端口已就绪"
}
$ready = $uiReady -and $apiReady

Write-Host ""
Line
if ($ready) {
    Ok "启动成功！"
} elseif ($uiReady) {
    Warn "前端已就绪，但后端接口暂时没有响应"
    Hint "请查看「后端-8081」窗口的日志，等它打印启动完成后刷新页面即可。"
} else {
    Warn "服务还没有就绪，可能仍在编译"
    Hint "请查看「前端-88」和「后端-8081」两个窗口的日志。"
}
try {
    Start-Process "http://localhost:88"
    Ok "已在默认浏览器中打开 http://localhost:88"
} catch {
    Warn "没能自动打开浏览器，请手动访问 http://localhost:88"
}
Line
Write-Host ""
Write-Host "  演示账号（密码统一 admin123）" -ForegroundColor Yellow
Write-Host "    admin        社区管理员   全部功能" -ForegroundColor Gray
Write-Host "    dispatcher   调度员       派单、催办、统计" -ForegroundColor Gray
Write-Host "    worker01     维修工       接单、进度、完工" -ForegroundColor Gray
Write-Host "    resident01   居民         报修、评价" -ForegroundColor Gray
Write-Host ""
Write-Host "  关闭服务：双击项目根目录的「一键停止.bat」" -ForegroundColor Gray
Write-Host "  它会关闭本目录的前端、后端和隐藏运行的 Redis；删除文件夹前请先停止。" -ForegroundColor Gray
Write-Host ""
Read-Host "  按回车键关闭本窗口（服务会继续运行）" | Out-Null
