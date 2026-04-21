<template>
  <div class="placeholder">
    <el-empty :description="`${currentTitle} 页面开发中...`">
      <el-button type="primary" @click="$router.push('/home')">返回工作台</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const titleMap: Record<string, string> = {
  '/order': '工单管理',
  '/user': '用户管理',
  '/stats': '统计分析'
}
const currentTitle = computed(() => titleMap[route.path] || '该')
</script>

<style lang="scss" scoped>
.placeholder {
  background: #fff;
  border-radius: 12px;
  padding: 60px 0;
}
</style>
