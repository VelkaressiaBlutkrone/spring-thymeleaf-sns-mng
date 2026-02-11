/**
 * 마이페이지 (TASK_WEB Step 2).
 * 보호 라우트 예시. Step 6에서 상세 구현.
 */
import { useAuth } from '@/hooks/useAuth';

export default function MePage() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <h2 className="text-xl font-semibold text-gray-900">마이페이지</h2>
      {user && (
        <div className="mt-4 rounded-lg border border-gray-200 bg-white p-4">
          <p className="text-sm text-gray-600">
            이메일: {user.email}
          </p>
          <p className="text-sm text-gray-600">
            닉네임: {user.nickname}
          </p>
          <p className="text-sm text-gray-600">
            역할: {user.role}
          </p>
        </div>
      )}
    </div>
  );
}
