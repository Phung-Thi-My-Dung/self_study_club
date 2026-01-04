# 05 — Architecture (MVC + DAO)

## Layers
1) **UI (Swing)**: `ui/screens/**`
2) **Service Layer**: `service/*Service.java`
3) **DAO Layer**: `dao/*` + `dao/jdbc/*`
4) **Database**: triggers enforce constraints

## Folder Structure (main)
```
src/main/java/com/selfstudyclub/
  App.java
  config/
  security/
  model/
  dao/
    jdbc/
  service/
  ui/
    screens/
src/main/resources/
  application.properties
  logback.xml
```
