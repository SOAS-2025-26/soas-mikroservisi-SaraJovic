import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AuthService } from './auth.service';

export interface ExchangeRateDto {
  from: string;
  to: string;
  rate: number;
}

export interface UserDto {
  id: number;
  email: string;
  password: string;
  role: string;
}

export interface BankAccountDto {
  id: number;
  email: string;
  currencyCode: string;
  amount: number;
}

export interface CryptoWalletDto {
  id: number;
  email: string;
  currencyCode: string;
  amount: number;
}

export interface ConversionResultDto {
  bankAccount: BankAccountDto | null;
  transactionMessage: string;
}

export interface TradeResultDto {
  bankAccount: BankAccountDto | null;
  cryptoWallet: CryptoWalletDto | null;
  transactionMessage: string;
}

const BASE_URL = 'http://localhost:8765';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({ Authorization: this.authService.getAuthHeader() });
  }

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

  getUsers(): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(`${BASE_URL}/users`, { headers: this.getAuthHeaders() });
  }

  createUser(user: Partial<UserDto>): Observable<UserDto> {
    return this.http.post<UserDto>(`${BASE_URL}/users`, user, { headers: this.getAuthHeaders() });
  }

  updateUser(id: number, user: Partial<UserDto>): Observable<UserDto> {
    return this.http.put<UserDto>(`${BASE_URL}/users/${id}`, user, { headers: this.getAuthHeaders() });
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/users/${id}`, { headers: this.getAuthHeaders() });
  }

  getAllBankAccounts(): Observable<BankAccountDto[]> {
    return this.http.get<BankAccountDto[]>(`${BASE_URL}/bank-accounts`, { headers: this.getAuthHeaders() });
  }

  getMyBankAccounts(): Observable<BankAccountDto[]> {
    return this.http.get<BankAccountDto[]>(`${BASE_URL}/bank-accounts/my`, { headers: this.getAuthHeaders() });
  }

  updateBankAccount(id: number, data: Partial<BankAccountDto>): Observable<BankAccountDto> {
    return this.http.put<BankAccountDto>(`${BASE_URL}/bank-accounts/${id}`, data, { headers: this.getAuthHeaders() });
  }

  addBankAccount(email: string, currency: string, amount: number): Observable<BankAccountDto> {
    return this.http.post<BankAccountDto>(`${BASE_URL}/bank-accounts/add`, null, {
      params: { email, currency, amount },
      headers: this.getAuthHeaders(),
    });
  }

  deleteBankAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/bank-accounts/${id}`, { headers: this.getAuthHeaders() });
  }

  getAllCryptoWallets(): Observable<CryptoWalletDto[]> {
    return this.http.get<CryptoWalletDto[]>(`${BASE_URL}/crypto-wallets`, { headers: this.getAuthHeaders() });
  }

  getMyCryptoWallets(): Observable<CryptoWalletDto[]> {
    return this.http.get<CryptoWalletDto[]>(`${BASE_URL}/crypto-wallets/my`, { headers: this.getAuthHeaders() });
  }

  updateCryptoWallet(id: number, data: Partial<CryptoWalletDto>): Observable<CryptoWalletDto> {
    return this.http.put<CryptoWalletDto>(`${BASE_URL}/crypto-wallets/${id}`, data, { headers: this.getAuthHeaders() });
  }

  addCryptoWallet(email: string, currency: string, amount: number): Observable<CryptoWalletDto> {
    return this.http.post<CryptoWalletDto>(`${BASE_URL}/crypto-wallets/add`, null, {
      params: { email, currency, amount },
      headers: this.getAuthHeaders(),
    });
  }

  deleteCryptoWallet(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/crypto-wallets/${id}`, { headers: this.getAuthHeaders() });
  }

  convertCurrency(from: string, to: string, quantity: number): Observable<ConversionResultDto> {
    return this.http.get<ConversionResultDto>(`${BASE_URL}/currency-conversion`, {
      params: { from, to, quantity },
      headers: this.getAuthHeaders(),
    });
  }

  trade(from: string, to: string, quantity: number): Observable<TradeResultDto> {
    return this.http.get<TradeResultDto>(`${BASE_URL}/trade-service`, {
      params: { from, to, quantity },
      headers: this.getAuthHeaders(),
    });
  }
}
