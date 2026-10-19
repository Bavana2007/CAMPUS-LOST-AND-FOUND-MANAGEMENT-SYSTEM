
public class TestSearchItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        System.out.println("Search test started");
        dao.searchItems("Laptop");
        System.out.println("Search test finished");
    }
}
