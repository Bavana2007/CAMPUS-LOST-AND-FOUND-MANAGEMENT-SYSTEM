
public class TestUpdateItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        dao.updateItem(2, "Water Bottle", "Bottle",
                "Blue water bottle", "Library", "Lost");

        dao.viewItems();
    }
}
