<template>
  <app-section-card
    data-testid="station-power-curve"
    title="出力预测 vs 实际"
    :subtitle="curveData.resourceUnitName || '当前资源单元'"
  >
    <template #extra>
      <el-date-picker
        :value="curveData.currentDate"
        size="mini"
        type="date"
        value-format="yyyy-MM-dd"
        placeholder="选择日期"
        @change="handleDateChange"
      />
    </template>

    <div class="station-power-curve__summary">
      <span>峰值出力 {{ curveData.peakPowerMw || 0 }} MW</span>
      <span>预测偏差 {{ curveData.deviationRate || 0 }}%</span>
    </div>

    <div ref="chart" class="station-power-curve__chart" />
  </app-section-card>
</template>

<script>
import echarts from '@/shared/plugins/echarts'
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'StationOverviewPowerCurve',
  components: {
    AppSectionCard
  },
  props: {
    curveData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      chartInstance: null
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
    handleDateChange(value) {
      if (value) {
        this.$emit('date-change', value)
      }
    },
    renderChart() {
      if (!this.chartInstance || !Array.isArray(this.curveData.actual)) {
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
            data: ['实际出力', '预测出力', '调度基线', '辐照度']
          },
          grid: {
            left: 16,
            right: 18,
            top: 42,
            bottom: 28,
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
              name: '出力(MW)',
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
              splitLine: {
                show: false
              },
              axisLabel: {
                color: 'rgba(255,255,255,0.42)'
              }
            }
          ],
          series: [
            {
              name: '实际出力',
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
              name: '预测出力',
              type: 'line',
              smooth: true,
              showSymbol: false,
              lineStyle: {
                width: 2,
                type: 'dashed',
                color: '#67c23a'
              },
              data: this.curveData.forecast.map(item => item.value)
            },
            {
              name: '调度基线',
              type: 'line',
              smooth: true,
              showSymbol: false,
              lineStyle: {
                width: 2,
                type: 'dotted',
                color: '#e6a23c'
              },
              data: this.curveData.baseline.map(item => item.value)
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
          ]
        },
        true
      )
    }
  }
}
</script>

<style lang="less" scoped>
.station-power-curve__summary {
  display: flex;
  gap: 18px;
  margin-bottom: 12px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.station-power-curve__chart {
  height: 340px;
}
</style>
