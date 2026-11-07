import type {
  DrinkDto,
  DrinkRequest,
  LoginResponse,
  OrderDto,
  StatsDto,
  UserDto,
} from './types'

/**
 * API client gắn token từ localStorage; mọi lỗi trả về body {error} được
 * throw Error(message) để trang hiện thông báo tiếng Việt từ server.
 */
const TOKEN_KEY = 'g04_token'
const LOGIN_KEY = 'g04_login'

export function saveSession(login: LoginResponse) {
  localStorage.setItem(TOKEN_KEY, login.token)
  localStorage.setItem(LOGIN_KEY, JSON.stringify(login))
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(LOGIN_KEY)
}

export function currentLogin(): LoginResponse | null {
  const raw = localStorage.getItem(LOGIN_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as LoginResponse
  } catch {
    return null
  }
}

export function currentToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(init.headers as Record<string, string> | undefined),
  }
  const token = currentToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const response = await fetch(path, { ...init, headers })
  if (response.status === 204) return undefined as T

  const body = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error((body as { error?: string }).error ?? `Lỗi ${response.status}`)
  }
  return body as T
}

// ----- auth -----
export function apiLogin(username: string, password: string) {
  return request<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

// ----- menu -----
export const listDrinks = () => request<DrinkDto[]>('/api/drinks')
export const createDrink = (body: DrinkRequest) =>
  request<DrinkDto>('/api/admin/drinks', { method: 'POST', body: JSON.stringify(body) })
export const updateDrink = (id: string, body: DrinkRequest) =>
  request<DrinkDto>(`/api/admin/drinks/${id}`, { method: 'PUT', body: JSON.stringify(body) })
export const deleteDrink = (id: string) =>
  request<void>(`/api/admin/drinks/${id}`, { method: 'DELETE' })

// ----- orders -----
export const listOrders = () => request<OrderDto[]>('/api/orders')
export const getOrder = (id: number) => request<OrderDto>(`/api/orders/${id}`)
export const createOrder = (customerName: string, items: Array<{ drinkId: string; quantity: number }>) =>
  request<OrderDto>('/api/orders', {
    method: 'POST',
    body: JSON.stringify({ customerName, items }),
  })

// ----- admin -----
export const adminOrders = () => request<OrderDto[]>('/api/admin/orders')
export const adminUsers = () => request<UserDto[]>('/api/admin/users')
export const changeRole = (username: string, role: string) =>
  request<UserDto>(`/api/admin/users/${username}/role`, {
    method: 'PUT',
    body: JSON.stringify({ role }),
  })
export const adminStats = () => request<StatsDto>('/api/admin/stats')

// ----- realtime -----
/** Đăng ký SSE bảng đơn realtime; trả hàm hủy cho useEffect cleanup. */
export function subscribeOrders(onUpdate: (orders: OrderDto[]) => void): () => void {
  const source = new EventSource('/api/stream/orders')
  source.addEventListener('orders', (event) => {
    try {
      onUpdate(JSON.parse((event as MessageEvent).data) as OrderDto[])
    } catch {
      // dữ liệu lỗi thì bỏ qua, đợt event kế tiếp sẽ chuẩn lại
    }
  })
  return () => source.close()
}
