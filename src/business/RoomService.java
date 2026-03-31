package business;

import entity.Room;
import utils.DBConnection;

import java.sql.*;
import java.util.*;

public class RoomService {

    // ================= ADD =================
    public boolean add(Room r) {

        if (r.getCapacity() <= 0) {
            System.out.println("❌ Sức chứa phải > 0");
            return false;
        }

        String check = "SELECT * FROM rooms WHERE name=?";
        String sql = "INSERT INTO rooms(name, capacity, location, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            // check trùng tên
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setString(1, r.getName());
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                System.out.println("❌ Tên phòng đã tồn tại!");
                return false;
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, r.getName());
            ps.setInt(2, r.getCapacity());
            ps.setString(3, r.getLocation());
            ps.setString(4, r.getDescription());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= GET ALL =================
    public List<Room> getAll() {

        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";

        try (Connection conn = DBConnection.getConnection()) {

            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                Room r = new Room();
                r.setId(rs.getInt("id"));
                r.setName(rs.getString("name"));
                r.setCapacity(rs.getInt("capacity"));
                r.setLocation(rs.getString("location"));
                r.setDescription(rs.getString("description"));
                list.add(r);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    // ================= UPDATE =================
    public boolean update(Room r) {

        String sql = "UPDATE rooms SET name=?, capacity=?, location=?, description=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection()) {

            // check tồn tại
            if (getById(r.getId()) == null) {
                System.out.println("❌ ID không tồn tại!");
                return false;
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, r.getName());
            ps.setInt(2, r.getCapacity());
            ps.setString(3, r.getLocation());
            ps.setString(4, r.getDescription());
            ps.setInt(5, r.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    // ================= DELETE =================
    public boolean delete(int id) {

        String sql = "DELETE FROM rooms WHERE id=?";

        try (Connection conn = DBConnection.getConnection()) {

            if (getById(id) == null) {
                System.out.println("❌ Không tìm thấy phòng!");
                return false;
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("❌ Không thể xóa (có thể đã được đặt)");
        }

        return false;
    }

    // ================= SEARCH =================
    public List<Room> searchByName(String keyword) {

        String sql = "SELECT * FROM rooms WHERE name LIKE ?";
        List<Room> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Room r = new Room();
                r.setId(rs.getInt("id"));
                r.setName(rs.getString("name"));
                r.setCapacity(rs.getInt("capacity"));
                r.setLocation(rs.getString("location"));
                r.setDescription(rs.getString("description"));
                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= GET BY ID =================
    public Room getById(int id) {

        String sql = "SELECT * FROM rooms WHERE id=?";

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Room r = new Room();
                r.setId(id);
                r.setName(rs.getString("name"));
                r.setCapacity(rs.getInt("capacity"));
                r.setLocation(rs.getString("location"));
                r.setDescription(rs.getString("description"));
                return r;
            }

        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }
}
