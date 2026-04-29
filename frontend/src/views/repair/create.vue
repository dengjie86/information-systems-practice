<template>
  <div class="page">
    <section class="page-head">
      <div>
        <div class="crumb">New Repair</div>
        <h1>报修申请</h1>
        <p>提交后进入待审核状态，管理员会继续分派维修人员。</p>
      </div>
      <el-button :icon="Tickets" @click="router.push('/repair/list')">查看我的工单</el-button>
    </section>

    <div class="grid">
      <section class="panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="报修标题" prop="title">
            <el-input v-model.trim="form.title" placeholder="例如：宿舍空调漏水" maxlength="100" show-word-limit />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="故障分类" prop="categoryId">
              <el-select v-model="form.categoryId" filterable placeholder="选择分类" :loading="categoryLoading">
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.categoryName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="紧急程度" prop="priority">
              <el-segmented v-model="form.priority" :options="priorityOptions" />
            </el-form-item>
          </div>

          <el-form-item label="问题描述" prop="description">
            <el-input
              v-model.trim="form.description"
              type="textarea"
              :rows="6"
              maxlength="2000"
              show-word-limit
              placeholder="写清楚故障位置、现象、是否影响使用。"
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="故障图片地址" prop="imageUrl">
              <el-input v-model.trim="form.imageUrl" placeholder="第 16 次提交接入真实上传，这里可先填图片 URL" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model.trim="form.contactPhone" placeholder="默认使用个人资料中的手机号" maxlength="20" />
            </el-form-item>
          </div>

          <div class="actions">
            <el-button @click="onReset">清空</el-button>
            <el-button type="primary" :loading="submitting" @click="onSubmit">提交报修</el-button>
          </div>
        </el-form>
      </section>

      <aside class="side">
        <div class="info-card">
          <div class="info-label">当前宿舍</div>
          <div class="dorm">{{ dormText }}</div>
          <div class="phone">{{ userStore.userInfo?.phone || '未填写联系电话' }}</div>
          <el-button text :icon="EditPen" @click="router.push('/repair/dorm')">维护宿舍信息</el-button>
        </div>

        <div class="flow">
          <div v-for="item in flow" :key="item" class="flow-item">
            <span />
            {{ item }}
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { EditPen, Tickets } from '@element-plus/icons-vue'
import { createOrder, getCategories, type Priority, type RepairCategory } from '@/api/repair'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const categoryLoading = ref(false)
const categories = ref<RepairCategory[]>([])

const form = reactive({
  title: '',
  categoryId: null as number | null,
  description: '',
  imageUrl: '',
  contactPhone: userStore.userInfo?.phone ?? '',
  priority: 'NORMAL' as Priority,
})

const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '普通', value: 'NORMAL' },
  { label: '高', value: 'HIGH' },
  { label: '紧急', value: 'URGENT' },
]

const rules: FormRules = {
  title: [{ required: true, message: '请填写报修标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择故障分类', trigger: 'change' }],
  description: [{ max: 2000, message: '问题描述不能超过 2000 字', trigger: 'blur' }],
  imageUrl: [{ max: 500, message: '图片地址不能超过 500 字', trigger: 'blur' }],
  contactPhone: [{ max: 20, message: '联系电话不能超过 20 字', trigger: 'blur' }],
}

const flow = ['提交报修', '管理员审核', '分派维修', '上门处理', '学生确认']

const dormText = computed(() => {
  const building = userStore.userInfo?.dormBuilding
  const room = userStore.userInfo?.dormRoom
  if (!building || !room) return '宿舍信息未完善'
  return `${building} ${room}`
})

const loadCategories = async () => {
  categoryLoading.value = true
  categories.value = await getCategories().finally(() => { categoryLoading.value = false })
}

const onReset = () => {
  formRef.value?.resetFields()
  form.imageUrl = ''
  form.contactPhone = userStore.userInfo?.phone ?? ''
  form.priority = 'NORMAL'
}

const onSubmit = async () => {
  if (submitting.value) return
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  const res = await createOrder(form).finally(() => { submitting.value = false })
  ElMessage.success(`工单 ${res.orderNo} 已提交`)
  router.push('/repair/list')
}

onMounted(loadCategories)
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

.grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
}

.panel,
.info-card,
.flow {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
}

.panel {
  padding: 22px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.side {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-card {
  padding: 18px;

  .info-label {
    color: var(--text-soft);
    font-size: 12px;
    margin-bottom: 8px;
  }

  .dorm {
    color: var(--text);
    font-size: 20px;
    font-weight: 600;
  }

  .phone {
    color: var(--text-muted);
    font-size: 13px;
    margin: 4px 0 12px;
  }
}

.flow {
  padding: 10px 18px;
}

.flow-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  color: var(--text-muted);
  font-size: 13px;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--text);
    box-shadow: 0 0 0 5px var(--bg-muted);
  }
}

:deep(.el-select),
:deep(.el-segmented) {
  width: 100%;
}

:deep(.el-button--primary:not(.is-disabled)) {
  background: var(--text);
  border-color: var(--text);
}

@media (max-width: 980px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page-head,
  .form-row {
    align-items: flex-start;
    grid-template-columns: 1fr;
  }

  .page-head {
    flex-direction: column;
  }
}
</style>
