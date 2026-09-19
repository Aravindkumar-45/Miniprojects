package com.train;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.collections.*;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// ============================================================
//  RailConnect — Online Train Ticket Booking System
//  Light Theme | IRCTC-style | All Errors Fixed
// ============================================================
public class OnlineTrainBooking extends Application {

    // ── Colours — 4K Premium Crisp Theme ─────────────────────
    static final String C_BG = "#F4F7FB";
    static final String C_SURFACE = "#FFFFFF";
    static final String C_BORDER = "#CBD5E1";
    static final String C_PRIMARY = "#2563EB";
    static final String C_PRIMARY_L = "#EFF6FF";
    static final String C_ACCENT = "#EA580C";
    static final String C_SUCCESS = "#059669";
    static final String C_SUCCESS_L = "#D1FAE5";
    static final String C_DANGER = "#DC2626";
    static final String C_DANGER_L = "#FEE2E2";
    static final String C_TEXT = "#0F172A";
    static final String C_SUBTEXT = "#334155";
    static final String C_MUTED = "#475569";
    static final String C_NAV = "#0B1120";
    static final String C_NAV_TEXT = "#F8FAFC";
    static final String C_ORANGE = "#EA580C";
    static final String C_TEAL = "#0D9488";

    // ── 8 Seat Classes ────────────────────────────────────────
    static final String[][] SC = {
            { "1A", "AC First Class", "4.0", "Luxury 4-berth private cabins", "#7C3AED", "Max Privacy" },
            { "2A", "AC 2-Tier", "2.5", "4-berth AC compartment, curtains", "#1D4ED8", "Premium" },
            { "3A", "AC 3-Tier", "1.8", "6-berth AC sleeper, most popular", "#0369A1", "Best Value" },
            { "3E", "AC 3-Tier Eco", "1.5", "Economy AC 6-berth sleeper", "#0891B2", "Budget AC" },
            { "SL", "Sleeper Class", "1.0", "Non-AC 8-berth sleeper", "#059669", "Economy" },
            { "CC", "AC Chair Car", "1.6", "Reclining AC seats for day trains", "#9333EA", "Day Train" },
            { "2S", "Second Sitting", "0.6", "Non-AC bench seats", "#D97706", "Short Trip" },
            { "GN", "General", "0.4", "Unreserved open seating", "#DC2626", "No Reserve" }
    };

    // ── Data ─────────────────────────────────────────────────
    static List<User> USERS = new ArrayList<>();
    static List<Train> TRAINS = new ArrayList<>();
    static List<Booking> BOOKINGS = new ArrayList<>();
    static User CURRENT_USER = null;
    static int PNR_SEQ = 2001000;
    static Stage PRIMARY_STAGE;
    static List<String> CUSTOM_STATIONS = new ArrayList<>();

    // ── Models ───────────────────────────────────────────────
    static class User {
        int id;
        String username, password, email, phone, fullName, dob, gender;
        boolean isAdmin;

        User(String u, String p, String e, String ph, String fn, boolean a) {
            username = u;
            password = p;
            email = e;
            phone = ph;
            fullName = fn;
            isAdmin = a;
            dob = "01-01-1990";
            gender = "Male";
            id = 0;
        }
    }

    static class Train {
        String trainNo, trainName, source, destination, departure, arrival, days, trainType;
        int totalSeats, availableSeats;
        double baseFare;

        Train(String no, String nm, String src, String dst, String dep, String arr, String days, String type, int seats,
                double fare) {
            trainNo = no;
            trainName = nm;
            source = src;
            destination = dst;
            departure = dep;
            arrival = arr;
            this.days = days;
            trainType = type;
            totalSeats = seats;
            availableSeats = seats;
            baseFare = fare;
        }
    }

    static class Booking {
        String pnr, username, trainNo, trainName, source, destination,
                journeyDate, seatClass, status, bookingDateTime, paymentMode, quota;
        List<Passenger> passengers;
        double totalFare;

        Booking(String p, String u, String tn, String tnm, String src, String dst,
                String jd, String sc, String q, List<Passenger> pass, double fare, String pm) {
            pnr = p;
            username = u;
            trainNo = tn;
            trainName = tnm;
            source = src;
            destination = dst;
            journeyDate = jd;
            seatClass = sc;
            quota = q;
            passengers = pass;
            totalFare = fare;
            paymentMode = pm;
            status = "CNF";
            bookingDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        }
    }

    static class Passenger {
        String name, gender, berthPref, nationality, seat;
        int age;

        Passenger(String n, int a, String g, String bp, String nat, String s) {
            name = n;
            age = a;
            gender = g;
            berthPref = bp;
            nationality = nat;
            seat = s;
        }
    }

    // ── All India Stations (200+) ───────────────────────────
    static final String[] ALL_INDIA_STATIONS = {
            // Tamil Nadu
            "Chennai", "Madurai", "Coimbatore", "Tiruchirappalli", "Salem", "Tirunelveli",
            "Erode", "Vellore", "Tirupur", "Tuticorin", "Kanchipuram", "Nagapattinam",
            "Kumbakonam", "Thanjavur", "Rameswaram", "Kanyakumari", "Cuddalore", "Karaikudi",
            "Pollachi", "Dindigul", "Nagercoil", "Chidambaram", "Villupuram", "Pudukkottai",
            // Kerala
            "Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", "Kollam", "Palakkad",
            "Alappuzha", "Kottayam", "Kannur", "Kasaragod", "Malappuram", "Manjeri",
            "Thalassery", "Vatakara", "Payyanur", "Shoranur", "Chalakudy", "Thrippunithura",
            // Karnataka
            "Bangalore", "Mysuru", "Hubli", "Mangalore", "Belagavi", "Davangere", "Bellary",
            "Shimoga", "Tumkur", "Bidar", "Kolar", "Mandya", "Hassan", "Chitradurga",
            "Raichur", "Dharwad", "Gadag", "Bagalkot", "Bijapur", "Udupi", "Hampi",
            // Andhra Pradesh & Telangana
            "Hyderabad", "Visakhapatnam", "Vijayawada", "Tirupati", "Guntur", "Nellore",
            "Kurnool", "Rajahmundry", "Warangal", "Nizamabad", "Karimnagar", "Khammam",
            "Ongole", "Eluru", "Bhimavaram", "Machilipatnam", "Kakinada", "Anakapalle",
            "Nalgonda", "Adilabad", "Mahbubnagar", "Srikakulam",
            // Maharashtra
            "Mumbai", "Pune", "Nagpur", "Nashik", "Aurangabad", "Solapur", "Amravati",
            "Kolhapur", "Nanded", "Latur", "Chandrapur", "Jalgaon", "Dhule", "Sangli",
            "Akola", "Ahmednagar", "Osmanabad", "Satara", "Ratnagiri", "Shirdi", "Alibag",
            "Thane", "Kalyan", "Panvel", "Vasai", "Dadar", "Lonavala", "Khandala",
            // Gujarat
            "Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar", "Jamnagar", "Junagadh",
            "Gandhinagar", "Anand", "Nadiad", "Morbi", "Mehsana", "Patan", "Bharuch",
            "Navsari", "Valsad", "Vapi", "Veraval", "Porbandar", "Dwarka", "Somnath",
            // Rajasthan
            "Jaipur", "Jodhpur", "Udaipur", "Ajmer", "Kota", "Bikaner", "Alwar", "Bharatpur",
            "Sikar", "Pali", "Tonk", "Barmer", "Jaisalmer", "Chittorgarh", "Sawai Madhopur",
            "Bundi", "Jhunjhunu", "Hanumangarh", "Sri Ganganagar", "Nagaur",
            // Delhi & NCR
            "New Delhi", "Delhi", "Gurgaon", "Noida", "Faridabad", "Ghaziabad",
            "New Delhi Cantt", "Hazrat Nizamuddin", "Sarai Rohilla",
            // Uttar Pradesh
            "Lucknow", "Varanasi", "Agra", "Kanpur", "Prayagraj", "Meerut", "Bareilly",
            "Aligarh", "Ghaziabad", "Moradabad", "Saharanpur", "Gorakhpur", "Firozabad",
            "Mathura", "Vrindavan", "Hapur", "Muzaffarnagar", "Rampur", "Budaun",
            "Jhansi", "Mirzapur", "Jaunpur", "Azamgarh", "Banda", "Ballia",
            // Bihar
            "Patna", "Gaya", "Muzaffarpur", "Bhagalpur", "Darbhanga", "Hajipur", "Bihar Sharif",
            "Purnia", "Munger", "Begusarai", "Katihar", "Samastipur", "Arrah", "Buxar",
            "Sitamarhi", "Motihari", "Chapra", "Siwan", "Sasaram", "Bettiah",
            // West Bengal
            "Kolkata", "Howrah", "Durgapur", "Asansol", "Siliguri", "Darjeeling", "Kharagpur",
            "Haldia", "Malda", "Bardhaman", "Cooch Behar", "Jalpaiguri", "Bankura", "Bolpur",
            "Krishnanagar", "Nabadwip", "Suri", "Purulia", "Midnapore", "Barrackpore",
            // Odisha
            "Bhubaneswar", "Cuttack", "Rourkela", "Berhampur", "Sambalpur", "Balasore",
            "Jharsuguda", "Puri", "Bhubaneswar", "Angul", "Kendrapara", "Jajpur",
            // Madhya Pradesh
            "Bhopal", "Indore", "Jabalpur", "Gwalior", "Ujjain", "Sagar", "Dewas",
            "Satna", "Ratlam", "Morena", "Bhind", "Chhindwara", "Vidisha", "Hoshangabad",
            "Rewa", "Mandla", "Damoh", "Tikamgarh", "Khargone", "Balaghat",
            // Jharkhand & Chhattisgarh
            "Ranchi", "Jamshedpur", "Dhanbad", "Bokaro", "Raipur", "Bilaspur", "Durg",
            "Korba", "Raigarh", "Jagdalpur", "Ambikapur", "Bhilai",
            // North East
            "Guwahati", "Dibrugarh", "Jorhat", "Silchar", "Tezpur", "Dispur",
            "Shillong", "Imphal", "Agartala", "Aizawl", "Kohima", "Itanagar",
            // Punjab & Haryana
            "Amritsar", "Ludhiana", "Jalandhar", "Chandigarh", "Patiala", "Bathinda",
            "Gurdaspur", "Firozpur", "Ambala", "Hisar", "Rohtak", "Karnal", "Panipat",
            "Sonipat", "Yamunanagar", "Kurukshetra", "Sirsa", "Bhiwani", "Rewari",
            // Himachal Pradesh & J&K
            "Shimla", "Solan", "Dharamsala", "Mandi", "Srinagar", "Jammu", "Udhampur",
            "Pathankot", "Kangra", "Una",
            // Uttarakhand
            "Dehradun", "Haridwar", "Rishikesh", "Roorkee", "Haldwani", "Nainital",
            // Goa
            "Panaji", "Margao", "Vasco da Gama"
    };

    // ── Seed Data — 50+ Trains Across India ──────────────────
    static void seedData() {
        USERS.add(new User("admin", "admin123", "admin@rail.in", "9999999999", "Administrator", true));
        USERS.add(new User("user1", "pass123", "aravind@mail.com", "9876543210", "Aravind Kumar", false));
        USERS.add(new User("sanjay", "sanjay123", "sanjay@mail.com", "9876543211", "Sanjay Kumar R", false));
        USERS.add(new User("dheer", "dheer123", "dheer@mail.com", "9876543212", "Dheeralingam M", false));

        // ── South India ──
        TRAINS.add(new Train("12001", "Shatabdi Express", "Chennai", "New Delhi", "06:00", "22:30", "Mon-Sun",
                "Superfast", 500, 1850));
        TRAINS.add(new Train("12621", "Tamil Nadu Express", "Chennai", "New Delhi", "22:00", "04:30", "Mon-Sun",
                "Express", 800, 850));
        TRAINS.add(new Train("12655", "Navjeevan Express", "Chennai", "Ahmedabad", "08:30", "14:00", "Mon,Wed,Fri",
                "Express", 600, 1200));
        TRAINS.add(
                new Train("16101", "Boat Mail", "Chennai", "Rameswaram", "21:30", "05:15", "Daily", "Mail", 400, 250));
        TRAINS.add(new Train("12673", "Cheran Express", "Chennai", "Coimbatore", "23:00", "04:15", "Daily", "Express",
                300, 750));
        TRAINS.add(new Train("22671", "Mumbai Chennai Express", "Mumbai", "Chennai", "08:00", "20:00", "Mon,Thu,Sat",
                "Superfast", 600, 1400));
        TRAINS.add(new Train("12028", "Chennai Rajdhani", "Chennai", "New Delhi", "21:00", "09:30", "Mon,Wed,Fri",
                "Rajdhani", 200, 3200));
        TRAINS.add(new Train("16723", "Anantapuri Express", "Thiruvananthapuram", "Chennai", "15:30", "05:00", "Daily",
                "Express", 700, 450));
        TRAINS.add(new Train("12637", "Pandian Express", "Chennai", "Madurai", "21:30", "04:30", "Daily", "Express",
                600, 320));
        TRAINS.add(new Train("12696", "Intercity Express", "Chennai", "Coimbatore", "06:20", "11:45", "Daily",
                "Intercity", 350, 480));
        TRAINS.add(new Train("11041", "Mumbai Express", "Mumbai", "Chennai", "09:00", "21:30", "Tue,Thu,Sat", "Express",
                500, 1100));
        TRAINS.add(new Train("16381", "Kanyakumari Express", "Mumbai", "Kanyakumari", "11:00", "07:30", "Mon,Wed",
                "Express", 450, 1350));
        TRAINS.add(new Train("22625", "Double Decker", "Chennai", "Bangalore", "06:00", "11:00", "Daily", "Express",
                400, 600));
        TRAINS.add(new Train("12163", "Dadar Express", "Chennai", "Mumbai", "21:15", "23:30", "Fri,Sun", "Express", 550,
                1300));
        TRAINS.add(new Train("12657", "Chennai Mail", "Chennai", "Bangalore", "22:30", "05:30", "Daily", "Mail", 650,
                380));
        TRAINS.add(new Train("16526", "Island Express", "Bangalore", "Kanyakumari", "20:45", "10:00", "Daily",
                "Express", 500, 900));
        TRAINS.add(new Train("22152", "Kochuveli Superfast", "Thiruvananthapuram", "Pune", "11:00", "23:00", "Tue,Sat",
                "Superfast", 480, 1600));
        TRAINS.add(new Train("16301", "Venad Express", "Thiruvananthapuram", "Shoranur", "19:30", "01:45", "Daily",
                "Express", 400, 280));
        TRAINS.add(new Train("12076", "Jan Shatabdi", "Coimbatore", "Chennai", "05:45", "11:15", "Daily",
                "Jan Shatabdi", 250, 520));
        TRAINS.add(new Train("12082", "Jan Shatabdi", "Chennai", "Coimbatore", "14:55", "20:15", "Daily",
                "Jan Shatabdi", 250, 520));
        // ── North India ──
        TRAINS.add(new Train("12301", "Howrah Rajdhani", "Howrah", "New Delhi", "14:05", "09:55", "Daily", "Rajdhani",
                250, 2800));
        TRAINS.add(new Train("12302", "New Delhi Howrah Rajdhani", "New Delhi", "Howrah", "17:00", "12:50", "Daily",
                "Rajdhani", 250, 2800));
        TRAINS.add(new Train("12951", "Mumbai Rajdhani", "Mumbai", "New Delhi", "17:40", "08:35", "Daily", "Rajdhani",
                240, 2600));
        TRAINS.add(new Train("12309", "Rajendra Nagar Rajdhani", "Patna", "New Delhi", "19:30", "08:40", "Daily",
                "Rajdhani", 220, 2400));
        TRAINS.add(new Train("22221", "Mumbai CSMT Rajdhani", "Mumbai", "New Delhi", "23:00", "17:00", "Daily",
                "Rajdhani", 230, 2700));
        TRAINS.add(new Train("12003", "Lucknow Shatabdi", "New Delhi", "Lucknow", "06:10", "12:30", "Mon-Sat",
                "Shatabdi", 180, 1100));
        TRAINS.add(new Train("12017", "Dehradun Shatabdi", "New Delhi", "Dehradun", "06:45", "11:59", "Daily",
                "Shatabdi", 175, 1050));
        TRAINS.add(new Train("12031", "Amritsar Shatabdi", "New Delhi", "Amritsar", "07:20", "13:15", "Daily",
                "Shatabdi", 170, 980));
        TRAINS.add(new Train("12559", "Shiv Ganga Express", "Varanasi", "New Delhi", "21:00", "08:30", "Daily",
                "Superfast", 600, 750));
        TRAINS.add(new Train("12165", "Ltt Sachkhand Express", "Mumbai", "Amritsar", "21:40", "23:45", "Tue,Fri",
                "Express", 500, 1450));
        // ── East India ──
        TRAINS.add(new Train("12314", "Sealdah Rajdhani", "Sealdah", "New Delhi", "16:55", "10:05", "Daily", "Rajdhani",
                240, 2750));
        TRAINS.add(new Train("13009", "Doon Express", "Howrah", "Dehradun", "23:55", "05:05", "Daily", "Express", 600,
                900));
        TRAINS.add(new Train("12843", "Puri Express", "Puri", "New Delhi", "22:30", "16:30", "Tue,Thu,Sat", "Express",
                520, 1300));
        TRAINS.add(new Train("18477", "Utkal Express", "Puri", "Haridwar", "14:25", "04:30", "Daily", "Express", 480,
                1200));
        TRAINS.add(new Train("22811", "New Delhi Bhubaneswar Rajdhani", "New Delhi", "Bhubaneswar", "20:35", "20:35",
                "Daily", "Rajdhani", 230, 2600));
        // ── West India ──
        TRAINS.add(new Train("19019", "Saurashtra Mail", "Mumbai", "Viramgam", "16:55", "08:35", "Daily", "Mail", 600,
                650));
        TRAINS.add(new Train("12009", "Shatabdi Express", "Mumbai", "Ahmedabad", "06:25", "13:10", "Daily", "Shatabdi",
                175, 1200));
        TRAINS.add(new Train("12915", "Ashram Express", "Ahmedabad", "New Delhi", "15:40", "05:55", "Daily",
                "Superfast", 600, 800));
        TRAINS.add(new Train("16209", "Ajmer Express", "Mumbai", "Ajmer", "19:10", "12:05", "Daily", "Express", 500,
                1100));
        TRAINS.add(new Train("12955", "Jaipur Superfast", "Mumbai", "Jaipur", "18:00", "10:00", "Daily", "Superfast",
                520, 1350));
        // ── Cross-Country ──
        TRAINS.add(new Train("12627", "Karnataka Express", "Bangalore", "New Delhi", "20:00", "05:30", "Daily",
                "Superfast", 700, 1650));
        TRAINS.add(new Train("11301", "Udyan Express", "Mumbai", "Bangalore", "08:05", "05:00", "Daily", "Express", 650,
                1050));
        TRAINS.add(new Train("12649", "Sampark Kranti", "Bangalore", "Hazrat Nizamuddin", "22:30", "17:35",
                "Mon,Wed,Sat", "Superfast", 500, 1550));
        TRAINS.add(new Train("12977", "Marusagar Express", "Jaipur", "Ernakulam", "23:30", "08:30", "Tue,Sat",
                "Express", 550, 2100));
        TRAINS.add(new Train("16317", "Himsagar Express", "Jammu", "Kanyakumari", "21:55", "13:30", "Mon", "Express",
                420, 3500));
        TRAINS.add(new Train("15906", "Vivek Express", "Dibrugarh", "Kanyakumari", "22:25", "11:15", "Mon", "Express",
                400, 3800));
        TRAINS.add(new Train("12641", "Thirukkural Express", "Chennai", "New Delhi", "22:30", "10:30", "Wed,Sat",
                "Express", 580, 870));
        TRAINS.add(new Train("12431", "Trivandrum Rajdhani", "Thiruvananthapuram", "New Delhi", "19:00", "14:25",
                "Mon,Wed,Fri", "Rajdhani", 210, 3100));
        TRAINS.add(new Train("16533", "Bgm Ypr Express", "Belgaum", "Bangalore", "05:30", "11:45", "Daily", "Express",
                350, 480));
        TRAINS.add(new Train("12286", "Duronto Express", "Secunderabad", "Hazrat Nizamuddin", "18:30", "13:15",
                "Mon,Thu", "Duronto", 280, 2200));

        // ── Pre-seed some bookings for admin analytics ──
        seedSampleBookings();
    }

    static void seedSampleBookings() {
        // Generate realistic booking history for admin charts
        String[] users2 = { "user1", "sanjay", "dheer" };
        String[] months = { "01-2025", "02-2025", "03-2025", "04-2025", "05-2025", "06-2025",
                "07-2025", "08-2025", "09-2025", "10-2025", "11-2025", "12-2025" };
        String[] classes2 = { "1A - AC First Class", "2A - AC 2-Tier", "3A - AC 3-Tier", "SL - Sleeper Class",
                "CC - AC Chair Car" };
        double[] classMults2 = { 4.0, 2.5, 1.8, 1.0, 1.6 };
        String[] payModes = { "UPI", "Card", "NetBanking", "Wallet" };
        java.util.Random rng = new java.util.Random(42); // fixed seed for consistent demo data
        int bIdx = 0;
        for (int mi = 0; mi < 12; mi++) {
            int bookCount = 8 + rng.nextInt(15); // 8-22 bookings per month
            if (mi == 4 || mi == 5 || mi == 9 || mi == 10)
                bookCount += 10; // peak months
            for (int bi = 0; bi < bookCount; bi++) {
                Train t = TRAINS.get(rng.nextInt(Math.min(TRAINS.size(), 20)));
                int clsIdx = rng.nextInt(classes2.length);
                String pnr = "PNR" + String.format("%07d", 2001000 + bIdx++);
                String uname = users2[rng.nextInt(users2.length)];
                int paxN = 1 + rng.nextInt(3);
                double fare = t.baseFare * classMults2[clsIdx] * paxN + 47.12 * paxN;
                String day = String.format("%02d", (1 + rng.nextInt(28)));
                String mon = months[mi];
                String jDate = day + "-" + mon;
                List<Passenger> pList = new ArrayList<>();
                for (int pi = 0; pi < paxN; pi++)
                    pList.add(new Passenger("Passenger " + (pi + 1), 25 + rng.nextInt(40),
                            rng.nextBoolean() ? "Male" : "Female", "Lower", "Indian", "S1-" + (pi + 1) + "LB"));
                Booking bk = new Booking(pnr, uname, t.trainNo, t.trainName, t.source, t.destination, jDate,
                        classes2[clsIdx], "General", pList, fare, payModes[rng.nextInt(payModes.length)]);
                if (rng.nextInt(10) < 2)
                    bk.status = "CAN"; // 20% cancellation
                bk.bookingDateTime = day + "-" + mon.replace("-", "-20") + " " + String.format("%02d", rng.nextInt(23))
                        + ":" + String.format("%02d", rng.nextInt(60)) + ":00";
                BOOKINGS.add(bk);
            }
        }
    }

    public static void main(String[] a) {
        launch(a);
    }

    @Override
    public void start(Stage s) {
        PRIMARY_STAGE = s;
        seedData();
        s.setTitle("RailConnect — Train Ticket Booking");
        s.setMaximized(true);
        showLogin();
        s.show();
    }

    // ════════════════════════════════════════════════════════
    // LOGIN — Professional Image LEFT | Form RIGHT
    // ════════════════════════════════════════════════════════

    // ════════════════════════════════════════════════════════
    // LOGIN — Beautiful Image LEFT | Login Form RIGHT
    // Fix 1: Professional left image
    // Fix 3: No captcha
    // Fix 4: Separate User & Admin login tabs
    // ════════════════════════════════════════════════════════
    static void showLogin() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#0A1929;");

        // ── Top Nav ──
        HBox topNav = new HBox(0);
        topNav.setStyle("-fx-background-color:#051020;");
        topNav.setPadding(new Insets(0, 30, 0, 30));
        topNav.setAlignment(Pos.CENTER_LEFT);
        topNav.setPrefHeight(50);
        Label logoL = label("🚆  RailConnect", 17, true, "#FFFFFF");
        HBox navSp = new HBox();
        HBox.setHgrow(navSp, Priority.ALWAYS);
        for (String l : new String[] { "Home", "Trains", "About", "Contact", "Help" }) {
            Label nl = label(l, 12, false, "#90CAF9");
            nl.setPadding(new Insets(0, 14, 0, 14));
            nl.setStyle("-fx-cursor:hand;");
            topNav.getChildren().add(nl);
        }
        topNav.getChildren().add(0, logoL);
        topNav.getChildren().add(1, navSp);
        root.setTop(topNav);

        HBox main = new HBox(0);
        main.setStyle("-fx-background-color:#0A1929;");

        // ══════════════════════════════════════════════════════════
        // LEFT PANEL — Rich Indian Railway Scene (Pure JavaFX)
        // ══════════════════════════════════════════════════════════
        StackPane leftPanel = new StackPane();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        // ── BACKGROUND GRADIENT: Deep night sky ──
        Region nightBg = new Region();
        nightBg.setMaxWidth(Double.MAX_VALUE);
        nightBg.setMaxHeight(Double.MAX_VALUE);
        nightBg.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #000814 0%, #001233 35%, #023E8A 75%, #0077B6 100%);");

        // ── ALL CONTENT ──
        VBox leftContent = new VBox(0);
        leftContent.setAlignment(Pos.TOP_CENTER);
        leftContent.setMaxWidth(Double.MAX_VALUE);

        // ── SECTION 1: Header branding ──
        VBox headerSec = new VBox(12);
        headerSec.setAlignment(Pos.CENTER);
        headerSec.setPadding(new Insets(36, 50, 20, 50));

        // Stars
        HBox starsHBox = new HBox(0);
        starsHBox.setAlignment(Pos.CENTER);
        starsHBox.setPadding(new Insets(0, 0, 6, 0));
        String[] starArr = { "✦", "·", "★", "·", "✦", "·", "·", "★", "·", "✦", "·", "★", "·", "✦", "·", "·", "★", "·",
                "✦" };
        for (String s : starArr) {
            Label sl = new Label(s);
            sl.setStyle("-fx-text-fill:rgba(255,255,255,0.5); -fx-font-size:9px; -fx-padding:0 10 0 0;");
            starsHBox.getChildren().add(sl);
        }
        Label moonStar = label("🌙", 18, false, "#FFFFFF");
        moonStar.setPadding(new Insets(0, 0, 0, 10));
        starsHBox.getChildren().add(moonStar);

        // Brand
        Label brandL = label("🚆  RailConnect", 30, true, "#FFFFFF");
        brandL.setStyle("-fx-effect:dropshadow(gaussian,rgba(0,180,255,0.6),18,0,0,2);");
        Label tagL2 = label("India's National Train Booking System", 13, false, "#64B5F6");

        // Divider line
        Region divLine = new Region();
        divLine.setPrefHeight(2);
        divLine.setPrefWidth(200);
        divLine.setStyle(
                "-fx-background-color: linear-gradient(to right, transparent, #FFA500, transparent); -fx-background-radius:2;");
        HBox divBox = new HBox();
        divBox.setAlignment(Pos.CENTER);
        divBox.getChildren().add(divLine);

        // Headline
        VBox headlineBox = new VBox(0);
        headlineBox.setAlignment(Pos.CENTER);
        Label hl1 = label("Journey Across", 36, true, "#FFFFFF");
        Label hl2 = label("Incredible India", 36, true, "#FFA500");
        hl1.setStyle("-fx-effect:dropshadow(gaussian,rgba(255,255,255,0.2),8,0,0,1);");
        hl2.setStyle("-fx-effect:dropshadow(gaussian,rgba(255,165,0,0.5),12,0,0,2);");
        Label hl3 = label("Safe  ·  Fast  ·  Comfortable  ·  Affordable", 13, false, "#90CAF9");
        headlineBox.getChildren().addAll(hl1, hl2, vg(6), hl3);

        headerSec.getChildren().addAll(starsHBox, brandL, tagL2, divBox, vg(10), headlineBox);

        // ══════════════════════════════════════════════════
        // SECTION 2: Professional Train Poster Visual
        // ══════════════════════════════════════════════════
        VBox heroPoster = new VBox(0);
        heroPoster.setAlignment(Pos.CENTER);
        heroPoster.setMaxWidth(Double.MAX_VALUE);
        heroPoster.setPadding(new Insets(10, 32, 0, 32));

        // Poster card with glow border
        VBox posterCard = new VBox(0);
        posterCard.setStyle(
                "-fx-background-color:linear-gradient(to bottom right,#051830,#082040,#0A2850); -fx-background-radius:16; -fx-border-color:rgba(30,136,229,0.38); -fx-border-radius:16; -fx-border-width:1; -fx-effect:dropshadow(gaussian,rgba(0,100,200,0.55),28,0,0,8);");

        // Top route bar
        HBox routeBar = new HBox(0);
        routeBar.setAlignment(Pos.CENTER);
        routeBar.setStyle("-fx-background-color:#020E1C; -fx-background-radius:16 16 0 0; -fx-padding:14 22 14 22;");

        VBox srcV = new VBox(4);
        srcV.setAlignment(Pos.CENTER_LEFT);
        srcV.getChildren().addAll(label("CHENNAI", 22, true, "#FFFFFF"),
                label("MAS  —  Chennai Central", 11, false, "#64B5F6"), label("Dep:  22:00", 12, true, "#FFA500"));

        VBox arrowMid = new VBox(6);
        arrowMid.setAlignment(Pos.CENTER);
        HBox.setHgrow(arrowMid, Priority.ALWAYS);
        HBox dotLine = new HBox(5);
        dotLine.setAlignment(Pos.CENTER);
        for (int di = 0; di < 9; di++) {
            Region dd = new Region();
            dd.setPrefWidth(di == 4 ? 11 : 6);
            dd.setPrefHeight(di == 4 ? 11 : 6);
            dd.setStyle("-fx-background-color:" + (di == 4 ? "#FFA500" : "rgba(255,165,0,0.3)")
                    + "; -fx-background-radius:50;");
            dotLine.getChildren().add(dd);
        }
        arrowMid.getChildren().addAll(dotLine, label("►  30 Hrs  30 Min", 12, true, "#FFA500"));

        VBox dstV = new VBox(4);
        dstV.setAlignment(Pos.CENTER_RIGHT);
        dstV.getChildren().addAll(label("NEW DELHI", 22, true, "#FFFFFF"),
                label("NDLS  —  New Delhi", 11, false, "#64B5F6"), label("Arr:  04:30", 12, true, "#FFA500"));
        routeBar.getChildren().addAll(srcV, arrowMid, dstV);

        // Centre: night scene with large train icon
        VBox centreBox = new VBox(12);
        centreBox.setAlignment(Pos.CENTER);
        centreBox.setStyle("-fx-background-color:linear-gradient(to bottom,#030F1E,#020A16);");
        centreBox.setPadding(new Insets(20, 20, 14, 20));

        // Scene row
        HBox sceneRow = new HBox(0);
        sceneRow.setAlignment(Pos.CENTER);
        VBox leftIco = new VBox(5);
        leftIco.setAlignment(Pos.CENTER);
        leftIco.setPrefWidth(100);
        leftIco.getChildren().addAll(label("🌙", 24, false, "#FFF3B0"),
                label("Night Journey", 9, false, "rgba(255,255,255,0.4)"));
        VBox centreIco = new VBox(8);
        centreIco.setAlignment(Pos.CENTER);
        HBox.setHgrow(centreIco, Priority.ALWAYS);
        Label bigTrain = label("🚄", 76, false, "#FFFFFF");
        bigTrain.setStyle("-fx-effect:dropshadow(gaussian,rgba(30,136,229,0.75),22,0,0,0);");
        Label trainBadge = label("Tamil Nadu Express  ·  #12621", 13, true, "#90CAF9");
        trainBadge.setStyle("-fx-background-color:rgba(26,86,219,0.18); -fx-background-radius:6; -fx-padding:4 14;");
        centreIco.getChildren().addAll(bigTrain, trainBadge);
        VBox rightIco = new VBox(5);
        rightIco.setAlignment(Pos.CENTER);
        rightIco.setPrefWidth(100);
        rightIco.getChildren().addAll(label("🌅", 24, false, "#FFD54F"),
                label("On Schedule", 9, false, "rgba(255,255,255,0.4)"));
        sceneRow.getChildren().addAll(leftIco, centreIco, rightIco);

        // Seat class chips
        HBox chips = new HBox(7);
        chips.setAlignment(Pos.CENTER);
        for (String[] cc : new String[][] { { "1A", "#7C3AED" }, { "2A", "#1565C0" }, { "3A", "#0369A1" },
                { "SL", "#059669" }, { "CC", "#9333EA" }, { "2S", "#D97706" }, { "GN", "#DC2626" } }) {
            Label cl = label(cc[0], 11, true, cc[1]);
            cl.setStyle("-fx-background-color:" + cc[1] + "20; -fx-background-radius:5; -fx-border-color:" + cc[1]
                    + "50; -fx-border-radius:5; -fx-border-width:1; -fx-padding:4 10; -fx-text-fill:" + cc[1] + ";");
            chips.getChildren().add(cl);
        }
        centreBox.getChildren().addAll(sceneRow, chips);

        // Bottom info strip
        HBox infoStrip = new HBox(0);
        infoStrip.setAlignment(Pos.CENTER);
        infoStrip.setStyle("-fx-background-color:#020D1C; -fx-background-radius:0 0 16 16; -fx-padding:11 14 11 14;");
        for (String[] ji : new String[][] { { "📅", "Date", "Any Date" }, { "💺", "Classes", "8 Available" },
                { "🎫", "Booking", "Instant" }, { "🔒", "Payment", "Secure" } }) {
            VBox jv = new VBox(2);
            jv.setAlignment(Pos.CENTER);
            HBox.setHgrow(jv, Priority.ALWAYS);
            jv.setStyle("-fx-border-color:rgba(255,255,255,0.06); -fx-border-width:0 1 0 0;");
            jv.getChildren().addAll(label(ji[0] + "  " + ji[1], 11, true, "#E0E0E0"),
                    label(ji[2], 9, false, "#607D8B"));
            infoStrip.getChildren().add(jv);
        }
        posterCard.getChildren().addAll(routeBar, centreBox, infoStrip);
        heroPoster.getChildren().add(posterCard);

        // ══════════════════════════════════════════════════
        // SECTION 3: Feature Cards (2x3 grid)
        // ══════════════════════════════════════════════════
        VBox featuresSec = new VBox(10);
        featuresSec.setPadding(new Insets(16, 32, 0, 32));
        featuresSec.getChildren().add(label("Why Choose RailConnect?", 12, true, "rgba(255,255,255,0.5)"));

        GridPane featGrid = new GridPane();
        featGrid.setHgap(10);
        featGrid.setVgap(10);
        String[][] feats = {
                { "🎫", "Instant Booking", "Confirm in under 2 minutes", "#1A56DB" },
                { "💺", "All 8 Classes", "1A · 2A · 3A · SL · CC · GN", "#059669" },
                { "🛰", "Live Tracking", "Real-time GPS location", "#047481" },
                { "🔒", "100% Secure", "Bank-grade encryption", "#7C3AED" },
                { "❌", "Easy Cancel", "Instant refund process", "#C81E1E" },
                { "📱", "E-Ticket", "Paperless always accessible", "#9333EA" }
        };
        for (int i = 0; i < feats.length; i++) {
            final String[] f = feats[i];
            HBox fc2 = new HBox(10);
            fc2.setAlignment(Pos.CENTER_LEFT);
            fc2.setPadding(new Insets(9, 12, 9, 12));
            fc2.setStyle(
                    "-fx-background-color:rgba(255,255,255,0.05); -fx-background-radius:10; -fx-border-color:rgba(255,255,255,0.08); -fx-border-radius:10; -fx-border-width:1; -fx-cursor:hand;");
            fc2.setOnMouseEntered(ev -> fc2
                    .setStyle("-fx-background-color:" + f[3] + "20; -fx-background-radius:10; -fx-border-color:" + f[3]
                            + "50; -fx-border-radius:10; -fx-border-width:1; -fx-cursor:hand;"));
            fc2.setOnMouseExited(ev -> fc2.setStyle(
                    "-fx-background-color:rgba(255,255,255,0.05); -fx-background-radius:10; -fx-border-color:rgba(255,255,255,0.08); -fx-border-radius:10; -fx-border-width:1; -fx-cursor:hand;"));
            Label fi = label(f[0], 20, false, "#FFFFFF");
            fi.setStyle("-fx-background-color:" + f[3] + "30; -fx-background-radius:7; -fx-padding:5 7;");
            VBox ft = new VBox(2);
            ft.getChildren().addAll(label(f[1], 12, true, "#FFFFFF"), label(f[2], 10, false, "#78909C"));
            fc2.getChildren().addAll(fi, ft);
            featGrid.add(fc2, i % 2, i / 2);
        }
        featuresSec.getChildren().add(featGrid);

        // Stats Bar
        HBox statsBar = new HBox(0);
        statsBar.setAlignment(Pos.CENTER);
        statsBar.setStyle("-fx-background-color:rgba(0,0,0,0.52); -fx-padding:13 16 13 16;");
        for (String[] s : new String[][] { { "🚆", "12,000+", "Trains" }, { "🏙", "8,000+", "Stations" },
                { "👥", "25 Cr+", "Users" }, { "💺", "8", "Classes" }, { "⚡", "2 Min", "Booking" } }) {
            VBox sv = new VBox(4);
            sv.setAlignment(Pos.CENTER);
            HBox.setHgrow(sv, Priority.ALWAYS);
            sv.getChildren().addAll(label(s[0] + " " + s[1], 13, true, "#FFA500"), label(s[2], 10, false, "#64B5F6"));
            statsBar.getChildren().add(sv);
        }

        leftContent.getChildren().addAll(headerSec, heroPoster, featuresSec, vg(12), statsBar);
        leftPanel.getChildren().addAll(nightBg, leftContent);
        StackPane.setAlignment(leftContent, Pos.TOP_CENTER);
        // ══════════════════════════════════════════════════════════
        // RIGHT PANEL — Login Form
        // Two tabs: User Login | Admin Login
        // No captcha
        // ══════════════════════════════════════════════════════════
        VBox rightPanel = new VBox(0);
        rightPanel.setPrefWidth(470);
        rightPanel.setMinWidth(440);
        rightPanel.setStyle("-fx-background-color:#FFFFFF;");

        // ── Service mini-tabs (dark navy) ──
        HBox svcTabs = new HBox(0);
        svcTabs.setStyle("-fx-background-color:#0A1929;");
        for (String[] t : new String[][] { { "🎫", "Book Ticket" }, { "📋", "PNR Status" }, { "❌", "Cancel" },
                { "⏱", "History" } }) {
            VBox tb = new VBox(3);
            tb.setAlignment(Pos.CENTER);
            tb.setPadding(new Insets(8, 12, 8, 12));
            tb.setStyle("-fx-cursor:hand;");
            tb.getChildren().addAll(label(t[0], 15, false, "#FFFFFF"), label(t[1], 9, false, "#90CAF9"));
            HBox.setHgrow(tb, Priority.ALWAYS);
            svcTabs.getChildren().add(tb);
        }

        // ── User / Admin Tab Switcher ──
        String activeTabStyle = "-fx-background-color:#FFFFFF; -fx-border-color:" + C_PRIMARY
                + "; -fx-border-width:0 0 3 0; -fx-cursor:hand;";
        String inactiveTabStyle = "-fx-background-color:#F1F5F9; -fx-border-color:transparent; -fx-border-width:0 0 3 0; -fx-cursor:hand;";

        HBox loginTypeTabs = new HBox(0);
        loginTypeTabs.setStyle("-fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");

        Label userTabL = label("👤  User Login", 14, true, C_PRIMARY);
        userTabL.setPadding(new Insets(14, 0, 14, 0));
        userTabL.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(userTabL, Priority.ALWAYS);
        userTabL.setAlignment(Pos.CENTER);
        userTabL.setStyle(activeTabStyle);

        Label adminTabL = label("🔧  Admin Login", 14, false, C_MUTED);
        adminTabL.setPadding(new Insets(14, 0, 14, 0));
        adminTabL.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(adminTabL, Priority.ALWAYS);
        adminTabL.setAlignment(Pos.CENTER);
        adminTabL.setStyle(inactiveTabStyle);

        loginTypeTabs.getChildren().addAll(userTabL, adminTabL);

        // ── USER LOGIN FORM ──
        VBox userForm = new VBox(13);
        userForm.setPadding(new Insets(26, 44, 16, 44));

        Label uTitle = label("Member Login", 22, true, C_TEXT);
        Label uSub = label("Welcome back! Sign in to continue your journey.", 13, false, C_MUTED);

        TextField tfUser = lf("Enter your username");
        tfUser.setPrefHeight(48);
        tfUser.setMaxWidth(Double.MAX_VALUE);
        PasswordField pfPass = lp("Enter your password");
        pfPass.setPrefHeight(48);
        pfPass.setMaxWidth(Double.MAX_VALUE);

        HBox fRow = new HBox();
        HBox fsp2 = new HBox();
        HBox.setHgrow(fsp2, Priority.ALWAYS);
        Label fL = label("Forgot Password?", 11, false, C_PRIMARY);
        fL.setStyle("-fx-cursor:hand;");
        fRow.getChildren().addAll(fsp2, fL);

        Label uErrL = label("", 12, false, C_DANGER);
        uErrL.setWrapText(true);

        Button btnSignIn = pb("  SIGN IN  ", C_PRIMARY);
        btnSignIn.setMaxWidth(Double.MAX_VALUE);
        btnSignIn.setPrefHeight(48);

        HBox rRow2 = new HBox(6);
        rRow2.setAlignment(Pos.CENTER);
        Button btnReg2 = lb("Register Here");
        btnReg2.setOnAction(e -> showRegister());
        rRow2.getChildren().addAll(label("New to RailConnect?", 12, false, C_MUTED), btnReg2);

        HBox orRow = new HBox(10);
        orRow.setAlignment(Pos.CENTER);
        Region od1 = new Region();
        od1.setPrefHeight(1);
        od1.setStyle("-fx-background-color:" + C_BORDER + ";");
        HBox.setHgrow(od1, Priority.ALWAYS);
        Region od2 = new Region();
        od2.setPrefHeight(1);
        od2.setStyle("-fx-background-color:" + C_BORDER + ";");
        HBox.setHgrow(od2, Priority.ALWAYS);
        orRow.getChildren().addAll(od1, label("OR", 11, false, C_MUTED), od2);

        Button btnGuest2 = gb("🔍  Search Trains without Login");
        btnGuest2.setMaxWidth(Double.MAX_VALUE);

        btnSignIn.setOnAction(e -> {
            String u = tfUser.getText().trim(), p = pfPass.getText().trim();
            if (u.isBlank() || p.isBlank()) {
                uErrL.setText("⚠  Please enter username and password.");
                return;
            }
            // ── Try MySQL first ──
            try {
                java.sql.ResultSet rs = DatabaseManager.loginUser(u, p);
                if (rs != null && rs.next()) {
                    User found = new User(rs.getString("username"), rs.getString("password"),
                            rs.getString("email"), rs.getString("phone"), rs.getString("full_name"), false);
                    found.id = rs.getInt("id");
                    found.gender = rs.getString("gender");
                    found.dob = rs.getString("dob");
                    CURRENT_USER = found;
                    showHome();
                    return;
                }
            } catch (Exception ex) {
                System.out.println("DB login failed, using local: " + ex.getMessage());
            }
            // ── Fallback to in-memory ──
            User found = USERS.stream().filter(x -> x.username.equals(u) && x.password.equals(p) && !x.isAdmin)
                    .findFirst().orElse(null);
            if (found == null) {
                uErrL.setText("❌  Invalid username or password. Please try again.");
                return;
            }
            CURRENT_USER = found;
            showHome();
        });
        btnGuest2.setOnAction(e -> {
            CURRENT_USER = USERS.get(1);
            showSearchAll();
        });

        userForm.getChildren().addAll(
                uTitle, uSub, vg(4),
                label("Username", 12, true, C_SUBTEXT), tfUser,
                label("Password", 12, true, C_SUBTEXT), pfPass, fRow,
                uErrL, btnSignIn, rRow2, orRow, btnGuest2);

        // ── ADMIN LOGIN FORM ──
        VBox adminForm = new VBox(13);
        adminForm.setPadding(new Insets(26, 44, 16, 44));
        adminForm.setVisible(false);
        adminForm.setManaged(false);

        Label aTitle = label("Admin Portal", 22, true, C_TEXT);
        Label aSub = label("Authorized personnel only. All sessions are logged.", 13, false, C_MUTED);

        // Admin warning card
        HBox aWarn = new HBox(12);
        aWarn.setAlignment(Pos.CENTER_LEFT);
        aWarn.setPadding(new Insets(12, 16, 12, 16));
        aWarn.setStyle(
                "-fx-background-color:#FFF3E0; -fx-background-radius:8; -fx-border-color:#FFCC80; -fx-border-radius:8; -fx-border-width:1;");
        VBox awText = new VBox(3);
        awText.getChildren().addAll(label("🔐  Secure Administrator Access", 12, true, "#E65100"),
                label("Use your administrator credentials to login.", 11, false, "#F57C00"));
        aWarn.getChildren().addAll(awText);

        TextField tfAdmin = lf("Admin Username");
        tfAdmin.setPrefHeight(48);
        tfAdmin.setMaxWidth(Double.MAX_VALUE);
        PasswordField pfAdmin = lp("Admin Password");
        pfAdmin.setPrefHeight(48);
        pfAdmin.setMaxWidth(Double.MAX_VALUE);
        Label aErrL = label("", 12, false, C_DANGER);
        aErrL.setWrapText(true);

        Button btnAdminIn = new Button("  ADMIN SIGN IN  ");
        btnAdminIn.setMaxWidth(Double.MAX_VALUE);
        btnAdminIn.setPrefHeight(48);
        btnAdminIn.setStyle("-fx-background-color:" + C_ORANGE
                + "; -fx-text-fill:#FFFFFF; -fx-font-weight:bold; -fx-font-size:14px; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand;");
        btnAdminIn.setOnMouseEntered(e -> btnAdminIn.setStyle(
                "-fx-background-color:#B84500; -fx-text-fill:#FFFFFF; -fx-font-weight:bold; -fx-font-size:14px; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand;"));
        btnAdminIn.setOnMouseExited(e -> btnAdminIn.setStyle("-fx-background-color:" + C_ORANGE
                + "; -fx-text-fill:#FFFFFF; -fx-font-weight:bold; -fx-font-size:14px; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand;"));

        btnAdminIn.setOnAction(e -> {
            String u = tfAdmin.getText().trim(), p = pfAdmin.getText().trim();
            if (u.isBlank() || p.isBlank()) {
                aErrL.setText("⚠  Please enter admin credentials.");
                return;
            }
            // ── Try MySQL first ──
            try {
                java.sql.ResultSet rs = DatabaseManager.loginAdmin(u, p);
                if (rs != null && rs.next()) {
                    User found = new User(rs.getString("username"), rs.getString("password"),
                            rs.getString("email"), rs.getString("phone"), rs.getString("full_name"), true);
                    found.id = rs.getInt("id");
                    CURRENT_USER = found;
                    showAdminDashboard();
                    return;
                }
            } catch (Exception ex) {
                System.out.println("DB admin login failed, using local: " + ex.getMessage());
            }
            // ── Fallback to in-memory ──
            User found = USERS.stream().filter(x -> x.username.equals(u) && x.password.equals(p) && x.isAdmin)
                    .findFirst().orElse(null);
            if (found == null) {
                aErrL.setText("❌  Invalid admin credentials or account not admin.");
                return;
            }
            CURRENT_USER = found;
            showAdminDashboard();
        });

        adminForm.getChildren().addAll(
                aTitle, aSub, vg(4), aWarn,
                label("Admin Username", 12, true, C_SUBTEXT), tfAdmin,
                label("Admin Password", 12, true, C_SUBTEXT), pfAdmin,
                aErrL, btnAdminIn);

        // ── TAB SWITCHING ──
        userTabL.setOnMouseClicked(e -> {
            userTabL.setStyle(activeTabStyle);
            userTabL.setFont(Font.font("System", FontWeight.BOLD, 14));
            userTabL.setTextFill(Color.web(C_PRIMARY));
            adminTabL.setStyle(inactiveTabStyle);
            adminTabL.setFont(Font.font("System", FontWeight.NORMAL, 14));
            adminTabL.setTextFill(Color.web(C_MUTED));
            userForm.setVisible(true);
            userForm.setManaged(true);
            adminForm.setVisible(false);
            adminForm.setManaged(false);
        });
        adminTabL.setOnMouseClicked(e -> {
            adminTabL.setStyle(activeTabStyle);
            adminTabL.setFont(Font.font("System", FontWeight.BOLD, 14));
            adminTabL.setTextFill(Color.web(C_PRIMARY));
            userTabL.setStyle(inactiveTabStyle);
            userTabL.setFont(Font.font("System", FontWeight.NORMAL, 14));
            userTabL.setTextFill(Color.web(C_MUTED));
            adminForm.setVisible(true);
            adminForm.setManaged(true);
            userForm.setVisible(false);
            userForm.setManaged(false);
        });

        // ── INFO CARDS ──
        HBox iCards = new HBox(8);
        iCards.setPadding(new Insets(6, 44, 10, 44));
        for (String[] ic : new String[][] { { "📱", "Mobile App", "Download now" }, { "🛡", "Secure", "100% safe" },
                { "⚡", "Fast", "Book in 2 min" } }) {
            HBox c = new HBox(8);
            c.setAlignment(Pos.CENTER_LEFT);
            c.setPadding(new Insets(8, 10, 8, 10));
            c.setStyle("-fx-background-color:" + C_BG + "; -fx-background-radius:8; -fx-border-color:" + C_BORDER
                    + "; -fx-border-radius:8;");
            VBox cv = new VBox(2);
            cv.getChildren().addAll(label(ic[0] + " " + ic[1], 11, true, C_TEXT), label(ic[2], 10, false, C_MUTED));
            c.getChildren().add(cv);
            HBox.setHgrow(c, Priority.ALWAYS);
            iCards.getChildren().add(c);
        }

        Label hint2 = label("Demo: user1 / pass123   |   Admin: admin / admin123", 10, false, C_MUTED);
        hint2.setPadding(new Insets(2, 44, 10, 44));
        hint2.setWrapText(true);

        VBox ticker = new VBox(5);
        ticker.setPadding(new Insets(8, 20, 10, 20));
        ticker.setStyle("-fx-background-color:#FFFBEB; -fx-border-color:#FCD34D; -fx-border-width:1 0 0 0;");
        ticker.getChildren().addAll(
                label("📢  Notice", 11, true, C_ACCENT),
                label("• Tatkal booking opens at 10:00 AM, one day before journey", 10, false, C_SUBTEXT),
                label("• Senior citizens get 40% concession on Sleeper & AC classes", 10, false, C_SUBTEXT));

        rightPanel.getChildren().addAll(svcTabs, loginTypeTabs, userForm, adminForm, iCards, hint2, ticker);

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        main.getChildren().addAll(leftPanel, rightPanel);

        ScrollPane sp = new ScrollPane(main);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:#0A1929; -fx-background-color:#0A1929;");
        root.setCenter(sp);
        setScene(root);
    }

    // Helper: build a wheel StackPane
    static StackPane wheelStack(int size) {
        Label outer = new Label();
        outer.setPrefWidth(size);
        outer.setPrefHeight(size);
        outer.setStyle(
                "-fx-background-color:#424242; -fx-background-radius:50; -fx-border-color:#1A1A1A; -fx-border-radius:50; -fx-border-width:2;");
        Label inner = new Label();
        int is = size / 2;
        inner.setPrefWidth(is);
        inner.setPrefHeight(is);
        inner.setStyle("-fx-background-color:#757575; -fx-background-radius:50;");
        StackPane sp = new StackPane(outer, inner);
        sp.setPrefWidth(size);
        sp.setPrefHeight(size);
        return sp;
    }

    // ════════════════════════════════════════════════════════
    // REGISTER
    // ════════════════════════════════════════════════════════
    static void showRegister() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(guestNav());
        VBox form = new VBox(14);
        form.setMaxWidth(520);
        form.setPadding(new Insets(36, 40, 36, 40));
        form.setStyle("-fx-background-color:" + C_SURFACE + "; -fx-background-radius:12; -fx-border-color:" + C_BORDER
                + "; -fx-border-radius:12;");
        TextField tfFull = lf("Full Name as per ID");
        TextField tfUser = lf("Choose Username");
        TextField tfEmail = lf("Email Address");
        TextField tfPhone = lf("Mobile Number (10 digits)");
        PasswordField pfPass = lp("Create Password");
        PasswordField pfPass2 = lp("Confirm Password");
        HBox gRow = new HBox(12);
        ComboBox<String> cbGender = lc(
                FXCollections.observableArrayList("Male", "Female", "Other", "Prefer not to say"));
        cbGender.setPromptText("Gender");
        cbGender.setPrefWidth(160);
        cbGender.setPrefHeight(42);
        TextField tfDob = lf("Date of Birth (DD-MM-YYYY)");
        tfDob.setPrefWidth(200);
        gRow.getChildren().addAll(cbGender, tfDob);
        Label errLbl = label("", 12, false, C_DANGER);
        errLbl.setWrapText(true);
        Label okLbl = label("", 12, false, C_SUCCESS);
        Button btnReg = pb("CREATE ACCOUNT", C_PRIMARY);
        btnReg.setMaxWidth(Double.MAX_VALUE);
        btnReg.setPrefHeight(44);
        Button btnBack = gb("← Back to Login");
        btnBack.setMaxWidth(Double.MAX_VALUE);
        btnReg.setOnAction(e -> {
            errLbl.setText("");
            okLbl.setText("");
            if (tfFull.getText().isBlank() || tfUser.getText().isBlank() || tfEmail.getText().isBlank()
                    || tfPhone.getText().isBlank() || pfPass.getText().isBlank()) {
                errLbl.setText("⚠  All fields are required.");
                return;
            }
            if (!pfPass.getText().equals(pfPass2.getText())) {
                errLbl.setText("⚠  Passwords do not match.");
                return;
            }
            if (tfPhone.getText().trim().length() != 10) {
                errLbl.setText("⚠  Enter valid 10-digit phone.");
                return;
            }
            String uname = tfUser.getText().trim();
            // ── Check in DB first ──
            boolean exists = false;
            try {
                exists = DatabaseManager.usernameExists(uname);
            } catch (Exception ex) {
                exists = USERS.stream().anyMatch(x -> x.username.equals(uname));
            }
            if (exists) {
                errLbl.setText("⚠  Username already exists.");
                return;
            }
            String gender = cbGender.getValue() != null ? cbGender.getValue() : "Male";
            String dob = tfDob.getText().isBlank() ? "01-01-1995" : tfDob.getText();
            // ── Save to DB ──
            boolean saved = false;
            try {
                saved = DatabaseManager.registerUser(uname, pfPass.getText(), tfEmail.getText().trim(),
                        tfPhone.getText().trim(), tfFull.getText().trim(), gender, dob);
            } catch (Exception ex) {
                System.out.println("DB register failed: " + ex.getMessage());
            }
            // ── Also save in-memory as fallback ──
            User u = new User(uname, pfPass.getText(), tfEmail.getText().trim(), tfPhone.getText().trim(),
                    tfFull.getText().trim(), false);
            u.gender = gender;
            u.dob = dob;
            USERS.add(u);
            if (saved)
                okLbl.setText("✅  Account created in database! You can now login.");
            else
                okLbl.setText("✅  Account created (local)! You can now login.");
        });
        btnBack.setOnAction(e -> showLogin());
        form.getChildren().addAll(
                label("Create New Account", 24, true, C_TEXT),
                label("Fill in the details below to register", 13, false, C_MUTED), vg(4),
                label("Full Name", 12, true, C_SUBTEXT), tfFull,
                label("Username", 12, true, C_SUBTEXT), tfUser,
                label("Email", 12, true, C_SUBTEXT), tfEmail,
                label("Mobile Number", 12, true, C_SUBTEXT), tfPhone,
                label("Gender & Date of Birth", 12, true, C_SUBTEXT), gRow,
                label("Password", 12, true, C_SUBTEXT), pfPass,
                label("Confirm Password", 12, true, C_SUBTEXT), pfPass2,
                errLbl, okLbl, btnReg, btnBack);
        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        StackPane center = new StackPane(sp);
        center.setStyle("-fx-background-color:" + C_BG + "; -fx-padding:30;");
        root.setCenter(center);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // HOME
    // ════════════════════════════════════════════════════════
    static void showHome() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Home"));
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color:" + C_BG + ";");

        // Hero search
        VBox hero = new VBox(18);
        hero.setPadding(new Insets(30, 40, 30, 40));
        hero.setStyle("-fx-background-color: linear-gradient(to bottom right,#1A3A8F,#2563EB);");
        hero.setAlignment(Pos.CENTER);
        hero.getChildren().addAll(
                label("Book Train Tickets", 36, true, C_NAV_TEXT),
                label("Fast · Reliable · Affordable", 15, false, "#BFDBFE"));

        // Search card
        VBox searchCard = new VBox(16);
        searchCard.setMaxWidth(980);
        searchCard.setPadding(new Insets(22, 24, 22, 24));
        searchCard.setStyle("-fx-background-color:" + C_SURFACE
                + "; -fx-background-radius:12; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),20,0,0,4);");

        HBox sRow = new HBox(10);
        sRow.setAlignment(Pos.CENTER_LEFT);

        // FROM box — autocomplete
        VBox fromBox = new VBox(4);
        Label fromLbl = label("FROM", 10, true, C_MUTED);
        TextField[] fromHolder = new TextField[1];
        String[] fromVal = { null };
        VBox fromAc = makeAutoCompleteField("Type city / station…", getStations(),
                selected -> fromVal[0] = selected, fromHolder);
        fromHolder[0].setPrefWidth(200);
        fromHolder[0].setPrefHeight(48);
        fromBox.getChildren().addAll(fromLbl, fromAc);

        // TO box — autocomplete
        VBox toBox = new VBox(4);
        Label toLbl = label("TO", 10, true, C_MUTED);
        TextField[] toHolder = new TextField[1];
        String[] toVal = { null };
        VBox toAc = makeAutoCompleteField("Type city / station…", getStations(),
                selected -> toVal[0] = selected, toHolder);
        toHolder[0].setPrefWidth(200);
        toHolder[0].setPrefHeight(48);
        toBox.getChildren().addAll(toLbl, toAc);

        // Swap button
        Button swapBtn = new Button("⇄");
        swapBtn.setStyle("-fx-background-color:" + C_PRIMARY_L + "; -fx-text-fill:" + C_PRIMARY
                + "; -fx-font-size:18px; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:8 12; -fx-border-color:"
                + C_PRIMARY + "; -fx-border-radius:8; -fx-border-width:1;");
        swapBtn.setOnAction(e -> {
            String tmp = fromVal[0];
            fromVal[0] = toVal[0];
            toVal[0] = tmp;
            fromHolder[0].setText(fromVal[0] != null ? fromVal[0] : "");
            toHolder[0].setText(toVal[0] != null ? toVal[0] : "");
        });

        VBox dateBox = new VBox(4);
        Label dateLbl = label("DATE", 10, true, C_MUTED);
        DatePicker dp = new DatePicker(LocalDate.now().plusDays(1));
        dp.setPrefWidth(160);
        dp.setPrefHeight(48);
        styleDp(dp);
        dateBox.getChildren().addAll(dateLbl, dp);

        VBox classBox = new VBox(4);
        Label clsLbl = label("CLASS", 10, true, C_MUTED);
        ComboBox<String> cbClass = lc(FXCollections.observableArrayList(
                "All Classes", "1A - AC First Class", "2A - AC 2-Tier", "3A - AC 3-Tier",
                "3E - AC 3-Tier Economy", "SL - Sleeper Class", "CC - AC Chair Car", "2S - Second Sitting",
                "GN - General"));
        cbClass.setValue("All Classes");
        cbClass.setPrefWidth(180);
        cbClass.setPrefHeight(48);
        classBox.getChildren().addAll(clsLbl, cbClass);

        VBox quotaBox = new VBox(4);
        Label quotaLbl = label("QUOTA", 10, true, C_MUTED);
        ComboBox<String> cbQuota = lc(FXCollections.observableArrayList(
                "General", "Ladies", "Senior Citizen", "Tatkal", "Premium Tatkal", "Defence", "Divyaang"));
        cbQuota.setValue("General");
        cbQuota.setPrefWidth(150);
        cbQuota.setPrefHeight(48);
        quotaBox.getChildren().addAll(quotaLbl, cbQuota);

        Button btnSearch = pb("🔍  SEARCH TRAINS", C_PRIMARY);
        btnSearch.setPrefHeight(48);
        btnSearch.setPrefWidth(170);
        btnSearch.setOnAction(e -> {
            // Use typed text as fallback if autocomplete wasn't picked
            String from = fromVal[0] != null ? fromVal[0] : fromHolder[0].getText().trim();
            String to = toVal[0] != null ? toVal[0] : toHolder[0].getText().trim();
            String date = dp.getValue() != null ? dp.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
            if (from.isEmpty() || to.isEmpty()) {
                showAlert("⚠  Select From and To stations.");
                return;
            }
            if (from.equalsIgnoreCase(to)) {
                showAlert("⚠  From and To cannot be the same.");
                return;
            }
            showTrains(from, to, date, cbClass.getValue(), cbQuota.getValue());
        });

        VBox btnBox = new VBox(0);
        btnBox.setAlignment(Pos.BOTTOM_CENTER);
        btnBox.getChildren().add(btnSearch);
        sRow.getChildren().addAll(fromBox, swapBtn, toBox, dateBox, classBox, quotaBox, btnBox);

        HBox opts = new HBox(20);
        opts.setAlignment(Pos.CENTER_LEFT);
        CheckBox cbFlex = new CheckBox("Flexible with Date");
        cbFlex.setStyle("-fx-text-fill:" + C_SUBTEXT + "; -fx-font-size:12px;");
        CheckBox cbAvail = new CheckBox("Only trains with available berths");
        cbAvail.setStyle("-fx-text-fill:" + C_SUBTEXT + "; -fx-font-size:12px;");
        opts.getChildren().addAll(cbFlex, cbAvail);
        searchCard.getChildren().addAll(sRow, opts);
        hero.getChildren().add(searchCard);

        // Quick service bar
        HBox qBar = new HBox(0);
        qBar.setStyle(
                "-fx-background-color:" + C_SURFACE + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");
        for (String[] q : new String[][] { { "📋", "PNR Status" }, { "💺", "Seat Avail" }, { "🕐", "Train Info" },
                { "💰", "Refund" }, { "🚌", "Bus" }, { "🏨", "Tourism" }, { "🎟", "Pass" },
                { "📍", "Locate Train" } }) {
            VBox qv = new VBox(6);
            qv.setAlignment(Pos.CENTER);
            qv.setPadding(new Insets(14, 0, 14, 0));
            qv.setStyle("-fx-cursor:hand;");
            qv.setOnMouseEntered(ev -> qv.setStyle("-fx-background-color:" + C_BG + "; -fx-cursor:hand;"));
            qv.setOnMouseExited(ev -> qv.setStyle("-fx-cursor:hand;"));
            qv.getChildren().addAll(label(q[0], 22, false, C_TEXT), label(q[1], 11, false, C_MUTED));
            HBox.setHgrow(qv, Priority.ALWAYS);
            qBar.getChildren().add(qv);
        }

        // Popular routes
        VBox routeSec = new VBox(14);
        routeSec.setPadding(new Insets(24, 40, 10, 40));
        routeSec.setStyle("-fx-background-color:" + C_BG + ";");
        routeSec.getChildren().add(st("Popular Routes"));
        HBox routes = new HBox(12);
        for (String[] r : new String[][] { { "Chennai", "New Delhi", "2D 16H", "₹850" },
                { "Mumbai", "Chennai", "1D 12H", "₹1,400" }, { "Chennai", "Coimbatore", "5H", "₹480" },
                { "Chennai", "Madurai", "7H", "₹320" } }) {
            VBox rc = new VBox(10);
            rc.setPadding(new Insets(16, 18, 16, 18));
            rc.setPrefWidth(230);
            rc.setStyle("-fx-background-color:" + C_SURFACE + "; -fx-background-radius:10; -fx-border-color:" + C_BORDER
                    + "; -fx-border-radius:10; -fx-cursor:hand; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);");
            rc.setOnMouseEntered(ev -> rc
                    .setStyle("-fx-background-color:" + C_PRIMARY_L + "; -fx-background-radius:10; -fx-border-color:"
                            + C_PRIMARY + "; -fx-border-radius:10; -fx-cursor:hand;"));
            rc.setOnMouseExited(ev -> rc.setStyle("-fx-background-color:" + C_SURFACE
                    + "; -fx-background-radius:10; -fx-border-color:" + C_BORDER
                    + "; -fx-border-radius:10; -fx-cursor:hand; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);"));
            HBox rh = new HBox(8);
            rh.setAlignment(Pos.CENTER_LEFT);
            rh.getChildren().addAll(label("🚆", 18, false, C_PRIMARY), label(r[0] + " → " + r[1], 13, true, C_TEXT));
            rc.getChildren().addAll(rh, label("Duration: " + r[2], 11, false, C_MUTED),
                    label("From " + r[3], 14, true, C_ACCENT));
            rc.setOnMouseClicked(ev -> {
                fromVal[0] = r[0];
                fromHolder[0].setText(r[0]);
                toVal[0] = r[1];
                toHolder[0].setText(r[1]);
            });
            routes.getChildren().add(rc);
        }
        routeSec.getChildren().add(routes);

        // Services
        VBox svcSec = new VBox(14);
        svcSec.setPadding(new Insets(24, 40, 10, 40));
        svcSec.setStyle("-fx-background-color:" + C_BG + ";");
        svcSec.getChildren().add(st("Our Services"));
        HBox svcCards = new HBox(14);
        for (String[] s : new String[][] { { "🎫", "Book Ticket", "Search & book", "#1A56DB", "#EBF0FF" },
                { "📋", "PNR Status", "Check status", "#057A55", "#DEF7EC" },
                { "❌", "Cancel", "Easy cancel", "#C81E1E", "#FDE8E8" },
                { "🛰", "Live Tracking", "Real-time GPS", "#047481", "#ECFEFF" },
                { "💳", "Refund", "Track refund", "#7C3AED", "#F5F3FF" } }) {
            VBox sc = new VBox(10);
            sc.setAlignment(Pos.CENTER);
            sc.setPadding(new Insets(20, 16, 20, 16));
            sc.setPrefWidth(180);
            sc.setStyle("-fx-background-color:" + s[4] + "; -fx-background-radius:12; -fx-border-color:" + s[3]
                    + "33; -fx-border-radius:12; -fx-cursor:hand;");
            sc.setOnMouseEntered(
                    ev -> sc.setStyle("-fx-background-color:" + s[3] + "22; -fx-background-radius:12; -fx-border-color:"
                            + s[3] + "; -fx-border-radius:12; -fx-cursor:hand;"));
            sc.setOnMouseExited(
                    ev -> sc.setStyle("-fx-background-color:" + s[4] + "; -fx-background-radius:12; -fx-border-color:"
                            + s[3] + "33; -fx-border-radius:12; -fx-cursor:hand;"));
            sc.getChildren().addAll(label(s[0], 32, false, C_TEXT), label(s[1], 13, true, s[3]),
                    label(s[2], 11, false, C_MUTED));
            sc.setOnMouseClicked(ev -> {
                switch (s[1]) {
                    case "Book Ticket":
                        showSearchAll();
                        break;
                    case "PNR Status":
                        showPNRStatus();
                        break;
                    case "Cancel":
                        showCancelTicket();
                        break;
                    case "Live Tracking":
                        showLiveTracking();
                        break;
                    default:
                        showHome();
                        break;
                }
            });
            svcCards.getChildren().add(sc);
        }
        svcSec.getChildren().add(svcCards);

        HBox infoBanner = new HBox(8);
        infoBanner.setAlignment(Pos.CENTER_LEFT);
        infoBanner.setStyle("-fx-background-color:#FFFBEB; -fx-border-color:#FCD34D; -fx-border-width:1 0 1 0;");
        infoBanner.setPadding(new Insets(10, 40, 10, 40));
        infoBanner.getChildren().addAll(label("📢", 16, false, C_ACCENT), label(
                "Tatkal opens 10:00 AM one day before  |  Book up to 120 days advance  |  Senior citizen concession available",
                12, false, C_SUBTEXT));

        content.getChildren().addAll(hero, qBar, routeSec, svcSec, infoBanner);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // SEARCH
    // ════════════════════════════════════════════════════════
    static void showTrains(String from, String to, String date, String cls, String quota) {
        List<Train> res = TRAINS.stream()
                .filter(t -> t.source.equalsIgnoreCase(from) && t.destination.equalsIgnoreCase(to))
                .collect(Collectors.toList());
        showTrainList(from, to, date, cls, quota, res);
    }

    static void showSearchAll() {
        showTrainList("All", "All", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                "All Classes", "General", new ArrayList<>(TRAINS));
    }

    static void showTrainList(String from, String to, String date, String selCls, String quota, List<Train> results) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Search Results"));

        // ── Filter bar (train name + source + destination search) ──
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(10, 30, 10, 30));
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar
                .setStyle("-fx-background-color:#EBF0FF; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");

        TextField tfTrainSearch = lf("🔍  Type train name to filter…");
        tfTrainSearch.setPrefWidth(260);
        tfTrainSearch.setPrefHeight(38);

        TextField[] srcHolder = new TextField[1];
        String[] srcVal = { from.equals("All") ? null : from };
        VBox srcAc = makeAutoCompleteField(from.equals("All") ? "Source station…" : from, getStations(),
                sel -> srcVal[0] = sel, srcHolder);
        srcHolder[0].setPrefWidth(190);
        srcHolder[0].setPrefHeight(38);
        if (!from.equals("All"))
            srcHolder[0].setText(from);

        TextField[] dstHolder = new TextField[1];
        String[] dstVal = { to.equals("All") ? null : to };
        VBox dstAc = makeAutoCompleteField(to.equals("All") ? "Destination station…" : to, getStations(),
                sel -> dstVal[0] = sel, dstHolder);
        dstHolder[0].setPrefWidth(190);
        dstHolder[0].setPrefHeight(38);
        if (!to.equals("All"))
            dstHolder[0].setText(to);

        Button btnReSearch = pb("🔍 Search", C_PRIMARY);
        btnReSearch.setPrefHeight(38);

        filterBar.getChildren().addAll(
                label("Train:", 12, true, C_SUBTEXT), tfTrainSearch,
                label("From:", 12, true, C_SUBTEXT), srcAc,
                label("To:", 12, true, C_SUBTEXT), dstAc,
                btnReSearch);

        HBox fb = new HBox(14);
        fb.setPadding(new Insets(10, 30, 10, 30));
        fb.setAlignment(Pos.CENTER_LEFT);
        fb.setStyle(
                "-fx-background-color:" + C_SURFACE + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");
        Label badge = label(results.size() + " Trains", 12, true, C_SURFACE);
        badge.setStyle("-fx-background-color:" + C_PRIMARY + "; -fx-background-radius:6; -fx-padding:3 10;");
        fb.getChildren().addAll(label("🚆  " + from + " → " + to, 15, true, C_TEXT),
                label("📅  " + date, 12, false, C_MUTED), badge);
        if (!selCls.equals("All Classes"))
            fb.getChildren().add(label("💺  " + selCls, 12, false, C_PRIMARY));

        VBox list = new VBox(12);
        list.setPadding(new Insets(16, 30, 30, 30));
        list.setStyle("-fx-background-color:" + C_BG + ";");
        if (results.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            empty.getChildren().addAll(label("🚫", 48, false, C_MUTED),
                    label("No trains found for this route.", 16, false, C_MUTED));
            list.getChildren().add(empty);
        }
        for (Train t : results) {
            VBox card = new VBox(0);
            card.setStyle(lc2());

            HBox th = new HBox(10);
            th.setPadding(new Insets(12, 18, 10, 18));
            th.setAlignment(Pos.CENTER_LEFT);
            th.setStyle("-fx-background-color:#F8FAFF; -fx-background-radius:10 10 0 0; -fx-border-color:" + C_BORDER
                    + "; -fx-border-width:0 0 1 0;");
            Label tNoL = label("#" + t.trainNo, 12, true, C_PRIMARY);
            tNoL.setStyle("-fx-background-color:" + C_PRIMARY_L + "; -fx-background-radius:4; -fx-padding:3 8;");
            String typeColor = t.trainType.equals("Rajdhani") ? C_ORANGE
                    : t.trainType.equals("Superfast") ? C_PRIMARY : C_SUCCESS;
            Label typeL = label(t.trainType, 11, true, C_SURFACE);
            typeL.setStyle("-fx-background-color:" + typeColor + "; -fx-background-radius:4; -fx-padding:3 8;");
            HBox tsp = new HBox();
            HBox.setHgrow(tsp, Priority.ALWAYS);
            Label avlL = label(t.availableSeats + " seats available", 12, false,
                    t.availableSeats > 50 ? C_SUCCESS : t.availableSeats > 10 ? C_ACCENT : C_DANGER);
            th.getChildren().addAll(tNoL, label(t.trainName, 15, true, C_TEXT), typeL, tsp, avlL);

            HBox rRow = new HBox(0);
            rRow.setPadding(new Insets(14, 18, 14, 18));
            rRow.setAlignment(Pos.CENTER);
            VBox dep = new VBox(3);
            dep.setAlignment(Pos.CENTER_LEFT);
            dep.getChildren().addAll(label(t.departure, 28, true, C_TEXT), label(t.source, 13, true, C_SUBTEXT));
            VBox mid = new VBox(4);
            mid.setAlignment(Pos.CENTER);
            mid.setPrefWidth(160);
            mid.getChildren().addAll(label("────────►", 14, false, C_BORDER),
                    label("Direct  |  " + t.days, 10, false, C_MUTED));
            VBox arr = new VBox(3);
            arr.setAlignment(Pos.CENTER_RIGHT);
            arr.getChildren().addAll(label(t.arrival, 28, true, C_TEXT), label(t.destination, 13, true, C_SUBTEXT));
            HBox.setHgrow(mid, Priority.ALWAYS);
            rRow.getChildren().addAll(dep, mid, arr);

            // Seat class chips
            HBox clsRow = new HBox(8);
            clsRow.setPadding(new Insets(0, 18, 14, 18));
            clsRow.setAlignment(Pos.CENTER_LEFT);
            clsRow.setStyle("-fx-border-color:" + C_BORDER + "; -fx-border-width:1 0 0 0;");
            for (String[] sc : SC) {
                double fare = t.baseFare * Double.parseDouble(sc[2]);
                String avColor = t.availableSeats > 50 ? "#057A55" : t.availableSeats > 10 ? "#D97706" : "#C81E1E";
                VBox cb = new VBox(3);
                cb.setAlignment(Pos.CENTER);
                cb.setPadding(new Insets(7, 10, 7, 10));
                cb.setStyle("-fx-background-color:" + sc[4] + "22; -fx-background-radius:6; -fx-border-color:" + sc[4]
                        + "66; -fx-border-radius:6; -fx-border-width:1; -fx-cursor:hand;");
                cb.getChildren().addAll(label(sc[0], 13, true, sc[4]),
                        label("₹" + String.format("%.0f", fare), 11, true, C_TEXT), label("AVBL", 9, false, avColor));
                cb.setOnMouseClicked(ev -> showBooking(t, date, sc[0] + " - " + sc[1], quota, 1));
                cb.setOnMouseEntered(ev -> cb
                        .setStyle("-fx-background-color:" + sc[4] + "44; -fx-background-radius:6; -fx-border-color:"
                                + sc[4] + "; -fx-border-radius:6; -fx-border-width:2; -fx-cursor:hand;"));
                cb.setOnMouseExited(ev -> cb
                        .setStyle("-fx-background-color:" + sc[4] + "22; -fx-background-radius:6; -fx-border-color:"
                                + sc[4] + "66; -fx-border-radius:6; -fx-border-width:1; -fx-cursor:hand;"));
                clsRow.getChildren().add(cb);
            }
            HBox csp2 = new HBox();
            HBox.setHgrow(csp2, Priority.ALWAYS);
            clsRow.getChildren().add(csp2);
            VBox fareInfo = new VBox(2);
            fareInfo.setAlignment(Pos.CENTER_RIGHT);
            fareInfo.getChildren().addAll(label("From ₹" + String.format("%.0f", t.baseFare), 16, true, C_ACCENT),
                    label("per person", 10, false, C_MUTED));
            Button btnBook = pb("BOOK NOW", C_PRIMARY);
            btnBook.setPrefWidth(110);
            btnBook.setPrefHeight(36);
            String defaultCls = selCls.equals("All Classes") ? "SL - Sleeper Class" : selCls;
            btnBook.setOnAction(e -> showBooking(t, date, defaultCls, quota, 1));
            clsRow.getChildren().addAll(fareInfo, new Label("  "), btnBook);
            card.getChildren().addAll(th, rRow, clsRow);
            list.getChildren().add(card);
        }

        // ── Wire up live train-name filter ──
        tfTrainSearch.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV.trim().toLowerCase();
            list.getChildren().clear();
            List<Train> filtered = results.stream()
                    .filter(t -> t.trainName.toLowerCase().contains(q) || t.trainNo.contains(q))
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) {
                VBox empty = new VBox(12);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(40));
                empty.getChildren().addAll(label("🚫", 36, false, C_MUTED),
                        label("No trains match '" + newV + "'.", 14, false, C_MUTED));
                list.getChildren().add(empty);
            } else {
                for (Train t2 : filtered) {
                    // rebuild simplified row
                    HBox mini = new HBox(14);
                    mini.setStyle(lc2());
                    mini.setPadding(new Insets(12, 18, 12, 18));
                    mini.setAlignment(Pos.CENTER_LEFT);
                    mini.getChildren().addAll(
                            label("#" + t2.trainNo, 12, true, C_PRIMARY),
                            label(t2.trainName, 14, true, C_TEXT),
                            label(t2.source + " → " + t2.destination, 12, false, C_SUBTEXT));
                    HBox sp3 = new HBox();
                    HBox.setHgrow(sp3, Priority.ALWAYS);
                    mini.getChildren().add(sp3);
                    String dc = selCls.equals("All Classes") ? "SL - Sleeper Class" : selCls;
                    Button bk = pb("BOOK", C_PRIMARY);
                    bk.setOnAction(ev -> showBooking(t2, date, dc, quota, 1));
                    mini.getChildren().add(bk);
                    list.getChildren().add(mini);
                }
            }
        });

        // ── Wire up re-search by source/dest ──
        btnReSearch.setOnAction(e -> {
            String newFrom = srcVal[0] != null ? srcVal[0] : srcHolder[0].getText().trim();
            String newTo = dstVal[0] != null ? dstVal[0] : dstHolder[0].getText().trim();
            String newDate = date;
            if (newFrom.isEmpty() || newTo.isEmpty()) {
                showAlert("⚠ Fill source and destination.");
                return;
            }
            showTrains(newFrom, newTo, newDate, selCls, quota);
        });

        ScrollPane sp = new ScrollPane(new VBox(filterBar, fb, list));
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // BOOKING FORM
    // ════════════════════════════════════════════════════════
    static void showBooking(Train train, String date, String preClass, String quota, int prePax) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Book Ticket"));

        // Progress steps
        HBox steps = new HBox(0);
        steps.setAlignment(Pos.CENTER);
        steps.setStyle(
                "-fx-background-color:" + C_SURFACE + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");
        for (int i = 0; i < 4; i++) {
            String[] stepNames = { "Journey Details", "Passenger Details", "Extras", "Payment" };
            HBox sb = new HBox(10);
            sb.setAlignment(Pos.CENTER);
            sb.setPadding(new Insets(14, 24, 14, 24));
            boolean active = i == 0;
            sb.setStyle(active ? "-fx-background-color:" + C_PRIMARY + "; -fx-border-color:" + C_PRIMARY
                    + "; -fx-border-width:0 0 3 0;" : "-fx-background-color:" + C_SURFACE + ";");
            Label numL = label((i + 1) + "", 12, true, active ? C_PRIMARY : C_MUTED);
            numL.setStyle("-fx-background-color:" + (active ? C_SURFACE : C_BORDER)
                    + "; -fx-background-radius:50; -fx-padding:2 7;");
            sb.getChildren().addAll(numL, label(stepNames[i], 13, active, active ? C_SURFACE : C_MUTED));
            steps.getChildren().add(sb);
        }

        VBox outer = new VBox(20);
        outer.setPadding(new Insets(20, 40, 40, 40));
        outer.setStyle("-fx-background-color:" + C_BG + ";");
        outer.getChildren().add(st("🎫  Book Your Ticket"));

        // Train summary
        HBox tSum = new HBox(16);
        tSum.setAlignment(Pos.CENTER_LEFT);
        tSum.setStyle(lc2());
        tSum.setPadding(new Insets(14, 18, 14, 18));
        VBox tInfo = new VBox(4);
        HBox.setHgrow(tInfo, Priority.ALWAYS);
        tInfo.getChildren().addAll(
                label(train.trainName + " (#" + train.trainNo + ")", 16, true, C_TEXT),
                label(train.source + "  →  " + train.destination, 13, false, C_SUBTEXT),
                label("Dep: " + train.departure + "  |  Arr: " + train.arrival + "  |  " + train.days, 12, false,
                        C_MUTED));
        VBox tDate = new VBox(3);
        tDate.setAlignment(Pos.CENTER_RIGHT);
        tDate.getChildren().addAll(label(date, 14, true, C_PRIMARY), label("Journey Date", 10, false, C_MUTED));
        tSum.getChildren().addAll(label("🚆", 30, false, C_PRIMARY), tInfo, tDate);
        outer.getChildren().add(tSum);

        HBox mainLayout = new HBox(20);
        mainLayout.setAlignment(Pos.TOP_LEFT);
        VBox formCol = new VBox(16);
        HBox.setHgrow(formCol, Priority.ALWAYS);

        // ── STEP 1: Seat Class ──
        VBox clsSec = new VBox(14);
        clsSec.setStyle(lc2());
        clsSec.setPadding(new Insets(18, 20, 18, 20));
        clsSec.getChildren().addAll(sh("1", "Select Seat Type & Quota"),
                label("Choose your class of travel and booking quota", 12, false, C_MUTED));

        // Quota row
        HBox quotaRow = new HBox(12);
        quotaRow.setAlignment(Pos.CENTER_LEFT);
        quotaRow.getChildren().add(label("Quota:", 13, true, C_SUBTEXT));
        ToggleGroup qGroup = new ToggleGroup();
        for (String q : new String[] { "General", "Ladies", "Senior Citizen", "Tatkal", "Premium Tatkal",
                "Divyaang" }) {
            RadioButton rb = new RadioButton(q);
            rb.setToggleGroup(qGroup);
            rb.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_SUBTEXT + ";");
            if (q.equals(quota))
                rb.setSelected(true);
            quotaRow.getChildren().add(rb);
        }
        clsSec.getChildren().add(quotaRow);
        clsSec.getChildren().add(label("─── Select Seat Class ───", 12, false, C_MUTED));

        String[] selClass = { preClass };
        double[] selMult = { 1.0 };
        for (String[] sc : SC) {
            if (selClass[0].startsWith(sc[0])) {
                selMult[0] = Double.parseDouble(sc[2]);
                break;
            }
        }

        GridPane clsGrid = new GridPane();
        clsGrid.setHgap(10);
        clsGrid.setVgap(10);
        Label totalFareLbl = label("Fare: ₹" + String.format("%.0f", train.baseFare * selMult[0]) + " per person", 16,
                true, C_PRIMARY);

        for (int i = 0; i < SC.length; i++) {
            String[] sc = SC[i];
            final int fi = i;
            double fare = train.baseFare * Double.parseDouble(sc[2]);
            boolean isSel = selClass[0].startsWith(sc[0]);
            VBox cb = new VBox(6);
            cb.setPadding(new Insets(12, 14, 12, 14));
            cb.setPrefWidth(220);
            cb.setStyle(isSel ? selCard(sc[4]) : normCard(sc[4]));
            HBox ctop = new HBox(8);
            ctop.setAlignment(Pos.CENTER_LEFT);
            Label codeL = label(sc[0], 15, true, sc[4]);
            codeL.setStyle("-fx-background-color:" + sc[4] + "22; -fx-background-radius:4; -fx-padding:3 8;");
            HBox csp3 = new HBox();
            HBox.setHgrow(csp3, Priority.ALWAYS);
            Label fareL = label("₹" + String.format("%.0f", fare), 14, true, C_ACCENT);
            ctop.getChildren().addAll(codeL, csp3, fareL);
            Label tagL = label(sc[5], 10, true, C_SURFACE);
            tagL.setStyle("-fx-background-color:" + sc[4] + "; -fx-background-radius:4; -fx-padding:2 6;");
            cb.getChildren().addAll(ctop, label(sc[1], 13, true, C_TEXT), label(sc[3], 10, false, C_MUTED), tagL);
            cb.setOnMouseClicked(ev -> {
                selClass[0] = sc[0] + " - " + sc[1];
                selMult[0] = Double.parseDouble(sc[2]);
                for (int j = 0; j < clsGrid.getChildren().size(); j++) {
                    Node n = clsGrid.getChildren().get(j);
                    if (n instanceof VBox)
                        ((VBox) n).setStyle(j == fi ? selCard(SC[j][4]) : normCard(SC[j][4]));
                }
                totalFareLbl.setText("Fare: ₹" + String.format("%.0f", train.baseFare * selMult[0]) + " per person");
            });
            cb.setOnMouseEntered(ev -> {
                if (!selClass[0].startsWith(sc[0]))
                    cb.setStyle("-fx-background-color:" + sc[4] + "11; -fx-background-radius:8; -fx-border-color:"
                            + sc[4] + "; -fx-border-radius:8; -fx-border-width:1; -fx-cursor:hand;");
            });
            cb.setOnMouseExited(ev -> {
                if (!selClass[0].startsWith(sc[0]))
                    cb.setStyle(normCard(sc[4]));
            });
            clsGrid.add(cb, i % 4, i / 4);
        }
        clsSec.getChildren().addAll(clsGrid, totalFareLbl);
        formCol.getChildren().add(clsSec);

        // ── STEP 2: Passengers ──
        VBox paxSec = new VBox(14);
        paxSec.setStyle(lc2());
        paxSec.setPadding(new Insets(18, 20, 18, 20));
        paxSec.getChildren().add(sh("2", "Passenger Details"));

        HBox paxCountRow = new HBox(12);
        paxCountRow.setAlignment(Pos.CENTER_LEFT);
        paxCountRow.getChildren().add(label("No. of Passengers:", 13, true, C_SUBTEXT));
        ComboBox<Integer> cbCount = lic(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));
        cbCount.setValue(prePax > 0 ? prePax : 1);
        cbCount.setPrefWidth(90);
        cbCount.setPrefHeight(38);
        paxCountRow.getChildren().addAll(cbCount, label("(Max 6 per booking)", 12, false, C_MUTED));
        paxSec.getChildren().add(paxCountRow);

        // Column headers — FIX: all values are Strings
        HBox pHdr = new HBox(0);
        pHdr.setPadding(new Insets(8, 0, 4, 0));
        pHdr.setStyle("-fx-background-color:" + C_BG + ";");
        for (String[] h : new String[][] { { "Name", "180" }, { "Age", "70" }, { "Gender", "110" },
                { "Berth Pref", "130" }, { "Nationality", "110" } }) {
            Label hl = label(h[0], 11, true, C_MUTED);
            hl.setPrefWidth(Integer.parseInt(h[1]));
            pHdr.getChildren().add(hl);
        }
        paxSec.getChildren().add(pHdr);

        VBox paxRows = new VBox(10);
        List<Object[]> paxWidgets = new ArrayList<>();
        String[] berths = { "No Preference", "Lower", "Middle", "Upper", "Side Lower", "Side Upper" };

        Runnable buildPax = () -> {
            paxRows.getChildren().clear();
            paxWidgets.clear();
            for (int i = 0; i < cbCount.getValue(); i++) {
                final int idx = i;
                HBox pr = new HBox(8);
                pr.setAlignment(Pos.CENTER_LEFT);
                pr.setStyle("-fx-background-color:" + C_SURFACE + "; -fx-background-radius:8; -fx-border-color:"
                        + C_BORDER + "; -fx-border-radius:8; -fx-padding:10 12;");
                TextField tfName = lf("Passenger " + (i + 1) + " Name");
                tfName.setPrefWidth(180);
                tfName.setPrefHeight(38);
                TextField tfAge = lf("Age");
                tfAge.setPrefWidth(70);
                tfAge.setPrefHeight(38);
                ComboBox<String> cbG = lc(FXCollections.observableArrayList("Male", "Female", "Transgender"));
                cbG.setPromptText("Gender");
                cbG.setPrefWidth(110);
                cbG.setPrefHeight(38);
                ComboBox<String> cbB = lc(FXCollections.observableArrayList(berths));
                cbB.setValue("No Preference");
                cbB.setPrefWidth(130);
                cbB.setPrefHeight(38);
                ComboBox<String> cbN = lc(FXCollections.observableArrayList("Indian", "Foreign"));
                cbN.setValue("Indian");
                cbN.setPrefWidth(110);
                cbN.setPrefHeight(38);
                pr.getChildren().addAll(tfName, tfAge, cbG, cbB, cbN);
                paxRows.getChildren().add(pr);
                paxWidgets.add(new Object[] { tfName, tfAge, cbG, cbB, cbN });
            }
            totalFareLbl.setText("Fare: ₹" + String.format("%.0f", train.baseFare * selMult[0] * cbCount.getValue())
                    + " (" + cbCount.getValue() + " pax)");
        };
        cbCount.setOnAction(e -> buildPax.run());
        buildPax.run();
        paxSec.getChildren().add(paxRows);
        formCol.getChildren().add(paxSec);

        // ── STEP 3: Seat Map Selection ──
        VBox seatMapSec = new VBox(14);
        seatMapSec.setStyle(lc2());
        seatMapSec.setPadding(new Insets(18, 20, 18, 20));
        seatMapSec.getChildren().addAll(sh("3", "Seat Map — Select Your Berth"),
                label("Realistic Indian Railways coach layout. Click an available berth to select.", 11, false,
                        C_MUTED));

        // ── Determine coach type based on selected class ──
        // SL/3A: 8 bays × (LB,MB,UB,SL,SU) = 72 berths per coach
        // 2A: 6 bays × (LB,UB,SL,SU) = 48 berths
        // 1A: 4 bays × (LB,UB) = 24 berths (2 per bay)
        // CC: 12 rows × 5 seats
        // GN/2S: 12 rows × 6 seats (unreserved)

        // Determine coach names and count based on class
        String clsCode = selClass[0].length() >= 2 ? selClass[0].substring(0, 2) : "SL";
        String[] coachNames;
        int baysPerCoach, berthsPerBay;
        String[] berthTypes;
        boolean isChairCar = clsCode.equals("CC") || clsCode.equals("2S") || clsCode.equals("GN");
        boolean is1A = clsCode.equals("1A");
        boolean is2A = clsCode.equals("2A");
        if (is1A) {
            coachNames = new String[] { "HA1", "HA2" };
            baysPerCoach = 4;
            berthTypes = new String[] { "LB", "UB", "SL", "SU" };
            berthsPerBay = 4;
        } else if (is2A) {
            coachNames = new String[] { "A1", "A2", "A3" };
            baysPerCoach = 6;
            berthTypes = new String[] { "LB", "UB", "SL", "SU" };
            berthsPerBay = 4;
        } else if (isChairCar) {
            coachNames = new String[] { "C1", "C2", "C3", "C4" };
            baysPerCoach = 12;
            berthTypes = new String[] { "W", "M", "A", "M", "W" };
            berthsPerBay = 5;
        } else {
            coachNames = new String[] { "S1", "S2", "S3", "S4", "S5", "S6" };
            baysPerCoach = 8;
            berthTypes = new String[] { "LB", "MB", "UB", "SL", "SU" };
            berthsPerBay = 5;
        }

        // Fixed seed random for deterministic booked seats per session
        java.util.Random seatRng = new java.util.Random(42);
        Set<String> bookedSeats = new HashSet<>();
        for (String cn : coachNames)
            for (int bay = 1; bay <= baysPerCoach; bay++)
                for (String bt : berthTypes)
                    if (seatRng.nextDouble() < 0.35)
                        bookedSeats.add(cn + "-" + bay + bt);
        // Also mark seats already in this booking session
        Set<String> selectedSeats = new HashSet<>();

        // Coach tab style
        String selCoachStyle = "-fx-background-color:" + C_PRIMARY
                + "; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:7 16; -fx-cursor:hand; -fx-font-weight:bold; -fx-font-size:12px;";
        String norCoachStyle = "-fx-background-color:#F1F5F9; -fx-text-fill:" + C_SUBTEXT
                + "; -fx-background-radius:6; -fx-padding:7 16; -fx-cursor:hand; -fx-font-size:12px; -fx-border-color:"
                + C_BORDER + "; -fx-border-radius:6; -fx-border-width:1;";

        // Stats bar
        HBox statsBar = new HBox(20);
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(10, 0, 4, 0));
        int totalSeatsMap = coachNames.length * baysPerCoach * berthsPerBay;
        int bookedCount = bookedSeats.size();
        int availCount = totalSeatsMap - bookedCount;
        Label[] statLabels = { label("Total: " + totalSeatsMap, 11, false, C_MUTED),
                label("Available: " + availCount, 11, true, C_SUCCESS),
                label("Booked: " + bookedCount, 11, true, C_DANGER), label("Selected: 0", 11, true, C_PRIMARY) };
        statsBar.getChildren().addAll(statLabels);

        // Seat grid container
        VBox seatGridContainer = new VBox(0);
        seatGridContainer.setStyle("-fx-background-color:#FAFBFF; -fx-padding:10;");
        final String[] currentCoach = { coachNames[0] };

        Runnable[] buildGrid = { null };
        buildGrid[0] = () -> {
            seatGridContainer.getChildren().clear();

            // Train silhouette header
            HBox trainHdr = new HBox(0);
            trainHdr.setAlignment(Pos.CENTER_LEFT);
            trainHdr.setPadding(new Insets(8, 10, 8, 10));
            trainHdr.setStyle("-fx-background-color:#1E3A5F; -fx-background-radius:8 8 0 0;");
            Region hdrSpacer = new Region();
            HBox.setHgrow(hdrSpacer, Priority.ALWAYS);
            trainHdr.getChildren().addAll(
                    label("🚂", 16, false, "#FFFFFF"),
                    label("  Coach: " + currentCoach[0] + "   |   Type: "
                            + (isChairCar ? "Chair Car" : is1A ? "1st AC" : is2A ? "2nd AC" : "Sleeper/3AC"), 12, true,
                            "#BFDBFE"),
                    hdrSpacer,
                    label("← Engine End", 10, false, "#6B9ED2"));
            seatGridContainer.getChildren().add(trainHdr);

            // Column headers
            HBox hdrRow = new HBox(6);
            hdrRow.setAlignment(Pos.CENTER);
            hdrRow.setPadding(new Insets(8, 10, 4, 10));
            hdrRow.setStyle("-fx-background-color:#EFF6FF; -fx-border-color:#BFDBFE; -fx-border-width:0 0 1 0;");
            Label bayHdr = label("Bay", 10, true, C_PRIMARY);
            bayHdr.setPrefWidth(40);
            bayHdr.setAlignment(Pos.CENTER);
            hdrRow.getChildren().add(bayHdr);

            if (!isChairCar) {
                // Left side label
                Label leftHdr = label("◄── LEFT BERTHS ──", 10, true, "#4B5563");
                leftHdr.setPrefWidth(180);
                leftHdr.setAlignment(Pos.CENTER);
                Label aisleHdr = label("AISLE", 10, true, "#9CA3AF");
                aisleHdr.setPrefWidth(30);
                aisleHdr.setAlignment(Pos.CENTER);
                Label rightHdr = label("── SIDE BERTHS ──►", 10, true, "#4B5563");
                rightHdr.setPrefWidth(110);
                rightHdr.setAlignment(Pos.CENTER);
                hdrRow.getChildren().addAll(leftHdr, aisleHdr, rightHdr);
            } else {
                Label leftHdr = label("◄── WINDOW ──── MIDDLE ──── AISLE ──── MIDDLE ──── WINDOW ──►", 10, true,
                        "#4B5563");
                hdrRow.getChildren().add(leftHdr);
            }
            seatGridContainer.getChildren().add(hdrRow);

            // Berth layout
            for (int bay = 1; bay <= baysPerCoach; bay++) {
                HBox bayRow = new HBox(6);
                bayRow.setAlignment(Pos.CENTER_LEFT);
                bayRow.setPadding(new Insets(5, 10, 5, 10));
                bayRow.setStyle("-fx-background-color:" + (bay % 2 == 0 ? "#F8FAFF" : "#FFFFFF")
                        + "; -fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;");

                // Bay number
                Label bayLbl = label(bay + "", 11, true, "#6B7280");
                bayLbl.setPrefWidth(40);
                bayLbl.setPrefHeight(38);
                bayLbl.setAlignment(Pos.CENTER);
                bayLbl.setStyle(
                        "-fx-background-color:#F1F5F9; -fx-background-radius:4; -fx-border-color:#E2E8F0; -fx-border-radius:4; -fx-border-width:1;");
                bayRow.getChildren().add(bayLbl);

                if (!isChairCar) {
                    // Main berths: LB, MB, UB
                    String[] mainBerths = is2A ? new String[] { "LB", "UB" }
                            : is1A ? new String[] { "LB", "UB" } : new String[] { "LB", "MB", "UB" };
                    for (String bt : mainBerths) {
                        String seatId = currentCoach[0] + "-" + bay + bt;
                        boolean isBooked = bookedSeats.contains(seatId);
                        boolean isSel = selectedSeats.contains(seatId);
                        Label seatL = makeSeatLabel(seatId, bay, bt, isBooked, isSel);
                        if (!isBooked) {
                            seatL.setOnMouseClicked(ev -> {
                                if (selectedSeats.contains(seatId)) {
                                    selectedSeats.remove(seatId);
                                    seatL.setStyle(seatAvailStyle());
                                    statLabels[3].setText("Selected: " + selectedSeats.size());
                                } else {
                                    selectedSeats.add(seatId);
                                    seatL.setStyle(seatSelStyle());
                                    statLabels[3].setText("Selected: " + selectedSeats.size());
                                }
                            });
                        }
                        bayRow.getChildren().add(seatL);
                    }
                    // Aisle gap
                    Region aisleGap = new Region();
                    aisleGap.setPrefWidth(24);
                    aisleGap.setPrefHeight(38);
                    aisleGap.setStyle(
                            "-fx-background-color:linear-gradient(to right,#F1F5F9,#E2E8F0,#F1F5F9); -fx-background-radius:2;");
                    Label aisleL = label("│", 12, false, "#D1D5DB");
                    aisleL.setPrefWidth(24);
                    aisleL.setAlignment(Pos.CENTER);
                    bayRow.getChildren().add(aisleL);
                    // Side berths: SL, SU
                    for (String bt : new String[] { "SL", "SU" }) {
                        String seatId = currentCoach[0] + "-" + bay + bt;
                        boolean isBooked = bookedSeats.contains(seatId);
                        boolean isSel = selectedSeats.contains(seatId);
                        Label seatL = makeSeatLabel(seatId, bay, bt, isBooked, isSel);
                        if (!isBooked) {
                            seatL.setOnMouseClicked(ev -> {
                                if (selectedSeats.contains(seatId)) {
                                    selectedSeats.remove(seatId);
                                    seatL.setStyle(seatAvailStyle());
                                    statLabels[3].setText("Selected: " + selectedSeats.size());
                                } else {
                                    selectedSeats.add(seatId);
                                    seatL.setStyle(seatSelStyle());
                                    statLabels[3].setText("Selected: " + selectedSeats.size());
                                }
                            });
                        }
                        bayRow.getChildren().add(seatL);
                    }
                } else {
                    // Chair car: W M A M W
                    String[] seatPos = { "W", "M", "A", "M2", "W2" };
                    String[] seatLabels2 = { "W", "M", "A", "M", "W" };
                    for (int s = 0; s < 5; s++) {
                        String seatId = currentCoach[0] + "-" + bay + seatPos[s];
                        boolean isBooked = bookedSeats.contains(seatId);
                        boolean isSel = selectedSeats.contains(seatId);
                        Label seatL = makeSeatLabel(seatId, bay, seatLabels2[s], isBooked, isSel);
                        if (s == 2) {
                            seatL.setStyle(
                                    seatL.getStyle() + "-fx-border-left-color:#9CA3AF; -fx-border-width:0 0 0 2;");
                        }
                        if (!isBooked) {
                            seatL.setOnMouseClicked(ev -> {
                                if (selectedSeats.contains(seatId)) {
                                    selectedSeats.remove(seatId);
                                    seatL.setStyle(seatAvailStyle());
                                    statLabels[3].setText("Selected: " + selectedSeats.size());
                                } else {
                                    selectedSeats.add(seatId);
                                    seatL.setStyle(seatSelStyle());
                                    statLabels[3].setText("Selected: " + selectedSeats.size());
                                }
                            });
                        }
                        bayRow.getChildren().add(seatL);
                        if (s == 1 || s == 2) {
                            Region g = new Region();
                            g.setPrefWidth(6);
                            bayRow.getChildren().add(g);
                        }
                    }
                }
                seatGridContainer.getChildren().add(bayRow);
            }
            // Footer
            HBox footer = new HBox(0);
            footer.setAlignment(Pos.CENTER);
            footer.setPadding(new Insets(8, 10, 8, 10));
            footer.setStyle("-fx-background-color:#1E3A5F; -fx-background-radius:0 0 8 8;");
            footer.getChildren().add(label("Guard/Toilet End  ►", 10, false, "#6B9ED2"));
        };

        // Coach tabs with scroll
        HBox coachTabsRow = new HBox(8);
        coachTabsRow.setAlignment(Pos.CENTER_LEFT);
        Label[] coachTabLabels = new Label[coachNames.length];
        for (int ci2 = 0; ci2 < coachNames.length; ci2++) {
            final int cIdx = ci2;
            Label tabL = label(coachNames[ci2], 12, ci2 == 0, ci2 == 0 ? "#FFFFFF" : C_SUBTEXT);
            tabL.setStyle(ci2 == 0 ? selCoachStyle : norCoachStyle);
            tabL.setOnMouseClicked(ev -> {
                currentCoach[0] = coachNames[cIdx];
                for (Label tl : coachTabLabels)
                    if (tl != null)
                        tl.setStyle(norCoachStyle);
                tabL.setStyle(selCoachStyle);
                buildGrid[0].run();
            });
            coachTabLabels[ci2] = tabL;
            coachTabsRow.getChildren().add(tabL);
        }
        buildGrid[0].run();

        // Legend
        HBox mapLegend = new HBox(18);
        mapLegend.setAlignment(Pos.CENTER_LEFT);
        mapLegend.setPadding(new Insets(8, 0, 0, 0));
        for (String[] leg : new String[][] { { "#DEF7EC", "#057A55", "✅ Available (click to select)" },
                { "#FDE8E8", "#C81E1E", "❌ Booked" }, { C_PRIMARY, "white", "🔵 Selected" } }) {
            HBox li = new HBox(6);
            li.setAlignment(Pos.CENTER_LEFT);
            Label ld2 = new Label(leg[2]);
            ld2.setStyle("-fx-font-size:11px; -fx-text-fill:" + leg[1] + "; -fx-background-color:" + leg[0]
                    + "44; -fx-background-radius:4; -fx-padding:2 8;");
            li.getChildren().add(ld2);
            mapLegend.getChildren().add(li);
        }

        ScrollPane seatScroll = new ScrollPane(seatGridContainer);
        seatScroll.setFitToWidth(true);
        seatScroll.setPrefHeight(400);
        seatScroll.setStyle("-fx-background:#FAFBFF; -fx-background-color:#FAFBFF;");

        seatMapSec.getChildren().addAll(statsBar, coachTabsRow, seatScroll, mapLegend);
        formCol.getChildren().add(seatMapSec);

        // ── STEP 4: Extras ──
        VBox extSec = new VBox(14);
        extSec.setStyle(lc2());
        extSec.setPadding(new Insets(18, 20, 18, 20));
        extSec.getChildren().add(sh("3", "Extras & Contact Info"));
        HBox contRow = new HBox(16);
        TextField tfEmail = lf("Email Address");
        tfEmail.setText(CURRENT_USER.email);
        tfEmail.setPrefWidth(240);
        tfEmail.setPrefHeight(40);
        TextField tfPhone = lf("Mobile Number");
        tfPhone.setText(CURRENT_USER.phone);
        tfPhone.setPrefWidth(180);
        tfPhone.setPrefHeight(40);
        contRow.getChildren().addAll(tfEmail, tfPhone);
        HBox extrasRow = new HBox(20);
        CheckBox cbMeal = new CheckBox("🍱 Add Meal");
        cbMeal.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_SUBTEXT + ";");
        CheckBox cbInsure = new CheckBox("🛡 Travel Insurance");
        cbInsure.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_SUBTEXT + ";");
        CheckBox cbSMS = new CheckBox("📱 SMS Alerts");
        cbSMS.setSelected(true);
        cbSMS.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_SUBTEXT + ";");
        extrasRow.getChildren().addAll(cbMeal, cbInsure, cbSMS);
        extSec.getChildren().addAll(label("Contact Details", 12, true, C_SUBTEXT), contRow,
                label("Optional Services", 12, true, C_SUBTEXT), extrasRow);
        formCol.getChildren().add(extSec);

        // ── RIGHT: Fare Summary ──
        VBox rightCol = new VBox(14);
        rightCol.setPrefWidth(300);
        VBox fareSumBox = new VBox(12);
        fareSumBox.setStyle(lc2());
        fareSumBox.setPadding(new Insets(18, 20, 18, 20));
        fareSumBox.getChildren().add(label("Fare Summary", 15, true, C_TEXT));
        fareSumBox.getChildren().add(new Separator());
        Label fsClass = label("Class: " + selClass[0], 12, false, C_SUBTEXT);
        Label fsQuota = label("Quota: " + quota, 12, false, C_SUBTEXT);
        Label fsPax = label("Passengers: " + prePax, 12, false, C_SUBTEXT);
        Label fsBase = label("Base Fare: ₹" + String.format("%.0f", train.baseFare), 12, false, C_MUTED);
        Label fsTotal = label("", 18, true, C_PRIMARY);
        Label fsGrand = label("", 16, true, C_DANGER);
        Runnable updateFare = () -> {
            double base = train.baseFare * selMult[0];
            int pax = cbCount.getValue();
            double svcFee = 47.12 * pax;
            fsClass.setText("Class: " + selClass[0]);
            fsPax.setText("Passengers: " + pax);
            fsBase.setText("Base: ₹" + String.format("%.0f", base) + " × " + pax);
            fsTotal.setText("₹" + String.format("%.0f", base * pax));
            fsGrand.setText("Total: ₹" + String.format("%.2f", base * pax + svcFee));
        };
        cbCount.setOnAction(e -> {
            buildPax.run();
            updateFare.run();
        });
        updateFare.run();
        fareSumBox.getChildren().addAll(fsClass, fsQuota, fsPax, new Separator(), fsBase,
                fr("Service Fee", "₹" + String.format("%.2f", 47.12 * prePax)), fr("GST", "Included"), new Separator(),
                fsTotal, fsGrand);

        VBox notesBox = new VBox(8);
        notesBox.setPadding(new Insets(14));
        notesBox.setStyle(
                "-fx-background-color:#FFFBEB; -fx-background-radius:8; -fx-border-color:#FCD34D; -fx-border-radius:8; -fx-border-width:1;");
        notesBox.getChildren().addAll(label("📝  Important Notes", 12, true, C_ACCENT),
                label("• Carry valid Government Photo ID", 11, false, C_SUBTEXT),
                label("• Tatkal charges extra 10-30%", 11, false, C_SUBTEXT),
                label("• No refund for GN class", 11, false, C_SUBTEXT),
                label("• E-ticket valid with original ID", 11, false, C_SUBTEXT));

        Button btnProceed = pb("PROCEED TO PAYMENT  →", C_PRIMARY);
        btnProceed.setMaxWidth(Double.MAX_VALUE);
        btnProceed.setPrefHeight(46);
        btnProceed.setOnAction(e -> {
            List<Passenger> pList = new ArrayList<>();
            for (Object[] pw : paxWidgets) {
                TextField tn = (TextField) pw[0], ta = (TextField) pw[1];
                @SuppressWarnings("unchecked")
                ComboBox<String> cg = (ComboBox<String>) pw[2];
                @SuppressWarnings("unchecked")
                ComboBox<String> cb2 = (ComboBox<String>) pw[3];
                @SuppressWarnings("unchecked")
                ComboBox<String> cn = (ComboBox<String>) pw[4];
                if (tn.getText().isBlank() || ta.getText().isBlank()) {
                    showAlert("⚠  Fill all passenger name and age.");
                    return;
                }
                try {
                    int age = Integer.parseInt(ta.getText().trim());
                    if (age < 1 || age > 120) {
                        showAlert("⚠  Age must be 1–120.");
                        return;
                    }
                    pList.add(new Passenger(tn.getText().trim(), age,
                            cg.getValue() != null ? cg.getValue() : "Male",
                            cb2.getValue() != null ? cb2.getValue() : "No Preference",
                            cn.getValue() != null ? cn.getValue() : "Indian", "TBD"));
                } catch (Exception ex) {
                    showAlert("⚠  Age must be a valid number.");
                    return;
                }
            }
            double svcFee = 47.12 * pList.size();
            double total = train.baseFare * selMult[0] * pList.size() + svcFee;
            String selQ = qGroup.getSelectedToggle() != null ? ((RadioButton) qGroup.getSelectedToggle()).getText()
                    : "General";
            showPayment(train, date, selClass[0], selQ, pList, total);
        });
        rightCol.getChildren().addAll(fareSumBox, notesBox, btnProceed);
        mainLayout.getChildren().addAll(formCol, rightCol);
        outer.getChildren().add(mainLayout);

        VBox pageContent = new VBox(0, steps, outer);
        ScrollPane sp = new ScrollPane(pageContent);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // PAYMENT — with full validation
    // ════════════════════════════════════════════════════════
    static void showPayment(Train train, String date, String seatClass, String quota, List<Passenger> passengers,
            double total) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Payment"));

        HBox layout = new HBox(20);
        layout.setPadding(new Insets(24, 40, 40, 40));
        layout.setStyle("-fx-background-color:" + C_BG + ";");
        VBox left = new VBox(16);
        HBox.setHgrow(left, Priority.ALWAYS);
        left.getChildren().add(st("💳  Choose Payment Method"));

        ToggleGroup tg = new ToggleGroup();

        // Card fields
        TextField tfCNo = lf("Card Number (16 digits)");
        tfCNo.setPrefHeight(42);
        HBox cr = new HBox(12);
        TextField tfExp = lf("MM/YY");
        tfExp.setPrefWidth(100);
        tfExp.setPrefHeight(42);
        TextField tfCVV = lf("CVV");
        tfCVV.setPrefWidth(80);
        tfCVV.setPrefHeight(42);
        TextField tfHolder = lf("Name on Card");
        tfHolder.setPrefHeight(42);
        cr.getChildren().addAll(tfExp, tfCVV, tfHolder);
        VBox cardPnl = new VBox(12);
        cardPnl.setPadding(new Insets(12, 0, 4, 0));
        cardPnl.getChildren().addAll(label("Card Details", 12, true, C_SUBTEXT), tfCNo, cr);

        // UPI fields
        TextField tfUPI = lf("UPI ID (e.g. name@okaxis)");
        tfUPI.setPrefHeight(42);
        HBox upiLogos = new HBox(10);
        for (String u : new String[] { "GPay", "PhonePe", "Paytm", "BHIM" }) {
            Label ul = label(u, 11, true, C_SUBTEXT);
            ul.setPadding(new Insets(5, 12, 5, 12));
            ul.setStyle("-fx-background-color:" + C_BG + "; -fx-background-radius:6; -fx-border-color:" + C_BORDER
                    + "; -fx-border-radius:6;");
            upiLogos.getChildren().add(ul);
        }
        VBox upiPnl = new VBox(12);
        upiPnl.setPadding(new Insets(12, 0, 4, 0));
        upiPnl.getChildren().addAll(label("UPI ID", 12, true, C_SUBTEXT), tfUPI, upiLogos);

        // Net Banking
        ComboBox<String> cbBank = lc(FXCollections.observableArrayList("State Bank of India", "HDFC Bank", "ICICI Bank",
                "Axis Bank", "Kotak Mahindra", "Punjab National Bank", "Bank of Baroda"));
        cbBank.setPromptText("Select Your Bank");
        cbBank.setPrefHeight(42);
        cbBank.setMaxWidth(Double.MAX_VALUE);
        VBox netPnl = new VBox(12);
        netPnl.setPadding(new Insets(12, 0, 4, 0));
        netPnl.getChildren().addAll(label("Select Bank", 12, true, C_SUBTEXT), cbBank);

        // Wallet
        ComboBox<String> cbWallet = lc(
                FXCollections.observableArrayList("Paytm Wallet", "PhonePe", "Amazon Pay", "MobiKwik", "Airtel Money"));
        cbWallet.setPromptText("Select Wallet");
        cbWallet.setPrefHeight(42);
        cbWallet.setMaxWidth(Double.MAX_VALUE);
        VBox walletPnl = new VBox(12);
        walletPnl.setPadding(new Insets(12, 0, 4, 0));
        walletPnl.getChildren().addAll(label("Select Wallet", 12, true, C_SUBTEXT), cbWallet);

        VBox[] panels = { cardPnl, upiPnl, netPnl, walletPnl };
        String[][] methods = { { "💳", "Credit / Debit Card" }, { "📱", "UPI Payment" }, { "🏦", "Net Banking" },
                { "👛", "Wallet" } };
        VBox methodsBox = new VBox(10);

        for (int i = 0; i < methods.length; i++) {
            final int idx = i;
            RadioButton rb = new RadioButton(methods[i][0] + "  " + methods[i][1]);
            rb.setToggleGroup(tg);
            rb.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
            if (i == 0)
                rb.setSelected(true);
            VBox mBox = new VBox(8);
            mBox.setPadding(new Insets(14, 18, 14, 18));
            mBox.setStyle(pm(i == 0));
            mBox.getChildren().add(rb);
            if (i == 0)
                mBox.getChildren().add(panels[i]);
            rb.setOnAction(e -> {
                for (Node n : methodsBox.getChildren()) {
                    if (n instanceof VBox) {
                        VBox mb = (VBox) n;
                        if (mb.getChildren().size() > 1)
                            mb.getChildren().remove(1);
                        mb.setStyle(pm(false));
                    }
                }
                mBox.getChildren().add(panels[idx]);
                mBox.setStyle(pm(true));
            });
            methodsBox.getChildren().add(mBox);
        }
        left.getChildren().add(methodsBox);

        // Right summary
        VBox right = new VBox(16);
        right.setPrefWidth(310);
        VBox summary = new VBox(12);
        summary.setStyle(lc2());
        summary.setPadding(new Insets(18, 20, 18, 20));
        summary.getChildren().addAll(
                label("Order Summary", 15, true, C_TEXT), new Separator(),
                label(train.trainName, 14, true, C_TEXT), label("#" + train.trainNo, 12, false, C_MUTED),
                label(train.source + " → " + train.destination, 13, false, C_SUBTEXT),
                label("Date: " + date, 12, false, C_MUTED),
                label("Class: " + seatClass, 12, true, C_PRIMARY),
                label("Quota: " + quota, 12, false, C_MUTED),
                label("Passengers: " + passengers.size(), 12, false, C_MUTED),
                new Separator(),
                fr("Ticket Fare", "₹" + String.format("%.2f", total - 47.12 * passengers.size())),
                fr("Service Fee", "₹" + String.format("%.2f", 47.12 * passengers.size())),
                fr("GST", "Included"),
                new Separator(),
                label("Grand Total: ₹" + String.format("%.2f", total), 18, true, C_DANGER));

        Label secureL = label("🔒  100% Secure Payment | PCI-DSS Compliant", 11, false, C_SUCCESS);

        // Validation error label (shown in red below pay button)
        Label payErrL = label("", 12, false, C_DANGER);
        payErrL.setWrapText(true);

        Button btnPay = pb("PAY SECURELY  ₹" + String.format("%.2f", total), C_SUCCESS);
        btnPay.setMaxWidth(Double.MAX_VALUE);
        btnPay.setPrefHeight(46);

        btnPay.setOnAction(e -> {
            payErrL.setText("");
            RadioButton sel = (RadioButton) tg.getSelectedToggle();
            String pm2 = sel != null ? sel.getText().replaceAll("  ", " ") : "Card";

            // ── PAYMENT VALIDATION ──
            if (pm2.contains("Card")) {
                String cardNo = tfCNo.getText().trim();
                String exp = tfExp.getText().trim();
                String cvv = tfCVV.getText().trim();
                String holder = tfHolder.getText().trim();
                if (cardNo.isBlank() || exp.isBlank() || cvv.isBlank() || holder.isBlank()) {
                    payErrL.setText(
                            "⚠  Please fill all card details: Card Number, Expiry (MM/YY), CVV and Name on Card.");
                    return;
                }
                if (cardNo.replaceAll("[\\s-]", "").length() != 16) {
                    payErrL.setText("⚠  Card number must be exactly 16 digits.");
                    return;
                }
                if (!exp.matches("\\d{2}/\\d{2}")) {
                    payErrL.setText("⚠  Expiry must be in MM/YY format (e.g. 08/27).");
                    return;
                }
                if (cvv.length() < 3 || cvv.length() > 4 || !cvv.matches("\\d+")) {
                    payErrL.setText("⚠  CVV must be 3 or 4 digits.");
                    return;
                }
            } else if (pm2.contains("UPI")) {
                String upiId = tfUPI.getText().trim();
                if (upiId.isBlank()) {
                    payErrL.setText("⚠  Please enter your UPI ID (e.g. name@okaxis).");
                    return;
                }
                if (!upiId.contains("@")) {
                    payErrL.setText("⚠  Invalid UPI ID. It must contain '@' symbol.");
                    return;
                }
            } else if (pm2.contains("Net Banking")) {
                if (cbBank.getValue() == null || cbBank.getValue().isBlank()) {
                    payErrL.setText("⚠  Please select your Bank for Net Banking.");
                    return;
                }
            } else if (pm2.contains("Wallet")) {
                if (cbWallet.getValue() == null || cbWallet.getValue().isBlank()) {
                    payErrL.setText("⚠  Please select your Wallet.");
                    return;
                }
            }

            // ── All validations passed — Save to DB ──
            String pnr;
            try {
                pnr = DatabaseManager.getNextPNR();
            } catch (Exception ex) {
                pnr = "PNR" + String.format("%07d", ++PNR_SEQ);
            }

            String[] berthNos = { "A1/LB", "A1/MB", "A1/UB", "A2/LB", "A2/MB", "A2/UB" };
            for (int i = 0; i < passengers.size(); i++)
                passengers.get(i).seat = berthNos[i % berthNos.length];

            // ── Save booking to MySQL ──
            boolean bookingSaved = false;
            try {
                bookingSaved = DatabaseManager.saveBooking(
                        pnr, CURRENT_USER.id, train.trainNo, train.trainName,
                        train.source, train.destination, date,
                        seatClass, quota, passengers.size(), total, pm2);
                // ── Save each passenger to MySQL ──
                if (bookingSaved) {
                    for (Passenger p3 : passengers) {
                        DatabaseManager.savePassenger(pnr, p3.name, p3.age, p3.gender, p3.berthPref, p3.nationality,
                                p3.seat);
                    }
                    // ── Update available seats in DB ──
                    DatabaseManager.updateAvailableSeats(train.trainNo, -passengers.size());
                    System.out.println("✅ Booking saved to DB: " + pnr);
                }
            } catch (Exception ex) {
                System.out.println("DB booking save failed: " + ex.getMessage());
            }

            // ── Always save in-memory (works even if DB is down) ──
            Booking b = new Booking(pnr, CURRENT_USER.username, train.trainNo, train.trainName,
                    train.source, train.destination, date, seatClass, quota, passengers, total, pm2);
            BOOKINGS.add(b);
            train.availableSeats = Math.max(0, train.availableSeats - passengers.size());
            showTicket(b);
        });

        right.getChildren().addAll(summary, secureL, payErrL, btnPay);
        layout.getChildren().addAll(left, right);
        ScrollPane sp = new ScrollPane(layout);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // TICKET — shows CONFIRMED or CANCELLED based on status
    // ════════════════════════════════════════════════════════
    static void showTicket(Booking b) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        boolean isCancelled = b.status.equals("CAN");
        root.setTop(navBar(isCancelled ? "Cancel Ticket" : "Booking Confirmed"));
        VBox outer = new VBox(20);
        outer.setPadding(new Insets(28, 80, 40, 80));
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Banner: GREEN for confirmed, RED for cancelled ──
        HBox banner = new HBox(16);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(16, 24, 16, 24));
        banner.setMaxWidth(760);
        VBox bt = new VBox(3);
        if (isCancelled) {
            banner.setStyle("-fx-background-color:" + C_DANGER_L + "; -fx-background-radius:10; -fx-border-color:"
                    + C_DANGER + "; -fx-border-radius:10; -fx-border-width:2;");
            bt.getChildren().addAll(label("Booking Cancelled", 20, true, C_DANGER), label(
                    "Your ticket has been cancelled. Refund will be processed in 5-7 days.", 13, false, C_DANGER));
            banner.getChildren().addAll(label("❌", 30, false, C_DANGER), bt);
        } else {
            banner.setStyle("-fx-background-color:" + C_SUCCESS_L + "; -fx-background-radius:10; -fx-border-color:"
                    + C_SUCCESS + "; -fx-border-radius:10; -fx-border-width:2;");
            bt.getChildren().addAll(label("Booking Confirmed!", 20, true, C_SUCCESS),
                    label("Your e-ticket is booked. Happy Journey! 🚆", 13, false, C_SUCCESS));
            banner.getChildren().addAll(label("✅", 30, false, C_SUCCESS), bt);
        }

        VBox ticket = new VBox(0);
        ticket.setMaxWidth(760);
        String ticketBorder = isCancelled ? "#FECACA" : "#CBD5E1";
        ticket.setStyle("-fx-background-color:" + C_SURFACE + "; -fx-background-radius:12; -fx-border-color:"
                + ticketBorder
                + "; -fx-border-radius:12; -fx-border-width:1; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),16,0,0,4);");

        String hdrColor = isCancelled ? C_DANGER : C_NAV;
        HBox hdr = new HBox(0);
        hdr.setStyle("-fx-background-color:" + hdrColor + "; -fx-background-radius:12 12 0 0;");
        hdr.setPadding(new Insets(14, 24, 14, 24));
        hdr.setAlignment(Pos.CENTER_LEFT);
        HBox hsp2 = new HBox();
        HBox.setHgrow(hsp2, Priority.ALWAYS);
        Label pnrL = label("PNR: " + b.pnr, 14, true, C_NAV_TEXT);
        pnrL.setStyle("-fx-background-color:rgba(255,255,255,0.2); -fx-background-radius:6; -fx-padding:4 12;");
        String statusBadgeText = isCancelled ? "● CANCELLED" : "● CONFIRMED";
        String statusBadgeBg = isCancelled ? "rgba(0,0,0,0.3)" : "rgba(0,150,50,0.4)";
        Label stL = label(statusBadgeText, 13, true, C_NAV_TEXT);
        stL.setStyle("-fx-background-color:" + statusBadgeBg + "; -fx-background-radius:6; -fx-padding:4 10;");
        hdr.getChildren().addAll(label("🚆  RailConnect — E-Ticket", 15, true, C_NAV_TEXT), hsp2, pnrL,
                new Label("   "), stL);

        HBox rRow = new HBox(0);
        rRow.setPadding(new Insets(20, 28, 16, 28));
        rRow.setAlignment(Pos.CENTER);
        rRow.setStyle("-fx-background-color:#F8FAFF;");
        VBox dep = new VBox(3);
        dep.setAlignment(Pos.CENTER_LEFT);
        dep.getChildren().addAll(label(b.source, 24, true, C_TEXT), label("Departure", 11, false, C_MUTED));
        VBox mid = new VBox(5);
        mid.setAlignment(Pos.CENTER);
        mid.setPrefWidth(240);
        mid.getChildren().addAll(label(b.trainName, 12, true, C_MUTED), label("─────────►", 16, false, C_BORDER),
                label(b.journeyDate, 12, true, C_PRIMARY));
        VBox arr = new VBox(3);
        arr.setAlignment(Pos.CENTER_RIGHT);
        arr.getChildren().addAll(label(b.destination, 24, true, C_TEXT), label("Arrival", 11, false, C_MUTED));
        HBox.setHgrow(mid, Priority.ALWAYS);
        rRow.getChildren().addAll(dep, mid, arr);

        Separator s1 = new Separator();
        s1.setStyle("-fx-background-color:#E2E8F0;");
        GridPane dg = new GridPane();
        dg.setPadding(new Insets(16, 28, 16, 28));
        dg.setHgap(40);
        dg.setVgap(10);
        String[][] details = { { "Train No", "#" + b.trainNo }, { "Seat Class", b.seatClass }, { "Quota", b.quota },
                { "Journey Date", b.journeyDate }, { "Payment", b.paymentMode }, { "Booked On", b.bookingDateTime },
                { "Passengers", b.passengers.size() + "" },
                { "Total Paid", "₹" + String.format("%.2f", b.totalFare) } };
        for (int i = 0; i < details.length; i++) {
            Label kl = label(details[i][0], 11, false, C_MUTED);
            kl.setPrefWidth(100);
            dg.add(kl, i % 2 * 2, i / 2);
            dg.add(label(details[i][1], 13, true, C_TEXT), i % 2 * 2 + 1, i / 2);
        }
        Separator s2 = new Separator();
        s2.setStyle("-fx-background-color:#E2E8F0;");

        VBox paxTable = new VBox(0);
        paxTable.setPadding(new Insets(12, 28, 8, 28));
        paxTable.getChildren().add(label("Passenger Details", 13, true, C_TEXT));
        HBox phdr = new HBox(0);
        phdr.setStyle("-fx-background-color:" + C_BG + "; -fx-padding:8 0 8 0;");
        // FIX: all values are Strings in String[][]
        for (String[] h : new String[][] { { "#", "40" }, { "Passenger Name", "200" }, { "Age", "60" },
                { "Gender", "90" }, { "Berth", "120" }, { "Seat No", "90" } }) {
            Label hl = label(h[0], 11, true, C_MUTED);
            hl.setPrefWidth(Integer.parseInt(h[1]));
            phdr.getChildren().add(hl);
        }
        paxTable.getChildren().add(phdr);
        int[] pn = { 1 };
        for (Passenger p : b.passengers) {
            HBox pr = new HBox(0);
            pr.setPadding(new Insets(8, 0, 8, 0));
            pr.setStyle("-fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;");
            // FIX: all values are Object[][] using int cast for width
            for (Object[] od : new Object[][] { { pn[0] + "", 40 }, { p.name, 200 }, { p.age + "", 60 },
                    { p.gender, 90 }, { p.berthPref, 120 }, { p.seat, 90 } }) {
                Label dl = label(od[0].toString(), 12, false, C_TEXT);
                dl.setPrefWidth((int) od[1]);
                pr.getChildren().add(dl);
            }
            paxTable.getChildren().add(pr);
            pn[0]++;
        }

        HBox footer = new HBox(12);
        footer.setPadding(new Insets(12, 28, 14, 28));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color:#F1F5F9; -fx-background-radius:0 0 12 12;");
        footer.getChildren().addAll(label("⚠", 14, false, C_ACCENT),
                label("Carry valid Government Photo ID. This ticket is valid only with original ID proof.", 11, false,
                        C_MUTED));

        ticket.getChildren().addAll(hdr, rRow, s1, dg, s2, paxTable, footer);

        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER);
        Button btnHome = gb("🏠  Home");
        Button btnHist = pb("📋  My Bookings", C_PRIMARY);
        Button btnPDF = pb("🖨️  Download Ticket", C_SUCCESS);
        btnHome.setOnAction(e -> showHome());
        btnHist.setOnAction(e -> showMyBookings());
        btnPDF.setOnAction(e -> generatePDFTicket(b));
        actions.getChildren().addAll(btnHome, btnHist, btnPDF);
        if (!isCancelled) {
            Button btnCan = new Button("❌  Cancel Ticket");
            btnCan.setStyle("-fx-background-color:" + C_DANGER_L + "; -fx-text-fill:" + C_DANGER
                    + "; -fx-font-weight:bold; -fx-font-size:13px; -fx-padding:10 20; -fx-background-radius:8; -fx-border-color:"
                    + C_DANGER + "; -fx-border-radius:8; -fx-border-width:1; -fx-cursor:hand;");
            btnCan.setOnAction(e -> {
                b.status = "CAN";
                trainByNo(b.trainNo).ifPresent(t -> t.availableSeats += b.passengers.size());
                // ── Cancel in DB ──
                try {
                    DatabaseManager.cancelBooking(b.pnr);
                    DatabaseManager.updateAvailableSeats(b.trainNo, b.passengers.size());
                    System.out.println("✅ Booking cancelled in DB: " + b.pnr);
                } catch (Exception ex) {
                    System.out.println("DB cancel failed: " + ex.getMessage());
                }
                showTicket(b);
            });
            actions.getChildren().add(btnCan);
        }
        outer.getChildren().addAll(banner, ticket, actions);
        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // MY BOOKINGS
    // ════════════════════════════════════════════════════════
    static void showMyBookings() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("My Bookings"));
        VBox outer = new VBox(16);
        outer.setPadding(new Insets(24, 40, 40, 40));
        outer.setStyle("-fx-background-color:" + C_BG + ";");
        outer.getChildren().add(st("📋  My Bookings"));
        // ── Load bookings from MySQL DB ──
        List<Booking> mine = new ArrayList<>();
        try {
            java.sql.ResultSet rs = DatabaseManager.getBookingsByUser(CURRENT_USER.id);
            if (rs != null) {
                while (rs.next()) {
                    String pnr2 = rs.getString("pnr");
                    // Load passengers for this PNR
                    List<Passenger> pList = new ArrayList<>();
                    try {
                        java.sql.ResultSet prs = DatabaseManager.getPassengersByPNR(pnr2);
                        if (prs != null)
                            while (prs.next())
                                pList.add(new Passenger(prs.getString("name"), prs.getInt("age"),
                                        prs.getString("gender"), prs.getString("berth_pref"),
                                        prs.getString("nationality"), prs.getString("seat_no")));
                    } catch (Exception pe) {
                        System.out.println("Passenger load err: " + pe.getMessage());
                    }
                    Booking bk = new Booking(pnr2, CURRENT_USER.username, rs.getString("train_no"),
                            rs.getString("train_name"), rs.getString("source"), rs.getString("destination"),
                            rs.getString("journey_date"), rs.getString("seat_class"), rs.getString("quota"), pList,
                            rs.getDouble("total_fare"), rs.getString("payment_mode"));
                    bk.status = rs.getString("status");
                    bk.bookingDateTime = rs.getString("booking_date");
                    mine.add(bk);
                }
            }
        } catch (Exception ex) {
            System.out.println("DB load bookings failed, using local: " + ex.getMessage());
            // Fallback to in-memory
            mine.addAll(BOOKINGS.stream().filter(bk -> bk.username.equals(CURRENT_USER.username))
                    .collect(Collectors.toList()));
        }
        if (mine.isEmpty()) {
            // Also check in-memory if DB returned nothing
            mine.addAll(BOOKINGS.stream().filter(bk -> bk.username.equals(CURRENT_USER.username))
                    .collect(Collectors.toList()));
        }
        Collections.reverse(mine);
        if (mine.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            empty.getChildren().addAll(label("🎫", 48, false, C_MUTED), label("No bookings yet.", 16, false, C_MUTED),
                    label("Book your first train ticket now!", 13, false, C_MUTED));
            outer.getChildren().add(empty);
        }
        for (Booking b : mine) {
            HBox card = new HBox(0);
            card.setStyle(lc2());
            VBox info = new VBox(6);
            info.setPadding(new Insets(14, 18, 14, 18));
            HBox.setHgrow(info, Priority.ALWAYS);
            Label pnrL = label("PNR: " + b.pnr, 11, true, C_NAV_TEXT);
            pnrL.setStyle("-fx-background-color:" + C_NAV + "; -fx-background-radius:4; -fx-padding:2 8;");
            info.getChildren().addAll(pnrL, label(b.trainName + " (#" + b.trainNo + ")", 15, true, C_TEXT),
                    label(b.source + "  →  " + b.destination, 13, false, C_SUBTEXT),
                    label("Date: " + b.journeyDate + "   |   Class: " + b.seatClass, 12, false, C_MUTED),
                    label("Passengers: " + b.passengers.size() + "   |   Paid: ₹" + String.format("%.2f", b.totalFare),
                            12, false, C_MUTED));
            boolean cnf = b.status.equals("CNF");
            VBox stBox = new VBox(8);
            stBox.setAlignment(Pos.CENTER);
            stBox.setPadding(new Insets(14, 18, 14, 18));
            stBox.setPrefWidth(150);
            stBox.setStyle("-fx-background-color:" + (cnf ? C_SUCCESS_L : C_DANGER_L) + ";");
            stBox.getChildren().addAll(label(cnf ? "✅" : "❌", 26, false, cnf ? C_SUCCESS : C_DANGER),
                    label(cnf ? "CONFIRMED" : "CANCELLED", 12, true, cnf ? C_SUCCESS : C_DANGER),
                    label("₹" + String.format("%.2f", b.totalFare), 14, true, C_TEXT));
            VBox actBox = new VBox(10);
            actBox.setAlignment(Pos.CENTER);
            actBox.setPadding(new Insets(14, 14, 14, 14));
            actBox.setStyle("-fx-background-color:" + C_BG + "; -fx-background-radius:0 10 10 0;");
            Button btnV = pb("VIEW", C_PRIMARY);
            btnV.setPrefWidth(110);
            btnV.setPrefHeight(34);
            btnV.setOnAction(e -> showTicket(b));
            Button btnP = new Button("🖨 PDF");
            btnP.setPrefWidth(110);
            btnP.setPrefHeight(34);
            btnP.setStyle("-fx-background-color:#DEF7EC; -fx-text-fill:" + C_SUCCESS
                    + "; -fx-font-weight:bold; -fx-font-size:12px; -fx-background-radius:8; -fx-border-color:"
                    + C_SUCCESS + "; -fx-border-radius:8; -fx-border-width:1; -fx-cursor:hand;");
            btnP.setOnAction(e -> generatePDFTicket(b));
            actBox.getChildren().addAll(btnV, btnP);
            if (cnf) {
                Button btnC = new Button("CANCEL");
                btnC.setPrefWidth(110);
                btnC.setPrefHeight(34);
                btnC.setStyle("-fx-background-color:" + C_DANGER_L + "; -fx-text-fill:" + C_DANGER
                        + "; -fx-font-weight:bold; -fx-font-size:12px; -fx-background-radius:8; -fx-border-color:"
                        + C_DANGER + "; -fx-border-radius:8; -fx-border-width:1; -fx-cursor:hand;");
                btnC.setOnAction(e -> {
                    b.status = "CAN";
                    trainByNo(b.trainNo).ifPresent(t -> t.availableSeats += b.passengers.size());
                    try {
                        DatabaseManager.cancelBooking(b.pnr);
                        DatabaseManager.updateAvailableSeats(b.trainNo, b.passengers.size());
                    } catch (Exception ex) {
                        System.out.println("DB cancel err:" + ex.getMessage());
                    }
                    showMyBookings();
                });
                actBox.getChildren().add(btnC);
            }
            card.getChildren().addAll(info, stBox, actBox);
            outer.getChildren().add(card);
        }
        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // CANCEL TICKET
    // ════════════════════════════════════════════════════════
    static void showCancelTicket() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Cancel Ticket"));
        VBox outer = new VBox(20);
        outer.setPadding(new Insets(40, 80, 60, 80));
        outer.setMaxWidth(660);
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setStyle("-fx-background-color:" + C_BG + ";");
        outer.getChildren().addAll(st("❌  Cancel Ticket"),
                label("Enter your PNR to find and cancel your booking.", 13, false, C_MUTED));
        VBox fc = new VBox(16);
        fc.setStyle(lc2());
        fc.setPadding(new Insets(24, 28, 24, 28));
        TextField tfPNR = lf("PNR Number (e.g. PNR2001001)");
        tfPNR.setPrefHeight(44);
        Label resL = label("", 13, false, C_TEXT);
        resL.setWrapText(true);
        Button btnFind = pb("SEARCH BOOKING", C_PRIMARY);
        btnFind.setPrefHeight(42);
        Button btnCan = pb("CONFIRM CANCELLATION", C_DANGER);
        btnCan.setPrefHeight(42);
        btnCan.setVisible(false);
        final Booking[] found = { null };
        btnFind.setOnAction(e -> {
            String pnr = tfPNR.getText().trim().toUpperCase();
            Booking bk = null;
            // ── Search DB first ──
            try {
                java.sql.ResultSet rs = DatabaseManager.getBookingByPNR(pnr);
                if (rs != null && rs.next() && rs.getInt("user_id") == CURRENT_USER.id) {
                    List<Passenger> pList = new ArrayList<>();
                    try {
                        java.sql.ResultSet prs = DatabaseManager.getPassengersByPNR(pnr);
                        if (prs != null)
                            while (prs.next())
                                pList.add(new Passenger(prs.getString("name"), prs.getInt("age"),
                                        prs.getString("gender"), prs.getString("berth_pref"),
                                        prs.getString("nationality"), prs.getString("seat_no")));
                    } catch (Exception pe) {
                    }
                    bk = new Booking(pnr, CURRENT_USER.username, rs.getString("train_no"), rs.getString("train_name"),
                            rs.getString("source"), rs.getString("destination"), rs.getString("journey_date"),
                            rs.getString("seat_class"), rs.getString("quota"), pList, rs.getDouble("total_fare"),
                            rs.getString("payment_mode"));
                    bk.status = rs.getString("status");
                }
            } catch (Exception ex) {
                System.out.println("DB search PNR failed: " + ex.getMessage());
            }
            // ── Fallback to in-memory ──
            if (bk == null)
                bk = BOOKINGS.stream().filter(x -> x.pnr.equals(pnr) && x.username.equals(CURRENT_USER.username))
                        .findFirst().orElse(null);
            if (bk == null) {
                resL.setTextFill(Color.web(C_DANGER));
                resL.setText("❌  No booking found with PNR: " + pnr);
                btnCan.setVisible(false);
            } else if (bk.status.equals("CAN")) {
                resL.setTextFill(Color.web(C_DANGER));
                resL.setText("⚠  This ticket is already cancelled.");
                btnCan.setVisible(false);
            } else {
                found[0] = bk;
                resL.setTextFill(Color.web(C_SUCCESS));
                resL.setText("✔  Found: " + bk.trainName + " | " + bk.source + " → " + bk.destination + " | "
                        + bk.journeyDate + " | " + bk.seatClass + " | ₹" + String.format("%.2f", bk.totalFare));
                btnCan.setVisible(true);
            }
        });
        btnCan.setOnAction(e -> {
            if (found[0] != null) {
                found[0].status = "CAN";
                trainByNo(found[0].trainNo).ifPresent(t -> t.availableSeats += found[0].passengers.size());
                try {
                    DatabaseManager.cancelBooking(found[0].pnr);
                    DatabaseManager.updateAvailableSeats(found[0].trainNo, found[0].passengers.size());
                } catch (Exception ex) {
                    System.out.println("DB cancel err:" + ex.getMessage());
                }
                resL.setTextFill(Color.web(C_SUCCESS));
                resL.setText("✅  Ticket " + found[0].pnr + " cancelled. Refund ₹"
                        + String.format("%.2f", found[0].totalFare * 0.85) + " in 5-7 business days.");
                btnCan.setVisible(false);
            }
        });
        VBox policyBox = new VBox(8);
        policyBox.setPadding(new Insets(14));
        policyBox.setStyle(
                "-fx-background-color:#FFFBEB; -fx-background-radius:8; -fx-border-color:#FCD34D; -fx-border-radius:8; -fx-border-width:1;");
        policyBox.getChildren().addAll(label("📋  Cancellation Policy", 12, true, C_ACCENT),
                label("• Cancel up to 4 hours before departure", 11, false, C_SUBTEXT),
                label("• Clerkage ₹30–₹120 will be deducted", 11, false, C_SUBTEXT),
                label("• Tatkal tickets — no refund on cancellation", 11, false, C_SUBTEXT),
                label("• Refund to original payment in 5–7 days", 11, false, C_SUBTEXT));
        fc.getChildren().addAll(label("PNR Number", 12, true, C_SUBTEXT), tfPNR, btnFind, resL, btnCan);
        outer.getChildren().addAll(fc, policyBox);
        StackPane c = new StackPane(outer);
        c.setStyle("-fx-background-color:" + C_BG + ";");
        StackPane.setAlignment(outer, Pos.TOP_CENTER);
        root.setCenter(c);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // PNR STATUS
    // ════════════════════════════════════════════════════════
    static void showPNRStatus() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("PNR Status"));
        VBox outer = new VBox(20);
        outer.setPadding(new Insets(40, 80, 60, 80));
        outer.setMaxWidth(660);
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setStyle("-fx-background-color:" + C_BG + ";");
        outer.getChildren().addAll(st("📍  PNR Status"),
                label("Check your booking status instantly.", 13, false, C_MUTED));
        VBox fc = new VBox(16);
        fc.setStyle(lc2());
        fc.setPadding(new Insets(24, 28, 24, 28));
        TextField tfPNR = lf("Enter PNR Number");
        tfPNR.setPrefHeight(44);
        VBox resBox = new VBox(10);
        Button btnCheck = pb("CHECK STATUS", C_PRIMARY);
        btnCheck.setPrefHeight(42);
        btnCheck.setOnAction(e -> {
            resBox.getChildren().clear();
            String pnrIn = tfPNR.getText().trim().toUpperCase();
            Booking bk = null;
            // ── Search DB first ──
            try {
                java.sql.ResultSet rs = DatabaseManager.getBookingByPNR(pnrIn);
                if (rs != null && rs.next()) {
                    List<Passenger> pList = new ArrayList<>();
                    try {
                        java.sql.ResultSet prs = DatabaseManager.getPassengersByPNR(pnrIn);
                        if (prs != null)
                            while (prs.next())
                                pList.add(new Passenger(prs.getString("name"), prs.getInt("age"),
                                        prs.getString("gender"), prs.getString("berth_pref"),
                                        prs.getString("nationality"), prs.getString("seat_no")));
                    } catch (Exception pe) {
                    }
                    bk = new Booking(pnrIn, rs.getString("train_no"), rs.getString("train_no"),
                            rs.getString("train_name"), rs.getString("source"), rs.getString("destination"),
                            rs.getString("journey_date"), rs.getString("seat_class"), rs.getString("quota"), pList,
                            rs.getDouble("total_fare"), rs.getString("payment_mode"));
                    bk.status = rs.getString("status");
                }
            } catch (Exception ex) {
                System.out.println("DB PNR check failed: " + ex.getMessage());
            }
            // ── Fallback to in-memory ──
            if (bk == null)
                bk = BOOKINGS.stream().filter(x -> x.pnr.equals(pnrIn)).findFirst().orElse(null);
            if (bk == null) {
                resBox.getChildren().add(label("❌  No booking found for PNR: " + pnrIn, 13, false, C_DANGER));
                return;
            }
            boolean cnf = bk.status.equals("CNF");
            VBox res = new VBox(10);
            res.setPadding(new Insets(14));
            res.setStyle("-fx-background-color:" + (cnf ? C_SUCCESS_L : C_DANGER_L)
                    + "; -fx-background-radius:8; -fx-border-color:" + (cnf ? C_SUCCESS : C_DANGER)
                    + "; -fx-border-radius:8; -fx-border-width:1;");
            res.getChildren().addAll(
                    label("PNR: " + bk.pnr, 14, true, C_TEXT),
                    label("Status: " + (cnf ? "CONFIRMED" : "CANCELLED"), 16, true, cnf ? C_SUCCESS : C_DANGER),
                    new Separator(),
                    label("Train: " + bk.trainName + " (#" + bk.trainNo + ")", 13, false, C_TEXT),
                    label("Route: " + bk.source + " → " + bk.destination, 13, false, C_SUBTEXT),
                    label("Date: " + bk.journeyDate + "  |  Class: " + bk.seatClass, 12, false, C_MUTED),
                    label("Passengers: " + bk.passengers.size() + "  |  Fare: ₹" + String.format("%.2f", bk.totalFare),
                            12, false, C_MUTED),
                    label("Booked: " + bk.bookingDateTime, 11, false, C_MUTED));
            resBox.getChildren().add(res);
        });
        fc.getChildren().addAll(label("PNR Number", 12, true, C_SUBTEXT), tfPNR, btnCheck, resBox);
        outer.getChildren().add(fc);
        StackPane c = new StackPane(outer);
        c.setStyle("-fx-background-color:" + C_BG + ";");
        StackPane.setAlignment(outer, Pos.TOP_CENTER);
        root.setCenter(c);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // SEAT AVAILABILITY
    // ════════════════════════════════════════════════════════
    static void showSeatAvailability() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Seat Availability"));

        VBox outer = new VBox(20);
        outer.setPadding(new Insets(24, 40, 40, 40));
        outer.setStyle("-fx-background-color:" + C_BG + ";");
        outer.getChildren().add(st("💺  Check Seat Availability"));
        outer.getChildren()
                .add(label("Search trains and check real-time seat availability for all classes.", 13, false, C_MUTED));

        // ── Search Form ──
        VBox searchCard = new VBox(16);
        searchCard.setStyle(lc2());
        searchCard.setPadding(new Insets(20, 24, 20, 24));
        searchCard.getChildren().add(label("Search Train Availability", 15, true, C_TEXT));

        HBox row1 = new HBox(14);
        row1.setAlignment(Pos.CENTER_LEFT);

        // FROM
        VBox fromB = new VBox(6);
        fromB.getChildren().add(label("From Station", 12, true, C_SUBTEXT));
        ComboBox<String> cbFrom = lc(getStations());
        cbFrom.setPromptText("Select Origin");
        cbFrom.setPrefWidth(200);
        cbFrom.setPrefHeight(44);
        fromB.getChildren().add(cbFrom);

        // TO — declared before swap button
        VBox toB = new VBox(6);
        toB.getChildren().add(label("To Station", 12, true, C_SUBTEXT));
        ComboBox<String> cbTo = lc(getStations());
        cbTo.setPromptText("Select Destination");
        cbTo.setPrefWidth(200);
        cbTo.setPrefHeight(44);
        toB.getChildren().add(cbTo);

        // Swap button
        Button swapBtn = new Button("⇄");
        swapBtn.setStyle("-fx-background-color:" + C_PRIMARY_L + "; -fx-text-fill:" + C_PRIMARY
                + "; -fx-font-size:16px; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:8 12; -fx-border-color:"
                + C_PRIMARY + "; -fx-border-radius:8;");
        swapBtn.setOnAction(e -> {
            String t = cbFrom.getValue();
            cbFrom.setValue(cbTo.getValue());
            cbTo.setValue(t);
        });
        VBox swapBox = new VBox(0);
        swapBox.setAlignment(Pos.BOTTOM_CENTER);
        swapBox.setPadding(new Insets(0, 0, 2, 0));
        swapBox.getChildren().add(swapBtn);

        // Date
        VBox dateB = new VBox(6);
        dateB.getChildren().add(label("Journey Date", 12, true, C_SUBTEXT));
        DatePicker dp = new DatePicker(LocalDate.now().plusDays(1));
        dp.setPrefWidth(170);
        dp.setPrefHeight(44);
        styleDp(dp);
        dateB.getChildren().add(dp);

        // Class filter
        VBox classB = new VBox(6);
        classB.getChildren().add(label("Class (Optional)", 12, true, C_SUBTEXT));
        ComboBox<String> cbClass = lc(FXCollections.observableArrayList(
                "All Classes", "1A - AC First Class", "2A - AC 2-Tier", "3A - AC 3-Tier",
                "3E - AC 3-Tier Economy", "SL - Sleeper Class", "CC - AC Chair Car", "2S - Second Sitting",
                "GN - General"));
        cbClass.setValue("All Classes");
        cbClass.setPrefWidth(190);
        cbClass.setPrefHeight(44);
        classB.getChildren().add(cbClass);

        Button btnSearch = pb("🔍  CHECK AVAILABILITY", C_PRIMARY);
        btnSearch.setPrefHeight(44);
        btnSearch.setPrefWidth(200);
        VBox searchBtnBox = new VBox(0);
        searchBtnBox.setAlignment(Pos.BOTTOM_CENTER);
        searchBtnBox.getChildren().add(btnSearch);

        row1.getChildren().addAll(fromB, swapBox, toB, dateB, classB, searchBtnBox);
        searchCard.getChildren().add(row1);
        outer.getChildren().add(searchCard);

        // ── Results Area ──
        VBox resultsArea = new VBox(14);
        resultsArea.setStyle("-fx-background-color:" + C_BG + ";");

        btnSearch.setOnAction(e -> {
            resultsArea.getChildren().clear();
            String from = cbFrom.getValue(), to = cbTo.getValue();
            String date = dp.getValue() != null ? dp.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
            String selCls = cbClass.getValue();

            if (from == null || to == null) {
                resultsArea.getChildren().add(label("⚠  Please select From and To stations.", 13, false, C_DANGER));
                return;
            }
            if (from.equals(to)) {
                resultsArea.getChildren()
                        .add(label("⚠  From and To stations cannot be the same.", 13, false, C_DANGER));
                return;
            }

            List<Train> found = TRAINS.stream()
                    .filter(t -> t.source.equalsIgnoreCase(from) && t.destination.equalsIgnoreCase(to))
                    .collect(Collectors.toList());

            if (found.isEmpty()) {
                VBox noRes = new VBox(10);
                noRes.setAlignment(Pos.CENTER);
                noRes.setPadding(new Insets(40));
                noRes.setStyle(lc2());
                noRes.getChildren().addAll(label("🚫", 40, false, C_MUTED),
                        label("No trains found for " + from + " → " + to, 16, false, C_MUTED),
                        label("Try different stations.", 12, false, C_MUTED));
                resultsArea.getChildren().add(noRes);
                return;
            }

            // Results header
            HBox resHdr = new HBox(16);
            resHdr.setAlignment(Pos.CENTER_LEFT);
            Label resCount = label(found.size() + " Train(s) Found", 14, true, C_SURFACE);
            resCount.setStyle("-fx-background-color:" + C_PRIMARY + "; -fx-background-radius:6; -fx-padding:4 12;");
            resHdr.getChildren().addAll(label("Results for  " + from + " → " + to + " | " + date, 14, true, C_TEXT),
                    resCount);
            resultsArea.getChildren().add(resHdr);

            for (Train t : found) {
                // Train card
                VBox tCard = new VBox(0);
                tCard.setStyle(lc2());

                // Train header
                HBox th = new HBox(12);
                th.setPadding(new Insets(12, 18, 10, 18));
                th.setAlignment(Pos.CENTER_LEFT);
                th.setStyle("-fx-background-color:#F8FAFF; -fx-background-radius:10 10 0 0; -fx-border-color:"
                        + C_BORDER + "; -fx-border-width:0 0 1 0;");
                Label tNoL = label("#" + t.trainNo, 12, true, C_PRIMARY);
                tNoL.setStyle("-fx-background-color:" + C_PRIMARY_L + "; -fx-background-radius:4; -fx-padding:3 8;");
                String typeColor = t.trainType.equals("Rajdhani") ? C_ORANGE
                        : t.trainType.equals("Superfast") ? C_PRIMARY : C_SUCCESS;
                Label typeL = label(t.trainType, 11, true, C_SURFACE);
                typeL.setStyle("-fx-background-color:" + typeColor + "; -fx-background-radius:4; -fx-padding:3 8;");
                HBox tsp2 = new HBox();
                HBox.setHgrow(tsp2, Priority.ALWAYS);
                Label daysL = label("🗓  " + t.days, 11, false, C_MUTED);
                th.getChildren().addAll(tNoL, label(t.trainName, 15, true, C_TEXT), typeL, tsp2, daysL);

                // Route timing
                HBox rr = new HBox(0);
                rr.setPadding(new Insets(12, 18, 12, 18));
                rr.setAlignment(Pos.CENTER);
                VBox dep = new VBox(3);
                dep.setAlignment(Pos.CENTER_LEFT);
                dep.getChildren().addAll(label(t.departure, 24, true, C_TEXT), label(t.source, 13, true, C_SUBTEXT));
                VBox mid = new VBox(4);
                mid.setAlignment(Pos.CENTER);
                mid.setPrefWidth(140);
                mid.getChildren().addAll(label("────────►", 14, false, C_BORDER), label("Direct", 10, false, C_MUTED));
                VBox arr = new VBox(3);
                arr.setAlignment(Pos.CENTER_RIGHT);
                arr.getChildren().addAll(label(t.arrival, 24, true, C_TEXT), label(t.destination, 13, true, C_SUBTEXT));
                HBox.setHgrow(mid, Priority.ALWAYS);
                rr.getChildren().addAll(dep, mid, arr);

                // ── SEAT CLASS AVAILABILITY TABLE ──
                VBox classTable = new VBox(0);
                classTable.setStyle("-fx-border-color:" + C_BORDER + "; -fx-border-width:1 0 0 0;");

                // Table header
                HBox tableHdr = new HBox(0);
                tableHdr.setPadding(new Insets(10, 18, 10, 18));
                tableHdr.setStyle("-fx-background-color:#EFF6FF;");
                for (String[] h : new String[][] { { "Class", "160" }, { "Available Seats", "160" },
                        { "Fare (per person)", "160" }, { "Status", "130" }, { "Action", "120" } }) {
                    Label hl = label(h[0], 11, true, C_PRIMARY);
                    hl.setPrefWidth(Integer.parseInt(h[1]));
                    tableHdr.getChildren().add(hl);
                }
                classTable.getChildren().add(tableHdr);

                // One row per seat class
                for (String[] sc : SC) {
                    // Skip if class filter is applied and doesn't match
                    if (!selCls.equals("All Classes") && !selCls.startsWith(sc[0]))
                        continue;

                    double fare = t.baseFare * Double.parseDouble(sc[2]);
                    int avail = t.availableSeats; // In real system each class has own count
                    // Simulate per-class availability
                    int classAvail = (int) (avail * (0.6 + Math.random() * 0.4));
                    String statusText, statusColor, statusBg;
                    if (classAvail > 50) {
                        statusText = "AVAILABLE";
                        statusColor = C_SUCCESS;
                        statusBg = C_SUCCESS_L;
                    } else if (classAvail > 10) {
                        statusText = "AVAILABLE";
                        statusColor = "#D97706";
                        statusBg = "#FFFBEB";
                    } else if (classAvail > 0) {
                        statusText = "AVAILABLE";
                        statusColor = C_DANGER;
                        statusBg = C_DANGER_L;
                    } else {
                        statusText = "WAITLIST";
                        statusColor = "#7C3AED";
                        statusBg = "#F5F3FF";
                        classAvail = 0;
                    }

                    HBox row = new HBox(0);
                    row.setPadding(new Insets(10, 18, 10, 18));
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-background-color:" + C_SURFACE + "; -fx-border-color:" + C_BORDER
                            + "; -fx-border-width:0 0 1 0;");
                    row.setOnMouseEntered(ev -> row.setStyle("-fx-background-color:" + C_BG + "; -fx-border-color:"
                            + C_BORDER + "; -fx-border-width:0 0 1 0;"));
                    row.setOnMouseExited(ev -> row.setStyle("-fx-background-color:" + C_SURFACE + "; -fx-border-color:"
                            + C_BORDER + "; -fx-border-width:0 0 1 0;"));

                    // Class name cell
                    HBox clsCell = new HBox(8);
                    clsCell.setAlignment(Pos.CENTER_LEFT);
                    clsCell.setPrefWidth(160);
                    Label codeL = label(sc[0], 13, true, sc[4]);
                    codeL.setStyle("-fx-background-color:" + sc[4]
                            + "22; -fx-background-radius:4; -fx-padding:3 8; -fx-text-fill:" + sc[4] + ";");
                    Label nameL = label(sc[1], 11, false, C_SUBTEXT);
                    clsCell.getChildren().addAll(codeL);

                    // Available seats cell
                    Label availL = label(classAvail + " Seats", 13, true, classAvail > 0 ? C_TEXT : C_MUTED);
                    availL.setPrefWidth(160);

                    // Fare cell
                    Label fareL = label("₹" + String.format("%.0f", fare), 13, true, C_ACCENT);
                    fareL.setPrefWidth(160);

                    // Status badge
                    Label statusL = label(statusText, 11, true, statusColor);
                    statusL.setStyle("-fx-background-color:" + statusBg
                            + "; -fx-background-radius:6; -fx-padding:4 10; -fx-text-fill:" + statusColor + ";");
                    statusL.setPrefWidth(130);

                    // Book button
                    Button btnBook = new Button(classAvail > 0 ? "BOOK NOW" : "JOIN WAITLIST");
                    btnBook.setPrefWidth(110);
                    btnBook.setPrefHeight(32);
                    String btnColor = classAvail > 0 ? C_PRIMARY : "#7C3AED";
                    btnBook.setStyle("-fx-background-color:" + btnColor
                            + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:11px; -fx-background-radius:6; -fx-cursor:hand;");
                    btnBook.setOnAction(ev -> showBooking(t, date, sc[0] + " - " + sc[1], "General", 1));

                    row.getChildren().addAll(clsCell, availL, fareL, statusL, btnBook);
                    classTable.getChildren().add(row);
                }

                // Refresh note
                Label refreshNote = label("  ℹ  Availability updates every few minutes. Seat count is indicative.", 11,
                        false, C_MUTED);
                refreshNote.setPadding(new Insets(8, 18, 10, 18));
                refreshNote.setStyle("-fx-background-color:#F8FAFF;");

                tCard.getChildren().addAll(th, rr, classTable, refreshNote);
                resultsArea.getChildren().add(tCard);
            }
        });

        outer.getChildren().add(resultsArea);
        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // LIVE TRAIN TRACKING
    // ════════════════════════════════════════════════════════
    static void showLiveTracking() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Live Train Tracking"));

        VBox outer = new VBox(20);
        outer.setPadding(new Insets(24, 40, 40, 40));
        outer.setStyle("-fx-background-color:" + C_BG + ";");

        // Header
        HBox hdrRow = new HBox(14);
        hdrRow.setAlignment(Pos.CENTER_LEFT);
        VBox hdrText = new VBox(4);
        hdrText.getChildren().addAll(st("🛰  Live Train Tracking"),
                label("Real-time position tracking via NTES-style simulation. Updates every 30 seconds.", 13, false,
                        C_MUTED));
        HBox livePill = new HBox(6);
        livePill.setAlignment(Pos.CENTER);
        livePill.setStyle("-fx-background-color:#0D3B1F; -fx-background-radius:20; -fx-padding:6 14;");
        Region liveDot2 = new Region();
        liveDot2.setPrefWidth(8);
        liveDot2.setPrefHeight(8);
        liveDot2.setStyle("-fx-background-color:#00E676; -fx-background-radius:50;");
        Label liveText = label("LIVE", 11, true, "#00E676");
        livePill.getChildren().addAll(liveDot2, liveText);
        HBox hSp = new HBox();
        HBox.setHgrow(hSp, Priority.ALWAYS);
        hdrRow.getChildren().addAll(hdrText, hSp, livePill);
        outer.getChildren().add(hdrRow);

        // Search Form
        VBox searchCard = new VBox(16);
        searchCard.setStyle(lc2());
        searchCard.setPadding(new Insets(20, 24, 20, 24));
        HBox searchRow = new HBox(14);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        VBox trainNoBox = new VBox(6);
        trainNoBox.getChildren().add(label("Train Number / Name", 12, true, C_SUBTEXT));
        ComboBox<String> cbTrain = lc(FXCollections.observableArrayList(
                TRAINS.stream().map(t -> "#" + t.trainNo + " — " + t.trainName).collect(Collectors.toList())));
        cbTrain.setPromptText("Select train");
        cbTrain.setPrefWidth(340);
        cbTrain.setPrefHeight(44);
        cbTrain.setEditable(true);
        trainNoBox.getChildren().add(cbTrain);

        VBox dateBox2 = new VBox(6);
        dateBox2.getChildren().add(label("Journey Date", 12, true, C_SUBTEXT));
        DatePicker dp2 = new DatePicker(LocalDate.now());
        dp2.setPrefWidth(180);
        dp2.setPrefHeight(44);
        styleDp(dp2);
        dateBox2.getChildren().add(dp2);

        Button btnTrack = pb("🛰  TRACK NOW", C_PRIMARY);
        btnTrack.setPrefHeight(44);
        btnTrack.setPrefWidth(160);
        VBox btnBox2 = new VBox(0);
        btnBox2.setAlignment(Pos.BOTTOM_CENTER);
        btnBox2.getChildren().add(btnTrack);

        searchRow.getChildren().addAll(trainNoBox, dateBox2, btnBox2);
        searchCard.getChildren().addAll(label("Enter Train Details", 15, true, C_TEXT), searchRow);
        outer.getChildren().add(searchCard);

        // Quick-track booked trains
        List<Booking> userBookings = BOOKINGS.stream()
                .filter(bk -> bk.username.equals(CURRENT_USER.username) && bk.status.equals("CNF"))
                .collect(Collectors.toList());
        if (!userBookings.isEmpty()) {
            VBox quickBox = new VBox(8);
            quickBox.setStyle(lc2());
            quickBox.setPadding(new Insets(14, 18, 14, 18));
            quickBox.getChildren().add(label("🎫  Your Active Bookings — Quick Track", 13, true, C_TEXT));
            HBox qRow = new HBox(10);
            qRow.setAlignment(Pos.CENTER_LEFT);
            for (Booking bk : userBookings.stream().limit(5).collect(Collectors.toList())) {
                Button qBtn = new Button("🚆 " + bk.trainName + " | " + bk.journeyDate);
                qBtn.setStyle("-fx-background-color:" + C_PRIMARY_L + "; -fx-text-fill:" + C_PRIMARY
                        + "; -fx-font-size:11px; -fx-background-radius:6; -fx-border-color:" + C_PRIMARY
                        + "55; -fx-border-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
                qBtn.setOnAction(ev -> {
                    cbTrain.getItems().stream()
                            .filter(item -> item.contains(bk.trainNo)).findFirst()
                            .ifPresent(item -> cbTrain.setValue(item));
                    dp2.setValue(LocalDate.now());
                    btnTrack.fire();
                });
                qRow.getChildren().add(qBtn);
            }
            quickBox.getChildren().add(qRow);
            outer.getChildren().add(quickBox);
        }

        VBox resultArea = new VBox(16);
        resultArea.setStyle("-fx-background-color:" + C_BG + ";");

        btnTrack.setOnAction(e -> {
            resultArea.getChildren().clear();
            if (cbTrain.getValue() == null || cbTrain.getValue().isBlank()) {
                resultArea.getChildren().add(label("⚠  Please select a train.", 13, false, C_DANGER));
                return;
            }
            String sel = cbTrain.getValue();
            String selNo = "";
            if (sel.contains("—"))
                selNo = sel.substring(1, sel.indexOf("—")).trim();
            else
                selNo = sel.replaceAll("[^0-9]", "");
            final String fSelNo = selNo;
            Train selTrain = TRAINS.stream().filter(t -> t.trainNo.equals(fSelNo)).findFirst().orElse(null);
            if (selTrain == null) {
                resultArea.getChildren()
                        .add(label("❌  Train not found. Try selecting from dropdown.", 13, false, C_DANGER));
                return;
            }

            String selDate = dp2.getValue() != null ? dp2.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    : "Today";
            boolean isUserBooking = BOOKINGS.stream().anyMatch(bk -> bk.username.equals(CURRENT_USER.username) &&
                    bk.trainNo.equals(selTrain.trainNo) && bk.status.equals("CNF"));

            // ── Comprehensive route map ──
            Map<String, String[][]> routeMap = new HashMap<>();
            routeMap.put("Chennai-New Delhi", new String[][] {
                    { "Chennai Central", "22:00", "0" }, { "Katpadi", "23:45", "145" }, { "Renigunta", "01:30", "248" },
                    { "Cuddapah", "03:15", "370" }, { "Guntakal", "05:30", "510" }, { "Wadi", "08:00", "690" },
                    { "Raichur", "09:15", "750" }, { "Yadgir", "10:30", "820" }, { "Gulbarga", "11:45", "900" },
                    { "Solapur", "14:00", "1060" }, { "Daund", "16:30", "1190" }, { "Pune", "17:45", "1260" },
                    { "Lonavala", "18:45", "1320" }, { "Kalyan", "20:00", "1380" }, { "Nagpur", "01:00", "1567" },
                    { "Bhopal", "07:30", "1929" }, { "Jhansi", "11:00", "2171" }, { "Agra", "14:00", "2365" },
                    { "New Delhi", "16:30", "2182" }
            });
            routeMap.put("Chennai-Madurai", new String[][] {
                    { "Chennai Egmore", "21:30", "0" }, { "Chengalpattu", "22:30", "50" },
                    { "Villupuram", "00:15", "160" }, { "Trichy", "02:45", "330" },
                    { "Dindigul", "04:00", "415" }, { "Madurai", "04:30", "462" }
            });
            routeMap.put("Chennai-Coimbatore", new String[][] {
                    { "Chennai Central", "23:00", "0" }, { "Arakkonam", "00:15", "68" },
                    { "Salem", "01:45", "216" }, { "Erode", "02:45", "287" }, { "Tiruppur", "03:30", "333" },
                    { "Coimbatore", "04:15", "395" }
            });
            routeMap.put("Chennai-Bangalore", new String[][] {
                    { "Chennai Central", "06:00", "0" }, { "Katpadi", "08:30", "145" },
                    { "Jolarpettai", "09:45", "202" }, { "Bangarapet", "10:45", "258" },
                    { "Bangalore", "11:00", "346" }
            });
            routeMap.put("Mumbai-Chennai", new String[][] {
                    { "Mumbai CST", "08:00", "0" }, { "Pune", "10:30", "192" }, { "Miraj", "14:00", "470" },
                    { "Hubli", "17:45", "643" }, { "Dharwad", "18:30", "692" }, { "Davangere", "20:15", "775" },
                    { "Arsikere", "22:00", "870" }, { "Salem", "01:00", "1124" }, { "Katpadi", "03:00", "1220" },
                    { "Chennai Central", "05:30", "1338" }
            });
            routeMap.put("Chennai-Rameswaram", new String[][] {
                    { "Chennai Egmore", "21:30", "0" }, { "Villupuram", "00:00", "160" },
                    { "Trichy", "02:30", "330" }, { "Karaikudi", "03:30", "390" },
                    { "Manamadurai", "04:00", "425" }, { "Rameswaram", "05:15", "500" }
            });
            routeMap.put("Thiruvananthapuram-Chennai", new String[][] {
                    { "Thiruvananthapuram", "15:30", "0" }, { "Kollam", "16:45", "71" },
                    { "Ernakulam", "19:00", "207" }, { "Thrissur", "20:30", "278" }, { "Palakkad", "22:00", "343" },
                    { "Coimbatore", "00:00", "420" }, { "Erode", "01:30", "510" },
                    { "Salem", "02:30", "571" }, { "Katpadi", "04:00", "643" }, { "Chennai Central", "05:00", "726" }
            });
            routeMap.put("Mumbai-Kanyakumari", new String[][] {
                    { "Mumbai CST", "11:00", "0" }, { "Pune", "13:30", "192" }, { "Miraj", "17:00", "470" },
                    { "Bangalore", "22:00", "1000" }, { "Salem", "01:30", "1250" },
                    { "Madurai", "04:00", "1530" }, { "Tirunelveli", "06:00", "1600" },
                    { "Nagercoil", "06:45", "1640" }, { "Kanyakumari", "07:30", "1648" }
            });
            routeMap.put("Howrah-New Delhi", new String[][] {
                    { "Howrah", "14:05", "0" }, { "Burdwan", "15:45", "107" }, { "Dhanbad", "18:30", "261" },
                    { "Gaya", "20:30", "376" }, { "Mughalsarai", "22:00", "450" },
                    { "Allahabad", "23:30", "525" }, { "Kanpur", "01:30", "643" },
                    { "Agra", "04:00", "820" }, { "New Delhi", "09:55", "1441" }
            });
            routeMap.put("Mumbai-New Delhi", new String[][] {
                    { "Mumbai Central", "17:40", "0" }, { "Surat", "20:45", "263" },
                    { "Vadodara", "22:30", "392" }, { "Ratlam", "01:15", "626" },
                    { "Nagda", "02:00", "656" }, { "Kota", "04:30", "810" },
                    { "Sawai Madhopur", "05:30", "890" }, { "Mathura", "07:45", "1000" },
                    { "New Delhi", "08:35", "1389" }
            });
            routeMap.put("Bangalore-New Delhi", new String[][] {
                    { "Bangalore", "20:00", "0" }, { "Tumkur", "21:30", "74" }, { "Arsikere", "23:00", "170" },
                    { "Davangere", "01:00", "268" }, { "Hubli", "03:00", "387" }, { "Gadag", "04:15", "453" },
                    { "Hospet", "05:30", "512" }, { "Guntakal", "07:30", "621" }, { "Renigunta", "10:00", "766" },
                    { "Wadi", "13:00", "900" }, { "Solapur", "16:00", "1050" },
                    { "Pune", "20:00", "1200" }, { "Mumbai CST", "23:00", "1400" },
                    { "Nagpur", "06:00", "1700" }, { "Bhopal", "12:00", "2000" },
                    { "New Delhi", "05:30", "2444" }
            });
            routeMap.put("Chennai-Ahmedabad", new String[][] {
                    { "Chennai Central", "08:30", "0" }, { "Renigunta", "13:00", "248" },
                    { "Guntur", "15:30", "490" }, { "Nagpur", "22:00", "1000" },
                    { "Surat", "07:00", "1680" }, { "Vadodara", "09:00", "1760" },
                    { "Ahmedabad", "11:30", "1848" }
            });

            String routeKey = selTrain.source + "-" + selTrain.destination;
            String[][] stationList = routeMap.getOrDefault(routeKey, new String[][] {
                    { selTrain.source, selTrain.departure, "0" },
                    { selTrain.destination, selTrain.arrival, "500" }
            });

            // Calculate position from real time
            LocalTime nowT = LocalTime.now();
            int currentStationIdx = 0;
            try {
                String[] dp3 = selTrain.departure.split(":");
                LocalTime dep = LocalTime.of(Integer.parseInt(dp3[0]), Integer.parseInt(dp3[1]));
                long minsElapsed = java.time.Duration.between(dep, nowT).toMinutes();
                if (minsElapsed < 0)
                    minsElapsed += 1440;
                int totalStations = stationList.length;
                currentStationIdx = (int) (minsElapsed / (1440.0 / totalStations));
                currentStationIdx = Math.max(0, Math.min(currentStationIdx, totalStations - 2));
            } catch (Exception ex) {
                currentStationIdx = Math.min(2, stationList.length - 2);
            }

            // ── STATUS HEADER CARD ──
            VBox statusCard = new VBox(0);
            statusCard.setStyle(lc2());
            HBox sHdr = new HBox(14);
            sHdr.setPadding(new Insets(16, 22, 16, 22));
            sHdr.setAlignment(Pos.CENTER_LEFT);
            sHdr.setStyle("-fx-background-color:" + C_NAV + "; -fx-background-radius:10 10 0 0;");
            Label tNoB = label("#" + selTrain.trainNo, 12, true, C_SURFACE);
            tNoB.setStyle("-fx-background-color:rgba(255,255,255,0.15); -fx-background-radius:4; -fx-padding:3 10;");
            Label tNameL = label(selTrain.trainName, 17, true, C_SURFACE);
            Label typeL2 = label(selTrain.trainType, 10, true, C_NAV);
            typeL2.setStyle("-fx-background-color:#FFA500; -fx-background-radius:4; -fx-padding:2 7;");
            HBox hSp2 = new HBox();
            HBox.setHgrow(hSp2, Priority.ALWAYS);
            if (isUserBooking) {
                Label myL = label("🎫 YOUR BOOKING", 10, true, "#FFA500");
                myL.setStyle("-fx-background-color:rgba(255,165,0,0.2); -fx-background-radius:6; -fx-padding:3 10;");
                sHdr.getChildren().addAll(tNoB, tNameL, typeL2, myL, hSp2);
            } else
                sHdr.getChildren().addAll(tNoB, tNameL, typeL2, hSp2);
            HBox runBadge2 = new HBox(6);
            runBadge2.setAlignment(Pos.CENTER);
            runBadge2.setStyle("-fx-background-color:#0D3B1F; -fx-background-radius:20; -fx-padding:5 14;");
            Region runDot2 = new Region();
            runDot2.setPrefWidth(9);
            runDot2.setPrefHeight(9);
            runDot2.setStyle("-fx-background-color:#00E676; -fx-background-radius:50;");
            runBadge2.getChildren().addAll(runDot2, label("LIVE RUNNING", 10, true, "#00E676"));
            Label dateL = label("📅 " + selDate, 12, false, "#BFDBFE");
            dateL.setPadding(new Insets(0, 12, 0, 0));
            sHdr.getChildren().addAll(dateL, runBadge2);

            // Route bar with animated progress
            HBox routeBar = new HBox(0);
            routeBar.setPadding(new Insets(20, 24, 18, 24));
            routeBar.setAlignment(Pos.CENTER);
            routeBar.setStyle("-fx-background-color:#F8FAFF;");
            VBox srcV2 = new VBox(5);
            srcV2.setAlignment(Pos.CENTER_LEFT);
            srcV2.getChildren().addAll(
                    label(selTrain.source.toUpperCase().length() > 10 ? selTrain.source.substring(0, 10).toUpperCase()
                            : selTrain.source.toUpperCase(), 26, true, C_TEXT),
                    label("Origin", 10, false, C_MUTED), label("Dep: " + selTrain.departure, 13, true, C_PRIMARY));
            VBox midV = new VBox(8);
            midV.setAlignment(Pos.CENTER);
            midV.setPrefWidth(260);
            HBox.setHgrow(midV, Priority.ALWAYS);
            double progress = (double) (currentStationIdx + 1) / stationList.length;
            int totalKm = stationList.length > 1 ? Integer.parseInt(stationList[stationList.length - 1][2]) : 800;
            int coveredKm = currentStationIdx < stationList.length ? Integer.parseInt(stationList[currentStationIdx][2])
                    : 0;
            // Animated progress bar (simulated with JavaFX)
            StackPane progBar = new StackPane();
            progBar.setPrefHeight(10);
            progBar.setMaxWidth(240);
            Region progBg = new Region();
            progBg.setPrefHeight(10);
            progBg.setMaxWidth(240);
            progBg.setStyle("-fx-background-color:#E2E8F0; -fx-background-radius:5;");
            Region progFill = new Region();
            progFill.setPrefHeight(10);
            progFill.setPrefWidth(240 * progress);
            progFill.setStyle("-fx-background-color:linear-gradient(to right," + C_SUCCESS
                    + ",#34D399); -fx-background-radius:5;");
            // Train icon on progress
            Label trainIconL = label("🚂", 14, false, "#FFFFFF");
            StackPane.setAlignment(progFill, Pos.CENTER_LEFT);
            StackPane.setAlignment(trainIconL, Pos.CENTER_LEFT);
            trainIconL.setTranslateX(Math.max(0, 240 * progress - 18));
            progBar.getChildren().addAll(progBg, progFill, trainIconL);
            midV.getChildren().addAll(
                    label(String.format("%.0f%%", progress * 100) + " complete", 11, true, C_SUCCESS),
                    progBar,
                    label(coveredKm + " km / " + totalKm + " km", 11, false, C_MUTED));
            VBox dstV2 = new VBox(5);
            dstV2.setAlignment(Pos.CENTER_RIGHT);
            dstV2.getChildren().addAll(
                    label(selTrain.destination.toUpperCase().length() > 10
                            ? selTrain.destination.substring(0, 10).toUpperCase()
                            : selTrain.destination.toUpperCase(), 26, true, C_TEXT),
                    label("Destination", 10, false, C_MUTED), label("Arr: " + selTrain.arrival, 13, true, C_PRIMARY));
            routeBar.getChildren().addAll(srcV2, midV, dstV2);

            // Current position strip
            String curStName = stationList[currentStationIdx][0];
            String nextStName = currentStationIdx + 1 < stationList.length ? stationList[currentStationIdx + 1][0]
                    : "Destination";
            String nextStTime = currentStationIdx + 1 < stationList.length ? stationList[currentStationIdx + 1][1]
                    : selTrain.arrival;
            HBox curInfo = new HBox(0);
            curInfo.setPadding(new Insets(14, 22, 14, 22));
            curInfo.setStyle(
                    "-fx-background-color:#F0FDF4; -fx-border-color:" + C_SUCCESS + "; -fx-border-width:1 0 0 0;");
            curInfo.setAlignment(Pos.CENTER_LEFT);
            String[] ciLabels = { "📍", "Current Position", "Near " + curStName };
            String[] ciNext = { "🚉", "Next Station", nextStName + " @ " + nextStTime };
            String[] ciSpeed = { "⚡", "Speed",
                    selTrain.trainType.equals("Rajdhani") || selTrain.trainType.equals("Duronto") ? "~130 km/h"
                            : selTrain.trainType.equals("Superfast") ? "~110 km/h" : "~80 km/h" };
            String[] ciStatus = { "🕐", "Status", "On Time ✓" };
            for (String[][] ci3 : new String[][][] { { ciLabels }, { ciNext }, { ciSpeed }, { ciStatus } }) {
                String[] d = ci3[0];
                VBox cv = new VBox(3);
                cv.setAlignment(Pos.CENTER_LEFT);
                cv.setPadding(new Insets(0, 30, 0, 0));
                cv.getChildren().addAll(label(d[0] + " " + d[1], 10, true, C_MUTED), label(d[2], 13, true, C_TEXT));
                curInfo.getChildren().add(cv);
            }
            statusCard.getChildren().addAll(sHdr, routeBar, curInfo);
            resultArea.getChildren().add(statusCard);

            // ── VISUAL ROUTE TIMELINE ──
            VBox timelineCard = new VBox(0);
            timelineCard.setStyle(lc2());
            HBox tlHdr = new HBox(14);
            tlHdr.setPadding(new Insets(16, 22, 14, 22));
            tlHdr.setAlignment(Pos.CENTER_LEFT);
            tlHdr.setStyle("-fx-background-color:#F8FAFF; -fx-background-radius:10 10 0 0; -fx-border-color:" + C_BORDER
                    + "; -fx-border-width:0 0 1 0;");
            tlHdr.getChildren().addAll(
                    label("🗺  Station-by-Station Progress", 15, true, C_TEXT),
                    label("(" + stationList.length + " stops)", 12, false, C_MUTED));
            timelineCard.getChildren().add(tlHdr);

            ScrollPane tlScroll = new ScrollPane();
            VBox tlContent = new VBox(0);
            tlContent.setPadding(new Insets(14, 22, 14, 22));

            for (int si = 0; si < stationList.length; si++) {
                String stName = stationList[si][0];
                String stTime = stationList[si][1];
                String stKm = stationList[si][2];
                boolean isCurrent = si == currentStationIdx;
                boolean isDone = si < currentStationIdx;
                boolean isFirst = si == 0, isLast = si == stationList.length - 1;

                HBox stRow = new HBox(14);
                stRow.setAlignment(Pos.CENTER_LEFT);
                stRow.setPadding(new Insets(10, 10, 10, 10));
                String stBg = isCurrent ? C_PRIMARY_L : isDone ? "#F0FDF4" : "#FAFAFA";
                String stBorder = isCurrent ? C_PRIMARY : isDone ? C_SUCCESS : C_BORDER;
                stRow.setStyle("-fx-background-color:" + stBg + "; -fx-background-radius:8; -fx-border-color:"
                        + stBorder + "; -fx-border-radius:8; -fx-border-width:1; -fx-margin:4px;");
                stRow.setMinHeight(isCurrent ? 54 : 42);

                // Connector line visual
                VBox connector = new VBox(0);
                connector.setAlignment(Pos.TOP_CENTER);
                connector.setPrefWidth(30);
                Label dotIcon = label(isCurrent ? "🚂" : isDone ? "✅" : isLast ? "🏁" : "○", isCurrent ? 18 : 16, false,
                        isCurrent ? C_PRIMARY : isDone ? C_SUCCESS : "#9CA3AF");
                if (!isLast) {
                    Region line = new Region();
                    line.setPrefWidth(2);
                    line.setPrefHeight(18);
                    line.setStyle(
                            "-fx-background-color:" + (isDone ? C_SUCCESS : "#E2E8F0") + "; -fx-background-radius:1;");
                    connector.getChildren().addAll(dotIcon, line);
                } else
                    connector.getChildren().add(dotIcon);

                VBox stInfo = new VBox(3);
                HBox.setHgrow(stInfo, Priority.ALWAYS);
                String nameColor = isCurrent ? C_PRIMARY : isFirst || isLast ? C_TEXT : isDone ? C_SUCCESS : C_SUBTEXT;
                String suffix = isFirst ? " (Origin)" : isLast ? " (Destination)" : isCurrent ? " ◄ TRAIN IS HERE" : "";
                Label stNameL2 = label(stName + suffix, isCurrent ? 15 : 13, isCurrent || isFirst || isLast, nameColor);
                HBox timKmRow = new HBox(20);
                timKmRow.setAlignment(Pos.CENTER_LEFT);
                Label schL = label("Scheduled: " + stTime, 11, false, C_MUTED);
                String actTxt = isDone ? "Departed ✓"
                        : isCurrent ? "Currently here — departing soon" : "Expected: " + stTime;
                Label actL = label(actTxt, 11, false, isDone ? C_SUCCESS : isCurrent ? C_PRIMARY : C_MUTED);
                Label kmL = label(Integer.parseInt(stKm) > 0 ? stKm + " km" : "Start", 11, false, C_MUTED);
                timKmRow.getChildren().addAll(schL, actL, kmL);
                stInfo.getChildren().addAll(stNameL2, timKmRow);

                String badgeTxt = isCurrent ? "► CURRENT" : isDone ? "DEPARTED" : isLast ? "DESTINATION" : "SCHEDULED";
                String badgeColor = isCurrent ? C_PRIMARY : isDone ? C_SUCCESS : isLast ? "#059669" : "#6B7280";
                Label badge = label(badgeTxt, 9, true, badgeColor);
                badge.setStyle("-fx-background-color:" + badgeColor
                        + "18; -fx-background-radius:5; -fx-padding:3 9; -fx-text-fill:" + badgeColor + ";");

                stRow.getChildren().addAll(connector, stInfo, badge);
                VBox wrapper = new VBox(3);
                wrapper.getChildren().add(stRow);
                tlContent.getChildren().add(wrapper);
            }
            tlScroll.setContent(tlContent);
            tlScroll.setFitToWidth(true);
            tlScroll.setPrefHeight(350);
            tlScroll.setStyle("-fx-background:" + C_SURFACE + "; -fx-background-color:" + C_SURFACE + ";");
            timelineCard.getChildren().add(tlScroll);
            resultArea.getChildren().add(timelineCard);

            // ── LIVE INFO CARDS ──
            HBox liveCards = new HBox(14);
            for (String[] lc4 : new String[][] {
                    { "🚂", "Train Type", selTrain.trainType, "#1A56DB", "#EBF0FF" },
                    { "📏", "Distance Covered", coveredKm + " / " + totalKm + " km", "#057A55", "#DEF7EC" },
                    { "⚡", "Est. Speed",
                            selTrain.trainType.equals("Rajdhani") || selTrain.trainType.equals("Duronto") ? "~130 km/h"
                                    : selTrain.trainType.equals("Superfast") ? "~110 km/h" : "~80 km/h",
                            "#D97706", "#FFFBEB" },
                    { "🛰", "GPS Signal", "NTES — Strong", "#7C3AED", "#F5F3FF" },
                    { "⏱", "ETA", nextStTime, "#059669", "#F0FDF4" }
            }) {
                VBox lcc = new VBox(8);
                lcc.setStyle("-fx-background-color:" + lc4[4] + "; -fx-background-radius:10; -fx-border-color:" + lc4[3]
                        + "44; -fx-border-radius:10; -fx-border-width:1; -fx-padding:14 16;");
                HBox.setHgrow(lcc, Priority.ALWAYS);
                lcc.setAlignment(Pos.CENTER_LEFT);
                lcc.getChildren().addAll(label(lc4[0] + "  " + lc4[1], 11, true, C_MUTED),
                        label(lc4[2], 14, true, lc4[3]));
                liveCards.getChildren().add(lcc);
            }
            resultArea.getChildren().add(liveCards);

            // API Info Note
            VBox noteCard = new VBox(8);
            noteCard.setStyle(
                    "-fx-background-color:#FFFBEB; -fx-background-radius:8; -fx-border-color:#FCD34D; -fx-border-radius:8; -fx-border-width:1; -fx-padding:12 16;");
            noteCard.getChildren().addAll(
                    label("ℹ️  Tracking Information", 12, true, C_ACCENT),
                    label("• Simulates Indian Railways NTES (National Train Enquiry System) data flow.", 11, false,
                            C_SUBTEXT),
                    label("• Position is calculated based on schedule and current time for demo mode.", 11, false,
                            C_SUBTEXT),
                    label("• For live GPS: integrate RailYatri API or IRCTC Railmadad real-time endpoints.", 11, false,
                            C_SUBTEXT),
                    label("• API endpoint: GET https://api.railyatri.in/api/train_between_stations", 11, true,
                            C_PRIMARY),
                    label(isUserBooking ? "• ✅ This train is linked to YOUR BOOKING — tracking is active."
                            : "• You have no active booking for this train.", 11, true,
                            isUserBooking ? C_SUCCESS : C_MUTED));
            resultArea.getChildren().add(noteCard);
        });

        outer.getChildren().add(resultArea);
        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // MODULE 4: NOTIFICATIONS PANEL 🔔
    // ════════════════════════════════════════════════════════
    static List<String[]> NOTIFICATIONS = new ArrayList<>();

    static void addNotification(String icon, String title, String msg, String color) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        NOTIFICATIONS.add(0, new String[] { icon, title, msg, color, time });
    }

    static void showNotifications() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Notifications"));
        VBox outer = new VBox(16);
        outer.setPadding(new Insets(24, 40, 40, 40));
        outer.setStyle("-fx-background-color:" + C_BG + ";");

        HBox titleRow = new HBox(14);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label alertsBadge = label(NOTIFICATIONS.size() + " alerts", 12, true, C_SURFACE);
        alertsBadge.setStyle("-fx-background-color:" + C_DANGER + "; -fx-background-radius:20; -fx-padding:2 10;");
        titleRow.getChildren().addAll(st("🔔  Notifications"), alertsBadge);
        Button btnClear = pb("🗑  Clear All", C_DANGER);
        btnClear.setPrefHeight(36);
        HBox tsp = new HBox();
        HBox.setHgrow(tsp, Priority.ALWAYS);
        titleRow.getChildren().addAll(tsp, btnClear);
        outer.getChildren().add(titleRow);

        VBox notifList = new VBox(10);

        Runnable buildNotifs = () -> {
            notifList.getChildren().clear();
            if (NOTIFICATIONS.isEmpty()) {
                VBox empty = new VBox(10);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(40));
                empty.getChildren().addAll(label("🔕", 40, false, C_MUTED),
                        label("No notifications yet.", 15, false, C_MUTED),
                        label("Book a ticket to receive booking alerts.", 12, false, C_MUTED));
                notifList.getChildren().add(empty);
                return;
            }
            for (String[] n : NOTIFICATIONS) {
                HBox nc = new HBox(14);
                nc.setAlignment(Pos.CENTER_LEFT);
                nc.setPadding(new Insets(14, 18, 14, 18));
                nc.setStyle(lc2());
                // Icon circle
                Label iconL = label(n[0], 20, false, C_SURFACE);
                iconL.setStyle("-fx-background-color:" + n[3] + "; -fx-background-radius:50; -fx-padding:8 10;");
                VBox nText = new VBox(4);
                HBox.setHgrow(nText, Priority.ALWAYS);
                nText.getChildren().addAll(label(n[1], 14, true, C_TEXT), label(n[2], 12, false, C_SUBTEXT));
                Label timeL = label(n[4], 10, false, C_MUTED);
                nc.getChildren().addAll(iconL, nText, timeL);
                notifList.getChildren().add(nc);
            }
        };
        buildNotifs.run();
        btnClear.setOnAction(e -> {
            NOTIFICATIONS.clear();
            buildNotifs.run();
        });
        outer.getChildren().add(notifList);
        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // MODULE 5: REFUND TRACKER 💰
    // ════════════════════════════════════════════════════════
    static void showRefundTracker() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("Refund Tracker"));
        VBox outer = new VBox(20);
        outer.setPadding(new Insets(24, 40, 40, 40));
        outer.setStyle("-fx-background-color:" + C_BG + ";");
        outer.getChildren().addAll(st("💰  Refund Tracker"),
                label("Track refund status for your cancelled tickets.", 13, false, C_MUTED));

        // Search box
        VBox searchCard = new VBox(14);
        searchCard.setStyle(lc2());
        searchCard.setPadding(new Insets(20, 24, 20, 24));
        searchCard.getChildren().add(label("Enter PNR to track refund", 15, true, C_TEXT));
        HBox sRow = new HBox(12);
        sRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfPNR = lf("Enter Cancelled PNR Number");
        tfPNR.setPrefWidth(300);
        tfPNR.setPrefHeight(44);
        Button btnTrack = pb("🔍  TRACK REFUND", C_PRIMARY);
        btnTrack.setPrefHeight(44);
        sRow.getChildren().addAll(tfPNR, btnTrack);
        VBox resultBox = new VBox(14);
        searchCard.getChildren().addAll(sRow, resultBox);

        btnTrack.setOnAction(e -> {
            resultBox.getChildren().clear();
            String pnr = tfPNR.getText().trim().toUpperCase();
            // Find cancelled booking
            Booking found2 = BOOKINGS.stream().filter(
                    bk -> bk.pnr.equals(pnr) && bk.username.equals(CURRENT_USER.username) && bk.status.equals("CAN"))
                    .findFirst().orElse(null);
            // Also try DB
            if (found2 == null) {
                try {
                    java.sql.ResultSet rs = DatabaseManager.getBookingByPNR(pnr);
                    if (rs != null && rs.next() && "CAN".equals(rs.getString("status"))
                            && rs.getInt("user_id") == CURRENT_USER.id) {
                        List<Passenger> pl = new ArrayList<>();
                        found2 = new Booking(pnr, CURRENT_USER.username, rs.getString("train_no"),
                                rs.getString("train_name"), rs.getString("source"), rs.getString("destination"),
                                rs.getString("journey_date"), rs.getString("seat_class"), rs.getString("quota"), pl,
                                rs.getDouble("total_fare"), rs.getString("payment_mode"));
                        found2.status = "CAN";
                        found2.bookingDateTime = rs.getString("booking_date");
                    }
                } catch (Exception ex) {
                    System.out.println("DB refund check: " + ex.getMessage());
                }
            }
            if (found2 == null) {
                resultBox.getChildren().add(label("❌  No cancelled booking found with PNR: " + pnr
                        + "\n\nOnly cancelled bookings are eligible for refund.", 13, false, C_DANGER));
                return;
            }
            final Booking fb = found2;
            double refundAmt = fb.totalFare * 0.85;
            double deductAmt = fb.totalFare * 0.15;

            // Refund details card
            VBox refCard = new VBox(0);
            refCard.setStyle(lc2());
            // Header
            HBox rfHdr = new HBox(0);
            rfHdr.setPadding(new Insets(14, 20, 14, 20));
            rfHdr.setAlignment(Pos.CENTER_LEFT);
            rfHdr.setStyle("-fx-background-color:" + C_NAV + "; -fx-background-radius:10 10 0 0;");
            HBox rfSp = new HBox();
            HBox.setHgrow(rfSp, Priority.ALWAYS);
            Label rfPNR = label("PNR: " + fb.pnr, 13, true, C_NAV_TEXT);
            rfPNR.setStyle("-fx-background-color:rgba(255,255,255,0.2); -fx-background-radius:5; -fx-padding:3 10;");
            rfHdr.getChildren().addAll(label("💰  Refund Details", 15, true, C_NAV_TEXT), rfSp, rfPNR);

            // Booking info
            VBox rfInfo = new VBox(10);
            rfInfo.setPadding(new Insets(16, 20, 16, 20));
            rfInfo.getChildren().addAll(
                    label(fb.trainName + " (#" + fb.trainNo + ")", 14, true, C_TEXT),
                    label(fb.source + " → " + fb.destination + "  |  " + fb.journeyDate, 12, false, C_MUTED),
                    label("Class: " + fb.seatClass + "  |  Passengers: " + fb.passengers.size(), 12, false, C_MUTED),
                    new Separator());
            // Fare breakdown
            GridPane fg = new GridPane();
            fg.setHgap(20);
            fg.setVgap(8);
            String[][] fareBreak = { { "Original Fare", "₹" + String.format("%.2f", fb.totalFare) },
                    { "Cancellation Charge (15%)", "- ₹" + String.format("%.2f", deductAmt) },
                    { "Refund Amount", "₹" + String.format("%.2f", refundAmt) } };
            for (int i = 0; i < fareBreak.length; i++) {
                String color2 = i == 2 ? C_SUCCESS : i == 1 ? C_DANGER : C_TEXT;
                fg.add(label(fareBreak[i][0], 12, i == 2, C_MUTED), 0, i);
                fg.add(label(fareBreak[i][1], 14, true, color2), 1, i);
            }
            rfInfo.getChildren().add(fg);

            // Refund timeline / status
            rfInfo.getChildren().add(new Separator());
            rfInfo.getChildren().add(label("Refund Status Timeline", 13, true, C_TEXT));

            // Determine days since cancellation (simulate)
            int daysSince = (int) (Math.random() * 7);
            String[][] timeline = {
                    { "✅", "Cancellation Requested", "Processed immediately", C_SUCCESS },
                    { "✅", "Refund Initiated", "Sent to payment gateway", C_SUCCESS },
                    { daysSince >= 2 ? "✅" : "⏳", daysSince >= 2 ? "Refund Processing" : "Processing...",
                            "Bank verification in progress", daysSince >= 2 ? C_SUCCESS : C_ACCENT },
                    { daysSince >= 5 ? "✅" : "⏳", daysSince >= 5 ? "Refund Credited" : "Pending Credit",
                            "Amount credited to original payment method", daysSince >= 5 ? C_SUCCESS : C_MUTED }
            };
            VBox timelineBox = new VBox(8);
            for (String[] tl : timeline) {
                HBox tlRow = new HBox(12);
                tlRow.setAlignment(Pos.CENTER_LEFT);
                tlRow.setPadding(new Insets(8, 12, 8, 12));
                tlRow.setStyle("-fx-background-color:" + tl[3] + "10; -fx-background-radius:8; -fx-border-color:"
                        + tl[3] + "30; -fx-border-radius:8; -fx-border-width:1;");
                Label tIcon = label(tl[0], 16, false, tl[3]);
                VBox tText = new VBox(2);
                HBox.setHgrow(tText, Priority.ALWAYS);
                tText.getChildren().addAll(label(tl[1], 12, true, C_TEXT), label(tl[2], 11, false, C_MUTED));
                tlRow.getChildren().addAll(tIcon, tText);
                timelineBox.getChildren().add(tlRow);
            }
            rfInfo.getChildren().add(timelineBox);

            // Expected date
            String expDate = LocalDate.now().plusDays(Math.max(0, 5 - daysSince))
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            HBox expRow = new HBox(10);
            expRow.setAlignment(Pos.CENTER_LEFT);
            expRow.setPadding(new Insets(12, 0, 0, 0));
            expRow.getChildren().addAll(label("📅", 16, false, C_PRIMARY),
                    label("Expected credit by: " + expDate, 13, true, C_PRIMARY));
            rfInfo.getChildren().add(expRow);

            refCard.getChildren().addAll(rfHdr, rfInfo);
            resultBox.getChildren().add(refCard);
        });

        // Show all cancelled bookings for this user
        outer.getChildren().add(searchCard);

        List<Booking> cancelled = BOOKINGS.stream()
                .filter(bk -> bk.username.equals(CURRENT_USER.username) && bk.status.equals("CAN"))
                .collect(Collectors.toList());
        if (!cancelled.isEmpty()) {
            VBox cancelledList = new VBox(12);
            cancelledList.setStyle(lc2());
            cancelledList.setPadding(new Insets(18, 20, 18, 20));
            cancelledList.getChildren()
                    .add(label("Your Cancelled Bookings (" + cancelled.size() + ")", 14, true, C_TEXT));
            for (Booking bk : cancelled) {
                HBox bkRow = new HBox(14);
                bkRow.setAlignment(Pos.CENTER_LEFT);
                bkRow.setPadding(new Insets(10, 14, 10, 14));
                bkRow.setStyle("-fx-background-color:" + C_DANGER_L + "; -fx-background-radius:8; -fx-border-color:"
                        + C_DANGER + "44; -fx-border-radius:8; -fx-border-width:1;");
                VBox bkInfo = new VBox(3);
                HBox.setHgrow(bkInfo, Priority.ALWAYS);
                bkInfo.getChildren().addAll(label("PNR: " + bk.pnr, 12, true, C_DANGER),
                        label(bk.trainName + " | " + bk.source + " → " + bk.destination, 12, false, C_TEXT),
                        label("Refund: ₹" + String.format("%.2f", bk.totalFare * 0.85) + " (5-7 days)", 11, false,
                                C_SUCCESS));
                Button trackBtn = new Button("Track →");
                trackBtn.setPrefHeight(32);
                trackBtn.setStyle("-fx-background-color:" + C_DANGER
                        + "; -fx-text-fill:white; -fx-font-size:11px; -fx-background-radius:6; -fx-cursor:hand;");
                trackBtn.setOnAction(ev -> {
                    tfPNR.setText(bk.pnr);
                    btnTrack.fire();
                });
                bkRow.getChildren().addAll(bkInfo, trackBtn);
                cancelledList.getChildren().add(bkRow);
            }
            outer.getChildren().add(cancelledList);
        }

        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // PROFILE
    // ════════════════════════════════════════════════════════
    static void showProfile() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(navBar("My Profile"));
        VBox outer = new VBox(20);
        outer.setPadding(new Insets(24, 80, 40, 80));
        outer.setMaxWidth(700);
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setStyle("-fx-background-color:" + C_BG + ";");
        VBox avatarCard = new VBox(10);
        avatarCard.setAlignment(Pos.CENTER);
        avatarCard.setStyle(
                "-fx-background-color: linear-gradient(to right,#1A3A8F,#2563EB); -fx-background-radius:14; -fx-padding:28;");
        avatarCard.getChildren().addAll(label("👤", 52, false, C_NAV_TEXT),
                label(CURRENT_USER.fullName, 20, true, C_NAV_TEXT),
                label("@" + CURRENT_USER.username + "  |  " + CURRENT_USER.email, 13, false, "#BFDBFE"));
        long total = BOOKINGS.stream().filter(bk -> bk.username.equals(CURRENT_USER.username)).count();
        long conf = BOOKINGS.stream().filter(bk -> bk.username.equals(CURRENT_USER.username) && bk.status.equals("CNF"))
                .count();
        HBox statsRow = new HBox(14);
        statsRow.setAlignment(Pos.CENTER);
        for (String[] s : new String[][] { { "🎫", total + "", "Total" }, { "✅", conf + "", "Confirmed" },
                { "❌", (total - conf) + "", "Cancelled" } }) {
            VBox sv = new VBox(4);
            sv.setAlignment(Pos.CENTER);
            sv.setPadding(new Insets(16, 24, 16, 24));
            sv.setStyle(lc2());
            HBox.setHgrow(sv, Priority.ALWAYS);
            sv.getChildren().addAll(label(s[0], 26, false, C_TEXT), label(s[1], 22, true, C_PRIMARY),
                    label(s[2], 11, false, C_MUTED));
            statsRow.getChildren().add(sv);
        }
        VBox editCard = new VBox(14);
        editCard.setStyle(lc2());
        editCard.setPadding(new Insets(20, 24, 20, 24));
        TextField tfFull = lf("Full Name");
        tfFull.setText(CURRENT_USER.fullName);
        tfFull.setPrefHeight(42);
        TextField tfEmail2 = lf("Email");
        tfEmail2.setText(CURRENT_USER.email);
        tfEmail2.setPrefHeight(42);
        TextField tfPhone2 = lf("Phone");
        tfPhone2.setText(CURRENT_USER.phone);
        tfPhone2.setPrefHeight(42);
        PasswordField pfOld = lp("Current Password");
        pfOld.setPrefHeight(42);
        PasswordField pfNew = lp("New Password");
        pfNew.setPrefHeight(42);
        Label msgL = label("", 12, false, C_SUCCESS);
        Button btnSave = pb("SAVE CHANGES", C_PRIMARY);
        btnSave.setPrefHeight(42);
        btnSave.setOnAction(e -> {
            CURRENT_USER.fullName = tfFull.getText().trim();
            CURRENT_USER.email = tfEmail2.getText().trim();
            CURRENT_USER.phone = tfPhone2.getText().trim();
            if (!pfOld.getText().isBlank() && pfOld.getText().equals(CURRENT_USER.password)
                    && !pfNew.getText().isBlank()) {
                CURRENT_USER.password = pfNew.getText();
                msgL.setTextFill(Color.web(C_SUCCESS));
                msgL.setText("✅  Profile & password updated.");
            } else if (!pfOld.getText().isBlank()) {
                msgL.setTextFill(Color.web(C_DANGER));
                msgL.setText("⚠  Current password is incorrect.");
            } else {
                msgL.setTextFill(Color.web(C_SUCCESS));
                msgL.setText("✅  Profile updated successfully.");
            }
        });
        editCard.getChildren().addAll(label("Edit Profile", 16, true, C_TEXT),
                label("Full Name", 12, true, C_SUBTEXT), tfFull,
                label("Email", 12, true, C_SUBTEXT), tfEmail2,
                label("Phone", 12, true, C_SUBTEXT), tfPhone2,
                new Separator(), label("Change Password", 13, true, C_TEXT), pfOld, pfNew, msgL, btnSave);
        outer.getChildren().addAll(st("👤  My Profile"), avatarCard, statsRow, editCard);
        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        root.setCenter(sp);
        setScene(root);
    }

    // ════════════════════════════════════════════════════════
    // ADMIN DASHBOARD
    // ════════════════════════════════════════════════════════
    static void showAdminDashboard() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setTop(adminNav());
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(16, 0, 16, 0));
        sidebar.setStyle("-fx-background-color:" + C_NAV + ";");
        sidebar.getChildren().add(label("  ADMIN PANEL", 10, true, "#90CAF9"));
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color:" + C_BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);
        String[][] menu = { { "📊", "Dashboard" }, { "🚂", "Manage Trains" }, { "📋", "All Bookings" },
                { "👥", "Users" }, { "📍", "Stations" }, { "💰", "Revenue Analytics" }, { "📈", "Top Trains Report" },
                { "🗓", "Monthly Trends" }, { "🔄", "Refund Analytics" } };
        String baseS = "-fx-background-color:transparent; -fx-cursor:hand;";
        String hovS = "-fx-background-color:rgba(255,255,255,0.15); -fx-cursor:hand; -fx-border-color:white; -fx-border-width:0 0 0 3;";
        for (int i = 0; i < menu.length; i++) {
            final int idx = i;
            HBox item = new HBox(14);
            item.setPadding(new Insets(13, 18, 13, 18));
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle(idx == 0 ? hovS : baseS);
            item.getChildren().addAll(label(menu[i][0], 16, false, C_NAV_TEXT),
                    label(menu[i][1], 13, idx == 0, C_NAV_TEXT));
            item.setOnMouseEntered(e -> item.setStyle(hovS));
            item.setOnMouseExited(e -> item.setStyle(baseS));
            item.setOnMouseClicked(e -> {
                for (Node n : sidebar.getChildren())
                    if (n instanceof HBox)
                        n.setStyle(baseS);
                item.setStyle(hovS);
                content.getChildren().setAll(adminPanel(idx));
            });
            sidebar.getChildren().add(item);
        }
        content.getChildren().add(adminPanel(0));
        root.setCenter(new HBox(sidebar, content));
        setScene(root);
    }

    static Node adminPanel(int idx) {
        switch (idx) {
            case 0:
                return aHome();
            case 1:
                return aTrains();
            case 2:
                return aBookings();
            case 3:
                return aUsers();
            case 4:
                return aStations();
            case 5:
                return aRevenueAnalytics();
            case 6:
                return aTopTrainsReport();
            case 7:
                return aMonthlyTrends();
            case 8:
                return aRefundAnalytics();
            default:
                return aHome();
        }
    }

    static Node aHome() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        VBox v = new VBox(20);
        v.setPadding(new Insets(24));
        v.setStyle("-fx-background-color:" + C_BG + ";");
        v.getChildren().add(st("📊  Dashboard Overview"));
        // ── Load stats from DB ──
        long tB = BOOKINGS.size(), cB = 0;
        double rev = 0.0;
        try {
            java.sql.ResultSet rs = DatabaseManager.getAllBookings();
            if (rs != null) {
                while (rs.next()) {
                    tB++;
                    if ("CNF".equals(rs.getString("status"))) {
                        cB++;
                        rev += rs.getDouble("total_fare");
                    }
                }
                tB = tB - BOOKINGS.size();
            }
        } catch (Exception ex) {
            cB = BOOKINGS.stream().filter(b -> b.status.equals("CNF")).count();
            rev = BOOKINGS.stream().filter(b -> b.status.equals("CNF")).mapToDouble(b -> b.totalFare).sum();
        }
        HBox stats = new HBox(14);
        for (Object[] s : new Object[][] { { " 🎫", "Total Bookings", tB + "", C_PRIMARY, "#EBF0FF" },
                { " ✅", "Confirmed", cB + "", C_SUCCESS, C_SUCCESS_L },
                { " 🚂", "Trains", TRAINS.size() + "", C_TEAL, "#ECFEFF" },
                { " 💰", "Revenue", "₹" + String.format("%.0f", rev), C_ORANGE, "#FFF7ED" } }) {
            VBox sc = new VBox(6);
            sc.setPadding(new Insets(18, 20, 18, 20));
            HBox.setHgrow(sc, Priority.ALWAYS);
            sc.setStyle("-fx-background-color:" + s[4] + "; -fx-background-radius:10; -fx-border-color:" + s[3]
                    + "44; -fx-border-radius:10; -fx-border-width:1;");
            sc.getChildren().addAll(label(s[0].toString(), 26, false, s[3].toString()),
                    label(s[2].toString(), 24, true, C_TEXT), label(s[1].toString(), 11, false, C_MUTED));
            stats.getChildren().add(sc);
        }
        v.getChildren().add(stats);
        v.getChildren().add(label("Recent Bookings", 15, true, C_TEXT));
        List<Booking> rec = new ArrayList<>(BOOKINGS);
        Collections.reverse(rec);
        for (Booking b : rec.stream().limit(8).collect(Collectors.toList())) {
            HBox row = new HBox(14);
            row.setPadding(new Insets(11, 14, 11, 14));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(lc2());
            Label pnrL2 = label(b.pnr, 12, true, C_PRIMARY);
            pnrL2.setPrefWidth(120);
            Label userL = label(b.username, 12, false, C_TEXT);
            userL.setPrefWidth(90);
            Label trainL = label(b.trainName, 12, false, C_MUTED);
            trainL.setPrefWidth(180);
            HBox rsp2 = new HBox();
            HBox.setHgrow(rsp2, Priority.ALWAYS);
            boolean cnf = b.status.equals("CNF");
            row.getChildren().addAll(pnrL2, userL, trainL, rsp2,
                    label("● " + (cnf ? "CNF" : "CAN"), 12, true, cnf ? C_SUCCESS : C_DANGER));
            v.getChildren().add(row);
        }
        sp.setContent(v);
        return sp;
    }

    static Node aTrains() {
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color:" + C_BG + ";");
        VBox top = new VBox(16);
        top.setPadding(new Insets(20));
        top.setStyle("-fx-background-color:" + C_BG + ";");
        top.getChildren().add(st("🚂  Manage Trains"));
        VBox form = new VBox(12);
        form.setStyle(lc2());
        form.setPadding(new Insets(16, 20, 16, 20));
        form.getChildren().add(label("➕  Add New Train", 14, true, C_PRIMARY));
        HBox r1 = new HBox(12);
        TextField tfNo = lf("Train No");
        tfNo.setPrefWidth(100);
        tfNo.setPrefHeight(40);
        TextField tfNm = lf("Train Name");
        tfNm.setPrefWidth(190);
        tfNm.setPrefHeight(40);
        TextField tfSrc = lf("Source");
        tfSrc.setPrefWidth(130);
        tfSrc.setPrefHeight(40);
        TextField tfDst = lf("Destination");
        tfDst.setPrefWidth(130);
        tfDst.setPrefHeight(40);
        r1.getChildren().addAll(tfNo, tfNm, tfSrc, tfDst);
        HBox r2 = new HBox(12);
        TextField tfDep = lf("Dep HH:MM");
        tfDep.setPrefWidth(110);
        tfDep.setPrefHeight(40);
        TextField tfArr = lf("Arr HH:MM");
        tfArr.setPrefWidth(110);
        tfArr.setPrefHeight(40);
        TextField tfFare = lf("Base Fare ₹");
        tfFare.setPrefWidth(110);
        tfFare.setPrefHeight(40);
        TextField tfSeats = lf("Seats");
        tfSeats.setPrefWidth(90);
        tfSeats.setPrefHeight(40);
        TextField tfDays = lf("Days");
        tfDays.setPrefWidth(130);
        tfDays.setPrefHeight(40);
        r2.getChildren().addAll(tfDep, tfArr, tfFare, tfSeats, tfDays);
        Label msgL3 = label("", 12, false, C_SUCCESS);
        Button btnAdd = pb("➕  ADD TRAIN", C_PRIMARY);
        btnAdd.setOnAction(e -> {
            try {
                TRAINS.add(new Train(tfNo.getText(), tfNm.getText(), tfSrc.getText(), tfDst.getText(), tfDep.getText(),
                        tfArr.getText(), tfDays.getText(), "Express", Integer.parseInt(tfSeats.getText()),
                        Double.parseDouble(tfFare.getText())));
                if (DatabaseManager.testConnection()) {
                    DatabaseManager.addTrain(tfNo.getText(), tfNm.getText(), tfSrc.getText(), tfDst.getText(), tfDep.getText(),
                            tfArr.getText(), tfDays.getText(), "Express", Integer.parseInt(tfSeats.getText()),
                            Double.parseDouble(tfFare.getText()));
                }
                msgL3.setTextFill(Color.web(C_SUCCESS));
                msgL3.setText("✅  Train added successfully!");
                bp.setCenter(aTrains());
            } catch (Exception ex) {
                msgL3.setTextFill(Color.web(C_DANGER));
                msgL3.setText("⚠  Fill all fields correctly.");
            }
        });
        form.getChildren().addAll(r1, r2, msgL3, btnAdd);
        top.getChildren().add(form);
        VBox tbl = new VBox(0);
        tbl.setPadding(new Insets(10, 20, 20, 20));
        tbl.setStyle("-fx-background-color:" + C_BG + ";");
        HBox hdr = new HBox(0);
        hdr.setPadding(new Insets(10, 0, 10, 0));
        hdr.setStyle("-fx-background-color:#E2E8F0;");
        // FIX: all String[][] values are Strings
        for (String[] h : new String[][] { { "No", "80" }, { "Name", "170" }, { "From", "120" }, { "To", "120" },
                { "Dep", "70" }, { "Arr", "70" }, { "Seats", "70" }, { "Avail", "70" }, { "Fare", "90" },
                { "Del", "60" } }) {
            Label hl = label(h[0], 11, true, C_SUBTEXT);
            hl.setPrefWidth(Integer.parseInt(h[1]));
            hl.setPadding(new Insets(0, 0, 0, 4));
            hdr.getChildren().add(hl);
        }
        tbl.getChildren().add(hdr);
        for (Train t : TRAINS) {
            HBox row = new HBox(0);
            row.setPadding(new Insets(10, 0, 10, 0));
            row.setStyle("-fx-background-color:" + C_SURFACE + "; -fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;");
            row.setAlignment(Pos.CENTER_LEFT);
            for (Object[] d : new Object[][] { { t.trainNo, 80 }, { t.trainName, 170 }, { t.source, 120 },
                    { t.destination, 120 }, { t.departure, 70 }, { t.arrival, 70 }, { t.totalSeats + "", 70 },
                    { t.availableSeats + "", 70 }, { "₹" + String.format("%.0f", t.baseFare), 90 } }) {
                Label dl = label(d[0].toString(), 12, false, C_TEXT);
                dl.setPrefWidth((int) d[1]);
                dl.setPadding(new Insets(0, 0, 0, 4));
                row.getChildren().add(dl);
            }
            Button del = new Button("🗑");
            del.setPrefWidth(60);
            del.setStyle("-fx-background-color:" + C_DANGER_L + "; -fx-text-fill:" + C_DANGER + "; -fx-border-color:"
                    + C_DANGER + "; -fx-border-radius:4; -fx-background-radius:4; -fx-cursor:hand;");
            del.setOnAction(e -> {
                if (DatabaseManager.testConnection()) {
                    DatabaseManager.deleteTrain(t.trainNo);
                }
                TRAINS.remove(t);
                bp.setCenter(aTrains());
            });
            row.getChildren().add(del);
            tbl.getChildren().add(row);
        }
        ScrollPane sp = new ScrollPane(new VBox(top, tbl));
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        bp.setCenter(sp);
        return bp;
    }

    static Node aBookings() {
        VBox v = new VBox(14);
        v.setPadding(new Insets(24));
        v.setStyle("-fx-background-color:" + C_BG + ";");
        v.getChildren().add(st("📋  All Bookings (" + BOOKINGS.size() + ")"));
        HBox hdr = new HBox(0);
        hdr.setPadding(new Insets(10, 0, 10, 0));
        hdr.setStyle("-fx-background-color:#E2E8F0;");
        for (String[] h : new String[][] { { "PNR", "120" }, { "User", "100" }, { "Train", "160" }, { "Route", "170" },
                { "Date", "100" }, { "Class", "140" }, { "Fare", "90" }, { "Status", "100" } }) {
            Label hl = label(h[0], 11, true, C_SUBTEXT);
            hl.setPrefWidth(Integer.parseInt(h[1]));
            hl.setPadding(new Insets(0, 0, 0, 4));
            hdr.getChildren().add(hl);
        }
        v.getChildren().add(hdr);
        List<Booking> rev = new ArrayList<>(BOOKINGS);
        Collections.reverse(rev);
        for (Booking b : rev) {
            HBox row = new HBox(0);
            row.setPadding(new Insets(10, 0, 10, 0));
            row.setStyle("-fx-background-color:" + C_SURFACE + "; -fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;");
            row.setAlignment(Pos.CENTER_LEFT);
            for (Object[] d : new Object[][] { { b.pnr, 120 }, { b.username, 100 }, { b.trainName, 160 },
                    { b.source + "→" + b.destination, 170 }, { b.journeyDate, 100 }, { b.seatClass, 140 },
                    { "₹" + String.format("%.0f", b.totalFare), 90 } }) {
                Label dl = label(d[0].toString(), 12, false, C_TEXT);
                dl.setPrefWidth((int) d[1]);
                dl.setPadding(new Insets(0, 0, 0, 4));
                row.getChildren().add(dl);
            }
            boolean cnf = b.status.equals("CNF");
            Label sl = label(cnf ? "CONFIRMED" : "CANCELLED", 12, true, cnf ? C_SUCCESS : C_DANGER);
            sl.setPrefWidth(100);
            row.getChildren().add(sl);
            v.getChildren().add(row);
        }
        ScrollPane sp = new ScrollPane(v);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        return sp;
    }

    static Node aUsers() {
        VBox v = new VBox(14);
        v.setPadding(new Insets(24));
        v.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Load users from MySQL DB ──
        List<User> allUsers = new ArrayList<>();
        try {
            java.sql.ResultSet rs = DatabaseManager.getAllUsers();
            if (rs != null) {
                while (rs.next()) {
                    User u = new User(rs.getString("username"), rs.getString("password"),
                            rs.getString("email"), rs.getString("phone"), rs.getString("full_name"),
                            rs.getInt("is_admin") == 1);
                    u.id = rs.getInt("id");
                    u.gender = rs.getString("gender");
                    allUsers.add(u);
                }
            }
        } catch (Exception ex) {
            System.out.println("DB load users failed: " + ex.getMessage());
            allUsers.addAll(USERS); // fallback
        }
        if (allUsers.isEmpty())
            allUsers.addAll(USERS);

        v.getChildren().add(st("👥  Manage Users (" + allUsers.size() + ")"));

        // ── Search bar ──
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfSearch = lf("Search by username, name or email...");
        tfSearch.setPrefWidth(320);
        tfSearch.setPrefHeight(40);
        Label searchResult = label("", 12, false, C_MUTED);
        searchRow.getChildren().addAll(label("🔍", 14, false, C_PRIMARY), tfSearch, searchResult);
        v.getChildren().add(searchRow);

        // ── Stats bar ──
        long adminCount = allUsers.stream().filter(u -> u.isAdmin).count();
        long userCount = allUsers.stream().filter(u -> !u.isAdmin).count();
        HBox statsRow = new HBox(14);
        for (String[] s : new String[][] { { "👥", "Total Users", allUsers.size() + "", "#1A56DB", "#EBF0FF" },
                { "🔧", "Admins", adminCount + "", "#D97706", "#FFFBEB" },
                { "👤", "Regular Users", userCount + "", "#057A55", "#DEF7EC" } }) {
            HBox sc = new HBox(10);
            sc.setAlignment(Pos.CENTER_LEFT);
            sc.setPadding(new Insets(10, 16, 10, 16));
            sc.setStyle("-fx-background-color:" + s[4] + "; -fx-background-radius:8; -fx-border-color:" + s[3]
                    + "44; -fx-border-radius:8; -fx-border-width:1;");
            VBox statTxt = new VBox(2);
            statTxt.getChildren().addAll(label(s[1], 10, false, C_MUTED), label(s[2], 18, true, s[3]));
            sc.getChildren().addAll(label(s[0], 18, false, C_TEXT), statTxt);
            statsRow.getChildren().add(sc);
        }
        v.getChildren().add(statsRow);

        // ── Table Header ──
        HBox hdr = new HBox(0);
        hdr.setPadding(new Insets(10, 0, 10, 0));
        hdr.setStyle("-fx-background-color:#E2E8F0;");
        for (String[] h : new String[][] { { "#", "40" }, { "Username", "130" }, { "Full Name", "180" },
                { "Email", "200" }, { "Phone", "130" }, { "Gender", "80" }, { "Role", "90" }, { "Action", "90" } }) {
            Label hl = label(h[0], 11, true, C_SUBTEXT);
            hl.setPrefWidth(Integer.parseInt(h[1]));
            hl.setPadding(new Insets(0, 0, 0, 4));
            hdr.getChildren().add(hl);
        }

        // ── User rows container ──
        VBox rowsBox = new VBox(0);

        final Runnable[] buildRowsRef = new Runnable[1];
        Runnable buildRows = buildRowsRef[0] = () -> {
            rowsBox.getChildren().clear();
            String query = tfSearch.getText().trim().toLowerCase();
            List<User> filtered = allUsers.stream().filter(u -> query.isBlank() ||
                    u.username.toLowerCase().contains(query) ||
                    u.fullName.toLowerCase().contains(query) ||
                    u.email.toLowerCase().contains(query)).collect(Collectors.toList());

            searchResult.setText(filtered.size() + " user(s) found");

            for (int i = 0; i < filtered.size(); i++) {
                User u = filtered.get(i);
                final int fi = i;
                HBox row = new HBox(0);
                row.setPadding(new Insets(9, 0, 9, 0));
                row.setStyle("-fx-background-color:" + (i % 2 == 0 ? C_SURFACE : "#FAFBFF")
                        + "; -fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;");
                row.setAlignment(Pos.CENTER_LEFT);
                row.setOnMouseEntered(ev -> row
                        .setStyle("-fx-background-color:#EFF6FF; -fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;"));
                row.setOnMouseExited(ev -> row.setStyle("-fx-background-color:" + (fi % 2 == 0 ? C_SURFACE : "#FAFBFF")
                        + "; -fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;"));

                Label numL = label((i + 1) + ".", 11, false, C_MUTED);
                numL.setPrefWidth(40);
                numL.setPadding(new Insets(0, 0, 0, 8));
                for (Object[] d : new Object[][] { { u.username, 130 }, { u.fullName, 180 }, { u.email, 200 },
                        { u.phone, 130 }, { u.gender != null ? u.gender : "—", 80 } }) {
                    Label dl = label(d[0].toString(), 12, false, C_TEXT);
                    dl.setPrefWidth((int) d[1]);
                    dl.setPadding(new Insets(0, 0, 0, 4));
                    row.getChildren().add(dl);
                }
                String roleColor = u.isAdmin ? C_ORANGE : C_PRIMARY;
                String roleBg = u.isAdmin ? "#FFF7ED" : C_PRIMARY_L;
                Label rl = label(u.isAdmin ? "🔧 ADMIN" : "👤 USER", 10, true, roleColor);
                rl.setStyle("-fx-background-color:" + roleBg
                        + "; -fx-background-radius:4; -fx-padding:2 7; -fx-text-fill:" + roleColor + ";");
                rl.setPrefWidth(90);

                // Delete button (only for non-admin users)
                Button delBtn = new Button(u.isAdmin ? "—" : "🗑 Delete");
                delBtn.setPrefWidth(86);
                delBtn.setPrefHeight(26);
                if (!u.isAdmin) {
                    delBtn.setStyle(
                            "-fx-background-color:#FDE8E8; -fx-text-fill:#C81E1E; -fx-font-size:10px; -fx-background-radius:5; -fx-border-color:#FCA5A5; -fx-border-radius:5; -fx-cursor:hand;");
                    delBtn.setOnAction(e -> {
                        try {
                            DatabaseManager.deleteUser(u.id);
                            allUsers.remove(u);
                            USERS.removeIf(x -> x.username.equals(u.username));
                        } catch (Exception ex) {
                            System.out.println("Delete user err: " + ex.getMessage());
                        }
                        buildRowsRef[0].run();
                    });
                } else {
                    delBtn.setStyle(
                            "-fx-background-color:#F1F5F9; -fx-text-fill:#9CA3AF; -fx-font-size:10px; -fx-background-radius:5; -fx-cursor:default;");
                    delBtn.setDisable(true);
                }
                row.getChildren().addAll(numL);
                // re-add username at front
                row.getChildren().remove(0); // remove numL we just added wrong
                row.getChildren().clear();
                row.getChildren().add(numL);
                for (Object[] d : new Object[][] { { u.username, 130 }, { u.fullName, 180 }, { u.email, 200 },
                        { u.phone, 130 }, { u.gender != null ? u.gender : "—", 80 } }) {
                    Label dl = label(d[0].toString(), 12, false, C_TEXT);
                    dl.setPrefWidth((int) d[1]);
                    dl.setPadding(new Insets(0, 0, 0, 4));
                    row.getChildren().add(dl);
                }
                row.getChildren().addAll(rl, delBtn);
                rowsBox.getChildren().add(row);
            }
            if (filtered.isEmpty()) {
                Label noRes = label("No users found matching '" + query + "'", 13, false, C_MUTED);
                noRes.setPadding(new Insets(20, 8, 20, 8));
                rowsBox.getChildren().add(noRes);
            }
        };

        // Live search
        tfSearch.textProperty().addListener((obs, oldV, newV) -> buildRows.run());
        buildRows.run();

        // Refresh button
        Button btnRefresh = pb("🔄  Refresh from DB", C_PRIMARY);
        btnRefresh.setOnAction(e -> {
            allUsers.clear();
            try {
                java.sql.ResultSet rs = DatabaseManager.getAllUsers();
                if (rs != null) {
                    while (rs.next()) {
                        User u = new User(rs.getString("username"), rs.getString("password"), rs.getString("email"),
                                rs.getString("phone"), rs.getString("full_name"), rs.getInt("is_admin") == 1);
                        u.id = rs.getInt("id");
                        u.gender = rs.getString("gender");
                        allUsers.add(u);
                    }
                }
            } catch (Exception ex) {
                allUsers.addAll(USERS);
            }
            buildRows.run();
            showAlert("✅  Users refreshed from database. Total: " + allUsers.size());
        });

        v.getChildren().addAll(btnRefresh, hdr, rowsBox);
        ScrollPane sp = new ScrollPane(v);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        return sp;
    }

    static Node aStations() {
        VBox v = new VBox(16);
        v.setPadding(new Insets(24));
        v.setStyle("-fx-background-color:" + C_BG + ";");
        v.getChildren().add(st("📍  Station Management"));
        Set<String> stations = new TreeSet<>();
        for (Train t : TRAINS) {
            stations.add(t.source);
            stations.add(t.destination);
        }
        for (String st : CUSTOM_STATIONS) {
            stations.add(st);
        }
        try {
            if (DatabaseManager.testConnection()) {
                java.sql.ResultSet rs = DatabaseManager.getAllStationRecords();
                if (rs != null) {
                    while (rs.next()) stations.add(rs.getString("station_name"));
                }
            }
        } catch (Exception ex) {}

        FlowPane fp = new FlowPane(10, 10);
        fp.setPadding(new Insets(10, 0, 10, 0));
        Label countLabel = label("Active Stations (" + stations.size() + ")", 13, true, C_MUTED);

        HBox addRow = new HBox(12);
        addRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfNew = lf("New Station Name");
        tfNew.setPrefWidth(240);
        tfNew.setPrefHeight(42);
        Button btnAdd = pb("➕  ADD", C_SUCCESS);
        btnAdd.setOnAction(e -> {
            String stName = tfNew.getText().trim();
            if (!stName.isBlank()) {
                String stCode = stName.length() >= 3 ? stName.substring(0, 3).toUpperCase() : stName.toUpperCase();
                if (DatabaseManager.testConnection()) {
                    DatabaseManager.addStation(stCode, stName, stName, "Unknown");
                }
                if (!stations.contains(stName)) {
                    CUSTOM_STATIONS.add(stName);
                    stations.add(stName);
                    Label sl = label(stName, 12, false, C_PRIMARY);
                    sl.setPadding(new Insets(7, 14, 7, 14));
                    sl.setStyle("-fx-background-color:" + C_PRIMARY_L + "; -fx-background-radius:20; -fx-border-color:"
                            + C_PRIMARY + "44; -fx-border-radius:20;");
                    fp.getChildren().add(sl);
                    countLabel.setText("Active Stations (" + stations.size() + ")");
                }
                showAlert("Station '" + stName + "' added.");
                tfNew.clear();
            }
        });
        addRow.getChildren().addAll(tfNew, btnAdd);
        
        for (String s : stations) {
            Label sl = label(s, 12, false, C_PRIMARY);
            sl.setPadding(new Insets(7, 14, 7, 14));
            sl.setStyle("-fx-background-color:" + C_PRIMARY_L + "; -fx-background-radius:20; -fx-border-color:"
                    + C_PRIMARY + "44; -fx-border-radius:20;");
            fp.getChildren().add(sl);
        }
        v.getChildren().addAll(addRow, countLabel, fp);
        ScrollPane sp = new ScrollPane(v);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        return sp;
    }

    // ════════════════════════════════════════════════════════
    // ADMIN — REVENUE ANALYTICS
    // ════════════════════════════════════════════════════════
    static Node aRevenueAnalytics() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        VBox v = new VBox(22);
        v.setPadding(new Insets(24));
        v.setStyle("-fx-background-color:" + C_BG + ";");
        v.getChildren().add(st("💰  Revenue Analytics — Monthly Breakdown"));
        v.getChildren()
                .add(label("Monthly revenue trend, class-wise breakdown, and booking patterns.", 13, false, C_MUTED));

        double totalRev = BOOKINGS.stream().filter(b -> b.status.equals("CNF")).mapToDouble(b -> b.totalFare).sum();
        long totalBk = BOOKINGS.stream().filter(b -> b.status.equals("CNF")).count();
        long totalCan = BOOKINGS.stream().filter(b -> b.status.equals("CAN")).count();
        double avgFare = totalBk > 0 ? totalRev / totalBk : 0;
        double refundAmt = BOOKINGS.stream().filter(b -> b.status.equals("CAN")).mapToDouble(b -> b.totalFare * 0.85)
                .sum();

        HBox kpiRow = new HBox(14);
        for (Object[] k : new Object[][] {
                { "💰", "Total Revenue", "₹" + String.format("%,.0f", totalRev), C_SUCCESS, "#DEF7EC" },
                { "🎫", "Confirmed Bookings", String.valueOf(totalBk), C_PRIMARY, "#EBF0FF" },
                { "❌", "Cancellations", String.valueOf(totalCan), C_DANGER, "#FDE8E8" },
                { "📊", "Avg Ticket Fare", "₹" + String.format("%.0f", avgFare), "#7C3AED", "#F5F3FF" },
                { "🔄", "Refunds Issued", "₹" + String.format("%,.0f", refundAmt), "#D97706", "#FFFBEB" }
        }) {
            VBox kc = new VBox(6);
            kc.setPadding(new Insets(16, 18, 16, 18));
            HBox.setHgrow(kc, Priority.ALWAYS);
            kc.setStyle("-fx-background-color:" + k[4] + "; -fx-background-radius:12; -fx-border-color:" + k[3]
                    + "44; -fx-border-radius:12; -fx-border-width:1;");
            kc.getChildren().addAll(label(k[0] + " " + k[1], 11, false, C_MUTED),
                    label(k[2].toString(), 22, true, k[3].toString()));
            kpiRow.getChildren().add(kc);
        }
        v.getChildren().add(kpiRow);

        // Monthly bar chart
        VBox monthChart = new VBox(16);
        monthChart.setStyle(lc2());
        monthChart.setPadding(new Insets(20, 22, 20, 22));
        monthChart.getChildren().add(label("📅  Monthly Revenue Bar Chart", 15, true, C_TEXT));
        String[] mNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        double[] mRev = new double[12];
        long[] mCnt = new long[12];
        for (Booking b : BOOKINGS) {
            if (!b.status.equals("CNF"))
                continue;
            try {
                String[] p = b.bookingDateTime.split("-");
                if (p.length >= 2) {
                    int mo = Integer.parseInt(p[1]) - 1;
                    if (mo >= 0 && mo < 12) {
                        mRev[mo] += b.totalFare;
                        mCnt[mo]++;
                    }
                }
            } catch (Exception ex) {
            }
        }
        double maxR = 0;
        for (double r : mRev)
            if (r > maxR)
                maxR = r;
        if (maxR == 0)
            maxR = 1;
        HBox barRow = new HBox(4);
        barRow.setAlignment(Pos.BOTTOM_LEFT);
        barRow.setPrefHeight(180);
        String[] bClrs = { "#1A56DB", "#2563EB", "#3B82F6", "#1D4ED8", "#0369A1", "#0284C7", "#059669", "#10B981",
                "#047481", "#9333EA", "#7C3AED", "#D97706" };
        for (int mi = 0; mi < 12; mi++) {
            VBox col = new VBox(3);
            col.setAlignment(Pos.BOTTOM_CENTER);
            HBox.setHgrow(col, Priority.ALWAYS);
            double bH = mRev[mi] / maxR * 150;
            if (bH < 3 && mRev[mi] > 0)
                bH = 3;
            Region bar = new Region();
            bar.setPrefWidth(34);
            bar.setPrefHeight(bH);
            bar.setStyle("-fx-background-color:" + bClrs[mi] + "; -fx-background-radius:3 3 0 0;");
            Label vl = label(mRev[mi] > 0 ? "₹" + String.format("%,.0f", mRev[mi] / 1000) + "k" : "", 8, false,
                    bClrs[mi]);
            vl.setWrapText(true);
            vl.setMaxWidth(50);
            vl.setAlignment(Pos.CENTER);
            Label ml = label(mNames[mi], 10, true, C_SUBTEXT);
            ml.setAlignment(Pos.CENTER);
            Label cntLbl = label(mCnt[mi] > 0 ? mCnt[mi] + "b" : "", 9, false, C_MUTED);
            cntLbl.setAlignment(Pos.CENTER);
            col.getChildren().addAll(vl, bar, ml, cntLbl);
            barRow.getChildren().add(col);
        }
        monthChart.getChildren().add(barRow);

        // Monthly summary table
        HBox th = new HBox(0);
        th.setPadding(new Insets(8, 0, 8, 0));
        th.setStyle("-fx-background-color:#EFF6FF; -fx-background-radius:6;");
        for (String[] h : new String[][] { { "Month", "90" }, { "Bookings", "100" }, { "Revenue", "140" },
                { "Avg Fare", "120" }, { "vs Prev", "140" } }) {
            Label hl = label(h[0], 11, true, C_PRIMARY);
            hl.setPrefWidth(Integer.parseInt(h[1]));
            hl.setPadding(new Insets(0, 0, 0, 8));
            th.getChildren().add(hl);
        }
        VBox tRows = new VBox(0);
        for (int mi = 0; mi < 12; mi++) {
            HBox row = new HBox(0);
            row.setPadding(new Insets(9, 0, 9, 0));
            row.setStyle("-fx-background-color:" + (mi % 2 == 0 ? C_SURFACE : "#FAFBFF")
                    + "; -fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;");
            double prev = mi > 0 ? mRev[mi - 1] : mRev[mi];
            double ch = prev > 0 ? (mRev[mi] - prev) / prev * 100 : 0;
            String chStr = ch > 0 ? "▲ +" + String.format("%.1f", ch) + "%"
                    : ch < 0 ? "▼ " + String.format("%.1f", ch) + "%" : "—";
            String chCol = ch > 0 ? C_SUCCESS : ch < 0 ? C_DANGER : C_MUTED;
            for (Object[] d : new Object[][] { { mNames[mi] + " 2025", 90 }, { mCnt[mi] + "", 100 },
                    { "₹" + String.format("%,.0f", mRev[mi]), 140 },
                    { mCnt[mi] > 0 ? "₹" + String.format("%.0f", mRev[mi] / Math.max(1, mCnt[mi])) : "—", 120 } }) {
                Label dl = label(d[0].toString(), 12, false, C_TEXT);
                dl.setPrefWidth((int) d[1]);
                dl.setPadding(new Insets(0, 0, 0, 8));
                row.getChildren().add(dl);
            }
            Label cl = label(chStr, 11, true, chCol);
            cl.setPrefWidth(140);
            cl.setPadding(new Insets(0, 0, 0, 8));
            row.getChildren().add(cl);
            tRows.getChildren().add(row);
        }
        monthChart.getChildren().addAll(th, tRows);
        v.getChildren().add(monthChart);

        // Revenue by seat class
        VBox classRev = new VBox(14);
        classRev.setStyle(lc2());
        classRev.setPadding(new Insets(20, 22, 20, 22));
        classRev.getChildren().add(label("💺  Revenue by Seat Class", 15, true, C_TEXT));
        Map<String, double[]> cd = new LinkedHashMap<>();
        for (String[] sc : SC)
            cd.put(sc[0], new double[] { 0, 0 });
        for (Booking b : BOOKINGS) {
            if (!b.status.equals("CNF"))
                continue;
            for (String[] sc : SC) {
                if (b.seatClass.startsWith(sc[0])) {
                    cd.get(sc[0])[0] += b.totalFare;
                    cd.get(sc[0])[1]++;
                    break;
                }
            }
        }
        double maxCR = cd.values().stream().mapToDouble(d -> d[0]).max().orElse(1);
        if (maxCR == 0)
            maxCR = 1;
        for (String[] sc : SC) {
            double[] d = cd.get(sc[0]);
            if (d == null || d[0] == 0)
                continue;
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 0, 5, 0));
            Label cL = label(sc[0], 12, true, sc[4]);
            cL.setStyle("-fx-background-color:" + sc[4] + "22; -fx-background-radius:4; -fx-padding:3 9; -fx-text-fill:"
                    + sc[4] + ";");
            cL.setPrefWidth(60);
            Label nL = label(sc[1], 12, false, C_SUBTEXT);
            nL.setPrefWidth(160);
            StackPane bg = new StackPane();
            HBox.setHgrow(bg, Priority.ALWAYS);
            bg.setPrefHeight(26);
            bg.setStyle("-fx-background-color:#F1F5F9; -fx-background-radius:5;");
            HBox fill = new HBox();
            fill.setPrefWidth(d[0] / maxCR * 350);
            fill.setPrefHeight(26);
            fill.setStyle("-fx-background-color:" + sc[4] + "; -fx-background-radius:5;");
            fill.setAlignment(Pos.CENTER_LEFT);
            if (d[0] > 0)
                fill.getChildren().add(label("  ₹" + String.format("%,.0f", d[0]), 10, true, "#FFFFFF"));
            bg.getChildren().add(fill);
            StackPane.setAlignment(fill, Pos.CENTER_LEFT);
            Label cntL = label((int) d[1] + " bookings", 11, false, C_MUTED);
            cntL.setPrefWidth(110);
            row.getChildren().addAll(cL, nL, bg, cntL);
            classRev.getChildren().add(row);
        }
        v.getChildren().add(classRev);
        sp.setContent(v);
        return sp;
    }

    // ════════════════════════════════════════════════════════
    // ADMIN — TOP TRAINS REPORT
    // ════════════════════════════════════════════════════════
    static Node aTopTrainsReport() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        VBox v = new VBox(22);
        v.setPadding(new Insets(24));
        v.setStyle("-fx-background-color:" + C_BG + ";");
        v.getChildren().add(st("🏆  Top Performing Trains"));
        v.getChildren()
                .add(label("Ranked by bookings, revenue, occupancy rate and cancellation rate.", 13, false, C_MUTED));

        Map<String, double[]> ts = new LinkedHashMap<>();
        for (Booking b : BOOKINGS) {
            ts.computeIfAbsent(b.trainName, k -> new double[3]);
            if (b.status.equals("CNF")) {
                ts.get(b.trainName)[0]++;
                ts.get(b.trainName)[1] += b.totalFare;
            } else if (b.status.equals("CAN"))
                ts.get(b.trainName)[2]++;
        }
        List<Map.Entry<String, double[]>> sorted = ts.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0])).collect(Collectors.toList());

        // Podium top 3
        if (!sorted.isEmpty()) {
            HBox podium = new HBox(16);
            podium.setAlignment(Pos.BOTTOM_CENTER);
            String[][] pc = { { "#FFA500", "🥇" }, { "#9CA3AF", "🥈" }, { "#CD7F32", "🥉" } };
            for (int pi = 0; pi < Math.min(3, sorted.size()); pi++) {
                Map.Entry<String, double[]> e = sorted.get(pi);
                VBox pv = new VBox(10);
                pv.setAlignment(Pos.CENTER);
                pv.setPadding(new Insets(20, 18, 20, 18));
                pv.setStyle("-fx-background-color:" + pc[pi][0] + "15; -fx-background-radius:14; -fx-border-color:"
                        + pc[pi][0] + "55; -fx-border-radius:14; -fx-border-width:2;");
                HBox.setHgrow(pv, Priority.ALWAYS);
                pv.getChildren().addAll(label(pc[pi][1], 32, false, "#FFFFFF"),
                        label("#" + (pi + 1), 13, true, pc[pi][0]),
                        label(e.getKey().length() > 22 ? e.getKey().substring(0, 22) : e.getKey(), 12, true, C_TEXT),
                        label((int) e.getValue()[0] + " bookings", 11, false, C_MUTED),
                        label("₹" + String.format("%,.0f", e.getValue()[1]), 16, true, pc[pi][0]));
                podium.getChildren().add(pv);
            }
            v.getChildren().add(podium);
        }

        // Rankings table
        VBox table = new VBox(0);
        table.setStyle(lc2());
        HBox tHdr = new HBox(0);
        tHdr.setPadding(new Insets(12, 18, 12, 18));
        tHdr.setStyle("-fx-background-color:#EFF6FF; -fx-background-radius:10 10 0 0;");
        for (String[] h : new String[][] { { "#", "40" }, { "Train Name", "220" }, { "Bookings", "110" },
                { "Revenue", "140" }, { "Cancelled", "110" }, { "Occupancy", "120" }, { "Avg Fare", "110" } }) {
            Label hl = label(h[0], 11, true, C_PRIMARY);
            hl.setPrefWidth(Integer.parseInt(h[1]));
            hl.setPadding(new Insets(0, 0, 0, 6));
            tHdr.getChildren().add(hl);
        }
        table.getChildren().add(tHdr);
        for (int i = 0; i < Math.min(sorted.size(), 20); i++) {
            Map.Entry<String, double[]> e = sorted.get(i);
            double[] d = e.getValue();
            HBox row = new HBox(0);
            row.setPadding(new Insets(11, 18, 11, 18));
            row.setStyle("-fx-background-color:" + (i % 2 == 0 ? C_SURFACE : "#FAFBFF")
                    + "; -fx-border-color:#E2E8F0; -fx-border-width:0 0 1 0;");
            row.setAlignment(Pos.CENTER_LEFT);
            Train tr = TRAINS.stream().filter(t -> t.trainName.equals(e.getKey())).findFirst().orElse(null);
            double occ = tr != null && tr.totalSeats > 0 ? Math.min(100, (d[0] / (tr.totalSeats * 0.1)) * 100) : 0;
            String oc = occ > 80 ? C_SUCCESS : occ > 50 ? "#D97706" : C_DANGER;
            Label rL = label((i + 1) + ".", 11, true, i < 3 ? "#FFA500" : C_MUTED);
            rL.setPrefWidth(40);
            rL.setPadding(new Insets(0, 0, 0, 6));
            Label nL = label(e.getKey(), 12, false, C_TEXT);
            nL.setPrefWidth(220);
            nL.setPadding(new Insets(0, 0, 0, 6));
            Label bL = label((int) d[0] + "", 13, true, C_TEXT);
            bL.setPrefWidth(110);
            bL.setPadding(new Insets(0, 0, 0, 6));
            Label rvL = label("₹" + String.format("%,.0f", d[1]), 12, true, C_SUCCESS);
            rvL.setPrefWidth(140);
            rvL.setPadding(new Insets(0, 0, 0, 6));
            Label caL = label((int) d[2] + "", 12, false, C_DANGER);
            caL.setPrefWidth(110);
            caL.setPadding(new Insets(0, 0, 0, 6));
            Label ocL = label(String.format("%.0f", occ) + "%", 12, true, oc);
            ocL.setPrefWidth(120);
            ocL.setPadding(new Insets(0, 0, 0, 6));
            Label avL = label(d[0] > 0 ? "₹" + String.format("%.0f", d[1] / d[0]) : "—", 12, false, C_MUTED);
            avL.setPrefWidth(110);
            avL.setPadding(new Insets(0, 0, 0, 6));
            row.getChildren().addAll(rL, nL, bL, rvL, caL, ocL, avL);
            table.getChildren().add(row);
        }
        if (sorted.isEmpty()) {
            Label nd = label("No data yet. Bookings will appear here automatically.", 13, false, C_MUTED);
            nd.setPadding(new Insets(24, 18, 24, 18));
            table.getChildren().add(nd);
        }
        v.getChildren().add(table);
        sp.setContent(v);
        return sp;
    }

    // ════════════════════════════════════════════════════════
    // ADMIN — MONTHLY TRENDS & PEAK SEASONS
    // ════════════════════════════════════════════════════════
    static Node aMonthlyTrends() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        VBox v = new VBox(22);
        v.setPadding(new Insets(24));
        v.setStyle("-fx-background-color:" + C_BG + ";");
        v.getChildren().add(st("📅  Peak Season & Trend Analysis"));
        v.getChildren().add(
                label("Identify high-demand months, plan capacity, and optimize dynamic pricing.", 13, false, C_MUTED));

        String[] mN = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        double[] mR = new double[12];
        long[] mC = new long[12];
        long[] mCan = new long[12];
        for (Booking b : BOOKINGS) {
            try {
                String[] p = b.bookingDateTime.split("-");
                if (p.length >= 2) {
                    int mo = Integer.parseInt(p[1]) - 1;
                    if (mo >= 0 && mo < 12) {
                        if (b.status.equals("CNF")) {
                            mR[mo] += b.totalFare;
                            mC[mo]++;
                        } else if (b.status.equals("CAN"))
                            mCan[mo]++;
                    }
                }
            } catch (Exception ex) {
            }
        }

        // Peak insights banner
        VBox peakBanner = new VBox(12);
        peakBanner.setStyle(
                "-fx-background-color:#FFFBEB; -fx-background-radius:10; -fx-border-color:#FCD34D; -fx-border-radius:10; -fx-border-width:1; -fx-padding:16 20;");
        peakBanner.getChildren().add(label("🔥  Indian Railways Peak Season Insights", 14, true, C_ACCENT));
        HBox iRow = new HBox(30);
        iRow.setAlignment(Pos.TOP_LEFT);
        VBox highV = new VBox(6);
        highV.getChildren().add(label("📈  High-Demand Months:", 12, true, C_SUCCESS));
        for (String s : new String[] { "May-June (Summer vacations)", "October (Navratri/Durga Puja)",
                "November (Diwali season)", "December (Christmas / Year-end)", "March (Board exam travel)" })
            highV.getChildren().add(label("  • " + s, 11, false, C_SUBTEXT));
        VBox lowV = new VBox(6);
        lowV.getChildren().add(label("📉  Off-Peak Months:", 12, true, C_MUTED));
        for (String s : new String[] { "February (post-Republic Day)", "July-August (Heavy monsoon)",
                "Early January (post New Year)" })
            lowV.getChildren().add(label("  • " + s, 11, false, C_SUBTEXT));
        iRow.getChildren().addAll(highV, lowV);
        peakBanner.getChildren().add(iRow);
        v.getChildren().add(peakBanner);

        // Booking vs Cancellation chart
        VBox chart = new VBox(16);
        chart.setStyle(lc2());
        chart.setPadding(new Insets(20, 22, 20, 22));
        chart.getChildren().add(label("📊  Monthly Bookings vs Cancellations", 15, true, C_TEXT));
        long maxC = 0;
        for (long c : mC)
            if (c > maxC)
                maxC = c;
        if (maxC == 0)
            maxC = 1;
        HBox barC = new HBox(6);
        barC.setAlignment(Pos.BOTTOM_LEFT);
        barC.setPrefHeight(180);
        for (int mi = 0; mi < 12; mi++) {
            boolean isPeak = (mi == 4 || mi == 5 || mi == 9 || mi == 10 || mi == 11);
            VBox mc = new VBox(3);
            mc.setAlignment(Pos.BOTTOM_CENTER);
            HBox.setHgrow(mc, Priority.ALWAYS);
            double bH = mC[mi] / (double) maxC * 140;
            if (bH < 2 && mC[mi] > 0)
                bH = 2;
            double cH = mCan[mi] / (double) maxC * 140;
            if (cH < 2 && mCan[mi] > 0)
                cH = 2;
            HBox pair = new HBox(2);
            pair.setAlignment(Pos.BOTTOM_LEFT);
            Region bBar = new Region();
            bBar.setPrefWidth(24);
            bBar.setPrefHeight(bH);
            bBar.setStyle(
                    "-fx-background-color:" + (isPeak ? C_SUCCESS : C_PRIMARY) + "; -fx-background-radius:3 3 0 0;");
            Region cBar = new Region();
            cBar.setPrefWidth(14);
            cBar.setPrefHeight(cH);
            cBar.setStyle("-fx-background-color:" + C_DANGER + "; -fx-background-radius:3 3 0 0;");
            pair.getChildren().addAll(bBar, cBar);
            Label monL = label(mN[mi], 9, isPeak, isPeak ? "#D97706" : C_SUBTEXT);
            monL.setAlignment(Pos.CENTER);
            if (isPeak) {
                Label fireL = label("🔥", 10, false, "#D97706");
                fireL.setAlignment(Pos.CENTER);
                mc.getChildren().add(fireL);
            }
            mc.getChildren().addAll(pair, monL);
            barC.getChildren().add(mc);
        }
        HBox leg = new HBox(14);
        leg.setAlignment(Pos.CENTER_LEFT);
        leg.setPadding(new Insets(8, 0, 0, 0));
        for (String[] l : new String[][] { { C_PRIMARY, "Bookings" }, { C_SUCCESS, "Peak Bookings" },
                { C_DANGER, "Cancellations" } }) {
            HBox li = new HBox(6);
            li.setAlignment(Pos.CENTER_LEFT);
            Region d = new Region();
            d.setPrefWidth(12);
            d.setPrefHeight(12);
            d.setStyle("-fx-background-color:" + l[0] + "; -fx-background-radius:2;");
            li.getChildren().addAll(d, label(l[1], 11, false, C_TEXT));
            leg.getChildren().add(li);
        }
        chart.getChildren().addAll(barC, leg);
        v.getChildren().add(chart);

        // Admin recommendations
        VBox recs = new VBox(12);
        recs.setStyle(lc2());
        recs.setPadding(new Insets(20, 22, 20, 22));
        recs.getChildren().add(label("💡  Admin Action Recommendations", 15, true, C_TEXT));
        for (String[] rec : new String[][] {
                { "🎯", "Increase Frequency",
                        "Add extra trains on Chennai-Delhi & Mumbai-Delhi routes in Oct-Nov peak season." },
                { "💰", "Dynamic Pricing",
                        "Implement 15-25% surge pricing for Tatkal quota during Diwali and Summer vacation." },
                { "📣", "Early Bird Discounts",
                        "Offer 10% off for bookings 90+ days in advance to spread demand evenly." },
                { "🚉", "Smart Waitlist",
                        "Auto-upgrade waitlisted SL passengers to 3A when berths open within 48 hours." },
                { "📊", "Coach Planning",
                        "Pre-allocate extra coaches on top 5 routes for Dec-Jan year-end travel season." }
        }) {
            HBox rh = new HBox(14);
            rh.setPadding(new Insets(12, 14, 12, 14));
            rh.setAlignment(Pos.CENTER_LEFT);
            rh.setStyle(
                    "-fx-background-color:#F8FAFF; -fx-background-radius:8; -fx-border-color:#E2E8F0; -fx-border-radius:8; -fx-border-width:1;");
            VBox rv = new VBox(4);
            HBox.setHgrow(rv, Priority.ALWAYS);
            rv.getChildren().addAll(label(rec[0] + " " + rec[1], 13, true, C_PRIMARY),
                    label(rec[2], 11, false, C_SUBTEXT));
            rh.getChildren().add(rv);
            recs.getChildren().add(rh);
        }
        v.getChildren().add(recs);
        sp.setContent(v);
        return sp;
    }

    // ════════════════════════════════════════════════════════
    // ADMIN — REFUND ANALYTICS
    // ════════════════════════════════════════════════════════
    static Node aRefundAnalytics() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:" + C_BG + "; -fx-background-color:" + C_BG + ";");
        VBox v = new VBox(22);
        v.setPadding(new Insets(24));
        v.setStyle("-fx-background-color:" + C_BG + ";");
        v.getChildren().add(st("🔄  Cancellation & Refund Analytics"));
        v.getChildren()
                .add(label("Track cancellation rates, refund amounts, and identify patterns.", 13, false, C_MUTED));

        List<Booking> cancelled = BOOKINGS.stream().filter(b -> b.status.equals("CAN")).collect(Collectors.toList());
        List<Booking> confirmed = BOOKINGS.stream().filter(b -> b.status.equals("CNF")).collect(Collectors.toList());
        double cancelRate = BOOKINGS.isEmpty() ? 0 : (double) cancelled.size() / BOOKINGS.size() * 100;
        double refundTotal = cancelled.stream().mapToDouble(b -> b.totalFare * 0.85).sum();
        double revTotal = confirmed.stream().mapToDouble(b -> b.totalFare).sum();

        // KPIs
        HBox kRow = new HBox(14);
        for (Object[] k : new Object[][] {
                { "❌", "Total Cancellations", cancelled.size() + "", C_DANGER, "#FDE8E8" },
                { "📊", "Cancellation Rate", String.format("%.1f", cancelRate) + "%", "#D97706", "#FFFBEB" },
                { "💸", "Refunds Issued", "₹" + String.format("%,.0f", refundTotal), C_MUTED, "#F9FAFB" },
                { "💰", "Net Revenue", "₹" + String.format("%,.0f", revTotal - refundTotal), C_SUCCESS, "#DEF7EC" }
        }) {
            VBox kc = new VBox(6);
            kc.setPadding(new Insets(16, 18, 16, 18));
            HBox.setHgrow(kc, Priority.ALWAYS);
            kc.setStyle("-fx-background-color:" + k[4] + "; -fx-background-radius:12; -fx-border-color:" + k[3]
                    + "44; -fx-border-radius:12; -fx-border-width:1;");
            kc.getChildren().addAll(label(k[0] + " " + k[1], 11, false, C_MUTED),
                    label(k[2].toString(), 22, true, k[3].toString()));
            kRow.getChildren().add(kc);
        }
        v.getChildren().add(kRow);

        // Cancellation by class
        VBox classCan = new VBox(14);
        classCan.setStyle(lc2());
        classCan.setPadding(new Insets(20, 22, 20, 22));
        classCan.getChildren().add(label("💺  Cancellations by Seat Class", 15, true, C_TEXT));
        Map<String, long[]> canByClass = new LinkedHashMap<>();
        for (String[] sc : SC)
            canByClass.put(sc[0], new long[] { 0, 0 });
        for (Booking b : BOOKINGS) {
            for (String[] sc : SC) {
                if (b.seatClass.startsWith(sc[0])) {
                    canByClass.get(sc[0])[0]++; // total
                    if (b.status.equals("CAN"))
                        canByClass.get(sc[0])[1]++; // cancelled
                    break;
                }
            }
        }
        for (String[] sc : SC) {
            long[] d = canByClass.get(sc[0]);
            if (d == null || d[0] == 0)
                continue;
            double rate = d[0] > 0 ? (double) d[1] / d[0] * 100 : 0;
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 0, 5, 0));
            Label cL = label(sc[0], 12, true, sc[4]);
            cL.setStyle("-fx-background-color:" + sc[4] + "22; -fx-background-radius:4; -fx-padding:3 9; -fx-text-fill:"
                    + sc[4] + ";");
            cL.setPrefWidth(60);
            Label nL = label(sc[1], 12, false, C_SUBTEXT);
            nL.setPrefWidth(180);
            StackPane barBg = new StackPane();
            HBox.setHgrow(barBg, Priority.ALWAYS);
            barBg.setPrefHeight(22);
            barBg.setStyle("-fx-background-color:#F1F5F9; -fx-background-radius:4;");
            HBox barF = new HBox();
            barF.setPrefWidth(rate / 100 * 300);
            barF.setPrefHeight(22);
            barF.setStyle("-fx-background-color:" + (rate > 30 ? C_DANGER : rate > 15 ? "#D97706" : C_SUCCESS)
                    + "; -fx-background-radius:4;");
            barBg.getChildren().add(barF);
            StackPane.setAlignment(barF, Pos.CENTER_LEFT);
            Label rateL = label(String.format("%.0f", rate) + "% cancel rate (" + d[1] + "/" + d[0] + ")", 11, false,
                    C_MUTED);
            rateL.setPrefWidth(200);
            row.getChildren().addAll(cL, nL, barBg, rateL);
            classCan.getChildren().add(row);
        }
        v.getChildren().add(classCan);

        // Recent cancellations table
        VBox canTable = new VBox(0);
        canTable.setStyle(lc2());
        HBox ctHdr = new HBox(0);
        ctHdr.setPadding(new Insets(12, 18, 12, 18));
        ctHdr.setStyle("-fx-background-color:#FDE8E8; -fx-background-radius:10 10 0 0;");
        for (String[] h : new String[][] { { "PNR", "130" }, { "Train", "180" }, { "Class", "150" }, { "Fare", "110" },
                { "Refund (85%)", "130" }, { "Booked On", "160" } }) {
            Label hl = label(h[0], 11, true, C_DANGER);
            hl.setPrefWidth(Integer.parseInt(h[1]));
            hl.setPadding(new Insets(0, 0, 0, 8));
            ctHdr.getChildren().add(hl);
        }
        Label canHdrLbl = label("❌  Recent Cancellations", 15, true, C_TEXT);
        canHdrLbl.setPadding(new Insets(20, 18, 12, 18));
        canTable.getChildren().addAll(canHdrLbl, ctHdr);
        List<Booking> recentCan = new ArrayList<>(cancelled);
        Collections.reverse(recentCan);
        for (int i = 0; i < Math.min(recentCan.size(), 15); i++) {
            Booking b = recentCan.get(i);
            HBox row = new HBox(0);
            row.setPadding(new Insets(9, 18, 9, 18));
            row.setStyle("-fx-background-color:" + (i % 2 == 0 ? C_SURFACE : "#FFF5F5")
                    + "; -fx-border-color:#FCA5A5; -fx-border-width:0 0 1 0;");
            for (Object[] d : new Object[][] { { b.pnr, 130 }, { b.trainName, 180 }, { b.seatClass, 150 },
                    { "₹" + String.format("%.0f", b.totalFare), 110 },
                    { "₹" + String.format("%.0f", b.totalFare * 0.85), 130 }, { b.bookingDateTime, 160 } }) {
                Label dl = label(d[0].toString(), 11, false, C_TEXT);
                dl.setPrefWidth((int) d[1]);
                dl.setPadding(new Insets(0, 0, 0, 8));
                row.getChildren().add(dl);
            }
            canTable.getChildren().add(row);
        }
        if (cancelled.isEmpty()) {
            Label noData = label("✅  No cancellations recorded yet. Great retention!", 13, false, C_SUCCESS);
            noData.setPadding(new Insets(20, 18, 20, 18));
            canTable.getChildren().add(noData);
        }
        v.getChildren().add(canTable);
        sp.setContent(v);
        return sp;
    }

    // ── Seat map helper methods ──────────────────────────────
    static String seatAvailStyle() {
        return "-fx-background-color:#DEF7EC; -fx-text-fill:#057A55; -fx-background-radius:6; -fx-border-color:#6EE7B7; -fx-border-radius:6; -fx-border-width:1; -fx-font-size:10px; -fx-cursor:hand; -fx-alignment:center;";
    }

    static String seatSelStyle() {
        return "-fx-background-color:#1A56DB; -fx-text-fill:white; -fx-background-radius:6; -fx-font-size:10px; -fx-font-weight:bold; -fx-cursor:hand; -fx-alignment:center; -fx-border-color:#1E40AF; -fx-border-radius:6; -fx-border-width:1;";
    }

    static String seatBookedStyle() {
        return "-fx-background-color:#FDE8E8; -fx-text-fill:#C81E1E; -fx-background-radius:6; -fx-border-color:#FCA5A5; -fx-border-radius:6; -fx-border-width:1; -fx-font-size:10px; -fx-alignment:center;";
    }

    static Label makeSeatLabel(String seatId, int bay, String berthType, boolean isBooked, boolean isSel) {
        // Determine berth icon
        String icon = berthType.equals("LB") ? "─"
                : berthType.equals("MB") ? "═"
                        : berthType.equals("UB") ? "▔"
                                : berthType.equals("SL") ? "◂"
                                        : berthType.equals("SU") ? "◃"
                                                : berthType.equals("W") ? "W" : berthType.equals("A") ? "A" : "M";
        Label l = new Label(bay + "\n" + berthType);
        l.setPrefWidth(50);
        l.setPrefHeight(42);
        l.setAlignment(Pos.CENTER);
        l.setWrapText(false);
        l.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 9));
        l.setStyle(isBooked ? seatBookedStyle() : isSel ? seatSelStyle() : seatAvailStyle());
        return l;
    }

    // ════════════════════════════════════════════════════════
    // NAV BARS
    // ════════════════════════════════════════════════════════
    static Node navBar(String active) {
        VBox nav = new VBox(0);
        HBox top = new HBox(0);
        top.setStyle("-fx-background-color:" + C_NAV + ";");
        top.setPadding(new Insets(0, 24, 0, 24));
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPrefHeight(50);
        Label logo = label("🚆  RailConnect", 16, true, C_NAV_TEXT);
        logo.setStyle("-fx-cursor:hand;");
        logo.setOnMouseClicked(e -> showHome());
        HBox sp2 = new HBox();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        Label userL = label("👤  " + CURRENT_USER.fullName, 12, false, "#BFDBFE");
        userL.setPadding(new Insets(0, 12, 0, 12));
        userL.setStyle("-fx-cursor:hand;");
        userL.setOnMouseClicked(e -> showProfile());
        Button btnLogout = new Button("Logout");
        btnLogout.setStyle(
                "-fx-background-color:rgba(255,255,255,0.2); -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:6 16; -fx-background-radius:6; -fx-cursor:hand; -fx-font-size:12px; -fx-border-color:rgba(255,255,255,0.4); -fx-border-radius:6;");
        btnLogout.setOnAction(e -> {
            CURRENT_USER = null;
            showLogin();
        });
        top.getChildren().addAll(logo, sp2, userL, btnLogout);
        HBox tabs = new HBox(0);
        tabs.setStyle(
                "-fx-background-color:" + C_SURFACE + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");
        for (String[] l : new String[][] { { "Home", "🏠 Home" }, { "Search Results", "🔍 Book Ticket" },
                { "My Bookings", "📋 My Bookings" }, { "PNR Status", "📍 PNR Status" },
                { "Seat Availability", "💺 Seat Avail" }, { "Live Train Tracking", "🛰 Live Track" },
                { "Cancel Ticket", "❌ Cancel" } }) {
            boolean cur = active.equals(l[0]);
            Label lk = label(l[1], 13, cur, cur ? C_PRIMARY : C_SUBTEXT);
            lk.setPadding(new Insets(12, 18, 12, 18));
            String aS = "-fx-cursor:hand; -fx-border-color:" + C_PRIMARY
                    + "; -fx-border-width:0 0 3 0; -fx-background-color:" + C_PRIMARY_L + ";";
            String nS = "-fx-cursor:hand;";
            lk.setStyle(cur ? aS : nS);
            lk.setOnMouseEntered(ev -> {
                if (!cur)
                    lk.setStyle("-fx-cursor:hand; -fx-background-color:" + C_BG + ";");
            });
            lk.setOnMouseExited(ev -> lk.setStyle(cur ? aS : nS));
            lk.setOnMouseClicked(ev -> {
                switch (l[0]) {
                    case "Home":
                        showHome();
                        break;
                    case "Search Results":
                        showSearchAll();
                        break;
                    case "My Bookings":
                        showMyBookings();
                        break;
                    case "PNR Status":
                        showPNRStatus();
                        break;
                    case "Seat Availability":
                        showSeatAvailability();
                        break;
                    case "Live Train Tracking":
                        showLiveTracking();
                        break;
                    case "Cancel Ticket":
                        showCancelTicket();
                        break;
                }
            });
            tabs.getChildren().add(lk);
        }
        nav.getChildren().addAll(top, tabs);
        return nav;
    }

    static Node adminNav() {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setStyle("-fx-background-color:" + C_NAV + ";");
        nav.setPadding(new Insets(0, 24, 0, 24));
        nav.setPrefHeight(56);
        Label logo = label("🚆  RailConnect — Admin Portal", 17, true, C_NAV_TEXT);
        HBox sp2 = new HBox();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        Button btnLogout = new Button("Logout");
        btnLogout.setStyle(
                "-fx-background-color:rgba(255,255,255,0.2); -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 18; -fx-background-radius:8; -fx-cursor:hand; -fx-border-color:rgba(255,255,255,0.4); -fx-border-radius:8;");
        btnLogout.setOnAction(e -> {
            CURRENT_USER = null;
            showLogin();
        });
        nav.getChildren().addAll(logo, sp2, label("🔧  " + CURRENT_USER.username, 13, false, "#BFDBFE"),
                new Label("   "), btnLogout);
        return nav;
    }

    static Node guestNav() {
        HBox nav = new HBox(16);
        nav.setStyle("-fx-background-color:" + C_NAV + ";");
        nav.setPadding(new Insets(10, 30, 10, 30));
        nav.setAlignment(Pos.CENTER_LEFT);
        Label logo = label("🚆  RailConnect", 18, true, C_NAV_TEXT);
        HBox sp2 = new HBox();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        Button btnBack = new Button("← Back to Login");
        btnBack.setStyle(
                "-fx-background-color:rgba(255,255,255,0.2); -fx-text-fill:white; -fx-font-size:12px; -fx-padding:6 14; -fx-background-radius:6; -fx-cursor:hand;");
        btnBack.setOnAction(e -> showLogin());
        nav.getChildren().addAll(logo, sp2, btnBack);
        return nav;
    }

    // ════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ════════════════════════════════════════════════════════
    static void setScene(Parent root) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        PRIMARY_STAGE.setScene(scene);
        PRIMARY_STAGE.setMaximized(true);
    }

    // Professional font stack — uses best available system font
    static final String FONT_FAMILY = resolveFont();

    static String resolveFont() {
        // Try professional fonts in priority order
        for (String f : new String[] { "Inter", "Segoe UI", "SF Pro Display", "Roboto", "Nunito", "Arial" }) {
            if (javafx.scene.text.Font.getFamilies().contains(f))
                return f;
        }
        return "System";
    }

    static Label label(String t, int size, boolean bold, String color) {
        Label l = new Label(t);
        l.setFont(Font.font(FONT_FAMILY, bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setTextFill(Color.web(color));
        return l;
    }

    // Short helpers
    static Label st(String t) {
        Label l = label(t, 20, true, C_TEXT);
        l.setPadding(new Insets(0, 0, 4, 0));
        return l;
    }

    static Label sh(String step, String title) {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        Label sL = label(step, 12, true, C_SURFACE);
        sL.setStyle("-fx-background-color:" + C_PRIMARY + "; -fx-background-radius:50; -fx-padding:3 9;");
        Label tL = label(title, 15, true, C_TEXT);
        Label r = new Label();
        r.setGraphic(new HBox(10, sL, tL));
        return r;
    }

    static TextField lf(String p) {
        TextField tf = new TextField();
        tf.setPromptText(p);
        tf.setStyle(
                "-fx-background-color:#FFFFFF; -fx-text-fill:" + C_TEXT + "; -fx-prompt-text-fill:" + C_MUTED + "; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:10 14; -fx-font-size:13px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);");
        tf.setPrefHeight(42);
        return tf;
    }

    static PasswordField lp(String p) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(p);
        pf.setStyle(
                "-fx-background-color:#FFFFFF; -fx-text-fill:" + C_TEXT + "; -fx-prompt-text-fill:" + C_MUTED + "; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:10 14; -fx-font-size:13px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);");
        pf.setPrefHeight(42);
        return pf;
    }

    static <T> ComboBox<T> lc(ObservableList<T> items) {
        ComboBox<T> cb = new ComboBox<>(items);
        cb.setStyle(
                "-fx-background-color:#FFFFFF; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);");
        cb.setCellFactory(lv -> new ListCell<T>() {
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText(null);
                else {
                    setText(item.toString());
                    setStyle("-fx-text-fill:" + C_TEXT + "; -fx-background-color:#FFFFFF; -fx-font-size:13px;");
                }
            }
        });
        cb.setButtonCell(new ListCell<T>() {
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText(null);
                else {
                    setText(item.toString());
                    setStyle("-fx-text-fill:#111928; -fx-background-color:transparent;");
                }
            }
        });
        return cb;
    }

    static ComboBox<Integer> lic(ObservableList<Integer> items) {
        ComboBox<Integer> cb = new ComboBox<>(items);
        cb.setStyle(
                "-fx-background-color:#FFFFFF; -fx-border-color:#D1D5DB; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px;");
        cb.setCellFactory(lv -> new ListCell<Integer>() {
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText(null);
                else {
                    setText(item.toString());
                    setStyle("-fx-text-fill:#111928; -fx-background-color:#FFFFFF;");
                }
            }
        });
        cb.setButtonCell(new ListCell<Integer>() {
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText(null);
                else {
                    setText(item.toString());
                    setStyle("-fx-text-fill:#111928; -fx-background-color:transparent;");
                }
            }
        });
        return cb;
    }

    static void styleDp(DatePicker dp) {
        dp.setStyle(
                "-fx-background-color:#FFFFFF; -fx-border-color:#D1D5DB; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px;");
        dp.getEditor().setStyle("-fx-background-color:#FFFFFF; -fx-text-fill:#111928; -fx-font-size:13px;");
    }

    static Button pb(String text, String bg) {
        Button b = new Button(text);
        String base = "-fx-background-color:" + bg + "; -fx-text-fill:#FFFFFF; -fx-font-weight:bold; -fx-font-size:13px; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);";
        String hov = "-fx-background-color:derive(" + bg + ", -15%); -fx-text-fill:#FFFFFF; -fx-font-weight:bold; -fx-font-size:13px; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 5);";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    static Button gb(String text) {
        Button b = new Button(text);
        String base = "-fx-background-color:transparent; -fx-text-fill:" + C_SUBTEXT + "; -fx-font-size:13px; -fx-padding:10 22; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand;";
        String hov = "-fx-background-color:" + C_PRIMARY_L + "; -fx-text-fill:" + C_PRIMARY + "; -fx-font-size:13px; -fx-padding:10 22; -fx-border-color:" + C_PRIMARY + "; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2);";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    static Button lb(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:transparent; -fx-text-fill:" + C_PRIMARY
                + "; -fx-font-size:12px; -fx-cursor:hand; -fx-padding:0;");
        return b;
    }

    static HBox fr(String k, String v) {
        HBox h = new HBox();
        h.setAlignment(Pos.CENTER_LEFT);
        Label kl = label(k, 12, false, C_MUTED);
        HBox.setHgrow(kl, Priority.ALWAYS);
        h.getChildren().addAll(kl, label(v, 12, true, C_TEXT));
        return h;
    }

    static Region vg(int h) {
        Region r = new Region();
        r.setMinHeight(h);
        return r;
    }

    static String lc2() {
        return "-fx-background-color:" + C_SURFACE + "; -fx-background-radius:12; -fx-border-color:" + C_BORDER
                + "; -fx-border-radius:12; -fx-border-width:1; -fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.08),12,0,0,4);";
    }

    static String normCard(String c) {
        return "-fx-background-color:" + c + "0D; -fx-background-radius:8; -fx-border-color:" + C_BORDER
                + "; -fx-border-radius:8; -fx-border-width:1; -fx-cursor:hand;";
    }

    static String selCard(String c) {
        return "-fx-background-color:" + c + "22; -fx-background-radius:8; -fx-border-color:" + c
                + "; -fx-border-radius:8; -fx-border-width:2; -fx-cursor:hand;";
    }

    static String pm(boolean sel) {
        return sel
                ? "-fx-background-color:" + C_PRIMARY_L + "; -fx-background-radius:10; -fx-border-color:" + C_PRIMARY
                        + "; -fx-border-radius:10; -fx-border-width:2;"
                : "-fx-background-color:" + C_SURFACE + "; -fx-background-radius:10; -fx-border-color:" + C_BORDER
                        + "; -fx-border-radius:10; -fx-border-width:1;";
    }

    static String qStyle(String c) {
        return "-fx-background-color:" + c + "22; -fx-background-radius:12; -fx-border-color:" + c
                + "44; -fx-border-radius:12; -fx-border-width:1; -fx-cursor:hand;";
    }

    static ObservableList<String> getStations() {
        Set<String> s = new TreeSet<>();
        // Add all India stations
        for (String st : ALL_INDIA_STATIONS)
            s.add(st);
        // Also add any train source/destination not already in list
        for (Train t : TRAINS) {
            s.add(t.source);
            s.add(t.destination);
        }
        for (String st : CUSTOM_STATIONS) {
            s.add(st);
        }
        try {
            if (DatabaseManager.testConnection()) {
                java.sql.ResultSet rs = DatabaseManager.getAllStationRecords();
                if (rs != null) {
                    while (rs.next()) s.add(rs.getString("station_name"));
                }
            }
        } catch (Exception ex) {}
        return FXCollections.observableArrayList(s);
    }

    static Optional<Train> trainByNo(String no) {
        return TRAINS.stream().filter(t -> t.trainNo.equals(no)).findFirst();
    }

    static void generatePDFTicket(Booking b) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save E-Ticket as HTML");
        fc.setInitialFileName("Ticket_" + b.pnr + ".html");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("HTML Files", "*.html"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        java.io.File file = fc.showSaveDialog(PRIMARY_STAGE);
        if (file == null)
            return;

        boolean cnf = b.status.equals("CNF");
        String sc = cnf ? "#057A55" : "#C81E1E";
        String sb = cnf ? "#DEF7EC" : "#FDE8E8";
        String st = cnf ? "CONFIRMED" : "CANCELLED";
        String si = cnf ? "&#x2705;" : "&#x274C;";
        String genTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        StringBuilder pr = new StringBuilder();
        int pn = 1;
        for (Passenger p : b.passengers) {
            pr.append("<tr style='background:").append(pn % 2 == 0 ? "#F8FAFF" : "#fff").append("'>")
                    .append("<td>").append(pn++).append("</td>")
                    .append("<td><b>").append(p.name).append("</b></td>")
                    .append("<td>").append(p.age).append("</td>")
                    .append("<td>").append(p.gender).append("</td>")
                    .append("<td>").append(p.berthPref).append("</td>")
                    .append("<td><span style='background:#EBF0FF;color:#1A56DB;padding:2px 10px;border-radius:4px;font-weight:700;font-size:11px'>")
                    .append(p.seat).append("</span></td>")
                    .append("</tr>\n");
        }

        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>"
                + "<title>RailConnect E-Ticket " + b.pnr + "</title>"
                + "<style>"
                + "*{box-sizing:border-box;margin:0;padding:0}"
                + "body{font-family:'Segoe UI',Arial,sans-serif;background:#F0F4FF;display:flex;flex-direction:column;align-items:center;padding:28px 10px}"
                + ".ticket{width:820px;border-radius:14px;overflow:hidden;box-shadow:0 8px 40px rgba(0,0,0,.18);border:1px solid #CBD5E1}"
                + ".hdr{background:#1A3A8F;padding:16px 28px;display:flex;align-items:center;justify-content:space-between}"
                + ".hdr-logo{color:#fff;font-size:19px;font-weight:700}"
                + ".pnr-pill{background:rgba(255,255,255,.2);color:#fff;padding:5px 16px;border-radius:6px;font-weight:700;font-size:14px}"
                + ".st-pill{padding:5px 14px;border-radius:6px;font-weight:700;font-size:13px;background:" + sb
                + ";color:" + sc + ";border:1px solid " + sc + "}"
                + ".banner{background:" + sb + ";border-bottom:2px solid " + sc
                + ";padding:14px 28px;display:flex;align-items:center;gap:12px}"
                + ".b-title{font-size:17px;font-weight:700;color:" + sc + "}"
                + ".b-sub{font-size:12px;color:" + sc + ";opacity:.85;margin-top:3px}"
                + ".route-bar{background:#F8FAFF;padding:20px 28px;display:flex;align-items:center;border-bottom:1px solid #E2E8F0}"
                + ".stn{flex:1}.stn-name{font-size:25px;font-weight:700;color:#111928}"
                + ".stn-lbl{font-size:11px;color:#6B7280;margin-top:3px}"
                + ".mid{flex:2;text-align:center}"
                + ".mid .tname{font-size:12px;font-weight:600;color:#6B7280;margin-bottom:6px}"
                + ".mid .arr{font-size:22px;color:#D1D5DB;letter-spacing:2px}"
                + ".mid .jd{font-size:13px;font-weight:700;color:#1A56DB;margin-top:6px}"
                + ".stn.r{text-align:right}"
                + ".grid{padding:18px 28px;background:#fff;display:grid;grid-template-columns:repeat(4,1fr);gap:14px;border-bottom:1px solid #E2E8F0}"
                + ".gi label{font-size:10px;color:#9CA3AF;text-transform:uppercase;letter-spacing:.5px;display:block;margin-bottom:3px}"
                + ".gi span{font-size:13px;font-weight:600;color:#111928}"
                + ".pax{padding:0 28px 22px;background:#fff}"
                + ".pax-h{font-size:14px;font-weight:700;color:#111928;padding:16px 0 10px}"
                + "table{width:100%;border-collapse:collapse;border:1px solid #E2E8F0;border-radius:8px;overflow:hidden}"
                + "th{background:#1A3A8F;color:#fff;padding:10px 14px;font-size:11px;text-align:left;font-weight:600;letter-spacing:.4px}"
                + "td{padding:9px 14px;font-size:12px;color:#374151;border-bottom:1px solid #F1F5F9}"
                + ".fare{background:linear-gradient(135deg,#1A3A8F,#2563EB);padding:16px 28px;display:flex;justify-content:space-between;align-items:center}"
                + ".fare .lbl{color:rgba(255,255,255,.7);font-size:11px}"
                + ".fare .amt{color:#fff;font-size:22px;font-weight:700}"
                + ".fare .pm{color:#BFDBFE;font-size:13px}"
                + ".foot{background:#F1F5F9;padding:11px 28px;font-size:11px;color:#6B7280}"
                + ".gen{margin-top:10px;font-size:10px;color:#9CA3AF;text-align:center}"
                + ".print-btn{margin-top:16px;padding:10px 34px;background:#1A56DB;color:#fff;border:none;border-radius:8px;font-size:14px;font-weight:700;cursor:pointer;box-shadow:0 2px 12px rgba(26,86,219,.3)}"
                + ".print-btn:hover{background:#1340a8}"
                + "@media print{body{background:#fff;padding:0}.ticket{box-shadow:none}.print-btn,.gen{display:none}}"
                + "</style></head><body>"
                + "<div class='ticket'>"
                // HEADER
                + "<div class='hdr'>"
                + "<div class='hdr-logo'>&#x1F686;&nbsp; RailConnect &mdash; E-Ticket</div>"
                + "<div style='display:flex;gap:10px;align-items:center'>"
                + "<div class='pnr-pill'>PNR: " + b.pnr + "</div>"
                + "<div class='st-pill'>" + si + " " + st + "</div>"
                + "</div></div>"
                // BANNER
                + "<div class='banner'>"
                + "<div style='font-size:26px'>" + si + "</div>"
                + "<div><div class='b-title'>Booking " + st + "</div>"
                + "<div class='b-sub'>"
                + (cnf ? "Your e-ticket is confirmed. Happy Journey! &#x1F686;"
                        : "Ticket cancelled. Refund within 5&ndash;7 business days.")
                + "</div></div>"
                + "</div>"
                // ROUTE BAR
                + "<div class='route-bar'>"
                + "<div class='stn'><div class='stn-name'>" + b.source
                + "</div><div class='stn-lbl'>Departure</div></div>"
                + "<div class='mid'><div class='tname'>" + b.trainName + " &nbsp;&bull;&nbsp; #" + b.trainNo + "</div>"
                + "<div class='arr'>&mdash;&mdash;&mdash;&mdash;&mdash;&#x25BA;</div>"
                + "<div class='jd'>&#x1F4C5;&nbsp;" + b.journeyDate + "</div></div>"
                + "<div class='stn r'><div class='stn-name'>" + b.destination
                + "</div><div class='stn-lbl'>Arrival</div></div>"
                + "</div>"
                // DETAIL GRID
                + "<div class='grid'>"
                + "<div class='gi'><label>Train No</label><span>#" + b.trainNo + "</span></div>"
                + "<div class='gi'><label>Seat Class</label><span>" + b.seatClass + "</span></div>"
                + "<div class='gi'><label>Quota</label><span>" + b.quota + "</span></div>"
                + "<div class='gi'><label>Passengers</label><span>" + b.passengers.size() + "</span></div>"
                + "<div class='gi'><label>Payment Mode</label><span>" + b.paymentMode + "</span></div>"
                + "<div class='gi'><label>Booked On</label><span>" + b.bookingDateTime + "</span></div>"
                + "<div class='gi'><label>Status</label><span style='color:" + sc + "'>" + st + "</span></div>"
                + "<div class='gi'><label>Journey Date</label><span>" + b.journeyDate + "</span></div>"
                + "</div>"
                // PASSENGER TABLE
                + "<div class='pax'><div class='pax-h'>&#x1F465;&nbsp; Passenger Details</div>"
                + "<table><tr><th>#</th><th>Name</th><th>Age</th><th>Gender</th><th>Berth Pref</th><th>Seat No</th></tr>"
                + pr
                + "</table></div>"
                // FARE STRIP
                + "<div class='fare'>"
                + "<div><div class='lbl'>Total Amount Paid</div><div class='amt'>&#x20B9;"
                + String.format("%.2f", b.totalFare) + "</div></div>"
                + "<div style='text-align:right'><div class='lbl'>Payment Mode</div><div class='pm'>" + b.paymentMode
                + "</div></div>"
                + "</div>"
                // FOOTER
                + "<div class='foot'>&#x26A0;&#xFE0F;&nbsp; Carry valid Government Photo ID. This ticket is valid only with original ID proof.</div>"
                + "</div>"
                + "<div class='gen'>Generated by RailConnect &mdash; " + genTime + "</div>"
                + "<button class='print-btn' onclick='window.print()'>&#x1F5A8;&nbsp; Print Ticket</button>"
                + "</body></html>";

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.print(html);
            showAlert("✅  HTML Ticket saved!\n" + file.getAbsolutePath()
                    + "\n\nOpen in any browser to view or print.");
        } catch (Exception ex) {
            showAlert("❌  Could not save ticket: " + ex.getMessage());
        }
    }

    static void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("RailConnect");
        a.getDialogPane().setStyle("-fx-background-color:" + C_SURFACE + "; -fx-font-size:13px;");
        a.showAndWait();
    }

    // ════════════════════════════════════════════════════════
    // AUTOCOMPLETE FIELD HELPER
    // Returns a VBox (StackPane over Popup) wrapping a styled TextField.
    // outHolder[0] is set to the TextField so the caller can read typed text.
    // onSelect is called with the chosen value.
    // ════════════════════════════════════════════════════════
    @FunctionalInterface
    interface SelectHandler {
        void onSelect(String value);
    }

    static VBox makeAutoCompleteField(String prompt, ObservableList<String> allItems,
            SelectHandler onSelect, TextField[] outHolder) {
        TextField tf = lf(prompt);
        outHolder[0] = tf;

        ListView<String> lv = new ListView<>();
        lv.setStyle("-fx-background-color:#FFFFFF; -fx-border-color:#D1D5DB; -fx-border-radius:0 0 8 8;"
                + "-fx-background-radius:0 0 8 8; -fx-font-size:13px;");
        lv.setPrefHeight(160);
        lv.setMaxHeight(160);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.getContent().add(lv);

        // Filter items as user types
        tf.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV.trim().toLowerCase();
            if (q.isEmpty()) {
                popup.hide();
                return;
            }
            List<String> matched = allItems.stream()
                    .filter(s -> s.toLowerCase().contains(q))
                    .limit(12)
                    .collect(Collectors.toList());
            if (matched.isEmpty()) {
                popup.hide();
                return;
            }
            lv.setItems(FXCollections.observableArrayList(matched));
            lv.setPrefWidth(tf.getWidth() > 0 ? tf.getWidth() : 200);
            if (!popup.isShowing()) {
                javafx.geometry.Bounds b2 = tf.localToScreen(tf.getBoundsInLocal());
                if (b2 != null)
                    popup.show(tf, b2.getMinX(), b2.getMaxY());
            }
        });

        // Pick from list
        lv.setOnMouseClicked(ev -> {
            String sel = lv.getSelectionModel().getSelectedItem();
            if (sel != null) {
                tf.setText(sel);
                onSelect.onSelect(sel);
                popup.hide();
            }
        });

        // Keyboard: Enter or Down-arrow to pick
        tf.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.DOWN && popup.isShowing()) {
                lv.requestFocus();
                lv.getSelectionModel().selectFirst();
            } else if (ev.getCode() == KeyCode.ENTER) {
                String sel = lv.getSelectionModel().getSelectedItem();
                if (sel == null && !lv.getItems().isEmpty())
                    sel = lv.getItems().get(0);
                if (sel != null) {
                    tf.setText(sel);
                    onSelect.onSelect(sel);
                    popup.hide();
                }
            } else if (ev.getCode() == KeyCode.ESCAPE) {
                popup.hide();
            }
        });
        lv.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                String sel = lv.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    tf.setText(sel);
                    onSelect.onSelect(sel);
                    popup.hide();
                    tf.requestFocus();
                }
            }
        });

        VBox wrapper = new VBox(tf);
        wrapper.setStyle("-fx-padding:0;");
        return wrapper;
    }
}
