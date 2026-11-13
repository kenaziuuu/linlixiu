<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="工号" prop="workerNo">
        <el-input v-model="queryParams.workerNo" placeholder="请输入工号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="workerName">
        <el-input v-model="queryParams.workerName" placeholder="请输入姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="技能" prop="skills">
        <el-select v-model="queryParams.skills" placeholder="请选择技能" clearable>
          <el-option v-for="dict in dict.type.repair_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="在岗" value="0" />
          <el-option label="休息" value="1" />
          <el-option label="离职" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['repair:worker:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['repair:worker:list']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="workerList">
      <el-table-column label="工号" align="center" prop="workerNo" width="110" />
      <el-table-column label="姓名" align="center" prop="workerName" width="100" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="技能类型" align="center" prop="skills">
        <template slot-scope="scope">
          <el-tag v-for="s in splitSkills(scope.row.skills)" :key="s" size="mini" style="margin-right:4px">
            {{ typeLabel(s) }}
          </el-tag>
          <span v-if="!scope.row.skills">-</span>
        </template>
      </el-table-column>
      <el-table-column label="在岗状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="statusTag(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="累计接单" align="center" prop="totalCount" width="90" />
      <el-table-column label="完成数" align="center" prop="finishCount" width="80" />
      <el-table-column label="平均评分" align="center" prop="avgScore" width="90">
        <template slot-scope="scope">
          <span class="score">{{ scope.row.avgScore || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['repair:worker:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['repair:worker:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="工号" prop="workerNo">
              <el-input v-model="form.workerNo" placeholder="如 W0001" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="workerName">
              <el-input v-model="form.workerName" placeholder="请输入姓名" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="在岗状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="在岗" value="0" />
                <el-option label="休息" value="1" />
                <el-option label="离职" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联账号" prop="userId">
          <el-input v-model="form.userId" placeholder="填写维修工对应的系统用户ID，用于接收派单" />
          <div class="tip">维修工的报修任务按该用户 ID 归属，须与“用户管理”中维修工账号的 ID 一致</div>
        </el-form-item>
        <el-form-item label="技能类型" prop="skills">
          <el-select v-model="skillList" multiple placeholder="请选择技能" style="width: 100%">
            <el-option v-for="dict in dict.type.repair_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="200" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listWorker, getWorker, addWorker, updateWorker, delWorker } from "@/api/repair/worker";

export default {
  name: "RepairWorker",
  dicts: ["repair_type"],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      workerList: [],
      title: "",
      open: false,
      skillList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        workerNo: null,
        workerName: null,
        skills: null,
        status: null
      },
      form: {},
      rules: {
        workerName: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }],
        userId: [{ required: true, message: "关联账号不能为空", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listWorker(this.queryParams).then(response => {
        this.workerList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    splitSkills(skills) {
      return skills ? skills.split(",") : [];
    },
    typeLabel(value) {
      const item = (this.dict.type.repair_type || []).find(d => d.value === value);
      return item ? item.label : value;
    },
    statusText(status) {
      return { "0": "在岗", "1": "休息", "2": "离职" }[status] || "-";
    },
    statusTag(status) {
      return { "0": "success", "1": "warning", "2": "info" }[status] || "info";
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    reset() {
      this.form = {
        workerId: null,
        userId: null,
        workerNo: null,
        workerName: null,
        phone: null,
        skills: null,
        status: "0",
        remark: null
      };
      this.skillList = [];
      this.resetForm("form");
    },
    handleAdd() {
      this.reset();
      this.title = "新增维修工";
      this.open = true;
    },
    handleUpdate(row) {
      getWorker(row.workerId).then(response => {
        this.form = response.data;
        this.skillList = row.skills ? row.skills.split(",") : [];
        this.title = "修改维修工";
        this.open = true;
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) return;
        this.form.skills = this.skillList.join(",");
        if (this.form.workerId != null) {
          updateWorker(this.form).then(() => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        } else {
          addWorker(this.form).then(() => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除维修工「' + row.workerName + '」吗？').then(() => {
        return delWorker(row.workerId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    handleExport() {
      this.download("/repair/worker/export", { ...this.queryParams }, `维修工_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.score {
  color: #e6a23c;
  font-weight: 600;
}
.tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
</style>
