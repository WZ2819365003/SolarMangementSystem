<template>
  <div class="pv-shell">
    <aside class="pv-shell__aside">
      <div class="pv-shell__brand">
        <div class="pv-shell__brand-mark">
          PV
        </div>
        <div>
          <div class="pv-shell__brand-name">
            {{ appName }}
          </div>
          <div class="pv-shell__brand-desc">
            {{ appDescription }}
          </div>
        </div>
      </div>

      <div class="pv-shell__tenant">
        <div class="pv-shell__tenant-label">
          当前站群
        </div>
        <div class="pv-shell__tenant-value">
          {{ runtime.tenantName }}
        </div>
      </div>

      <el-menu
        :default-active="activeMenuPath"
        class="pv-shell__menu"
        router
        background-color="transparent"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#ffffff"
      >
        <el-menu-item v-for="item in menus" :key="item.key" :index="item.path">
          <i :class="item.icon" />
          <span slot="title">{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <section class="pv-shell__main">
      <header class="pv-shell__header">
        <div>
          <div class="pv-shell__breadcrumb">
            {{ currentRouteMeta.section }}
          </div>
          <h1 class="pv-shell__header-title">
            {{ currentRouteMeta.title }}
          </h1>
        </div>
        <div class="pv-shell__user">
          <div class="pv-shell__user-avatar">
            {{ runtime.userName.slice(0, 1) }}
          </div>
          <div>
            <div class="pv-shell__user-name">
              {{ runtime.userName }}
            </div>
            <div class="pv-shell__user-meta">
              {{ runtime.hostMode ? '宿主挂载模式' : '本地预览模式' }}
            </div>
          </div>
        </div>
      </header>

      <global-filter-bar v-if="$route.meta && $route.meta.filterKey" />

      <main class="pv-shell__content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script>
import GlobalFilterBar from '@/components/GlobalFilterBar.vue'

export default {
  name: 'ShellLayout',
  components: {
    GlobalFilterBar
  },
  computed: {
    appName() {
      return this.$store.state.appName
    },
    appDescription() {
      return this.$store.state.appDescription
    },
    menus() {
      return this.$store.getters.menus
    },
    runtime() {
      return this.$store.getters.runtime
    },
    currentRouteMeta() {
      return this.$route.meta || {}
    },
    activeMenuPath() {
      return this.$route.meta.activeMenu || this.$route.path
    }
  }
}
</script>

<style lang="less" scoped>
.pv-shell {
  display: flex;
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(26, 141, 255, 0.16), transparent 30%),
    linear-gradient(180deg, rgba(7, 27, 61, 0.98), rgba(5, 18, 45, 1));
}

.pv-shell__aside {
  display: flex;
  flex-direction: column;
  width: 288px;
  padding: 28px 20px 20px;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(180deg, rgba(7, 31, 74, 0.96), rgba(6, 21, 48, 0.98));
}

.pv-shell__brand {
  display: flex;
  gap: 14px;
  align-items: center;
}

.pv-shell__brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(6, 162, 153, 0.95), rgba(26, 141, 255, 0.9));
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.pv-shell__brand-name {
  color: var(--pvms-text-primary);
  font-size: 18px;
  font-weight: 600;
}

.pv-shell__brand-desc {
  margin-top: 4px;
  color: var(--pvms-text-muted);
  font-size: 12px;
  line-height: 18px;
}

.pv-shell__tenant {
  margin-top: 24px;
  padding: 14px 16px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
}

.pv-shell__tenant-label {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.pv-shell__tenant-value {
  margin-top: 8px;
  color: var(--pvms-text-primary);
  font-size: 14px;
  font-weight: 500;
}

.pv-shell__menu {
  flex: 1;
  margin-top: 18px;
  border-right: none;
}

.pv-shell__menu /deep/ .el-menu-item {
  height: 54px;
  line-height: 54px;
  margin-bottom: 10px;
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.72) !important;
}

.pv-shell__menu /deep/ .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(6, 162, 153, 0.26), rgba(26, 141, 255, 0.22)) !important;
  color: #fff !important;
}

.pv-shell__menu /deep/ .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08) !important;
}

.pv-shell__menu /deep/ .el-menu-item i {
  margin-right: 12px;
  color: inherit;
}

.pv-shell__env {
  padding: 16px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--pvms-text-muted);
  font-size: 12px;
  line-height: 20px;
}

.pv-shell__env p {
  margin: 12px 0 0;
}

.pv-shell__env-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 88px;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(6, 162, 153, 0.16);
  color: var(--pvms-primary);
  font-weight: 600;
}

.pv-shell__main {
  flex: 1;
  min-width: 0;
  padding: 24px;
}

.pv-shell__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}

.pv-shell__breadcrumb {
  color: var(--pvms-text-muted);
  font-size: 12px;
  letter-spacing: 1px;
}

.pv-shell__header-title {
  margin: 8px 0 0;
  color: var(--pvms-text-primary);
  font-size: 28px;
  line-height: 34px;
}

.pv-shell__user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
}

.pv-shell__user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(6, 162, 153, 0.82), rgba(26, 141, 255, 0.76));
  color: #fff;
  font-weight: 700;
}

.pv-shell__user-name {
  color: var(--pvms-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.pv-shell__user-meta {
  margin-top: 4px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.pv-shell__content {
  min-height: calc(100vh - 120px);
}

@media (max-width: 1280px) {
  .pv-shell {
    flex-direction: column;
  }

  .pv-shell__aside {
    width: 100%;
  }
}
</style>
