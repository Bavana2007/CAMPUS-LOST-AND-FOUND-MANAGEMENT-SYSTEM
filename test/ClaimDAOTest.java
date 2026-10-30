import dao.ClaimDAO;
import dao.ItemDAO;
import model.Claim;
import model.Item;

import java.sql.Date;
import java.time.LocalDate;

public class ClaimDAOTest {
    public static void main(String[] args) throws Exception {
        ItemDAO itemDAO = new ItemDAO();
        ClaimDAO claimDAO = new ClaimDAO();

        long ts = System.currentTimeMillis() % 10000;
        Item item = new Item(
            "Test Backpack " + ts,
            "Blue Jansport backpack",
            "Personal Belongings",
            "Sports Complex",
            Date.valueOf(LocalDate.now()),
            "found",
            1
        );
        itemDAO.addItem(item);

        // 1. Submit Claim
        Claim claim1 = new Claim(item.getItemId(), 2, "This backpack is mine");
        boolean submitted = claimDAO.submitClaim(claim1);
        assert submitted : "Claim submission failed";

        // 2. Duplicate Claim check
        boolean dupBlocked = false;
        try {
            claimDAO.submitClaim(new Claim(item.getItemId(), 2, "Second attempt"));
        } catch (IllegalStateException e) {
            dupBlocked = true;
        }
        assert dupBlocked : "Duplicate claim should be blocked";

        // 3. Second claimant submit claim
        Claim claim2 = new Claim(item.getItemId(), 3, "I also believe this is mine");
        claimDAO.submitClaim(claim2);

        // 4. Approve Claim Transaction
        boolean approved = claimDAO.approveClaimTransaction(claim1.getClaimId(), item.getItemId());
        assert approved : "Claim approval transaction failed";

        // Verify item is claimed
        Item updatedItem = itemDAO.getItemById(item.getItemId());
        assert "claimed".equalsIgnoreCase(updatedItem.getStatus()) : "Item status should be claimed";

        // Clean up
        itemDAO.deleteItem(item.getItemId());

        System.out.println("ClaimDAOTest PASSED.");
    }
}
