import { Routes } from '@angular/router';

import { authGuard } from './guards/auth.guard';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ExchangeRatesComponent } from './components/exchange-rates/exchange-rates.component';
import { LoginComponent } from './pages/login/login.component';

export const routes: Routes = [
  { path: '', component: ExchangeRatesComponent },
  { path: 'exchange-rates', component: ExchangeRatesComponent },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
];
