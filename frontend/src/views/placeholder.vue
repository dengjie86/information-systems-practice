<template>
  <div class="placeholder">
    <el-empty :description="`${title} 页面开发中...`">
      <el-button type="primary" @click="$router.push('/home')">返回工作台</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '该')
</script>

<style lang="scss" scoped>
.placeholder {
  background: #fff;
  border-radius: 12px;
  padding: 60px 0;
}
</style>
