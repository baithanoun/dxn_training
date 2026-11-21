import { createContext, useContext, useEffect, useState, type ReactNode } from "react"
import { Navigate, Outlet } from "react-router-dom"
import { api, TOKEN_KEY } from "@/lib/api"
import type { User } from "@/lib/types"

interface AuthState {
  user: User | null
  loading: boolean
  login(login: string, password: string): Promise<void>
  logout(): Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(() => !!localStorage.getItem(TOKEN_KEY))

  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) return
    // /me returns a bare JsonResource, which Laravel wraps in {data: ...}.
    api<{ data: User }>("/api/auth/me")
      .then((r) => setUser(r.data))
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setLoading(false))
  }, [])

  const login = async (login: string, password: string) => {
    const r = await api<{ token: string; user: User }>("/api/auth/login", {
      json: { login, password },
    })
    if (r.user.role !== "ADMIN") {
      // Revoke the token we were just issued so it is not left valid server-side.
      await fetch(`${import.meta.env.VITE_API_URL}/api/auth/logout`, {
        method: "POST",
        headers: { Accept: "application/json", Authorization: `Bearer ${r.token}` },
      }).catch(() => {})
      throw new Error("هذا الحساب لا يملك صلاحية الإدارة")
    }
    localStorage.setItem(TOKEN_KEY, r.token)
    setUser(r.user)
  }

  const logout = async () => {
    try {
      await api("/api/auth/logout", { method: "POST" })
    } catch {
      // token may already be invalid; clear locally regardless
    }
    localStorage.removeItem(TOKEN_KEY)
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, loading, login, logout }}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider")
  return ctx
}

export function RequireAuth() {
  const { user, loading } = useAuth()
  if (loading) return <div dir="rtl" className="p-8 text-center text-muted-foreground">جارٍ التحميل...</div>
  return user ? <Outlet /> : <Navigate to="/login" replace />
}
