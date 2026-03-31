package business;

import entity.User;
import utils.DBConnection;

import java.sql.*;

public class AuthService {

    public boolean register(User u) {
        String sql = """
INSERT INTO users(username, password, full_name, email, phone, department)
VALUES (?, ?, ?, ?, ?, ?)
""";

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getDepartment());

            int rows =ps.executeUpdate();
            return rows >0;

        } catch (SQLException e) {
            e.printStackTrace(); // 🔥 in lỗi thật
            System.out.println("Lỗi đăng ký (có thể trùng username/email)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name")); //  QUAN TRỌNG
                u.setRole(rs.getString("role"));

                return u;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
