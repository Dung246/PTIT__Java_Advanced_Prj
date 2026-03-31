package entity;

public class Equipment {

    private int id;
    private String name;
    private int availableQuantity;

    public Equipment() {}

    public Equipment(String name, int availableQuantity) {
        this.name = name;
        this.availableQuantity = availableQuantity;
    }

    public Equipment(int id, String name, int availableQuantity) {
        this.id = id;
        this.name = name;
        this.availableQuantity = availableQuantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
