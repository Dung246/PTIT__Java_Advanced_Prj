package presentation;

import business.*;
import entity.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

import static presentation.Main.*;

public class Menu {
    static Scanner sc = new Scanner(System.in);

    static ServiceManager service = new ServiceManager();
    static RoomService roomService = new RoomService();
    static EquipmentService equipmentService = new EquipmentService();
    static BookingService bookingService= new BookingService();
    static UserService userService = new UserService();
    static User currentUser = null;

    // ================= UI =================

    static void printHeader(String title) {
        System.out.println(YELLOW + "\n========================================");
        System.out.println("        " + title);
        System.out.println("========================================" + RESET);
    }

    static void loading(String msg) {
        System.out.print(CYAN + msg);
        try {
            for (int i = 0; i < 3; i++) {
                Thread.sleep(300);
                System.out.print(".");
            }
        } catch (Exception ignored) {}
        System.out.println(RESET);
    }

    // ================= DASHBOARD (FIX SRS) =================

    public static void dashboard(User user) {

        while (true) {
            printHeader("DASHBOARD - " + user.getFullName());

            // ===== ADMIN =====
            if (user.getRole().equalsIgnoreCase("ADMIN")) {

                System.out.println(CYAN + "1.Quản lý dịch vụ");
                System.out.println("2.Quản lý phòng");
                System.out.println("3.Quản lý thiết bị");
                System.out.println("4.Quản lý người dùng");
                System.out.println("5.Duyệt & phân công");
                System.out.println("0.Đăng xuất" + RESET);

                int c = getInt("Chọn: ");

                switch (c) {
                    case 1 -> menuService();
                    case 2 -> menuRoom();
                    case 3 -> menuEquipment();
                    case 4 -> menuUser();
                    case 5 -> approveAndAssign();
                    case 0 -> {
                        System.out.println(YELLOW + " Đăng xuất..." + RESET);
                        return;
                    }
                    default -> System.out.println(RED + " Không hợp lệ!" + RESET);
                }
            }

            // ===== EMPLOYEE =====
            else if (user.getRole().equalsIgnoreCase("EMPLOYEE")) {

                System.out.println(CYAN + "1.Đặt phòng");
                System.out.println("2.Xem lịch của tôi");
                System.out.println("0.Trở lại" + RESET);

                int c = getInt("Chọn: ");

                switch (c) {
                    case 1 -> bookingStub(user);
                    case 2 -> viewMyBookingStub(user);
                    case 0 -> {
                        System.out.println(YELLOW + "Đăng xuất..." + RESET);
                        return;
                    }
                    default -> System.out.println(RED + " Không hợp lệ!" + RESET);
                }
            }

            // ===== SUPPORT =====
            else if (user.getRole().equalsIgnoreCase("SUPPORT")) {

                System.out.println(CYAN + "1.Công việc được phân công");
                System.out.println("2.Cập nhật trạng thái");
                System.out.println("0.Thoát" + RESET);

                int c = getInt(" Chọn: ");

                switch (c) {
                    case 1 -> supportTaskStub(user);
                    case 2 -> updateStatusStub(user);
                    case 0 -> {
                        System.out.println(YELLOW + " Đăng xuất..." + RESET);
                        return;
                    }
                    default -> System.out.println(RED + "Không hợp lệ!" + RESET);
                }
            }

            else {
                System.out.println(RED + "Role không hợp lệ!" + RESET);
                return;
            }
        }
    }

    // ================= STUB (FULL SRS) =================

    static void bookingStub(User user) {

        printHeader("ĐẶT PHÒNG");

        // ===== chọn phòng =====
        int roomId = getValidRoomId();

        int people = getPositiveInt("Số người: ");

        // ===== thời gian =====
        LocalTime[] times = getValidTime();
        LocalTime startTime = times[0];
        LocalTime endTime = times[1];

        java.time.LocalDate today = java.time.LocalDate.now();

        //  check trùng lịch
        while (true) {
            boolean conflict = bookingService.isTimeConflict(
                    roomId,
                    java.time.LocalDateTime.of(today, startTime),
                    java.time.LocalDateTime.of(today, endTime)
            );

            if (!conflict) break;

            System.out.println(RED + " Phòng đã bị đặt trong khoảng thời gian này!" + RESET);

            times = getValidTime();
            startTime = times[0];
            endTime = times[1];
        }

        // ===== service =====
        int serviceId = getValidServiceId();

        // ===== equipment =====
        int equipmentId = getValidEquipmentId();

        // ===== tạo booking =====
        Booking b = new Booking();
        b.setUserId(user.getId());
        b.setRoomId(roomId);
        b.setParticipantCount(people);
        b.setStartTime(java.time.LocalDateTime.of(today, startTime));
        b.setEndTime(java.time.LocalDateTime.of(today, endTime));
        b.setServiceId(serviceId);
        b.setEquipmentId(equipmentId);

        b.setStatus("PENDING");
        b.setSupportStatus("PREPARING");

        loading("Đang đặt");

        if (bookingService.create(b)) {
            System.out.println(GREEN + " Đặt phòng thành công!" + RESET);
        } else {
            System.out.println(RED + " Thất bại!" + RESET);
        }
    }

// ==================================================

    static void viewMyBookingStub(User user) {

        printHeader("LỊCH CỦA TÔI");

        List<Booking> list = bookingService.getByUser(user.getId());

        if (list.isEmpty()) {
            System.out.println(RED + "Không có lịch!" + RESET);
            return;
        }

        System.out.println(GREEN + "--------------------------------------------------------------------------" + RESET);
        System.out.printf("%-5s | %-8s | %-20s | %-10s | %-15s\n",
                "ID", "Room", "Time", "Status", "Support");
        System.out.println(GREEN + "--------------------------------------------------------------------------" + RESET);

        for (Booking b : list) {
            System.out.printf("%-5d | %-8d | %-20s | %-10s | %-15s\n",
                    b.getId(),
                    b.getRoomId(),
                    b.getStartTime(),
                    b.getStatus(),
                    b.getSupportStatus());
        }

        System.out.println(GREEN + "--------------------------------------------------------------------------" + RESET);
        System.out.println("\n1. Hủy lịch");
        System.out.println("0. Quay lại");

        int c = getInt("Chọn: ");

        if (c == 1) {

            int id = getInt("Nhập ID cần hủy: ");

            Booking b = bookingService.getById(id);

            // ===== validate =====
            if (b == null) {
                System.out.println(RED + "Không tồn tại!" + RESET);
                return;
            }

            if (b.getUserId() != user.getId()) {
                System.out.println(RED + "Không phải lịch của bạn!" + RESET);
                return;
            }

            //  chỉ cho hủy khi chưa duyệt
            if (!b.getStatus().equalsIgnoreCase("PENDING")) {
                System.out.println(RED + "Chỉ được hủy khi chưa duyệt!" + RESET);
                return;
            }

            loading("Đang hủy");

            if (bookingService.cancel(id, user.getId())) {
                System.out.println(GREEN + "Đã hủy thành công!" + RESET);
            } else {
                System.out.println(RED + "Hủy thất bại!" + RESET);
            }
        }
    }

// ==================================================

    static void supportTaskStub(User user) {

        printHeader("TASK SUPPORT");

        List<Booking> list = bookingService.getBySupport(user.getId());

        if (list.isEmpty()) {
            System.out.println(RED + "Không có task!" + RESET);
            return;
        }

        System.out.println(GREEN + "--------------------------------------------------------------------------" + RESET);
        System.out.printf("%-5s | %-8s | %-20s | %-10s | %-15s\n",
                "ID", "Room", "Time", "Status", "Support");
        System.out.println(GREEN + "--------------------------------------------------------------------------" + RESET);

        for (Booking b : list) {
            System.out.printf("%-5d | %-8d | %-20s | %-10s | %-15s\n",
                    b.getId(),
                    b.getRoomId(),
                    b.getStartTime(),
                    b.getStatus(),
                    b.getSupportStatus());
        }

        System.out.println(GREEN + "--------------------------------------------------------------------------" + RESET);
    }

// ==================================================

    static void updateStatusStub(User user) {

        printHeader("UPDATE SUPPORT STATUS");

        int id = getInt("Booking ID: ");

        Booking b = bookingService.getById(id);

        if (b == null) {
            System.out.println(RED + "Không tồn tại booking!" + RESET);
            return;
        }
        if (b.getSupportStaffId() != user.getId()) {
            System.out.println(RED + "Không phải việc của bạn!" + RESET);
            return;
        }

        System.out.println("1. PREPARING");
        System.out.println("2. READY");
        System.out.println("3. MISSING");

        int c = getInt("Chọn: ");

        String status = switch (c) {
            case 1 -> "PREPARING";
            case 2 -> "READY";
            case 3 -> "MISSING";
            default -> {
                System.out.println(RED + "Không hợp lệ!" + RESET);
                yield "PREPARING";
            }
        };

        loading("Đang cập nhật");

        boolean ok = bookingService.updateSupportStatus(id, user.getId(), status);
        if (ok) {
            System.out.println(GREEN + "Đã cập nhật!" + RESET);
        } else {
            System.out.println(RED + "Không phải việc của bạn hoặc lỗi!" + RESET);
        }

        System.out.println(GREEN + "Đã cập nhật!" + RESET);
    }

    // ================= SERVICE =================

    static void menuService() {
        while (true) {
            printHeader("SERVICE MANAGEMENT");

            System.out.println(CYAN + "1.Thêm");
            System.out.println("2.Xem danh sách");
            System.out.println("3.Sửa danh sách");
            System.out.println("4.Xóa danh sách");
            System.out.println("5.Tìm kiếm theo tên");
            System.out.println("0.Trở lại" + RESET);

            int c = getInt(" Chọn: ");

            switch (c) {
                case 1 -> addService();
                case 2 -> showService();
                case 3 -> updateService();
                case 4 -> deleteService();
                case 5 -> searchService();
                case 0 -> { return; }
                default -> System.out.println(RED + " Sai!" + RESET);
            }
        }
    }

    static void addService() {
        String name = getString("Tên: ");
        double price = getPositiveDouble("Giá: ");

        loading(" Đang thêm");

        try {
            service.add(new Service(name, price));
            System.out.println(GREEN + " Thành công!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + " Lỗi!" + RESET);
        }
    }

    static void showService() {
        try {
            List<Service> list = service.getAll();

            if (list.isEmpty()) {
                System.out.println(RED + " Không có dữ liệu!" + RESET);
                return;
            }

            System.out.println(GREEN + "\n SERVICE LIST" + RESET);
            System.out.println(GREEN + "-----------------------------------------------" + RESET);
            System.out.printf("%-5s | %-25s | %-10s\n", "ID", "Tên", "Giá");
            System.out.println(GREEN + "-----------------------------------------------" + RESET);

            for (Service s : list) {
                System.out.printf("%-5d | %-25s | %-10.0f\n",
                        s.getId(), s.getName(), s.getPrice());
            }

            System.out.println(GREEN + "-----------------------------------------------" + RESET);


        } catch (Exception e) {
            System.out.println(RED + " Lỗi DB!" + RESET);
        }
    }

    static void updateService() {
        showService();

        int id = getInt("ID: ");
        String name = getString("Tên mới: ");
        double price = getPositiveDouble("Giá: ");

        loading(" Đang cập nhật");

        try {
            service.update(new Service(id, name, price));
            System.out.println(GREEN + " OK!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + " Lỗi!" + RESET);
        }
    }

    static void deleteService() {
        showService();

        int id = getInt("ID: ");

        loading(" Đang xóa");

        try {
            service.delete(id);
            System.out.println(GREEN + " Đã xóa!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + " Lỗi!" + RESET);
        }
    }

    static void searchService() {

        String keyword = getString("Nhập tên cần tìm: ");

        List<Service> list = service.searchByName(keyword);

        if (list.isEmpty()) {
            System.out.println(RED + "Không tìm thấy!" + RESET);
            return;
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
        System.out.printf("%-5s | %-25s | %-10s\n", "ID", "Tên", "Giá");
        System.out.println(GREEN + "-----------------------------------------------" + RESET);

        for (Service s : list) {
            System.out.printf("%-5d | %-25s | %-10.0f\n",
                    s.getId(), s.getName(), s.getPrice());
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
    }

    // thời gian cụ thể
    static java.time.LocalTime parseTime(String input) {

        input = input.trim().toLowerCase();

        if (input.contains("h")) {
            String[] parts = input.split("h");

            int hour = Integer.parseInt(parts[0]);
            int minute = (parts.length > 1 && !parts[1].isEmpty())
                    ? Integer.parseInt(parts[1])
                    : 0;

            return java.time.LocalTime.of(hour, minute);
        }

        return java.time.LocalTime.parse(input);
    }

    // ================= ROOM =================

    static void menuRoom() {
        while (true) {
            printHeader("ROOM MANAGEMENT");

            System.out.println(CYAN + "1.Thêm phòng");
            System.out.println("2.Xem phòng ");
            System.out.println("3.Sửa phòng");
            System.out.println("4.Xóa phòng");
            System.out.println("5.Tìm phòng theo tên");
            System.out.println("0.Trở lại" + RESET);

            int c = getInt(" Chọn: ");

            switch (c) {
                case 1 -> addRoom();
                case 2 -> showRoom();
                case 3 -> updateRoom();
                case 4 -> deleteRoom();
                case 5 -> searchRoom();
                case 0 -> { return; }
                default -> System.out.println(RED + " Mời chọn lại!" + RESET);
            }
        }
    }

    static void addRoom() {
        String name = getString("Tên phòng ban: ");
        int cap = getPositiveInt("Sức chứa phòng: ");

        loading(" Đang thêm");

        roomService.add(new Room(name, cap));
        System.out.println(GREEN + " Hợp lệ!" + RESET);
    }

    static void showRoom() {
        List<Room> list = roomService.getAll();

        if (list.isEmpty()) {
            System.out.println(RED + " Không có phòng!" + RESET);
            return;
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
        System.out.printf("%-5s | %-20s | %-10s\n", "ID", "Tên", "Sức chứa");
        System.out.println(GREEN + "-----------------------------------------------" + RESET);

        for (Room r : list) {
            System.out.printf("%-5d | %-20s | %-10d\n",
                    r.getId(), r.getName(), r.getCapacity());
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
    }

    static void updateRoom() {
        showRoom();

        int id = getInt("ID: ");
        String name = getString("Tên phòng mới: ");
        int cap = getPositiveInt("Sức chứa phòng: ");

        loading(" Đang cập nhật");

        roomService.update(new Room(id, name, cap));
        System.out.println(GREEN + " OK!" + RESET);
    }

    static void deleteRoom() {
        showRoom();

        int id = getInt("ID: ");

        loading(" Đang xóa");

        roomService.delete(id);
        System.out.println(GREEN + " Đã xóa!" + RESET);
    }

    static void searchRoom() {

        String keyword = getString("Nhập tên phòng: ");

        List<Room> list = roomService.searchByName(keyword);

        if (list.isEmpty()) {
            System.out.println(RED + "Không tìm thấy!" + RESET);
            return;
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
        System.out.printf("%-5s | %-20s | %-10s\n", "ID", "Tên", "Sức chứa");
        System.out.println(GREEN + "-----------------------------------------------" + RESET);

        for (Room r : list) {
            System.out.printf("%-5d | %-20s | %-10d\n",
                    r.getId(), r.getName(), r.getCapacity());
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
    }

    // ================= EQUIPMENT =================

    static void menuEquipment() {
        while (true) {
            printHeader("EQUIPMENT MANAGEMENT");

            System.out.println(CYAN + "1.Thêm thiết bị");
            System.out.println("2.Xem danh sách thiết bị");
            System.out.println("3.Sửa thiết bị");
            System.out.println("4.Xóa thiết bị");
            System.out.println("5.Tìm thiết bị theo tên");
            System.out.println("0.Trở lại" + RESET);

            int c = getInt("Chọn lựa chọn: ");

            switch (c) {
                case 1 -> addEquipment();
                case 2 -> showEquipment();
                case 3 -> updateEquipment();
                case 4 -> deleteEquipment();
                case 5 -> searchEquipment();
                case 0 -> { return; }
                default -> System.out.println(RED + " Sai!" + RESET);
            }
        }
    }

    static void addEquipment() {
        String name = getString("Tên thiết bị: ");
        int q = getPositiveInt("Số lượng thiết bị: ");

        loading(" Đang thêm");

        equipmentService.add(new Equipment(name, q));
        System.out.println(GREEN + " OK!" + RESET);
    }


    static void showEquipment() {
        List<Equipment> list = equipmentService.getAll();

        if (list.isEmpty()) {
            System.out.println(RED + " Không có thiết bị!" + RESET);
            return;
        }

        System.out.println(GREEN + "\n EQUIPMENT LIST" + RESET);
        System.out.println(GREEN + "-----------------------------------------------" + RESET);
        System.out.printf("%-5s | %-20s | %-10s\n", "ID", "Tên", "Số lượng");
        System.out.println(GREEN + "-----------------------------------------------" + RESET);

        for (Equipment e : list) {
            System.out.printf("%-5d | %-20s | %-10d\n",
                    e.getId(), e.getName(), e.getAvailableQuantity());
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
    }

    static void updateEquipment() {
        showEquipment();

        int id = getInt("ID: ");
        String name = getString("Tên thiết bị mới: ");
        int q = getPositiveInt("Số lượng thiết bị: ");

        loading(" Đang cập nhật");

        equipmentService.update(new Equipment(id, name, q));
        System.out.println(GREEN + " OK!" + RESET);
    }

    static void deleteEquipment() {
        showEquipment();

        int id = getInt("ID: ");

        loading(" Đang xóa");

        equipmentService.delete(id);
        System.out.println(GREEN + " Đã xóa!" + RESET);
    }

    static void searchEquipment() {

        String keyword = getString("Nhập tên thiết bị: ");

        List<Equipment> list = equipmentService.searchByName(keyword);

        if (list.isEmpty()) {
            System.out.println(RED + "Không tìm thấy!" + RESET);
            return;
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
        System.out.printf("%-5s | %-20s | %-10s\n", "ID", "Tên", "Số lượng");
        System.out.println(GREEN + "-----------------------------------------------" + RESET);

        for (Equipment e : list) {
            System.out.printf("%-5d | %-20s | %-10d\n",
                    e.getId(), e.getName(), e.getAvailableQuantity());
        }

        System.out.println(GREEN + "-----------------------------------------------" + RESET);
    }

    // ================= VALIDATE =================

    static int getInt(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println(RED + " Phải nhập số!" + RESET);
            }
        }
    }

    static int getPositiveInt(String msg) {
        while (true) {
            int n = getInt(msg);
            if (n > 0) return n;
            System.out.println(RED + " Phải > 0!" + RESET);
        }
    }

    static double getDouble(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Double.parseDouble(sc.nextLine());
            } catch (Exception e) {
                System.out.println(RED + " Phải nhập số!" + RESET);
            }
        }
    }

    static double getPositiveDouble(String msg) {
        while (true) {
            double d = getDouble(msg);
            if (d > 0) return d;
            System.out.println(RED + " Phải > 0!" + RESET);
        }
    }

    static String getString(String msg) {
        while (true) {
            System.out.print(msg);
            String s = sc.nextLine();
            if (!s.trim().isEmpty()) return s;
            System.out.println(RED + " Không được để trống!" + RESET);
        }
    }

    static int getValidRoomId() {
        while (true) {
            showRoom();
            int id = getInt("Chọn ID phòng: ");

            Room r = roomService.getById(id);
            if (r != null) return id;

            System.out.println(RED + " ID phòng không tồn tại! Nhập lại!" + RESET);
        }
    }

    static int getValidServiceId() {
        while (true) {
            showService();
            int id = getInt("Service ID (0 = bỏ qua): ");

            if (id == 0) return 0;

            Service s = service.getById(id);
            if (s != null) return id;

            System.out.println(RED + " Service không tồn tại!" + RESET);
        }
    }

    static int getValidEquipmentId() {
        while (true) {
            showEquipment();
            int id = getInt("Equipment ID: ");

            if (id == 0) return 0;

            Equipment e = equipmentService.getById(id);
            if (e != null) return id;

            System.out.println(RED + " Equipment không tồn tại!" + RESET);
        }
    }

    static LocalTime[] getValidTime() {
        while (true) {
            String startStr = getString("Bắt đầu (vd 8h30): ");
            String endStr = getString("Kết thúc (vd 10h): ");

            try {
                LocalTime start = parseTime(startStr);
                LocalTime end = parseTime(endStr);

                if (end.isAfter(start)) {
                    return new LocalTime[]{start, end};
                }

            } catch (Exception e) {}

            System.out.println(RED + " Thời gian không hợp lệ! Nhập lại!" + RESET);
        }
    }

    static void menuUser() {
        while (true) {
            printHeader("USER MANAGEMENT");

            System.out.println(CYAN + "1.Xem danh sách");
            System.out.println("2.Cập nhật role");
            System.out.println("3.Tìm kiếm");
            System.out.println("0.Trở lại" + RESET);

            int c = getInt("Chọn: ");

            switch (c) {
                case 1 -> showUser();
                case 2 -> updateUserRole();
                case 3 -> searchUser();
                case 0 -> { return; }
                default -> System.out.println(RED + "Sai!" + RESET);
            }
        }
    }


    static void showUser() {
        List<User> list = userService.getAll();

        if (list.isEmpty()) {
            System.out.println(RED + "Không có user!" + RESET);
            return;
        }

        System.out.println(GREEN + "-------------------------------------------------------------------------------" + RESET);
        System.out.printf("%-5s | %-20s | %-15s | %-25s | %-10s\n",
                "ID", "Tên", "Username", "Email", "Role");
        System.out.println(GREEN + "-------------------------------------------------------------------------------" + RESET);

        for (User u : list) {
            System.out.printf("%-5d | %-20s | %-15s | %-25s | %-10s\n",
                    u.getId(),
                    u.getFullName(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole());
        }

        System.out.println(GREEN + "-------------------------------------------------------------------------------" + RESET);
    }

    static void updateUserRole() {

        showUser();

        int id = getInt("Nhập ID user: ");

        User u = userService.getById(id);

        if (u == null) {
            System.out.println(RED + "Không tồn tại!" + RESET);
            return;
        }

        System.out.println("Role hiện tại: " + u.getRole());
        String role = getString("Role mới (ADMIN/EMPLOYEE/SUPPORT): ");

        u.setRole(role);

        userService.update(u);

        System.out.println(GREEN + "Đã cập nhật role!" + RESET);
    }

    static void searchUser() {
        String keyword = getString("Nhập tên: ");

        List<User> list = userService.searchByName(keyword);

        if (list.isEmpty()) {
            System.out.println(RED + "Không tìm thấy!" + RESET);
            return;
        }

        for (User u : list) {
            System.out.println(u.getId() + " - " + u.getFullName() + " - " + u.getRole());
        }
    }

    static void approveAndAssign() {

        while (true) {

            printHeader("DUYỆT & PHÂN CÔNG");

            System.out.println(CYAN + "1. Duyệt booking");
            System.out.println("2. Phân công support");
            System.out.println("0. Trở lại" + RESET);

            int choice = getInt("Chọn: ");

            switch (choice) {
                case 1 -> approveBooking();   // duyệt
                case 2 -> assignSupport();    // phân công
                case 0 -> {
                    System.out.println(YELLOW + "Quay lại..." + RESET);
                    return;
                }
                default -> System.out.println(RED + "Không hợp lệ!" + RESET);
            }
        }
    }
    static void approveBooking() {

        printHeader("DUYỆT BOOKING");

        List<Booking> list = bookingService.getAll();

        boolean found = false;

        for (Booking b : list) {
            if (b.getStatus().equalsIgnoreCase("PENDING")) {

                System.out.printf("ID:%d | Room:%d | User:%d | %s -> %s\n",
                        b.getId(),
                        b.getRoomId(),
                        b.getUserId(),
                        b.getStartTime(),
                        b.getEndTime()
                );

                found = true;
            }
        }

        if (!found) {
            System.out.println(RED + "Không có booking chờ duyệt!" + RESET);
            return;
        }

        int id = getInt("Chọn booking: ");
        Booking b = bookingService.getById(id);

        if (b == null || !b.getStatus().equalsIgnoreCase("PENDING")) {
            System.out.println(RED + "Không hợp lệ!" + RESET);
            return;
        }

        // check trùng lịch
        if (bookingService.isTimeConflict(
                b.getRoomId(),
                b.getStartTime(),
                b.getEndTime()
        )) {
            System.out.println(RED + "Trùng lịch phòng!" + RESET);
            return;
        }

        System.out.println("1. APPROVED");
        System.out.println("2. REJECTED");

        int c = getInt("Chọn: ");

        String status = (c == 1) ? "APPROVED" : "REJECTED";

        boolean ok = bookingService.updateStatus(id, status);

        if (ok) {
            System.out.println(GREEN + "Đã cập nhật!" + RESET);
        } else {
            System.out.println(RED + "Thất bại!" + RESET);
        }
    }
    static void assignSupport() {

        printHeader("PHÂN CÔNG SUPPORT");

        List<Booking> list = bookingService.getAll();

        boolean found = false;

        for (Booking b : list) {

            if (b.getStatus().equalsIgnoreCase("APPROVED")
                    && b.getSupportStaffId() == 0) {

                System.out.printf("ID:%d | Room:%d | Time:%s\n",
                        b.getId(),
                        b.getRoomId(),
                        b.getStartTime()
                );

                found = true;
            }
        }

        if (!found) {
            System.out.println(RED + "Không có booking cần phân công!" + RESET);
            return;
        }

        int bookingId = getInt("Chọn booking: ");
        Booking b = bookingService.getById(bookingId);

        if (b == null || !b.getStatus().equalsIgnoreCase("APPROVED")) {
            System.out.println(RED + "Không hợp lệ!" + RESET);
            return;
        }

        // ===== hiển thị SUPPORT =====
        List<User> users = userService.getAll();

        for (User u : users) {
            if (u.getRole().equalsIgnoreCase("SUPPORT")) {
                System.out.printf("%d - %s\n", u.getId(), u.getFullName());
            }
        }

        int supportId = getInt("Chọn SUPPORT: ");

        User s = userService.getById(supportId);

        if (s == null || !s.getRole().equalsIgnoreCase("SUPPORT")) {
            System.out.println(RED + "Không hợp lệ!" + RESET);
            return;
        }

        boolean ok = bookingService.assignSupport(bookingId, supportId, "APPROVED");

        if (ok) {
            System.out.println(GREEN + "Phân công thành công!" + RESET);
        } else {
            System.out.println(RED + "Thất bại!" + RESET);
        }
    }

}
