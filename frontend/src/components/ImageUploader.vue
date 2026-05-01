<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import { ElMessage, type UploadProps, type UploadRequestOptions } from 'element-plus'
import { Delete, UploadFilled } from '@element-plus/icons-vue'
import { uploadImage, type UploadFileType } from '@/api/file'
import ImagePreview from './ImagePreview.vue'

const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png']
const MAX_IMAGE_SIZE = 5 * 1024 * 1024

interface Props {
  type: UploadFileType
  title?: string
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '上传图片',
  hint: '支持 jpg/jpeg/png，单张不超过 5MB。',
})

const model = defineModel<string>({ default: '' })
const uploading = shallowRef(false)

const hasImage = computed(() => !!model.value)

const beforeUpload: UploadProps['beforeUpload'] = file => {
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    ElMessage.error('仅支持 jpg/jpeg/png 格式图片')
    return false
  }
  if (file.size > MAX_IMAGE_SIZE) {
    ElMessage.error('文件大小不能超过 5MB')
    return false
  }
  return true
}

async function uploadRequest(options: UploadRequestOptions) {
  uploading.value = true
  try {
    const url = await uploadImage(options.file, props.type)
    model.value = url
    options.onSuccess(url)
    ElMessage.success('图片上传成功')
  } catch (error) {
    const uploadError = Object.assign(
      new Error(error instanceof Error ? error.message : '图片上传失败'),
      { status: 0, method: 'POST', url: '/api/files/upload' }
    )
    options.onError(uploadError as Parameters<typeof options.onError>[0])
  } finally {
    uploading.value = false
  }
}

function clearImage() {
  model.value = ''
}
</script>

<template>
  <div class="image-uploader">
    <ImagePreview v-if="hasImage" :src="model" :title="title" compact />
    <el-upload
      class="upload-control"
      action="#"
      accept="image/jpeg,image/png"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="uploadRequest"
      :disabled="uploading"
    >
      <el-button :icon="UploadFilled" :loading="uploading">
        {{ hasImage ? '重新上传' : title }}
      </el-button>
    </el-upload>
    <el-button v-if="hasImage" text :icon="Delete" @click="clearImage">清除图片</el-button>
    <div class="upload-hint">{{ hint }}</div>
  </div>
</template>

<style scoped lang="scss">
.image-uploader {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.image-uploader :deep(.image-preview) {
  flex: 0 0 100%;
  margin-top: 0;
  margin-bottom: 0;
}

.upload-control {
  display: inline-flex;
}

.upload-hint {
  flex: 0 0 100%;
  color: var(--text-soft);
  font-size: 12px;
}
</style>
