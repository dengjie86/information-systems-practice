<script setup lang="ts">
import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ArrowDown, ArrowUp, Delete, Plus, Refresh } from '@element-plus/icons-vue'
import {
  addCategory,
  deleteCategory,
  getAllCategories,
  updateCategory,
  updateCategoryStatus,
  type CategoryParams,
} from '@/api/admin'
import type { RepairCategory } from '@/api/repair'

const loading = shallowRef(false)
const submitting = shallowRef(false)
const deleting = shallowRef(false)
const sorting = shallowRef(false)
const dialogOpen = shallowRef(false)
const categories = ref<RepairCategory[]>([])
const formRef = ref<FormInstance>()

const form = reactive<CategoryParams>({
  id: undefined,
  categoryName: '',
  description: '',
  sortOrder: undefined,
})

const dialogTitle = computed(() => form.id ? '编辑故障分类' : '新增故障分类')
const enabledCount = computed(() => categories.value.filter(item => item.status === 1).length)
const nextSortOrder = computed(() =>
  (Math.max(0, ...categories.value.map(item => item.sortOrder ?? 0)) + 10)
)

const rules: FormRules = {
  categoryName: [{ required: true, message: '请填写分类名称', trigger: 'blur' }],
}

async function loadCategories() {
  loading.value = true
  try {
    categories.value = await getAllCategories()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = undefined
  form.categoryName = ''
  form.description = ''
  form.sortOrder = undefined
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  form.sortOrder = nextSortOrder.value
  dialogOpen.value = true
}

function openEdit(row: RepairCategory) {
  form.id = row.id
  form.categoryName = row.categoryName
  form.description = row.description ?? ''
  form.sortOrder = row.sortOrder ?? 0
  dialogOpen.value = true
}

async function submitForm() {
  if (submitting.value) return
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  try {
    if (form.id) {
      await updateCategory(form)
      ElMessage.success('分类已更新')
    } else {
      await addCategory(form)
      ElMessage.success('分类已新增')
    }
    dialogOpen.value = false
    await loadCategories()
  } finally {
    submitting.value = false
  }
}

function toCategoryParams(row: RepairCategory, sortOrder: number): CategoryParams {
  return {
    id: row.id,
    categoryName: row.categoryName,
    description: row.description,
    sortOrder,
  }
}

async function moveCategory(row: RepairCategory, direction: -1 | 1) {
  if (sorting.value) return
  const index = categories.value.findIndex(item => item.id === row.id)
  const targetIndex = index + direction
  if (index < 0 || targetIndex < 0 || targetIndex >= categories.value.length) return

  const next = [...categories.value]
  const [current] = next.splice(index, 1)
  next.splice(targetIndex, 0, current)

  sorting.value = true
  try {
    await Promise.all(
      next.map((item, itemIndex) => updateCategory(toCategoryParams(item, (itemIndex + 1) * 10)))
    )
    ElMessage.success('分类排序已更新')
    await loadCategories()
  } finally {
    sorting.value = false
  }
}

async function toggleStatus(row: RepairCategory) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateCategoryStatus(row.id, nextStatus)
  ElMessage.success(nextStatus === 1 ? '分类已启用' : '分类已停用')
  await loadCategories()
}

async function deleteCurrentCategory() {
  if (!form.id || deleting.value) return
  try {
    await ElMessageBox.confirm('仅未被任何工单使用的分类可以删除，确认删除该分类？', '删除分类', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'app-confirm-dialog app-confirm-dialog-danger',
      confirmButtonClass: 'app-confirm-danger-button',
    })
  } catch {
    return
  }

  deleting.value = true
  try {
    await deleteCategory(form.id)
    ElMessage.success('分类已删除')
    dialogOpen.value = false
    await loadCategories()
  } finally {
    deleting.value = false
  }
}

onMounted(loadCategories)
</script>

<template>
  <div class="page">
    <section class="page-head">
      <div>
        <div class="crumb">Categories</div>
        <h1>故障分类</h1>
        <p>维护学生报修时可选择的故障类型。</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Refresh" @click="loadCategories">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
      </div>
    </section>

    <section class="summary">
      <div><span>分类总数</span><b>{{ categories.length }}</b></div>
      <div><span>启用中</span><b>{{ enabledCount }}</b></div>
    </section>

    <section class="panel" v-loading="loading">
      <el-table :data="categories" row-key="id" empty-text="暂无分类数据">
        <el-table-column prop="categoryName" label="分类名称" min-width="160" />
        <el-table-column prop="description" label="说明" min-width="220">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column label="排序" width="82" align="center">
          <template #default="{ row, $index }">
            <div class="sort-cell">
              <div class="sort-actions">
                <el-button
                  text
                  :icon="ArrowUp"
                  :disabled="$index === 0 || sorting"
                  @click="moveCategory(row, -1)"
                />
                <el-button
                  text
                  :icon="ArrowDown"
                  :disabled="$index === categories.length - 1 || sorting"
                  @click="moveCategory(row, 1)"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button text @click="openEdit(row)">编辑</el-button>
            <el-button text @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogOpen" :title="dialogTitle" width="440px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model.trim="form.categoryName" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model.trim="form.description" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button
            v-if="form.id"
            type="danger"
            plain
            :icon="Delete"
            :loading="deleting"
            @click="deleteCurrentCategory"
          >
            删除
          </el-button>
          <div class="footer-actions">
            <el-button @click="dialogOpen = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
          </div>
        </div>
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

.head-actions {
  display: flex;
  gap: 8px;
}

.summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.sort-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.sort-actions {
  display: inline-flex;
  flex-direction: column;
  gap: 0;

  :deep(.el-button) {
    width: 22px;
    height: 18px;
    padding: 0;
    margin: 0;
  }
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

:deep(.el-button--primary) {
  background: var(--text);
  border-color: var(--text);
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
