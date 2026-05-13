# Database Setup Guide

## Problem: "Local Mode" instead of Database

The ATM is showing "Local Mode" because the MySQL database is not set up or the MySQL JDBC driver is missing.

## Quick Setup Steps:

### 1. Install MySQL Server
- Download and install MySQL Community Server
- Start MySQL service
- Set root password (default: none)

### 2. Create Database
Open MySQL Command Line and run:
```sql
mysql -u root -p
```

Then run the setup script:
```sql
source "c:\ATM Simulation\database_setup.sql"
```

### 3. Update MySQL Password
If your MySQL has a password, update `DBConnection.java`:
```java
private static final String PASSWORD = "your_mysql_password";
```

### 4. Add MySQL Driver (Optional)
Download MySQL Connector/J from: https://dev.mysql.com/downloads/connector/j/
Add to classpath: `-cp ".;mysql-connector-java-x.x.xx.jar"`

## Test Database Connection:
After setup, the ATM should show:
- Database balance instead of "Local Data"
- Real transaction history from database
- No more "Local Mode" messages

## Current Database Configuration:
- **Database**: `atm_db`
- **Host**: `localhost:3306`
- **User**: `root`
- **Password**: `root123`
- **Test Cards**: 8144344192 (PIN: 1234), 7205010463 (PIN: 1111)

## Troubleshooting:
- **"No suitable driver"**: Add MySQL Connector/J JAR to classpath
- **"Access denied"**: Check MySQL password in DBConnection.java
- **"Unknown database"**: Run database_setup.sql first
- **"Communications link failure"**: Start MySQL service
