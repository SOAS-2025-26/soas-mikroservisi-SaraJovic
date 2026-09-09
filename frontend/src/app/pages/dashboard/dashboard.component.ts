import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService, BankAccountDto, CryptoWalletDto, UserDto } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface UserFormState {
  email: string;
  password: string;
  role: string;
}

interface AddAccountFormState {
  email: string;
  currency: string;
  amount: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly apiService = inject(ApiService);
  private readonly authService = inject(AuthService);

  readonly fiatCurrencies = ['EUR', 'USD', 'GBP', 'CHF', 'RSD', 'JPY', 'CAD', 'AUD'];
  readonly cryptoCurrencies = ['BTC', 'ETH', 'BNB', 'SOL', 'ADA', 'DOGE', 'XRP', 'DOT', 'AVAX', 'MATIC'];
  readonly allCurrencies = [...this.fiatCurrencies, ...this.cryptoCurrencies];

  readonly role = this.authService.getRole();
  readonly email = this.authService.getEmail();
  readonly isOwnerOrAdmin = this.role === 'OWNER' || this.role === 'ADMIN';

  // Users management (OWNER/ADMIN)
  users: UserDto[] = [];
  usersLoading = false;
  userFormError = '';
  showAddUserForm = false;
  newUser: UserFormState = { email: '', password: '', role: 'USER' };
  editingUserId: number | null = null;
  editUserForm: UserFormState = { email: '', password: '', role: 'USER' };

  // Bank accounts management (OWNER/ADMIN)
  bankAccounts: BankAccountDto[] = [];
  bankAccountsLoading = false;
  editingBankAccountId: number | null = null;
  editBankAccountAmount = 0;
  showAddBankAccountForm = false;
  newBankAccount: AddAccountFormState = { email: '', currency: '', amount: 0 };
  bankAccountFormError = '';

  // Crypto wallets management (OWNER/ADMIN)
  cryptoWallets: CryptoWalletDto[] = [];
  cryptoWalletsLoading = false;
  editingCryptoWalletId: number | null = null;
  editCryptoWalletAmount = 0;
  showAddCryptoWalletForm = false;
  newCryptoWallet: AddAccountFormState = { email: '', currency: '', amount: 0 };
  cryptoWalletFormError = '';

  // USER: own accounts
  myBankAccounts: BankAccountDto[] = [];
  myCryptoWallets: CryptoWalletDto[] = [];

  // USER: currency conversion
  convFrom = 'EUR';
  convTo = 'USD';
  convQuantity = 0;
  conversionLoading = false;
  conversionResult = '';
  conversionError = '';

  // USER: crypto trading
  tradeFrom = 'BTC';
  tradeTo = 'USD';
  tradeQuantity = 0;
  tradeLoading = false;
  tradeResult = '';
  tradeError = '';

  get availableRoles(): string[] {
    return this.role === 'OWNER' ? ['ADMIN', 'USER'] : ['USER'];
  }

  get visibleUsers(): UserDto[] {
    if (this.role === 'ADMIN') {
      return this.users.filter((u) => u.role === 'USER');
    }
    return this.users;
  }

  get isAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  ngOnInit(): void {
    if (this.role === 'OWNER') {
      this.loadUsers();
    } else if (this.role === 'ADMIN') {
      this.loadUsers();
      this.loadBankAccounts();
      this.loadCryptoWallets();
    } else {
      this.loadMyAccounts();
    }
  }

  // --- Users management ---

  canEditUser(user: UserDto): boolean {
    if (this.role === 'OWNER') return true;
    if (this.role === 'ADMIN') return user.role === 'USER';
    return false;
  }

  canDeleteUser(user: UserDto): boolean {
    if (this.role === 'OWNER') return user.role !== 'OWNER';
    if (this.role === 'ADMIN') return user.role === 'USER';
    return false;
  }

  toggleAddUserForm(): void {
    this.showAddUserForm = !this.showAddUserForm;
    this.newUser = { email: '', password: '', role: 'USER' };
    this.userFormError = '';
  }

  cancelAddUser(): void {
    this.showAddUserForm = false;
  }

  submitAddUser(): void {
    this.userFormError = '';

    this.apiService.createUser(this.newUser).subscribe({
      next: () => {
        this.showAddUserForm = false;
        this.loadUsers();
      },
      error: (err) => {
        this.userFormError = err?.error?.message ?? 'Failed to create user.';
      },
    });
  }

  startEditUser(user: UserDto): void {
    this.editingUserId = user.id;
    this.editUserForm = { email: user.email, password: '', role: user.role };
    this.userFormError = '';
  }

  cancelEditUser(): void {
    this.editingUserId = null;
  }

  submitEditUser(user: UserDto): void {
    this.userFormError = '';

    const payload: Partial<UserDto> = {
      email: user.email,
      role: this.editUserForm.role,
    };
    if (this.editUserForm.password) {
      payload.password = this.editUserForm.password;
    }

    this.apiService.updateUser(user.id, payload).subscribe({
      next: () => {
        this.editingUserId = null;
        this.loadUsers();
      },
      error: (err) => {
        this.userFormError = err?.error?.message ?? 'Failed to update user.';
      },
    });
  }

  removeUser(user: UserDto): void {
    if (
      !confirm(
        `Da li ste sigurni da želite da obrišete korisnika ${user.email}? Ovo će takođe obrisati njegov bankovni nalog i crypto novčanik.`,
      )
    ) {
      return;
    }

    this.userFormError = '';

    this.apiService.deleteUser(user.id).subscribe({
      next: () => this.loadUsers(),
      error: (err) => {
        this.userFormError = err?.error?.message ?? 'Failed to delete user.';
      },
    });
  }

  private loadUsers(): void {
    this.usersLoading = true;

    this.apiService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.usersLoading = false;
      },
      error: () => {
        this.usersLoading = false;
      },
    });
  }

  // --- Bank accounts management ---

  toggleAddBankAccountForm(): void {
    this.showAddBankAccountForm = !this.showAddBankAccountForm;
    this.newBankAccount = { email: '', currency: '', amount: 0 };
    this.bankAccountFormError = '';
  }

  cancelAddBankAccount(): void {
    this.showAddBankAccountForm = false;
  }

  submitAddBankAccount(): void {
    this.bankAccountFormError = '';

    this.apiService
      .addBankAccount(this.newBankAccount.email, this.newBankAccount.currency, this.newBankAccount.amount)
      .subscribe({
        next: () => {
          this.showAddBankAccountForm = false;
          this.loadBankAccounts();
        },
        error: (err) => {
          this.bankAccountFormError = err?.error?.message ?? 'Failed to add bank account.';
        },
      });
  }

  startEditBankAccount(account: BankAccountDto): void {
    this.editingBankAccountId = account.id;
    this.editBankAccountAmount = account.amount;
  }

  cancelEditBankAccount(): void {
    this.editingBankAccountId = null;
  }

  submitEditBankAccount(account: BankAccountDto): void {
    this.apiService
      .updateBankAccount(account.id, { ...account, amount: this.editBankAccountAmount })
      .subscribe({
        next: () => {
          this.editingBankAccountId = null;
          this.loadBankAccounts();
        },
      });
  }

  removeBankAccount(account: BankAccountDto): void {
    if (
      !confirm(
        `Da li ste sigurni da želite da obrišete ${account.currencyCode} nalog korisnika ${account.email}?`,
      )
    ) {
      return;
    }

    this.bankAccountFormError = '';

    this.apiService.deleteBankAccount(account.id).subscribe({
      next: () => this.loadBankAccounts(),
      error: (err) => {
        this.bankAccountFormError = err?.error?.message ?? 'Failed to delete bank account.';
      },
    });
  }

  private loadBankAccounts(): void {
    this.bankAccountsLoading = true;

    this.apiService.getAllBankAccounts().subscribe({
      next: (accounts) => {
        this.bankAccounts = [...accounts].sort((a, b) => a.email.localeCompare(b.email));
        this.bankAccountsLoading = false;
      },
      error: () => {
        this.bankAccountsLoading = false;
      },
    });
  }

  // --- Crypto wallets management ---

  toggleAddCryptoWalletForm(): void {
    this.showAddCryptoWalletForm = !this.showAddCryptoWalletForm;
    this.newCryptoWallet = { email: '', currency: '', amount: 0 };
    this.cryptoWalletFormError = '';
  }

  cancelAddCryptoWallet(): void {
    this.showAddCryptoWalletForm = false;
  }

  submitAddCryptoWallet(): void {
    this.cryptoWalletFormError = '';

    this.apiService
      .addCryptoWallet(this.newCryptoWallet.email, this.newCryptoWallet.currency, this.newCryptoWallet.amount)
      .subscribe({
        next: () => {
          this.showAddCryptoWalletForm = false;
          this.loadCryptoWallets();
        },
        error: (err) => {
          this.cryptoWalletFormError = err?.error?.message ?? 'Failed to add crypto wallet.';
        },
      });
  }

  startEditCryptoWallet(wallet: CryptoWalletDto): void {
    this.editingCryptoWalletId = wallet.id;
    this.editCryptoWalletAmount = wallet.amount;
  }

  cancelEditCryptoWallet(): void {
    this.editingCryptoWalletId = null;
  }

  submitEditCryptoWallet(wallet: CryptoWalletDto): void {
    this.apiService
      .updateCryptoWallet(wallet.id, { ...wallet, amount: this.editCryptoWalletAmount })
      .subscribe({
        next: () => {
          this.editingCryptoWalletId = null;
          this.loadCryptoWallets();
        },
      });
  }

  removeCryptoWallet(wallet: CryptoWalletDto): void {
    if (
      !confirm(
        `Da li ste sigurni da želite da obrišete ${wallet.currencyCode} novčanik korisnika ${wallet.email}?`,
      )
    ) {
      return;
    }

    this.cryptoWalletFormError = '';

    this.apiService.deleteCryptoWallet(wallet.id).subscribe({
      next: () => this.loadCryptoWallets(),
      error: (err) => {
        this.cryptoWalletFormError = err?.error?.message ?? 'Failed to delete crypto wallet.';
      },
    });
  }

  private loadCryptoWallets(): void {
    this.cryptoWalletsLoading = true;

    this.apiService.getAllCryptoWallets().subscribe({
      next: (wallets) => {
        this.cryptoWallets = [...wallets].sort((a, b) => a.email.localeCompare(b.email));
        this.cryptoWalletsLoading = false;
      },
      error: () => {
        this.cryptoWalletsLoading = false;
      },
    });
  }

  // --- USER: own accounts + conversion + trading ---

  private loadMyAccounts(): void {
    this.apiService.getMyBankAccounts().subscribe({
      next: (accounts) => {
        this.myBankAccounts = accounts;
      },
    });

    this.apiService.getMyCryptoWallets().subscribe({
      next: (wallets) => {
        this.myCryptoWallets = wallets;
      },
    });
  }

  onConvert(): void {
    this.conversionLoading = true;
    this.conversionError = '';
    this.conversionResult = '';

    this.apiService.convertCurrency(this.convFrom, this.convTo, this.convQuantity).subscribe({
      next: (result) => {
        this.conversionResult = result.transactionMessage;
        this.conversionLoading = false;
        this.loadMyAccounts();
      },
      error: (err) => {
        this.conversionError = err?.error?.message ?? 'Conversion failed.';
        this.conversionLoading = false;
      },
    });
  }

  onTrade(): void {
    this.tradeLoading = true;
    this.tradeError = '';
    this.tradeResult = '';

    this.apiService.trade(this.tradeFrom, this.tradeTo, this.tradeQuantity).subscribe({
      next: (result) => {
        this.tradeResult = result.transactionMessage;
        this.tradeLoading = false;
        this.loadMyAccounts();
      },
      error: (err) => {
        this.tradeError = err?.error?.message ?? 'Trade failed.';
        this.tradeLoading = false;
      },
    });
  }
}
