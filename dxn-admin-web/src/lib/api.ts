export const TOKEN_KEY = "dxn_admin_token"

export class ApiError extends Error {
  status: number
  errors: Record<string, string[]>
  constructor(status: number, message: string, errors: Record<string, string[]> = {}) {
    super(message)
    this.status = status
    this.errors = errors
  }
}

export async function api<T>(
  path: string,
  opts: { method?: string; json?: unknown; body?: FormData } = {},
): Promise<T> {
  const headers: Record<string, string> = { Accept: "application/json" }
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) headers.Authorization = `Bearer ${token}`
  let body: BodyInit | undefined
  if (opts.json !== undefined) {
    headers["Content-Type"] = "application/json"
    body = JSON.stringify(opts.json)
  } else if (opts.body) {
    body = opts.body
  }

  // Production builds set VITE_API_URL="" so all calls stay on the same origin.
  const base = import.meta.env.VITE_API_URL ?? ""
  const res = await fetch(`${base}${path}`, {
    method: opts.method ?? (body ? "POST" : "GET"),
    headers,
    body,
  })

  // A failed login is also a 401; let the caller show the message instead of redirecting.
  if (res.status === 401 && path !== "/api/auth/login") {
    localStorage.removeItem(TOKEN_KEY)
    // Keep the redirect inside the /admin basename used in production.
    window.location.assign("/admin/login")
  }
  if (res.status === 204) return undefined as T
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new ApiError(res.status, data.message ?? res.statusText, data.errors)
  return data as T
}
