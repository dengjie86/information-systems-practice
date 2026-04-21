<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '232px'" class="aside">
      <div class="logo">
        <div class="logo-mark">D</div>
        <transition name="fade">
          <span v-if="!collapsed">DormRepair</span>
        </transition>
      </div>

      <el-scrollbar>
        <div v-if="!collapsed" class="menu-group">工作空间</div>
        <el-menu
          router
          :default-active="route.path"
          :collapse="collapsed"
          :collapse-transition="false"
          class="menu"
        >
          <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
            <el-icon><component :is="m.icon" /></el-icon>
            <template #title>{{ m.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>

      <div v-if="!collapsed" class="aside-footer">
        <div class="status-dot" />
        系统运行正常
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="toggle" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button text class="icon-btn"><el-icon><Search /></el-icon></el-button>
          <el-button text class="icon-btn"><el-icon><Bell /></el-icon></el-button>
          <div class="divider" />
          <el-dropdown @command="onCommand">
            <div class="user">
              <div class="avatar">管</div>
              <div class="user-meta">
                <div class="user-name">管理员</div>
                <div class="user-role">admin@dorm</div>
              </div>
              <el-icon class="chev"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  Odometer, Tickets, UserFilled, DataAnalysis,
  Fold, Expand, CaretBottom, Search, Bell
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)

const menus = [
  { path: '/home',  title: '工作台', icon: markRaw(Odometer) },
  { path: '/order', title: '工单管理', icon: markRaw(Tickets) },
  { path: '/user',  title: '用户管理', icon: markRaw(UserFilled) },
  { path: '/stats', title: '统计分析', icon: markRaw(DataAnalysis) }
]

const currentTitle = computed(
  () => menus.find(m => m.path === route.path)?.title ?? '首页'
)

const onCommand = async (cmd: string) => {
  if (cmd !== 'logout') return
  await ElMessageBox.confirm('确认退出当前账号？', '提示', { type: 'warning' })
  userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.layout {
  height: 100vh;
  background: var(--bg);
}

.aside {
  background: var(--bg-subtle);
  border-right: 1px solid var(--border);
  transition: width 0.2s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: -0.01em;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;

  .logo-mark {
    width: 26px;
    height: 26px;
    border-radius: 7px;
    background: var(--text);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 700;
  }
}

.menu-group {
  padding: 16px 20px 6px;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-soft);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.menu {
  border: none;
  background: transparent;
  padding: 0 10px;

  :deep(.el-menu-item) {
    height: 38px;
    line-height: 38px;
    font-size: 13px;
    color: var(--text-muted);
    border-radius: 6px;
    margin-bottom: 2px;
    padding-left: 12px !important;

    .el-icon {
      font-size: 16px;
      color: var(--text-soft);
    }

    &:hover {
      background: var(--bg-muted);
      color: var(--text);
      .el-icon { color: var(--text); }
    }

    &.is-active {
      background: var(--bg);
      color: var(--text);
      font-weight: 500;
      box-shadow: 0 0 0 1px var(--border);
      .el-icon { color: var(--text); }
    }
  }
}

.aside-footer {
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-muted);
  border-top: 1px solid var(--border);
  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--success);
    box-shadow: 0 0 0 3px var(--success-soft);
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg);
  border-bottom: 1px solid var(--border);
  padding: 0 20px;
  height: 60px !important;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .toggle {
    font-size: 18px;
    cursor: pointer;
    color: var(--text-muted);
    padding: 6px;
    border-radius: 6px;
    &:hover {
      color: var(--text);
      background: var(--bg-muted);
    }
  }

  :deep(.el-breadcrumb) {
    font-size: 13px;
    .el-breadcrumb__item { color: var(--text-muted); }
    .is-link { color: var(--text-muted) !important; font-weight: 400 !important; }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .icon-btn {
    width: 34px;
    height: 34px;
    color: var(--text-muted);
    &:hover {
      background: var(--bg-muted);
      color: var(--text);
    }
  }

  .divider {
    width: 1px;
    height: 20px;
    background: var(--border);
    margin: 0 8px;
  }

  .user {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    padding: 4px 10px 4px 4px;
    border-radius: 8px;
    transition: background 0.15s;
    &:hover { background: var(--bg-muted); }

    .avatar {
      width: 30px;
      height: 30px;
      border-radius: 7px;
      background: var(--text);
      color: #fff;
      font-size: 12px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .user-meta {
      display: flex;
      flex-direction: column;
      line-height: 1.2;
    }
    .user-name {
      font-size: 13px;
      color: var(--text);
      font-weight: 500;
    }
    .user-role {
      font-size: 11px;
      color: var(--text-soft);
    }
    .chev {
      font-size: 12px;
      color: var(--text-soft);
    }
  }
}

.main {
  background: var(--bg-subtle);
  padding: 24px 28px;
  overflow: auto;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.18s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(6px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
