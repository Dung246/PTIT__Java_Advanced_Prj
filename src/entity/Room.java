package entity;

public class Room {

    private int id;
    private String name;
    private int capacity;
    private String location;
    private String description;

    // constructor rỗng
    public Room() {}

    // constructor add
    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    // constructor update
    public Room(int id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    // constructor full
    public Room(int id, String name, int capacity, String location, String description) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
    }

    // getter setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
