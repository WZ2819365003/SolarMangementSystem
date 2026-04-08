<template>
  <div v-loading="loading" class="forecast-overview-view" data-testid="forecast-overview-view">
    <!-- KPI Cards -->
    <section class="pv-card-grid forecast-overview-view__kpis">
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

    <!-- Comparison Chart -->
    <app-section-card title="预测对比曲线">
      <div ref="comparisonChart" class="forecast-overview-view__chart" />
    </app-section-card>

    <!-- Bottom grid: heatmap + table -->
    <div class="forecast-overview-view__grid">
      <app-section-card title="偏差热力图">
        <div ref="heatmapChart" class="forecast-overview-view__heatmap" />
      </app-section-card>

      <app-section-card title="电站预测明细">
        <el-table :data="pagedStationRows" size="mini" stripe style="width: 100%;">
          <el-table-column prop="name" label="电站" min-width="160" show-overflow-tooltip />
          <el-table-column prop="predicted" label="预测出力(MW)" min-width="130" align="right" />
          <el-table-column prop="actual" label="实际出力(MW)" min-width="130" align="right" />
          <el-table-column prop="deviation" label="偏差(MW)" min-width="100" align="right" />
          <el-table-column prop="accuracy" label="精度(%)" min-width="100" align="right" />
        </el-table>
        <el-pagination
          small
          background
          layout="total, sizes, prev, pager, next"
          :page-sizes="[6, 10, 20]"
          :page-size="stationPageSize"
          :current-page="stationCurrentPage"
          :total="stationRows.length"
          style="margin-top: 12px; text-align: right;"
          @size-change="val => { stationPageSize = val; stationCurrentPage = 1 }"
          @current-change="val => { stationCurrentPage = val }"
        />
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
  name: 'ForecastOverviewView',

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
      comparisonInstance: null,
      heatmapInstance: null,
      stationCurrentPage: 1,
      stationPageSize: 6
    }
  },

  computed: {
    kpis: function () {
      return this.viewData.kpis || {}
    },

    kpiCards: function () {
      var kpis = this.kpis
      return [
        { key: 'dayAhead', title: '日前预测精度', value: kpis.dayAheadAccuracy, unit: '%', icon: 'el-icon-data-line', accent: 'teal' },
        { key: 'ultraShort', title: '超短期预测精度', value: kpis.ultraShortAccuracy, unit: '%', icon: 'el-icon-time', accent: 'blue' },
        { key: 'deviation', title: '当日预测偏差', value: kpis.dailyDeviation, unit: 'MW', icon: 'el-icon-warning-outline', accent: 'orange' },
        { key: 'qualified', title: '预测合格率', value: kpis.qualifiedRate, unit: '%', icon: 'el-icon-circle-check', accent: 'emerald' }
      ]
    },

    stationRows: function () {
      return this.viewData.stationTable || []
    },

    pagedStationRows: function () {
      var start = (this.stationCurrentPage - 1) * this.stationPageSize
      return this.stationRows.slice(start, start + this.stationPageSize)
    }
  },

  watch: {
    viewData: {
      deep: true,
      handler: function () {
        var self = this
        self.$nextTick(function () {
          self.renderComparisonChart()
          self.renderHeatmapChart()
        })
      }
    }
  },

  mounted: function () {
    this.handleResize = this._handleResize.bind(this)
    window.addEventListener('resize', this.handleResize)
    this.$nextTick(function () {
      this.renderComparisonChart()
      this.renderHeatmapChart()
    })
  },

  beforeDestroy: function () {
    if (this.comparisonInstance) {
      this.comparisonInstance.dispose()
      this.comparisonInstance = null
    }
    if (this.heatmapInstance) {
      this.heatmapInstance.dispose()
      this.heatmapInstance = null
    }
    window.removeEventListener('resize', this.handleResize)
  },

  methods: {
    _handleResize: function () {
      if (this.comparisonInstance) {
        this.comparisonInstance.resize()
      }
      if (this.heatmapInstance) {
        this.heatmapInstance.resize()
      }
    },

    renderComparisonChart: function () {
      var comparison = this.viewData.comparison
      if (!comparison || !this.$refs.comparisonChart) return

      if (!this.comparisonInstance) {
        this.comparisonInstance = echarts.init(this.$refs.comparisonChart)
      }

      var timeLabels = comparison.timeLabels || []
      var series = comparison.series || {}

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
          data: timeLabels,
          axisLabel: {
            color: CHART_TEXT_COLOR,
            interval: 3
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
          name: 'MW',
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
            name: '日前预测',
            type: 'line',
            smooth: true,
            data: series.dayAhead || [],
            itemStyle: { color: '#409EFF' },
            lineStyle: { color: '#409EFF' },
            areaStyle: {
              color: 'rgba(64,158,255,0.08)'
            },
            symbol: 'none'
          },
          {
            name: '超短期预测',
            type: 'line',
            smooth: true,
            data: series.ultraShort || [],
            itemStyle: { color: '#36CFC9' },
            lineStyle: { color: '#36CFC9' },
            symbol: 'none'
          },
          {
            name: '实际出力',
            type: 'line',
            smooth: true,
            data: series.actual || [],
            itemStyle: { color: '#67C23A' },
            lineStyle: { color: '#67C23A' },
            symbol: 'none'
          }
        ]
      }

      this.comparisonInstance.setOption(option, true)
    },

    renderHeatmapChart: function () {
      var heatmap = this.viewData.heatmap
      if (!heatmap || !this.$refs.heatmapChart) return

      if (!this.heatmapInstance) {
        this.heatmapInstance = echarts.init(this.$refs.heatmapChart)
      }

      var hours = heatmap.hours || []
      var stations = heatmap.stations || []
      var rawData = heatmap.data || []

      // Flatten 2D array to [[xIndex, yIndex, value], ...]
      var flatData = []
      for (var yIdx = 0; yIdx < rawData.length; yIdx++) {
        var row = rawData[yIdx] || []
        for (var xIdx = 0; xIdx < row.length; xIdx++) {
          flatData.push([xIdx, yIdx, row[xIdx]])
        }
      }

      var option = {
        backgroundColor: 'transparent',
        textStyle: {
          color: CHART_TEXT_COLOR
        },
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(0,0,0,0.75)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: {
            color: CHART_TEXT_COLOR
          },
          formatter: function (params) {
            var xLabel = hours[params.value[0]]
            var yLabel = stations[params.value[1]]
            var val = params.value[2]
            return yLabel + '<br/>' + xLabel + '时: ' + val + ' MW'
          }
        },
        grid: {
          top: 20,
          right: 80,
          bottom: 40,
          left: 120
        },
        xAxis: {
          type: 'category',
          data: hours,
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
          },
          splitArea: {
            show: false
          }
        },
        yAxis: {
          type: 'category',
          data: stations,
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
          },
          splitArea: {
            show: false
          }
        },
        visualMap: {
          min: 0,
          max: 15,
          calculable: true,
          orient: 'vertical',
          right: 0,
          top: 'center',
          inRange: {
            color: ['#1a9850', '#fee08b', '#d73027']
          },
          textStyle: {
            color: CHART_TEXT_COLOR
          }
        },
        series: [
          {
            type: 'heatmap',
            data: flatData,
            emphasis: {
              itemStyle: {
                shadowBlur: 6,
                shadowColor: 'rgba(0,0,0,0.3)'
              }
            }
          }
        ]
      }

      this.heatmapInstance.setOption(option, true)
    }
  }
}
</script>

<style lang="less" scoped>
.forecast-overview-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.forecast-overview-view__kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.forecast-overview-view__chart {
  width: 100%;
  min-height: 520px;
}

.forecast-overview-view__heatmap {
  width: 100%;
  min-height: 520px;
}

.forecast-overview-view__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 1280px) {
  .forecast-overview-view__kpis {
    grid-template-columns: repeat(2, 1fr);
  }
  .forecast-overview-view__grid {
    grid-template-columns: 1fr;
  }
}
</style>
