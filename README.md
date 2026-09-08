# Currency Exchange App

Multi-modul mikroservisni sistem za razmenu fiat i kripto valuta. Backend: Java 17, Spring Boot, Spring Cloud (Eureka, Gateway, OpenFeign, Resilience4j), Docker / Docker Compose. Frontend: Angular. Ceo sistem se koristi preko **API Gateway-a**: `http://localhost:8765`.

## Kredencijali

Seed-ovano automatski pri svakom pokretanju (`users-service/src/main/resources/data.sql`):

```
owner@app.com / owner123 / OWNER
admin@app.com / admin123 / ADMIN
user@app.com  / user123  / USER
```

## Endpoint-i (preko API Gateway-a, http://localhost:8765)

### Users service
- `GET /users` — lista svih korisnika (Basic Auth, OWNER ili ADMIN)
- `GET /users/{id}` — korisnik po ID-ju (Basic Auth, OWNER ili ADMIN)
- `GET /users/email?email=...` — korisnik po email-u (javno, bez autentikacije)
- `POST /users` — kreiranje korisnika, body `{email, password, role}` (Basic Auth, OWNER ili ADMIN — ADMIN sme samo rolu USER)
- `PUT /users/{id}` — izmena korisnika, body `{email, password, role}` (Basic Auth, OWNER ili ADMIN — ADMIN samo za postojeće USER korisnike, i samo na rolu USER)
- `DELETE /users/{id}` — brisanje korisnika, kaskadno briše bank nalog i crypto novčanik (Basic Auth, OWNER)

### Bank account
- `GET /bank-accounts` — lista svih bankovnih naloga (Basic Auth, ADMIN)
- `GET /bank-accounts/my` — nalozi ulogovanog korisnika (Basic Auth, USER)
- `PUT /bank-accounts/{id}` — izmena naloga, body `{email, currencyCode, amount}` (Basic Auth, ADMIN)
- `GET /bank-accounts/balance?email=...&currency=...` — stanje po email-u i valuti (Basic Auth, ADMIN)
- `POST /bank-accounts/deduct?email=...&currency=...&amount=...` — oduzimanje sredstava (Basic Auth, ADMIN)
- `POST /bank-accounts/add?email=...&currency=...&amount=...` — dodavanje sredstava / kreiranje naloga za novu valutu, samo za postojeći USER email (Basic Auth, ADMIN)
- `POST /bank-accounts/internal?email=...`, `DELETE /bank-accounts/internal?email=...` — interni auto-provisioning pri kreiranju/brisanju korisnika (Basic Auth, ADMIN — poziva ih isključivo users-service, nije za ručno testiranje)

### Crypto wallet
- `GET /crypto-wallets` — lista svih crypto novčanika (Basic Auth, ADMIN)
- `GET /crypto-wallets/my` — novčanici ulogovanog korisnika (Basic Auth, USER)
- `PUT /crypto-wallets/{id}` — izmena novčanika, body `{email, currencyCode, amount}` (Basic Auth, ADMIN)
- `GET /crypto-wallets/balance?email=...&currency=...` — stanje po email-u i valuti (Basic Auth, ADMIN)
- `POST /crypto-wallets/deduct?email=...&currency=...&amount=...` — oduzimanje sredstava (Basic Auth, ADMIN)
- `POST /crypto-wallets/add?email=...&currency=...&amount=...` — dodavanje sredstava / kreiranje novčanika za novu valutu, samo za postojeći USER email (Basic Auth, ADMIN)
- `POST /crypto-wallets/internal?email=...`, `DELETE /crypto-wallets/internal?email=...` — interni auto-provisioning (Basic Auth, ADMIN — poziva ih isključivo users-service, nije za ručno testiranje)

### Currency exchange
- `GET /currency-exchange?from=X&to=Y` — trenutni kurs fiat valute (javno, bez autentikacije)

### Crypto exchange
- `GET /crypto-exchange?from=X&to=Y` — trenutni kurs kripto valute (javno, bez autentikacije)

### Currency conversion
- `GET /currency-conversion?from=X&to=Y&quantity=Q` — konverzija fiat valute sa bankovnog naloga ulogovanog korisnika (Basic Auth, USER, ADMIN ili OWNER)

### Trade service
- `GET /trade-service?from=X&to=Y&quantity=Q` — trgovina (fiat↔crypto, crypto↔crypto) sa bankovnog naloga / crypto novčanika ulogovanog korisnika (Basic Auth, USER, ADMIN ili OWNER)

### Infrastrukturni servisi
- `naming-server` (Eureka, port 8761) i `api-gateway` (port 8765) nemaju sopstvene poslovne endpoint-e — služe za service discovery i rutiranje ka gornjim servisima.

## Autentikacija

Basic Auth se šalje na **svakom** zahtevu, osim `/currency-exchange`, `/crypto-exchange` i `/users/email`, koji su javni. API Gateway dekodira Basic Auth header i validira kredencijale pozivom ka `users-service` (`GET /users/validate`) pre nego što zahtev prosledi dalje — pri uspešnoj validaciji, gateway dodaje `X-User-Role` i `X-User-Email` header-e koje downstream servisi koriste za autorizaciju i identifikaciju korisnika.

## Pokretanje

```
docker-compose up -d
```
pokreće ceo backend (svih 9 servisa) iz root foldera. Frontend se pokreće odvojeno:
```
cd frontend
npm install
ng serve
```
dostupno na `http://localhost:4200`.



