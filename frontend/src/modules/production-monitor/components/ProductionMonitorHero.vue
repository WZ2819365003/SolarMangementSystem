<template>
  <div data-testid="production-monitor-hero">
    <app-page-hero
      eyebrow="M02 Production Monitor"
      title="生产监控层"
      description="以虚拟电厂聚合商视角，在同一模块内查看资源单元总览、出力分析、调度执行与光伏气象研判。"
    >
      <template #meta>
        <div class="production-monitor-hero__meta">
          <div class="production-monitor-hero__unit">
            <div class="production-monitor-hero__unit-name">
              {{ resourceUnit.name || '加载中...' }}
            </div>
            <div class="production-monitor-hero__unit-tags">
              <el-tag size="mini" effect="dark" type="success">
                {{ resourceUnit.statusLabel || '资源单元' }}
              </el-tag>
              <span>{{ resourceUnit.region || '--' }}</span>
              <span>{{ resourceUnit.city || '--' }}</span>
            </div>
          </div>
          <div class="production-monitor-hero__status">
            <div>
              <span>当前视图</span>
              <strong>{{ viewLabel }}</strong>
            </div>
            <div>
              <span>成员电站</span>
              <strong>{{ resourceUnit.stationCount || 0 }} 座</strong>
            </div>
            <div>
              <span>站群最大距离</span>
              <strong>{{ resourceUnit.clusterRadiusKm || '--' }} km</strong>
            </div>
          </div>
        </div>
      </template>
    </app-page-hero>
  </div>
</template>

<script>
import AppPageHero from '@/components/AppPageHero.vue'

export default {
  name: 'ProductionMonitorHero',
  components: {
    AppPageHero
  },
  props: {
    resourceUnit: {
      type: Object,
      default: () => ({})
    },
    viewKey: {
      type: String,
      default: 'overview'
    }
  },
  computed: {
    viewLabel() {
      return {
        overview: '资源总览',
        output: '出力分析',
        dispatch: '调度执行',
        weather: '气象研判'
      }[this.viewKey]
    }
  }
}
</script>

<style lang="less" scoped>
.production-monitor-hero__meta {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.production-monitor-hero__unit-name {
  color: var(--pvms-text-primary);
  font-size: 18px;
  font-weight: 600;
}

.production-monitor-hero__unit-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.production-monitor-hero__status {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.production-monitor-hero__status div {
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
}

.production-monitor-hero__status span {
  display: block;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.production-monitor-hero__status strong {
  display: block;
  margin-top: 8px;
  color: var(--pvms-text-primary);
  font-size: 15px;
  font-weight: 600;
}
</style>
