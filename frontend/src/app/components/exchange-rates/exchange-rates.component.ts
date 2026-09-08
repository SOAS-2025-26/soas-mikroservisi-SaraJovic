import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService, ExchangeRateDto } from '../../services/api.service';

interface PopularConversion {
  from: string;
  to: string;
  rate: number | null;
  loading: boolean;
  errorMessage: string | null;
}

@Component({
  selector: 'app-exchange-rates',
  imports: [CommonModule, FormsModule],
  templateUrl: './exchange-rates.component.html',
  styleUrl: './exchange-rates.component.scss',
})
export class ExchangeRatesComponent implements OnInit {
  private readonly apiService = inject(ApiService);

  readonly fiatCurrencies = ['EUR', 'USD', 'GBP', 'CHF', 'RSD', 'JPY', 'CAD', 'AUD'];
  readonly cryptoCurrencies = ['BTC', 'ETH', 'BNB', 'SOL', 'ADA', 'DOGE', 'XRP', 'DOT', 'AVAX', 'MATIC'];
  readonly cryptoTargets = ['USD', 'EUR', 'GBP', 'CHF'];

  fiatFrom = 'EUR';
  fiatTo = 'USD';
  fiatRate: number | null = null;
  fiatLoading = false;
  fiatError = '';

  cryptoFrom = 'BTC';
  cryptoTo = 'USD';
  cryptoRate: number | null = null;
  cryptoLoading = false;
  cryptoError = '';

  popularConversions: PopularConversion[] = [
    { from: 'EUR', to: 'RSD', rate: null, loading: true, errorMessage: null },
    { from: 'USD', to: 'RSD', rate: null, loading: true, errorMessage: null },
    { from: 'BTC', to: 'USD', rate: null, loading: true, errorMessage: null },
    { from: 'ETH', to: 'USD', rate: null, loading: true, errorMessage: null },
    { from: 'USD', to: 'EUR', rate: null, loading: true, errorMessage: null },
  ];

  ngOnInit(): void {
    this.loadPopularConversions();
  }

  onGetFiatRate(): void {
    this.fiatLoading = true;
    this.fiatError = '';
    this.fiatRate = null;

    this.apiService.getExchangeRate(this.fiatFrom, this.fiatTo).subscribe({
      next: (result) => {
        this.fiatRate = result.rate;
        this.fiatLoading = false;
      },
      error: () => {
        this.fiatError = 'Failed to fetch exchange rate.';
        this.fiatLoading = false;
      },
    });
  }

  onGetCryptoRate(): void {
    this.cryptoLoading = true;
    this.cryptoError = '';
    this.cryptoRate = null;

    this.apiService.getCryptoRate(this.cryptoFrom, this.cryptoTo).subscribe({
      next: (result) => {
        this.cryptoRate = result.rate;
        this.cryptoLoading = false;
      },
      error: () => {
        this.cryptoError = 'Failed to fetch crypto rate.';
        this.cryptoLoading = false;
      },
    });
  }

  private loadPopularConversions(): void {
    this.popularConversions.forEach((conversion) => {
      const isCrypto = this.cryptoCurrencies.includes(conversion.from);
      const request = isCrypto
        ? this.apiService.getCryptoRate(conversion.from, conversion.to)
        : this.apiService.getExchangeRate(conversion.from, conversion.to);

      request.subscribe({
        next: (result: ExchangeRateDto) => {
          conversion.rate = result.rate;
          conversion.loading = false;
        },
        error: (err) => {
          console.error('Failed to load rate for', conversion.from, '->', conversion.to, err);
          conversion.errorMessage = err?.error?.message ?? err?.message ?? 'Failed to load';
          conversion.loading = false;
        },
      });
    });
  }
}
