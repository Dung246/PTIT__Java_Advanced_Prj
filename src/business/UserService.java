package business;

import entity.User;
import utils.DBConnection;
import utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    // ================= LOGIN =================
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= REGISTER =================
    public boolean register(User user) {
        String check = "SELECT * FROM users WHERE username=? OR email=?";

        String insert = """
            INSERT INTO users(username, password, full_name, email, phone, department)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {

            // check trùng
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setString(1, user.getUsername());
            psCheck.setString(2, user.getEmail());

            if (psCheck.executeQuery().next()) {
                System.out.println("Username hoặc Email đã tồn tại!");
                return false;
            }

            // insert
            PreparedStatement ps = conn.prepareStatement(insert);
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hash(user.getPassword()));
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getDepartment());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= GET ALL =================
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapUser(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= ADD ADMIN/SUPPORT =================
    public boolean addUser(User user) {
        String sql = """
            INSERT INTO users(username, password, full_name, email, phone, department, role)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hash(user.getPassword()));
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getDepartment());
            ps.setString(7, user.getRole());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= UPDATE PROFILE =================
    public boolean updateProfile(User user) {
        String sql = """
            UPDATE users 
            SET full_name=?, email=?, phone=?, department=? 
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getDepartment());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= MAP =================
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("department")
        );
    }

    //User
    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public List<User> searchByName(String keyword) {
        List<User> list = new ArrayList<>();

        String sql = "SELECT * FROM users WHERE full_name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapUser(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public boolean update(User user) {
        String sql = """
        UPDATE users 
        SET username=?, password=?, full_name=?, role=? 
        WHERE id=?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hash(user.getPassword()));
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
