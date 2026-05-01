<script setup lang="ts">
import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, EditPen, Select, Close } from '@element-plus/icons-vue'
import {
  cancelOrder,
  confirmOrder,
  editOrder,
  getCategories,
  getOrderDetail,
  type CreateOrderParams,
  type EvaluateOrderParams,
  type RepairCategory,
  type RepairOrder,
} from '@/api/repair'
import ImagePreview from '@/components/ImagePreview.vue'
import ImageUploader from '@/components/ImageUploader.vue'
import { formatTime, priorityMap, statusClass, statusMap } from './meta'

const route = useRoute()
const router = useRouter()

const loading = shallowRef(false)
const submitting = shallowRef(false)
const categoryLoading = shallowRef(false)
const editDialogOpen = shallowRef(false)
const evaluateDialogOpen = shallowRef(false)

const detail = ref<RepairOrder | null>(null)
const categories = ref<RepairCategory[]>([])
const editFormRef = ref<FormInstance>()
const evaluateFormRef = ref<FormInstance>()

const editForm = reactive<CreateOrderParams>({
  title: '',
  categoryId: null,
  description: '',
  imageUrl: '',
  contactPhone: '',
  priority: 'NORMAL',
})

const evaluateForm = reactive<EvaluateOrderParams>({
  score: 5,
  content: '',
})

const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '普通', value: 'NORMAL' },
  { label: '高', value: 'HIGH' },
  { label: '紧急', value: 'URGENT' },
]

const evaluationOptions = [
  { label: '差评', value: 1 },
  { label: '中评', value: 3 },
  { label: '好评', value: 5 },
]

const editRules: FormRules = {
  title: [{ required: true, message: '请填写报修标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择故障分类', trigger: 'change' }],
  description: [{ max: 2000, message: '问题描述不能超过 2000 字', trigger: 'blur' }],
  imageUrl: [{ max: 500, message: '图片地址不能超过 500 字', trigger: 'blur' }],
  contactPhone: [{ max: 20, message: '联系电话不能超过 20 字', trigger: 'blur' }],
}

const evaluateRules: FormRules = {
  score: [{ required: true, message: '请选择评价等级', trigger: 'change' }],
  content: [{ max: 500, message: '评价内容不能超过 500 字', trigger: 'blur' }],
}

const canEdit = computed(() => detail.value?.status === 'PENDING_AUDIT')
const canCancel = computed(() => detail.value?.status === 'PENDING_AUDIT')
const canConfirm = computed(() => detail.value?.status === 'PENDING_CONFIRM')

const visibleRejectReason = computed(() =>
  detail.value?.status === 'REJECTED' ? detail.value.rejectReason : undefined
)

const statusText = computed(() => {
  const status = detail.value?.status
  return status ? statusMap[status]?.label : '-'
})

const timeline = computed(() => {
  const item = detail.value
  if (!item) return []
  if (item.status === 'CANCELLED') {
    return [
      { label: '提交报修', time: formatTime(item.submitTime), done: true },
      { label: '学生取消报修', time: formatTime(item.closeTime), done: true },
    ]
  }

  return [
    { label: '提交报修', time: formatTime(item.submitTime), done: true },
    { label: '管理员审核', time: item.status === 'REJECTED' ? '已驳回' : formatTime(item.assignTime), done: item.status !== 'PENDING_AUDIT' },
    { label: '维修人员接单', time: formatTime(item.acceptTime), done: !!item.acceptTime },
    { label: '维修完成', time: formatTime(item.finishTime), done: !!item.finishTime },
    { label: '学生确认完成', time: formatTime(item.closeTime), done: item.status === 'COMPLETED' },
  ]
})

const evaluationLabel = computed(() => {
  const score = detail.value?.evaluation?.score
  return evaluationOptions.find(item => item.value === score)?.label ?? '-'
})

const timelineRecords = computed(() => detail.value?.records ?? [])

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await getOrderDetail(String(route.params.id))
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  if (categories.value.length) return
  categoryLoading.value = true
  try {
    categories.value = await getCategories()
  } finally {
    categoryLoading.value = false
  }
}

async function openEditDialog() {
  if (!detail.value) return
  await loadCategories()
  editForm.title = detail.value.title
  editForm.categoryId = detail.value.categoryId
  editForm.description = detail.value.description ?? ''
  editForm.imageUrl = detail.value.imageUrl ?? ''
  editForm.contactPhone = detail.value.contactPhone ?? ''
  editForm.priority = detail.value.priority
  editFormRef.value?.clearValidate()
  editDialogOpen.value = true
}

async function submitEdit() {
  if (!detail.value || submitting.value) return
  const ok = await editFormRef.value?.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  try {
    await editOrder(detail.value.id, editForm)
    ElMessage.success('工单已更新')
    editDialogOpen.value = false
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

async function cancelRepair() {
  if (!detail.value || submitting.value) return

  const result = await ElMessageBox.prompt('可填写取消原因，留空则仅关闭工单。', '取消报修', {
    inputType: 'textarea',
    inputPlaceholder: '取消原因（可选）',
    inputValidator: value => !value || value.length <= 255 || '取消原因不能超过 255 字',
    confirmButtonText: '确认取消',
    cancelButtonText: '再想想',
    customClass: 'app-confirm-dialog',
    confirmButtonClass: 'app-confirm-danger-button',
  }).catch(() => null)
  if (!result) return

  submitting.value = true
  try {
    await cancelOrder(detail.value.id, { cancelReason: result.value?.trim() || undefined })
    ElMessage.success('报修已取消')
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

function openEvaluateDialog() {
  evaluateForm.score = 5
  evaluateForm.content = ''
  evaluateFormRef.value?.clearValidate()
  evaluateDialogOpen.value = true
}

function actionLabel(actionType: string) {
  return {
    ACCEPT: '接单',
    REJECT: '拒单',
    FINISH: '完成维修',
    CANCEL: '取消报修',
    CONFIRM: '确认完成',
    RECORD: '维修记录',
  }[actionType] ?? actionType
}

async function submitEvaluation() {
  if (!detail.value || submitting.value) return
  const ok = await evaluateFormRef.value?.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  try {
    await confirmOrder(detail.value.id, {
      score: evaluateForm.score,
      content: evaluateForm.content?.trim() || undefined,
    })
    ElMessage.success('已确认完成并提交评价')
    evaluateDialogOpen.value = false
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<template>
  <div class="page" v-loading="loading">
    <section class="page-head">
      <div>
        <div class="crumb">{{ detail?.orderNo || 'Order Detail' }}</div>
        <h1>{{ detail?.title || '工单详情' }}</h1>
        <p>{{ detail ? `${detail.dormBuilding} ${detail.dormRoom}` : '正在读取工单信息' }}</p>
      </div>
      <div class="head-actions">
        <el-button v-if="canEdit" :icon="EditPen" @click="openEditDialog">编辑</el-button>
        <el-button v-if="canCancel" :icon="Close" :loading="submitting" @click="cancelRepair">取消报修</el-button>
        <el-button v-if="canConfirm" type="primary" :icon="Select" @click="openEvaluateDialog">确认完成</el-button>
        <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
      </div>
    </section>

    <el-empty v-if="!loading && !detail" description="没有找到工单" />

    <template v-else-if="detail">
      <section class="summary">
        <div>
          <span>当前状态</span>
          <b>{{ statusText }}</b>
        </div>
        <div>
          <span>故障分类</span>
          <b>{{ detail.categoryName || '未分类' }}</b>
        </div>
        <div>
          <span>优先级</span>
          <b>{{ priorityMap[detail.priority] }}</b>
        </div>
        <div>
          <span>提交时间</span>
          <b>{{ formatTime(detail.submitTime) }}</b>
        </div>
      </section>

      <section class="panel">
        <header>
          <h2>问题描述</h2>
          <el-tag :type="statusMap[detail.status]?.type" :class="statusClass(detail.status)">
            {{ statusMap[detail.status]?.label }}
          </el-tag>
        </header>
        <p class="desc">{{ detail.description || '未填写问题描述' }}</p>

        <ImagePreview :src="detail.imageUrl" title="故障图片" />

        <div v-if="visibleRejectReason || detail.adminRemark" class="notice">
          <b>{{ visibleRejectReason ? '驳回原因' : '管理员备注' }}</b>
          <p>{{ visibleRejectReason || detail.adminRemark }}</p>
        </div>
      </section>

      <section v-if="detail.evaluation" class="panel">
        <header>
          <h2>评价反馈</h2>
          <el-tag type="success">{{ evaluationLabel }}</el-tag>
        </header>
        <p class="desc">{{ detail.evaluation.content || '未填写评价内容' }}</p>
        <div class="image-line">
          <span>评价时间</span>
          <b>{{ formatTime(detail.evaluation.createTime) }}</b>
        </div>
      </section>

      <section class="panel">
        <header>
          <h2>工单流转</h2>
        </header>
        <el-timeline>
          <el-timeline-item
            v-for="item in timeline"
            :key="item.label"
            :timestamp="item.time"
            :type="item.done ? 'primary' : 'info'"
          >
            {{ item.label }}
          </el-timeline-item>
        </el-timeline>
      </section>

      <section v-if="timelineRecords.length" class="panel">
        <header>
          <h2>处理记录</h2>
        </header>
        <el-timeline>
          <el-timeline-item
            v-for="record in timelineRecords"
            :key="record.id"
            :timestamp="formatTime(record.actionTime)"
            type="primary"
          >
            <div class="record-title">{{ actionLabel(record.actionType) }}</div>
            <p v-if="record.actionDesc" class="record-desc">{{ record.actionDesc }}</p>
            <ImagePreview :src="record.resultImage" title="维修结果图片" compact />
          </el-timeline-item>
        </el-timeline>
      </section>
    </template>

    <el-dialog v-model="editDialogOpen" title="编辑报修" width="620px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top">
        <el-form-item label="报修标题" prop="title">
          <el-input v-model.trim="editForm.title" maxlength="100" show-word-limit />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="故障分类" prop="categoryId">
            <el-select v-model="editForm.categoryId" filterable placeholder="选择分类" :loading="categoryLoading">
              <el-option
                v-for="item in categories"
                :key="item.id"
                :label="item.categoryName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="紧急程度" prop="priority">
            <el-segmented v-model="editForm.priority" :options="priorityOptions" />
          </el-form-item>
        </div>

        <el-form-item label="问题描述" prop="description">
          <el-input v-model.trim="editForm.description" type="textarea" :rows="5" maxlength="2000" show-word-limit />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="故障图片" prop="imageUrl">
            <ImageUploader v-model="editForm.imageUrl" type="repair" title="上传故障图片" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model.trim="editForm.contactPhone" maxlength="20" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="editDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="evaluateDialogOpen" title="确认完成" width="500px">
      <el-form ref="evaluateFormRef" :model="evaluateForm" :rules="evaluateRules" label-position="top">
        <el-form-item label="评价等级" prop="score">
          <el-segmented v-model="evaluateForm.score" :options="evaluationOptions" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model.trim="evaluateForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="可以留空"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluateDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEvaluation">确认并评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 320px;
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
  }

  p {
    margin: 0;
    color: var(--text-muted);
    font-size: 13px;
  }
}

.head-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;

  div {
    padding: 18px;
    border-right: 1px solid var(--border-soft);

    &:last-child {
      border-right: none;
    }
  }

  span {
    display: block;
    color: var(--text-soft);
    font-size: 12px;
    margin-bottom: 8px;
  }

  b {
    color: var(--text);
    font-size: 15px;
  }
}

.panel {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 20px;

  header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;
  }

  h2 {
    margin: 0;
    font-size: 15px;
  }
}

.desc {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.8;
  white-space: pre-wrap;
}

.image-line {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  font-size: 13px;

  span {
    color: var(--text-soft);
  }

  b {
    color: var(--text);
    font-weight: 500;
  }
}

.notice {
  margin-top: 16px;
  padding: 14px;
  border-radius: 10px;
  background: var(--danger-soft);
  color: var(--danger);

  p {
    margin: 6px 0 0;
  }
}

.record-title {
  margin-bottom: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
}

.record-desc {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.7;
  white-space: pre-wrap;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

:deep(.el-select),
:deep(.el-segmented) {
  width: 100%;
}

:deep(.el-button--primary:not(.is-disabled)) {
  background: var(--text);
  border-color: var(--text);
}

@media (max-width: 820px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .head-actions {
    justify-content: flex-start;
  }

  .summary,
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
