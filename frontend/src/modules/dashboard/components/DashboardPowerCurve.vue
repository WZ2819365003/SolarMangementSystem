<template>
  <app-section-card
    class="dashboard-power-curve"
    data-testid="dashboard-power-curve"
    title="计划 vs 实际功率曲线"
    :subtitle="curveData.stationName || '当前关注电站'"
  >
    <template #extra>
      <div class="dashboard-power-curve__actions">
        <el-radio-group
          size="mini"
          :value="activePreset"
          @input="handlePresetChange"
        >
          <el-radio-button
            v-for="item in curveData.dateTabs || []"
            :key="item.value"
            :label="item.value"
          >
            {{ item.label }}
          </el-radio-button>
        </el-radio-group>
        <el-date-picker
          :value="curveData.currentDate"
          size="mini"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择日期"
          @change="handleCustomDateChange"
        />
      </div>
    </template>

    <div class="dashboard-power-curve__summary">
      <span>峰值功率 {{ curveData.peakPowerKw }} kW</span>
      <span>偏差率 {{ curveData.deviationRate }}%</span>
    </div>

    <div ref="chart" class="dashboard-power-curve__chart" />
  </app-section-card>
</template>

<script>
import echarts from '@/shared/plugins/echarts'
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'DashboardPowerCurve',
  components: {
    AppSectionCard
  },
  props: {
    curveData: {
      type: Object,
      default: () => ({
        dateTabs: []
      })
    }
  },
  data() {
    return {
      chartInstance: null
    }
  },
  computed: {
    activePreset() {
      const tab = (this.curveData.dateTabs || []).find(
        item => item.value === this.curveData.currentDate
      )
      return tab ? tab.value : ''
    }
  },
  watch: {
    curveData: {
      deep: true,
      handler() {
        this.$nextTick(() => {
          this.renderChart()
        })
      }
    }
  },
  mounted() {
    this.chartInstance = echarts.init(this.$refs.chart)
    window.addEventListener('resize', this.handleResize)
    this.renderChart()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
  },
  methods: {
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    },
    handlePresetChange(value) {
      this.$emit('date-change', value)
    },
    handleCustomDateChange(value) {
      if (value) {
        this.$emit('date-change', value)
      }
    },
    buildAdjustableSeries() {
      const ts = this.curveData.adjustableTimeSeries
      if (!ts || !ts.length) {
        return []
      }
      return [
        {
          name: '可上调容量',
          type: 'line',
          smooth: true,
          showSymbol: false,
          lineStyle: { width: 1.5, type: 'dashed', color: 'rgba(0,181,120,0.7)' },
          areaStyle: { color: 'rgba(0,181,120,0.10)' },
          data: ts.map(item => item.maxUpCapacity)
        },
        {
          name: '可下调容量',
          type: 'line',
          smooth: true,
          showSymbol: false,
          lineStyle: { width: 1.5, type: 'dashed', color: 'rgba(245,155,35,0.7)' },
          areaStyle: { color: 'rgba(245,155,35,0.10)' },
          data: ts.map(item => item.maxDownCapacity)
        }
      ]
    },
    renderChart() {
      if (!this.chartInstance || !this.curveData.actual) {
        return
      }

      const xAxisData = this.curveData.actual.map(item => item.time.slice(11, 16))
      this.chartInstance.setOption(
        {
          backgroundColor: 'transparent',
          animationDuration: 400,
          tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(8, 24, 54, 0.92)',
            borderColor: 'rgba(255, 255, 255, 0.08)',
            textStyle: {
              color: '#f4f7fb'
            }
          },
          legend: {
            top: 0,
            right: 0,
            itemWidth: 10,
            itemHeight: 10,
            textStyle: {
              color: 'rgba(255,255,255,0.72)'
            },
            data: ['实际功率', '日前计划', '超短期预测', '辐照度', '可上调容量', '可下调容量']
          },
          grid: {
            left: 16,
            right: 18,
            top: 42,
            bottom: 44,
            containLabel: true
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: xAxisData,
            axisLine: {
              lineStyle: {
                color: 'rgba(255,255,255,0.14)'
              }
            },
            axisLabel: {
              color: 'rgba(255,255,255,0.58)'
            }
          },
          yAxis: [
            {
              type: 'value',
              name: '功率(kW)',
              nameTextStyle: {
                color: 'rgba(255,255,255,0.58)'
              },
              splitLine: {
                lineStyle: {
                  color: 'rgba(255,255,255,0.06)'
                }
              },
              axisLabel: {
                color: 'rgba(255,255,255,0.58)'
              }
            },
            {
              type: 'value',
              name: '辐照度',
              nameTextStyle: {
                color: 'rgba(255,255,255,0.58)'
              },
              splitLine: {
                show: false
              },
              axisLabel: {
                color: 'rgba(255,255,255,0.42)'
              }
            }
          ],
          dataZoom: [
            {
              type: 'inside'
            },
            {
              type: 'slider',
              height: 16,
              bottom: 10,
              borderColor: 'transparent',
              backgroundColor: 'rgba(255,255,255,0.06)',
              fillerColor: 'rgba(26,141,255,0.18)'
            }
          ],
          series: [
            {
              name: '实际功率',
              type: 'line',
              smooth: true,
              showSymbol: false,
              lineStyle: {
                width: 3,
                color: '#1a8dff'
              },
              areaStyle: {
                color: 'rgba(26,141,255,0.14)'
              },
              data: this.curveData.actual.map(item => item.value)
            },
            {
              name: '日前计划',
              type: 'line',
              smooth: true,
              showSymbol: false,
              lineStyle: {
                width: 2,
                type: 'dashed',
                color: '#e6a23c'
              },
              data: this.curveData.plan.map(item => item.value)
            },
            {
              name: '超短期预测',
              type: 'line',
              smooth: true,
              showSymbol: false,
              lineStyle: {
                width: 2,
                type: 'dotted',
                color: '#67c23a'
              },
              data: this.curveData.forecast.map(item => item.value)
            },
            {
              name: '辐照度',
              type: 'line',
              smooth: true,
              yAxisIndex: 1,
              showSymbol: false,
              lineStyle: {
                width: 1.5,
                color: 'rgba(250,200,50,0.7)'
              },
              areaStyle: {
                color: 'rgba(250,200,50,0.14)'
              },
              data: this.curveData.irradiance.map(item => item.value)
            }
          ].concat(this.buildAdjustableSeries())
        },
        true
      )
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-power-curve__actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.dashboard-power-curve__actions /deep/ .el-input__inner {
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: var(--pvms-text-primary);
}

.dashboard-power-curve__summary {
  display: flex;
  gap: 18px;
  margin-bottom: 12px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.dashboard-power-curve__chart {
  height: 320px;
}
</style>
