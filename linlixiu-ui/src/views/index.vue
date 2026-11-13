<template>
  <div class="app-container home">
    <!-- 欢迎 -->
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <div class="welcome-left">
          <div class="hello">您好，{{ nickName }}！</div>
          <div class="hello-sub">
            欢迎使用邻里修，您当前所在部门：<span class="dept-name">{{ deptName }}</span>
          </div>
        </div>
        <div class="welcome-right">
          <div class="date">{{ today }}</div>
          <div class="tip">报修不跑腿 · 派单有响应 · 进度可追踪</div>
        </div>
      </div>
    </el-card>

    <!-- 数据概览：仅调度员与管理员可见 -->
    <el-row :gutter="12" class="mb16" v-if="canViewStats">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">累计报修</div>
          <div class="stat-value">{{ overview.total || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">待派单</div>
          <div class="stat-value warning">{{ overview.pendingCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">维修中</div>
          <div class="stat-value primary">{{ overview.repairingCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">完成率</div>
          <div class="stat-value success">{{ overview.finishRate || 0 }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card shadow="never" class="mb16">
      <div slot="header" class="card-title">快捷入口</div>
      <el-row :gutter="12">
        <el-col :span="entrySpan" v-for="item in entries" :key="item.path">
          <div class="entry" @click="go(item.path)" v-hasPermi="item.perm">
            <div class="entry-icon" :style="{ background: item.color }">
              <i :class="item.icon"></i>
              <span
                class="entry-dot"
                v-if="dotCount(item) > 0"
                :class="{ 'is-dot': dotCount(item) > 9 }"
              >{{ dotCount(item) > 9 ? "" : dotCount(item) }}</span>
            </div>
            <div class="entry-text">
              <div class="entry-title">{{ item.title }}</div>
              <div class="entry-desc">{{ item.desc }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="12">
      <!-- 报修流程 -->
      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header" class="card-title">报修处理流程</div>
          <el-steps :active="5" align-center finish-status="success" class="steps">
            <el-step title="提交报修" description="居民填写故障信息与现场照片" />
            <el-step title="工单派单" description="调度员按技能指派维修工" />
            <el-step title="上门维修" description="维修工接单并更新处理进度" />
            <el-step title="完工回单" description="登记维修结果，等待居民评价" />
            <el-step title="服务评价" description="居民打分，工单归档" />
          </el-steps>
          <div class="flow-tip">
            工单状态流转：待派单 → 待接单 → 维修中 → 待评价 → 已完成；居民可在派单前取消报修，维修工可退回工单重新调度。
          </div>
        </el-card>
      </el-col>

      <!-- 公告栏 + 角色说明 -->
      <el-col :span="10">
        <el-card shadow="never" class="mb16">
          <div slot="header" class="card-title">公告栏</div>
          <div v-if="notices.length === 0" class="notice-empty">暂无公告</div>
          <div
            class="notice-item"
            v-for="n in notices"
            :key="n.noticeId"
            @click="openNotice(n)"
          >
            <i
              class="notice-icon"
              :class="n.noticeType === '1' ? 'el-icon-bell is-info' : 'el-icon-document is-warn'"
            ></i>
            <span class="notice-title">{{ n.noticeTitle }}</span>
            <span class="notice-date">{{ parseTime(n.createTime, "{m}-{d}") }}</span>
          </div>
        </el-card>

        <!-- 系统角色：仅管理员可见 -->
        <el-card shadow="never" v-if="isAdmin">
          <div slot="header" class="card-title">系统角色</div>
          <div class="role-item" v-for="r in roles" :key="r.name">
            <el-tag :type="r.type" size="small" class="role-tag">{{ r.name }}</el-tag>
            <span class="role-desc">{{ r.desc }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告详情 -->
    <el-dialog
      :title="currentNotice.noticeTitle"
      :visible.sync="noticeVisible"
      width="620px"
      append-to-body
    >
      <div class="notice-content" v-html="currentNotice.noticeContent"></div>
    </el-dialog>
  </div>
</template>

<script>
import { getOverview, getBadges, getNotices } from "@/api/repair/stats";
import { getUserProfile } from "@/api/system/user";

export default {
  name: "Index",
  data() {
    return {
      overview: {},
      canViewStats: false,
      entrySpan: 6,
      nickName: "",
      deptName: "",
      today: "",
      entries: [
        { key: "order", title: "我要报修", desc: "提交新的报修申请", path: "/repair/order", perm: ["repair:order:list"], icon: "el-icon-edit-outline", color: "#409eff" },
        { key: "dispatch", title: "工单派单", desc: "指派维修工并催办", path: "/repair/dispatch", perm: ["repair:dispatch:list"], icon: "el-icon-s-promotion", color: "#e6a23c" },
        { key: "task", title: "维修任务", desc: "接单、更新进度与完工", path: "/repair/task", perm: ["repair:task:list"], icon: "el-icon-s-tools", color: "#67c23a" },
        { key: "message", title: "消息中心", desc: "查看派单与催办通知", path: "/repair/message", perm: ["repair:message:list"], icon: "el-icon-bell", color: "#909399" },
        { key: "evaluation", title: "评价反馈", desc: "评价已完工的报修单", path: "/repair/evaluation", perm: ["repair:evaluation:list"], icon: "el-icon-star-on", color: "#f56c6c" }
      ],
      // 快捷入口红点数据：未读消息 / 维修工待办任务 / 居民待评价报修
      badges: { unread: 0, pendingTask: 0, pendingEvaluation: 0 },
      // 公告栏
      notices: [],
      noticeVisible: false,
      currentNotice: {},
      // 系统角色卡片仅管理员可见
      isAdmin: false,
      roles: [
        { name: "居民", type: "primary", desc: "提交报修、跟踪进度、服务评价" },
        { name: "调度员", type: "warning", desc: "受理工单、按技能派单、超时催办" },
        { name: "维修工", type: "success", desc: "接单上门、更新进度、登记维修结果" },
        { name: "社区管理员", type: "danger", desc: "维修工档案、公告发布、统计报表" }
      ]
    };
  },
  created() {
    this.nickName = this.$store.state.user.name || "用户";
    this.today = this.parseTime(new Date(), "{y} 年 {m} 月 {d} 日");
    getUserProfile().then(res => {
      const data = res.data || {};
      this.nickName = data.nickName || this.nickName;
      this.deptName = (data.dept && data.dept.deptName) || "阳光社区";
    }).catch(() => {
      this.deptName = "阳光社区";
    });
    this.canViewStats = this.$auth.hasPermi("repair:stats:view");
    // 快捷入口超过 4 个时收窄每张卡片，保证一行排得下
    const visibleCount = this.entries.filter(e => (e.perm || []).some(p => this.$auth.hasPermi(p))).length;
    this.entrySpan = visibleCount > 4 ? 4 : 6;
    this.loadBadges();
    this.isAdmin = this.$auth.hasRole("admin");
    this.loadNotices();
    if (this.canViewStats) {
      getOverview().then(res => {
        this.overview = res.data || {};
      }).catch(() => {});
    }
  },
  // 首页被 keep-alive 缓存，从其他页面切回来时 created 不会再执行，
  // 这里重新拉一次红点和公告，避免处理完待办后红点不消失
  activated() {
    this.loadBadges();
    this.loadNotices();
  },
  methods: {
    // 拉取快捷入口的红点数据
    loadBadges() {
      getBadges().then(res => {
        this.badges = Object.assign(
          { unread: 0, pendingTask: 0, pendingEvaluation: 0 },
          res.data || {}
        );
      }).catch(() => {});
    },
    // 拉取公告栏数据
    loadNotices() {
      getNotices().then(res => {
        this.notices = res.data || [];
      }).catch(() => {});
    },
    // 查看公告详情
    openNotice(notice) {
      this.currentNotice = notice;
      this.noticeVisible = true;
    },
    // 某个入口的待处理数量：1~9 显示数字，超过 9 只显示红点
    dotCount(item) {
      if (!item || !item.key) return 0;
      if (item.key === "message") return this.badges.unread || 0;
      if (item.key === "task") return this.badges.pendingTask || 0;
      // 待评价的报修单在「评价反馈」上提示，「我要报修」不再冒红点
      if (item.key === "evaluation") return this.badges.pendingEvaluation || 0;
      return 0;
    },
    go(path) {
      this.$router.push(path);
    }
  }
};
</script>

<style scoped lang="scss">
.home {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
}
.mb16 {
  margin-bottom: 16px;
}
.welcome-card {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #409eff 0%, #2f7d4f 100%);
  border: none;
  ::v-deep .el-card__body {
    padding: 24px 28px;
  }
}
.welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}
.hello {
  font-size: 22px;
  font-weight: 600;
}
.hello-sub {
  margin-top: 8px;
  font-size: 13px;
  opacity: 0.9;
}
.hello-sub .dept-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.5px;
  vertical-align: -1px;
}
.welcome-right {
  text-align: right;
}
.date {
  font-size: 15px;
  font-weight: 600;
}
.tip {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.85;
}
.stat-card {
  text-align: center;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
  margin-top: 8px;
  color: #303133;
}
.stat-value.warning {
  color: #e6a23c;
}
.stat-value.primary {
  color: #409eff;
}
.stat-value.success {
  color: #67c23a;
}
.card-title {
  font-weight: 600;
  color: #303133;
}
.entry {
  display: flex;
  align-items: center;
  padding: 14px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.entry:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}
.entry-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  margin-right: 12px;
  flex-shrink: 0;
  position: relative;
}
.entry-dot {
  position: absolute;
  top: -6px;
  right: -8px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  box-sizing: border-box;
  border-radius: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  box-shadow: 0 0 0 2px #fff;
}
/* 超过 9 条只显示一个小圆点 */
.entry-dot.is-dot {
  top: -3px;
  right: -3px;
  min-width: 9px;
  width: 9px;
  height: 9px;
  padding: 0;
  border-radius: 50%;
  font-size: 0;
}
.entry-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.entry-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.steps {
  padding: 10px 0 4px;
}
.flow-tip {
  margin-top: 16px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 12.5px;
  line-height: 1.7;
}
.notice-empty {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 18px 0;
}
.notice-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
  cursor: pointer;
}
.notice-item:last-child {
  border-bottom: none;
}
.notice-item:hover .notice-title {
  color: #409eff;
}
.notice-icon {
  font-size: 15px;
  margin-right: 8px;
  flex-shrink: 0;
}
.notice-icon.is-warn {
  color: #e6a23c;
}
.notice-icon.is-info {
  color: #409eff;
}
.notice-title {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}
.notice-date {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: 8px;
  flex-shrink: 0;
}
.notice-content {
  line-height: 1.8;
  font-size: 14px;
  color: #606266;
}
.notice-content img {
  max-width: 100%;
}
.role-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}
.role-item:last-child {
  border-bottom: none;
}
.role-tag {
  width: 84px;
  text-align: center;
  flex-shrink: 0;
}
.role-desc {
  margin-left: 12px;
  color: #606266;
  font-size: 13px;
}
</style>
