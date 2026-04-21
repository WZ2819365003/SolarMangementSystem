<template>
  <header class="strategy-hero" data-testid="strategy-hero">
    <div class="strategy-hero__text">
      <h1 class="strategy-hero__title">策略管理</h1>
      <p class="strategy-hero__desc">{{ viewDesc }}</p>
    </div>
  </header>
</template>

<script>
var viewDescMap = {
  list: '查看策略台账、状态流转、收益快照和批量操作结果',
  config: '配置策略参数、站点范围、电价窗口，并在后台执行模拟和创建',
  revenue: '查看收益趋势、收益明细和策略对比，明确哪些结果来自后台实时计算'
}

export default {
  name: 'StrategyHero',
  props: {
    viewKey: {
      type: String,
      default: 'list'
    }
  },
  computed: {
    viewDesc() {
      return viewDescMap[this.viewKey] || viewDescMap.list
    }
  }
}
</script>

<style lang="less" scoped>
.strategy-hero {
  padding: 24px 28px;
  border-radius: 4px;
  border: 1px solid var(--pvms-border-soft);
  background: linear-gradient(135deg, rgba(16, 54, 86, 0.96), rgba(9, 29, 67, 0.92));
  box-shadow: var(--pvms-shadow-soft);
}

.strategy-hero__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 22px;
  font-weight: 600;
  line-height: 1.4;
}

.strategy-hero__desc {
  margin: 8px 0 0;
  color: var(--pvms-text-muted);
  font-size: 14px;
  line-height: 1.6;
}
</style>
