<template>
  <div v-loading="loading" class="forecast-accuracy-view" data-testid="forecast-accuracy-view">
    <!-- KPI Cards -->
    <section class="pv-card-grid forecast-accuracy-view__kpis">
      <app-metric-card
        v-for="item in kpiCards"
        :key="item.key"
        :title="item.title"
        :value="item.value"
        :unit="item.unit"
        :icon="item.icon"
        :accent="item.accent"
      />
    </section>

    <!-- Top row: Trend + Distribution -->
    <div class="forecast-accuracy-view__grid">
      <app-section-card title="预测精度趋势（近30日）">
        <div ref="trendChart" class="forecast-accuracy-view__chart" />
      </app-section-card>

      <app-section-card title="偏差分布">
        <div ref="distChart" class="forecast-accuracy-view__chart" />
      </app-section-card>
    </div>

    <!-- Bottom row: Ranking + Monthly Table -->
    <div class="forecast-accuracy-view__grid">
      <app-section-card title="电站精度排名">
        <div ref="rankChart" class="forecast-accuracy-view__chart" />
      </app-section-card>

      <app-section-card title="月度精度对比">
        <el-table :data="monthlyRows" size="mini" stripe>
          <el-table-column prop="month" label="月份" width="100" />
          <el-table-column prop="mae" label="MAE(MW)" width="110" align="right" />
          <el-table-column prop="rmse" label="RMSE(MW)" width="110" align="right" />
          <el-table-column prop="accuracy" label="精度(%)" width="100" align="right" />
          <el-table-column prop="improvement" label="环比" width="100" align="right">
            <template slot-scope="{ row }">
              <span :style="{ color: row.improvement > 0 ? '#67c23a' : '#f56c6c' }">
                {{ row.improvement > 0 ? '+' : '' }}{{ row.improvement }}%
              </span>
            </template>
          </el-table-column>
        </el-table>
      </app-section-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'

var CHART_TEXT_COLOR = 'rgba(255,255,255,0.65)'
var AXIS_LINE_COLOR = 'rgba(255,255,255,0.15)'
var SPLIT_LINE_COLOR = 'rgba(255,255,255,0.06)'

export default {
  name: 'ForecastAccuracyView',

  components: {
    AppMetricCard,
    AppSectionCard
  },

  props: {
    viewData: {
      type: Object,
      default: function () {
        return {}
      }
    },
    query: {
      type: Object,
      default: function () {
        return {}
      }
    },
    loading: {
      type: Boolean,
      default: false
    }
  },

  data: function () {
    return {
      trendInstance: null,
      distInstance: null,
      rankInstance: null
    }
  },

  computed: {
    kpis: function () {
      return this.viewData.kpis || {}
    },

    kpiCards: function () {
      var kpis = this.kpis
      return [
        { key: 'mae', title: 'MAE', value: kpis.mae, unit: 'MW', icon: 'el-icon-data-line', accent: 'teal' },
        { key: 'rmse', title: 'RMSE', value: kpis.rmse, unit: 'MW', icon: 'el-icon-s-data', accent: 'blue' },
        { key: 'qualified', title: '合格率', value: kpis.qualifiedRate, unit: '%', icon: 'el-icon-circle-check', accent: 'emerald' },
        { key: 'monthly', title: '本月平均精度', value: kpis.monthlyAvgAccuracy, unit: '%', icon: 'el-icon-date', accent: 'orange' }
      ]
    },

    monthlyRows: function () {
      return this.viewData.monthlyTable || []
    }
  },

  watch: {
    viewData: {
      deep: true,
      handler: function () {
        var self = this
        self.$nextTick(function () {
          self.renderTrendChart()
          self.renderDistChart()
          self.renderRankChart()
        })
      }
    }
  },

  mounted: function () {
    this.handleResize = this._handleResize.bind(this)
    window.addEventListener('resize', this.handleResize)
    this.$nextTick(function () {
      this.renderTrendChart()
      this.renderDistChart()
      this.renderRankChart()
    })
  },

  beforeDestroy: function () {
    if (this.trendInstance) {
      this.trendInstance.dispose()
      this.trendInstance = null
    }
    if (this.distInstance) {
      this.distInstance.dispose()
      this.distInstance = null
    }
    if (this.rankInstance) {
      this.rankInstance.dispose()
      this.rankInstance = null
    }
    window.removeEventListener('resize', this.handleResize)
  },

  methods: {
    _handleResize: function () {
      if (this.trendInstance) {
        this.trendInstance.resize()
      }
      if (this.distInstance) {
        this.distInstance.resize()
      }
      if (this.rankInstance) {
        this.rankInstance.resize()
      }
    },

    renderTrendChart: function () {
      var trend = this.viewData.trend
      if (!trend || !this.$refs.trendChart) return

      if (!this.trendInstance) {
        this.trendInstance = echarts.init(this.$refs.trendChart)
      }

      var dates = trend.dates || []
      var dayAheadAccuracy = trend.dayAheadAccuracy || []
      var ultraShortAccuracy = trend.ultraShortAccuracy || []

      var option = {
        backgroundColor: 'transparent',
        textStyle: {
          color: CHART_TEXT_COLOR
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(0,0,0,0.75)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: {
            color: CHART_TEXT_COLOR
          }
        },
        legend: {
          top: 6,
          textStyle: {
            color: CHART_TEXT_COLOR
          }
        },
        grid: {
          top: 60,
          right: 30,
          bottom: 40,
          left: 60
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            color: CHART_TEXT_COLOR,
            interval: 4
          },
          axisLine: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          },
          axisTick: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '%',
          min: 80,
          max: 100,
          nameTextStyle: {
            color: CHART_TEXT_COLOR
          },
          axisLabel: {
            color: CHART_TEXT_COLOR
          },
          axisLine: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          },
          splitLine: {
            lineStyle: {
              color: SPLIT_LINE_COLOR
            }
          }
        },
        series: [
          {
            name: '日前预测精度',
            type: 'line',
            smooth: true,
            data: dayAheadAccuracy,
            itemStyle: { color: '#409EFF' },
            lineStyle: { color: '#409EFF' },
            symbol: 'none',
            markLine: {
              silent: true,
              symbol: 'none',
              lineStyle: {
                type: 'dashed',
                color: '#f56c6c'
              },
              label: {
                color: '#f56c6c',
                formatter: '合格线 90%'
              },
              data: [
                { yAxis: 90 }
              ]
            }
          },
          {
            name: '超短期预测精度',
            type: 'line',
            smooth: true,
            data: ultraShortAccuracy,
            itemStyle: { color: '#36CFC9' },
            lineStyle: { color: '#36CFC9' },
            symbol: 'none'
          }
        ]
      }

      this.trendInstance.setOption(option, true)
    },

    renderDistChart: function () {
      var distribution = this.viewData.distribution
      if (!distribution || !this.$refs.distChart) return

      if (!this.distInstance) {
        this.distInstance = echarts.init(this.$refs.distChart)
      }

      var bins = distribution.bins || []
      var counts = distribution.counts || []

      // Generate normal distribution fit curve
      var maxCount = Math.max.apply(null, counts)
      var sigma = 2
      var mean = 0
      var normalData = []
      for (var i = 0; i < bins.length; i++) {
        var x = -4.5 + i
        var y = maxCount * Math.exp(-0.5 * Math.pow((x - mean) / sigma, 2))
        normalData.push(Math.round(y * 100) / 100)
      }

      var option = {
        backgroundColor: 'transparent',
        textStyle: {
          color: CHART_TEXT_COLOR
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(0,0,0,0.75)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: {
            color: CHART_TEXT_COLOR
          }
        },
        legend: {
          top: 6,
          textStyle: {
            color: CHART_TEXT_COLOR
          }
        },
        grid: {
          top: 60,
          right: 30,
          bottom: 40,
          left: 60
        },
        xAxis: {
          type: 'category',
          data: bins,
          axisLabel: {
            color: CHART_TEXT_COLOR
          },
          axisLine: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          },
          axisTick: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          }
        },
        yAxis: {
          type: 'value',
          nameTextStyle: {
            color: CHART_TEXT_COLOR
          },
          axisLabel: {
            color: CHART_TEXT_COLOR
          },
          axisLine: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          },
          splitLine: {
            lineStyle: {
              color: SPLIT_LINE_COLOR
            }
          }
        },
        series: [
          {
            name: '频次',
            type: 'bar',
            data: counts,
            barWidth: '60%',
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '正态拟合',
            type: 'line',
            smooth: true,
            data: normalData,
            itemStyle: { color: '#f56c6c' },
            lineStyle: {
              color: '#f56c6c',
              width: 2
            },
            symbol: 'none'
          }
        ]
      }

      this.distInstance.setOption(option, true)
    },

    renderRankChart: function () {
      var stationRanking = this.viewData.stationRanking
      if (!stationRanking || !this.$refs.rankChart) return

      if (!this.rankInstance) {
        this.rankInstance = echarts.init(this.$refs.rankChart)
      }

      // Sort ascending so best accuracy appears at top with inverse yAxis
      var sorted = stationRanking.slice().sort(function (a, b) {
        return a.accuracy - b.accuracy
      })

      var names = []
      var values = []
      for (var i = 0; i < sorted.length; i++) {
        names.push(sorted[i].name)
        values.push(sorted[i].accuracy)
      }

      var option = {
        backgroundColor: 'transparent',
        textStyle: {
          color: CHART_TEXT_COLOR
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          backgroundColor: 'rgba(0,0,0,0.75)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: {
            color: CHART_TEXT_COLOR
          },
          formatter: function (params) {
            var item = params[0]
            return item.name + ': ' + item.value + '%'
          }
        },
        grid: {
          top: 20,
          right: 40,
          bottom: 20,
          left: 120
        },
        xAxis: {
          type: 'value',
          min: 80,
          max: 100,
          axisLabel: {
            color: CHART_TEXT_COLOR
          },
          axisLine: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          },
          splitLine: {
            lineStyle: {
              color: SPLIT_LINE_COLOR
            }
          }
        },
        yAxis: {
          type: 'category',
          data: names,
          inverse: true,
          axisLabel: {
            color: CHART_TEXT_COLOR
          },
          axisLine: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          },
          axisTick: {
            lineStyle: {
              color: AXIS_LINE_COLOR
            }
          }
        },
        series: [
          {
            type: 'bar',
            data: values,
            itemStyle: {
              color: function (params) {
                var val = params.value
                if (val >= 95) return '#67C23A'
                if (val >= 90) return '#E6A23C'
                return '#F56C6C'
              }
            }
          }
        ]
      }

      this.rankInstance.setOption(option, true)
    }
  }
}
</script>

<style lang="less" scoped>
.forecast-accuracy-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.forecast-accuracy-view__kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.forecast-accuracy-view__chart {
  width: 100%;
  min-height: 520px;
}

.forecast-accuracy-view__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 1280px) {
  .forecast-accuracy-view__kpis {
    grid-template-columns: repeat(2, 1fr);
  }
  .forecast-accuracy-view__grid {
    grid-template-columns: 1fr;
  }
}
</style>
