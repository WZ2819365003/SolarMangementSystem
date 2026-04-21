<template>
  <app-section-card title="成员电站概况" subtitle="同区位电站共享天气与出力趋势，当前只按权重和状态分摊">
    <div class="pv-table-shell" data-testid="member-station-summary-table">
      <el-table :data="stations" size="mini" style="width: 100%">
        <el-table-column prop="name" label="电站" min-width="180" />
        <el-table-column label="状态" width="90">
          <template slot-scope="{ row }">
            <el-tag size="mini" effect="dark" :type="resolveTagType(row.statusLabel)">
              {{ row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="capacityMw" label="容量(MW)" width="100" />
        <el-table-column prop="realtimePowerMw" label="实时出力(MW)" width="130" />
        <el-table-column prop="onlineRate" label="在线率" width="100">
          <template slot-scope="{ row }">
            {{ row.onlineRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="adjustableCapacityMw" label="可调容量(MW)" width="130" />
        <el-table-column prop="alarmCount" label="告警数" width="90" />
        <el-table-column label="天气" min-width="130">
          <template slot-scope="{ row }">
            <span data-testid="member-station-weather-cell">
              {{ row.weatherText }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'
import { resolveTagType } from '@/utils/formatters'

export default {
  name: 'MemberStationSummaryTable',
  components: {
    AppSectionCard
  },
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
