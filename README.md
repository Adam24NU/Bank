# NorthStar Bank

NorthStar Bank is a refactored Java servlet banking demo running on embedded Jetty with a local SQLite database. The project started from a coursework-style repository with vulnerable/demo-only features and has been cleaned up into a smaller, safer, easier-to-maintain application focused on the core banking flows that remain relevant.

## Final Feature Set

- Secure login at `/welcome`
- Authenticated account dashboard at `/account`
- Profile editing with output-safe rendering
- Avatar upload with image validation and PNG re-encoding
- Balance transfer flow at `/transfer`
- Customer directory at `/balance`
- Logout flow at `/logout`

Removed from the final project:

- Coursework-only XML statement feature
- Standalone path traversal demo
- Demo attack files and sample exploit content
- Prebuilt/generated artifacts and duplicate libraries

## Security Improvements

The current application includes these practical hardening changes:

- PBKDF2 password hashing for seeded users
- Prepared statements for database access
- Transactional transfer updates
- Session rotation on login
- HttpOnly cookie usage and cookie-only session tracking
- CSRF protection on all state-changing forms
- Input validation for usernames, card numbers, transfer amounts, and profiles
- Output encoding for user-controlled text
- Avatar upload size limits and safe server-side image processing
- Security response headers on application pages
- Tighter authorization around profile views, balances, and card visibility
- Static directory listing disabled

## Tech Stack

- Java 11
- Java Servlets
- Embedded Jetty 9
- SQLite
- Apache Ant
- Apache Commons FileUpload / Commons IO
- Centralized CSS in `src/WebContext/app.css`

## Project Structure

- `src/TargetServer.java`: Jetty startup and route registration
- `src/Database.java`: database access layer
- `src/DatabaseBootstrap.java`: schema creation and seeded data
- `src/WebSecurity.java`: session, CSRF, flash messages, and security headers
- `src/HtmlUtil.java`: HTML shell and encoding helpers
- `src/ValidationUtil.java`: request validation helpers
- `src/ViewPageServlet.java`: login page
- `src/AccountPageServlet.java`: account dashboard, profile updates, avatar upload
- `src/TransferPageServlet.java`: transfer flow
- `src/CustomersListPageServlet.java`: customer directory
- `src/AvatarServlet.java`: authenticated avatar image endpoint
- `src/LogoutServlet.java`: logout handler
- `src/WebContext/`: shared frontend assets
- `build.xml`: build and bootstrap workflow

## Prerequisites

- Java 11 or newer
- Apache Ant

All required runtime JARs are bundled in `src/lib`.

## Build And Run

From the repository root:

```bash
ant compile
cd built/classes
./run.sh
```

Then open:

```text
http://localhost:15000/welcome
```

To stop the server, type:

```text
quit
```

## Demo Login Details

The build seeds these local demo accounts:

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `adminPassword` | `admin` |
| `user1` | `user1Password` | `normal` |
| `user2` | `user2Password` | `normal` |
| `user3` | `user3Password` | `normal` |
| `user4` | `user4Password` | `normal` |
| `user5` | `user5Password` | `normal` |
| `user6` | `user6Password` | `normal` |

These passwords are hashed when the SQLite database is generated.

## Build Notes

`ant compile` now:

- recreates `built/classes`
- copies only the current web assets
- compiles the application for Java 11
- runs `DatabaseBootstrap` to create `built/classes/users.db`

The build no longer depends on `sqlite3`.

## Uploads, Seeded Data, And Limitations

- Avatar uploads are stored under the runtime `uploads/` directory and are not intended as permanent content
- Rebuilding resets the seeded database back to the default demo state
- The app is designed for local/demo use and does not include HTTPS or production deployment wiring
- The project still uses seeded credentials because it is a demo banking app, not a real multi-user registration system

## Repository Notes

- Generated output is written under `built/` and ignored by git
- Runtime uploads and common editor/OS noise are ignored by git
- The old coursework/demo functionality has been removed rather than left dormant
