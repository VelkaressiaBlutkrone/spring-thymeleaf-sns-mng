/**
 * 인증 API (TASK_WEB Step 2).
 * RULE 6.1~6.5: JWT 로그인·토큰 갱신·로그아웃.
 */
import { apiClient } from './client';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface MemberResponse {
  id: number;
  email: string;
  nickname: string;
  role: string;
  createdAt: string;
}

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<LoginResponse>('/api/auth/login', data),

  refresh: () => apiClient.post<LoginResponse>('/api/auth/refresh'),

  logout: () => apiClient.post('/api/auth/logout'),

  getMe: () => apiClient.get<MemberResponse>('/api/auth/me'),
};
