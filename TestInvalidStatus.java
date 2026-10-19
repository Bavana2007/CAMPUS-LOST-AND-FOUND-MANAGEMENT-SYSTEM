public class TestInvalidStatus {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();
        dao.filterByStatus("Unknown");
    }
}
