# SMCCCMS Frontend

Small Claims Court Council Management System - Frontend Application

## Quick Start

### Prerequisites
- Node.js 20.x (see `.nvmrc`)
- Backend API running on port 8080

### Development

1. **Install dependencies:**
   ```bash
   npm ci
   ```

2. **Start development server:**
   ```bash
   npm run dev
   ```

3. **Open browser:**
   - http://localhost:5173

### OR Use Full Stack Script

Run everything together (database, backend, frontend):
```bash
cd ../ops
./run_stack.sh
```

## Authentication Flow

The frontend implements a 3-step GOV.UK style authentication:

### 1. Government ID (`/login/gov-id`)
- Enter Gov ID (e.g., `ID-UK-001`)  
- Calls `POST /api/auth/verify-id`
- Redirects to contact page on success

### 2. Contact Details (`/login/contact`)
- Enter mobile/email (any value accepted in demo)
- Calls `POST /api/auth/request-code`
- Shows verification code in toast notification
- Click toast to auto-fill code page

### 3. Verification Code (`/login/code`)
- Enter 6-digit code (auto-filled from toast)
- Calls `POST /api/auth/verify-code`
- Sets session cookie and redirects to dashboard

## Demo Credentials

- **ID-UK-001** - Resident (RES)
- **ID-UK-007** - Solicitor (SOL)  
- **ID-UK-011** - Caseworker (CWS)
- **ID-UK-017** - Judge (JDG)

## Tech Stack

- **Framework:** Vite + React 18 + TypeScript
- **Routing:** React Router v6
- **Styling:** GOV.UK Frontend + SASS
- **HTTP Client:** Axios with cookie support
- **Notifications:** React Hot Toast
- **Auth:** Cookie-based sessions with route guards

## Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint  
- `npm run type-check` - Run TypeScript checks

## Environment Variables

Copy `.env.example` to `.env.local`:

```bash
VITE_BE_PORT=8080
VITE_ANTHROPIC_API_KEY=
```

## Architecture

- **Route Guards:** Prevent step skipping and protect authenticated pages
- **Session Management:** HTTP-only cookies with browser-based auth checks
- **State Management:** Session storage for auth flow, cookies for persistence
- **Error Handling:** Toast notifications for user feedback
- **Responsive Design:** GOV.UK Design System patterns