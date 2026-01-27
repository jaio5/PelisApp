import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-register',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-page">
      <div class="login-card">
        <div class="brand">
          <div class="logo">P</div>
          <h1>Crear cuenta</h1>
        </div>

        <form (submit)="register($event)" #f="ngForm" class="login-form" novalidate>
          <div class="field">
            <label>Usuario</label>
            <input name="username" required minlength="3" #user="ngModel" ngModel />
            <small class="error" *ngIf="user.invalid && user.touched">Usuario inválido (min 3 caracteres)</small>
          </div>

          <div class="field">
            <label>Email</label>
            <input name="email" type="email" #email="ngModel" ngModel />
          </div>

          <div class="field">
            <label>Contraseña</label>
            <input name="password" type="password" required minlength="5" #pass="ngModel" ngModel />
            <small class="error" *ngIf="pass.invalid && pass.touched">Contraseña inválida (min 5 caracteres)</small>
          </div>

          <div *ngIf="apiError" class="error">{{ apiError }}</div>

          <button type="submit" [disabled]="f.invalid || loading" class="btn primary">
            {{ loading ? 'Creando...' : 'Crear cuenta' }}
          </button>
        </form>

        <div class="alt">
          <p>¿Ya tienes cuenta? <a routerLink="/login">Entrar</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [
    `:host{display:block;height:100%}
    .login-page{display:flex;align-items:center;justify-content:center;height:100vh;background:transparent;padding:1rem}
    .login-card{width:100%;max-width:420px;background:#fff;padding:2rem;border-radius:12px;box-shadow:0 10px 30px rgba(2,6,23,0.4);color:#0f172a}
    .brand{display:flex;align-items:center;gap:0.75rem;margin-bottom:1rem}
    .logo{width:48px;height:48px;border-radius:8px;background:linear-gradient(135deg,#ff416c,#8338ec);display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:1.25rem}
    h1{margin:0;font-size:1.5rem}
    .field{margin-top:0.75rem}
    label{display:block;font-size:0.85rem;color:#334155;margin-bottom:0.25rem}
    input{width:100%;padding:0.6rem 0.75rem;border:1px solid #e2e8f0;border-radius:8px;font-size:0.95rem}
    .error{color:#ef4444;font-size:0.85rem;margin-top:0.5rem}
    .btn{display:inline-block;padding:0.6rem 1rem;border-radius:8px;border:0;cursor:pointer}
    .btn.primary{background:linear-gradient(90deg,#ff416c,#8338ec);color:#fff;width:100%;margin-top:1rem}
    .alt{margin-top:1rem;text-align:center;color:#64748b}
    `
  ]
})
export class RegisterComponent {
  loading = false;
  apiError: string | null = null;

  constructor(private router: Router, private auth: AuthService) {}

  async register(e: Event) {
    e.preventDefault();
    const form = e.target as HTMLFormElement;
    const fd = new FormData(form);
    const username = String(fd.get('username') || '').trim();
    const password = String(fd.get('password') || '');
    const email = String(fd.get('email') || '').trim();

    if (username.length < 3 || password.length < 5) {
      this.apiError = 'Datos inválidos';
      return;
    }

    try {
      this.loading = true;
      this.apiError = null;
      await this.auth.register(username, password, email || undefined);
      this.router.navigate(['/login']);
    } catch (err: any) {
      this.apiError = err?.message || 'Error en el registro';
    } finally {
      this.loading = false;
    }
  }
}
