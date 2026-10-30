import dao.ClaimDAO;
import dao.ItemDAO;
import dao.UserDAO;
import db.DBConnection;
import model.Claim;
import model.Item;
import model.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated test suite covering:
 * 1. Database connectivity
 * 2. UserDAO insert (registration) and login authentication
 * 3. ItemDAO insert, update, search, and delete (CRUD)
 * 4. ClaimDAO duplicate claim blocking rule
 * 5. ClaimDAO claimed item blocking rule
 * 6. ClaimDAO transactional claim approval (atomicity and rejection of competing claims)
 */
public class TestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("CAMPUS LOST AND FOUND - AUTOMATED TEST SUITE");
        System.out.println("==================================================");

        // Check if database is reachable
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("[DB Check] Connected successfully to: " + DBConnection.getUrl());
            System.out.println("[DB Check] Catalog: " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("\n[DB CONNECTION FAILED]");
            System.err.println("Could not connect to MySQL server: " + e.getMessage());
            System.err.println("Ensure MySQL is running, campus_lost_found DB exists, and password is configured.");
            System.err.println("Configure in config/db.properties or environment variable DB_PASSWORD.\n");
            return;
        }

        UserDAO userDAO = new UserDAO();
        ItemDAO itemDAO = new ItemDAO();
        ClaimDAO claimDAO = new ClaimDAO();

        long testSuffix = System.currentTimeMillis() % 100000;

        // TEST 1: UserDAO Registration and Login
        try {
            System.out.println("\n--- TEST 1: User Registration & Login ---");
            String testEmail = "testuser" + testSuffix + "@campus.edu";
            User user = new User("Test Student", testEmail, "pass1234", "student");

            boolean registered = userDAO.register(user);
            assertTrue(registered, "User registration should return true");
            assertTrue(user.getUserId() > 0, "Registered user should have a valid generated user_id");

            User loggedIn = userDAO.login(testEmail, "pass1234");
            assertTrue(loggedIn != null, "Login should succeed with correct credentials");
            assertTrue(user.getName().equals(loggedIn.getName()), "User name should match after login");

            User badLogin = userDAO.login(testEmail, "wrongpassword");
            assertTrue(badLogin == null, "Login should fail with incorrect password");

            // Duplicate registration
            boolean dup = userDAO.register(new User("Dup Student", testEmail, "pass123", "student"));
            assertTrue(!dup, "Duplicate email registration must be blocked");

        } catch (Exception e) {
            recordFailure("Test 1 encountered exception: " + e.getMessage());
        }

        // TEST 2: ItemDAO CRUD & Search
        Item createdItem = null;
        try {
            System.out.println("\n--- TEST 2: ItemDAO CRUD & Search ---");
            User reporter = userDAO.login("alice@student.campus.edu", "student123");
            int reporterId = (reporter != null) ? reporter.getUserId() : 1;

            Item item = new Item(
                "Test Wireless Mouse " + testSuffix,
                "Logitech black ergonomic mouse with USB dongle",
                "Electronics",
                "Library 2nd Floor Desk 5",
                Date.valueOf(LocalDate.now()),
                "found",
                reporterId
            );

            boolean added = itemDAO.addItem(item);
            assertTrue(added, "ItemDAO.addItem should succeed");
            assertTrue(item.getItemId() > 0, "Item should have generated item_id");
            createdItem = item;

            // Search by keyword
            List<Item> searchResults = itemDAO.searchAndFilterItems("Wireless Mouse " + testSuffix, "ALL");
            assertTrue(!searchResults.isEmpty(), "ItemDAO search should locate inserted item by keyword");

            // Filter by status
            List<Item> filterResults = itemDAO.searchAndFilterItems("", "found");
            assertTrue(!filterResults.isEmpty(), "ItemDAO filter by status 'found' should return records");

            // Update Item
            item.setTitle("Updated Wireless Mouse " + testSuffix);
            item.setLocation("Library Front Desk");
            boolean updated = itemDAO.updateItem(item);
            assertTrue(updated, "ItemDAO.updateItem should return true");

            Item fetched = itemDAO.getItemById(item.getItemId());
            assertTrue(fetched != null && fetched.getTitle().startsWith("Updated"), "Item title should reflect update");

        } catch (Exception e) {
            recordFailure("Test 2 encountered exception: " + e.getMessage());
        }

        // TEST 3: Duplicate Claim & Claim on Already Claimed Item
        try {
            System.out.println("\n--- TEST 3: Claim Duplicate Prevention ---");
            if (createdItem != null) {
                User claimant = userDAO.login("bob@student.campus.edu", "student123");
                int claimantId = (claimant != null) ? claimant.getUserId() : 2;

                Claim claim1 = new Claim(createdItem.getItemId(), claimantId, "This is my mouse dropped in library.");
                boolean submitted1 = claimDAO.submitClaim(claim1);
                assertTrue(submitted1, "First claim submission should succeed");

                // Try duplicate claim
                boolean dupBlocked = false;
                try {
                    Claim claimDup = new Claim(createdItem.getItemId(), claimantId, "Duplicate attempt message");
                    claimDAO.submitClaim(claimDup);
                } catch (IllegalStateException e) {
                    dupBlocked = true; // Expected
                }
                assertTrue(dupBlocked, "Duplicate claim on same item by same user must be blocked");
            }
        } catch (Exception e) {
            recordFailure("Test 3 encountered exception: " + e.getMessage());
        }

        // TEST 4: Transactional Claim Approval Flow
        try {
            System.out.println("\n--- TEST 4: Transactional Claim Approval Flow ---");
            // Create a test item with 2 distinct claims from 2 different users
            User student1 = userDAO.login("alice@student.campus.edu", "student123");
            User student2 = userDAO.login("charlie@student.campus.edu", "student123");
            int id1 = student1 != null ? student1.getUserId() : 2;
            int id2 = student2 != null ? student2.getUserId() : 4;

            Item transItem = new Item(
                "Silver Watch " + testSuffix,
                "Analog wrist watch with leather band",
                "Accessories",
                "Campus Gym Locker Room",
                Date.valueOf(LocalDate.now()),
                "found",
                1 // reported by admin
            );
            itemDAO.addItem(transItem);

            Claim claimA = new Claim(transItem.getItemId(), id1, "I forgot my silver watch in the locker.");
            claimDAO.submitClaim(claimA);

            Claim claimB = new Claim(transItem.getItemId(), id2, "I also believe this is my silver watch.");
            claimDAO.submitClaim(claimB);

            // Admin approves Claim A
            boolean approved = claimDAO.approveClaimTransaction(claimA.getClaimId(), transItem.getItemId());
            assertTrue(approved, "claimDAO.approveClaimTransaction should succeed");

            // Verify Item status is now 'claimed'
            Item updatedTransItem = itemDAO.getItemById(transItem.getItemId());
            assertTrue("claimed".equalsIgnoreCase(updatedTransItem.getStatus()), "Item status must be 'claimed' after approval");

            // Verify Claim A is 'approved'
            List<Claim> allClaims = claimDAO.getAllClaims();
            Claim approvedC = null;
            Claim rejectedC = null;
            for (Claim c : allClaims) {
                if (c.getClaimId() == claimA.getClaimId()) approvedC = c;
                if (c.getClaimId() == claimB.getClaimId()) rejectedC = c;
            }

            assertTrue(approvedC != null && "approved".equalsIgnoreCase(approvedC.getStatus()), "Target claim status must be 'approved'");
            assertTrue(rejectedC != null && "rejected".equalsIgnoreCase(rejectedC.getStatus()), "Competing claim status must be automatically 'rejected'");

            // Verify new claims are blocked because item is now 'claimed'
            boolean blockedOnClaimed = false;
            try {
                claimDAO.submitClaim(new Claim(transItem.getItemId(), 3, "Attempting claim on already claimed item"));
            } catch (IllegalStateException e) {
                blockedOnClaimed = true;
            }
            assertTrue(blockedOnClaimed, "Submitting claim on an already claimed item must throw IllegalStateException");

            // Clean up test items
            itemDAO.deleteItem(transItem.getItemId());
            if (createdItem != null) {
                itemDAO.deleteItem(createdItem.getItemId());
            }

        } catch (Exception e) {
            recordFailure("Test 4 encountered exception: " + e.getMessage());
        }

        // Summary
        System.out.println("\n==================================================");
        System.out.println("TEST SUMMARY: " + testsPassed + " Passed, " + testsFailed + " Failed");
        System.out.println("==================================================");
    }

    private static void assertTrue(boolean condition, String description) {
        if (condition) {
            System.out.println(" [PASS] " + description);
            testsPassed++;
        } else {
            System.err.println(" [FAIL] " + description);
            testsFailed++;
        }
    }

    private static void recordFailure(String description) {
        System.err.println(" [ERROR] " + description);
        testsFailed++;
    }
}
