import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private storageKey = 'pelisapp_token';
  private userKey = 'pelisapp_user';
  private getStorage(): Storage | null {
    try {
      if (typeof window !== 'undefined' && window.localStorage) return window.localStorage;
    } catch {
      // SSR or no localStorage available
    }
    return null;
  }

  setToken(token: string) {
    const s = this.getStorage();
    if (s) s.setItem(this.storageKey, token);
  }

  getToken(): string | null {
    const s = this.getStorage();
    if (!s) return null;
    return s.getItem(this.storageKey);
  }

  setUser(username: string) {
    const s = this.getStorage();
    if (s) s.setItem(this.userKey, username);
  }

  getUser(): string | null {
    const s = this.getStorage();
    if (!s) return null;
    return s.getItem(this.userKey);
  }

  clear() {
    const s = this.getStorage();
    if (!s) return;
    s.removeItem(this.storageKey);
    s.removeItem(this.userKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  async login(username: string, password: string) {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!res.ok) throw new Error('Login failed');
    const data = await res.json();
    if (data.token) this.setToken(data.token);
    if (data.username) this.setUser(data.username);
    return data;
  }

  async register(username: string, password: string, email?: string) {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password, email })
    });
    if (!res.ok) throw new Error('Register failed');
    return res.json();
  }

  async fetchWithAuth(input: RequestInfo, init: RequestInit = {}) {
    const token = this.getToken();
    const headers = new Headers(init.headers || {});
    if (token) headers.set('Authorization', `Bearer ${token}`);
    init.headers = headers;
    return fetch(input, init);
  }
}
