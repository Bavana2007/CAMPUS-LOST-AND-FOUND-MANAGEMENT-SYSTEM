public class TestUpdateItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        System.out.println("Starting update test...");

        dao.updateItem(1, "Black Bag", "Bag",
                "Black bag found near classroom",
                "Library", "Found");

        System.out.println("Update method finished.");

        dao.viewItems();
    }
}
