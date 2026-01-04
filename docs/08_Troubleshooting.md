# 08 — Troubleshooting

## Cannot connect to SQL Server
Common causes:
- Wrong password in `application.properties`
- Docker port not published: `-p 1433:1433`
- Encrypt mismatch

Try:
- `encrypt=true;trustServerCertificate=true`
- or `encrypt=false` for local dev

## Completion rejected
- Need ACTIVE enrollment for the task's course (trigger)

## Course creation blocked
- MEMBER course limit is enforced by trigger (default 5)
