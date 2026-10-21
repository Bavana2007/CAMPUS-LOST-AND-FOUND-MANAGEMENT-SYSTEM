
package campuslostandfound;

public class TestAddItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        dao.addItem(1, "Test Bag", "Bag",
                "Test description",
                "Library", "2026-10-19",
                "FOUND");

        dao.viewItems();
    }
}
