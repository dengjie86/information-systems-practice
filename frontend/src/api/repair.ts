import request from '@/utils/request'

export type OrderStatus =
  | 'PENDING_AUDIT'
  | 'PENDING_ASSIGN'
  | 'REJECTED'
  | 'PENDING_ACCEPT'
  | 'PROCESSING'
  | 'PENDING_CONFIRM'
  | 'CANCELLED'
  | 'COMPLETED'
  | 'CLOSED'

export type Priority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'

export interface RepairCategory {
  id: number
  categoryName: string
  description?: string
  sortOrder?: number
  status?: number
}

export interface RepairOrder {
  id: number
  orderNo: string
  title: string
  categoryId: number
  categoryName?: string
  description?: string
  imageUrl?: string
  dormBuilding?: string
  dormRoom?: string
  contactPhone?: string
  status: OrderStatus
  priority: Priority
  rejectReason?: string
  adminRemark?: string
  dispatchRemark?: string
  submitTime?: string
  assignTime?: string
  acceptTime?: string
  finishTime?: string
  closeTime?: string
  evaluation?: Evaluation
  records?: RepairRecord[]
}

export interface Evaluation {
  id: number
  orderId: number
  score: 1 | 3 | 5
  content?: string
  createTime?: string
}

export interface RepairRecord {
  id: number
  orderId: number
  workerId: number
  actionType: 'ACCEPT' | 'REJECT' | 'FINISH' | 'CANCEL' | 'CONFIRM' | 'RECORD'
  actionDesc?: string
  resultImage?: string
  statusBefore?: OrderStatus
  statusAfter?: OrderStatus
  actionTime?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface CreateOrderParams {
  title: string
  categoryId: number | null
  description?: string
  imageUrl?: string
  contactPhone?: string
  priority?: Priority
}

export type EditOrderParams = CreateOrderParams

export interface CancelOrderParams {
  cancelReason?: string
}

export interface EvaluateOrderParams {
  score: 1 | 3 | 5
  content?: string
}

export interface CreateOrderResult {
  id: number
  orderNo: string
  status: OrderStatus
}

export interface OrderQuery {
  pageNum: number
  pageSize: number
  status?: OrderStatus | ''
}

export const getCategories = () =>
  request.get<any, RepairCategory[]>('/category/list')

export const createOrder = (data: CreateOrderParams) =>
  request.post<any, CreateOrderResult>('/orders', data)

export const getMyOrders = (params: OrderQuery) =>
  request.get<any, PageResult<RepairOrder>>('/orders/my', { params })

export const getMyOrderCount = async (status: OrderStatus) => {
  const res = await getMyOrders({ pageNum: 1, pageSize: 1, status })
  return res.total
}

export const getOrderDetail = (id: number | string) =>
  request.get<any, RepairOrder>(`/orders/${id}`)

export const acceptOrder = (id: number | string, remark?: string) =>
  request.post<any, null>(`/orders/${id}/accept`, { remark })

export const rejectWorkerOrder = (id: number | string, rejectReason: string) =>
  request.post<any, null>(`/orders/${id}/worker-reject`, { rejectReason })

export const finishRepairOrder = (id: number | string, data: { actionDesc: string; resultImage?: string }) =>
  request.post<any, null>(`/orders/${id}/finish`, data)

export const editOrder = (id: number | string, data: EditOrderParams) =>
  request.put<any, null>(`/orders/${id}`, data)

export const cancelOrder = (id: number | string, data?: CancelOrderParams) =>
  request.post<any, null>(`/orders/${id}/cancel`, data ?? {})

export const confirmOrder = (id: number | string, data: EvaluateOrderParams) =>
  request.post<any, null>(`/orders/${id}/confirm`, data)
