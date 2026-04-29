<script setup lang="ts">
import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Refresh, View } from '@element-plus/icons-vue'
import {
  acceptOrder,
  finishRepairOrder,
  getMyOrderCount,
  getMyOrders,
  getOrderDetail,
  rejectWorkerOrder,
  type OrderStatus,
  type Priority,
  type RepairOrder,
} from '@/api/repair'
import { formatTime, priorityMap, statusClass, statusMap } from '@/views/repair/meta'

type WorkerAction = 'accept' | 'reject' | 'finish'

const loading = shallowRef(false)
const detailLoading = shallowRef(false)
const submitting = shallowRef(false)
const drawerOpen = shallowRef(false)
const actionDialogOpen = shallowRef(false)
const actionMode = shallowRef<WorkerAction>('accept')

const orders = ref<RepairOrder[]>([])
const current = ref<RepairOrder | null>(null)
const total = shallowRef(0)
const pendingAcceptCount = shallowRef(0)
const processingCount = shallowRef(0)
const waitingConfirmCount = shallowRef(0)
const actionFormRef = ref<FormInstance>()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '' as OrderStatus | '',
})

const actionForm = reactive({
  remark: '',
  rejectReason: '',
  actionDesc: '',
})

const statusFilters = [
  { label: '全部', value: '' },
  { label: '待接单', value: 'PENDING_ACCEPT' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '待确认', value: 'PENDING_CONFIRM' },
  { label: '已完成', value: 'COMPLETED' },
]

const actionTitle = computed(() => ({
  accept: '接单',
  reject: '拒单',
  finish: '完成维修',
})[actionMode.value])

const actionRules = computed<FormRules>(() => ({
  rejectReason: actionMode.value === 'reject'
    ? [{ required: true, message: '请填写拒单原因', trigger: 'blur' }]
    : [],
  actionDesc: actionMode.value === 'finish'
    ? [{ required: true, message: '请填写完成说明', trigger: 'blur' }]
    : [],
}))

const selectedStatusLabel = computed(() =>
  statusFilters.find(item => item.value === query.status)?.label ?? '该状态'
)

const tableEmptyText = computed(() =>
  query.status ? `暂无${selectedStatusLabel.value}工单` : '暂无分派给你的工单'
)

const timelineRecords = computed(() => {
  return current.value?.records ?? []
})

async function loadOrders() {
  loading.value = true
  try {
    const res = await getMyOrders(query)
    orders.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadSummaryCounts() {
  const [pendingAccept, processing, waitingConfirm] = await Promise.all([
    getMyOrderCount('PENDING_ACCEPT'),
    getMyOrderCount('PROCESSING'),
    getMyOrderCount('PENDING_CONFIRM'),
  ])
  pendingAcceptCount.value = pendingAccept
  processingCount.value = processing
  waitingConfirmCount.value = waitingConfirm
}

async function refreshCurrent() {
  if (!current.value) return
  current.value = await getOrderDetail(current.value.id)
}

async function openDetail(row: RepairOrder) {
  drawerOpen.value = true
  detailLoading.value = true
  try {
    current.value = await getOrderDetail(row.id)
  } finally {
    detailLoading.value = false
  }
}

function resetActionForm() {
  actionForm.remark = ''
  actionForm.rejectReason = ''
  actionForm.actionDesc = ''
  actionFormRef.value?.clearValidate()
}

async function openAction(mode: WorkerAction, row?: RepairOrder) {
  if (row) {
    current.value = await getOrderDetail(row.id)
  }
  if (!current.value) return
  actionMode.value = mode
  resetActionForm()
  actionDialogOpen.value = true
}

async function submitAction() {
  if (!current.value || submitting.value) return
  const ok = await actionFormRef.value?.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  try {
    const mode = actionMode.value
    if (actionMode.value === 'accept') {
      await acceptOrder(current.value.id, optionalText(actionForm.remark))
      ElMessage.success('已接单，工单进入处理中')
    } else if (actionMode.value === 'reject') {
      await rejectWorkerOrder(current.value.id, actionForm.rejectReason)
      ElMessage.success('已拒单，工单退回待分派')
    } else {
      await finishRepairOrder(current.value.id, {
        actionDesc: actionForm.actionDesc,
      })
      ElMessage.success('已提交完成，等待学生确认')
    }
    actionDialogOpen.value = false
    await loadSummaryCounts()
    await loadOrders()
    if (mode === 'reject') {
      drawerOpen.value = false
      current.value = null
    } else if (drawerOpen.value) {
      await refreshCurrent()
    }
  } finally {
    submitting.value = false
  }
}

function optionalText(value: string) {
  const text = value.trim()
  return text || undefined
}

function canAccept(row: RepairOrder) {
  return row.status === 'PENDING_ACCEPT'
}

function canWork(row: RepairOrder) {
  return row.status === 'PROCESSING'
}

function statusLabel(status: OrderStatus) {
  return statusMap[status]?.label ?? status
}

function statusType(status: OrderStatus) {
  return statusMap[status]?.type ?? 'info'
}

function priorityLabel(priority: Priority) {
  return priorityMap[priority] ?? priority
}

function actionLabel(actionType: string) {
  return {
    ACCEPT: '接单',
    REJECT: '拒单',
    FINISH: '完成维修',
  }[actionType] ?? actionType
}

function onFilter() {
  query.pageNum = 1
  loadOrders()
}

function onRefresh() {
  loadOrders()
  loadSummaryCounts()
}

onMounted(() => {
  loadOrders()
  loadSummaryCounts()
})
</script>

<template>
  <div class="page">
    <section class="page-head">
      <div>
        <div class="crumb">Worker Orders</div>
        <h1>维修工单</h1>
        <p>处理分派给你的宿舍报修，接单后提交完成结果。</p>
      </div>
      <el-button :icon="Refresh" @click="onRefresh">刷新</el-button>
    </section>

    <section class="summary">
      <div class="summary-item">
        <span>待接单</span>
        <b>{{ pendingAcceptCount }}</b>
      </div>
      <div class="summary-item">
        <span>处理中</span>
        <b>{{ processingCount }}</b>
      </div>
      <div class="summary-item">
        <span>待学生确认</span>
        <b>{{ waitingConfirmCount }}</b>
      </div>
    </section>

    <section class="toolbar">
      <el-segmented v-model="query.status" :options="statusFilters" @change="onFilter" />
    </section>

    <section class="panel" v-loading="loading">
      <el-table :data="orders" row-key="id" class="table" :empty-text="tableEmptyText" @row-dblclick="openDetail">
        <el-table-column prop="orderNo" label="工单号" min-width="150" />
        <el-table-column prop="title" label="报修内容" min-width="190">
          <template #default="{ row }">
            <div class="title-cell">
              <b>{{ row.title }}</b>
              <span>{{ row.categoryName || '未分类' }} · {{ row.dormBuilding }} {{ row.dormRoom }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" :class="statusClass(row.status)" effect="light">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="96" align="center" header-align="center">
          <template #default="{ row }">
            <span class="priority" :class="row.priority">{{ priorityLabel(row.priority) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="130" align="center" header-align="center">
          <template #default="{ row }">{{ row.contactPhone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="150" align="center" header-align="center">
          <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="128" fixed="right" align="center" header-align="center" class-name="table-action-column">
          <template #default="{ row }">
            <el-button class="table-detail-action" text :icon="View" @click.stop="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <footer v-if="total > query.pageSize" class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="prev, pager, next"
          :total="total"
          @current-change="loadOrders"
        />
      </footer>
    </section>

    <el-drawer v-model="drawerOpen" title="维修工单详情" size="540px">
      <div v-loading="detailLoading" class="detail">
        <template v-if="current">
          <div class="detail-head">
            <div>
              <span>{{ current.orderNo }}</span>
              <h2>{{ current.title }}</h2>
            </div>
            <el-tag :type="statusType(current.status)" :class="statusClass(current.status)">
              {{ statusLabel(current.status) }}
            </el-tag>
          </div>

          <dl class="detail-list">
            <div><dt>宿舍</dt><dd>{{ current.dormBuilding }} {{ current.dormRoom }}</dd></div>
            <div><dt>联系电话</dt><dd>{{ current.contactPhone || '-' }}</dd></div>
            <div><dt>故障分类</dt><dd>{{ current.categoryName || '-' }}</dd></div>
            <div><dt>优先级</dt><dd>{{ priorityLabel(current.priority) }}</dd></div>
            <div><dt>提交时间</dt><dd>{{ formatTime(current.submitTime) }}</dd></div>
            <div><dt>接单时间</dt><dd>{{ formatTime(current.acceptTime) }}</dd></div>
          </dl>

          <section class="desc">
            <h3>问题描述</h3>
            <p>{{ current.description || '学生未填写详细描述。' }}</p>
          </section>

          <section v-if="current.dispatchRemark" class="desc note private">
            <h3>派单备注</h3>
            <p>{{ current.dispatchRemark }}</p>
          </section>

          <section v-if="current.imageUrl" class="desc image-link">
            <h3>故障图片</h3>
            <el-link :href="current.imageUrl" target="_blank" underline="never">{{ current.imageUrl }}</el-link>
          </section>

          <section class="records">
            <h3>处理记录</h3>
            <el-empty v-if="!timelineRecords.length" description="暂无处理记录" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="record in timelineRecords"
                :key="record.id"
                :timestamp="formatTime(record.actionTime)"
                type="primary"
              >
                <div class="record-title">{{ actionLabel(record.actionType) }}</div>
                <p v-if="record.actionDesc">{{ record.actionDesc }}</p>
                <el-link v-if="record.resultImage" :href="record.resultImage" target="_blank" underline="never">
                  {{ record.resultImage }}
                </el-link>
              </el-timeline-item>
            </el-timeline>
          </section>

          <div class="drawer-actions">
            <el-button :disabled="!canAccept(current)" @click="openAction('reject')">拒单</el-button>
            <el-button :disabled="!canAccept(current)" @click="openAction('accept')">接单</el-button>
            <el-button :disabled="!canWork(current)" @click="openAction('finish')">完成维修</el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="actionDialogOpen" :title="actionTitle" width="460px" @closed="resetActionForm">
      <el-form ref="actionFormRef" :model="actionForm" :rules="actionRules" label-position="top">
        <el-form-item v-if="actionMode === 'accept'" label="接单备注" prop="remark">
          <el-input v-model.trim="actionForm.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item v-if="actionMode === 'reject'" label="拒单原因" prop="rejectReason">
          <el-input v-model.trim="actionForm.rejectReason" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
        <template v-if="actionMode === 'finish'">
          <el-form-item label="完成说明" prop="actionDesc">
            <el-input v-model.trim="actionForm.actionDesc" type="textarea" :rows="4" maxlength="2000" show-word-limit />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.page {
  display: flex;
  flex-direction: column;
  gap: 18px;
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

.summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
}

.summary-item {
  padding: 16px 20px;
  border-right: 1px solid var(--border-soft);

  &:last-child {
    border-right: none;
  }

  span {
    display: block;
    color: var(--text-muted);
    font-size: 12px;
    margin-bottom: 6px;
  }

  b {
    font-size: 24px;
    font-weight: 600;
  }
}

.toolbar,
.panel {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
}

.toolbar {
  padding: 12px;
  overflow-x: auto;
}

.panel {
  overflow: hidden;
}

.table {
  width: 100%;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;

  b {
    color: var(--text);
    font-size: 13px;
  }

  span {
    color: var(--text-soft);
    font-size: 12px;
  }
}

.priority {
  font-size: 12px;
  padding: 3px 9px;
  border-radius: 999px;
  background: var(--bg-muted);
  color: var(--text-muted);

  &.HIGH,
  &.URGENT {
    color: var(--danger);
    background: var(--danger-soft);
  }
}

.pager {
  display: flex;
  justify-content: center;
  padding: 14px 18px;
  border-top: 1px solid var(--border-soft);
}

.detail {
  min-height: 260px;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-soft);

  span {
    color: var(--text-soft);
    font-size: 12px;
  }

  h2 {
    margin: 6px 0 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.detail-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 18px 0;

  div {
    padding: 12px;
    border: 1px solid var(--border-soft);
    border-radius: 10px;
    background: var(--bg-subtle);
  }

  dt {
    color: var(--text-soft);
    font-size: 12px;
    margin-bottom: 6px;
  }

  dd {
    margin: 0;
    color: var(--text);
    font-size: 13px;
  }
}

.desc,
.records {
  padding: 14px;
  border: 1px solid var(--border-soft);
  border-radius: 10px;
  margin-bottom: 12px;

  h3 {
    margin: 0 0 8px;
    font-size: 13px;
  }

  p {
    margin: 0;
    color: var(--text-muted);
    line-height: 1.7;
    white-space: pre-wrap;
  }
}

.image-link {
  background: var(--bg-subtle);
}

.note.private {
  background: var(--bg-muted);
}

.record-title {
  margin-bottom: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
}

.drawer-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 8px;

  :deep(.el-button) {
    background: var(--text);
    border-color: var(--text);
    color: #fff;
  }

  :deep(.el-button:not(.is-disabled):hover),
  :deep(.el-button:not(.is-disabled):focus) {
    background: #27272a;
    border-color: #27272a;
    color: #fff;
  }

  :deep(.el-button.is-disabled),
  :deep(.el-button.is-disabled:hover),
  :deep(.el-button.is-disabled:focus) {
    background: #f4f4f5;
    border-color: #dcdfe6;
    color: #a8abb2;
    opacity: 1;
  }
}

:deep(.el-button--primary:not(.is-disabled)) {
  background: var(--text);
  border-color: var(--text);
}

@media (max-width: 780px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary,
  .detail-list {
    grid-template-columns: 1fr;
  }
}
</style>
