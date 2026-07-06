import { Injectable } from '@angular/core';

const EMAIL_KEY = 'auth_email';
const PASSWORD_KEY = 'auth_password';

@Injectable({ providedIn: 'root' })
export class AuthService {
  login(email: string, password: string): void {
    localStorage.setItem(EMAIL_KEY, email);
    localStorage.setItem(PASSWORD_KEY, password);
  }

  logout(): void {
    localStorage.removeItem(EMAIL_KEY);
    localStorage.removeItem(PASSWORD_KEY);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(EMAIL_KEY);
  }

  getEmail(): string {
    return localStorage.getItem(EMAIL_KEY) ?? '';
  }

  getAuthHeader(): string {
    const email = localStorage.getItem(EMAIL_KEY) ?? '';
    const password = localStorage.getItem(PASSWORD_KEY) ?? '';
    return `Basic ${btoa(`${email}:${password}`)}`;
  }
}
