# 07 — Config & Runbook

## Config file
`src/main/resources/application.properties`

Example (Docker SQL on localhost):
```
db.url=jdbc:sqlserver://localhost:1433;databaseName=club_test;encrypt=true;trustServerCertificate=true
db.user=sa
db.password=YourStrong!Pass123
app.pageSize=50
```

## Run
```
mvnd -q clean compile
mvnd -q exec:java
```

## Database prerequisites
- SQL Server running and reachable
- Database `club_test` exists
- Tables + triggers created
- Roles seeded: ADMIN / MEMBER
