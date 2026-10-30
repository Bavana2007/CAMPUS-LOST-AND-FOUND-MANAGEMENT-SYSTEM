# Campus Lost and Found Management System (CAMPUS-LOST-AND-FOUND)

A complete Java Swing desktop application connected to MySQL using plain JDBC with the DAO pattern (Data Access Object).

Students can report lost or found items, browse and search cataloged items with dynamic filters, and submit claims of ownership. Administrators review claims, approve or reject them atomically within a database transaction, and manage items.

---

## 🛠 Tech Stack & Architecture

- **User Interface:** Java Swing with modern **Nimbus Look and Feel**
- **Language / Runtime:** Java SE (JDK 17 to JDK 26)
- **Database:** MySQL Server 8.0+
- **Driver:** MySQL Connector/J (`lib/mysql-connector-j-26.7.0.jar`)
- **Persistence:** Plain JDBC with DAO Pattern (`PreparedStatement` throughout to prevent SQL injection, `try-with-resources` for auto-closing connections and statements)
- **Transactions:** Multi-statement atomic transaction with `commit()` and `rollback()` for claim approvals

### Project Directory Structure
```
CAMPUS LOST AND FOUND MANAGEMENT SYSTEM/
├── src/
│   ├── db/
│   │   └── DBConnection.java          # Singleton JDBC connection & config loader
│   ├── model/
│   │   ├── User.java                  # User entity (Student, Admin)
│   │   ├── Item.java                  # Item entity (lost, found, claimed)
│   │   └── Claim.java                 # Claim entity (pending, approved, rejected)
│   ├── dao/
│   │   ├── UserDAO.java               # Authentication, registration & user lookup
│   │   ├── ItemDAO.java               # Item CRUD, keyword search & status filter
│   │   └── ClaimDAO.java              # Claim submissions, duplicate guards & transaction approval
│   ├── ui/
│   │   ├── UITheme.java               # Nimbus theme styling & color constants
│   │   ├── LoginForm.java             # Login & Registration with input validation
│   │   ├── MainMenu.java              # Role-based dashboard (Student vs Admin)
│   │   ├── ItemForm.java              # Add & Edit item dialog
│   │   ├── ViewItems.java             # JTable of items, live search, status filter & actions
│   │   ├── ClaimForm.java             # Student claim submission dialog
│   │   └── AdminClaimsPanel.java      # Admin pending claims review & transaction actions
│   └── Main.java                      # Application launcher (opens LoginForm on EDT)
├── test/
│   ├── TestSuite.java                 # Complete automated test suite
│   ├── UserDAOTest.java               # UserDAO unit tests
│   ├── ItemDAOTest.java               # ItemDAO unit tests
│   └── ClaimDAOTest.java              # ClaimDAO unit tests
├── config/
│   └── db.properties                  # Database credentials configuration
├── lib/
│   └── mysql-connector-j-26.7.0.jar   # MySQL JDBC Driver
├── sql/
│   └── database.sql                   # MySQL DDL & DML script
├── docs/                              # Flowcharts, reports, and architecture diagrams
├── database.sql                       # Root database script copy
├── pom.xml                            # Maven build configuration
├── run.bat                            # 1-Click Windows run script
├── test.bat                           # 1-Click Windows test script
└── README.md                          # Documentation & Setup guide
```

---

## 🗄️ Database Setup (`database.sql`)

1. Start your local **MySQL Server 8.0+** service (via Windows Services or XAMPP).
2. Open Command Prompt or PowerShell and import `database.sql`:
   ```cmd
   mysql -u root -p < database.sql
   ```
   *Or inside MySQL CLI:*
   ```sql
   SOURCE c:/path/to/database.sql;
   ```
3. The script creates the `campus_lost_found` database with 3 tables:
   - **`users`**: `user_id` (PK, auto-increment), `name`, `email` (unique), `password` (SHA-256 hashed or demo plain), `role` (`student`, `admin`), `created_at`
   - **`items`**: `item_id` (PK, auto-increment), `title`, `description`, `category`, `location`, `date_reported`, `status` (`lost`, `found`, `claimed`), `reported_by` (FK to `users`)
   - **`claims`**: `claim_id` (PK, auto-increment), `item_id` (FK to `items`), `claimant_id` (FK to `users`), `message`, `status` (`pending`, `approved`, `rejected`), `claim_date`

### Pre-populated Sample Accounts
| Role | Name | Email | Password |
| :--- | :--- | :--- | :--- |
| **Admin** | System Administrator | `admin@campus.edu` | `admin123` |
| **Student** | Alice Smith | `alice@student.campus.edu` | `student123` |
| **Student** | Bob Johnson | `bob@student.campus.edu` | `student123` |
| **Student** | Charlie Brown | `charlie@student.campus.edu` | `student123` |

Includes **6 realistic items** (laptops, headphones, ID cards, wallets, calculators, water bottles) and **3 sample claims**.

---

## ⚙️ Configuration (MySQL Username & Password)

Before running the application, ensure `DBConnection` knows your local MySQL credentials.

### Method 1: Edit `config/db.properties` (Recommended)
Open `config/db.properties` in any text editor:
```properties
db.url=jdbc:mysql://localhost:3306/campus_lost_found?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_PASSWORD_HERE
```
*(Leave `db.password=` empty if your root user has no password).*

### Method 2: In-App UI Dialog
You can also change credentials directly in the running app at any time by clicking **"Database Settings"** at the bottom of the Login or Main screen.

### Method 3: Environment Variables
Alternatively, set environment variables:
```cmd
set DB_USER=root
set DB_PASSWORD=YOUR_MYSQL_PASSWORD
```

---

## 🚀 How to Run the Application

### Option A: Using the 1-Click Script (Windows)
Double-click `run.bat` or run:
```cmd
run.bat
```

### Option B: Using the Command Line (`javac` & `java`)
1. Compile:
   ```cmd
   javac -cp ".;lib/mysql-connector-j-26.7.0.jar" -d bin src\db\*.java src\model\*.java src\dao\*.java src\ui\*.java src\Main.java
   ```
2. Run:
   ```cmd
   java -cp ".;bin;lib/mysql-connector-j-26.7.0.jar" Main
   ```

### Option C: Using Maven
```cmd
mvn clean compile
mvn exec:java
```

---

## 🧪 Running the Automated Tests

The test suite covers:
1. **UserDAO**: User registration, login authentication, password hash matching, and duplicate email prevention.
2. **ItemDAO**: Insert item, keyword search (title, category, location, description), status filtering, update, and delete.
3. **ClaimDAO**: Duplicate claim blocking rule (same user on same item), already 'claimed' item blocking rule.
4. **Transaction Approval**: Atomic approval of a claim setting item status to `'claimed'` and automatically rejecting other pending claims on the same item.

Run the tests with:
```cmd
test.bat
```
Or manually:
```cmd
javac -cp ".;lib/mysql-connector-j-26.7.0.jar;bin" -d bin test\*.java
java -cp ".;bin;lib/mysql-connector-j-26.7.0.jar" TestSuite
```

---

## ✨ Key Application Features

1. **Role-Based Portals:**
   - **Students:** Browse cataloged items, report items, submit ownership claims, and check real-time status of their claims (`pending`, `approved`, `rejected`).
   - **Admins:** Full CRUD over all items and an **Admin Claims Panel** with atomic claim resolution.
2. **Dynamic Live Search & Filters:**
   - Real-time search across titles, categories, locations, and descriptions.
   - Status filters (`ALL`, `lost`, `found`, `claimed`).
3. **Claim Rules Enforced:**
   - Cannot claim an item that is already marked as `claimed`.
   - Cannot submit duplicate claims on the same item.
   - Cannot claim an item that you reported yourself.
4. **JDBC Transaction with Commit & Rollback:**
   - When an admin approves a claim, `ClaimDAO.approveClaimTransaction` updates the selected claim to `approved`, updates the item to `claimed`, and rejects all competing pending claims on that item within an atomic database transaction. If any update fails, the transaction rolls back cleanly.
5. **Form Validation:**
   - Real-time email syntax regex validation, password length/match validation, required fields validation, and descriptive `JOptionPane` messages.
