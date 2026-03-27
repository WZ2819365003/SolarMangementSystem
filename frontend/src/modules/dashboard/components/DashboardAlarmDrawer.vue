<template>
  <el-drawer
    :visible.sync="innerVisible"
    size="420px"
    custom-class="dashboard-alarm-drawer"
    :with-header="false"
  >
    <div v-if="alarm" class="dashboard-alarm-drawer__body">
      <div class="dashboard-alarm-drawer__eyebrow">
        告警详情
      </div>
      <h3 class="dashboard-alarm-drawer__title">
        {{ alarm.deviceName }}
      </h3>
      <p class="dashboard-alarm-drawer__desc">
        {{ alarm.description }}
      </p>

      <div class="dashboard-alarm-drawer__tag-row">
        <el-tag effect="dark" size="mini">
          {{ alarm.levelLabel }}
        </el-tag>
        <el-tag type="info" size="mini">
          {{ alarm.status }}
        </el-tag>
      </div>

      <dl class="dashboard-alarm-drawer__grid">
        <div>
          <dt>所属电站</dt>
          <dd>{{ alarm.stationName }}</dd>
        </div>
        <div>
          <dt>告警编号</dt>
          <dd>{{ alarm.id }}</dd>
        </div>
        <div>
          <dt>发生时间</dt>
          <dd>{{ alarm.time }}</dd>
        </div>
        <div>
          <dt>责任人</dt>
          <dd>{{ alarm.owner }}</dd>
        </div>
      </dl>

      <div class="dashboard-alarm-drawer__section">
        <div class="dashboard-alarm-drawer__section-title">
          建议动作
        </div>
        <p>{{ alarm.suggestion }}</p>
      </div>

      <div class="dashboard-alarm-drawer__actions">
        <hs-button @click="innerVisible = false">
          关闭
        </hs-button>
        <hs-button type="primary">
          确认派单
        </hs-button>
      </div>
    </div>
  </el-drawer>
</template>

<script>
export default {
  name: 'DashboardAlarmDrawer',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    alarm: {
      type: Object,
      default: null
    }
  },
  computed: {
    innerVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-alarm-drawer__body {
  padding: 28px 24px;
  color: #0e2145;
}

.dashboard-alarm-drawer__eyebrow {
  color: #7b889b;
  font-size: 12px;
  letter-spacing: 1px;
}

.dashboard-alarm-drawer__title {
  margin: 12px 0 0;
  font-size: 24px;
}

.dashboard-alarm-drawer__desc {
  margin: 12px 0 0;
  color: #4f5d73;
  line-height: 22px;
}

.dashboard-alarm-drawer__tag-row {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.dashboard-alarm-drawer__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 12px;
  margin-top: 22px;
}

.dashboard-alarm-drawer__grid dt {
  color: #7b889b;
  font-size: 12px;
}

.dashboard-alarm-drawer__grid dd {
  margin: 6px 0 0;
  color: #0e2145;
  font-size: 14px;
  font-weight: 600;
}

.dashboard-alarm-drawer__section {
  margin-top: 24px;
  padding: 16px;
  border-radius: 4px;
  background: #f5f7fb;
  color: #31445f;
  line-height: 22px;
}

.dashboard-alarm-drawer__section-title {
  margin-bottom: 8px;
  color: #0e2145;
  font-weight: 600;
}

.dashboard-alarm-drawer__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 26px;
}
</style>
