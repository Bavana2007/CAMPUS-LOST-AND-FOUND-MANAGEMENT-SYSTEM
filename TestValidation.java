
public class TestValidation {
    public static void main(String[] args) {
        ItemDAO dao = new ItemDAO();

        System.out.println(
            dao.validateItem("Black Bag", "School bag",
                             "Library", "Lost"));

        System.out.println(
            dao.validateItem("", "School bag",
                             "Library", "Lost"));
    }
}
