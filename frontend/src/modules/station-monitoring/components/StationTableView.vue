<template>
  <el-table
    :data="stations"
    stripe
    class="station-table-view"
    data-testid="station-table-view"
    @row-click="$emit('select', $event)"
  >
    <el-table-column label="资源单元名称" min-width="220">
      <template slot-scope="{ row }">
        <button class="station-table-view__link" @click.stop="$emit('select', row)">
          {{ row.name }}
        </button>
      </template>
    </el-table-column>
    <el-table-column prop="region" label="聚合区域" min-width="120" />
    <el-table-column label="状态" width="110">
      <template slot-scope="{ row }">
        <el-tag size="mini" :type="resolveTagType(row.statusLabel)">
          {{ row.statusLabel }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column label="纳管电站数" width="120" align="center">
      <template slot-scope="{ row }">
        {{ row.stationCount }} 座
      </template>
    </el-table-column>
    <el-table-column label="可调容量" width="130">
      <template slot-scope="{ row }">
        {{ row.dispatchableCapacityMw }} MW
      </template>
    </el-table-column>
    <el-table-column label="实时出力" width="130">
      <template slot-scope="{ row }">
        {{ row.realtimePowerMw }} MW
      </template>
    </el-table-column>
    <el-table-column label="预测偏差" width="130">
      <template slot-scope="{ row }">
        <span :class="{ 'station-table-view__warning': Math.abs(row.forecastDeviationRate) >= 5 }">
          {{ row.forecastDeviationRate }}%
        </span>
      </template>
    </el-table-column>
    <el-table-column label="在线率" width="140">
      <template slot-scope="{ row }">
        <el-progress :percentage="row.onlineRate" :show-text="false" />
      </template>
    </el-table-column>
    <el-table-column label="告警数" width="100" align="center">
      <template slot-scope="{ row }">
        <el-badge :value="row.alarmCount" :hidden="!row.alarmCount" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="110" align="center">
      <template slot-scope="{ row }">
        <el-button type="text" @click.stop="$emit('select', row)">
          查看总览
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import { resolveTagType } from '@/utils/formatters'

export default {
  name: 'StationTableView',
  props: {
    stations: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    resolveTagType
  }
}
</script>

<style lang="less" scoped>
.station-table-view__link {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--pvms-primary);
  cursor: pointer;
}

.station-table-view__warning {
  color: #f56c6c;
}

.station-table-view /deep/ .el-progress-bar__outer {
  background: rgba(255, 255, 255, 0.08);
}

.station-table-view /deep/ .el-progress-bar__inner {
  background: linear-gradient(90deg, rgba(6, 162, 153, 0.95), rgba(26, 141, 255, 0.95));
}
</style>
