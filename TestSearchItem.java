package campuslostandfound;

import java.util.List;

public class TestSearchItem {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        String keyword = "Test Bag";
        System.out.println("Searching for " + keyword + "...");

        List<Item> results = dao.searchItems(keyword);

        if (results.isEmpty()) {
            System.out.println("No items found.");
        } else {
            System.out.println("Found " + results.size() + " item(s):");
            for (Item item : results) {
                System.out.println(item);
            }
        }
    }
}
