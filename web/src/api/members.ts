/**
 * 회원 API (TASK_WEB Step 2).
 */
import { apiClient } from './client';

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export interface MemberResponse {
  id: number;
  email: string;
  nickname: string;
  role: string;
  createdAt: string;
}

export interface ErrorResponse {
  code: string;
  message: string;
  fieldErrors?: Array<{ field: string; value: string | null; reason: string }>;
}

export const membersApi = {
  signup: (data: SignupRequest) =>
    apiClient.post<MemberResponse>('/api/members', data),
};
