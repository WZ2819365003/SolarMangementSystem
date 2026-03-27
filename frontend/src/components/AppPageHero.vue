<template>
  <section class="app-page-hero">
    <div class="app-page-hero__copy">
      <div class="app-page-hero__eyebrow">
        {{ eyebrow }}
      </div>
      <h1 class="app-page-hero__title">
        {{ title }}
      </h1>
      <p class="app-page-hero__description">
        {{ description }}
      </p>
    </div>
    <div class="app-page-hero__meta">
      <slot name="meta" />
    </div>
  </section>
</template>

<script>
export default {
  name: 'AppPageHero',
  props: {
    eyebrow: {
      type: String,
      default: 'PVMS'
    },
    title: {
      type: String,
      required: true
    },
    description: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="less" scoped>
.app-page-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px;
  border: 1px solid var(--pvms-border-soft);
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(7, 46, 94, 0.98), rgba(5, 23, 56, 0.96));
  box-shadow: 0 16px 34px rgba(4, 16, 40, 0.28);
}

.app-page-hero__eyebrow {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--pvms-text-muted);
  letter-spacing: 1px;
  font-size: 12px;
}

.app-page-hero__title {
  margin: 14px 0 10px;
  color: var(--pvms-text-primary);
  font-size: 28px;
  line-height: 36px;
}

.app-page-hero__description {
  max-width: 580px;
  margin: 0;
  color: var(--pvms-text-secondary);
  line-height: 24px;
}

.app-page-hero__meta {
  min-width: 220px;
}

@media (max-width: 1200px) {
  .app-page-hero {
    flex-direction: column;
  }
}
</style>
