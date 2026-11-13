# 邻里修

面向社区居民与物业服务中心的报修全流程管理系统。居民在线提交报修，调度员按技能派单，维修工接单上门并回填维修结果，居民对服务进行评价，管理员查看统计报表。

## 一、技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Java 8、Spring Boot 2.5.15、Spring Security + JWT、MyBatis、Druid、Quartz |
| 前端 | Vue 2.6、Element UI 2.15、Vuex、Vue Router、Axios、ECharts 5 |
| 数据库 | MySQL 8.0（utf8mb4）、Redis 6 |
| 构建 | Maven 多模块、vue-cli 5 |

## 二、角色与功能

系统共 4 类角色，覆盖 12 个核心功能点。

| 角色 | 账号 | 核心功能 |
|---|---|---|
| 居民 | resident01 / 02 / 03 | 提交报修、查看进度、取消报修、服务评价、站内消息 |
| 调度员 | dispatcher | 工单派单、改派、超时催办、报修统计 |
| 维修工 | worker01 / 02 / 03 | 接单、更新维修进度、完工回单、退回工单 |
| 社区管理员 | admin | 维修工档案、公告发布、统计报表、组织与权限管理 |

**功能清单**

1. 报修提交（类型、地址、紧急程度、问题描述、现场照片）
2. 我的报修查询与撤销
3. 工单派单与改派（按技能匹配维修工）
4. 维修工接单
5. 维修进度跟踪（时间线）
6. 完工回单（维修结果登记）
7. 服务评价（综合 + 响应速度 + 维修质量 + 服务态度四项评分）
8. 评价回复与管理
9. 维修工档案与绩效（接单量、完成量、平均分）
10. 站内消息通知（派单、接单、完工、评价、催办）
11. 报修统计（概览、类型分布、状态分布、近 N 天趋势、维修工排行）
12. 超时工单自动标记（Quartz 定时任务）

## 三、工单状态机

```
                 派单                接单               完工               评价
 pending(待派单) ──▶ assigned(待接单) ──▶ repairing(维修中) ──▶ completed(待评价) ──▶ rated(已完成)
      │                  │                     │
      │ 居民取消          │ 居民取消 / 维修工退回  │ 维修工退回（回到 pending）
      ▼                  ▼                     ▼
 cancelled(已取消)   cancelled(已取消)      pending(待派单)
```

- 状态取值与数据字典 `repair_status` 一一对应，前端标签颜色由字典配置驱动。
- 每次状态变更都会写入 `repair_progress`，形成可追溯的处理时间线。
- 超时判定：待派单/待接单超过 2 小时、维修中超过 24 小时，由定时任务置 `is_overtime = 1`，列表整行标红。

## 四、目录结构

```
Community_repair_reporting_management_system
├── linlixiu-admin           # Web 入口，含 repair 包下的 7 个业务 Controller 与 1 个定时任务
├── linlixiu-common          # 通用工具、注解、常量
├── linlixiu-framework       # 安全认证、数据源、AOP 日志等框架配置
├── linlixiu-system          # 业务与系统 Service / Mapper / Domain
│   ├── domain            # RepairOrder / RepairProgress / RepairEvaluation / RepairWorker / RepairMessage
│   └── mapper            # 对应 Mapper 接口与 XML
├── linlixiu-quartz          # 定时任务调度
├── linlixiu-generator       # 代码生成器
├── linlixiu-ui              # Vue 前端
│   └── src
│       ├── api/repair    # 业务接口封装
│       └── views/repair  # order / dispatch / task / evaluation / worker / message / stats
├── tools                    # 一键启动相关，正常使用无需改动
│   ├── launcher.ps1         # 环境检测、自动安装、启动编排
│   └── redis                # 内置 Redis 便携版（约 2.5MB，免安装）
├── 一键启动.bat             # 双击启动整个系统
└── sql
    ├── ry_20231130.sql       # 框架基础库（先执行）
    ├── quartz.sql            # Quartz 调度表（按需执行）
    └── repair_business.sql   # 业务表 + 菜单 + 字典 + 演示数据（后执行）
```

## 五、快速开始

### 一键启动（推荐，最省事）

点击即可运行，需要安装内容也有公示。

**双击项目根目录的 `一键启动.bat` 即可。**

脚本会自动完成下面这些事：

| 步骤 | 内容 |
|---|---|
| 1. 检测环境 | 逐个检查 JDK 8、MySQL、Node.js 是否就绪 |
| 2. 缺失自动安装 | 没装会提示，输入 `y` 自动下载安装到 `D:\linlixiu-runtime`（没有 D 盘则放项目内 `tools\runtime`） |
| 3. 启动 Redis | 使用项目内置的 `tools\redis`，**无需单独安装 Redis** |
| 4. 初始化数据库 | 首次运行自动创建 `community_repair` 库并导入三个 SQL；连不上会提示输入 MySQL 密码并写回配置 |
| 5. 编译打包 | 没有 `linlixiu-admin.jar` 时自动执行 `mvn package` |
| 6. 安装依赖 | 没有 `node_modules` 时自动 `npm install` |
| 7. 启动服务 | 后端 8081 + 前端 88，就绪后自动打开浏览器 |

**环境要求**：脚本可以自动下载安装 JDK 8 和 Node.js；**MySQL 需要本机已安装并启动**（涉及 root 密码与系统服务，脚本只做检测和提示，不会静默安装）。

**关闭服务**：关掉标题为「后端-8081」和「前端-88」的两个命令行窗口即可。

> 想了解每一步在做什么、或者要自己控制启动过程，继续看下面的 5.0 起的手动步骤。
> 手动步骤里的 Redis 路径按你本机实际安装位置填写；如果本机没装过 Redis，直接用项目内置的 `tools\redis\redis-server.exe` 即可。

### 5.0 先看懂启动顺序

整个系统由 **4 个进程**组成，必须按顺序启动。前一个没起来，后一个必然报错：

```
① Redis (6379)  →  ② MySQL (3306)  →  ③ 后端 (8081)  →  ④ 前端 (88)  →  浏览器访问 localhost:88
   缓存登录令牌       存业务数据          提供接口           提供页面
```

| 顺序 | 组件 | 端口 | 没启动会出现什么现象 |
|---|---|---|---|
| ① | Redis | 6379 | 后端启动直接退出，日志报 `RedisConnectionFailureException: Connection refused` |
| ② | MySQL | 3306 | 后端报 `Access denied for user` 或 `Unknown database 'community_repair'` |
| ③ | 后端 | 8081 | 前端页面能打开，但登录一直转圈 / 提示「系统接口请求异常」 |
| ④ | 前端 | 88 | 浏览器打不开 `localhost:88` |

> **日常开发只需要启动 ①③④**。MySQL 一般装成了 Windows 服务，开机自动运行，不用管它。
> **只有第一次运行**才需要做 5.3（建库导数据）和 5.4（改数据库口令）。

### 5.1 环境要求与检查

| 软件 | 版本 | 检查命令 | 期望输出 |
|---|---|---|---|
| JDK | **必须是 1.8** | `java -version` | `1.8.0_xxx` |
| MySQL | 8.0 | `mysql --version` | `mysql  Ver 8.0.x` |
| Redis | 5.0+ | `redis-cli ping` | `PONG` |
| Node.js | 16 ~ 22 | `node -v` | `v18.x` / `v20.x` / `v22.x` |
| Maven | 3.6+ | `mvn -v` | `Apache Maven 3.x`（没装也能用 IDEA 内置的） |

> 端口说明：后端 **8081**，前端 **88**。8080 常被 Steam 等程序占用，80 端口在 Windows 下需要管理员权限，因此均已避开。

**如果 `java -version` 显示的不是 1.8**（比如显示 17、21、24），必须临时切换，否则后端会报各种奇怪的错。在要执行命令的那个窗口里先跑下面两行（只对当前窗口有效），路径按你本机 JDK 8 的实际位置改：

```bat
set JAVA_HOME=D:\jdk\jdk1.8
set PATH=%JAVA_HOME%\bin;%PATH%
java -version
```

### 5.2 第一步：启动 Redis

**方式 A：安装版**

按 `Win + R` → 输入 `services.msc` → 找到 `Redis` → 右键「启动」。

**验证**：另开一个命令行窗口执行 `D:\redis\redis-cli.exe ping`，返回 `PONG` 即正常。

**方式 B：便携版（若已安装则找到即用）**

按 `Win + R`，输入 `cmd`，回车，打开命令行窗口，执行：

```bat
cd /d D:\redis
redis-server.exe redis.windows.conf
```

看到下面这行就是成功了，**这个黑窗口不要关**（关掉 Redis 就停了）：

```
 * Ready to accept connections
```

### 5.3 第二步：初始化数据库（只做一次）

#### ① 建库

打开命令行，登录 MySQL（会提示输入密码）：

```bat
mysql -uroot -p
```

输入密码后，在 `mysql>` 提示符下执行：

```sql
CREATE DATABASE `community_repair` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

#### ② 导入数据（顺序不能乱）

退出 MySQL（输入 `exit`），回到命令行，找到sql文件夹的路径。**注：命令中一定要带 `--default-character-set=utf8mb4`**，否则中文会变乱码：

```bat
cd /d D:\project\soft_class\Community_repair_reporting_management_system\sql

mysql --default-character-set=utf8mb4 -uroot -p community_repair < ry_20231130.sql
mysql --default-character-set=utf8mb4 -uroot -p community_repair < repair_business.sql
mysql --default-character-set=utf8mb4 -uroot -p community_repair < quartz.sql
```

三个文件的作用：

| 文件 | 内容 | 是否必须 |
|---|---|---|
| `ry_20231130.sql` | 框架表（用户、部门、角色、菜单、字典、参数） | **必须，第一个导** |
| `repair_business.sql` | 业务表 + 菜单 + 字典 + 8 条演示工单 | **必须，第二个导** |
| `quartz.sql` | 定时任务调度表 | 要用超时扫描功能才导 |

> **用 Navicat 的话**：新建库 `community_repair`（字符集选 `utf8mb4`）→ 右键「运行 SQL 文件」→ 按上面顺序导三个文件 → 弹窗里**编码选 `65001 (UTF-8)`**，别用默认值。

#### ③ 验证导入是否完整

这一步很重要。之前踩过坑：导入中途报错会静默中断，表建了但数据是空的。

```sql
use community_repair;

select count(*) from sys_user;      -- 期望 8（管理员 + 调度员 + 3 维修工 + 3 居民）
select count(*) from sys_menu;      -- 期望 100 以上
select count(*) from sys_dict_data; -- 期望 50 左右
select count(*) from repair_order;  -- 期望 8（演示工单）
```

只要有一个数字是 0，说明导入中断了，往上翻命令行输出找 `ERROR` 行。

### 5.4 第三步：确认数据库口令（只做一次）

打开配置文件，路径是 `linlixiu-admin/src/main/resources/application-druid.yml`，找到第 8～11 行的 `master` 数据源：

```yaml
master:
    url: jdbc:mysql://localhost:3306/community_repair?useUnicode=true&characterEncoding=utf8&...
    username: root
    password: 你的MySQL密码      # ← 改成你自己的MySQL密码
```

> 本项目当前已填好可用的本机口令。**换电脑或重装 MySQL 后才需要改这里。**

Redis 配置在 `linlixiu-admin/src/main/resources/application.yml` 第 69 行，默认 `localhost:6379`、无密码，一般不用动。

### 5.5 第四步：启动后端

#### 方式 A：IDEA 运行（推荐，可断点调试）

1. 打开 IDEA → `File` → `Open` → 选择项目根目录 `Community_repair_reporting_management_system`
2. 右下角弹出 Maven 提示时点 **Load**，或者在右边界面点击m的图标点击更新，等待右下角进度条走完（首次下载依赖约 3~10 分钟）
3. 左上角四条横杠的图标点击后选择`File(文件)` → `Project Structure(项目结构)`（快捷键 `Ctrl+Alt+Shift+S`）→ 左侧栏目选择项目设置的`Project(项目)` 页签：**SDK 选 1.8**，**Language level(语言级别) 选 8**（若无SDK1.8则选中下拉选项中的添加SDK，下载JDK，厂商任意）
4. 左上角四条横杠的图标点击后选择`File(文件)` →`Settings(设置)` → `Build, Execution, Deployment(构建、执行、部署)` → `Build Tools(构建工具)` → `Maven` → **`Runner(运行程序)`** 页签：**JRE 选 1.8**
5. 直接点击上方启动或打开 `linlixiu-admin/src/main/java/com/linlixiu/LinlixiuApplication.java`，点左侧绿色三角 → `Run`
6. 控制台出现下面这段就是启动成功：

```
Started LinlixiuApplication in ... seconds

★ 邻里修 · 社区报修管理系统  启动成功 ★
```

#### 方式 B：命令行运行（jar 方式）

依次执行下面四行。**前两行是临时切到 JDK 8**，如果你的 `java -version` 本来就是 1.8 可以跳过：

```bat
set JAVA_HOME=D:\jdk\jdk1.8
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d D:\project\soft_class\Community_repair_reporting_management_system
mvn clean package -DskipTests
```

打包成功后启动（首次打包约 1~2 分钟，之后可以只执行这一行）：

```bat
java -jar linlixiu-admin\target\linlixiu-admin.jar
```

> 项目根目录还提供了 `bin\run.bat`，双击等价于上面的「启动 jar」，但要求已经 `package` 过。

> 如果提示 `'mvn' 不是内部或外部命令`，说明命令行里没有 Maven。两种办法：① 改用上面的方式 A 在 IDEA 里运行；② 用 IDEA 自带的 Maven，把 `mvn` 换成完整路径，例如
> `"D:\idea\IntelliJ IDEA 2025.3.1.1\plugins\maven\lib\maven3\bin\mvn" clean package -DskipTests`

**验证后端是否起来了**：浏览器访问 `http://localhost:8081/captchaImage`，能看到一段 `{"msg":"操作成功","code":200,"uuid":"..."}` 的 JSON 就说明接口正常。

### 5.6 第五步：启动前端

**新开一个命令行窗口**（后端那个窗口不能关），确定`linlixiu-ui`文件路径后，执行：

```bat
cd /d D:\project\soft_class\Community_repair_reporting_management_system\linlixiu-ui
npm install
npm run dev
```

其中 `npm install` 只有第一次需要（约 2~5 分钟），之后直接 `npm run dev` 即可，稍等片刻后看到下面这行就成功了：

```
App running at:
- Local:   http://localhost:88/
```

浏览器一般会自动打开，没打开就手动访问 `http://localhost:88`。

打开后显示系统接口请求超时，并且没有验证码出现可以尝试刷新页面。

> 若报 `ERR_OSSL_EVP_UNSUPPORTED`，先在当前窗口执行 `set NODE_OPTIONS=--openssl-legacy-provider`，再 `npm run dev`。

### 5.7 第六步：登录体验

所有演示账号口令统一为 **admin123**。

| 账号 | 角色 | 登录后能看到 |
|---|---|---|
| `admin` | 社区管理员 | 全部菜单：报修、派单、维修、评价、消息、统计、维修工管理、系统管理 |
| `dispatcher` | 调度员 | 工单派单、报修统计、消息中心 |
| `worker01` / `worker02` / `worker03` | 维修工 | 维修任务、消息中心 |
| `resident01` / `resident02` / `resident03` | 居民 | 我要报修、评价反馈、消息中心 |

**建议按这个顺序演示一遍完整流程**（答辩/录屏可用）：

1. `resident01` 登录 → 我要报修 → 填写并提交
2. `dispatcher` 登录 → 工单派单 → 找到刚提交的工单 → 派给 `worker01`
3. `worker01` 登录 → 维修任务 → 接单 → 更新进度 → 完工回单
4. `resident01` 登录 → 评价反馈 → 打分提交
5. `admin` 登录 → 报修统计 → 查看图表与排行

### 5.8 定时任务说明

系统启动后，在「系统监控 → 定时任务」中可看到「报修工单超时扫描」（调用目标 `repairTask.scanOvertime()`，每 30 分钟执行一次），用于把超时工单标记为 `is_overtime = 1`，列表中整行标红。

> 前提是已经导入 `sql/quartz.sql`，否则该页面为空。

### 5.9 常见报错对照表

| 报错信息 | 原因 | 解决 |
|---|---|---|
| `RedisConnectionFailureException: Connection refused` | Redis 没启动 | 回到 5.2，确认 `redis-cli ping` 返回 `PONG` |
| `Access denied for user 'root'@'localhost'` | 数据库口令不对 | 改 `application-druid.yml` 的 `password` |
| `Unknown database 'community_repair'` | 库没建或名字拼错 | 回到 5.3 建库 |
| 登录提示「用户不存在/密码错误」，日志里 `selectUserByUserName ... Total: 0` | **数据没导进去**，表是空的 | 回到 5.3 第 ③ 步检查 count，为 0 就重导 |
| 报修类型等中文显示成 `姘寸數缁翠慨` | 导入时没指定 utf8mb4，或 Redis 缓存了旧乱码 | 清字典缓存（见下方说明），再 `Ctrl+F5` 强制刷新 |
| `Port 8081 was already in use` | 上次的进程没关干净 | 任务管理器结束 `java.exe`，或 `netstat -ano \| findstr :8081` 查 PID 后 `taskkill /PID xxx /F` |
| `Port 88 was already in use` | 前端重复启动 | 关掉多余的命令行窗口 |
| `Unable to access jarfile linlixiu-admin.jar` | 还没打包 | 先执行 `mvn clean package -DskipTests` |
| 页面能打开但所有接口 404 / 500 | 后端没起来或端口不对 | 确认 `localhost:8081` 能访问，检查 `vue.config.js` 里代理指向 `8081` |
| 菜单是空的，只有「系统管理」 | `repair_business.sql` 没导全 | 重导该文件 |
| `npm install` 卡住不动 | 网络问题 | 执行 `npm config set registry https://registry.npmmirror.com` 后重试 |

**清字典缓存的具体做法**（中文乱码时用）：

```bat
D:\redis\redis-cli.exe keys "sys_dict:*"
```

会列出若干 `sys_dict:repair_type` 这样的键名，逐个删除：

```bat
D:\redis\redis-cli.exe del sys_dict:repair_type
D:\redis\redis-cli.exe del sys_dict:repair_status
D:\redis\redis-cli.exe del sys_dict:repair_urgency
```

删完后前端按 `Ctrl + F5` 强制刷新（普通刷新可能用到浏览器缓存），系统会自动从数据库重新加载正确的中文。

> 排查乱码的顺序：**先看数据库里的值对不对 → 再看 Redis 缓存 → 最后才怀疑前端字体**。

### 5.10 日常开发的最短启动路径

第一次配置好之后，以后每次开机只需要开 **3 个命令提示符cmd窗口**：

**窗口 1 —— Redis**（启动后不要关）

```bat
cd /d D:\redis
redis-server.exe redis.windows.conf
```

**窗口 2 —— 后端**（IDEA 里点运行，或命令行执行）

```bat
java -jar D:\project\soft_class\Community_repair_reporting_management_system\linlixiu-admin\target\linlixiu-admin.jar
```

> 若 `java -version` 不是 1.8，先执行 5.1 里那两行 `set` 命令再启动。

**窗口 3 —— 前端**

```bat
cd /d D:\project\soft_class\Community_repair_reporting_management_system\linlixiu-ui
npm run dev
```

然后浏览器打开 `http://localhost:88`，用 `admin` / `admin123` 登录。

> 停止服务：在对应窗口按 `Ctrl + C`，或直接关闭窗口。

## 六、数据表说明

| 表名 | 说明 |
|---|---|
| `repair_order` | 报修工单主表，记录报修信息、状态、维修工、各环节时间与超时标记 |
| `repair_progress` | 报修进度表，按节点记录处理时间线 |
| `repair_evaluation` | 服务评价表，一个工单一条评价，含四项评分与回复 |
| `repair_worker` | 维修工档案表，含技能、在岗状态、接单量与平均分 |
| `repair_message` | 站内消息表，派单/接单/完工/评价/催办通知 |

