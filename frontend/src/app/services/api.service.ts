import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AuthService } from './auth.service';

export interface ExchangeRateDto {
  from: string;
  to: string;
  rate: number;
}

const BASE_URL = 'http://localhost:8765';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  getExchangeRate(from: string, to: string): Observable<ExchangeRateDto> {
    return this.http.get<ExchangeRateDto>(`${BASE_URL}/currency-exchange`, {
      params: { from, to },
    });
  }

  getCryptoRate(from: string, to: string): Observable<ExchangeRateDto> {
    return this.http.get<ExchangeRateDto>(`${BASE_URL}/crypto-exchange`, {
      params: { from, to },
    });
  }
}
