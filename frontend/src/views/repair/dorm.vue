<template>
  <div class="page">
    <section class="page-head">
      <div>
        <div class="crumb">Student Profile</div>
        <h1>宿舍信息</h1>
        <p>报修工单会自动带入宿舍和联系电话。</p>
      </div>
      <el-button type="primary" :icon="Check" :loading="submitting" @click="onSave">保存信息</el-button>
    </section>

    <section class="panel">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="宿舍楼" prop="dormBuilding">
            <el-input v-model.trim="form.dormBuilding" placeholder="例如：东 3 栋" maxlength="30" />
          </el-form-item>
          <el-form-item label="房间号" prop="dormRoom">
            <el-input v-model.trim="form.dormRoom" placeholder="例如：406" maxlength="30" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model.trim="form.phone" placeholder="维修人员联系用" maxlength="20" />
          </el-form-item>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getCurrentUser, updateDorm } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  dormBuilding: userStore.userInfo?.dormBuilding ?? '',
  dormRoom: userStore.userInfo?.dormRoom ?? '',
  phone: userStore.userInfo?.phone ?? '',
})

const rules: FormRules = {
  dormBuilding: [{ required: true, message: '请填写宿舍楼', trigger: 'blur' }],
  dormRoom: [{ required: true, message: '请填写房间号', trigger: 'blur' }],
  phone: [{ min: 6, max: 20, message: '联系电话长度不正确', trigger: 'blur' }],
}

const onSave = async () => {
  if (submitting.value) return
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  await updateDorm(form).finally(() => { submitting.value = false })
  const user = await getCurrentUser()
  userStore.setUserInfo(user)
  ElMessage.success('宿舍信息已更新')
}
</script>

<style scoped lang="scss">
.page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;

  .crumb {
    font-size: 12px;
    color: var(--text-soft);
    font-weight: 600;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    margin-bottom: 6px;
  }

  h1 {
    margin: 0 0 4px;
    font-size: 26px;
    font-weight: 600;
    color: var(--text);
  }

  p {
    margin: 0;
    color: var(--text-muted);
    font-size: 13px;
  }
}

.panel {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 22px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

:deep(.el-button--primary:not(.is-disabled)) {
  background: var(--text);
  border-color: var(--text);
}

@media (max-width: 860px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
