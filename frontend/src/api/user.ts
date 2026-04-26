import request from '@/utils/request'
import type { UserInfo } from '@/stores/user'

export interface DormParams {
  dormBuilding: string
  dormRoom: string
  phone?: string
}

export const updateDorm = (data: DormParams) =>
  request.put<any, null>('/user/dorm', data)

export const getCurrentUser = () =>
  request.get<any, UserInfo>('/user/me')
