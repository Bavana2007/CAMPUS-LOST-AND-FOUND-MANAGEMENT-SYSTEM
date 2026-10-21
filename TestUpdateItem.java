package campuslostandfound;
public class TestUpdateItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        System.out.println("Starting update test...");
dao.updateItem(999, "Test Item", "Bag",
        "Testing invalid ID",
        "Library", "CLAIMED");
        System.out.println("Update method finished.");

        dao.viewItems();
    }
}
