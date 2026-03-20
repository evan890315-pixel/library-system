import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// ── Request interceptor: attach JWT ──────────────────────────────────────
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ── Response interceptor: handle 401 ─────────────────────────────────────
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ── Auth APIs ─────────────────────────────────────────────────────────────
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login:    (data) => api.post('/auth/login',    data)
}

// ── Book APIs ─────────────────────────────────────────────────────────────
export const bookApi = {
  getAll: () => api.get('/books')
}

// ── Borrow APIs ───────────────────────────────────────────────────────────
export const borrowApi = {
  borrow:      (data) => api.post('/borrow',        data),
  returnBook:  (data) => api.post('/borrow/return', data),
  myBorrowings: ()   => api.get('/borrow/my')
}

export default api
