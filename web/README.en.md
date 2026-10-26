# Hunan Grade-A Hospital · Patient Service Platform (Web)

Web frontend for a single-hospital smart healthcare platform. Includes three sets of interfaces: patient portal, doctor workbench, and admin console.

## Tech Stack

Vue 3.5 + Vite 8 + Element Plus + Vue Router + Axios

## Features

- **Patient**: hospital overview, departments & doctors, doctor schedules, online appointment booking (mock payment, cancellation), health records, AI triage, AI health advice, AI lab-report interpretation (image upload), Q&A community, favorites, reviews, medication reminders.
- **Doctor**: workbench, schedule management, appointment handling, patient records, consultation replies, reviews.
- **Admin**: dashboard, hospital info, doctors, articles, diseases, drugs, pharmacies, appointments, consultations, users & roles.

## Getting Started

```sh
npm install
npm run dev      # development, http://localhost:5173
npm run build    # production build into dist/
```

The backend must be running at `http://localhost:8888`; the dev proxy for `/api` and `/uploads` is configured in `vite.config.js`.

## Notes

- Payment is **mocked**; no real payment gateway is integrated.
- AI-generated content is for reference only and is not medical advice.
