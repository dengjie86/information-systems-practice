<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getUsers } from '@/api/admin'
import type { UserInfo } from '@/stores/user'

const loading = shallowRef(false)
const users = ref<UserInfo[]>([])

const roleLabel = {
  STUDENT: '学生',
  ADMIN: '管理员',
  WORKER: '维修人员',
} as const

const counts = computed(() => ({
  STUDENT: users.value.filter(item => item.role === 'STUDENT').length,
  ADMIN: users.value.filter(item => item.role === 'ADMIN').length,
  WORKER: users.value.filter(item => item.role === 'WORKER').length,
}))

async function loadUsers() {
  loading.value = true
  try {
    users.value = await getUsers()
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <div class="page">
    <section class="page-head">
      <div>
        <div class="crumb">Users</div>
        <h1>用户管理</h1>
        <p>查看学生、管理员与维修人员账号，用于工单审核和分派。</p>
      </div>
      <el-button :icon="Refresh" @click="loadUsers">刷新</el-button>
    </section>

    <section class="summary">
      <div><span>学生</span><b>{{ counts.STUDENT }}</b></div>
      <div><span>管理员</span><b>{{ counts.ADMIN }}</b></div>
      <div><span>维修人员</span><b>{{ counts.WORKER }}</b></div>
    </section>

    <section class="panel" v-loading="loading">
      <el-table :data="users" row-key="id" empty-text="暂无用户数据">
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="role" label="角色" width="110">
          <template #default="{ row }">{{ roleLabel[row.role as keyof typeof roleLabel] }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column label="宿舍" min-width="150">
          <template #default="{ row }">
            {{ row.dormBuilding && row.dormRoom ? `${row.dormBuilding} ${row.dormRoom}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
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

  div {
    padding: 16px 20px;
    border-right: 1px solid var(--border-soft);
  }

  div:last-child {
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

.panel {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
}

@media (max-width: 720px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary {
    grid-template-columns: 1fr;
  }
}
</style>
