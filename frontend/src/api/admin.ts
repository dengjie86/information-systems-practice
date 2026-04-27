import request from '@/utils/request'
import type { UserInfo } from '@/stores/user'
import type { OrderStatus, PageResult, Priority, RepairCategory } from './repair'

export interface AdminOrder {
  id: number
  orderNo: string
  userId: number
  studentName?: string
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
  assignedWorkerId?: number
  assignedWorkerName?: string
  rejectReason?: string
  adminRemark?: string
  dispatchRemark?: string
  submitTime?: string
  assignTime?: string
  acceptTime?: string
  finishTime?: string
  closeTime?: string
}

export interface AdminOrderQuery {
  pageNum: number
  pageSize: number
  status?: OrderStatus | ''
  userId?: number
  assignedWorkerId?: number
}

export interface AuditParams {
  adminRemark?: string
  rejectReason?: string
}

export interface AssignParams {
  workerId: number | null
  dispatchRemark?: string
}

export interface CategoryParams {
  id?: number
  categoryName: string
  description?: string
  sortOrder?: number
}

export const getAdminOrders = (params: AdminOrderQuery) =>
  request.get<any, PageResult<AdminOrder>>('/orders/admin', { params })

export const getAdminOrderDetail = (id: number | string) =>
  request.get<any, AdminOrder>(`/orders/admin/${id}`)

export const approveOrder = (id: number | string, data: AuditParams) =>
  request.post<any, null>(`/orders/admin/${id}/approve`, data)

export const rejectOrder = (id: number | string, data: AuditParams) =>
  request.post<any, null>(`/orders/admin/${id}/reject`, data)

export const assignOrder = (id: number | string, data: AssignParams) =>
  request.post<any, null>(`/orders/admin/${id}/assign`, data)

export const getUsers = () =>
  request.get<any, UserInfo[]>('/user/list')

export const getWorkers = () =>
  request.get<any, UserInfo[]>('/user/workers')

export const getAllCategories = () =>
  request.get<any, RepairCategory[]>('/category/all')

export const addCategory = (data: CategoryParams) =>
  request.post<any, null>('/category/add', data)

export const updateCategory = (data: CategoryParams) =>
  request.put<any, null>('/category/update', data)

export const updateCategoryStatus = (id: number, status: 0 | 1) =>
  request.put<any, null>(`/category/${id}/status`, null, { params: { status } })

export const deleteCategory = (id: number) =>
  request.delete<any, null>(`/category/${id}`)
