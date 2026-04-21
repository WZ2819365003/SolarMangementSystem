<template>
  <header class="forecast-hero" data-testid="forecast-hero">
    <div class="forecast-hero__text">
      <h1 class="forecast-hero__title">预测与分析</h1>
      <p class="forecast-hero__desc">{{ viewDesc }}</p>
    </div>
  </header>
</template>

<script>
var viewDescMap = {
  overview: '查看日前与超短期光伏功率预测对比、偏差热力图和电站预测明细',
  adjustable: '查看24小时可调能力预测曲线、VPP节点可调时间线和电站可调明细',
  accuracy: '评估预测精度趋势、偏差分布、电站精度排名和月度对比'
}

export default {
  name: 'ForecastHero',
  props: {
    viewKey: {
      type: String,
      default: 'overview'
    }
  },
  computed: {
    viewDesc() {
      return viewDescMap[this.viewKey] || viewDescMap.overview
    }
  }
}
</script>

<style lang="less" scoped>
.forecast-hero {
  padding: 24px 28px;
  border-radius: 4px;
  border: 1px solid var(--pvms-border-soft);
  background: linear-gradient(135deg, rgba(10, 40, 88, 0.96), rgba(7, 27, 66, 0.92));
  box-shadow: var(--pvms-shadow-soft);
}

.forecast-hero__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 22px;
  font-weight: 600;
  line-height: 1.4;
}

.forecast-hero__desc {
  margin: 8px 0 0;
  color: var(--pvms-text-muted);
  font-size: 14px;
  line-height: 1.6;
}
</style>
