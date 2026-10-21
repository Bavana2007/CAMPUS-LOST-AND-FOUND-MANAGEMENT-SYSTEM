
package campuslostandfound;

public class TestSearchItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();
        System.out.println("Searching for Test Bag...");
        dao.searchItems("Test Bag");
    }
}
