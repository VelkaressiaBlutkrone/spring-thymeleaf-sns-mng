/**
 * 앱 초기 로드 시 Refresh Token으로 세션 복원 (TASK_WEB Step 2).
 * RULE 6.1.6: Refresh Token은 HttpOnly 쿠키에 저장됨.
 */
import { useEffect } from 'react';
import { authApi } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';

export function AuthInitializer({ children }: { children: React.ReactNode }) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const setAuth = useAuthStore((s) => s.setAuth);

  useEffect(() => {
    if (accessToken) return;

    authApi
      .refresh()
      .then(({ data }) => {
        setAuth(data.accessToken, null);
        return authApi.getMe();
      })
      .then(({ data }) => {
        useAuthStore.getState().setUser({
          id: data.id,
          email: data.email,
          nickname: data.nickname,
          role: data.role,
        });
      })
      .catch(() => {
        // Refresh 실패 시 무시 (비로그인 상태 유지)
      });
  }, []);

  return <>{children}</>;
}
