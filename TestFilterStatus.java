
public class TestFilterStatus {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();
        dao.filterByStatus("OPEN");
    }
}
