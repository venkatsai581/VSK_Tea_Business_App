# Pavan Tea - Business Project (Spring Boot + Thymeleaf)

## Run it
Needs JDK 17+. 

**Option A - no database needed (quickest):**
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```
**Option B - MySQL:** start MySQL, set your password in `src/main/resources/application.properties`
(or env vars `DB_USER`, `DB_PASSWORD`), then `./mvnw spring-boot:run`. The database is created automatically.

In Eclipse: import as *Existing Maven Project*, run `BusinessProjectApplication` (for Option A add `--spring.profiles.active=demo` as program argument).

Open http://localhost:9090

## First login (created automatically on an empty database)
| Role | Email | Password |
|---|---|---|
| Admin | admin@business.com | Admin@123 |
| Customer (demo profile only) | user@business.com | User@123 |

Change the admin password after the first login (Dashboard -> Admins -> Edit).

## What the app does
- Public: home, products (live search + sort), about, location, customer registration.
- Customer (`/shop`): order tea with quantity stepper and live total, order history, cancel placed orders, edit profile / password.
- Admin (`/admin`): dashboard with totals; add/edit/delete products, customers, admins; change order status; search and sort every table.

## What was improved
- Real login sessions (before, one shared variable meant every visitor became the last user who logged in).
- Admin pages are protected; passwords are BCrypt-hashed (old plain-text passwords are upgraded on first login).
- All changes use POST (before: delete/update via links); same-origin check, brute-force lockout, session-fixation protection.
- Validation with clear messages; duplicate email/product checks; price is read from the database, not from the browser.
- Cannot delete yourself or the last admin; order status (placed/delivered/cancelled).
- New responsive UI: dark mode, mobile menu, toasts, confirm dialogs, tabs, live search, sortable tables.
