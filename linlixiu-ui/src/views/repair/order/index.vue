<template>
  <div class="app-container">
    <!-- 状态快捷筛选 -->
    <el-radio-group v-model="quickStatus" size="small" class="mb8" @change="handleQuickStatus">
      <el-radio-button label="">全部</el-radio-button>
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
      <el-form-item label="工单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择工单状态" clearable>
          <el-option v-for="dict in dict.type.repair_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="提交时间">
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['repair:order:add']">我要报修</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['repair:order:export']">导出</el-button>
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
      <el-table-column label="维修工" align="center" prop="workerName" width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.workerName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="createTime" width="150" />
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === 'pending'" size="mini" type="text" icon="el-icon-edit"
            @click="handleUpdate(scope.row)" v-hasPermi="['repair:order:edit']">修改</el-button>
          <el-button v-if="scope.row.status === 'pending' || scope.row.status === 'assigned'" size="mini" type="text"
            icon="el-icon-circle-close" @click="handleCancel(scope.row)" v-hasPermi="['repair:order:edit']">取消</el-button>
          <el-button v-if="scope.row.status === 'completed'" size="mini" type="text" icon="el-icon-star-on"
            @click="handleEvaluate(scope.row)" v-hasPermi="['repair:evaluation:add']">评价</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 报修表单 -->
    <el-dialog :title="title" :visible.sync="open" width="640px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="报修类型" prop="repairType">
              <el-select v-model="form.repairType" placeholder="请选择报修类型" style="width: 100%">
                <el-option v-for="dict in dict.type.repair_type" :key="dict.value" :label="dict.label"
                  :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="urgency">
              <span slot="label">紧急程度</span>
              <div class="urgency-wrap">
                <el-select v-model="form.urgency" placeholder="请选择紧急程度" style="width: 100%">
                  <el-option v-for="dict in dict.type.repair_urgency" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>
                <el-tooltip placement="top" effect="dark">
                  <div slot="content" style="line-height: 1.7; max-width: 350px;">
                    <div><b>普通</b>：一般性故障，不影响正常生活，可安排在 3 个工作日内处理</div>
                    <div style="margin-top: 6px;"><b>紧急</b>：影响日常生活，如水路漏水、局部断电，需在 24 小时内处理</div>
                    <div style="margin-top: 6px;"><b>特急</b>：严重威胁居民人身安全，如燃气泄漏、电梯困人、电线冒火，需立即处理</div>
                  </div>
                  <i class="el-icon-question urgency-help"></i>
                </el-tooltip>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="报修标题" prop="title">
          <el-input v-model="form.title" placeholder="请用一句话描述问题，如：厨房水龙头漏水" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="报修地址" prop="address">
          <el-input v-model="form.address" placeholder="如：阳光花园 3 栋 2 单元 501" maxlength="100" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请补充故障现象、发生时间等信息" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="现场照片">
          <image-upload v-model="form.images" :limit="3" :file-size="5" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 工单详情 -->
    <el-drawer title="报修详情" :visible.sync="detailOpen" size="520px" append-to-body>
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
          <el-descriptions-item label="联系人">{{ current.contactName }} / {{ current.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="问题描述">{{ current.description }}</el-descriptions-item>
          <el-descriptions-item label="维修工">{{ current.workerName || '待派单' }}</el-descriptions-item>
          <el-descriptions-item label="维修结果">{{ current.finishResult || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ current.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="current.images" class="img-box">
          <el-image v-for="(img, idx) in current.images.split(',')" :key="idx" :src="img"
            :preview-src-list="current.images.split(',')" fit="cover" class="thumb" />
        </div>

        <div class="timeline-title">处理进度</div>
        <el-timeline>
          <el-timeline-item v-for="item in progressList" :key="item.progressId"
            :timestamp="item.createTime" placement="top" :type="nodeType(item.node)">
            <div class="node-name">{{ item.nodeName }}</div>
            <div class="node-content">{{ item.content }}</div>
            <div class="node-op" v-if="item.operatorName">操作人：{{ item.operatorName }}</div>
          </el-timeline-item>
        </el-timeline>

        <el-empty v-if="progressList.length === 0" description="暂无进度记录" :image-size="80" />
      </div>
    </el-drawer>

    <!-- 取消报修 -->
    <el-dialog title="取消报修" :visible.sync="cancelOpen" width="420px" append-to-body>
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.cancelReason" type="textarea" :rows="3" placeholder="请说明取消原因" maxlength="200" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCancel">确 定</el-button>
        <el-button @click="cancelOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 评价 -->
    <el-dialog title="服务评价" :visible.sync="evalOpen" width="480px" append-to-body>
      <el-form :model="evalForm" label-width="90px">
        <el-form-item label="综合评分">
          <el-rate v-model="evalForm.score" show-text :texts="['很差', '较差', '一般', '满意', '非常满意']" />
        </el-form-item>
        <el-form-item label="响应速度">
          <el-rate v-model="evalForm.speedScore" />
        </el-form-item>
        <el-form-item label="维修质量">
          <el-rate v-model="evalForm.qualityScore" />
        </el-form-item>
        <el-form-item label="服务态度">
          <el-rate v-model="evalForm.attitudeScore" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="evalForm.content" type="textarea" :rows="3" placeholder="说说本次维修服务的感受" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEvaluate">提 交</el-button>
        <el-button @click="evalOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrder, getOrder, getProgress, addOrder, updateOrder, cancelOrder } from "@/api/repair/order";
import { addEvaluation } from "@/api/repair/evaluation";

export default {
  name: "RepairOrder",
  dicts: ["repair_type", "repair_status", "repair_urgency"],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      orderList: [],
      quickStatus: "",
      daterangeCreateTime: [],
      title: "",
      open: false,
      detailOpen: false,
      cancelOpen: false,
      evalOpen: false,
      current: null,
      progressList: [],
      cancelForm: { orderId: null, cancelReason: "" },
      evalForm: { orderId: null, score: 5, speedScore: 5, qualityScore: 5, attitudeScore: 5, content: "" },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        repairType: null,
        status: null,
        params: {}
      },
      form: {},
      rules: {
        repairType: [{ required: true, message: "请选择报修类型", trigger: "change" }],
        title: [{ required: true, message: "报修标题不能为空", trigger: "blur" }],
        address: [{ required: true, message: "报修地址不能为空", trigger: "blur" }],
        contactName: [{ required: true, message: "联系人不能为空", trigger: "blur" }],
        contactPhone: [
          { required: true, message: "联系电话不能为空", trigger: "blur" },
          { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
        ],
        urgency: [{ required: true, message: "请选择紧急程度", trigger: "change" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (this.daterangeCreateTime && this.daterangeCreateTime.length) {
        this.queryParams.params["beginTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endTime"] = this.daterangeCreateTime[1];
      }
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    rowClassName({ row }) {
      return row.isOvertime === 1 ? "overtime-row" : "";
    },
    nodeType(node) {
      const map = { submit: "primary", dispatch: "warning", accept: "", repair: "", finish: "success", rate: "success", cancel: "info", return: "danger" };
      return map[node] || "";
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
      this.daterangeCreateTime = [];
      this.quickStatus = "";
      this.resetForm("queryForm");
      this.queryParams.status = null;
      this.handleQuery();
    },
    reset() {
      this.form = {
        orderId: null,
        repairType: "water",
        title: null,
        address: null,
        contactName: null,
        contactPhone: null,
        urgency: "normal",
        description: null,
        images: null
      };
      this.resetForm("form");
    },
    handleAdd() {
      this.reset();
      this.title = "我要报修";
      this.open = true;
    },
    handleUpdate(row) {
      getOrder(row.orderId).then(response => {
        this.form = response.data;
        this.title = "修改报修";
        this.open = true;
      });
    },
    handleDetail(row) {
      getOrder(row.orderId).then(response => {
        this.current = response.data;
        this.detailOpen = true;
        getProgress(row.orderId).then(res => {
          this.progressList = res.data || [];
        });
      });
    },
    handleCancel(row) {
      this.cancelForm = { orderId: row.orderId, cancelReason: "" };
      this.cancelOpen = true;
    },
    submitCancel() {
      cancelOrder(this.cancelForm.orderId, { cancelReason: this.cancelForm.cancelReason }).then(() => {
        this.$modal.msgSuccess("已取消该报修");
        this.cancelOpen = false;
        this.getList();
      });
    },
    handleEvaluate(row) {
      this.evalForm = { orderId: row.orderId, score: 5, speedScore: 5, qualityScore: 5, attitudeScore: 5, content: "" };
      this.evalOpen = true;
    },
    submitEvaluate() {
      addEvaluation(this.evalForm).then(() => {
        this.$modal.msgSuccess("评价成功，感谢您的反馈");
        this.evalOpen = false;
        this.getList();
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) return;
        if (this.form.orderId != null) {
          updateOrder(this.form).then(() => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        } else {
          addOrder(this.form).then(() => {
            this.$modal.msgSuccess("报修提交成功，请等待社区派单");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    handleExport() {
      this.queryParams.params = {};
      if (this.daterangeCreateTime && this.daterangeCreateTime.length) {
        this.queryParams.params["beginTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endTime"] = this.daterangeCreateTime[1];
      }
      this.download("/repair/order/export", { ...this.queryParams }, `我的报修_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.urgency-wrap {
  position: relative;
}
.urgency-wrap ::v-deep .el-input__inner {
  padding-right: 54px;
}
.urgency-help {
  position: absolute;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
  font-size: 14px;
  cursor: help;
  z-index: 3;
  transition: color 0.2s;
}
.urgency-help:hover {
  color: #409eff;
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
  color: #303133;
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
.img-box {
  margin-top: 12px;
}
.thumb {
  width: 88px;
  height: 88px;
  margin-right: 8px;
  border-radius: 4px;
}
</style>

<style>
.overtime-row {
  background-color: #fef0f0 !important;
}
</style>
