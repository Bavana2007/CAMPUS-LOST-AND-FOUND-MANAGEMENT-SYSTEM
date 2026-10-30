import dao.ItemDAO;
import model.Item;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class ItemDAOTest {
    public static void main(String[] args) throws Exception {
        ItemDAO dao = new ItemDAO();
        long ts = System.currentTimeMillis() % 10000;

        Item item = new Item(
            "Test Umbrella " + ts,
            "Black folding umbrella",
            "Accessories",
            "Cafeteria Entrance",
            Date.valueOf(LocalDate.now()),
            "found",
            1
        );

        // Insert
        boolean added = dao.addItem(item);
        assert added : "Item addition failed";
        assert item.getItemId() > 0 : "Generated item_id is missing";

        // Search
        List<Item> results = dao.searchAndFilterItems("Umbrella " + ts, "ALL");
        assert !results.isEmpty() : "Search failed for inserted umbrella";

        // Update
        item.setLocation("Cafeteria Counter");
        boolean updated = dao.updateItem(item);
        assert updated : "Item update failed";

        // Clean up
        boolean deleted = dao.deleteItem(item.getItemId());
        assert deleted : "Item deletion failed";

        System.out.println("ItemDAOTest PASSED.");
    }
}
