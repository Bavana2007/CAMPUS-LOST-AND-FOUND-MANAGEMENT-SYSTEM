public class TestAddItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        dao.addItem("Water Bottle", "Bottle",
                "Blue water bottle", "Classroom",
                java.time.LocalDate.now().toString(),
                "Lost", 1);

        dao.viewItems();
    }
}
