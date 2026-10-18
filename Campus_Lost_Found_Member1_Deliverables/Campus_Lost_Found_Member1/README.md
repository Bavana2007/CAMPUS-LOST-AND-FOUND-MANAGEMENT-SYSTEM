# Campus Lost & Found Management System — Member 1 Deliverables

## Role
Database and JDBC Integration

## Files
- `database.sql` — creates the `campus_lost_found` database, the `users`,
  `items`, and `claims` tables, sample records, indexes, foreign keys,
  and example verification/search queries.
- `src/DBConnection.java` — reusable JDBC connection helper.
- `src/DBConnectionTest.java` — connection and table-existence smoke test.

## Requirements
- MySQL Server 8.0 or later
- JDK 17 or later recommended
- MySQL Connector/J (JDBC driver)

MySQL Workbench is not required. Use the MySQL command-line client instead.

## Database setup
1. Start the MySQL Server service.
2. Open Command Prompt and log in:
   `mysql -u root -p`
3. Run the script from the directory where it is saved:
   `SOURCE C:/full/path/to/database.sql;`
   Replace the path with the actual path. In the MySQL client, forward slashes
   in Windows paths are usually easiest.
4. Confirm that `SHOW TABLES;` lists `users`, `items`, and `claims`.

Alternatively, from Windows Command Prompt:
`mysql -u root -p < "C:\\full\\path\\to\\database.sql"`

## Configure JDBC credentials
Add MySQL Connector/J to your IDE project's libraries/classpath.

Set environment variables in a new Command Prompt before running Java:
- `set CLF_DB_USER=root`
- `set CLF_DB_PASSWORD=YOUR_LOCAL_MYSQL_PASSWORD`

Optional:
- `set CLF_DB_URL=jdbc:mysql://localhost:3306/campus_lost_found`

Do not submit real passwords or commit them to source control.

## Compile/run example
From the `src` folder, with Connector/J JAR path adjusted to your downloaded driver:

`javac -cp ".;C:\\path\\to\\mysql-connector-j.jar" DBConnection.java DBConnectionTest.java`

`java -cp ".;C:\\path\\to\\mysql-connector-j.jar" DBConnectionTest`

In IntelliJ IDEA or Eclipse, add the Connector/J JAR through the project's library/dependency settings and run `DBConnectionTest`.

## Data model
- `users.user_id` is the primary key.
- `items.user_id` references `users.user_id`.
- `claims.item_id` references `items.item_id`.
- `claims.claimant_id` references `users.user_id`.
- `claims.reviewed_by` optionally references the administrator/reviewer in `users`.
- Unique email, required fields, enums, foreign keys, and search indexes are defined.

## Security notes
- The sample password strings in `database.sql` are placeholders, not valid password hashes.
  Implement secure password hashing in the application before using authentication.
- Use prepared statements for all user-provided values in Java queries.
- The demo script is designed to be re-runnable for sample users/items, but inspect
  existing data before using it against any non-demo database.
- Do not use the root account for a deployed application; create a restricted DB user.

## Verification status
These deliverables have been prepared, but the JDBC test is NOT claimed to have passed
on the student's computer. Run the SQL script and `DBConnectionTest` locally and
record the actual result before describing the connection as tested.
