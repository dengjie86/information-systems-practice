import request from '@/utils/request'

export type OrderStatus =
  | 'PENDING_AUDIT'
  | 'PENDING_ASSIGN'
  | 'REJECTED'
  | 'PENDING_ACCEPT'
  | 'PROCESSING'
  | 'PENDING_CONFIRM'
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
  submitTime?: string
  assignTime?: string
  acceptTime?: string
  finishTime?: string
  closeTime?: string
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

export const getOrderDetail = (id: number | string) =>
  request.get<any, RepairOrder>(`/orders/${id}`)
