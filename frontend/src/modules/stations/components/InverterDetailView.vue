<template>
  <div class="inverter-detail-view">
    <div v-if="!data" class="inverter-detail-view__loading">
      <i class="el-icon-loading" /> 加载中...
    </div>
    <template v-else>
      <!-- KPI row -->
      <div class="inverter-detail-view__kpis">
        <div class="inverter-detail-view__kpi" v-for="kpi in kpiList" :key="kpi.key">
          <span class="inverter-detail-view__kpi-label">{{ kpi.label }}</span>
          <span class="inverter-detail-view__kpi-value">{{ kpi.value }} <small>{{ kpi.unit }}</small></span>
        </div>
      </div>

      <!-- Topology diagram -->
      <div v-if="data.topology" class="inverter-detail-view__topology">
        <div class="inverter-detail-view__section-title">拓扑结构</div>
        <div class="inverter-detail-view__topo-diagram">
          <!-- Inverter node (center) -->
          <div class="inverter-detail-view__topo-inverter">
            <div class="inverter-detail-view__topo-inv-box">
              <i class="el-icon-cpu" />
              <span>{{ data.topology.inverterName }}</span>
              <small>{{ data.topology.ratedPowerKw }} kW</small>
            </div>
            <div class="inverter-detail-view__topo-meta">
              {{ data.topology.stringCount }} 组串 / {{ data.topology.totalPanels }} 组件
            </div>
          </div>
          <!-- String nodes -->
          <div class="inverter-detail-view__topo-strings">
            <div
              v-for="str in data.topology.strings"
              :key="str.id"
              class="inverter-detail-view__topo-string"
              :class="{ 'is-fault': str.status === 'fault' }"
            >
              <div class="inverter-detail-view__topo-string-header">
                <span class="inverter-detail-view__topo-string-dot" :class="'is-' + str.status" />
                <span>{{ str.name }}</span>
              </div>
              <div class="inverter-detail-view__topo-string-data">
                <span>{{ str.currentA }} A</span>
                <span>{{ str.voltageV }} V</span>
                <span>{{ str.panelCount }} 块</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Device Alarms -->
      <div class="inverter-detail-view__alarms">
        <div class="inverter-detail-view__section-title">
          设备告警
          <el-tag v-if="data.alarms && data.alarms.length" size="mini" type="danger" effect="plain" style="margin-left: 8px;">
            {{ data.alarms.length }}
          </el-tag>
          <el-tag v-else size="mini" type="success" effect="plain" style="margin-left: 8px;">无告警</el-tag>
        </div>
        <el-table v-if="data.alarms && data.alarms.length" :data="data.alarms" size="mini" stripe style="width: 100%;">
          <el-table-column prop="time" label="时间" width="150" />
          <el-table-column prop="type" label="类型" width="130" />
          <el-table-column prop="level" label="级别" width="80" align="center">
            <template slot-scope="{ row }">
              <el-tag :type="row.level === '严重' ? 'danger' : row.level === '故障' ? 'danger' : 'warning'" size="mini">{{ row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template slot-scope="{ row }">
              <el-tag :type="row.status === '未处理' ? 'danger' : 'info'" size="mini">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="inverter-detail-view__no-alarm">当前设备无告警记录</div>
      </div>

      <!-- Device Info -->
      <div class="inverter-detail-view__device-info">
        <div class="inverter-detail-view__section-title">设备信息</div>
        <div class="inverter-detail-view__info-grid">
          <div class="inverter-detail-view__info-item" v-for="item in deviceInfoList" :key="item.label">
            <span class="inverter-detail-view__info-label">{{ item.label }}</span>
            <span class="inverter-detail-view__info-value">{{ item.value }}</span>
          </div>
        </div>
      </div>

      <!-- Power Curve -->
      <div class="inverter-detail-view__curve">
        <div class="inverter-detail-view__section-title">功率曲线</div>
        <div ref="chart" class="inverter-detail-view__chart" />
      </div>
    </template>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'InverterDetailView',
  props: {
    data: { type: Object, default: null }
  },
  computed: {
    kpiList() {
      if (!this.data) return []
      return [
        { key: 'rated', label: '额定功率', value: this.data.ratedPowerKw, unit: 'kW' },
        { key: 'realtime', label: '实时功率', value: this.data.realtimePowerKw, unit: 'kW' },
        { key: 'energy', label: '日发电量', value: this.data.dailyEnergyMwh, unit: 'MWh' },
        { key: 'eff', label: '转换效率', value: this.data.efficiency, unit: '%' }
      ]
    },
    deviceInfoList() {
      if (!this.data || !this.data.deviceInfo) return []
      var d = this.data.deviceInfo
      return [
        { label: '设备型号', value: d.model },
        { label: '制造商', value: d.manufacturer },
        { label: '序列号', value: d.sn },
        { label: '固件版本', value: d.firmwareVersion },
        { label: '安装日期', value: d.installDate },
        { label: 'MPPT通道数', value: d.mpptChannels },
        { label: '直流输入电压', value: d.dcInputVoltage },
        { label: '交流输出电压', value: d.acOutputVoltage },
        { label: '电网频率', value: d.gridFrequency },
        { label: '模块温度', value: d.moduleTemperature },
        { label: '环境温度', value: d.ambientTemperature }
      ]
    }
  },
  watch: {
    data: { handler: 'renderChart', deep: true }
  },
  mounted() {
    this.chart = null
    this.renderChart()
    this._resizeHandler = () => { if (this.chart) this.chart.resize() }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.chart) { this.chart.dispose(); this.chart = null }
  },
  methods: {
    renderChart() {
      if (!this.$refs.chart || !this.data || !this.data.powerCurve) return
      if (!this.chart) { this.chart = echarts.init(this.$refs.chart) }
      var d = this.data.powerCurve
      this.chart.setOption({
        backgroundColor: 'transparent',
        grid: { left: 60, right: 30, top: 40, bottom: 60 },
        legend: { data: d.series.map(function (s) { return s.name }), bottom: 8, textStyle: { color: 'rgba(255,255,255,0.65)', fontSize: 12 }, icon: 'roundRect', itemWidth: 14, itemHeight: 3 },
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(6,20,48,0.95)', borderColor: 'rgba(255,255,255,0.1)', textStyle: { color: '#fff', fontSize: 12 } },
        xAxis: { type: 'category', data: d.times, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }, axisLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 11 }, splitLine: { show: false } },
        yAxis: { type: 'value', name: 'kW', nameTextStyle: { color: 'rgba(255,255,255,0.5)', fontSize: 11 }, axisLine: { show: false }, axisLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 11 }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } },
        series: d.series.map(function (s) {
          return { name: s.name, type: 'line', data: s.data, smooth: true, symbol: 'none', lineStyle: { width: 2 }, areaStyle: { opacity: 0.12 }, itemStyle: { color: '#06d6a0' } }
        })
      }, true)
    }
  }
}
</script>

<style lang="less" scoped>
.inverter-detail-view {
  width: 100%;

  &__loading {
    padding: 60px 0;
    text-align: center;
    color: rgba(255, 255, 255, 0.55);
    font-size: 14px;
    i { margin-right: 8px; }
  }

  &__kpis {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
    margin-bottom: 16px;
  }

  &__kpi {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 14px 16px;
    border-radius: 4px;
    border: 1px solid rgba(255, 255, 255, 0.06);
    background: rgba(255, 255, 255, 0.03);
  }

  &__kpi-label { font-size: 11px; color: rgba(255, 255, 255, 0.55); }
  &__kpi-value {
    font-size: 20px; font-weight: 600; color: rgba(255, 255, 255, 0.92);
    small { font-size: 11px; font-weight: 400; color: rgba(255, 255, 255, 0.5); margin-left: 3px; }
  }

  &__section-title {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.88);
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  }

  // ---- Topology ----
  &__topology {
    margin-bottom: 20px;
    padding: 16px 18px;
    border-radius: 4px;
    border: 1px solid rgba(255, 255, 255, 0.06);
    background: rgba(255, 255, 255, 0.02);
  }

  &__topo-diagram {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  &__topo-inverter {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__topo-inv-box {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    border-radius: 6px;
    background: linear-gradient(135deg, rgba(6, 162, 153, 0.2), rgba(24, 144, 255, 0.15));
    border: 1px solid rgba(6, 162, 153, 0.4);
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    i { font-size: 20px; color: #06a299; }
    small { font-size: 12px; font-weight: 400; color: rgba(255, 255, 255, 0.6); }
  }

  &__topo-meta {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
  }

  &__topo-strings {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  &__topo-string {
    padding: 10px 12px;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.06);
    transition: border-color 0.2s;

    &.is-fault {
      border-color: rgba(245, 108, 108, 0.5);
      background: rgba(245, 108, 108, 0.06);
    }
  }

  &__topo-string-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    font-weight: 500;
    color: rgba(255, 255, 255, 0.85);
    margin-bottom: 6px;
  }

  &__topo-string-dot {
    display: inline-block;
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: #67c23a;
    &.is-fault { background: #f56c6c; }
    &.is-offline { background: #909399; }
  }

  &__topo-string-data {
    display: flex;
    gap: 10px;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.55);
  }

  // ---- Alarms ----
  &__alarms {
    margin-bottom: 20px;
  }

  &__no-alarm {
    padding: 30px 0;
    text-align: center;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.04);
    border-radius: 4px;
  }

  /deep/ .el-table {
    background-color: transparent;
    color: rgba(255, 255, 255, 0.85);
    &::before { background-color: rgba(255, 255, 255, 0.06); }
    th { background-color: rgba(255, 255, 255, 0.04); color: rgba(255, 255, 255, 0.58); border-bottom: 1px solid rgba(255, 255, 255, 0.06); font-size: 12px; }
    tr { background-color: transparent; }
    td { border-bottom: 1px solid rgba(255, 255, 255, 0.04); font-size: 12px; }
    .el-table__row--striped td { background-color: rgba(255, 255, 255, 0.02); }
    .el-table__row:hover > td { background-color: rgba(24, 144, 255, 0.08); }
    .el-table__empty-block { background-color: transparent; }
  }

  /deep/ .el-tag {
    border-radius: 3px; font-size: 11px; height: 22px; line-height: 20px; padding: 0 8px; background: transparent;
    &--success { color: #67c23a; border-color: rgba(103, 194, 58, 0.4); }
    &--warning { color: #e6a23c; border-color: rgba(230, 162, 60, 0.4); }
    &--danger { color: #f56c6c; border-color: rgba(245, 108, 108, 0.4); }
    &--info { color: rgba(255, 255, 255, 0.55); border-color: rgba(255, 255, 255, 0.2); }
  }

  // ---- Device Info ----
  &__device-info {
    margin-bottom: 20px;
  }

  &__info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0;
    border: 1px solid rgba(255, 255, 255, 0.06);
    border-radius: 4px;
    overflow: hidden;
  }

  &__info-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.04);
    &:nth-child(odd) { border-right: 1px solid rgba(255, 255, 255, 0.04); }
  }

  &__info-label { font-size: 12px; color: rgba(255, 255, 255, 0.55); }
  &__info-value { font-size: 13px; font-weight: 500; color: rgba(255, 255, 255, 0.88); }

  // ---- Power Curve ----
  &__curve {
    margin-bottom: 16px;
  }

  &__chart {
    width: 100%;
    height: 350px;
  }
}
</style>
