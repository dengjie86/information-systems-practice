import request from '@/utils/request'

export type UploadFileType = 'repair' | 'result'

export const uploadImage = (file: File, type: UploadFileType) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)

  return request.post<any, string>('/files/upload', formData)
}
