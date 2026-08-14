import { CommonModule, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-exchange-rates',
  imports: [CommonModule, FormsModule, NgFor, NgIf],
  templateUrl: './exchange-rates.component.html',
  styleUrl: './exchange-rates.component.scss',
})
export class ExchangeRatesComponent {
  private readonly apiService = inject(ApiService);

  readonly currencies = ['EUR', 'USD', 'GBP', 'CHF', 'RSD', 'JPY', 'CAD', 'AUD'];

  fromCurrency = 'EUR';
  toCurrency = 'USD';
  rate: any = null;
  loading = false;
  error = '';

  getRate(): void {
    console.log('getRate called');
    console.log('from:', this.fromCurrency, 'to:', this.toCurrency);
    this.loading = true;
    this.error = '';
    this.rate = null;

    this.apiService.getExchangeRate(this.fromCurrency, this.toCurrency).subscribe({
      next: (result) => {
        console.log('result:', result);
        this.rate = result['rate'];
        this.loading = false;
      },
      error: (err) => {
        console.log('error:', err);
        this.error = 'Failed to fetch exchange rate.';
        this.loading = false;
      },
    });
  }
}
