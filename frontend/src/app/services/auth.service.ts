import { Injectable } from '@angular/core';

const EMAIL_KEY = 'auth_email';
const PASSWORD_KEY = 'auth_password';
const ROLE_KEY = 'auth_role';

@Injectable({ providedIn: 'root' })
export class AuthService {
  login(email: string, password: string, role: string): void {
    localStorage.setItem(EMAIL_KEY, email);
    localStorage.setItem(PASSWORD_KEY, password);
    localStorage.setItem(ROLE_KEY, role);
  }

  logout(): void {
    localStorage.removeItem(EMAIL_KEY);
    localStorage.removeItem(PASSWORD_KEY);
    localStorage.removeItem(ROLE_KEY);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(EMAIL_KEY);
  }

  getEmail(): string {
    return localStorage.getItem(EMAIL_KEY) ?? '';
  }

  getPassword(): string {
    return localStorage.getItem(PASSWORD_KEY) ?? '';
  }

  getRole(): string {
    return localStorage.getItem(ROLE_KEY) ?? '';
  }

  getAuthHeader(): string {
    const email = this.getEmail();
    const password = this.getPassword();
    return `Basic ${btoa(`${email}:${password}`)}`;
  }
}
