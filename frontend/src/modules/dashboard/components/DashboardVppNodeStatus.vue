<template>
  <article class="dashboard-vpp-node-status" data-testid="dashboard-vpp-node-status">
    <header class="dashboard-vpp-node-status__header">
      <div>
        <h3 class="dashboard-vpp-node-status__title">VPP 节点状态</h3>
        <p class="dashboard-vpp-node-status__subtitle">可调能力与响应概览</p>
      </div>
    </header>

    <div class="dashboard-vpp-node-status__headline">
      当前生效策略：{{ payload.activeStrategyName || '暂无' }}
      <span v-if="payload.lastReportTime" class="dashboard-vpp-node-status__time">
        · 上报 {{ payload.lastReportTime }}
      </span>
    </div>

    <div class="dashboard-vpp-node-status__grid">
      <section class="dashboard-vpp-node-status__tile is-teal">
        <div class="dashboard-vpp-node-status__label">可调总量</div>
        <div class="dashboard-vpp-node-status__value">
          {{ payload.totalDeferrableLoad || 0 }}
          <span class="dashboard-vpp-node-status__unit">kW</span>
        </div>
      </section>
      <section class="dashboard-vpp-node-status__tile is-blue">
        <div class="dashboard-vpp-node-status__label">今日响应</div>
        <div class="dashboard-vpp-node-status__value">
          {{ payload.todayResponseCount || 0 }}
          <span class="dashboard-vpp-node-status__unit">次</span>
        </div>
      </section>
      <section class="dashboard-vpp-node-status__tile is-emerald">
        <div class="dashboard-vpp-node-status__label">累计响应电量</div>
        <div class="dashboard-vpp-node-status__value">
          {{ payload.todayResponseEnergyKwh || 0 }}
          <span class="dashboard-vpp-node-status__unit">kWh</span>
        </div>
      </section>
      <section class="dashboard-vpp-node-status__tile is-orange">
        <div class="dashboard-vpp-node-status__label">策略执行率</div>
        <div class="dashboard-vpp-node-status__value">
          {{ payload.strategyExecutionRate || 0 }}
          <span class="dashboard-vpp-node-status__unit">%</span>
        </div>
        <div class="dashboard-vpp-node-status__progress">
          <span :style="{ width: `${Math.max(8, payload.strategyExecutionRate || 0)}%` }" />
        </div>
      </section>
      <section class="dashboard-vpp-node-status__tile is-purple">
        <div class="dashboard-vpp-node-status__label">平均响应时间</div>
        <div class="dashboard-vpp-node-status__value">
          {{ payload.avgResponseTime || 0 }}
          <span class="dashboard-vpp-node-status__unit">s</span>
        </div>
      </section>
      <section class="dashboard-vpp-node-status__tile is-pink">
        <div class="dashboard-vpp-node-status__label">响应成功率</div>
        <div class="dashboard-vpp-node-status__value">
          {{ payload.responseSuccessRate || 0 }}
          <span class="dashboard-vpp-node-status__unit">%</span>
        </div>
        <div class="dashboard-vpp-node-status__progress">
          <span :style="{ width: `${Math.max(8, payload.responseSuccessRate || 0)}%` }" />
        </div>
      </section>
    </div>
  </article>
</template>

<script>
export default {
  name: 'DashboardVppNodeStatus',
  props: {
    payload: {
      type: Object,
      default: () => ({})
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-vpp-node-status {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  background:
    radial-gradient(circle at top right, rgba(6, 162, 153, 0.14), transparent 30%),
    linear-gradient(180deg, rgba(8, 31, 72, 0.88), rgba(7, 24, 56, 0.96));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.dashboard-vpp-node-status__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 15px;
  line-height: 22px;
}

.dashboard-vpp-node-status__subtitle {
  margin: 4px 0 0;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-vpp-node-status__headline {
  margin-top: 14px;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--pvms-text-secondary);
  font-size: 12px;
  line-height: 18px;
}

.dashboard-vpp-node-status__time {
  color: var(--pvms-text-muted);
}

.dashboard-vpp-node-status__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.dashboard-vpp-node-status__tile {
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.dashboard-vpp-node-status__label {
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.dashboard-vpp-node-status__value {
  margin-top: 12px;
  color: var(--pvms-text-primary);
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.dashboard-vpp-node-status__unit {
  margin-left: 4px;
  color: var(--pvms-text-muted);
  font-size: 12px;
  font-weight: 400;
}

.dashboard-vpp-node-status__progress {
  height: 6px;
  margin-top: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.05);
  overflow: hidden;
}

.dashboard-vpp-node-status__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(6, 162, 153, 0.9), rgba(26, 141, 255, 0.9));
}

.is-teal { box-shadow: inset 0 0 0 1px rgba(6, 162, 153, 0.14); }
.is-blue { box-shadow: inset 0 0 0 1px rgba(64, 158, 255, 0.14); }
.is-emerald { box-shadow: inset 0 0 0 1px rgba(0, 181, 120, 0.14); }
.is-orange { box-shadow: inset 0 0 0 1px rgba(245, 155, 35, 0.14); }
.is-purple { box-shadow: inset 0 0 0 1px rgba(139, 92, 246, 0.14); }
.is-pink { box-shadow: inset 0 0 0 1px rgba(236, 72, 153, 0.14); }
</style>
