<template>
  <div class="app-container">
    <!-- 概览 -->
    <el-row :gutter="12" class="mb16">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">累计报修</div>
          <div class="stat-value">{{ overview.total || 0 }}</div>
          <div class="stat-sub">今日新增 {{ overview.todayCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">待处理（待派单 + 待接单 + 维修中）</div>
          <div class="stat-value warning">{{ pendingTotal }}</div>
          <div class="stat-sub">待派单 {{ overview.pendingCount || 0 }} · 待接单 {{ overview.assignedCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">完成率</div>
          <div class="stat-value success">{{ overview.finishRate || 0 }}%</div>
          <div class="stat-sub">已完成 {{ (overview.completedCount || 0) + (overview.ratedCount || 0) }} 单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">超时工单 / 平均评分</div>
          <div class="stat-value danger">{{ overview.overtimeCount || 0 }}</div>
          <div class="stat-sub">平均 {{ score.avgScore || 0 }} 分 · 好评 {{ score.goodCount || 0 }} 条</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mb16">
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>报修类型分布</span>
          </div>
          <div ref="typeChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>近 7 天报修趋势</span>
            <el-radio-group v-model="trendDays" size="mini" @change="loadTrend">
              <el-radio-button :label="7">7 天</el-radio-button>
              <el-radio-button :label="14">14 天</el-radio-button>
              <el-radio-button :label="30">30 天</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="card-header"><span>维修工绩效排行（前 5 名）</span></div>
          <el-table :data="workerRank" size="small">
            <el-table-column label="排名" align="center" width="70">
              <template slot-scope="scope">
                <el-tag :type="scope.$index === 0 ? 'danger' : (scope.$index === 1 ? 'warning' : 'info')" size="mini">
                  {{ scope.$index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="工号" align="center" prop="workerNo" width="90" />
            <el-table-column label="姓名" align="center" prop="workerName" width="90" />
            <el-table-column label="接单" align="center" prop="orderCount" width="70" />
            <el-table-column label="完成" align="center" prop="finishCount" width="70" />
            <el-table-column label="平均评分" align="center" prop="avgScore">
              <template slot-scope="scope">
                <span class="score">{{ scope.row.avgScore }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="card-header"><span>工单状态分布</span></div>
          <div ref="statusChart" class="chart-sm"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from "echarts";
import { getOverview, getByType, getByStatus, getTrend, getWorkerRank, getScoreSummary } from "@/api/repair/stats";

export default {
  name: "RepairStats",
  dicts: ["repair_type", "repair_status"],
  data() {
    return {
      overview: {},
      score: {},
      workerRank: [],
      trendDays: 7,
      typeChart: null,
      trendChart: null,
      statusChart: null
    };
  },
  computed: {
    pendingTotal() {
      const o = this.overview;
      return (o.pendingCount || 0) + (o.assignedCount || 0) + (o.repairingCount || 0);
    }
  },
  mounted() {
    this.loadAll();
    window.addEventListener("resize", this.resizeCharts);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.resizeCharts);
    [this.typeChart, this.trendChart, this.statusChart].forEach(c => c && c.dispose());
  },
  methods: {
    loadAll() {
      getOverview().then(res => {
        this.overview = res.data || {};
      });
      getScoreSummary().then(res => {
        this.score = res.data || {};
      });
      getWorkerRank(5).then(res => {
        this.workerRank = res.data || [];
      });
      this.loadType();
      this.loadTrend();
      this.loadStatus();
    },
    typeLabel(name) {
      const item = (this.dict.type.repair_type || []).find(d => d.value === name);
      return item ? item.label : name;
    },
    statusLabel(name) {
      const item = (this.dict.type.repair_status || []).find(d => d.value === name);
      return item ? item.label : name;
    },
    loadType() {
      getByType().then(res => {
        const data = (res.data || []).map(i => ({ name: this.typeLabel(i.name), value: i.value }));
        if (!this.typeChart) {
          this.typeChart = echarts.init(this.$refs.typeChart);
        }
        this.typeChart.setOption({
          tooltip: { trigger: "item", formatter: "{b}: {c} 单 ({d}%)" },
          legend: { bottom: 0, icon: "circle" },
          color: ["#409eff", "#67c23a", "#e6a23c", "#f56c6c", "#909399", "#8e79c9"],
          series: [{
            name: "报修类型",
            type: "pie",
            radius: ["38%", "62%"],
            center: ["50%", "45%"],
            avoidLabelOverlap: true,
            label: { formatter: "{b}\n{c} 单" },
            data: data
          }]
        }, true);
      });
    },
    loadTrend() {
      getTrend(this.trendDays).then(res => {
        const list = res.data || [];
        if (!this.trendChart) {
          this.trendChart = echarts.init(this.$refs.trendChart);
        }
        this.trendChart.setOption({
          tooltip: { trigger: "axis" },
          grid: { left: 40, right: 20, top: 30, bottom: 40 },
          xAxis: { type: "category", data: list.map(i => i.name), axisLine: { lineStyle: { color: "#dcdfe6" } } },
          yAxis: { type: "value", minInterval: 1, splitLine: { lineStyle: { type: "dashed" } } },
          series: [{
            name: "报修量",
            type: "bar",
            barWidth: "45%",
            itemStyle: { color: "#409eff", borderRadius: [4, 4, 0, 0] },
            data: list.map(i => i.value)
          }]
        }, true);
      });
    },
    loadStatus() {
      getByStatus().then(res => {
        const list = (res.data || []).map(i => ({ name: this.statusLabel(i.name), value: i.value }));
        if (!this.statusChart) {
          this.statusChart = echarts.init(this.$refs.statusChart);
        }
        this.statusChart.setOption({
          tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
          grid: { left: 80, right: 30, top: 20, bottom: 30 },
          xAxis: { type: "value", minInterval: 1, splitLine: { lineStyle: { type: "dashed" } } },
          yAxis: { type: "category", data: list.map(i => i.name) },
          series: [{
            type: "bar",
            barWidth: "50%",
            itemStyle: { color: "#67c23a", borderRadius: [0, 4, 4, 0] },
            data: list.map(i => i.value)
          }]
        }, true);
      });
    },
    resizeCharts() {
      [this.typeChart, this.trendChart, this.statusChart].forEach(c => c && c.resize());
    }
  }
};
</script>

<style scoped>
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
  font-size: 26px;
  font-weight: 600;
  margin-top: 6px;
  color: #303133;
}
.stat-value.warning {
  color: #e6a23c;
}
.stat-value.success {
  color: #67c23a;
}
.stat-value.danger {
  color: #f56c6c;
}
.stat-sub {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.chart {
  height: 280px;
}
.chart-sm {
  height: 220px;
}
.score {
  color: #e6a23c;
  font-weight: 600;
}
</style>
