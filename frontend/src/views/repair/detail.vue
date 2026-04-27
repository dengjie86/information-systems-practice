<template>
  <div class="page" v-loading="loading">
    <section class="page-head">
      <div>
        <div class="crumb">{{ detail?.orderNo || 'Order Detail' }}</div>
        <h1>{{ detail?.title || '工单详情' }}</h1>
        <p>{{ detail ? `${detail.dormBuilding} ${detail.dormRoom}` : '正在读取工单信息' }}</p>
      </div>
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
    </section>

    <el-empty v-if="!loading && !detail" description="没有找到工单" />

    <template v-else-if="detail">
      <section class="summary">
        <div>
          <span>当前状态</span>
          <b>{{ statusMap[detail.status]?.label }}</b>
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

        <div v-if="detail.imageUrl" class="image-line">
          <span>故障图片</span>
          <el-link :href="detail.imageUrl" target="_blank" :underline="false">{{ detail.imageUrl }}</el-link>
        </div>

        <div v-if="detail.rejectReason || detail.adminRemark" class="notice">
          <b>{{ detail.rejectReason ? '驳回原因' : '管理员备注' }}</b>
          <p>{{ detail.rejectReason || detail.adminRemark }}</p>
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
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOrderDetail, type RepairOrder } from '@/api/repair'
import { formatTime, priorityMap, statusClass, statusMap } from './meta'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<RepairOrder>()

const timeline = computed(() => {
  const item = detail.value
  if (!item) return []
  return [
    { label: '提交报修', time: formatTime(item.submitTime), done: true },
    { label: '管理员审核', time: item.status === 'REJECTED' ? '已驳回' : formatTime(item.assignTime), done: item.status !== 'PENDING_AUDIT' },
    { label: '维修人员接单', time: formatTime(item.acceptTime), done: !!item.acceptTime },
    { label: '维修完成', time: formatTime(item.finishTime), done: !!item.finishTime },
    { label: '工单关闭', time: formatTime(item.closeTime), done: !!item.closeTime },
  ]
})

const loadDetail = async () => {
  loading.value = true
  detail.value = await getOrderDetail(String(route.params.id)).finally(() => { loading.value = false })
}

onMounted(loadDetail)
</script>

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

@media (max-width: 820px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
