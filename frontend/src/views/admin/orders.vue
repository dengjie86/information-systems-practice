<script setup lang="ts">
import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Refresh, View } from '@element-plus/icons-vue'
import {
  approveOrder,
  assignOrder,
  getAdminOrderDetail,
  getAdminOrders,
  getWorkers,
  rejectOrder,
  type AdminOrder,
  type AssignParams,
  type AuditParams,
} from '@/api/admin'
import type { OrderStatus } from '@/api/repair'
import type { Priority } from '@/api/repair'
import type { UserInfo } from '@/stores/user'
import { formatTime, priorityMap, statusClass, statusMap } from '@/views/repair/meta'

const loading = shallowRef(false)
const detailLoading = shallowRef(false)
const submitting = shallowRef(false)
const drawerOpen = shallowRef(false)
const actionDialogOpen = shallowRef(false)
const actionMode = shallowRef<'approve' | 'reject' | 'assign'>('approve')
const actionTitle = computed(() => ({
  approve: '审核通过',
  reject: '驳回工单',
  assign: '分派维修人员',
})[actionMode.value])

const remarkLabel = computed(() => ({
  approve: '审核备注（学生可见）',
  reject: '',
  assign: '派单备注（仅管理端/维修端可见）',
})[actionMode.value])

const orders = ref<AdminOrder[]>([])
const workers = ref<UserInfo[]>([])
const current = ref<AdminOrder | null>(null)
const total = shallowRef(0)
const actionFormRef = ref<FormInstance>()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '' as OrderStatus | '',
})

const actionForm = reactive<AuditParams & AssignParams>({
  adminRemark: '',
  rejectReason: '',
  workerId: null,
})

const statusFilters = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING_AUDIT' },
  { label: '待分派', value: 'PENDING_ASSIGN' },
  { label: '待接单', value: 'PENDING_ACCEPT' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '待确认', value: 'PENDING_CONFIRM' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已驳回', value: 'REJECTED' },
]

const actionRules = computed<FormRules>(() => ({
  rejectReason: actionMode.value === 'reject'
    ? [{ required: true, message: '请填写驳回原因', trigger: 'blur' }]
    : [],
  workerId: actionMode.value === 'assign'
    ? [{ required: true, message: '请选择维修人员', trigger: 'change' }]
    : [],
}))

const pendingAuditCount = computed(() =>
  orders.value.filter(item => item.status === 'PENDING_AUDIT').length
)

const pendingAssignCount = computed(() =>
  orders.value.filter(item => item.status === 'PENDING_ASSIGN').length
)

const activeCount = computed(() =>
  orders.value.filter(item => !['COMPLETED', 'CLOSED', 'REJECTED'].includes(item.status)).length
)

async function loadOrders() {
  loading.value = true
  try {
    const res = await getAdminOrders(query)
    orders.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadWorkers() {
  workers.value = await getWorkers()
}

async function openDetail(row: AdminOrder) {
  drawerOpen.value = true
  detailLoading.value = true
  try {
    current.value = await getAdminOrderDetail(row.id)
  } finally {
    detailLoading.value = false
  }
}

function resetActionForm() {
  actionForm.adminRemark = ''
  actionForm.rejectReason = ''
  actionForm.workerId = null
  actionFormRef.value?.clearValidate()
}

function openAction(mode: 'approve' | 'reject' | 'assign', row?: AdminOrder) {
  current.value = row ?? current.value
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
    if (actionMode.value === 'approve') {
      await approveOrder(current.value.id, { adminRemark: actionForm.adminRemark })
      ElMessage.success('工单已审核通过')
    } else if (actionMode.value === 'reject') {
      await rejectOrder(current.value.id, {
        rejectReason: actionForm.rejectReason,
      })
      ElMessage.success('工单已驳回')
    } else {
      await assignOrder(current.value.id, {
        workerId: actionForm.workerId,
        dispatchRemark: actionForm.adminRemark,
      })
      ElMessage.success('工单已分派')
    }
    actionDialogOpen.value = false
    await loadOrders()
    if (drawerOpen.value) {
      await openDetail(current.value)
    }
  } finally {
    submitting.value = false
  }
}

function canApprove(row: AdminOrder) {
  return row.status === 'PENDING_AUDIT'
}

function canAssign(row: AdminOrder) {
  return row.status === 'PENDING_ASSIGN'
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

function onFilter() {
  query.pageNum = 1
  loadOrders()
}

function onRefresh() {
  loadOrders()
}

onMounted(() => {
  loadOrders()
  loadWorkers()
})
</script>

<template>
  <div class="page">
    <section class="page-head">
      <div>
        <div class="crumb">Order Admin</div>
        <h1>工单管理</h1>
        <p>审核学生报修，分派维修人员，并跟踪当前处理状态。</p>
      </div>
      <el-button :icon="Refresh" @click="onRefresh">刷新</el-button>
    </section>

    <section class="summary">
      <div class="summary-item">
        <span>待审核</span>
        <b>{{ pendingAuditCount }}</b>
      </div>
      <div class="summary-item">
        <span>待分派</span>
        <b>{{ pendingAssignCount }}</b>
      </div>
      <div class="summary-item">
        <span>本页未完结</span>
        <b>{{ activeCount }}</b>
      </div>
    </section>

    <section class="toolbar">
      <el-segmented v-model="query.status" :options="statusFilters" @change="onFilter" />
    </section>

    <section class="panel" v-loading="loading">
      <el-table :data="orders" row-key="id" class="table" @row-dblclick="openDetail">
        <el-table-column prop="orderNo" label="工单号" min-width="150" />
        <el-table-column prop="title" label="报修内容" min-width="180">
          <template #default="{ row }">
            <div class="title-cell">
              <b>{{ row.title }}</b>
              <span>{{ row.categoryName || '未分类' }} · {{ row.dormBuilding }} {{ row.dormRoom }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生" width="110" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" :class="statusClass(row.status)" effect="light">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="96">
          <template #default="{ row }">
            <span class="priority" :class="row.priority">{{ priorityLabel(row.priority) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="assignedWorkerName" label="维修人员" width="120">
          <template #default="{ row }">{{ row.assignedWorkerName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="150">
          <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button text :icon="View" @click.stop="openDetail(row)">详情</el-button>
            <el-button text :disabled="!canApprove(row)" @click.stop="openAction('approve', row)">通过</el-button>
            <el-button text :disabled="!canApprove(row)" @click.stop="openAction('reject', row)">驳回</el-button>
            <el-button text :disabled="!canAssign(row)" @click.stop="openAction('assign', row)">分派</el-button>
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

    <el-drawer v-model="drawerOpen" title="工单详情" size="520px">
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
            <div><dt>学生</dt><dd>{{ current.studentName || '-' }}</dd></div>
            <div><dt>宿舍</dt><dd>{{ current.dormBuilding }} {{ current.dormRoom }}</dd></div>
            <div><dt>联系电话</dt><dd>{{ current.contactPhone || '-' }}</dd></div>
            <div><dt>故障分类</dt><dd>{{ current.categoryName || '-' }}</dd></div>
            <div><dt>优先级</dt><dd>{{ priorityLabel(current.priority) }}</dd></div>
            <div><dt>维修人员</dt><dd>{{ current.assignedWorkerName || '-' }}</dd></div>
            <div><dt>提交时间</dt><dd>{{ formatTime(current.submitTime) }}</dd></div>
            <div><dt>分派时间</dt><dd>{{ formatTime(current.assignTime) }}</dd></div>
          </dl>
          <section class="desc">
            <h3>问题描述</h3>
            <p>{{ current.description || '学生未填写详细描述。' }}</p>
          </section>
          <section v-if="current.rejectReason" class="desc note danger">
            <h3>驳回原因</h3>
            <p>{{ current.rejectReason }}</p>
          </section>
          <section v-if="current.adminRemark" class="desc note">
            <h3>审核备注</h3>
            <p>{{ current.adminRemark }}</p>
          </section>
          <section v-if="current.dispatchRemark" class="desc note private">
            <h3>派单备注</h3>
            <p>{{ current.dispatchRemark }}</p>
          </section>
          <div class="drawer-actions">
            <el-button :disabled="!canApprove(current)" @click="openAction('approve')">审核通过</el-button>
            <el-button :disabled="!canApprove(current)" @click="openAction('reject')">驳回工单</el-button>
            <el-button type="primary" :disabled="!canAssign(current)" @click="openAction('assign')">分派维修</el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="actionDialogOpen" :title="actionTitle" width="440px" @closed="resetActionForm">
      <el-form ref="actionFormRef" :model="actionForm" :rules="actionRules" label-position="top">
        <el-form-item v-if="actionMode === 'assign'" label="维修人员" prop="workerId">
          <el-select v-model="actionForm.workerId" placeholder="选择维修人员" filterable>
            <el-option
              v-for="worker in workers"
              :key="worker.id"
              :label="`${worker.realName || worker.username} ${worker.phone || ''}`"
              :value="worker.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="actionMode === 'reject'" label="驳回原因" prop="rejectReason">
          <el-input v-model.trim="actionForm.rejectReason" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item v-if="actionMode !== 'reject'" :label="remarkLabel" prop="adminRemark">
          <el-input v-model.trim="actionForm.adminRemark" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
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
  justify-content: flex-end;
  padding: 14px 18px;
  border-top: 1px solid var(--border-soft);
}

.detail {
  min-height: 240px;
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

.desc {
  padding: 14px;
  border: 1px solid var(--border-soft);
  border-radius: 10px;
  margin-bottom: 12px;

  &.note {
    background: var(--warning-soft);
  }

  &.danger {
    background: var(--danger-soft);
  }

  &.private {
    background: var(--bg-muted);
  }

  h3 {
    margin: 0 0 8px;
    font-size: 13px;
  }

  p {
    margin: 0;
    color: var(--text-muted);
    line-height: 1.7;
  }
}

.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
}

:deep(.el-button--primary) {
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
