<template>
  <div class="station-data-header">
    <div class="station-data-header__path">
      <span
        v-for="(crumb, idx) in breadcrumbs"
        :key="idx"
        class="station-data-header__crumb"
      >
        {{ crumb }}
        <i v-if="idx < breadcrumbs.length - 1" class="el-icon-arrow-right" />
      </span>
    </div>
    <div class="station-data-header__info">
      <h2 class="station-data-header__name">{{ node.label }}</h2>
      <el-tag
        v-if="dataQualityLabel"
        size="mini"
        :type="dataQualityType"
        effect="plain"
      >
        {{ dataQualityLabel }}
      </el-tag>
      <el-tag
        v-if="statusLabel"
        size="mini"
        :type="statusType"
        effect="plain"
      >
        {{ statusLabel }}
      </el-tag>
    </div>
  </div>
</template>

<script>
const STATUS_MAP = {
  normal: { label: '正常', type: 'success' },
  warning: { label: '告警', type: 'warning' },
  fault: { label: '故障', type: 'danger' },
  maintenance: { label: '检修', type: 'info' },
  offline: { label: '离线', type: 'info' }
}

const DATA_QUALITY_MAP = {
  good: { label: '数据正常', type: 'success' },
  missing: { label: '存在漏采', type: 'warning' }
}

export default {
  name: 'StationDataHeader',
  props: {
    node: {
      type: Object,
      required: true,
      default: () => ({
        id: '',
        label: '',
        nodeType: '',
        statusTag: '',
        statusColor: '',
        extra: {},
        parentPath: []
      })
    }
  },
  computed: {
    breadcrumbs() {
      const path = this.node.parentPath
      if (!path || !path.length) {
        return []
      }
      return path.map(item => item.label)
    },
    statusLabel() {
      const status = this.node.extra && this.node.extra.status
      if (!status || !STATUS_MAP[status]) {
        return ''
      }
      return STATUS_MAP[status].label
    },
    statusType() {
      const status = this.node.extra && this.node.extra.status
      if (!status || !STATUS_MAP[status]) {
        return ''
      }
      return STATUS_MAP[status].type
    },
    dataQualityLabel() {
      if (this.node.nodeType !== 'station') {
        return ''
      }
      const quality = this.node.extra && this.node.extra.dataQuality
      if (!quality || !DATA_QUALITY_MAP[quality]) {
        return ''
      }
      return DATA_QUALITY_MAP[quality].label
    },
    dataQualityType() {
      if (this.node.nodeType !== 'station') {
        return ''
      }
      const quality = this.node.extra && this.node.extra.dataQuality
      if (!quality || !DATA_QUALITY_MAP[quality]) {
        return ''
      }
      return DATA_QUALITY_MAP[quality].type
    }
  }
}
</script>

<style lang="less" scoped>
.station-data-header {
  flex-shrink: 0;
  padding: 16px 0 14px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--pvms-border-soft, rgba(255, 255, 255, 0.08));

  &__path {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    margin-bottom: 8px;
    line-height: 1.5;
  }

  &__crumb {
    display: inline-flex;
    align-items: center;
    font-size: 12px;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
    white-space: nowrap;

    .el-icon-arrow-right {
      margin: 0 6px;
      font-size: 11px;
      color: rgba(255, 255, 255, 0.3);
    }
  }

  &__info {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }

  &__name {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--pvms-text-primary, rgba(255, 255, 255, 0.92));
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 400px;
  }

  /deep/ .el-tag {
    border-radius: 3px;
    font-size: 11px;
    height: 22px;
    line-height: 20px;
    padding: 0 8px;
    background: transparent;
    flex-shrink: 0;

    &--success {
      color: #67c23a;
      border-color: rgba(103, 194, 58, 0.4);
    }

    &--warning {
      color: #e6a23c;
      border-color: rgba(230, 162, 60, 0.4);
    }

    &--danger {
      color: #f56c6c;
      border-color: rgba(245, 108, 108, 0.4);
    }

    &--info {
      color: rgba(255, 255, 255, 0.55);
      border-color: rgba(255, 255, 255, 0.2);
    }
  }
}
</style>
