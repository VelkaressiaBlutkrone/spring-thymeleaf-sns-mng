/**
 * 회원가입 페이지 (TASK_WEB Step 2).
 * @Valid 검증 오류(fieldErrors) 표시. RULE 1.5.6: XSS 방지.
 */
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { membersApi, type ErrorResponse } from '@/api/members';

interface FieldError {
  field: string;
  value: string | null;
  reason: string;
}

export default function SignupPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldError[]>([]);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);
    setFieldErrors([]);
    setLoading(true);
    try {
      await membersApi.signup({ email, password, nickname });
      navigate('/login', { state: { message: '회원가입이 완료되었습니다. 로그인해 주세요.' } });
    } catch (err: unknown) {
      const res = (err as { response?: { data?: ErrorResponse; status?: number } })?.response;
      const data = res?.data;
      if (data?.fieldErrors && Array.isArray(data.fieldErrors)) {
        setFieldErrors(data.fieldErrors);
        setMessage(data.message ?? '입력값을 확인해 주세요.');
      } else if (data?.code === 'E006') {
        setMessage('이미 사용 중인 이메일입니다.');
      } else {
        setMessage(data?.message ?? '회원가입에 실패했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const getFieldError = (field: string) =>
    fieldErrors.find((e) => e.field === field)?.reason;

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm rounded-lg border border-gray-200 bg-white p-6 shadow">
        <h2 className="text-lg font-semibold text-gray-900">회원가입</h2>
        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              이메일
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
            />
            {getFieldError('email') && (
              <p className="mt-1 text-sm text-red-600">{getFieldError('email')}</p>
            )}
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700">
              비밀번호 (8자 이상)
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
              autoComplete="new-password"
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
            />
            {getFieldError('password') && (
              <p className="mt-1 text-sm text-red-600">{getFieldError('password')}</p>
            )}
          </div>
          <div>
            <label htmlFor="nickname" className="block text-sm font-medium text-gray-700">
              닉네임
            </label>
            <input
              id="nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              required
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
            />
            {getFieldError('nickname') && (
              <p className="mt-1 text-sm text-red-600">{getFieldError('nickname')}</p>
            )}
          </div>
          {message && !fieldErrors.length && (
            <p className="text-sm text-red-600" role="alert">
              {message}
            </p>
          )}
          {message && fieldErrors.length > 0 && (
            <p className="text-sm text-amber-600" role="alert">
              {message}
            </p>
          )}
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>
        <p className="mt-4 text-center text-sm text-gray-500">
          이미 계정이 있으신가요?{' '}
          <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
}
