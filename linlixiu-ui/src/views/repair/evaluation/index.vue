<template>
  <div class="app-container">
    <!-- 评价概览 -->
    <el-row :gutter="12" class="mb16">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">累计评价</div>
          <div class="stat-value">{{ summary.total || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">平均评分</div>
          <div class="stat-value score">{{ summary.avgScore || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">好评数（≥4 星）</div>
          <div class="stat-value good">{{ summary.goodCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">差评数（≤2 星）</div>
          <div class="stat-value bad">{{ summary.badCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="报修单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入报修单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="评分" prop="score">
        <el-select v-model="queryParams.score" placeholder="请选择评分" clearable>
          <el-option v-for="n in 5" :key="n" :label="n + ' 星'" :value="n" />
        </el-select>
      </el-form-item>
      <el-form-item label="评价时间">
        <el-date-picker v-model="daterangeCreateTime" style="width: 240px" value-format="yyyy-MM-dd"
          type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['repair:evaluation:list']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="evaluationList">
      <el-table-column label="报修单号" align="center" prop="orderNo" width="170" />
      <el-table-column label="维修工" align="center" prop="workerName" width="90" />
      <el-table-column label="评价人" align="center" prop="residentName" width="90" />
      <el-table-column label="综合评分" align="center" width="160">
        <template slot-scope="scope">
          <el-tag v-if="!scope.row.evaluationId" type="warning" size="small">待评价</el-tag>
          <el-rate v-else :value="scope.row.score" disabled show-score text-color="#ff9900" score-template="{value}" />
        </template>
      </el-table-column>
      <el-table-column label="分项评分" align="center" width="200">
        <template slot-scope="scope">
          <span v-if="scope.row.evaluationId" class="sub-score">
            速度 {{ scope.row.speedScore }} · 质量 {{ scope.row.qualityScore }} · 态度 {{ scope.row.attitudeScore }}
          </span>
          <span v-else class="sub-score">-</span>
        </template>
      </el-table-column>
      <el-table-column label="评价内容" align="center" prop="content" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.content || "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="评价时间" align="center" prop="createTime" width="150">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime || "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <template v-if="scope.row.evaluationId">
            <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
            <el-button size="mini" type="text" icon="el-icon-chat-dot-round" @click="handleReply(scope.row)"
              v-hasPermi="['repair:evaluation:reply']">回复</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
              v-hasPermi="['repair:evaluation:remove']">删除</el-button>
          </template>
          <el-button v-else size="mini" type="text" icon="el-icon-star-on" @click="goEvaluate">去评价</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 回复评价 -->
    <el-dialog title="回复评价" :visible.sync="replyOpen" width="500px" append-to-body>
      <el-form :model="replyForm" label-width="90px">
        <el-form-item label="居民评价">
          <div class="quote">{{ current.content || '（未填写文字评价）' }}</div>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.replyContent" type="textarea" :rows="4"
            placeholder="如：感谢您的反馈，我们已安排复查" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReply">提 交</el-button>
        <el-button @click="replyOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 评价详情 -->
    <el-dialog title="评价详情" :visible.sync="detailOpen" width="520px" append-to-body>
      <div class="detail-item"><span class="detail-label">报修单号</span><span>{{ detail.orderNo }}</span></div>
      <div class="detail-item"><span class="detail-label">维修工</span><span>{{ detail.workerName }}</span></div>
      <div class="detail-item"><span class="detail-label">评价人</span><span>{{ detail.residentName }}</span></div>
      <div class="detail-item">
        <span class="detail-label">综合评分</span>
        <el-rate :value="detail.score" disabled show-score text-color="#ff9900" score-template="{value}" />
      </div>
      <div class="detail-item">
        <span class="detail-label">分项评分</span>
        <span>速度 {{ detail.speedScore }} · 质量 {{ detail.qualityScore }} · 态度 {{ detail.attitudeScore }}</span>
      </div>
      <div class="detail-item"><span class="detail-label">评价内容</span><span>{{ detail.content || "（未填写文字评价）" }}</span></div>
      <div class="detail-item"><span class="detail-label">评价时间</span><span>{{ detail.createTime }}</span></div>
      <div class="detail-item" v-if="detail.replyContent">
        <span class="detail-label">官方回复</span><span>{{ detail.replyContent }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listEvaluation, replyEvaluation, delEvaluation, summaryEvaluation } from "@/api/repair/evaluation";

export default {
  name: "RepairEvaluation",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      evaluationList: [],
      summary: {},
      // 评价详情
      detailOpen: false,
      detail: {},
      daterangeCreateTime: [],
      replyOpen: false,
      current: {},
      replyForm: { evaluationId: null, replyContent: "" },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        score: null,
        params: {}
      }
    };
  },
  created() {
    this.getList();
    this.getSummary();
  },
  methods: {
    // 查看评价详情
    handleDetail(row) {
      this.detail = row;
      this.detailOpen = true;
    },
    // 去评价：跳到「我要报修」页面，在那边完成评价
    goEvaluate() {
      this.$router.push("/repair/order");
    },
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (this.daterangeCreateTime && this.daterangeCreateTime.length) {
        this.queryParams.params["beginTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endTime"] = this.daterangeCreateTime[1];
      }
      listEvaluation(this.queryParams).then(response => {
        this.evaluationList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getSummary() {
      summaryEvaluation().then(res => {
        this.summary = res.data || {};
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.daterangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleReply(row) {
      this.current = row;
      this.replyForm = { evaluationId: row.evaluationId, replyContent: row.replyContent || "" };
      this.replyOpen = true;
    },
    submitReply() {
      if (!this.replyForm.replyContent) {
        this.$modal.msgWarning("请填写回复内容");
        return;
      }
      replyEvaluation(this.replyForm).then(() => {
        this.$modal.msgSuccess("回复成功");
        this.replyOpen = false;
        this.getList();
      });
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除该条评价吗？').then(() => {
        return delEvaluation(row.evaluationId);
      }).then(() => {
        this.getList();
        this.getSummary();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download("/repair/evaluation/export", { ...this.queryParams }, `评价反馈_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.mb16 {
  margin-bottom: 16px;
}
.card-title {
  font-weight: 600;
  color: #303133;
}
.detail-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}
.detail-item:last-child {
  border-bottom: none;
}
.detail-label {
  width: 90px;
  flex-shrink: 0;
  color: #909399;
}
.stat-card {
  text-align: center;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 22px;
  font-weight: 600;
  margin-top: 6px;
  color: #303133;
}
.stat-value.score {
  color: #e6a23c;
}
.stat-value.good {
  color: #67c23a;
}
.stat-value.bad {
  color: #f56c6c;
}
.sub-score {
  color: #606266;
  font-size: 12px;
}
.quote {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 12px;
  color: #606266;
  line-height: 1.6;
}
</style>
