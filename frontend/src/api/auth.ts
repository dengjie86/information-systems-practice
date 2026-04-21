import request from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  role: 'student' | 'admin' | 'worker'
  userInfo: Record<string, any>
}

// TODO: 联调时后端接口路径以实际为准
export const login = (data: LoginParams) =>
  request.post<any, LoginResult>('/auth/login', data)

export const logout = () => request.post('/auth/logout')

export const getCurrentUser = () => request.get('/auth/me')
