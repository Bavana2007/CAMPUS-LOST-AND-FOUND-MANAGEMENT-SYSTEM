import dao.UserDAO;
import model.User;

public class UserDAOTest {
    public static void main(String[] args) throws Exception {
        UserDAO dao = new UserDAO();
        long ts = System.currentTimeMillis() % 10000;
        String email = "student_test" + ts + "@campus.edu";

        // Insert
        User user = new User("Test Student " + ts, email, "pass123", "student");
        boolean inserted = dao.register(user);
        assert inserted : "User registration failed";

        // Login correct
        User loggedIn = dao.login(email, "pass123");
        assert loggedIn != null : "User login failed with valid credentials";

        // Login wrong password
        User wrong = dao.login(email, "badpass");
        assert wrong == null : "User login should fail with bad password";

        // Duplicate email
        boolean dup = dao.register(new User("Another", email, "pass123", "student"));
        assert !dup : "Duplicate email should be blocked";

        System.out.println("UserDAOTest PASSED.");
    }
}
