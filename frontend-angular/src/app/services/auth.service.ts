import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse } from '../models/inventario.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly STORAGE_KEY = 'intranet_andina_session';
  readonly session = signal<LoginResponse | null>(this.loadSession());

  constructor(private http: HttpClient) {}

  private loadSession(): LoginResponse | null {
    try {
      const raw = localStorage.getItem(this.STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { username, password }).pipe(
      tap((res) => {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(res));
        this.session.set(res);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.session.set(null);
  }

  getToken(): string | null {
    return this.session()?.token ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasPermiso(codigo: string): boolean {
    const s = this.session();
    if (!s) return false;
    if (s.roles?.includes('ADMINISTRADOR')) return true;
    return s.permisos?.some(p => p.codigo === codigo) ?? false;
  }
}
