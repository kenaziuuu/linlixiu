<template>
  <div class="app-container">
    <!-- 我的维修工档案 -->
    <el-row :gutter="12" class="mb16" v-if="profile">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">工号 / 姓名</div>
          <div class="stat-value">{{ profile.workerNo }} · {{ profile.workerName }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">累计接单 / 完成</div>
          <div class="stat-value">{{ profile.totalCount }} / {{ profile.finishCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">平均评分</div>
          <div class="stat-value score">{{ profile.avgScore || '暂无' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">在岗状态</div>
          <div class="stat-value">
            <el-tag :type="profile.status === '0' ? 'success' : 'info'" size="small">
              {{ profile.status === '0' ? '在岗' : (profile.status === '1' ? '休息' : '离职') }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-radio-group v-model="quickStatus" size="small" class="mb8" @change="handleQuickStatus">
      <el-radio-button label="">全部任务</el-radio-button>
      <el-radio-button label="assigned">待接单</el-radio-button>
      <el-radio-button label="repairing">维修中</el-radio-button>
      <el-radio-button label="completed">已完工</el-radio-button>
    </el-radio-group>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="报修单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入报修单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="报修类型" prop="repairType">
        <el-select v-model="queryParams.repairType" placeholder="请选择报修类型" clearable>
          <el-option v-for="dict in dict.type.repair_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['repair:task:list']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" :row-class-name="rowClassName">
      <el-table-column label="报修单号" align="center" prop="orderNo" width="170" />
      <el-table-column label="报修类型" align="center" prop="repairType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.repair_type" :value="scope.row.repairType" />
        </template>
      </el-table-column>
      <el-table-column label="报修标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="报修地址" align="center" prop="address" :show-overflow-tooltip="true" width="180" />
      <el-table-column label="紧急程度" align="center" prop="urgency" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.repair_urgency" :value="scope.row.urgency" />
        </template>
      </el-table-column>
      <el-table-column label="工单状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.repair_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="派单时间" align="center" prop="dispatchTime" width="150" />
      <el-table-column label="操作" align="center" width="250" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === 'assigned'" size="mini" type="text" icon="el-icon-check"
            @click="handleAccept(scope.row)" v-hasPermi="['repair:task:accept']">接单</el-button>
          <el-button v-if="scope.row.status === 'repairing'" size="mini" type="text" icon="el-icon-edit-outline"
            @click="handleProgress(scope.row)" v-hasPermi="['repair:task:progress']">进度</el-button>
          <el-button v-if="scope.row.status === 'repairing'" size="mini" type="text" icon="el-icon-finished"
            @click="handleFinish(scope.row)" v-hasPermi="['repair:task:finish']">完工</el-button>
          <el-button v-if="scope.row.status === 'assigned' || scope.row.status === 'repairing'" size="mini" type="text"
            icon="el-icon-back" @click="handleReturn(scope.row)" v-hasPermi="['repair:task:return']">退回</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 完工 -->
    <el-dialog title="提交完工" :visible.sync="finishOpen" width="520px" append-to-body>
      <el-form :model="finishForm" label-width="90px">
        <el-form-item label="报修单号">
          <span class="strong">{{ currentOrder.orderNo }}</span>
        </el-form-item>
        <el-form-item label="维修结果" required>
          <el-input v-model="finishForm.finishResult" type="textarea" :rows="4"
            placeholder="请填写维修过程与结果，如：更换水龙头密封圈，已试水正常" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFinish">提交完工</el-button>
        <el-button @click="finishOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 追加进度 -->
    <el-dialog title="更新维修进度" :visible.sync="progressOpen" width="480px" append-to-body>
      <el-form :model="progressForm" label-width="90px">
        <el-form-item label="进度说明">
          <el-input v-model="progressForm.remark" type="textarea" :rows="3"
            placeholder="如：已到达现场，正在排查故障" maxlength="200" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitProgress">提 交</el-button>
        <el-button @click="progressOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 退回 -->
    <el-dialog title="退回工单" :visible.sync="returnOpen" width="460px" append-to-body>
      <el-form :model="returnForm" label-width="90px">
        <el-form-item label="退回原因" required>
          <el-input v-model="returnForm.cancelReason" type="textarea" :rows="3"
            placeholder="如：缺少配件，需重新调度" maxlength="200" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReturn">确认退回</el-button>
        <el-button @click="returnOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer title="工单详情" :visible.sync="detailOpen" size="520px" append-to-body>
      <div v-if="current" class="detail-wrap">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="报修单号">{{ current.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="报修标题">{{ current.title }}</el-descriptions-item>
          <el-descriptions-item label="工单状态">
            <dict-tag :options="dict.type.repair_status" :value="current.status" />
          </el-descriptions-item>
          <el-descriptions-item label="报修地址">{{ current.address }}</el-descriptions-item>
          <el-descriptions-item label="报修人">{{ current.residentName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ current.contactName }} / {{ current.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="问题描述">{{ current.description }}</el-descriptions-item>
          <el-descriptions-item label="维修结果">{{ current.finishResult || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="timeline-title">处理进度</div>
        <el-timeline>
          <el-timeline-item v-for="item in progressList" :key="item.progressId" :timestamp="item.createTime" placement="top">
            <div class="node-name">{{ item.nodeName }}</div>
            <div class="node-content">{{ item.content }}</div>
            <div class="node-op" v-if="item.operatorName">操作人：{{ item.operatorName }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { listTask, getTask, getWorkerProfile, acceptTask, finishTask, returnTask, addTaskProgress } from "@/api/repair/task";
import { getProgress } from "@/api/repair/order";

export default {
  name: "RepairTask",
  dicts: ["repair_type", "repair_status", "repair_urgency"],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      taskList: [],
      profile: null,
      quickStatus: "",
      finishOpen: false,
      progressOpen: false,
      returnOpen: false,
      detailOpen: false,
      currentOrder: {},
      current: null,
      progressList: [],
      finishForm: { orderId: null, finishResult: "" },
      progressForm: { orderId: null, remark: "" },
      returnForm: { orderId: null, cancelReason: "" },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        repairType: null,
        status: null
      }
    };
  },
  created() {
    this.getList();
    this.loadProfile();
  },
  methods: {
    getList() {
      this.loading = true;
      listTask(this.queryParams).then(response => {
        this.taskList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    loadProfile() {
      getWorkerProfile().then(res => {
        this.profile = res.data;
      });
    },
    rowClassName({ row }) {
      return row.isOvertime === 1 ? "overtime-row" : "";
    },
    handleQuickStatus(val) {
      this.queryParams.status = val || null;
      this.queryParams.pageNum = 1;
      this.getList();
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.quickStatus = "";
      this.resetForm("queryForm");
      this.queryParams.status = null;
      this.handleQuery();
    },
    handleAccept(row) {
      this.$modal.confirm('确认接受工单「' + row.orderNo + '」吗？').then(() => {
        return acceptTask(row.orderId);
      }).then(() => {
        this.$modal.msgSuccess("接单成功，请尽快上门处理");
        this.getList();
        this.loadProfile();
      }).catch(() => {});
    },
    handleFinish(row) {
      this.currentOrder = row;
      this.finishForm = { orderId: row.orderId, finishResult: "" };
      this.finishOpen = true;
    },
    submitFinish() {
      if (!this.finishForm.finishResult) {
        this.$modal.msgWarning("请填写维修结果");
        return;
      }
      finishTask(this.finishForm).then(() => {
        this.$modal.msgSuccess("完工提交成功，已通知居民评价");
        this.finishOpen = false;
        this.getList();
        this.loadProfile();
      });
    },
    handleProgress(row) {
      this.progressForm = { orderId: row.orderId, remark: "" };
      this.progressOpen = true;
    },
    submitProgress() {
      if (!this.progressForm.remark) {
        this.$modal.msgWarning("请填写进度说明");
        return;
      }
      addTaskProgress(this.progressForm).then(() => {
        this.$modal.msgSuccess("进度已更新");
        this.progressOpen = false;
      });
    },
    handleReturn(row) {
      this.returnForm = { orderId: row.orderId, cancelReason: "" };
      this.returnOpen = true;
    },
    submitReturn() {
      if (!this.returnForm.cancelReason) {
        this.$modal.msgWarning("请填写退回原因");
        return;
      }
      returnTask(this.returnForm).then(() => {
        this.$modal.msgSuccess("已退回，工单将重新派单");
        this.returnOpen = false;
        this.getList();
      });
    },
    handleDetail(row) {
      getTask(row.orderId).then(response => {
        this.current = response.data;
        this.detailOpen = true;
        getProgress(row.orderId).then(res => {
          this.progressList = res.data || [];
        });
      });
    },
    handleExport() {
      this.download("/repair/task/export", { ...this.queryParams }, `维修任务_${new Date().getTime()}.xlsx`);
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
.stat-card {
  text-align: center;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 20px;
  font-weight: 600;
  margin-top: 6px;
  color: #303133;
}
.stat-value.score {
  color: #e6a23c;
}
.detail-wrap {
  padding: 0 20px 20px;
}
.timeline-title {
  margin: 20px 0 12px;
  font-weight: 600;
  color: #303133;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}
.node-name {
  font-weight: 600;
}
.node-content {
  color: #606266;
  font-size: 13px;
  margin-top: 4px;
}
.node-op {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}
.strong {
  font-weight: 600;
}
</style>

<style>
.overtime-row {
  background-color: #fef0f0 !important;
}
</style>
