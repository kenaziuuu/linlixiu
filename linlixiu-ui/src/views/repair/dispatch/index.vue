<template>
  <div class="app-container">
    <el-radio-group v-model="quickStatus" size="small" class="mb8" @change="handleQuickStatus">
      <el-radio-button label="">全部工单</el-radio-button>
      <el-radio-button label="pending">待派单</el-radio-button>
      <el-radio-button label="assigned">待接单</el-radio-button>
      <el-radio-button label="repairing">维修中</el-radio-button>
      <el-radio-button label="completed">待评价</el-radio-button>
      <el-radio-button label="rated">已完成</el-radio-button>
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
      <el-form-item label="紧急程度" prop="urgency">
        <el-select v-model="queryParams.urgency" placeholder="请选择紧急程度" clearable>
          <el-option v-for="dict in dict.type.repair_urgency" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="报修地址" prop="address">
        <el-input v-model="queryParams.address" placeholder="请输入报修地址" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['repair:dispatch:list']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" :row-class-name="rowClassName">
      <el-table-column label="报修单号" align="center" prop="orderNo" width="170" />
      <el-table-column label="报修类型" align="center" prop="repairType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.repair_type" :value="scope.row.repairType" />
        </template>
      </el-table-column>
      <el-table-column label="报修标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="报修地址" align="center" prop="address" :show-overflow-tooltip="true" width="170" />
      <el-table-column label="报修人" align="center" prop="residentName" width="90" />
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
      <el-table-column label="维修工" align="center" prop="workerName" width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.workerName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="createTime" width="150" />
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === 'pending'" size="mini" type="text" icon="el-icon-s-promotion"
            @click="handleAssign(scope.row)" v-hasPermi="['repair:dispatch:assign']">派单</el-button>
          <el-button v-if="scope.row.status === 'assigned'" size="mini" type="text" icon="el-icon-refresh"
            @click="handleAssign(scope.row)" v-hasPermi="['repair:dispatch:assign']">改派</el-button>
          <el-button v-if="scope.row.workerId && (scope.row.status === 'assigned' || scope.row.status === 'repairing')"
            size="mini" type="text" icon="el-icon-bell" @click="handleUrge(scope.row)"
            v-hasPermi="['repair:dispatch:urge']">催办</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 派单 -->
    <el-dialog title="工单派单" :visible.sync="assignOpen" width="560px" append-to-body>
      <el-form :model="assignForm" label-width="90px">
        <el-form-item label="报修单号">
          <span class="strong">{{ currentOrder.orderNo }}</span>
        </el-form-item>
        <el-form-item label="报修内容">
          <span>{{ currentOrder.title }}（{{ currentOrder.address }}）</span>
        </el-form-item>
        <el-form-item label="报修类型">
          <dict-tag :options="dict.type.repair_type" :value="currentOrder.repairType" />
        </el-form-item>
        <el-form-item label="维修工" prop="workerId">
          <el-select v-model="assignForm.workerId" placeholder="请选择维修工" filterable style="width: 100%">
            <el-option v-for="w in workerList" :key="w.workerId" :value="w.workerId"
              :label="`${w.workerName}（${w.workerNo}） 接单 ${w.finishCount} 单 / 评分 ${w.avgScore}`" />
          </el-select>
          <div class="tip">已按报修类型过滤具备对应技能的维修工，按评分与接单量排序</div>
        </el-form-item>
        <el-form-item label="派单说明">
          <el-input v-model="assignForm.remark" type="textarea" :rows="3" placeholder="可填写上门时间、注意事项等" maxlength="200" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAssign">确认派单</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer title="工单详情" :visible.sync="detailOpen" size="520px" append-to-body>
      <div v-if="current" class="detail-wrap">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="报修单号">{{ current.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="报修标题">{{ current.title }}</el-descriptions-item>
          <el-descriptions-item label="报修类型">
            <dict-tag :options="dict.type.repair_type" :value="current.repairType" />
          </el-descriptions-item>
          <el-descriptions-item label="工单状态">
            <dict-tag :options="dict.type.repair_status" :value="current.status" />
            <el-tag v-if="current.isOvertime === 1" type="danger" size="mini" style="margin-left:6px">超时</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报修地址">{{ current.address }}</el-descriptions-item>
          <el-descriptions-item label="报修人">{{ current.residentName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ current.contactName }} / {{ current.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="问题描述">{{ current.description }}</el-descriptions-item>
          <el-descriptions-item label="维修工">{{ current.workerName || '待派单' }}</el-descriptions-item>
          <el-descriptions-item label="派单时间">{{ current.dispatchTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="接单时间">{{ current.acceptTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完工时间">{{ current.finishTime || '-' }}</el-descriptions-item>
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
import { listDispatch, getDispatch, listAvailableWorkers, assignOrder, urgeOrder } from "@/api/repair/dispatch";
import { getProgress } from "@/api/repair/order";

export default {
  name: "RepairDispatch",
  dicts: ["repair_type", "repair_status", "repair_urgency"],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      orderList: [],
      workerList: [],
      quickStatus: "",
      assignOpen: false,
      detailOpen: false,
      currentOrder: {},
      current: null,
      progressList: [],
      assignForm: { orderId: null, workerId: null, remark: "" },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        repairType: null,
        urgency: null,
        address: null,
        status: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listDispatch(this.queryParams).then(response => {
        this.orderList = response.rows;
        this.total = response.total;
        this.loading = false;
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
    handleAssign(row) {
      this.currentOrder = row;
      this.assignForm = { orderId: row.orderId, workerId: null, remark: "" };
      listAvailableWorkers(row.repairType).then(res => {
        this.workerList = res.data || [];
        this.assignOpen = true;
      });
    },
    submitAssign() {
      if (!this.assignForm.workerId) {
        this.$modal.msgWarning("请选择维修工");
        return;
      }
      assignOrder(this.assignForm).then(() => {
        this.$modal.msgSuccess("派单成功，已通知维修工");
        this.assignOpen = false;
        this.getList();
      });
    },
    handleUrge(row) {
      this.$modal.confirm('确认向维修工「' + row.workerName + '」发送催办提醒吗？').then(() => {
        return urgeOrder(row.orderId);
      }).then(() => {
        this.$modal.msgSuccess("催办已发送");
        this.getList();
      }).catch(() => {});
    },
    handleDetail(row) {
      getDispatch(row.orderId).then(response => {
        this.current = response.data;
        this.detailOpen = true;
        getProgress(row.orderId).then(res => {
          this.progressList = res.data || [];
        });
      });
    },
    handleExport() {
      this.download("/repair/dispatch/export", { ...this.queryParams }, `报修工单_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
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
.tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
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
