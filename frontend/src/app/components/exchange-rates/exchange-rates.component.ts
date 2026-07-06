import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-exchange-rates',
  imports: [CommonModule, FormsModule],
  templateUrl: './exchange-rates.component.html',
  styleUrl: './exchange-rates.component.scss',
})
export class ExchangeRatesComponent {
  private readonly apiService = inject(ApiService);

  readonly currencies = ['EUR', 'USD', 'GBP', 'CHF', 'RSD', 'JPY', 'CAD', 'AUD'];

  fromCurrency = 'EUR';
  toCurrency = 'USD';
  rate: number | null = null;
  loading = false;
  error = '';

  getRate(): void {
    this.loading = true;
    this.error = '';
    this.rate = null;

    this.apiService.getExchangeRate(this.fromCurrency, this.toCurrency).subscribe({
      next: (result) => {
        this.rate = result.rate;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to fetch exchange rate.';
        this.loading = false;
      },
    });
  }
}
