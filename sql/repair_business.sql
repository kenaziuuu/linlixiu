-- =============================================================
-- 邻里修 · 社区报修管理系统 —— 业务库初始化脚本
-- 说明：本脚本在 ry_20231130.sql（Linlixiu 框架基础库）之后执行
-- 执行顺序：1) ry_20231130.sql  2) repair_business.sql
-- 默认口令：admin123（所有演示账号一致）
-- =============================================================

-- ------------------------------------------------------------
-- 1、报修工单表
-- ------------------------------------------------------------
drop table if exists repair_order;
create table repair_order (
  order_id            bigint(20)    not null auto_increment    comment '工单ID',
  order_no            varchar(32)   not null                   comment '报修单号',
  resident_id         bigint(20)    default null               comment '报修人ID',
  dept_id             bigint(20)    default null               comment '所属小区ID',
  repair_type         varchar(20)   not null                   comment '报修类型（water/electric/door/appliance/public/other）',
  title               varchar(100)  not null                   comment '报修标题',
  description         varchar(500)  default null               comment '问题描述',
  address             varchar(200)  default null               comment '报修地址',
  contact_name        varchar(20)   default null               comment '联系人',
  contact_phone       varchar(20)   default null               comment '联系电话',
  urgency             varchar(10)   default 'normal'           comment '紧急程度（normal/urgent/emergency）',
  images              varchar(1000) default null               comment '现场图片（逗号分隔）',
  status              varchar(20)   default 'pending'          comment '工单状态（pending/assigned/repairing/completed/rated/cancelled）',
  worker_id           bigint(20)    default null               comment '维修工用户ID',
  dispatcher_id       bigint(20)    default null               comment '派单人ID',
  dispatch_time       datetime      default null               comment '派单时间',
  accept_time         datetime      default null               comment '接单时间',
  finish_time         datetime      default null               comment '完工时间',
  finish_result       varchar(500)  default null               comment '维修结果',
  cancel_reason       varchar(200)  default null               comment '取消原因',
  is_overtime         tinyint(1)    default 0                  comment '是否超时（0否 1是）',
  evaluate_flag       tinyint(1)    default 0                  comment '是否已评价（0否 1是）',
  create_by           varchar(64)   default ''                 comment '创建者',
  create_time         datetime      default current_timestamp  comment '创建时间',
  update_by           varchar(64)   default ''                 comment '更新者',
  update_time         datetime      default current_timestamp on update current_timestamp comment '更新时间',
  remark              varchar(255)  default null               comment '备注',
  primary key (order_id),
  unique key uk_order_no (order_no),
  key idx_resident (resident_id),
  key idx_worker (worker_id),
  key idx_status (status),
  key idx_create_time (create_time)
) engine=innodb auto_increment=1 comment='报修工单表';

-- ------------------------------------------------------------
-- 2、报修进度表（流程跟踪时间线）
-- ------------------------------------------------------------
drop table if exists repair_progress;
create table repair_progress (
  progress_id         bigint(20)    not null auto_increment    comment '进度ID',
  order_id            bigint(20)    not null                   comment '工单ID',
  node                varchar(20)   not null                   comment '节点类型（submit/dispatch/accept/repair/finish/rate/cancel/return）',
  node_name           varchar(30)   default null               comment '节点名称',
  content             varchar(500)  default null               comment '进度说明',
  operator_id         bigint(20)    default null               comment '操作人ID',
  images              varchar(1000) default null               comment '现场图片',
  create_time         datetime      default current_timestamp  comment '发生时间',
  primary key (progress_id),
  key idx_order (order_id)
) engine=innodb auto_increment=1 comment='报修进度表';

-- ------------------------------------------------------------
-- 3、报修评价表
-- ------------------------------------------------------------
drop table if exists repair_evaluation;
create table repair_evaluation (
  evaluation_id       bigint(20)    not null auto_increment    comment '评价ID',
  order_id            bigint(20)    not null                   comment '工单ID',
  resident_id         bigint(20)    default null               comment '评价人ID',
  worker_id           bigint(20)    default null               comment '维修工ID',
  score               int(1)        not null                   comment '综合评分（1-5）',
  speed_score         int(1)        default null               comment '响应速度（1-5）',
  quality_score       int(1)        default null               comment '维修质量（1-5）',
  attitude_score      int(1)        default null               comment '服务态度（1-5）',
  content             varchar(300)  default null               comment '评价内容',
  reply_content       varchar(300)  default null               comment '回复内容',
  reply_time          datetime      default null               comment '回复时间',
  create_time         datetime      default current_timestamp  comment '评价时间',
  primary key (evaluation_id),
  unique key uk_order (order_id)
) engine=innodb auto_increment=1 comment='报修评价表';

-- ------------------------------------------------------------
-- 4、维修工档案表
-- ------------------------------------------------------------
drop table if exists repair_worker;
create table repair_worker (
  worker_id           bigint(20)    not null auto_increment    comment '维修工ID',
  user_id             bigint(20)    default null               comment '关联用户ID',
  worker_no           varchar(20)   default null               comment '工号',
  worker_name         varchar(30)   default null               comment '姓名',
  phone               varchar(20)   default null               comment '手机号',
  skills              varchar(100)  default null               comment '技能类型（逗号分隔，取值同报修类型）',
  status              varchar(2)    default '0'                comment '在岗状态（0在岗 1休息 2离职）',
  total_count         int(11)       default 0                  comment '累计接单数',
  finish_count        int(11)       default 0                  comment '完成数',
  avg_score           decimal(3,1)  default 0.0                comment '平均评分',
  remark              varchar(255)  default null               comment '备注',
  primary key (worker_id),
  unique key uk_user (user_id)
) engine=innodb auto_increment=1 comment='维修工档案表';

-- ------------------------------------------------------------
-- 5、站内消息表
-- ------------------------------------------------------------
drop table if exists repair_message;
create table repair_message (
  message_id          bigint(20)    not null auto_increment    comment '消息ID',
  receiver_id         bigint(20)    not null                   comment '接收人ID',
  title               varchar(100)  default null               comment '消息标题',
  content             varchar(500)  default null               comment '消息内容',
  msg_type            varchar(20)   default 'system'           comment '消息类型（dispatch/accept/finish/rate/urge/system）',
  order_id            bigint(20)    default null               comment '关联工单ID',
  is_read             tinyint(1)    default 0                  comment '是否已读（0未读 1已读）',
  create_time         datetime      default current_timestamp  comment '发送时间',
  primary key (message_id),
  key idx_receiver (receiver_id)
) engine=innodb auto_increment=1 comment='站内消息表';


-- =============================================================
-- 6、组织与账号（把框架示例数据替换为社区结构）
-- =============================================================
delete from sys_user_role;
delete from sys_role_menu;
delete from sys_role_dept;
delete from sys_user_post;
delete from sys_user;
delete from sys_role;
delete from sys_dept;

-- 社区组织：服务中心 → 三个职能部门 + 三个小区
insert into sys_dept values(100, 0,   '0',           '阳光社区服务中心', 0, '王主任', '05718888801', 'service@sunny.gov.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101, 100, '0,100',       '维修服务部',       1, '李主管', '05718888802', 'repair@sunny.gov.cn',  '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102, 100, '0,100',       '客户服务部',       2, '张主管', '05718888803', 'service2@sunny.gov.cn','0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(103, 100, '0,100',       '综合管理部',       3, '赵主管', '05718888804', 'admin@sunny.gov.cn',   '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(201, 100, '0,100',       '阳光花园小区',     4, null,     null,           null,                   '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(202, 100, '0,100',       '翠竹苑小区',       5, null,     null,           null,                   '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(203, 100, '0,100',       '梧桐里小区',       6, null,     null,           null,                   '0', '0', 'admin', sysdate(), '', null);

-- 账号：密码统一为 admin123
insert into sys_user values(1, 103, 'admin',      '社区管理员', '00', 'admin@sunny.gov.cn',   '13800000000', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '社区管理员');
insert into sys_user values(2, 102, 'dispatcher', '张调度',     '00', 'dispatch@sunny.gov.cn','13800000010', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '调度员');
insert into sys_user values(3, 101, 'worker01',   '李师傅',     '00', 'li@sunny.gov.cn',      '13800000001', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '水电维修工');
insert into sys_user values(4, 101, 'worker02',   '王师傅',     '00', 'wang@sunny.gov.cn',    '13800000002', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '门窗家电维修工');
insert into sys_user values(5, 101, 'worker03',   '赵师傅',     '00', 'zhao@sunny.gov.cn',    '13800000003', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '公共设施维修工');
insert into sys_user values(6, 201, 'resident01', '陈阿姨',     '00', 'chen@example.com',     '13900000001', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '阳光花园业主');
insert into sys_user values(7, 202, 'resident02', '刘先生',     '00', 'liu@example.com',      '13900000002', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '翠竹苑业主');
insert into sys_user values(8, 203, 'resident03', '周女士',     '00', 'zhou@example.com',     '13900000003', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '梧桐里业主');

-- 角色
insert into sys_role values('1', '超级管理员', 'admin',      1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超级管理员');
insert into sys_role values('2', '普通角色',   'common',     2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');
insert into sys_role values('3', '调度员',     'dispatcher', 3, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '负责工单派单与催办');
insert into sys_role values('4', '维修工',     'worker',     4, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '负责接单与维修');
insert into sys_role values('5', '居民',       'resident',   5, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '社区业主');

-- 用户角色
insert into sys_user_role values('2', '3');
insert into sys_user_role values('3', '4');
insert into sys_user_role values('4', '4');
insert into sys_user_role values('5', '4');
insert into sys_user_role values('6', '5');
insert into sys_user_role values('7', '5');
insert into sys_user_role values('8', '5');


-- =============================================================
-- 7、数据字典
-- =============================================================
delete from sys_dict_data where dict_type in ('repair_type','repair_status','repair_urgency','repair_msg_type');
delete from sys_dict_type where dict_type in ('repair_type','repair_status','repair_urgency','repair_msg_type');

insert into sys_dict_type values(100, '报修类型', 'repair_type',    '0', 'admin', sysdate(), '', null, '社区报修的故障类型');
insert into sys_dict_type values(101, '工单状态', 'repair_status',  '0', 'admin', sysdate(), '', null, '报修工单状态机');
insert into sys_dict_type values(102, '紧急程度', 'repair_urgency', '0', 'admin', sysdate(), '', null, '报修紧急程度');
insert into sys_dict_type values(103, '消息类型', 'repair_msg_type','0', 'admin', sysdate(), '', null, '站内消息类型');

insert into sys_dict_data values(100, 1, '水电维修', 'water',      'repair_type',    '', 'primary', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(101, 2, '电路维修', 'electric',   'repair_type',    '', 'warning', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(102, 3, '门窗维修', 'door',       'repair_type',    '', 'info',    'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(103, 4, '家电维修', 'appliance',  'repair_type',    '', 'success', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(104, 5, '公共设施', 'public',     'repair_type',    '', 'danger',  'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(105, 6, '其他',     'other',      'repair_type',    '', 'info',    'N', '0', 'admin', sysdate(), '', null, null);

insert into sys_dict_data values(110, 1, '待派单', 'pending',   'repair_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(111, 2, '待接单', 'assigned',  'repair_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(112, 3, '维修中', 'repairing', 'repair_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(113, 4, '待评价', 'completed', 'repair_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(114, 5, '已完成', 'rated',     'repair_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(115, 6, '已取消', 'cancelled', 'repair_status', '', 'info',    'N', '0', 'admin', sysdate(), '', null, null);

insert into sys_dict_data values(120, 1, '普通', 'normal',    'repair_urgency', '', 'info',    'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(121, 2, '紧急', 'urgent',    'repair_urgency', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(122, 3, '特急', 'emergency', 'repair_urgency', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, null);

insert into sys_dict_data values(130, 1, '派单通知', 'dispatch', 'repair_msg_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(131, 2, '接单通知', 'accept',   'repair_msg_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(132, 3, '完工通知', 'finish',   'repair_msg_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(133, 4, '评价通知', 'rate',     'repair_msg_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(134, 5, '催办通知', 'urge',     'repair_msg_type', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(135, 6, '系统消息', 'system',   'repair_msg_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, null);


-- =============================================================
-- 8、菜单与权限
-- =============================================================
delete from sys_menu where menu_id between 2000 and 2099;

-- 一级目录 + 二级菜单（两级结构，前端路由为 /repair/xxx）
insert into sys_menu values('2000', '社区报修', '0', '0', 'repair',     null, '', 1, 0, 'M', '0', '0', '', 'tool',      'admin', sysdate(), '', null, '社区报修目录');
insert into sys_menu values('2002', '我要报修', '2000', '1', 'order',      'repair/order/index',      '', 1, 0, 'C', '0', '0', 'repair:order:list',      'form',      'admin', sysdate(), '', null, '居民报修入口');
insert into sys_menu values('2003', '工单派单', '2000', '2', 'dispatch',   'repair/dispatch/index',   '', 1, 0, 'C', '0', '0', 'repair:dispatch:list',   'component', 'admin', sysdate(), '', null, '调度员派单');
insert into sys_menu values('2004', '维修任务', '2000', '3', 'task',       'repair/task/index',       '', 1, 0, 'C', '0', '0', 'repair:task:list',       'build',     'admin', sysdate(), '', null, '维修工任务');
insert into sys_menu values('2005', '评价反馈', '2000', '4', 'evaluation', 'repair/evaluation/index', '', 1, 0, 'C', '0', '0', 'repair:evaluation:list', 'star',      'admin', sysdate(), '', null, '服务评价');
insert into sys_menu values('2006', '消息中心', '2000', '5', 'message',    'repair/message/index',    '', 1, 0, 'C', '0', '0', 'repair:message:list',    'message',   'admin', sysdate(), '', null, '站内消息');
insert into sys_menu values('2008', '报修统计', '2000', '6', 'stats',      'repair/stats/index',      '', 1, 0, 'C', '0', '0', 'repair:stats:view',      'chart',     'admin', sysdate(), '', null, '报修统计报表');
insert into sys_menu values('2010', '维修工管理', '2000', '7', 'worker',   'repair/worker/index',     '', 1, 0, 'C', '0', '0', 'repair:worker:list',     'peoples',   'admin', sysdate(), '', null, '维修工档案');

-- 按钮权限
insert into sys_menu values('2011', '报修查询', '2002', '1', '', '', '', 1, 0, 'F', '0', '0', 'repair:order:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '报修新增', '2002', '2', '', '', '', 1, 0, 'F', '0', '0', 'repair:order:add',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2013', '报修修改', '2002', '3', '', '', '', 1, 0, 'F', '0', '0', 'repair:order:edit',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2014', '报修删除', '2002', '4', '', '', '', 1, 0, 'F', '0', '0', 'repair:order:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2015', '报修导出', '2002', '5', '', '', '', 1, 0, 'F', '0', '0', 'repair:order:export',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2016', '工单派单', '2003', '1', '', '', '', 1, 0, 'F', '0', '0', 'repair:dispatch:assign',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2017', '工单催办', '2003', '2', '', '', '', 1, 0, 'F', '0', '0', 'repair:dispatch:urge',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2018', '维修接单', '2004', '1', '', '', '', 1, 0, 'F', '0', '0', 'repair:task:accept',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2019', '维修完工', '2004', '2', '', '', '', 1, 0, 'F', '0', '0', 'repair:task:finish',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2020', '工单退回', '2004', '3', '', '', '', 1, 0, 'F', '0', '0', 'repair:task:return',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2021', '更新进度', '2004', '4', '', '', '', 1, 0, 'F', '0', '0', 'repair:task:progress',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2022', '评价查询', '2005', '1', '', '', '', 1, 0, 'F', '0', '0', 'repair:evaluation:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2023', '提交评价', '2005', '2', '', '', '', 1, 0, 'F', '0', '0', 'repair:evaluation:add',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2024', '回复评价', '2005', '3', '', '', '', 1, 0, 'F', '0', '0', 'repair:evaluation:reply', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2025', '删除评价', '2005', '4', '', '', '', 1, 0, 'F', '0', '0', 'repair:evaluation:remove','#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2026', '维修工查询', '2010', '1', '', '', '', 1, 0, 'F', '0', '0', 'repair:worker:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2027', '维修工新增', '2010', '2', '', '', '', 1, 0, 'F', '0', '0', 'repair:worker:add',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2028', '维修工修改', '2010', '3', '', '', '', 1, 0, 'F', '0', '0', 'repair:worker:edit',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2029', '维修工删除', '2010', '4', '', '', '', 1, 0, 'F', '0', '0', 'repair:worker:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2030', '消息删除', '2006', '1', '', '', '', 1, 0, 'F', '0', '0', 'repair:message:remove',  '#', 'admin', sysdate(), '', null, '');

-- 角色菜单：调度员
insert into sys_role_menu values('3', '2000');
insert into sys_role_menu values('3', '2003');
insert into sys_role_menu values('3', '2006');
insert into sys_role_menu values('3', '2008');
insert into sys_role_menu values('3', '2016');
insert into sys_role_menu values('3', '2017');
insert into sys_role_menu values('3', '2030');

-- 角色菜单：维修工
insert into sys_role_menu values('4', '2000');
insert into sys_role_menu values('4', '2004');
insert into sys_role_menu values('4', '2006');
insert into sys_role_menu values('4', '2018');
insert into sys_role_menu values('4', '2019');
insert into sys_role_menu values('4', '2020');
insert into sys_role_menu values('4', '2021');
insert into sys_role_menu values('4', '2030');

-- 角色菜单：居民
insert into sys_role_menu values('5', '2000');
insert into sys_role_menu values('5', '2002');
insert into sys_role_menu values('5', '2005');
insert into sys_role_menu values('5', '2006');
insert into sys_role_menu values('5', '2011');
insert into sys_role_menu values('5', '2012');
insert into sys_role_menu values('5', '2013');
insert into sys_role_menu values('5', '2014');
insert into sys_role_menu values('5', '2015');
insert into sys_role_menu values('5', '2022');
insert into sys_role_menu values('5', '2023');
insert into sys_role_menu values('5', '2030');


-- =============================================================
-- 9、演示数据
-- =============================================================
insert into repair_worker values(1, 3, 'W0001', '李师傅', '13800000001', 'water,electric',  '0', 3, 2, 4.5, '水电类维修');
insert into repair_worker values(2, 4, 'W0002', '王师傅', '13800000002', 'door,appliance',  '0', 2, 1, 5.0, '门窗与家电维修');
insert into repair_worker values(3, 5, 'W0003', '赵师傅', '13800000003', 'public,other',    '0', 1, 0, 0.0, '公共设施维护');

insert into repair_order values(1, 'BX202605010001', 6, 201, 'water',     '厨房水龙头持续漏水', '水龙头关闭后仍滴水，地面已积水', '阳光花园 3 栋 2 单元 501', '陈阿姨', '13900000001', 'normal',    null, 'rated',     3, 2, date_sub(sysdate(), interval 9 day), date_sub(sysdate(), interval 9 day), date_sub(sysdate(), interval 8 day), '更换水龙头阀芯并做密封处理，试水无渗漏', null, 0, 1, 'resident01', date_sub(sysdate(), interval 9 day), '', null, null);
insert into repair_order values(2, 'BX202605020002', 7, 202, 'door',      '阳台推拉门卡滞',     '推拉门滑轨变形，开关困难', '翠竹苑 5 栋 1 单元 302', '刘先生', '13900000002', 'normal',    null, 'completed', 4, 2, date_sub(sysdate(), interval 4 day), date_sub(sysdate(), interval 4 day), date_sub(sysdate(), interval 1 day), '更换滑轨并调整门扇，推拉顺畅', null, 0, 0, 'resident02', date_sub(sysdate(), interval 4 day), '', null, null);
insert into repair_order values(3, 'BX202605030003', 8, 203, 'public',    '单元楼道照明灯不亮', '3 单元 1-3 层楼道灯全部不亮，夜间出行不便', '梧桐里 2 栋 3 单元', '周女士', '13900000003', 'urgent',    null, 'repairing', 5, 2, date_sub(sysdate(), interval 2 day), date_sub(sysdate(), interval 2 day), null, null, null, 0, 0, 'resident03', date_sub(sysdate(), interval 2 day), '', null, null);
insert into repair_order values(4, 'BX202605040004', 6, 201, 'appliance', '客厅空调不制冷',     '开机后出风但不制冷，外机有异响', '阳光花园 3 栋 2 单元 501', '陈阿姨', '13900000001', 'urgent',    null, 'assigned',  4, 2, date_sub(sysdate(), interval 1 day), null, null, null, null, 0, 0, 'resident01', date_sub(sysdate(), interval 1 day), '', null, null);
insert into repair_order values(5, 'BX202605050005', 7, 202, 'electric',  '客厅插座频繁跳闸',   '使用电热水壶时总闸跳闸，怀疑线路过载', '翠竹苑 5 栋 1 单元 302', '刘先生', '13900000002', 'emergency', null, 'pending',   null, null, null, null, null, null, null, 0, 0, 'resident02', date_sub(sysdate(), interval 3 hour), '', null, null);
insert into repair_order values(6, 'BX202605050006', 8, 203, 'other',     '楼道堆放杂物影响通行', '2 层楼道堆放旧家具，消防通道被占', '梧桐里 2 栋 3 单元 2 层', '周女士', '13900000003', 'normal',    null, 'pending',   null, null, null, null, null, null, null, 0, 0, 'resident03', date_sub(sysdate(), interval 5 hour), '', null, null);
insert into repair_order values(7, 'BX202605060007', 6, 201, 'water',     '卫生间下水道堵塞',   '洗手池排水缓慢，有返味', '阳光花园 3 栋 2 单元 501', '陈阿姨', '13900000001', 'normal',    null, 'cancelled', null, null, null, null, null, null, '自行联系疏通后已解决，撤销报修', 0, 0, 'resident01', date_sub(sysdate(), interval 2 day), '', null, null);
insert into repair_order values(8, 'BX202605060008', 7, 202, 'door',      '卧室窗户玻璃破损',   '窗户玻璃出现裂纹，存在安全隐患', '翠竹苑 5 栋 1 单元 302', '刘先生', '13900000002', 'urgent',    null, 'rated',     3, 2, date_sub(sysdate(), interval 12 day), date_sub(sysdate(), interval 12 day), date_sub(sysdate(), interval 11 day), '更换钢化玻璃并加固窗框', null, 0, 1, 'resident02', date_sub(sysdate(), interval 12 day), '', null, null);

-- 进度时间线
insert into repair_progress values(1, 1, 'submit',   '提交报修', '居民提交报修申请：厨房水龙头持续漏水', 6, null, date_sub(sysdate(), interval 9 day));
insert into repair_progress values(2, 1, 'dispatch', '派单',     '调度员将工单指派给维修工【李师傅】',   2, null, date_sub(sysdate(), interval 9 day));
insert into repair_progress values(3, 1, 'accept',   '维修工接单', '维修工已接单，开始上门维修',          3, null, date_sub(sysdate(), interval 9 day));
insert into repair_progress values(4, 1, 'finish',   '维修完工', '维修结果：更换水龙头阀芯并做密封处理，试水无渗漏', 3, null, date_sub(sysdate(), interval 8 day));
insert into repair_progress values(5, 1, 'rate',     '居民评价', '居民给出 5 星评价：师傅很专业，修得很快', 6, null, date_sub(sysdate(), interval 8 day));

insert into repair_progress values(6, 2, 'submit',   '提交报修', '居民提交报修申请：阳台推拉门卡滞',     7, null, date_sub(sysdate(), interval 4 day));
insert into repair_progress values(7, 2, 'dispatch', '派单',     '调度员将工单指派给维修工【王师傅】',   2, null, date_sub(sysdate(), interval 4 day));
insert into repair_progress values(8, 2, 'accept',   '维修工接单', '维修工已接单，开始上门维修',          4, null, date_sub(sysdate(), interval 4 day));
insert into repair_progress values(9, 2, 'finish',   '维修完工', '维修结果：更换滑轨并调整门扇，推拉顺畅', 4, null, date_sub(sysdate(), interval 1 day));

insert into repair_progress values(10, 3, 'submit',   '提交报修', '居民提交报修申请：单元楼道照明灯不亮', 8, null, date_sub(sysdate(), interval 2 day));
insert into repair_progress values(11, 3, 'dispatch', '派单',     '调度员将工单指派给维修工【赵师傅】',   2, null, date_sub(sysdate(), interval 2 day));
insert into repair_progress values(12, 3, 'accept',   '维修工接单', '维修工已接单，开始上门维修',          5, null, date_sub(sysdate(), interval 2 day));
insert into repair_progress values(13, 3, 'repair',   '维修进度', '已排查线路，需更换 3 只声控灯座',      5, null, date_sub(sysdate(), interval 1 day));

insert into repair_progress values(14, 4, 'submit',   '提交报修', '居民提交报修申请：客厅空调不制冷',     6, null, date_sub(sysdate(), interval 1 day));
insert into repair_progress values(15, 4, 'dispatch', '派单',     '调度员将工单指派给维修工【王师傅】',   2, null, date_sub(sysdate(), interval 1 day));

insert into repair_progress values(16, 5, 'submit',   '提交报修', '居民提交报修申请：客厅插座频繁跳闸',   7, null, date_sub(sysdate(), interval 3 hour));
insert into repair_progress values(17, 6, 'submit',   '提交报修', '居民提交报修申请：楼道堆放杂物影响通行', 8, null, date_sub(sysdate(), interval 5 hour));
insert into repair_progress values(18, 7, 'submit',   '提交报修', '居民提交报修申请：卫生间下水道堵塞',   6, null, date_sub(sysdate(), interval 2 day));
insert into repair_progress values(19, 7, 'cancel',   '取消报修', '居民取消报修，原因：自行联系疏通后已解决，撤销报修', 6, null, date_sub(sysdate(), interval 2 day));
insert into repair_progress values(20, 8, 'submit',   '提交报修', '居民提交报修申请：卧室窗户玻璃破损',   7, null, date_sub(sysdate(), interval 12 day));
insert into repair_progress values(21, 8, 'dispatch', '派单',     '调度员将工单指派给维修工【李师傅】',   2, null, date_sub(sysdate(), interval 12 day));
insert into repair_progress values(22, 8, 'accept',   '维修工接单', '维修工已接单，开始上门维修',          3, null, date_sub(sysdate(), interval 12 day));
insert into repair_progress values(23, 8, 'finish',   '维修完工', '维修结果：更换钢化玻璃并加固窗框',     3, null, date_sub(sysdate(), interval 11 day));
insert into repair_progress values(24, 8, 'rate',     '居民评价', '居民给出 4 星评价：处理及时，价格合理', 7, null, date_sub(sysdate(), interval 11 day));

-- 评价
insert into repair_evaluation values(1, 1, 6, 3, 5, 5, 5, 5, '师傅很专业，修得很快，还帮忙清理了积水', '感谢您的认可，后续有问题随时报修', date_sub(sysdate(), interval 8 day), date_sub(sysdate(), interval 8 day));
insert into repair_evaluation values(2, 8, 7, 3, 4, 4, 5, 4, '处理及时，价格合理', null, null, date_sub(sysdate(), interval 11 day));

-- 站内消息
insert into repair_message values(1, 3, '新报修工单待接单', '工单 BX202605040004（客厅空调不制冷）已指派给您，请及时接单。', 'dispatch', 4, 0, date_sub(sysdate(), interval 1 day));
insert into repair_message values(2, 5, '新报修工单待接单', '工单 BX202605030003（单元楼道照明灯不亮）已指派给您，请及时接单。', 'dispatch', 3, 1, date_sub(sysdate(), interval 2 day));
insert into repair_message values(3, 6, '报修已完工，待评价', '您的报修 BX202605010001 已维修完成，请对本次服务进行评价。', 'finish', 1, 1, date_sub(sysdate(), interval 8 day));
insert into repair_message values(4, 3, '收到新的服务评价', '工单 BX202605010001 获得 5 星评价。', 'rate', 1, 1, date_sub(sysdate(), interval 8 day));
insert into repair_message values(5, 7, '报修已完工，待评价', '您的报修 BX202605020002 已维修完成，请对本次服务进行评价。', 'finish', 2, 0, date_sub(sysdate(), interval 1 day));

-- 定时任务：每日超时扫描
delete from sys_job where job_name = '报修工单超时扫描';
insert into sys_job values(100, '报修工单超时扫描', 'DEFAULT', 'repairTask.scanOvertime()', '0 0/30 * * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '每 30 分钟扫描一次超时工单');
