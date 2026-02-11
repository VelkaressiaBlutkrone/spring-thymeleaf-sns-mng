/**
 * 인증 상태 골격 (TASK_WEB Step 1).
 * Step 2에서 로그인·토큰 갱신·보호 라우트 연동.
 */
import { create } from 'zustand';

export interface User {
  id: number;
  email: string;
  nickname: string;
  role: string;
}

interface AuthState {
  accessToken: string | null;
  user: User | null;
  setAuth: (accessToken: string, user: User) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  setAuth: (accessToken, user) => set({ accessToken, user }),
  clearAuth: () => set({ accessToken: null, user: null }),
}));
