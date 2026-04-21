<template>
  <div class="station-card-grid" data-testid="station-card-grid">
    <station-card-item
      v-for="item in stations"
      :key="item.id"
      :station="item"
      @select="$emit('select', $event)"
    />
  </div>
</template>

<script>
import StationCardItem from './StationCardItem.vue'

export default {
  name: 'StationCardGrid',
  components: {
    StationCardItem
  },
  props: {
    stations: {
      type: Array,
      default: () => []
    }
  }
}
</script>

<style lang="less" scoped>
.station-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1440px) {
  .station-card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .station-card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
