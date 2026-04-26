import type { OrderStatus, Priority } from '@/api/repair'

export const statusMap: Record<OrderStatus, { label: string; type: 'primary' | 'success' | 'warning' | 'danger' | 'info' }> = {
  PENDING_AUDIT: { label: '待审核', type: 'warning' },
  PENDING_ASSIGN: { label: '待分派', type: 'warning' },
  REJECTED: { label: '已驳回', type: 'danger' },
  PENDING_ACCEPT: { label: '待接单', type: 'warning' },
  PROCESSING: { label: '处理中', type: 'primary' },
  PENDING_CONFIRM: { label: '待确认', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'success' },
  CLOSED: { label: '已关闭', type: 'info' },
}

export const priorityMap: Record<Priority, string> = {
  LOW: '低',
  NORMAL: '普通',
  HIGH: '高',
  URGENT: '紧急',
}

export const formatTime = (val?: string) => {
  if (!val) return '-'
  return val.replace('T', ' ').slice(0, 16)
}
