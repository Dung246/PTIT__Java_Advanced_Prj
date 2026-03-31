package business;

import entity.Equipment;
import utils.DBConnection;

import java.sql.*;
import java.util.*;

public class EquipmentService {

    public void add(Equipment e) {
        String sql = "INSERT INTO equipment(name, total_quantity, available_quantity) VALUES (?,?,?)";

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, e.getName());
            ps.setInt(2, e.getAvailableQuantity()); // total = available lúc đầu
            ps.setInt(3, e.getAvailableQuantity());

            ps.executeUpdate();

            System.out.println("✅ Thêm thiết bị thành công!");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<Equipment> getAll() {

        List<Equipment> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM equipment");

            while (rs.next()) {
                list.add(new Equipment(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("available_quantity")
                ));
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    public boolean update(Equipment e) {
        String sql = "UPDATE equipment SET name=?, available_quantity=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, e.getName());
            ps.setInt(2, e.getAvailableQuantity());
            ps.setInt(3, e.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception ex) { ex.printStackTrace(); }

        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM equipment WHERE id=?";

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    public List<Equipment> searchByName(String keyword) {

        String sql = "SELECT * FROM equipment WHERE name LIKE ?";

        List<Equipment> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Equipment(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("available_quantity")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public Equipment getById(int id) {
        List<Equipment> list = getAll();

        for (Equipment e : list) {
            if (e.getId() == id) return e;
        }
        return null;
    }
}
