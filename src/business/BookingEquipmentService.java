package business;

import utils.DBConnection;
import java.sql.*;

public class BookingEquipmentService {

    public boolean add(int bookingId, int eqId, int qty) {

        String check = "SELECT available_quantity FROM equipment WHERE id=?";
        String insert = "INSERT INTO booking_equipment VALUES (?,?,?)";
        String update = "UPDATE equipment SET available_quantity = available_quantity - ? WHERE id=?";

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(check);
            ps.setInt(1, eqId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println(" Không tồn tại!");
                return false;
            }

            int available = rs.getInt(1);

            if (qty <= 0 || qty > available) {
                System.out.println(" Số lượng không hợp lệ!");
                return false;
            }

            PreparedStatement ps2 = conn.prepareStatement(insert);
            ps2.setInt(1, bookingId);
            ps2.setInt(2, eqId);
            ps2.setInt(3, qty);
            ps2.executeUpdate();

            PreparedStatement ps3 = conn.prepareStatement(update);
            ps3.setInt(1, qty);
            ps3.setInt(2, eqId);
            ps3.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
