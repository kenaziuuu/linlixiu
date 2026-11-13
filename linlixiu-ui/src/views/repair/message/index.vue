<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="消息标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="消息类型" prop="msgType">
        <el-select v-model="queryParams.msgType" placeholder="请选择类型" clearable>
          <el-option v-for="dict in dict.type.repair_msg_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="读取状态" prop="isRead">
        <el-select v-model="queryParams.isRead" placeholder="请选择状态" clearable>
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-check" size="mini" @click="handleReadAll"
          v-hasPermi="['repair:message:list']">全部已读</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['repair:message:remove']">删除</el-button>
      </el-col>
      <el-col :span="6">
        <span class="unread-tip">当前未读：<b>{{ unread }}</b> 条</span>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="消息标题" align="left" prop="title" width="220">
        <template slot-scope="scope">
          <span :class="{ unread: scope.row.isRead === 0 }">{{ scope.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="消息内容" align="left" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="msgType" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.repair_msg_type" :value="scope.row.msgType" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="isRead" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isRead === 1 ? 'info' : 'danger'" size="mini">
            {{ scope.row.isRead === 1 ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" align="center" prop="createTime" width="150" />
      <el-table-column label="操作" align="center" width="90" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-if="scope.row.isRead === 0" size="mini" type="text" icon="el-icon-check"
            @click="handleRead(scope.row)">标记已读</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />
  </div>
</template>

<script>
import { listMessage, unreadCount, readMessage, readAllMessage, delMessage } from "@/api/repair/message";

export default {
  name: "RepairMessage",
  dicts: ["repair_msg_type"],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      unread: 0,
      messageList: [],
      ids: [],
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        msgType: null,
        isRead: null
      }
    };
  },
  created() {
    this.getList();
    this.getUnread();
  },
  methods: {
    getList() {
      this.loading = true;
      listMessage(this.queryParams).then(response => {
        this.messageList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getUnread() {
      unreadCount().then(res => {
        this.unread = res.data || 0;
      });
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.messageId);
      this.multiple = !selection.length;
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleRead(row) {
      readMessage(row.messageId).then(() => {
        this.getList();
        this.getUnread();
      });
    },
    handleReadAll() {
      readAllMessage().then(() => {
        this.$modal.msgSuccess("已全部标记为已读");
        this.getList();
        this.getUnread();
      });
    },
    handleDelete() {
      this.$modal.confirm('确认删除选中的 ' + this.ids.length + ' 条消息吗？').then(() => {
        return delMessage(this.ids.join(","));
      }).then(() => {
        this.getList();
        this.getUnread();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.unread {
  font-weight: 600;
  color: #303133;
}
.unread-tip {
  color: #909399;
  font-size: 13px;
  line-height: 28px;
}
.unread-tip b {
  color: #f56c6c;
}
</style>
