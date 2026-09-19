package com.train;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// ============================================================
//  DatabaseManager.java — RailConnect MySQL Integration
//  ► Run DBSetup.sql FIRST before using this
//  ► Add mysql-connector-j-8.x.xx.jar to your classpath
// ============================================================
public class DatabaseManager {

    // ── ✏️ Change these to match your MySQL ──────────────────
    private static final String DB_URL = "jdbc:mysql://localhost:3306/railconnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "aravind@161145"; // ← change this!
    // ─────────────────────────────────────────────────────────

    private static Connection conn = null;

    // ── Get / reuse connection ───────────────────────────────
    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✅ MySQL connected: railconnect");
        }
        return conn;
    }

    // ── Test if DB is reachable ──────────────────────────────
    public static boolean testConnection() {
        try {
            return getConnection() != null;
        } catch (SQLException e) {
            System.out.println("❌ DB Error: " + e.getMessage());
            return false;
        }
    }

    // ── Close connection ─────────────────────────────────────
    public static void close() {
        try {
            if (conn != null && !conn.isClosed())
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════
    // USER OPERATIONS
    // ════════════════════════════════════════════════════════

    /** Login normal user — returns ResultSet row or null */
    public static ResultSet loginUser(String username, String password) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT * FROM users WHERE username=? AND password=? AND is_admin=0");
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.out.println("loginUser error: " + e.getMessage());
            return null;
        }
    }

    /** Login admin user */
    public static ResultSet loginAdmin(String username, String password) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT * FROM users WHERE username=? AND password=? AND is_admin=1");
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.out.println("loginAdmin error: " + e.getMessage());
            return null;
        }
    }

    /** Register new user — returns true on success */
    public static boolean registerUser(String username, String password,
            String email, String phone,
            String fullName, String gender, String dob) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO users (username,password,email,phone,full_name,gender,dob,is_admin) VALUES (?,?,?,?,?,?,?,0)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, fullName);
            ps.setString(6, gender);
            ps.setString(7, dob);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("registerUser error: " + e.getMessage());
            return false;
        }
    }

    /** Check if username already exists */
    public static boolean usernameExists(String username) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Update user profile */
    public static boolean updateUserProfile(int userId, String fullName, String email, String phone) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE users SET full_name=?, email=?, phone=? WHERE id=?");
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Change password */
    public static boolean changePassword(int userId, String newPassword) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE users SET password=? WHERE id=?");
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Get all users — admin use */
    public static ResultSet getAllUsers() {
        try {
            return getConnection().createStatement().executeQuery(
                    "SELECT * FROM users ORDER BY id");
        } catch (SQLException e) {
            return null;
        }
    }

    /** Delete user */
    public static boolean deleteUser(int userId) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "DELETE FROM users WHERE id=? AND is_admin=0");
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ════════════════════════════════════════════════════════
    // TRAIN OPERATIONS
    // ════════════════════════════════════════════════════════

    /** Get all trains */
    public static ResultSet getAllTrains() {
        try {
            return getConnection().createStatement().executeQuery(
                    "SELECT * FROM trains ORDER BY train_no");
        } catch (SQLException e) {
            return null;
        }
    }

    /** Search trains by source and destination */
    public static ResultSet searchTrains(String source, String destination) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT * FROM trains WHERE source=? AND destination=? AND available_seats > 0");
            ps.setString(1, source);
            ps.setString(2, destination);
            return ps.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    /** Get train by train number */
    public static ResultSet getTrainByNo(String trainNo) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT * FROM trains WHERE train_no=?");
            ps.setString(1, trainNo);
            return ps.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    /** Add new train — admin */
    public static boolean addTrain(String trainNo, String trainName, String source,
            String destination, String departure, String arrival,
            String days, String trainType, int totalSeats, double baseFare) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO trains (train_no,train_name,source,destination,departure,arrival,days,train_type,total_seats,available_seats,base_fare) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1, trainNo);
            ps.setString(2, trainName);
            ps.setString(3, source);
            ps.setString(4, destination);
            ps.setString(5, departure);
            ps.setString(6, arrival);
            ps.setString(7, days);
            ps.setString(8, trainType);
            ps.setInt(9, totalSeats);
            ps.setInt(10, totalSeats);
            ps.setDouble(11, baseFare);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("addTrain error: " + e.getMessage());
            return false;
        }
    }

    /** Delete train — admin */
    public static boolean deleteTrain(String trainNo) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "DELETE FROM trains WHERE train_no=?");
            ps.setString(1, trainNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Update available seats after booking/cancel */
    public static boolean updateAvailableSeats(String trainNo, int change) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE trains SET available_seats = available_seats + ? WHERE train_no=?");
            ps.setInt(1, change);
            ps.setString(2, trainNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Get distinct source stations */
    public static List<String> getAllSources() {
        List<String> list = new ArrayList<>();
        try {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "SELECT DISTINCT source FROM trains ORDER BY source");
            while (rs.next())
                list.add(rs.getString("source"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Get distinct destination stations */
    public static List<String> getAllDestinations() {
        List<String> list = new ArrayList<>();
        try {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "SELECT DISTINCT destination FROM trains ORDER BY destination");
            while (rs.next())
                list.add(rs.getString("destination"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Get all unique stations (source + destination combined) */
    public static List<String> getAllStations() {
        List<String> list = new ArrayList<>();
        try {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "SELECT DISTINCT station FROM (SELECT source AS station FROM trains UNION SELECT destination AS station FROM trains) t ORDER BY station");
            while (rs.next())
                list.add(rs.getString("station"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ════════════════════════════════════════════════════════
    // BOOKING OPERATIONS
    // ════════════════════════════════════════════════════════

    /** Generate next PNR using stored procedure */
    public static String getNextPNR() {
        try {
            CallableStatement cs = getConnection().prepareCall(
                    "{CALL get_next_pnr(?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.execute();
            return cs.getString(1);
        } catch (SQLException e) {
            // Fallback: manual PNR
            System.out.println("PNR proc error: " + e.getMessage());
            return "PNR" + String.format("%07d", System.currentTimeMillis() % 9999999);
        }
    }

    /** Save a new booking */
    public static boolean saveBooking(String pnr, int userId, String trainNo, String trainName,
            String source, String destination, String journeyDate,
            String seatClass, String quota, int passengerCount,
            double totalFare, String paymentMode) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO bookings (pnr,user_id,train_no,train_name,source,destination,journey_date,seat_class,quota,passenger_count,total_fare,payment_mode,status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'CNF')");
            ps.setString(1, pnr);
            ps.setInt(2, userId);
            ps.setString(3, trainNo);
            ps.setString(4, trainName);
            ps.setString(5, source);
            ps.setString(6, destination);
            ps.setString(7, journeyDate);
            ps.setString(8, seatClass);
            ps.setString(9, quota);
            ps.setInt(10, passengerCount);
            ps.setDouble(11, totalFare);
            ps.setString(12, paymentMode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("saveBooking error: " + e.getMessage());
            return false;
        }
    }

    /** Save a passenger record */
    public static boolean savePassenger(String pnr, String name, int age,
            String gender, String berthPref,
            String nationality, String seatNo) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO passengers (pnr,name,age,gender,berth_pref,nationality,seat_no) VALUES (?,?,?,?,?,?,?)");
            ps.setString(1, pnr);
            ps.setString(2, name);
            ps.setInt(3, age);
            ps.setString(4, gender);
            ps.setString(5, berthPref);
            ps.setString(6, nationality);
            ps.setString(7, seatNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("savePassenger error: " + e.getMessage());
            return false;
        }
    }

    /** Get all bookings for a user */
    public static ResultSet getBookingsByUser(int userId) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT * FROM bookings WHERE user_id=? ORDER BY booking_date DESC");
            ps.setInt(1, userId);
            return ps.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    /** Get booking by PNR */
    public static ResultSet getBookingByPNR(String pnr) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT * FROM bookings WHERE pnr=?");
            ps.setString(1, pnr);
            return ps.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    /** Get passengers for a PNR */
    public static ResultSet getPassengersByPNR(String pnr) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT * FROM passengers WHERE pnr=?");
            ps.setString(1, pnr);
            return ps.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    /** Cancel a booking */
    public static boolean cancelBooking(String pnr) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE bookings SET status='CAN' WHERE pnr=?");
            ps.setString(1, pnr);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Get all bookings — admin */
    public static ResultSet getAllBookings() {
        try {
            return getConnection().createStatement().executeQuery(
                    "SELECT b.*, u.username, u.full_name FROM bookings b " +
                            "JOIN users u ON b.user_id=u.id ORDER BY b.booking_date DESC");
        } catch (SQLException e) {
            return null;
        }
    }

    /** Count total confirmed bookings — admin dashboard */
    public static int countConfirmedBookings() {
        try {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "SELECT COUNT(*) FROM bookings WHERE status='CNF'");
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Get total revenue — admin dashboard */
    public static double getTotalRevenue() {
        try {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "SELECT COALESCE(SUM(total_fare),0) FROM bookings WHERE status='CNF'");
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // ════════════════════════════════════════════════════════
    // STATION OPERATIONS
    // ════════════════════════════════════════════════════════

    /** Get all stations */
    public static ResultSet getAllStationRecords() {
        try {
            return getConnection().createStatement().executeQuery(
                    "SELECT * FROM stations ORDER BY station_name");
        } catch (SQLException e) {
            return null;
        }
    }

    /** Add a new station */
    public static boolean addStation(String code, String name, String city, String state) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO stations (station_code,station_name,city,state) VALUES (?,?,?,?)");
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, city);
            ps.setString(4, state);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}