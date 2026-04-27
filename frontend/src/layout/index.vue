<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '232px'" class="aside">
      <div class="logo" :class="{ 'logo-collapsed': collapsed }">
        <div class="logo-mark">D</div>
        <span class="logo-text" :class="{ hidden: collapsed }">DormRepair</span>
      </div>

      <el-scrollbar>
        <div class="menu-group" :class="{ hidden: collapsed }">工作空间</div>
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
              <div class="avatar">{{ avatarChar }}</div>
              <div class="user-meta">
                <div class="user-name">{{ displayName }}</div>
                <div class="user-role">{{ displayRole }}</div>
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
import { useUserStore, type Role } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  Odometer, Tickets, UserFilled, DataAnalysis, EditPen, Menu as MenuIcon, Tools,
  Fold, Expand, CaretBottom, Search, Bell, House,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)

interface MenuItem {
  path: string
  title: string
  icon: ReturnType<typeof markRaw>
}

const menusByRole: Record<Role, MenuItem[]> = {
  STUDENT: [
    { path: '/home',          title: '工作台',   icon: markRaw(Odometer) },
    { path: '/repair/dorm',   title: '宿舍信息', icon: markRaw(House) },
    { path: '/repair/create', title: '报修申请', icon: markRaw(EditPen) },
    { path: '/repair/list',   title: '我的工单', icon: markRaw(Tickets) },
  ],
  ADMIN: [
    { path: '/home',       title: '工作台',   icon: markRaw(Odometer) },
    { path: '/order/list', title: '工单管理', icon: markRaw(Tickets) },
    { path: '/user/list',  title: '用户管理', icon: markRaw(UserFilled) },
    { path: '/category',   title: '故障分类', icon: markRaw(MenuIcon) },
    { path: '/stats',      title: '统计分析', icon: markRaw(DataAnalysis) },
  ],
  WORKER: [
    { path: '/home',      title: '工作台',   icon: markRaw(Odometer) },
    { path: '/work/list', title: '我的工单', icon: markRaw(Tools) },
  ],
}

const menus = computed(() =>
  menusByRole[userStore.role as Role] ?? menusByRole.ADMIN
)

const currentTitle = computed(
  () => menus.value.find(m => m.path === route.path)?.title ?? '首页'
)

const roleLabel: Record<Role, string> = {
  STUDENT: '学生',
  ADMIN: '管理员',
  WORKER: '维修人员',
}

const displayName = computed(() =>
  userStore.userInfo?.realName || userStore.userInfo?.username || '用户'
)

const displayRole = computed(() =>
  roleLabel[userStore.role as Role] ?? '未知'
)

const avatarChar = computed(() =>
  displayName.value.slice(-1)
)

const onCommand = async (cmd: string) => {
  if (cmd !== 'logout') return
  await ElMessageBox.confirm('确认退出当前账号？', '退出登录', {
    confirmButtonText: '确认退出',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'app-confirm-dialog',
  })
  userStore.logout()
  sessionStorage.setItem('fromLogout', '1')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.layout {
  height: 100%;
  background: var(--bg);
}

.aside {
  background: var(--bg-subtle);
  box-shadow: 3px 0 12px rgba(0, 0, 0, 0.053);
  z-index: 2;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
  flex-shrink: 0;
  white-space: nowrap;
  overflow: hidden;

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
    flex-shrink: 0;
  }

  .logo-text {
    transition: opacity 0.15s ease 0.1s;
  }

  &.logo-collapsed {
    padding: 0 19px;
    .logo-text { opacity: 0; transition-delay: 0s; }
  }
}

.menu-group {
  padding: 14px 20px 6px;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-soft);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  white-space: nowrap;
  overflow: hidden;
  max-height: 36px;
  transition: max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1) 0.05s,
              opacity 0.2s ease 0.1s,
              padding 0.3s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
  &.hidden {
    max-height: 0;
    opacity: 0;
    padding: 0 20px;
    transition: max-height 0.2s cubic-bezier(0.4, 0, 0.2, 1),
                opacity 0.1s ease,
                padding 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

.menu {
  border: none;
  background: transparent;
  padding: 0 10px;
  margin-top: 10px;

  &.el-menu--collapse {
    padding: 0 8px;

    :deep(.el-menu-item) {
      padding: 0 !important;
      display: flex !important;
      justify-content: center !important;
      align-items: center !important;
      min-width: auto !important;

      .el-icon { margin-right: 0 !important; }
      .el-menu-tooltip__trigger { padding: 0 !important; display: flex !important; justify-content: center !important; width: 100% !important; }
    }
  }

  :deep(.el-menu-item) {
    height: 38px;
    line-height: 38px;
    font-size: 13px;
    color: var(--text-muted);
    border-radius: 6px;
    margin-bottom: 2px;
    padding-left: 12px !important;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
    white-space: nowrap;

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


.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.035);
  padding: 0 20px;
  height: 60px !important;
  position: relative;
  z-index: 1;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .toggle {
    font-size: 22px;
    cursor: pointer;
    color: var(--text-muted);
    width: 36px;
    height: 36px;
    border-radius: 8px;
    transition: all 0.15s;
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

.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.18s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(6px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
