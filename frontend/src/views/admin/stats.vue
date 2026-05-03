<script setup lang="ts">
import { computed, onMounted, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { DataAnalysis, Histogram, PieChart, Refresh, TrendCharts } from '@element-plus/icons-vue'
import type { EChartsOption } from 'echarts'
import {
  getAdminOrderDetail,
  getAdminOrders,
  getStatsCategory,
  getStatsOverview,
  getStatsTrend,
  getStatsWorkerLoad,
  type AdminOrder,
  type CategoryDistribution,
  type DailyTrend,
  type OverviewStats,
  type WorkerLoad,
} from '@/api/admin'
import type { OrderStatus } from '@/api/repair'
import { useEChart } from '@/composables/useEChart'
import { formatTime, priorityMap, statusMap } from '@/views/repair/meta'

const router = useRouter()
const loading = shallowRef(false)
const updatedAt = shallowRef('')
const overview = shallowRef<OverviewStats | null>(null)
const categories = shallowRef<CategoryDistribution[]>([])
const workerLoads = shallowRef<WorkerLoad[]>([])
const trends = shallowRef<DailyTrend[]>([])
const todoOrders = shallowRef<AdminOrder[]>([])
const favorableRate = shallowRef(0)

const trendChartRef = shallowRef<HTMLElement | null>(null)
const categoryChartRef = shallowRef<HTMLElement | null>(null)
const workerChartRef = shallowRef<HTMLElement | null>(null)

const totalOrders = computed(() => overview.value?.total ?? 0)
const hasOverviewData = computed(() => totalOrders.value > 0)
const hasTrendData = computed(() => trends.value.some(item => item.count > 0))
const hasCategoryData = computed(() => categories.value.some(item => item.count > 0))
const hasWorkerData = computed(() => workerLoads.value.some(item => item.total > 0 || item.completed > 0))
const todoStatuses: OrderStatus[] = ['PENDING_AUDIT', 'PENDING_ASSIGN', 'PENDING_CONFIRM']

const kpis = computed(() => {
  const data = overview.value
  return [
    { label: '工单总数', value: data?.total ?? 0, desc: '系统累计报修工单', icon: DataAnalysis, color: '#2563eb' },
    { label: '已完成', value: data?.completed ?? 0, desc: '学生已确认完成', icon: TrendCharts, color: '#16a34a' },
    { label: '处理中', value: data?.processing ?? 0, desc: '分派、接单、维修、待确认', icon: Histogram, color: '#0f766e' },
    { label: '待审核', value: data?.pendingAudit ?? 0, desc: '等待管理员审核', icon: PieChart, color: '#d97706' },
    { label: '已取消/驳回', value: (data?.cancelled ?? 0) + (data?.rejected ?? 0), desc: '无需继续处理的工单', icon: DataAnalysis, color: '#71717a' },
    { label: '好评率', value: `${favorableRate.value}%`, desc: '已评价工单中的好评占比', icon: TrendCharts, color: '#be185d' },
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

const todoCount = computed(() => todoItems.value.length)

const trendOption = computed<EChartsOption>(() => ({
  grid: { left: 36, right: 18, top: 28, bottom: 32 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trends.value.map(item => item.date.slice(5)),
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
    name: '新增工单',
    type: 'line',
    smooth: true,
    symbolSize: 7,
    data: trends.value.map(item => item.count),
    areaStyle: { color: 'rgba(37, 99, 235, 0.10)' },
    lineStyle: { width: 3, color: '#2563eb' },
    itemStyle: { color: '#2563eb' },
  }],
}))

const categoryOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: {
    bottom: 0,
    left: 'center',
    icon: 'circle',
    textStyle: { color: '#52525b' },
  },
  series: [{
    name: '故障分类',
    type: 'pie',
    radius: ['48%', '72%'],
    center: ['50%', '43%'],
    avoidLabelOverlap: true,
    label: { formatter: '{b}\n{d}%', color: '#52525b' },
    data: categories.value.map(item => ({ name: item.name || '未分类', value: item.count })),
    color: ['#2563eb', '#16a34a', '#d97706', '#be185d', '#0f766e', '#8b5cf6', '#71717a'],
  }],
}))

const workerOption = computed<EChartsOption>(() => ({
  grid: { left: 40, right: 18, top: 28, bottom: 42 },
  tooltip: { trigger: 'axis' },
  legend: { top: 0, right: 8, textStyle: { color: '#52525b' } },
  xAxis: {
    type: 'category',
    data: workerLoads.value.map(item => item.workerName || `人员${item.workerId}`),
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
  series: [
    {
      name: '处理量',
      type: 'bar',
      data: workerLoads.value.map(item => item.total),
      barMaxWidth: 24,
      itemStyle: { color: '#2563eb', borderRadius: [5, 5, 0, 0] },
    },
    {
      name: '已完成',
      type: 'bar',
      data: workerLoads.value.map(item => item.completed),
      barMaxWidth: 24,
      itemStyle: { color: '#16a34a', borderRadius: [5, 5, 0, 0] },
    },
  ],
}))

const trendChart = useEChart(trendChartRef, () => trendOption.value, () => hasTrendData.value)
const categoryChart = useEChart(categoryChartRef, () => categoryOption.value, () => hasCategoryData.value)
const workerChart = useEChart(workerChartRef, () => workerOption.value, () => hasWorkerData.value)

function formatUpdatedAt() {
  const date = new Date()
  const pad = (val: number) => String(val).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

async function renderCharts() {
  await Promise.all([
    trendChart.render(),
    categoryChart.render(),
    workerChart.render(),
  ])
}

async function loadTodos() {
  const pages = await Promise.all(
    todoStatuses.map(status => getAdminOrders({ pageNum: 1, pageSize: 4, status }))
  )
  todoOrders.value = pages
    .flatMap(page => page.records)
    .sort((a, b) => String(b.submitTime ?? '').localeCompare(String(a.submitTime ?? '')))
    .slice(0, 5)
}

async function loadFavorableRate() {
  const countPage = await getAdminOrders({ pageNum: 1, pageSize: 1, status: 'COMPLETED' })
  if (!countPage.total) {
    favorableRate.value = 0
    return
  }

  const completedPage = await getAdminOrders({
    pageNum: 1,
    pageSize: countPage.total,
    status: 'COMPLETED',
  })
  const details = await Promise.all(
    completedPage.records.map(order => getAdminOrderDetail(order.id).catch(() => null))
  )
  const evaluated = details.filter((item): item is AdminOrder => !!item?.evaluation)
  if (!evaluated.length) {
    favorableRate.value = 0
    return
  }

  const good = evaluated.filter(item => item.evaluation?.score === 5).length
  favorableRate.value = Math.round(good * 10000 / evaluated.length) / 100
}

function openOrderList(status?: OrderStatus) {
  router.push({
    path: '/order/list',
    query: status ? { status } : {},
  })
}

async function loadStats() {
  if (loading.value) return
  loading.value = true
  try {
    const [overviewData, categoryData, workerData, trendData] = await Promise.all([
      getStatsOverview(),
      getStatsCategory(),
      getStatsWorkerLoad(),
      getStatsTrend(),
    ])
    overview.value = overviewData
    categories.value = categoryData
    workerLoads.value = workerData
    trends.value = trendData
    await Promise.all([loadTodos(), loadFavorableRate()])
    updatedAt.value = formatUpdatedAt()
    await renderCharts()
  } catch {
    overview.value = null
    categories.value = []
    workerLoads.value = []
    trends.value = []
    todoOrders.value = []
    favorableRate.value = 0
    await renderCharts()
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<template>
  <div class="stats-page">
    <section class="page-head">
      <div>
        <div class="crumb">Workbench</div>
        <h1>工作台</h1>
        <p>汇总当前工单状态、维修处理情况和需要管理员尽快处理的待办。</p>
      </div>
      <div class="head-actions">
        <span class="updated-time">{{ updatedAt ? `最近更新 ${updatedAt}` : '等待加载' }}</span>
        <el-button :icon="Refresh" :loading="loading" @click="loadStats">刷新</el-button>
      </div>
    </section>

    <section class="kpi-grid" v-loading="loading">
      <div v-for="item in kpis" :key="item.label" class="kpi-card">
        <div class="kpi-icon" :style="{ color: item.color, backgroundColor: `${item.color}14` }">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="kpi-content">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <p>{{ item.desc }}</p>
        </div>
      </div>
    </section>

    <el-empty
      v-if="!loading && !hasOverviewData"
      class="page-empty"
      description="暂无统计数据，完成演示数据初始化或产生工单后再查看图表"
    />

    <section class="chart-grid" v-loading="loading">
      <div class="chart-panel chart-panel-wide">
        <header class="chart-head">
          <div>
            <h2>近 7 天工单趋势</h2>
            <p>按提交日期统计新增报修数量</p>
          </div>
        </header>
        <div v-show="hasTrendData" ref="trendChartRef" class="chart-box" />
        <el-empty v-if="!loading && !hasTrendData" class="chart-empty" description="暂无趋势数据" />
      </div>

      <div class="chart-panel">
        <header class="chart-head">
          <div>
            <h2>故障分类分布</h2>
            <p>查看不同故障类型占比</p>
          </div>
        </header>
        <div v-show="hasCategoryData" ref="categoryChartRef" class="chart-box" />
        <el-empty v-if="!loading && !hasCategoryData" class="chart-empty" description="暂无分类数据" />
      </div>

      <div class="chart-panel chart-panel-wide">
        <header class="chart-head">
          <div>
            <h2>维修人员处理量</h2>
            <p>对比维修人员总处理量和已完成量</p>
          </div>
        </header>
        <div v-show="hasWorkerData" ref="workerChartRef" class="chart-box" />
        <el-empty v-if="!loading && !hasWorkerData" class="chart-empty" description="暂无维修人员处理数据" />
      </div>

      <div class="chart-panel todo-panel">
        <header class="chart-head">
          <div>
            <h2>待办提醒</h2>
            <p>当前需要审核、分派或确认跟进的工单</p>
          </div>
          <el-button text @click="openOrderList()">查看全部</el-button>
        </header>

        <div v-if="todoCount" class="todo-list">
          <button
            v-for="item in todoItems"
            :key="item.id"
            class="todo-item"
            type="button"
            @click="openOrderList(item.status)"
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
        <el-empty v-else-if="!loading" class="chart-empty" description="暂无待办工单" />
      </div>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.stats-page {
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
    font-weight: 500;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    margin-bottom: 6px;
  }

  h1 {
    font-size: 26px;
    font-weight: 600;
    color: var(--text);
    margin: 0 0 4px;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: var(--text-muted);
  }
}

.head-actions {
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
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.kpi-card {
  min-height: 132px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
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

.page-empty {
  background: var(--bg);
  border: 1px dashed var(--border);
  border-radius: 8px;
}

.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.9fr);
  gap: 16px;
}

.chart-panel {
  min-height: 360px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chart-panel-wide {
  min-width: 0;
}

.chart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.chart-empty {
  flex: 1;
  min-height: 260px;
}

.todo-panel {
  min-height: 360px;
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

@media (max-width: 1320px) {
  .kpi-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }

  .head-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
