<template>
  <div class="home">
    <section class="page-head">
      <div>
        <div class="crumb">工作台</div>
        <h1>欢迎回来</h1>
        <p>{{ today }}，当前有 <b>28</b> 个待处理工单。</p>
      </div>
      <div class="head-actions">
        <el-button>导出报表</el-button>
        <el-button type="primary" :icon="Plus">新建工单</el-button>
      </div>
    </section>

    <section class="stats">
      <div v-for="(s, i) in stats" :key="s.label" class="stat" :class="{ divider: i < stats.length - 1 }">
        <div class="stat-label">
          <span class="marker" :style="{ background: s.color }" />
          {{ s.label }}
        </div>
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-trend" :class="s.trendType">
          <el-icon><component :is="s.trendType === 'up' ? CaretTop : CaretBottom" /></el-icon>
          {{ s.trend }}
          <span class="stat-trend-text">较上周</span>
        </div>
      </div>
    </section>

    <div class="grid">
      <div class="card panel">
        <header class="card-head">
          <div>
            <h3>工单趋势</h3>
            <p>近 7 日新建、完成数量对比</p>
          </div>
          <el-radio-group v-model="range" size="small">
            <el-radio-button value="7d">7 天</el-radio-button>
            <el-radio-button value="30d">30 天</el-radio-button>
            <el-radio-button value="90d">本季</el-radio-button>
          </el-radio-group>
        </header>
        <div class="chart-empty">
          <el-icon :size="22"><DataLine /></el-icon>
          <div>
            <div class="empty-title">图表待接入</div>
            <div class="empty-desc">联调完成后将在此处展示 ECharts 趋势图</div>
          </div>
        </div>
      </div>

      <div class="card">
        <header class="card-head">
          <div>
            <h3>待办提醒</h3>
            <p>今天需要你处理的 4 项</p>
          </div>
          <el-link underline="never" class="link-more">查看全部 →</el-link>
        </header>
        <ul class="todo-list">
          <li v-for="t in todos" :key="t.id">
            <div class="todo-id">#{{ t.id }}</div>
            <div class="todo-main">
              <div class="todo-title">{{ t.title }}</div>
              <div class="todo-meta">{{ t.dorm }} · {{ t.submit }}</div>
            </div>
            <span class="status" :class="t.level">{{ t.levelText }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Plus, CaretTop, CaretBottom, DataLine
} from '@element-plus/icons-vue'

const today = new Date().toLocaleDateString('zh-CN', {
  month: 'long', day: 'numeric', weekday: 'long'
})

const range = ref('7d')

// TODO: 联调后换真实接口数据
const stats = [
  { label: '待处理', value: 28,    trend: '12%', trendType: 'up',   color: '#2563eb' },
  { label: '处理中', value: 15,    trend: '3%',  trendType: 'down', color: '#d97706' },
  { label: '已完成', value: 126,   trend: '18%', trendType: 'up',   color: '#16a34a' },
  { label: '平均评分', value: '4.8', trend: '2%', trendType: 'up',   color: '#09090b' }
]

const todos = [
  { id: 2041, title: '宿舍热水器漏水',       dorm: '东 3-406',  submit: '10 分钟前', level: 'urgent', levelText: '紧急' },
  { id: 2040, title: '空调制冷不正常',       dorm: '西 5-218',  submit: '1 小时前',  level: 'normal', levelText: '常规' },
  { id: 2039, title: '门锁无法正常开启',     dorm: '东 1-303',  submit: '3 小时前',  level: 'normal', levelText: '常规' },
  { id: 2038, title: '卫生间排水缓慢',       dorm: '南 2-501',  submit: '昨天',      level: 'low',    levelText: '低' }
]
</script>

<style lang="scss" scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 24px;
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
    letter-spacing: -0.02em;
    color: var(--text);
    margin: 0 0 4px;
  }
  p {
    margin: 0;
    font-size: 13px;
    color: var(--text-muted);
    b { color: var(--text); font-weight: 600; }
  }
  .head-actions {
    display: flex;
    gap: 8px;
  }
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
}

.stat {
  padding: 20px 24px;
  &.divider { border-right: 1px solid var(--border); }

  .stat-label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: var(--text-muted);
    .marker {
      width: 8px;
      height: 8px;
      border-radius: 2px;
    }
  }
  .stat-value {
    font-size: 28px;
    font-weight: 600;
    letter-spacing: -0.02em;
    color: var(--text);
    margin: 8px 0 4px;
    font-variant-numeric: tabular-nums;
  }
  .stat-trend {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 500;
    &.up   { color: var(--success); }
    &.down { color: var(--danger); }
    .stat-trend-text {
      color: var(--text-soft);
      margin-left: 4px;
      font-weight: 400;
    }
  }
}

.grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.card {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid var(--border-soft);
  h3 {
    margin: 0 0 2px;
    font-size: 14px;
    font-weight: 600;
    color: var(--text);
  }
  p {
    margin: 0;
    font-size: 12px;
    color: var(--text-soft);
  }
}

.chart-empty {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: var(--text-soft);
  background:
    radial-gradient(circle at 1px 1px, rgba(9, 9, 11, 0.04) 1px, transparent 0);
  background-size: 16px 16px;
  .empty-title { font-size: 13px; font-weight: 500; color: var(--text-muted); }
  .empty-desc  { font-size: 12px; margin-top: 2px; }
}

.todo-list {
  list-style: none;
  margin: 0;
  padding: 8px 0;
  li {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 20px;
    cursor: pointer;
    transition: background 0.12s;
    &:hover { background: var(--bg-subtle); }
  }
}
.todo-id {
  font-size: 12px;
  color: var(--text-soft);
  font-variant-numeric: tabular-nums;
  font-weight: 500;
}
.todo-main {
  flex: 1;
  min-width: 0;
}
.todo-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.todo-meta {
  font-size: 12px;
  color: var(--text-soft);
}

.status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 500;
  &.urgent { color: var(--danger);  background: var(--danger-soft); }
  &.normal { color: var(--warning); background: var(--warning-soft); }
  &.low    { color: var(--text-muted); background: var(--bg-muted); }
}

.link-more {
  font-size: 12px;
  color: var(--text-muted) !important;
  &:hover { color: var(--text) !important; }
}

:deep(.el-button) {
  font-size: 13px;
  font-weight: 500;
  border-radius: 8px;
}
:deep(.el-button--primary:not(.is-disabled)) {
  background: var(--text);
  border-color: var(--text);
  &:hover, &:focus {
    background: #27272a;
    border-color: #27272a;
  }
}

@media (max-width: 960px) {
  .stats { grid-template-columns: repeat(2, 1fr); }
  .stat.divider:nth-child(2) { border-right: none; }
  .grid { grid-template-columns: 1fr; }
}
</style>
