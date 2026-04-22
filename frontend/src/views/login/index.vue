<template>
  <div class="login-page" :class="{ revealed }">
    <aside class="brand">
      <div class="brand-inner">
        <div class="logo">
          <div class="logo-mark">D</div>
          <span>DormRepair</span>
        </div>

        <div class="hero">
          <div class="badge">
            <span class="dot" />
            v0.1 · 创业学院 · 信息系统实践
          </div>
          <h1>让每一次报修<br />都有迹可循。</h1>
          <p>从学生提报、管理员审核分派，到维修师傅处理回执，全流程在线、可追溯、可统计。</p>
        </div>

        <div class="meta">
          <div class="meta-item">
            <div class="meta-num">3</div>
            <div class="meta-label">角色</div>
          </div>
          <div class="meta-item">
            <div class="meta-num">6</div>
            <div class="meta-label">工单状态</div>
          </div>
          <div class="meta-item">
            <div class="meta-num">24h</div>
            <div class="meta-label">响应时效</div>
          </div>
        </div>
      </div>
    </aside>

    <section class="form-side">
      <div class="form-box">
        <header>
          <h2>登录账号</h2>
          <p>账号由管理员统一分配，首次登录请联系辅导员。</p>
        </header>

        <el-form :model="form" size="large" @keyup.enter="handleLogin">
          <el-form-item>
            <label class="label">用户名 / 学号</label>
            <el-input v-model="form.username" placeholder="请输入" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <label class="label">密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <div class="extra">
            <el-checkbox v-model="remember">7 天内免登录</el-checkbox>
            <el-link :underline="false" class="link">忘记密码？</el-link>
          </div>

          <el-button type="primary" :loading="loading" class="btn-primary" @click="handleLogin">
            登录
          </el-button>
        </el-form>

        <footer>
          <span>© {{ year }} DormRepair</span>
          <span class="sep" />
          <el-link :underline="false" class="link">使用协议</el-link>
          <el-link :underline="false" class="link">隐私政策</el-link>
        </footer>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login as loginApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const remember = ref(true)
const year = new Date().getFullYear()

// 退出登录回来跳过动画，其余情况正常播放
const fromLogout = sessionStorage.getItem('fromLogout')
sessionStorage.removeItem('fromLogout')
const revealed = ref(!!fromLogout)
onMounted(() => {
  if (!fromLogout) {
    setTimeout(() => (revealed.value = true), 850)
  }
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  loading.value = true
  try {
    const res = await loginApi(form.value)
    userStore.setToken(res.token)
    userStore.setUserInfo(res.userInfo)
    ElMessage.success('登录成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background: var(--bg);
}

.brand {
  flex: 1.2 1 0;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-subtle);
  border-right: 1px solid var(--border);
  position: relative;

  // 极淡点阵网格，Linear/Vercel 登录页常用
  background-image:
    radial-gradient(circle at 1px 1px, rgba(9, 9, 11, 0.06) 1px, transparent 0);
  background-size: 24px 24px;
}

.brand-inner {
  width: 100%;
  max-width: 520px;
  padding: 56px 72px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 560px;

  > * {
    opacity: 0;
    animation: brand-in 0.8s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  }
  > *:nth-child(1) { animation-delay: 0.1s; }
  > *:nth-child(2) { animation-delay: 0.28s; }
  > *:nth-child(3) { animation-delay: 0.36s; }
}

@keyframes brand-in {
  from { opacity: 0; transform: translateY(14px); }
  to   { opacity: 1; transform: translateY(0); }
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--text);

  .logo-mark {
    width: 28px;
    height: 28px;
    border-radius: 7px;
    background: var(--text);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
  }
}

.hero {
  margin: 80px 0;

  .badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 5px 12px;
    border: 1px solid var(--border);
    background: var(--bg);
    border-radius: 999px;
    font-size: 12px;
    color: var(--text-muted);
    margin-bottom: 28px;

    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--success);
      box-shadow: 0 0 0 3px var(--success-soft);
    }
  }

  h1 {
    font-size: 44px;
    line-height: 1.15;
    font-weight: 600;
    letter-spacing: -0.03em;
    color: var(--text);
    margin: 0 0 20px;
  }

  p {
    font-size: 15px;
    line-height: 1.7;
    color: var(--text-muted);
    margin: 0;
    max-width: 380px;
  }
}

.meta {
  display: flex;
  gap: 48px;
  padding-top: 24px;
  border-top: 1px solid var(--border);
}
.meta-item {
  .meta-num {
    font-size: 22px;
    font-weight: 600;
    color: var(--text);
    letter-spacing: -0.02em;
  }
  .meta-label {
    font-size: 12px;
    color: var(--text-soft);
    margin-top: 2px;
  }
}

.form-side {
  flex: 0 0 0;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 60px;
  background: var(--bg);
  overflow: hidden;
  opacity: 0;
  transition:
    flex 0.55s cubic-bezier(0.83, 0, 0.17, 1),
    opacity 0.3s ease 0.15s;
}

.login-page.revealed .form-side {
  flex: 0.4 0 480px;
  opacity: 1;
}

.form-box {
  width: 100%;
  max-width: 440px;
  padding: 40px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-top-color: rgba(255, 255, 255, 0.7);
  border-left-color: rgba(255, 255, 255, 0.6);
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 16px 48px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  opacity: 0;
  transform: translateX(-16px);
  transition:
    opacity 0.35s ease 0.25s,
    transform 0.4s cubic-bezier(0.22, 1, 0.36, 1) 0.25s;

  .login-page.revealed & {
    opacity: 1;
    transform: translateX(0);
  }

  header {
    margin-bottom: 36px;
    h2 {
      font-size: 26px;
      font-weight: 600;
      letter-spacing: -0.02em;
      color: var(--text);
      margin: 0 0 8px;
    }
    p {
      margin: 0;
      color: var(--text-muted);
      font-size: 13px;
      line-height: 1.6;
    }
  }

  .label {
    font-size: 13px;
    font-weight: 500;
    color: var(--text);
    margin-bottom: 6px;
    display: block;
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
    display: block;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px var(--border) inset;
    transition: box-shadow 0.15s;
    &:hover {
      box-shadow: 0 0 0 1px #a1a1aa inset;
    }
    &.is-focus {
      box-shadow: 0 0 0 1px var(--primary) inset, 0 0 0 3px var(--primary-soft) !important;
    }
  }

  .extra {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 4px 0 20px;
    font-size: 13px;
  }

  .btn-primary {
    width: 100%;
    height: 42px;
    border-radius: 8px;
    background: var(--text);
    border-color: var(--text);
    font-weight: 500;
    font-size: 14px;
    letter-spacing: 0.02em;
    &:hover,
    &:focus {
      background: #27272a;
      border-color: #27272a;
    }
  }

  footer {
    margin-top: 32px;
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
    color: var(--text-soft);

    .sep {
      width: 1px;
      height: 10px;
      background: var(--border);
    }
  }
}

.link {
  font-size: 13px;
  color: var(--text-muted) !important;
  &:hover {
    color: var(--text) !important;
  }
}

@media (max-width: 960px) {
  .brand { display: none; }
  .form-side {
    flex: 1 !important;
    max-width: none;
    opacity: 1 !important;
  }
  .form-box {
    opacity: 1 !important;
    transform: none !important;
  }
}
</style>
