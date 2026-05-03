<script setup lang="ts">
import { computed, onMounted, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { DataLine, Refresh, Tickets, TrendCharts, WarningFilled } from '@element-plus/icons-vue'
import type { EChartsOption } from 'echarts'
import { getMyOrderCount, getMyOrders, getOrderDetail, type OrderStatus, type RepairOrder } from '@/api/repair'
import { useUserStore } from '@/stores/user'
import { useEChart } from '@/composables/useEChart'
import { formatTime, priorityMap, statusMap } from '@/views/repair/meta'

type TodoTarget = {
  statuses: OrderStatus[]
}

const router = useRouter()
const userStore = useUserStore()

const loading = shallowRef(false)
const updatedAt = shallowRef('')
const orders = shallowRef<RepairOrder[]>([])
const todoOrders = shallowRef<RepairOrder[]>([])
const total = shallowRef(0)
const completed = shallowRef(0)
const processing = shallowRef(0)
const pendingAudit = shallowRef(0)
const pendingConfirm = shallowRef(0)
const favorableRate = shallowRef(0)
const trendChartRef = shallowRef<HTMLElement | null>(null)

const isWorker = computed(() => userStore.role === 'WORKER')
const listPath = computed(() => isWorker.value ? '/work/list' : '/repair/list')
const todoConfig = computed<TodoTarget>(() =>
  isWorker.value
    ? { statuses: ['PENDING_ACCEPT', 'PROCESSING'] }
    : { statuses: ['PENDING_CONFIRM'] }
)

const displayName = computed(() =>
  userStore.userInfo?.realName || userStore.userInfo?.username || '用户'
)

const pageCopy = computed(() => {
  if (isWorker.value) {
    return {
      crumb: 'Worker Workbench',
      title: '维修工作台',
      desc: '查看分派给你的工单进展、近期处理趋势和当前待办。',
      todoTitle: '待办提醒',
      todoDesc: '只显示待接单和处理中工单',
      emptyTodo: '暂无需要处理的维修工单',
      trendTitle: '个人最近工单趋势',
      trendDesc: '近 7 天分派给你的工单数量',
    }
  }
  return {
    crumb: 'Student Workbench',
    title: '学生工作台',
    desc: '查看个人报修进度、待审核情况和需要确认的维修结果。',
    todoTitle: '确认提醒',
    todoDesc: '待确认工单需要确认完成并评价',
    emptyTodo: '暂无待确认工单',
    trendTitle: '个人最近工单趋势',
    trendDesc: '近 7 天你提交的报修数量',
  }
})

const today = computed(() =>
  new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })
)

const kpis = computed(() => {
  if (isWorker.value) {
    return [
      { label: '工单总数', value: total.value, desc: '已分派给你的全部工单', icon: Tickets, color: '#2563eb' },
      { label: '已完成', value: completed.value, desc: '学生已确认完成', icon: TrendCharts, color: '#16a34a', targetStatus: 'COMPLETED' as OrderStatus },
      { label: '处理中', value: processing.value, desc: '当前正在维修处理', icon: DataLine, color: '#d97706', targetStatus: 'PROCESSING' as OrderStatus },
      { label: '好评率', value: `${favorableRate.value}%`, desc: '已评价工单中的好评占比', icon: WarningFilled, color: '#be185d', targetStatus: 'COMPLETED' as OrderStatus },
    ]
  }
  return [
    { label: '工单总数', value: total.value, desc: '你提交过的全部报修', icon: Tickets, color: '#2563eb' },
    { label: '已完成', value: completed.value, desc: '已确认完成的报修', icon: TrendCharts, color: '#16a34a', targetStatus: 'COMPLETED' as OrderStatus },
    { label: '处理中', value: processing.value, desc: '维修人员正在处理', icon: DataLine, color: '#d97706', targetStatus: 'PROCESSING' as OrderStatus },
    { label: '待审核', value: pendingAudit.value, desc: '等待管理员审核', icon: WarningFilled, color: '#be185d', targetStatus: 'PENDING_AUDIT' as OrderStatus },
  ]
})

const todoItems = computed(() =>
  todoOrders.value.map(item => ({
    id: item.id,
    orderNo: item.orderNo,
    title: item.title,
    status: item.status,
    statusLabel: statusMap[item.status]?.label ?? item.status,
    priority: priorityMap[item.priority] ?? item.priority,
    location: [item.dormBuilding, item.dormRoom].filter(Boolean).join(' '),
    submitTime: formatTime(item.submitTime),
  }))
)

const trendDays = computed(() => {
  const days: { key: string; label: string; count: number }[] = []
  const todayDate = new Date()
  for (let index = 6; index >= 0; index -= 1) {
    const date = new Date(todayDate)
    date.setDate(todayDate.getDate() - index)
    const key = localDateKey(date)
    days.push({ key, label: key.slice(5), count: 0 })
  }
  for (const order of orders.value) {
    const key = order.submitTime?.slice(0, 10)
    const day = days.find(item => item.key === key)
    if (day) day.count += 1
  }
  return days
})

const hasTrendData = computed(() => trendDays.value.some(item => item.count > 0))

const trendOption = computed<EChartsOption>(() => ({
  grid: { left: 36, right: 18, top: 28, bottom: 32 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trendDays.value.map(item => item.label),
    axisTick: { show: false },
    axisLine: { lineStyle: { color: '#e4e4e7' } },
    axisLabel: { color: '#71717a' },
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: '#f1f1f3' } },
    axisLabel: { color: '#71717a' },
  },
  series: [{
    name: '工单数量',
    type: 'line',
    smooth: true,
    symbolSize: 7,
    data: trendDays.value.map(item => item.count),
    areaStyle: { color: 'rgba(37, 99, 235, 0.10)' },
    lineStyle: { width: 3, color: '#2563eb' },
    itemStyle: { color: '#2563eb' },
  }],
}))

const trendChart = useEChart(trendChartRef, () => trendOption.value, () => hasTrendData.value)

function formatUpdatedAt() {
  const date = new Date()
  const pad = (val: number) => String(val).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function localDateKey(date: Date) {
  const pad = (val: number) => String(val).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

async function fetchAllOrders(status?: OrderStatus) {
  const countPage = await getMyOrders({ pageNum: 1, pageSize: 1, status: status ?? '' })
  if (!countPage.total) return []
  const page = await getMyOrders({ pageNum: 1, pageSize: countPage.total, status: status ?? '' })
  return page.records
}

async function loadFavorableRate() {
  if (!isWorker.value || !completed.value) {
    favorableRate.value = 0
    return
  }

  const completedOrders = await fetchAllOrders('COMPLETED')
  const details = await Promise.all(
    completedOrders.map(order => getOrderDetail(order.id).catch(() => null))
  )
  const evaluated = details.filter((item): item is RepairOrder => !!item?.evaluation)
  if (!evaluated.length) {
    favorableRate.value = 0
    return
  }

  const good = evaluated.filter(item => item.evaluation?.score === 5).length
  favorableRate.value = Math.round(good * 10000 / evaluated.length) / 100
}

async function loadTodos() {
  const pages = await Promise.all(
    todoConfig.value.statuses.map(status => getMyOrders({ pageNum: 1, pageSize: 5, status }))
  )
  todoOrders.value = pages
    .flatMap(page => page.records)
    .sort((a, b) => String(b.submitTime ?? '').localeCompare(String(a.submitTime ?? '')))
    .slice(0, 5)
}

async function loadWorkbench() {
  if (loading.value) return
  loading.value = true
  try {
    const [allOrders, completedCount, processingCount] = await Promise.all([
      fetchAllOrders(),
      getMyOrderCount('COMPLETED'),
      getMyOrderCount('PROCESSING'),
    ])
    orders.value = allOrders
    total.value = allOrders.length
    completed.value = completedCount
    processing.value = processingCount

    if (!isWorker.value) {
      const [auditCount, confirmCount] = await Promise.all([
        getMyOrderCount('PENDING_AUDIT'),
        getMyOrderCount('PENDING_CONFIRM'),
      ])
      pendingAudit.value = auditCount
      pendingConfirm.value = confirmCount
    }

    await Promise.all([loadTodos(), loadFavorableRate()])
    updatedAt.value = formatUpdatedAt()
    await trendChart.render()
  } finally {
    loading.value = false
  }
}

function openList(status?: OrderStatus) {
  router.push({
    path: listPath.value,
    query: status ? { status } : {},
  })
}

onMounted(loadWorkbench)
</script>

<template>
  <div class="home-page">
    <section class="page-head">
      <div>
        <div class="crumb">{{ pageCopy.crumb }}</div>
        <h1>{{ pageCopy.title }}</h1>
        <p>{{ today }}，{{ displayName }}，{{ pageCopy.desc }}</p>
      </div>
      <div class="head-actions">
        <span class="updated-time">{{ updatedAt ? `最近更新 ${updatedAt}` : '等待加载' }}</span>
        <el-button :icon="Refresh" :loading="loading" @click="loadWorkbench">刷新</el-button>
      </div>
    </section>

    <section class="kpi-grid" v-loading="loading">
      <button
        v-for="item in kpis"
        :key="item.label"
        class="kpi-card"
        type="button"
        @click="openList(item.targetStatus)"
      >
        <div class="kpi-icon" :style="{ color: item.color, backgroundColor: `${item.color}14` }">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="kpi-content">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <p>{{ item.desc }}</p>
        </div>
      </button>
    </section>

    <section class="dashboard-grid" v-loading="loading">
      <div class="panel trend-panel">
        <header class="panel-head">
          <div>
            <h2>{{ pageCopy.trendTitle }}</h2>
            <p>{{ pageCopy.trendDesc }}</p>
          </div>
        </header>
        <div v-show="hasTrendData" ref="trendChartRef" class="chart-box" />
        <el-empty v-if="!loading && !hasTrendData" class="panel-empty" description="暂无趋势数据" />
      </div>

      <div class="panel todo-panel">
        <header class="panel-head">
          <div>
            <h2>{{ pageCopy.todoTitle }}</h2>
            <p>{{ pageCopy.todoDesc }}</p>
          </div>
          <div class="todo-actions">
            <span v-if="!isWorker" class="confirm-count">待确认 {{ pendingConfirm }}</span>
            <el-button text @click="openList()">查看全部</el-button>
          </div>
        </header>

        <div v-if="todoItems.length" class="todo-list">
          <button
            v-for="item in todoItems"
            :key="item.id"
            class="todo-item"
            type="button"
            @click="openList(item.status)"
          >
            <div class="todo-main">
              <span class="todo-no">#{{ item.orderNo }}</span>
              <strong>{{ item.title }}</strong>
              <p>{{ item.location || '未填写宿舍' }} · {{ item.submitTime }}</p>
            </div>
            <div class="todo-side">
              <span class="todo-status">{{ item.statusLabel }}</span>
              <span class="todo-priority">{{ item.priority }}</span>
            </div>
          </button>
        </div>
        <el-empty v-else-if="!loading" class="panel-empty" :description="pageCopy.emptyTodo" />
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.home-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
  width: 100%;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;

  .crumb {
    font-size: 12px;
    color: var(--text-soft);
    font-weight: 600;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    margin-bottom: 6px;
  }

  h1 {
    margin: 0 0 4px;
    color: var(--text);
    font-size: 26px;
    font-weight: 600;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    color: var(--text-muted);
    font-size: 13px;
  }
}

.head-actions,
.todo-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.updated-time {
  color: var(--text-soft);
  font-size: 12px;
  white-space: nowrap;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.kpi-card {
  min-height: 132px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;

  &:hover {
    border-color: #d4d4d8;
    box-shadow: 0 8px 18px rgba(15, 23, 42, 0.06);
  }
}

.kpi-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.kpi-content {
  display: flex;
  flex-direction: column;
  gap: 4px;

  span {
    color: var(--text-muted);
    font-size: 12px;
  }

  strong {
    color: var(--text);
    font-size: 25px;
    font-weight: 650;
    line-height: 1.1;
    font-variant-numeric: tabular-nums;
  }

  p {
    margin: 0;
    color: var(--text-soft);
    font-size: 12px;
    line-height: 1.45;
  }
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.9fr);
  gap: 16px;
}

.panel {
  min-height: 360px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 18px 20px 10px;

  h2 {
    margin: 0 0 4px;
    color: var(--text);
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    color: var(--text-soft);
    font-size: 12px;
  }
}

.chart-box {
  width: 100%;
  height: 300px;
  min-height: 300px;
}

.panel-empty {
  flex: 1;
  min-height: 260px;
}

.confirm-count {
  color: var(--warning);
  background: var(--warning-soft);
  border-radius: 999px;
  padding: 4px 9px;
  font-size: 12px;
  white-space: nowrap;
}

.todo-list {
  padding: 6px 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-item {
  width: 100%;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;

  &:hover {
    background: var(--bg-muted);
    border-color: var(--border);
  }
}

.todo-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;

  strong {
    color: var(--text);
    font-size: 13px;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  p {
    margin: 0;
    color: var(--text-soft);
    font-size: 12px;
  }
}

.todo-no {
  color: var(--text-soft);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.todo-side {
  flex: 0 0 auto;
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  gap: 6px;
}

.todo-status,
.todo-priority {
  border-radius: 999px;
  padding: 3px 8px;
  font-size: 12px;
  line-height: 1.2;
}

.todo-status {
  color: #2563eb;
  background: #eff6ff;
}

.todo-priority {
  color: var(--warning);
  background: var(--warning-soft);
}

@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page-head,
  .panel-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .head-actions {
    width: 100%;
    justify-content: space-between;
  }

  .kpi-grid {
    grid-template-columns: 1fr;
  }
}
</style>
