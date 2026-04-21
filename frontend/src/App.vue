<template>
  <router-view />
</template>

<script>
export default {
  name: 'App'
}
</script>

<style lang="less">
/* ===== Global Dark Theme - AGGRESSIVE Element UI Overrides ===== */
/* 禁止使用白色的效果 — No white colors anywhere in tables */

/* --- Base table container --- */
.el-table,
.el-table__header-wrapper,
.el-table__body-wrapper,
.el-table__footer-wrapper {
  background: transparent !important;
  background-color: transparent !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

/* --- ALL table cells (td and th) via .el-table__cell --- */
.el-table .el-table__cell,
.el-table td,
.el-table th,
.el-table td.el-table__cell,
.el-table th.el-table__cell,
.el-table__body td,
.el-table__body td.el-table__cell,
.el-table__body-wrapper td,
.el-table__body-wrapper td.el-table__cell {
  background: transparent !important;
  background-color: transparent !important;
  color: rgba(255, 255, 255, 0.85) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06) !important;
}

/* --- Header cells --- */
.el-table th,
.el-table th.el-table__cell,
.el-table__header th,
.el-table__header th.el-table__cell,
.el-table__header-wrapper th,
.el-table__header-wrapper th.el-table__cell,
.el-table thead th,
.el-table .el-table__header-wrapper .el-table__header th {
  background-color: rgba(255, 255, 255, 0.04) !important;
  color: rgba(255, 255, 255, 0.58) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
}

/* --- Table header cell inner content --- */
.el-table th .cell,
.el-table th.el-table__cell .cell {
  color: rgba(255, 255, 255, 0.58) !important;
}

/* --- Table body cell inner content --- */
.el-table td .cell,
.el-table td.el-table__cell .cell,
.el-table__body .cell,
.el-table__body-wrapper .cell {
  color: rgba(255, 255, 255, 0.85) !important;
}

/* --- All table rows --- */
.el-table tr,
.el-table__body tr,
.el-table__header tr {
  background: transparent !important;
  background-color: transparent !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

/* --- Striped rows --- */
.el-table .el-table__row--striped td,
.el-table .el-table__row--striped td.el-table__cell,
.el-table--striped .el-table__body tr.el-table__row--striped td,
.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: rgba(255, 255, 255, 0.02) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

/* --- Hover states (CRITICAL — prevent white flash) --- */
.el-table__body tr.hover-row > td,
.el-table__body tr.hover-row > td.el-table__cell,
.el-table__body tr:hover > td,
.el-table__body tr:hover > td.el-table__cell,
.el-table--enable-row-hover .el-table__body tr:hover > td,
.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell,
.el-table__body tr.hover-row.el-table__row--striped > td,
.el-table__body tr.hover-row.el-table__row--striped > td.el-table__cell,
.el-table__body tr.el-table__row--striped:hover > td,
.el-table__body tr.el-table__row--striped:hover > td.el-table__cell {
  background: rgba(24, 144, 255, 0.08) !important;
  background-color: rgba(24, 144, 255, 0.08) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

/* --- Hover cell inner text --- */
.el-table__body tr.hover-row > td .cell,
.el-table__body tr:hover > td .cell {
  color: rgba(255, 255, 255, 0.85) !important;
}

/* --- Selected / current row --- */
.el-table__body tr.current-row > td,
.el-table__body tr.current-row > td.el-table__cell,
.el-table__body tr.current-row > td .cell {
  background: rgba(6, 162, 153, 0.15) !important;
  color: rgba(255, 255, 255, 0.92) !important;
}

/* --- Table borders & dividers --- */
.el-table::before,
.el-table::after,
.el-table--border::after,
.el-table--border::before,
.el-table__fixed::before,
.el-table__fixed-right::before,
.el-table--border .el-table__cell {
  border-color: rgba(255, 255, 255, 0.06) !important;
}

.el-table::before,
.el-table--border::after {
  background-color: rgba(255, 255, 255, 0.06) !important;
}

.el-table--border .el-table__cell,
.el-table--border td,
.el-table--border th {
  border-right: 1px solid rgba(255, 255, 255, 0.06) !important;
}

/* --- Fixed columns (left/right sticky) --- */
.el-table__fixed,
.el-table__fixed-right,
.el-table__fixed .el-table__header-wrapper,
.el-table__fixed .el-table__body-wrapper,
.el-table__fixed-right .el-table__header-wrapper,
.el-table__fixed-right .el-table__body-wrapper {
  background: transparent !important;
  background-color: transparent !important;
}

.el-table__fixed-right-patch {
  background-color: transparent !important;
}

/* --- Empty block --- */
.el-table__empty-block,
.el-table__empty-text {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.45) !important;
}

/* --- Column resize handle --- */
.el-table .el-table__column-resize-proxy {
  border-left: 1px solid rgba(24, 144, 255, 0.5) !important;
}

/* --- Expand row --- */
.el-table__expanded-cell,
.el-table__expanded-cell[class*="cell"] {
  background-color: rgba(255, 255, 255, 0.02) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

/* --- Sort icons and filter icons --- */
.el-table .sort-caret,
.el-table .caret-wrapper {
  color: rgba(255, 255, 255, 0.35) !important;
}

.el-table .ascending .sort-caret.ascending {
  border-bottom-color: rgba(24, 144, 255, 0.85) !important;
}

.el-table .descending .sort-caret.descending {
  border-top-color: rgba(24, 144, 255, 0.85) !important;
}

/* --- Table filter panel --- */
.el-table-filter {
  background-color: #1f1f2e !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}

.el-table-filter__list-item {
  color: rgba(255, 255, 255, 0.85) !important;
}

/* --- Catch-all: any remaining descendants that might have white bg/color --- */
.el-table *,
.el-table__body-wrapper * {
  border-color: rgba(255, 255, 255, 0.06);
}

/* ===== Element UI Drawer - Dark Theme ===== */
.el-drawer {
  background-color: #1a1a2e !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

.el-drawer__header {
  color: rgba(255, 255, 255, 0.92) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
  padding: 16px 20px !important;
  margin-bottom: 0 !important;
}

.el-drawer__header span,
.el-drawer__header .el-drawer__title {
  color: rgba(255, 255, 255, 0.92) !important;
}

.el-drawer__close-btn,
.el-drawer__header .el-icon {
  color: rgba(255, 255, 255, 0.55) !important;
}

.el-drawer__close-btn:hover,
.el-drawer__header .el-icon:hover {
  color: rgba(255, 255, 255, 0.85) !important;
}

.el-drawer__body {
  color: rgba(255, 255, 255, 0.85) !important;
  background-color: #1a1a2e !important;
}

.el-drawer__footer {
  border-top: 1px solid rgba(255, 255, 255, 0.08) !important;
}

/* ===== Element UI Tag - Dark Theme ===== */
.el-tag {
  background-color: rgba(24, 144, 255, 0.15) !important;
  border-color: rgba(24, 144, 255, 0.3) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

.el-tag.el-tag--info {
  background-color: rgba(255, 255, 255, 0.08) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.75) !important;
}

.el-tag.el-tag--success {
  background-color: rgba(82, 196, 26, 0.15) !important;
  border-color: rgba(82, 196, 26, 0.3) !important;
  color: rgba(82, 196, 26, 0.9) !important;
}

.el-tag.el-tag--warning {
  background-color: rgba(250, 173, 20, 0.15) !important;
  border-color: rgba(250, 173, 20, 0.3) !important;
  color: rgba(250, 173, 20, 0.9) !important;
}

.el-tag.el-tag--danger {
  background-color: rgba(245, 34, 45, 0.15) !important;
  border-color: rgba(245, 34, 45, 0.3) !important;
  color: rgba(245, 34, 45, 0.9) !important;
}

.el-tag .el-tag__close {
  color: rgba(255, 255, 255, 0.55) !important;
}

.el-tag .el-tag__close:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}
</style>
