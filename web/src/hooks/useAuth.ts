/**
 * 인증 훅 (TASK_WEB Step 2).
 */
import { useAuthStore } from '@/store/authStore';

export function useAuth() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);
  const setAuth = useAuthStore((s) => s.setAuth);
  const clearAuth = useAuthStore((s) => s.clearAuth);

  return {
    accessToken,
    user,
    isAuthenticated: !!accessToken,
    setAuth,
    clearAuth,
  };
}
