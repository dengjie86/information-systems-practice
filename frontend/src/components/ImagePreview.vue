<script setup lang="ts">
interface Props {
  src?: string
  title?: string
  compact?: boolean
}

withDefaults(defineProps<Props>(), {
  title: '图片',
  compact: false,
})
</script>

<template>
  <div v-if="src" class="image-preview" :class="{ compact }">
    <div class="preview-head">
      <span>{{ title }}</span>
      <el-link :href="src" target="_blank" underline="never">打开原图</el-link>
    </div>
    <el-image
      class="preview-image"
      :src="src"
      :alt="title"
      :preview-src-list="[src]"
      fit="cover"
      preview-teleported
    >
      <template #error>
        <div class="preview-error">图片无法加载</div>
      </template>
    </el-image>
  </div>
</template>

<style scoped lang="scss">
.image-preview {
  margin-top: 16px;
  margin-bottom: 12px;
  padding: 12px;
  border: 1px solid var(--border-soft);
  border-radius: 10px;
  background: var(--bg-subtle);
}

.preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  font-size: 13px;

  span {
    color: var(--text-soft);
  }
}

.preview-image {
  display: block;
  width: 100%;
  height: 220px;
  border-radius: 8px;
  border: 1px solid var(--border-soft);
  background: var(--bg);
}

.preview-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--text-soft);
  font-size: 13px;
}

.compact {
  .preview-image {
    height: 150px;
  }
}
</style>
