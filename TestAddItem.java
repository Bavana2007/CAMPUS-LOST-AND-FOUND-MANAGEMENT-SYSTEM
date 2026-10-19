public class TestAddItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        dao.addItem(1, "Water Bottle", "Bottle",
                "Blue water bottle", "Classroom",
                java.time.LocalDate.now().toString(), "OPEN");

        dao.viewItems();
    }
}