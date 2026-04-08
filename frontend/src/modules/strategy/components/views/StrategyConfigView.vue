<template>
  <div v-loading="loading" class="strategy-config-view" data-testid="strategy-config-view">

    <!-- Strategy card list -->
    <app-section-card title="已有策略" subtitle="点击编辑可调整配置，开关可快速启用或停用策略">
      <template #extra>
        <el-button type="primary" size="mini" icon="el-icon-plus" @click="openCreateDialog">新增策略</el-button>
      </template>

      <el-empty v-if="!candidateRows.length" description="暂无策略，点击右上角新增" :image-size="80" />

      <div v-else class="scard-grid">
        <div
          v-for="item in pagedItems"
          :key="item.id"
          class="scard"
          :class="'scard--' + item.status"
        >
          <!-- Card header: type badge + enable switch -->
          <div class="scard__head">
            <el-tag size="mini" :type="statusTagType(item.status)" class="scard__status-tag">{{ item.statusLabel }}</el-tag>
            <el-switch
              :value="isEnabled(item)"
              :active-color="'#06a299'"
              :inactive-color="'rgba(255,255,255,0.15)'"
              :title="isEnabled(item) ? '点击停用' : '点击启用'"
              @change="handleToggle(item, $event)"
            />
          </div>

          <!-- Strategy name -->
          <div class="scard__name" :title="item.name">{{ item.name }}</div>

          <!-- Type + station -->
          <div class="scard__meta">
            <span class="scard__type">{{ item.typeLabel }}</span>
            <span class="scard__dot">·</span>
            <span class="scard__station" :title="item.stationName">{{ item.stationName }}</span>
          </div>

          <!-- Time range -->
          <div class="scard__time">
            <i class="el-icon-time" />
            {{ formatTime(item.startTime) }} ~ {{ formatTime(item.endTime) }}
          </div>

          <!-- Revenue stats -->
          <div class="scard__revenue-row">
            <div class="scard__revenue-item">
              <span class="scard__revenue-label">当日收益</span>
              <span class="scard__revenue-val">{{ item.todayRevenue }} <small>¥</small></span>
            </div>
            <div class="scard__revenue-item">
              <span class="scard__revenue-label">预估收益</span>
              <span class="scard__revenue-val">{{ item.estimatedRevenue }} <small>¥</small></span>
            </div>
            <div class="scard__revenue-item">
              <span class="scard__revenue-label">成功概率</span>
              <span class="scard__revenue-val">{{ item.successProbability }} <small>%</small></span>
            </div>
          </div>

          <!-- Actions -->
          <div class="scard__actions">
            <el-button size="mini" plain icon="el-icon-edit" @click="openEditDialog(item)">编辑</el-button>
            <el-button
              size="mini"
              plain
              icon="el-icon-delete"
              style="color:#ef476f;border-color:rgba(239,71,111,0.4);"
              @click="handleDelete(item)"
            >删除</el-button>
          </div>
        </div>
      </div>

      <el-pagination
        v-if="candidateRows.length > pageSize"
        small
        background
        layout="total, prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="candidateRows.length"
        style="margin-top: 16px; text-align: right;"
        @current-change="currentPage = $event"
      />
    </app-section-card>

    <!-- Config Dialog -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="1060px"
      :close-on-click-modal="false"
      custom-class="sconfig-dialog"
      @close="onDialogClose"
    >
      <div class="sconfig-layout">

        <!-- Left: basic info + time-slot table -->
        <div class="sconfig-left">
          <el-form :inline="true" label-width="72px" size="small" class="sconfig-basic-form">
            <el-form-item label="策略名称">
              <el-input v-model.trim="form.name" placeholder="请输入策略名称" style="width:170px;" />
            </el-form-item>
            <el-form-item label="策略类型">
              <el-select v-model="form.type" style="width:145px;">
                <el-option
                  v-for="t in meta.typeOptions"
                  :key="t.value"
                  :label="t.label"
                  :value="t.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="电站">
              <el-select
                v-model="form.stationId"
                filterable
                placeholder="请选择电站"
                style="width:170px;"
                @change="onStationChange"
              >
                <el-option
                  v-for="s in meta.stations"
                  :key="s.id"
                  :label="s.name"
                  :value="s.id"
                />
              </el-select>
            </el-form-item>
          </el-form>

          <!-- Slot config header -->
          <div class="sconfig-slot-header">
            <div>
              <span class="sconfig-slot-title">策略配置</span>
              <span class="sconfig-slot-hint">（各时段时间不允许交叉，待机时功率自动为 0）</span>
            </div>
            <div class="sconfig-slot-btns">
              <el-button size="mini" plain icon="el-icon-magic-stick" @click="handleBatchFill">批量设置</el-button>
              <el-button size="mini" plain icon="el-icon-refresh-left" @click="resetSlots">重置</el-button>
            </div>
          </div>

          <!-- Revenue result banner -->
          <div v-if="revenueResult !== null" class="sconfig-revenue-banner">
            <i class="el-icon-s-finance" />
            峰合套利预计收益：<strong>{{ revenueResult }} 元</strong>
            <span class="sconfig-revenue-range">
              区间：{{ revenueRange.low }} ~ {{ revenueRange.high }} 元
            </span>
            <span class="sconfig-revenue-prob">成功概率：{{ revenueProbability }}%</span>
          </div>

          <!-- Time-slot table -->
          <el-table
            :data="slots"
            size="mini"
            stripe
            border
            class="sconfig-slot-table"
          >
            <el-table-column label="序" width="40" align="center">
              <template slot-scope="{ row }">
                <span :class="{ 'seq--active': isSlotFilled(row) }">{{ row.id }}</span>
              </template>
            </el-table-column>

            <el-table-column label="起始时间" min-width="196">
              <template slot-scope="{ row }">
                <div class="sconfig-time-range">
                  <el-time-picker
                    v-model="row.startTime"
                    format="HH:mm"
                    value-format="HH:mm"
                    size="mini"
                    placeholder="开始"
                    :clearable="true"
                    style="width:80px;"
                  />
                  <span class="tsep">至</span>
                  <el-time-picker
                    v-model="row.endTime"
                    format="HH:mm"
                    value-format="HH:mm"
                    size="mini"
                    placeholder="结束"
                    :clearable="true"
                    style="width:80px;"
                  />
                </div>
              </template>
            </el-table-column>

            <el-table-column label="状态" min-width="135">
              <template slot-scope="{ row }">
                <el-select
                  v-model="row.action"
                  size="mini"
                  placeholder="充放电状态"
                  clearable
                  style="width:115px;"
                  @change="onActionChange(row)"
                >
                  <el-option label="充电" value="charge" />
                  <el-option label="放电" value="discharge" />
                  <el-option label="待机" value="standby" />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="功率(kW)" min-width="105">
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.powerKw"
                  size="mini"
                  :min="0"
                  :step="50"
                  controls-position="right"
                  style="width:90px;"
                  :disabled="row.action === 'standby'"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Right: electricity price reference -->
        <div class="sconfig-price-col">
          <div class="sconfig-price-header">
            <span class="sconfig-price-title">电价参考</span>
            <el-date-picker
              v-model="priceDate"
              type="date"
              size="mini"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              style="width:122px;"
            />
          </div>

          <div class="sconfig-price-rates">
            <span class="rate-tag rate--peak">峰：{{ peakPrice }} 元</span>
            <span class="rate-tag rate--flat">平：{{ flatPrice }} 元</span>
            <span class="rate-tag rate--valley">谷：{{ valleyPrice }} 元</span>
          </div>

          <!-- 48-cell grid: 8 per row × 6 rows -->
          <div class="sconfig-price-grid">
            <div
              v-for="(cell, idx) in priceGrid"
              :key="idx"
              class="sconfig-price-cell"
              :class="'cell--' + cell.type"
              :title="cell.time + '  ' + cell.price + ' ¥/kWh'"
            >{{ cell.time }}</div>
          </div>

          <div class="sconfig-price-legend">
            <span class="legend-item"><i class="legend-dot dot--peak" />峰段</span>
            <span class="legend-item"><i class="legend-dot dot--flat" />平段</span>
            <span class="legend-item"><i class="legend-dot dot--valley" />谷段</span>
          </div>

          <!-- Price bar chart -->
          <div class="sconfig-pricechart-label">电价元/kWh</div>
          <div class="sconfig-pricechart">
            <div class="sconfig-pricechart-bars">
              <div
                v-for="(cell, idx) in priceGrid"
                :key="idx"
                class="pbar"
                :class="'cell--' + cell.type"
                :style="{ height: priceBarHeight(cell.price) + 'px' }"
              />
            </div>
            <div class="sconfig-pricechart-axis">
              <span>00:00</span><span>06:00</span><span>12:00</span><span>18:00</span><span>24:00</span>
            </div>
          </div>
        </div>
      </div>

      <div slot="footer" class="sconfig-dialog-footer">
        <span v-if="revenueResult !== null" class="sconfig-footer-revenue">
          预计收益 <strong>{{ revenueResult }}</strong> 元
        </span>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          plain
          icon="el-icon-s-finance"
          :loading="calcLoading"
          @click="handleCalcRevenue"
        >收益计算</el-button>
        <el-button
          type="success"
          icon="el-icon-check"
          :loading="saveLoading"
          @click="handleSave"
        >保存策略</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'
import {
  fetchStrategyElectricityPrice,
  simulateStrategy,
  createStrategy,
  submitStrategy,
  terminateStrategy,
  batchDeleteStrategy
} from '@/api/pvms'

function buildDefaultSlots() {
  return Array.from({ length: 10 }, function (_, i) {
    return { id: i + 1, startTime: null, endTime: null, action: '', powerKw: null }
  })
}

var STATUS_TAG = {
  draft: 'info',
  pending: 'warning',
  executing: 'success',
  completed: '',
  terminated: 'danger'
}

export default {
  name: 'StrategyConfigView',
  components: { AppSectionCard },
  props: {
    viewData: { type: Object, default: function () { return {} } },
    meta: { type: Object, default: function () { return {} } },
    query: { type: Object, default: function () { return {} } },
    loading: { type: Boolean, default: false }
  },
  data() {
    return {
      // Card pagination
      currentPage: 1,
      pageSize: 9,

      // Dialog state
      dialogVisible: false,
      dialogTitle: '新增策略',
      calcLoading: false,
      saveLoading: false,

      // Form inside dialog
      form: { stationId: '', name: '', type: 'peak-shaving' },
      slots: buildDefaultSlots(),
      localPrice: { periods: [] },
      priceDate: '2026-04-08',
      revenueResult: null,
      revenueRange: { low: 0, high: 0 },
      revenueProbability: 0
    }
  },
  computed: {
    candidateRows() {
      return (this.viewData.list && this.viewData.list.items) || []
    },
    pagedItems() {
      var start = (this.currentPage - 1) * this.pageSize
      return this.candidateRows.slice(start, start + this.pageSize)
    },
    pricePeriods() { return this.localPrice.periods || [] },
    peakPrice() {
      var p = this.pricePeriods.find(function (x) { return x.priceType === 'peak' })
      return p ? p.price : '--'
    },
    flatPrice() {
      var p = this.pricePeriods.find(function (x) { return x.priceType === 'flat' })
      return p ? p.price : '--'
    },
    valleyPrice() {
      var p = this.pricePeriods.find(function (x) { return x.priceType === 'valley' })
      return p ? p.price : '--'
    },
    priceGrid() {
      var periods = this.pricePeriods
      return Array.from({ length: 48 }, function (_, i) {
        var h = Math.floor(i / 2)
        var m = (i % 2) * 30
        var time = String(h).padStart(2, '0') + ':' + (m === 0 ? '00' : '30')
        var slot = i * 2
        var period = periods.find(function (p) { return p.startSlot <= slot && slot <= p.endSlot })
        return { time: time, type: period ? period.priceType : 'flat', price: period ? period.price : 0 }
      })
    },
    maxPrice() {
      var vals = this.pricePeriods.map(function (p) { return p.price || 0 })
      return vals.length ? Math.max.apply(null, vals) : 1
    }
  },
  watch: {
    query: {
      immediate: true,
      deep: true,
      handler(value) {
        if (value.stationId && !this.form.stationId) this.form.stationId = value.stationId
        if (value.type && value.type !== this.form.type) this.form.type = value.type
      }
    },
    viewData: {
      immediate: true,
      deep: true,
      handler(value) {
        if (value && value.electricityPrice && value.electricityPrice.periods && value.electricityPrice.periods.length) {
          this.localPrice = value.electricityPrice
        }
      }
    }
  },
  methods: {
    // ===== Card helpers =====
    isEnabled(item) { return item.status === 'executing' || item.status === 'pending' },
    statusTagType(status) { return STATUS_TAG[status] || 'info' },
    formatTime(dt) {
      if (!dt) return '--'
      if (typeof dt === 'string' && dt.includes('T')) return dt.split('T')[1].slice(0, 5)
      return String(dt).slice(0, 5)
    },

    // ===== Toggle (enable/disable) =====
    async handleToggle(item, enabled) {
      try {
        if (enabled) {
          await submitStrategy({ id: item.id })
          this.$message.success('策略已启用')
        } else {
          await terminateStrategy({ id: item.id })
          this.$message.success('策略已停用')
        }
        this.$emit('refresh')
      } catch (e) {
        this.$message.error('操作失败')
      }
    },

    // ===== Delete =====
    async handleDelete(item) {
      try {
        await this.$confirm('确认删除策略 "' + item.name + '"？', '删除确认', {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await batchDeleteStrategy({ ids: [item.id] })
        this.$message.success('策略已删除')
        this.$emit('refresh')
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败')
      }
    },

    // ===== Dialog =====
    openCreateDialog() {
      this.dialogTitle = '新增策略'
      this.form = {
        stationId: this.query.stationId || (this.meta.stations && this.meta.stations[0] && this.meta.stations[0].id) || '',
        name: '',
        type: this.form.type || 'peak-shaving'
      }
      this.slots = buildDefaultSlots()
      this.revenueResult = null
      this.dialogVisible = true
      if (this.form.stationId) this.loadPrice(this.form.stationId)
    },
    openEditDialog(item) {
      this.dialogTitle = '编辑策略 — ' + item.name
      this.form = {
        stationId: item.stationId || '',
        name: item.name || '',
        type: item.type || 'peak-shaving'
      }
      this.slots = buildDefaultSlots()
      this.revenueResult = null
      this.dialogVisible = true
      if (item.stationId) this.loadPrice(item.stationId)
    },
    onDialogClose() {
      this.revenueResult = null
      this.calcLoading = false
      this.saveLoading = false
    },

    // ===== Slot helpers =====
    isSlotFilled(row) { return !!(row.startTime && row.endTime && row.action) },
    onActionChange(row) {
      if (row.action === 'standby') row.powerKw = null
    },
    resetSlots() { this.slots = buildDefaultSlots() },
    handleBatchFill() {
      var pattern = [
        { startTime: '00:00', endTime: '08:00', action: 'charge', powerKw: 100 },
        { startTime: '10:30', endTime: '12:00', action: 'discharge', powerKw: 150 },
        { startTime: '12:00', endTime: '14:00', action: 'standby', powerKw: null },
        { startTime: '14:01', endTime: '19:00', action: 'discharge', powerKw: 150 }
      ]
      pattern.forEach(function (p, i) {
        Object.assign(this.slots[i], p)
      }, this)
      this.$message.success('已填入峰谷套利模板')
    },

    // ===== Price =====
    async loadPrice(stationId) {
      try {
        var res = await fetchStrategyElectricityPrice({ stationId: stationId })
        if (res.data && res.data.periods) this.localPrice = res.data
      } catch (e) { /* silent */ }
    },
    async onStationChange(stationId) { await this.loadPrice(stationId) },
    priceBarHeight(price) {
      if (!price || !this.maxPrice) return 4
      return Math.max(4, Math.round((price / this.maxPrice) * 56))
    },

    // ===== Build API payload =====
    buildPayload() {
      var filled = this.slots.filter(this.isSlotFilled)
      var startTime = filled.length ? '2026-03-30T' + filled[0].startTime + ':00' : '2026-03-30T08:00:00'
      var endTime = filled.length ? '2026-03-30T' + filled[filled.length - 1].endTime + ':00' : '2026-03-30T18:00:00'
      var maxPower = filled.reduce(function (acc, s) { return Math.max(acc, s.powerKw || 0) }, 800)
      var stationId = this.form.stationId || (this.meta.stations && this.meta.stations[0] && this.meta.stations[0].id) || ''
      return {
        stationId: stationId,
        type: this.form.type,
        name: this.form.name || this.form.type + ' Strategy',
        targetPowerKw: maxPower || 800,
        startTime: startTime,
        endTime: endTime,
        remark: ''
      }
    },

    // ===== Revenue calculation (real backend) =====
    async handleCalcRevenue() {
      if (!this.form.stationId) { this.$message.warning('请先选择电站'); return }
      this.calcLoading = true
      try {
        var res = await simulateStrategy(this.buildPayload())
        var data = res.data || {}
        this.revenueResult = data.estimatedRevenue !== undefined ? data.estimatedRevenue : '--'
        this.revenueRange = data.confidenceRange || { low: 0, high: 0 }
        this.revenueProbability = data.successProbability || 0
        this.$message.success('收益计算完成')
      } catch (e) {
        this.$message.error('收益计算失败')
      } finally {
        this.calcLoading = false
      }
    },

    // ===== Save strategy =====
    async handleSave() {
      if (!this.form.stationId) { this.$message.warning('请先选择电站'); return }
      this.saveLoading = true
      try {
        await createStrategy(this.buildPayload())
        this.$message.success('策略已保存')
        this.dialogVisible = false
        this.$emit('refresh')
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.saveLoading = false
      }
    }
  }
}
</script>

<style lang="less" scoped>
.strategy-config-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== Strategy card grid ===== */
.scard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;

  @media (max-width: 1400px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 900px)  { grid-template-columns: 1fr; }
}

.scard {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(160deg, rgba(14, 49, 106, 0.92), rgba(9, 29, 67, 0.96));
  transition: box-shadow 0.2s, border-color 0.2s;
  cursor: default;

  &:hover {
    border-color: rgba(255, 255, 255, 0.16);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  }

  &--executing {
    border-color: rgba(6, 162, 153, 0.4);
    background: linear-gradient(160deg, rgba(14, 60, 80, 0.92), rgba(9, 29, 67, 0.96));
  }

  &--pending { border-color: rgba(255, 209, 102, 0.3); }
  &--terminated { border-color: rgba(239, 71, 111, 0.2); opacity: 0.75; }
}

.scard__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.scard__status-tag {
  font-size: 10px;
}

.scard__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--pvms-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.scard__meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--pvms-text-muted);
  overflow: hidden;
}

.scard__type { color: #1a8dff; flex-shrink: 0; }
.scard__dot { opacity: 0.4; }
.scard__station {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.scard__time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  i { margin-right: 4px; }
}

.scard__revenue-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.scard__revenue-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.scard__revenue-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.45);
}

.scard__revenue-val {
  font-size: 14px;
  font-weight: 600;
  color: #06d6a0;
  small { font-size: 10px; font-weight: 400; }
}

.scard__actions {
  display: flex;
  gap: 6px;
  padding-top: 6px;
}


/* ===== Dialog ===== */
.sconfig-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
  max-height: 72vh;
  overflow: hidden;
}

.sconfig-left {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  padding-right: 4px;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.12); border-radius: 2px; }
}

/* Basic form */
.sconfig-basic-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;

  /deep/ .el-form-item {
    margin-bottom: 0;
    margin-right: 6px;
  }

  /deep/ .el-form-item__label {
    color: var(--pvms-text-muted);
    font-size: 12px;
  }

  /deep/ .el-input__inner {
    background: rgba(255, 255, 255, 0.06);
    border-color: rgba(255, 255, 255, 0.14);
    color: var(--pvms-text-primary);
  }
}

/* Slot header */
.sconfig-slot-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 6px;
}

.sconfig-slot-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--pvms-text-primary);
}

.sconfig-slot-hint {
  font-size: 11px;
  color: var(--pvms-text-muted);
}

.sconfig-slot-btns {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

/* Revenue banner */
.sconfig-revenue-banner {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(6, 214, 160, 0.1);
  border: 1px solid rgba(6, 214, 160, 0.25);
  border-radius: 4px;
  font-size: 13px;
  color: var(--pvms-text-primary);

  i { color: #06d6a0; margin-right: 4px; }

  strong {
    color: #06d6a0;
    font-size: 16px;
    margin: 0 2px;
  }
}

.sconfig-revenue-range,
.sconfig-revenue-prob {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}

/* Slot table */
.sconfig-slot-table {
  /deep/ .el-table {
    background: transparent;
    color: var(--pvms-text-secondary);
    font-size: 12px;

    &::before { background: rgba(255, 255, 255, 0.06); }
    th { background: rgba(255, 255, 255, 0.04); color: rgba(255, 255, 255, 0.5); border-color: rgba(255, 255, 255, 0.08); }
    td { border-color: rgba(255, 255, 255, 0.06); }
    .el-table__row--striped td { background: rgba(255, 255, 255, 0.02); }
  }

  /deep/ .el-input__inner,
  /deep/ .el-input-number .el-input__inner {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.12);
    color: var(--pvms-text-primary);
    height: 28px;
    line-height: 28px;
  }

  /deep/ .el-input-number__decrease,
  /deep/ .el-input-number__increase {
    background: rgba(255, 255, 255, 0.04);
    border-color: rgba(255, 255, 255, 0.1);
    color: rgba(255, 255, 255, 0.6);
  }
}

.sconfig-time-range {
  display: flex;
  align-items: center;
  gap: 3px;
}

.tsep {
  font-size: 11px;
  color: var(--pvms-text-muted);
  flex-shrink: 0;
}

.seq--active { color: #06d6a0; font-weight: 600; }

/* Right price column */
.sconfig-price-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-left: 1px solid rgba(255, 255, 255, 0.07);
  padding-left: 18px;
  overflow-y: auto;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.12); border-radius: 2px; }
}

.sconfig-price-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sconfig-price-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--pvms-text-primary);
}

.sconfig-price-rates {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.rate-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 3px 8px;
  border-radius: 3px;

  &.rate--peak { background: rgba(239, 71, 111, 0.2); color: #ef476f; border: 1px solid rgba(239, 71, 111, 0.35); }
  &.rate--flat { background: rgba(255, 209, 102, 0.18); color: #ffd166; border: 1px solid rgba(255, 209, 102, 0.3); }
  &.rate--valley { background: rgba(6, 214, 160, 0.15); color: #06d6a0; border: 1px solid rgba(6, 214, 160, 0.3); }
}

/* 48-cell price grid */
.sconfig-price-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 2px;
}

.sconfig-price-cell {
  font-size: 9px;
  text-align: center;
  padding: 4px 1px;
  border-radius: 2px;
  cursor: default;
  line-height: 1.2;

  &.cell--peak   { background: rgba(239, 71, 111, 0.35); color: #faa; border: 1px solid rgba(239, 71, 111, 0.5); }
  &.cell--flat   { background: rgba(255, 209, 102, 0.25); color: #ffd166; border: 1px solid rgba(255, 209, 102, 0.4); }
  &.cell--valley { background: rgba(6, 214, 160, 0.2); color: #06d6a0; border: 1px solid rgba(6, 214, 160, 0.35); }
}

.sconfig-price-legend {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: var(--pvms-text-muted);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 2px;

  &.dot--peak   { background: #ef476f; }
  &.dot--flat   { background: #ffd166; }
  &.dot--valley { background: #06d6a0; }
}

/* Price bar chart */
.sconfig-pricechart-label {
  font-size: 11px;
  color: var(--pvms-text-muted);
}

.sconfig-pricechart {
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sconfig-pricechart-bars {
  display: flex;
  align-items: flex-end;
  height: 60px;
  gap: 1px;
  padding: 0 3px;
}

.pbar {
  flex: 1;
  border-radius: 1px 1px 0 0;
  min-width: 1px;

  &.cell--peak   { background: rgba(239, 71, 111, 0.7); }
  &.cell--flat   { background: rgba(255, 209, 102, 0.6); }
  &.cell--valley { background: rgba(6, 214, 160, 0.5); }
}

.sconfig-pricechart-axis {
  display: flex;
  justify-content: space-between;
  padding: 2px 3px 0;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.3);
}

/* Dialog footer */
.sconfig-dialog-footer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sconfig-footer-revenue {
  flex: 1;
  font-size: 13px;
  color: var(--pvms-text-muted);

  strong {
    color: #06d6a0;
    font-size: 15px;
    margin: 0 2px;
  }
}
</style>

<!-- Dialog dark theme (not scoped — targets portal-rendered el-dialog) -->
<style lang="less">
.sconfig-dialog {
  background: rgba(9, 29, 67, 0.98) !important;
  border: 1px solid rgba(255, 255, 255, 0.1);

  .el-dialog__header {
    background: rgba(255, 255, 255, 0.03);
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    padding: 14px 20px;

    .el-dialog__title {
      color: rgba(255, 255, 255, 0.92);
      font-size: 15px;
      font-weight: 600;
    }

    .el-dialog__headerbtn .el-dialog__close {
      color: rgba(255, 255, 255, 0.55);
      &:hover { color: #fff; }
    }
  }

  .el-dialog__body {
    padding: 16px 20px;
    background: transparent;
  }

  .el-dialog__footer {
    border-top: 1px solid rgba(255, 255, 255, 0.08);
    padding: 12px 20px;
    background: rgba(255, 255, 255, 0.02);
  }

  // Input dark styles
  .el-input__inner,
  .el-textarea__inner {
    background: rgba(255, 255, 255, 0.06) !important;
    border-color: rgba(255, 255, 255, 0.14) !important;
    color: rgba(255, 255, 255, 0.9) !important;

    &::placeholder { color: rgba(255, 255, 255, 0.3) !important; }
  }

  .el-select-dropdown {
    background: rgba(9, 29, 67, 0.98);
    border-color: rgba(255, 255, 255, 0.12);

    .el-select-dropdown__item {
      color: rgba(255, 255, 255, 0.75);
      &.hover, &:hover { background: rgba(255, 255, 255, 0.06); }
      &.selected { color: #06a299; }
    }
  }

  .el-date-editor .el-input__inner {
    background: rgba(255, 255, 255, 0.06) !important;
  }
}
</style>
