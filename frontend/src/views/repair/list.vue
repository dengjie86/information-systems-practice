<template>
  <div class="page">
    <section class="page-head">
      <div>
        <div class="crumb">My Orders</div>
        <h1>我的工单</h1>
        <p>查看报修进度、审核结果和处理状态。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="router.push('/repair/create')">新建报修</el-button>
    </section>

    <section class="toolbar">
      <el-segmented v-model="query.status" :options="statusFilters" @change="onFilter" />
    </section>

    <section class="panel" v-loading="loading">
      <el-empty v-if="!loading && !orders.length" :description="emptyDescription">
        <el-button v-if="showCreateEmptyAction" type="primary" @click="router.push('/repair/create')">
          提交第一条报修
        </el-button>
      </el-empty>

      <div v-else class="order-list">
        <article v-for="item in orders" :key="item.id" class="order" @click="router.push(`/repair/detail/${item.id}`)">
          <div class="order-main">
            <div class="order-top">
              <span class="order-no">{{ item.orderNo }}</span>
              <el-tag :type="statusMap[item.status]?.type" :class="statusClass(item.status)" effect="light">
                {{ statusMap[item.status]?.label }}
              </el-tag>
            </div>
            <h3>{{ item.title }}</h3>
            <div class="meta">
              <span>{{ item.categoryName || '未分类' }}</span>
              <span>{{ item.dormBuilding }} {{ item.dormRoom }}</span>
              <span>{{ formatTime(item.submitTime) }}</span>
            </div>
            <p v-if="item.status === 'REJECTED' && item.rejectReason" class="reject">{{ item.rejectReason }}</p>
          </div>
          <div class="order-side">
            <span class="priority" :class="item.priority">{{ priorityMap[item.priority] }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </article>
      </div>

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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Plus } from '@element-plus/icons-vue'
import { getMyOrders, type OrderStatus, type RepairOrder } from '@/api/repair'
import { formatTime, priorityMap, statusClass, statusMap } from './meta'

const router = useRouter()
const loading = ref(false)
const orders = ref<RepairOrder[]>([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '' as OrderStatus | '',
})

const statusFilters = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING_AUDIT' },
  { label: '待分派', value: 'PENDING_ASSIGN' },
  { label: '待接单', value: 'PENDING_ACCEPT' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '待确认', value: 'PENDING_CONFIRM' },
  { label: '已完成', value: 'COMPLETED' },
]

const selectedStatusLabel = computed(() =>
  statusFilters.find(item => item.value === query.status)?.label ?? '该状态'
)

const emptyDescription = computed(() =>
  query.status ? `暂无${selectedStatusLabel.value}工单` : '暂无工单记录'
)

const showCreateEmptyAction = computed(() => !query.status)

const loadOrders = async () => {
  loading.value = true
  const res = await getMyOrders(query).finally(() => { loading.value = false })
  orders.value = res.records
  total.value = res.total
}

const onFilter = () => {
  query.pageNum = 1
  loadOrders()
}

onMounted(loadOrders)
</script>

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

.toolbar,
.panel {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
}

.toolbar {
  padding: 12px;
}

.panel {
  min-height: 280px;
  overflow: hidden;
}

.order-list {
  display: flex;
  flex-direction: column;
}

.order {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 20px;
  cursor: pointer;
  border-bottom: 1px solid var(--border-soft);
  transition: background 0.14s ease;

  &:hover {
    background: var(--bg-subtle);
  }

  &:last-child {
    border-bottom: none;
  }
}

.order-main {
  min-width: 0;
}

.order-top,
.meta,
.order-side {
  display: flex;
  align-items: center;
}

.order-top {
  gap: 10px;
  margin-bottom: 8px;
}

.order-no {
  color: var(--text-soft);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.meta {
  gap: 14px;
  color: var(--text-muted);
  font-size: 13px;
  flex-wrap: wrap;
}

.reject {
  margin: 10px 0 0;
  color: var(--danger);
  font-size: 13px;
}

.order-side {
  gap: 12px;
  color: var(--text-soft);
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

:deep(.el-button--primary:not(.is-disabled)) {
  background: var(--text);
  border-color: var(--text);
}

@media (max-width: 760px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .order {
    flex-direction: column;
  }

  .order-side {
    justify-content: space-between;
  }
}
</style>
