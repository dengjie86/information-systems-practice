import request from '@/utils/request'
import type { UserInfo } from '@/stores/user'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userInfo: UserInfo
}

export const login = (data: LoginParams) =>
  request.post<any, LoginResult>('/auth/login', data)

export const getCurrentUser = () =>
  request.get<any, UserInfo>('/user/me')
