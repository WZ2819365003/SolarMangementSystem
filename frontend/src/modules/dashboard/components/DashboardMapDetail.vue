<template>
  <section class="dashboard-map-detail">
    <div class="dashboard-map-detail__head">
      <div>
        <div class="dashboard-map-detail__title">
          {{ station.name }}
        </div>
        <div class="dashboard-map-detail__meta">
          {{ station.region }}
        </div>
      </div>
      <el-tag size="mini" effect="dark" :color="station.statusColor">
        {{ station.statusLabel }}
      </el-tag>
    </div>

    <dl class="dashboard-map-detail__grid">
      <div>
        <dt>装机容量</dt>
        <dd>{{ formatCapacity(station.capacityKwp) }}</dd>
      </div>
      <div>
        <dt>实时功率</dt>
        <dd>{{ station.realtimePowerKw }} kW</dd>
      </div>
      <div>
        <dt>今日发电</dt>
        <dd>{{ station.todayEnergyKwh }} kWh</dd>
      </div>
      <div>
        <dt>今日收益</dt>
        <dd>¥ {{ station.todayRevenueCny }}</dd>
      </div>
    </dl>

    <div class="dashboard-map-detail__address">
      {{ station.address }}
    </div>

    <div class="dashboard-map-detail__actions">
      <hs-button size="mini" @click="$emit('view-detail', station)">
        查看详情
      </hs-button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'DashboardMapDetail',
  props: {
    station: {
      type: Object,
      required: true
    }
  },
  methods: {
    formatCapacity(value) {
      return `${(value / 1000).toFixed(1)} MWp`
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-map-detail {
  width: 280px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  background: rgba(6, 23, 53, 0.92);
  backdrop-filter: blur(12px);
  box-shadow: 0 20px 32px rgba(0, 10, 28, 0.28);
}

.dashboard-map-detail__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.dashboard-map-detail__title {
  color: var(--pvms-text-primary);
  font-size: 15px;
  font-weight: 600;
  line-height: 22px;
}

.dashboard-map-detail__meta {
  margin-top: 6px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-map-detail__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 16px 0 0;
}

.dashboard-map-detail__grid dt {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-map-detail__grid dd {
  margin: 6px 0 0;
  color: var(--pvms-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.dashboard-map-detail__address {
  margin-top: 14px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
  line-height: 18px;
}

.dashboard-map-detail__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
