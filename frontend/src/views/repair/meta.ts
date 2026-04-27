import type { OrderStatus, Priority } from '@/api/repair'

export const statusMap: Record<OrderStatus, { label: string; type: 'primary' | 'success' | 'warning' | 'danger' | 'info'; className: string }> = {
  PENDING_AUDIT: { label: '待审核', type: 'warning', className: 'status-pending-audit' },
  PENDING_ASSIGN: { label: '待分派', type: 'warning', className: 'status-pending-assign' },
  PENDING_ACCEPT: { label: '待接单', type: 'primary', className: 'status-pending-accept' },
  PROCESSING: { label: '处理中', type: 'primary', className: 'status-processing' },
  PENDING_CONFIRM: { label: '待确认', type: 'warning', className: 'status-pending-confirm' },
  COMPLETED: { label: '已完成', type: 'success', className: 'status-completed' },
  REJECTED: { label: '已驳回', type: 'danger', className: 'status-rejected' },
  CLOSED: { label: '已关闭', type: 'info', className: 'status-closed' },
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

export const statusClass = (status: OrderStatus) =>
  ['status-tag', statusMap[status]?.className].filter(Boolean)
