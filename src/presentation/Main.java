    package presentation;

    import business.AuthService;
    import entity.User;

    import java.util.Scanner;

    public class Main {

        static Scanner sc = new Scanner(System.in);
        static AuthService auth = new AuthService();

        //  COLOR
        public static final String RESET = "\033[0m";
        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
        public static final String CYAN = "\033[0;36m";

        // ================= MAIN =================

        public static void main(String[] args) {

            while (true) {
                printHeader("HỆ THỐNG QUẢN LÝ ĐẶT PHÒNG");

                System.out.println(CYAN + "║  1. Đăng ký tài khoản                    ║");
                System.out.println("║  2. Đăng nhập                            ║");
                System.out.println("║  0. Thoát chương trình                   ║" + RESET);

                printFooter();

                int c = getInt("Chọn chức năng: ");

                switch (c) {
                    case 1 -> register();
                    case 2 -> login();
                    case 0 -> exitApp();
                    default -> System.out.println(RED + "Lựa chọn không hợp lệ!" + RESET);
                }
            }
        }

        // ================= UI =================
        static void printHeader(String title) {
            int width = 42;

            System.out.println(YELLOW + "\n╔" + "═".repeat(width) + "╗");

            String padded = center(title, width);
            System.out.println("║" + padded + "║");

            System.out.println("╠" + "═".repeat(width) + "╣" + RESET);
        }

        static void printFooter() {
            System.out.println(YELLOW + "╚" + "═".repeat(42) + "╝" + RESET);
        }

        static String center(String text, int width) {
            int padding = (width - text.length()) / 2;
            return " ".repeat(Math.max(0, padding)) + text
                    + " ".repeat(Math.max(0, width - text.length() - padding));
        }

        static void loading(String msg) {
            System.out.print(CYAN + msg);
            try {
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(300);
                    System.out.print(" •");
                }
            } catch (Exception ignored) {}
            System.out.println(RESET);
        }

        // ================= INPUT =================

        static int getInt(String msg) {
            while (true) {
                try {
                    System.out.print(msg);
                    return Integer.parseInt(sc.nextLine());
                } catch (Exception e) {
                    System.out.println(RED + "Vui lòng nhập số!" + RESET);
                }
            }
        }

        // ================= REGISTER =================

        static void register() {

            printHeader("ĐĂNG KÝ TÀI KHOẢN");

            System.out.print("Tên người dùng: ");
            String u = sc.nextLine();

            String p;
            while (true) {
                while (true) {

                    if (System.console() != null) {
                        p = new String(System.console().readPassword("Mật khẩu (>=6): "));
                    } else {
                        System.out.print("Password (>=6): ");
                        p = sc.nextLine();
                    }

                    if (p.length() < 6) {
                        System.out.println(RED + "Mật khẩu phải >= 6 ký tự!" + RESET);
                    } else break;
                }

                if (p.length() < 6) {
                    System.out.println(RED + "Mật khẩu phải >= 6 ký tự!" + RESET);
                } else break;
            }

            System.out.print("Họ tên: ");
            String f = sc.nextLine();

            //  THÊM EMAIL
            String email;
            while (true) {
                System.out.print("Email: ");
                email = sc.nextLine();

                if (!email.contains("@")) {
                    System.out.println(RED + "Email không hợp lệ!" + RESET);
                } else break;
            }

            System.out.print("Điện thoại: ");
            String phone = sc.nextLine();

            System.out.print("Phòng ban: ");
            String d = sc.nextLine();

            loading("Đang xử lý");

            // CONSTRUCTOR MỚI
            User user = new User(u, p, f, email, phone, d);

            if (auth.register(user)) {
                System.out.println(GREEN + "Đăng ký thành công!" + RESET);
            } else {
                System.out.println(RED + "Username hoặc Email đã tồn tại!" + RESET);
            }

            printFooter();
        }

        // ================= LOGIN =================

        static void login() {

            printHeader("ĐĂNG NHẬP HỆ THỐNG");

            System.out.print(" Username: ");
            String u = sc.nextLine();

            String p;
            if (System.console() != null) {
                p = new String(System.console().readPassword("Password: "));
            } else {
                System.out.print("Password: ");
                p = sc.nextLine();
            }

            loading("Đang đăng nhập");

            User user = auth.login(u, p);

            if (user == null) {
                System.out.println(RED + " Sai tài khoản hoặc mật khẩu!" + RESET);
                printFooter();
                return;
            }

            System.out.println(GREEN + " Xin chào " + user.getFullName() + RESET);
            printFooter();

            Menu.dashboard(user);
        }

        // ================= EXIT =================

        static void exitApp() {
            loading("Đang thoát");
            System.out.println(YELLOW + " Tạm biệt!" + RESET);
            System.exit(0);
        }
    }
