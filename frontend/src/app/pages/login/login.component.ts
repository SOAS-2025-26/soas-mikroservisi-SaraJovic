import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { UserDto } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

const USERS_URL = 'http://localhost:8765/users/email';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  password = '';
  error = '';
  loading = false;

  login(): void {
    this.loading = true;
    this.error = '';

    const authHeader = `Basic ${btoa(`${this.email}:${this.password}`)}`;

    this.http
      .get<UserDto>(USERS_URL, {
        headers: { Authorization: authHeader },
        params: { email: this.email },
      })
      .subscribe({
        next: (user) => {
          this.authService.login(this.email, this.password, user.role);
          this.loading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.error =
            err.status === 401 || err.status === 403
              ? 'Invalid email or password'
              : 'Login failed. Please try again.';
          this.loading = false;
        },
      });
  }
}
