# NorthStar Bank

NorthStar Bank is a modern banking web app template and demo built with Java servlets. It provides a polished account dashboard, transfer flow, customer directory, and profile management experience that teams can clone and adapt for prototypes, internal demos, or learning projects.

The repository is intentionally small and reusable. It keeps the current hardened implementation in place, but presents the project as a clean starter rather than a personal one-off codebase. It is meant for local development and evaluation, not production banking infrastructure.

## Feature Overview

- Sign-in flow at `/welcome`
- Account dashboard at `/account`
- Profile editing and avatar upload
- Fund transfer flow at `/transfer`
- Internal customer directory at `/balance`
- Cohesive shared UI styling in `src/WebContext/app.css`
- Sample seeded accounts for local exploration

## Project Structure

- `src/TargetServer.java`: local server bootstrap and route registration
- `src/Database.java`: data-access layer
- `src/DatabaseBootstrap.java`: schema creation and sample data seeding
- `src/WebSecurity.java`: session, CSRF, flash message, and response-header helpers
- `src/HtmlUtil.java`: shared HTML shell and escaping helpers
- `src/ValidationUtil.java`: request validation helpers
- `src/ViewPageServlet.java`: sign-in page
- `src/AccountPageServlet.java`: dashboard, profile updates, and avatar uploads
- `src/TransferPageServlet.java`: transfer flow
- `src/CustomersListPageServlet.java`: customer directory
- `src/AvatarServlet.java`: authenticated avatar endpoint
- `src/LogoutServlet.java`: logout handler
- `build.xml`: compile and bootstrap workflow

## Prerequisites

- Java 11 or newer
- Apache Ant

Runtime JARs are already bundled in `src/lib`.

## Build And Run

From the repository root:

```bash
ant compile
cd built/classes
./run.sh
```

Open:

```text
http://localhost:15000/welcome
```

To stop the server, type:

```text
quit
```

## Sample Accounts

The local build seeds these placeholder accounts for template review:

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `adminPassword` | `admin` |
| `user1` | `user1Password` | `normal` |
| `user2` | `user2Password` | `normal` |
| `user3` | `user3Password` | `normal` |
| `user4` | `user4Password` | `normal` |
| `user5` | `user5Password` | `normal` |
| `user6` | `user6Password` | `normal` |

Rebuilding the project resets the local database to this default sample state.

## Notes

- Generated output is written under `built/` and ignored by git
- Runtime uploads are written to `uploads/` in the active run directory
- The application is intended for local demos, learning, and template adaptation
- Production deployment concerns such as HTTPS termination, secrets management, persistent object storage, monitoring, and operational scaling are outside the scope of this repository
