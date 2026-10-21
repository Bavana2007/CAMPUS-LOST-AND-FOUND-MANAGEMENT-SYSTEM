
package campuslostandfound;
public class TestFilterStatus {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

       System.out.println("Filtering items with status Unknown...");
dao.filterByStatus("Unknown");
    }
}
