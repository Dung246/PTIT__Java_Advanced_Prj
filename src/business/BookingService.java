package business;

import entity.Booking;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class BookingService {

    // ================= CHECK TRÙNG =================
    public boolean isConflict(int roomId, Timestamp start, Timestamp end) {

        String sql = """
            SELECT COUNT(*) FROM bookings
            WHERE room_id = ?
            AND (start_time < ? AND end_time > ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);
            ps.setTimestamp(2, end);
            ps.setTimestamp(3, start);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= CREATE =================
    public boolean create(Booking b) {

        String insertBooking = """
            INSERT INTO bookings
            (user_id, room_id, start_time, end_time, participant_count, status, support_status)
            VALUES (?,?,?,?,?,?,?)
        """;

        String insertService = """
            INSERT INTO booking_services(booking_id, service_id, quantity)
            VALUES (?,?,?)
        """;

        String insertEquipment = """
            INSERT INTO booking_equipment(booking_id, equipment_id, quantity)
            VALUES (?,?,?)
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false); // ✅ đúng vị trí

            try {
                Timestamp start = Timestamp.valueOf(b.getStartTime());
                Timestamp end = Timestamp.valueOf(b.getEndTime());

                // validate time
                if (end.before(start) || end.equals(start)) {
                    System.out.println("End phải sau Start");
                    return false;
                }

                // check trùng
                if (isConflict(b.getRoomId(), start, end)) {
                    System.out.println("Trùng lịch phòng!");
                    return false;
                }

                // ===== INSERT BOOKING =====
                PreparedStatement ps = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS);

                ps.setInt(1, b.getUserId());
                ps.setInt(2, b.getRoomId());
                ps.setTimestamp(3, start);
                ps.setTimestamp(4, end);
                ps.setInt(5, b.getParticipantCount());
                ps.setString(6, "PENDING");
                ps.setString(7, "PREPARING");

                ps.executeUpdate();

                // lấy booking_id
                ResultSet rs = ps.getGeneratedKeys();
                int bookingId = 0;
                if (rs.next()) bookingId = rs.getInt(1);

                // ===== SERVICE =====
                if (b.getServiceId() > 0) {
                    PreparedStatement psService = conn.prepareStatement(insertService);
                    psService.setInt(1, bookingId);
                    psService.setInt(2, b.getServiceId());
                    psService.setInt(3, 1);
                    psService.executeUpdate();
                }

                // ===== EQUIPMENT =====
                if (b.getEquipmentId() > 0) {
                    PreparedStatement psEquip = conn.prepareStatement(insertEquipment);
                    psEquip.setInt(1, bookingId);
                    psEquip.setInt(2, b.getEquipmentId());
                    psEquip.setInt(3, 1);
                    psEquip.executeUpdate();
                }

                conn.commit(); // ✅ commit đúng
                return true;

            } catch (Exception e) {
                conn.rollback(); // ✅ FIX QUAN TRỌNG
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= GET ALL =================
    public List<Booking> getAll() {

        List<Booking> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM bookings");

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= GET BY ID =================
    public Booking getById(int id) {

        String sql = "SELECT * FROM bookings WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) return map(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= MAP =================
    private Booking map(ResultSet rs) throws SQLException {

        Booking b = new Booking();

        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setRoomId(rs.getInt("room_id"));

        b.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        b.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());

        b.setParticipantCount(rs.getInt("participant_count"));

        b.setStatus(rs.getString("status"));
        b.setSupportStatus(rs.getString("support_status"));
        b.setSupportStaffId(rs.getInt("support_staff_id"));

        return b;
    }

    // ================= GET BY USER =================
    public List<Booking> getByUser(int userId) {

        List<Booking> list = new ArrayList<>();

        String sql = "SELECT * FROM bookings WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= CANCEL =================
    public boolean cancel(int id, int userId) {

        String sql = """
        DELETE FROM bookings
        WHERE id = ?
        AND user_id = ?
        AND status = 'PENDING'
    """;

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= UPDATE SUPPORT STATUS =================
    public boolean updateSupportStatus(int bookingId, int supportId, String status) {

        String sql = """
        UPDATE bookings 
        SET support_status = ?
        WHERE id = ?
        AND support_staff_id = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.setInt(3, supportId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= CHECK TRÙNG (JAVA) =================
    public boolean isTimeConflict(int roomId, LocalDateTime start, LocalDateTime end) {
        List<Booking> list = getByRoom(roomId);

        for (Booking b : list) {
            if (start.isBefore(b.getEndTime()) && end.isAfter(b.getStartTime())) {
                return true;
            }
        }
        return false;
    }

    public List<Booking> getByRoom(int roomId) {
        List<Booking> list = getAll();
        List<Booking> result = new ArrayList<>();

        for (Booking b : list) {
            if (b.getRoomId() == roomId) {
                result.add(b);
            }
        }
        return result;
    }

    // ================= ASSIGN SUPPORT =================
    public boolean assignSupport(int bookingId, int supportId, String status) {
        String sql = """
        UPDATE bookings 
        SET status=?, support_staff_id=?, support_status='PREPARING'
        WHERE id=? AND status='PENDING'
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, supportId);
            ps.setInt(3, bookingId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= GET BY SUPPORT =================
    public List<Booking> getBySupport(int supportId) {
        List<Booking> list = new ArrayList<>();

        String sql = """
        SELECT * FROM bookings
        WHERE support_staff_id=?
        AND status='APPROVED'
        AND support_status != 'READY' 
        AND DATE(start_time) = CURRENT_DATE
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supportId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs)); //  FIX
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public boolean updateStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, bookingId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
