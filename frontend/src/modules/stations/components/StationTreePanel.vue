<template>
  <div class="station-tree-panel">
    <!-- Status filter tabs -->
    <el-tabs
      v-model="activeStatus"
      type="card"
      class="station-tree-panel__tabs"
      @tab-click="handleStatusTab"
    >
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="在线" name="normal" />
      <el-tab-pane label="离线" name="offline" />
      <el-tab-pane label="故障" name="fault" />
    </el-tabs>

    <!-- Search box -->
    <div class="station-tree-panel__search">
      <el-input
        v-model="searchText"
        size="mini"
        placeholder="搜索电站..."
        prefix-icon="el-icon-search"
        clearable
        @input="handleSearchDebounced"
      />
    </div>

    <!-- Tree -->
    <div class="station-tree-panel__tree-wrap">
      <el-tree
        ref="tree"
        :data="filteredTree"
        :props="treeProps"
        node-key="id"
        :highlight-current="true"
        :current-node-key="selectedNodeId"
        :default-expanded-keys="defaultExpandedKeys"
        :expand-on-click-node="false"
        :render-content="renderTreeNode"
        @node-click="handleNodeClick"
      />
      <div v-if="!filteredTree.length && !loading" class="station-tree-panel__empty">
        暂无匹配节点
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StationTreePanel',
  props: {
    treeData: {
      type: Array,
      default: () => []
    },
    selectedNodeId: {
      type: String,
      default: ''
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      activeStatus: '',
      searchText: '',
      searchTimer: null
    }
  },
  computed: {
    treeProps() {
      return { children: 'children', label: 'label' }
    },
    defaultExpandedKeys() {
      // Auto-expand first company node
      if (this.treeData.length) {
        return [this.treeData[0].id]
      }
      return []
    },
    filteredTree() {
      if (!this.searchText) {
        return this.treeData
      }
      return this.filterNodes(this.treeData, this.searchText.toLowerCase())
    }
  },
  watch: {
    selectedNodeId(val) {
      this.$nextTick(() => {
        if (this.$refs.tree && val) {
          this.$refs.tree.setCurrentKey(val)
        }
      })
    }
  },
  beforeDestroy() {
    clearTimeout(this.searchTimer)
  },
  methods: {
    filterNodes(nodes, keyword) {
      if (!nodes || !nodes.length) {
        return []
      }
      const result = []
      for (const node of nodes) {
        const labelMatch = node.label && node.label.toLowerCase().includes(keyword)
        const filteredChildren = this.filterNodes(node.children, keyword)
        if (labelMatch || filteredChildren.length > 0) {
          result.push({
            ...node,
            children: labelMatch ? (node.children || []) : filteredChildren
          })
        }
      }
      return result
    },
    handleStatusTab(tab) {
      this.$emit('status-filter', tab.name)
    },
    handleSearchDebounced() {
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        // filteredTree is reactive via computed — searchText change triggers recompute
      }, 300)
    },
    handleNodeClick(data) {
      this.$emit('node-click', data)
    },
    renderTreeNode(h, { node, data }) {
      const children = []

      // Type tag (e.g. 代理用户 / 资源 / 子资源)
      if (data.statusTag) {
        children.push(
          h(
            'el-tag',
            {
              props: { size: 'mini', effect: 'plain' },
              style: {
                marginRight: '8px',
                borderColor: data.statusColor || '#409eff',
                color: data.statusColor || '#409eff',
                background: 'transparent'
              }
            },
            data.statusTag
          )
        )
      }

      // Status indicator dot for station and inverter nodes
      if ((data.nodeType === 'station' || data.nodeType === 'inverter') && data.extra) {
        const statusColors = {
          normal: '#67C23A',
          warning: '#E6A23C',
          fault: '#F56C6C',
          maintenance: '#409EFF',
          offline: '#909399'
        }
        children.push(
          h('span', {
            class: 'station-tree-panel__status-dot',
            style: {
              background: statusColors[data.extra.status] || '#909399'
            }
          })
        )
      }

      children.push(
        h(
          'span',
          { class: 'station-tree-panel__node-label' },
          data.label
        )
      )

      return h('span', { class: 'station-tree-panel__node' }, children)
    }
  }
}
</script>

<style lang="less" scoped>
.station-tree-panel {
  width: 320px;
  flex-shrink: 0;
  align-self: stretch;
  display: flex;
  flex-direction: column;
  background: rgba(6, 20, 48, 0.96);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;

  // ---- Status filter tabs ----
  &__tabs {
    flex-shrink: 0;
    padding: 12px 12px 0;

    /deep/ .el-tabs__header {
      margin-bottom: 0;
      border-bottom: none;
    }

    /deep/ .el-tabs__nav-wrap {
      &::after {
        display: none;
      }
    }

    /deep/ .el-tabs__nav {
      border: none;
      display: flex;
      width: 100%;
    }

    /deep/ .el-tabs__item {
      flex: 1;
      text-align: center;
      height: 32px;
      line-height: 32px;
      padding: 0 8px;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.55);
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 0;
      transition: all 0.25s ease;

      &:first-child {
        border-radius: 4px 0 0 4px;
      }

      &:last-child {
        border-radius: 0 4px 4px 0;
      }

      &:hover {
        color: rgba(255, 255, 255, 0.85);
        background: rgba(24, 144, 255, 0.08);
      }

      &.is-active {
        color: #fff;
        background: var(--pvms-primary, #1890ff);
        border-color: var(--pvms-primary, #1890ff);
      }
    }
  }

  // ---- Search box ----
  &__search {
    flex-shrink: 0;
    padding: 12px;

    /deep/ .el-input__inner {
      background: rgba(255, 255, 255, 0.06);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 4px;
      color: rgba(255, 255, 255, 0.85);
      font-size: 13px;

      &::placeholder {
        color: rgba(255, 255, 255, 0.35);
      }

      &:focus {
        border-color: var(--pvms-primary, #1890ff);
        background: rgba(255, 255, 255, 0.08);
      }
    }

    /deep/ .el-input__prefix {
      color: rgba(255, 255, 255, 0.35);
    }

    /deep/ .el-input__suffix {
      color: rgba(255, 255, 255, 0.35);

      .el-input__clear {
        color: rgba(255, 255, 255, 0.35);

        &:hover {
          color: rgba(255, 255, 255, 0.65);
        }
      }
    }
  }

  // ---- Tree wrapper ----
  &__tree-wrap {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 0 4px 12px;

    // Scrollbar styling
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.12);
      border-radius: 3px;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }

    // --- el-tree dark theme overrides ---
    /deep/ .el-tree {
      background: transparent;
      color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.65));
    }

    /deep/ .el-tree-node__content {
      height: 36px;
      padding-right: 8px;
      background: transparent;
      border-radius: 4px;
      transition: background 0.2s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.06);
      }
    }

    /deep/ .el-tree-node.is-current > .el-tree-node__content {
      background: rgba(24, 144, 255, 0.15);
      color: #fff;
    }

    /deep/ .el-tree-node__expand-icon {
      color: rgba(255, 255, 255, 0.35);
      font-size: 14px;

      &.is-leaf {
        color: transparent;
      }
    }

    /deep/ .el-tree-node__children {
      overflow: visible;
    }

    /deep/ .el-tree--highlight-current
      .el-tree-node.is-current
      > .el-tree-node__content {
      background: rgba(24, 144, 255, 0.15);
      color: #fff;
    }
  }

  // ---- Custom node rendering ----
  &__node {
    display: inline-flex;
    align-items: center;
    width: calc(100% - 24px);
    overflow: hidden;
  }

  &__status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 6px;
    flex-shrink: 0;
  }

  &__node-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    line-height: 1.4;
    color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.65));
  }

  // Highlight label color when node is current
  /deep/ .el-tree-node.is-current > .el-tree-node__content {
    .station-tree-panel__node-label {
      color: #fff;
    }
  }

  // ---- Empty state ----
  &__empty {
    text-align: center;
    padding: 40px 16px;
    color: rgba(255, 255, 255, 0.3);
    font-size: 13px;
    line-height: 1.6;
  }
}
</style>
