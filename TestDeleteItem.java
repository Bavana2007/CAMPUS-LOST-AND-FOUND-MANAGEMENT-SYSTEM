
public class TestDeleteItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        dao.deleteItem(2);

        dao.viewItems();
    }
}
