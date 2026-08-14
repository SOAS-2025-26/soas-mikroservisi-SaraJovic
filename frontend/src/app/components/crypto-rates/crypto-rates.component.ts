import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-crypto-rates',
  imports: [CommonModule, FormsModule],
  templateUrl: './crypto-rates.component.html',
  styleUrl: './crypto-rates.component.scss',
})
export class CryptoRatesComponent {
  private readonly apiService = inject(ApiService);

  readonly cryptoCurrencies = ['BTC', 'ETH', 'BNB', 'SOL', 'ADA', 'DOGE', 'XRP', 'DOT', 'AVAX', 'MATIC'];
  readonly fiatCurrencies = ['USD', 'EUR', 'GBP', 'CHF'];

  fromCurrency = 'BTC';
  toCurrency = 'USD';
  rate: number | null = null;
  loading = false;
  error = '';

  getRate(): void {
    this.loading = true;
    this.error = '';
    this.rate = null;

    this.apiService.getCryptoRate(this.fromCurrency, this.toCurrency).subscribe({
      next: (result) => {
        this.rate = result.rate;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to fetch crypto rate.';
        this.loading = false;
      },
    });
  }
}
