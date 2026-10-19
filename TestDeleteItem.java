
public class TestDeleteItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        System.out.println("Starting delete test...");

        dao.deleteItem(4);

        System.out.println("Checking remaining items...");
        dao.viewItems();
    }
}
