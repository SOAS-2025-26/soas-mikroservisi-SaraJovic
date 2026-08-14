import { Routes } from '@angular/router';

import { authGuard } from './guards/auth.guard';
import { CryptoRatesComponent } from './components/crypto-rates/crypto-rates.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ExchangeRatesComponent } from './components/exchange-rates/exchange-rates.component';
import { LoginComponent } from './pages/login/login.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'exchange-rates', component: ExchangeRatesComponent },
  { path: 'crypto-rates', component: CryptoRatesComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: '', redirectTo: 'exchange-rates', pathMatch: 'full' },
];
