/**
 * 홈 페이지 (TASK_WEB Step 2).
 * 로그인 시 사용자 정보·로그아웃 표시.
 */
import { Link } from 'react-router-dom';
import { authApi } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';

export default function HomePage() {
  const user = useAuthStore((s) => s.user);
  const accessToken = useAuthStore((s) => s.accessToken);
  const clearAuth = useAuthStore((s) => s.clearAuth);

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } finally {
      clearAuth();
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <nav className="mb-6 flex items-center justify-between border-b border-gray-200 pb-4">
        <h1 className="text-2xl font-bold text-gray-900">지도 기반 SNS</h1>
        <div className="flex gap-4">
          {accessToken ? (
            <>
              {user && (
                <>
                  <Link to="/me" className="text-sm text-gray-600 hover:text-gray-900">
                    {user.nickname}({user.email})
                  </Link>
                </>
              )}
              <button
                type="button"
                onClick={handleLogout}
                className="text-sm font-medium text-blue-600 hover:text-blue-500"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm font-medium text-blue-600 hover:text-blue-500">
                로그인
              </Link>
              <Link to="/signup" className="text-sm font-medium text-blue-600 hover:text-blue-500">
                회원가입
              </Link>
            </>
          )}
        </div>
      </nav>
      <p className="text-gray-600">환영합니다.</p>
    </div>
  );
}
