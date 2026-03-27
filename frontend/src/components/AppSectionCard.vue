<template>
  <section class="app-section-card">
    <header class="app-section-card__header">
      <div>
        <h2 class="app-section-card__title">
          {{ title }}
        </h2>
        <p v-if="subtitle" class="app-section-card__subtitle">
          {{ subtitle }}
        </p>
      </div>
      <div class="app-section-card__extra">
        <slot name="extra" />
      </div>
    </header>
    <div class="app-section-card__content">
      <slot />
    </div>
  </section>
</template>

<script>
export default {
  name: 'AppSectionCard',
  props: {
    title: {
      type: String,
      required: true
    },
    subtitle: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="less" scoped>
.app-section-card {
  padding: 22px 24px;
  border-radius: 4px;
  border: 1px solid var(--pvms-border-soft);
  background: var(--pvms-panel);
  box-shadow: var(--pvms-shadow-soft);
}

.app-section-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.app-section-card__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 18px;
  line-height: 28px;
}

.app-section-card__subtitle {
  margin: 6px 0 0;
  color: var(--pvms-text-muted);
  font-size: 13px;
}
</style>
