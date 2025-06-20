package portfolioadminapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.SoftBevelBorder; // For button effects
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter; // For button hover effects
import java.awt.event.MouseEvent;   // For button hover effects
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.awt.geom.Point2D; // For GradientPaint
import java.util.List;
import java.util.ArrayList;
import java.util.Collections; // For sorting categories
import java.util.UUID; // NEW: Added for generating unique filenames for images


/**
 * Custom JPanel for drawing a linear gradient background.
 */
class GradientPanel extends JPanel {
    private Color color1;
    private Color color2;
    private boolean horizontal;

    /**
     * Constructs a GradientPanel with two colors and an orientation.
     * @param c1 The starting color of the gradient.
     * @param c2 The ending color of the gradient.
     * @param horizontal True for a horizontal gradient, false for a vertical gradient.
     */
    public GradientPanel(Color c1, Color c2, boolean horizontal) {
        this.color1 = c1;
        this.color2 = c2;
        this.horizontal = horizontal;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Improve rendering quality for gradients
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();

        GradientPaint gp;
        if (horizontal) {
            // Horizontal gradient from left to right
            gp = new GradientPaint(0, 0, color1, w, 0, color2);
        } else {
            // Vertical gradient from top to bottom
            gp = new GradientPaint(0, 0, color1, 0, h, color2);
        }
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }
}

/**
 * Custom JButton that supports gradient backgrounds and hover effects.
 */
class GradientButton extends JButton {
    private Color startColor;
    private Color endColor;
    private Color hoverStartColor;
    private Color hoverEndColor;
    private boolean hovered = false;

    /**
     * Constructs a GradientButton with specified text, normal gradient colors, and hover gradient colors.
     * @param text The text to display on the button.
     * @param start The starting color of the normal gradient.
     * @param end The ending color of the normal gradient.
     * @param hoverStart The starting color of the hover gradient.
     * @param hoverEnd The ending color of the hover gradient.
     */
    public GradientButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd) {
        super(text);
        this.startColor = start;
        this.endColor = end;
        this.hoverStartColor = hoverStart;
        this.hoverEndColor = hoverEnd;

        setFont(PortfolioAdminApp.FONT_BUTTON);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setContentAreaFilled(false); // Don't fill content area, we'll paint it
        setBorderPainted(false); // We'll use a custom border
        setOpaque(false); // Make it opaque for custom painting
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Add a compound border for padding and a subtle bevel effect
        setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(220, 220, 220, 80), new Color(80, 80, 80, 80)), // Softer bevel
                new EmptyBorder(10, 20, 10, 20) // Default padding
        ));
        putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific for rounded corners

        // Add mouse listeners for hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                hovered = true;
                repaint(); // Repaint to show hover gradient
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                hovered = false;
                repaint(); // Repaint to show normal gradient
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Anti-aliasing for smooth edges
        GradientPaint gp;
        if (hovered) {
            gp = new GradientPaint(0, 0, hoverStartColor, 0, getHeight(), hoverEndColor);
        } else {
            gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
        }
        g2d.setPaint(gp);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded corners for the filled area
        g2d.dispose();
        super.paintComponent(g); // Paint text and icon on top of the gradient
    }
}


/**
 * Main class for the Portfolio Admin GUI application.
 * This application allows administrators to manage portfolio projects, skills,
 * 'About Me' content, and contact information through a graphical user interface.
 * It connects to a MySQL database using JDBC.
 */
public class PortfolioAdminApp extends JFrame {

    private CardLayout mainCardLayout;
    private JPanel mainContentPanel; // This panel will hold different views (Login, Dashboard, Management panels)

    // --- Global Design Constants ---
    // Colors (Vibrant, professional palette with defined gradient steps)
    public static final Color PRIMARY_BLUE = new Color(0, 123, 255); // Vibrant Royal Blue
    public static final Color ACCENT_GREEN = new Color(40, 167, 69);   // Vibrant Green
    public static final Color ACCENT_RED = new Color(220, 53, 69);     // Vibrant Red
    public static final Color ACCENT_ORANGE = new Color(255, 193, 7);  // Vibrant Amber/Orange
    public static final Color NEUTRAL_GREY = new Color(108, 117, 125);  // Medium Grey for secondary actions
    public static final Color ACCENT_CYAN = new Color(23, 162, 184); // Cyan for restore button

    // Defined gradient start/end colors for buttons (normal state)
    public static final Color GRADIENT_PRIMARY_BLUE_START = new Color(52, 152, 219); // Brighter blue
    public static final Color GRADIENT_PRIMARY_BLUE_END = new Color(41, 128, 185);   // Deeper blue
    public static final Color GRADIENT_ACCENT_GREEN_START = new Color(46, 204, 113); // Brighter green
    public static final Color GRADIENT_ACCENT_GREEN_END = new Color(39, 174, 96);   // Deeper green
    public static final Color GRADIENT_ACCENT_RED_START = new Color(231, 76, 60);   // Brighter red
    public static final Color GRADIENT_ACCENT_RED_END = new Color(192, 57, 43);     // Deeper red
    public static final Color GRADIENT_ACCENT_ORANGE_START = new Color(243, 156, 18); // Brighter orange
    public static final Color GRADIENT_ACCENT_ORANGE_END = new Color(211, 84, 0);   // Deeper orange
    public static final Color GRADIENT_NEUTRAL_GREY_START = new Color(189, 195, 199); // Brighter grey
    public static final Color GRADIENT_NEUTRAL_GREY_END = new Color(127, 140, 141);   // Deeper grey
    public static final Color GRADIENT_ACCENT_CYAN_START = new Color(26, 188, 156);  // Brighter cyan
    public static final Color GRADIENT_ACCENT_CYAN_END = new Color(22, 160, 133);    // Deeper cyan

    // Defined gradient start/end colors for buttons (hover state - slightly brighter/more intense)
    public static final Color GRADIENT_PRIMARY_BLUE_HOVER_START = new Color(74, 169, 240);
    public static final Color GRADIENT_PRIMARY_BLUE_HOVER_END = new Color(63, 143, 200);
    public static final Color GRADIENT_ACCENT_GREEN_HOVER_START = new Color(68, 226, 135);
    public static final Color GRADIENT_ACCENT_GREEN_HOVER_END = new Color(58, 196, 118);
    public static final Color GRADIENT_ACCENT_RED_HOVER_START = new Color(255, 98, 82);
    public static final Color GRADIENT_ACCENT_RED_HOVER_END = new Color(212, 79, 65);
    public static final Color GRADIENT_ACCENT_ORANGE_HOVER_START = new Color(255, 178, 40);
    public static final Color GRADIENT_ACCENT_ORANGE_HOVER_END = new Color(231, 106, 20);
    public static final Color GRADIENT_NEUTRAL_GREY_HOVER_START = new Color(209, 215, 219);
    public static final Color GRADIENT_NEUTRAL_GREY_HOVER_END = new Color(147, 160, 161);
    public static final Color GRADIENT_ACCENT_CYAN_HOVER_START = new Color(48, 208, 178);
    public static final Color GRADIENT_ACCENT_CYAN_HOVER_END = new Color(42, 180, 153);


    // Main background gradient for the JFrame
    public static final Color BACKGROUND_LIGHT_START = new Color(200, 230, 255); // Soft Sky Blue
    public static final Color BACKGROUND_LIGHT_END = new Color(255, 255, 255);   // Pure White
    public static final Color BACKGROUND_PANEL = Color.WHITE; // White for content panels
    public static final Color TEXT_DARK = new Color(26, 35, 126); // Dark Indigo for strong contrast
    public static final Color BORDER_COLOR = new Color(150, 150, 150); // Slightly darker grey for definition

    // Fonts (Arial for a clean, modern, and professional appearance)
    public static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 48); // Larger title
    public static final Font FONT_SUBTITLE = new Font("Arial", Font.BOLD, 32); // Larger subtitle
    public static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 20); // Header for sections/tables
    public static final Font FONT_BODY = new Font("Arial", Font.PLAIN, 16); // Standard text
    public static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 18); // Button text
    public static final Font FONT_SMALL_ITALIC = new Font("Arial", Font.ITALIC, 14); // Small italic text

    // --- IMPORTANT: Configure these paths and URLs for your web server ---
    // This is the absolute file system path on your server where images will be saved.
    // Example for Windows XAMPP: "C:\\xampp\\htdocs\\your_portfolio_site\\assets\\project_images\\"
    // Example for Linux/Apache: "/var/www/html/your_portfolio_site/assets/project_images/"
    public static final String PROJECT_IMAGE_BASE_DIR = "C:\\xampp\\htdocs\\your_portfolio_site\\assets\\project_images\\"; // <--- **CHANGE THIS**

    // This is the public web URL that corresponds to the directory above.
    // Example: "http://localhost/your_portfolio_site/assets/project_images/" or "http://yourdomain.com/assets/project_images/"
    public static final String PROJECT_IMAGE_BASE_URL = "http://localhost/your_portfolio_site/assets/project_images/"; // <--- **CHANGE THIS**


    /**
     * Constructor for the PortfolioAdminApp.
     * Sets up the main frame, card layout, and initial panels (Login and Dashboard).
     */
    public PortfolioAdminApp() {
        // Set a modern Look and Feel for the application
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // Fallback to default if Nimbus is not available
            System.err.println("Nimbus LookAndFeel not available, falling back to default.");
        }

        setTitle("Portfolio Admin Dashboard");
        setSize(1000, 700); // Increased size for better layout and design elements
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        
         try {
            // Use getResource() to load the image from the classpath, which works when
            // running from a JAR file.
            Image icon = new ImageIcon(getClass().getResource("/Oppack.png")).getImage();
            setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
            // Handle the error, e.g., by logging it or using a default icon
        }

        // Create the main content panel with a gradient background
        // This panel will directly hold the LoginPanel or AdminDashboardPanel via CardLayout
        GradientPanel gradientBackgroundPanel = new GradientPanel(BACKGROUND_LIGHT_START, BACKGROUND_LIGHT_END, false); // Vertical gradient
        gradientBackgroundPanel.setLayout(new CardLayout()); // Use CardLayout directly here
        
        mainCardLayout = (CardLayout) gradientBackgroundPanel.getLayout(); // Get the CardLayout instance
        mainContentPanel = gradientBackgroundPanel; // The background panel itself is now the main content panel for card layout

        // Initialize panels and add them to mainContentPanel
        LoginPanel loginPanel = new LoginPanel(this);
        loginPanel.setOpaque(false); // Make loginPanel transparent to show gradient
        mainContentPanel.add(loginPanel, "Login");

        // Set the gradient panel as the content pane
        setContentPane(gradientBackgroundPanel);

        // Show the login panel initially
        mainCardLayout.show(mainContentPanel, "Login");

        createTables(); // Ensure database tables are created on app startup
    }

    /**
     * Switches to the Admin Dashboard panel after successful login.
     */
    public void showAdminDashboard() {
        AdminDashboardPanel dashboardPanel = new AdminDashboardPanel(this);
        dashboardPanel.setOpaque(false); // Make dashboardPanel transparent
        mainContentPanel.add(dashboardPanel, "Dashboard");
        mainCardLayout.show(mainContentPanel, "Dashboard");
    }

    /**
     * Switches to the Project Management panel.
     */
    public void showProjectManagement() {
        ProjectManagementPanel projectPanel = new ProjectManagementPanel(this);
        projectPanel.setOpaque(false); // Make projectPanel transparent
        mainContentPanel.add(projectPanel, "Projects");
        mainCardLayout.show(mainContentPanel, "Projects");
    }

    /**
     * Switches to the Experience Management panel (formerly Skill Management).
     */
    public void showExperienceManagement() {
        ExperienceManagementPanel experiencePanel = new ExperienceManagementPanel(this);
        experiencePanel.setOpaque(false); // Make experiencePanel transparent
        mainContentPanel.add(experiencePanel, "Experience");
        mainCardLayout.show(mainContentPanel, "Experience");
    }

    /**
     * Switches to the About Me Management panel.
     */
    public void showAboutManagement() {
        AboutManagementPanel aboutPanel = new AboutManagementPanel(this);
        aboutPanel.setOpaque(false); // Make aboutPanel transparent
        mainContentPanel.add(aboutPanel, "About");
        mainCardLayout.show(mainContentPanel, "About");
    }

    /**
     * Switches to the Contact Management panel.
     */
    public void showContactManagement() {
        ContactManagementPanel contactPanel = new ContactManagementPanel(this);
        contactPanel.setOpaque(false); // Make contactPanel transparent
        mainContentPanel.add(contactPanel, "Contacts");
        mainCardLayout.show(mainContentPanel, "Contacts");
    }

    /**
     * Switches back to the Login panel (for logout).
     */
    public void showLoginPanel() {
        LoginPanel loginPanel = new LoginPanel(this); // Recreate to clear fields
        loginPanel.setOpaque(false); // Make loginPanel transparent
        mainContentPanel.add(loginPanel, "Login");
        mainCardLayout.show(mainContentPanel, "Login");
    }

    /**
     * Main method to start the application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new PortfolioAdminApp().setVisible(true);
        });
    }

    /**
     * Ensures necessary database tables exist.
     */
    private void createTables() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create 'users' table if it doesn't exist (for login)
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "username VARCHAR(50) NOT NULL UNIQUE," +
                         "password VARCHAR(255) NOT NULL" +
                         ")");
            // Add a default admin user if no users exist
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertUserSql = "INSERT INTO users (username, password) VALUES (?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql)) {
                        pstmt.setString(1, "admin");
                        pstmt.setString(2, DatabaseManager.hashPassword("admin123")); // Hash the default password
                        pstmt.executeUpdate();
                        System.out.println("Default admin user 'admin' with password 'admin123' created.");
                    }
                }
            }


            // Create 'skills' table (Updated to include 'category')
            stmt.execute("CREATE TABLE IF NOT EXISTS skills (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "name VARCHAR(255) NOT NULL," +
                         "category VARCHAR(100) DEFAULT 'General'" + // Added category column
                         ")");

            // Create 'about' table
            stmt.execute("CREATE TABLE IF NOT EXISTS about (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "content TEXT NOT NULL" +
                         ")");

            // Create 'projects' table (Updated to use image_url VARCHAR for storing URLs)
            stmt.execute("CREATE TABLE IF NOT EXISTS projects (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "title VARCHAR(255) NOT NULL," +
                         "description TEXT NOT NULL," +
                         "image_url VARCHAR(255) DEFAULT NULL," + // Changed to VARCHAR
                         "link VARCHAR(255) DEFAULT NULL," +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                         ")");

            // Create 'contacts' table (Updated to include 'deleted' flag)
            stmt.execute("CREATE TABLE IF NOT EXISTS contacts (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "platform VARCHAR(100) DEFAULT NULL," +
                         "link VARCHAR(255) DEFAULT NULL," +
                         "deleted TINYINT(1) NOT NULL DEFAULT 0" + // Added deleted column for soft delete
                         ")");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error during table creation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}

/**
 * Manages database connections and operations for the portfolio application.
 * Uses JDBC to interact with a MySQL database.
 */
class DatabaseManager {
    // NOTE: For security and best practice, do not hardcode sensitive information
    // like database credentials in production applications. Use environment variables
    // or a secure configuration mechanism.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/portfolio_db"; // Your database URL
    private static final String DB_USER = "admin"; // Your database username - adjusted to match first file
    private static final String DB_PASSWORD = "admin123"; // Your database password - adjusted to match first file

    /**
     * Establishes a connection to the database.
     * @return A Connection object if successful, null otherwise.
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Open a connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException se) {
            se.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found. Please add it to your classpath.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return conn;
    }

    /**
     * Hashes a given password using SHA-256 algorithm.
     * @param password The plain text password to hash.
     * @return The SHA-256 hashed password as a hexadecimal string.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Authenticates a user against the 'users' table.
     * @param username The username to check.
     * @param password The plain text password to check.
     * @return True if credentials are valid, false otherwise.
     */
    public static boolean authenticateUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return false; // Hashing failed
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if a row is found (user exists with credentials)
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Authentication error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}

/**
 * Represents a Project entity with properties corresponding to the 'projects' table.
 * Now uses imageUrl for image paths.
 */
class Project {
    private int id;
    private String title;
    private String description;
    private String imageUrl;
    private String link;

    public Project(int id, String title, String description, String imageUrl, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.link = link;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}

/**
 * Represents an Experience entity with properties corresponding to the 'skills' table,
 * including a category.
 */
class Experience {
    private int id;
    private String name;
    private String category; // New field for skill category

    public Experience(int id, String name, String level, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

/**
 * Represents a Contact entity with properties corresponding to the 'contacts' table,
 * including a deleted flag.
 */
class Contact {
    private int id;
    private String platform;
    private String link;
    private boolean deleted; // 0 for not deleted, 1 for deleted

    public Contact(int id, String platform, String link, boolean deleted) {
        this.id = id;
        this.platform = platform;
        this.link = link;
        this.deleted = deleted;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}

/**
 * Represents the About Me content entity.
 */
class About {
    private int id;
    private String content;

    public About(int id, String content) {
        this.id = id;
        this.content = content;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

/**
 * Login Panel for the application.
 * Allows users to enter username and password to log in.
 */
class LoginPanel extends JPanel {
    private PortfolioAdminApp parentFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private JCheckBox showPasswordCheckBox; // New checkbox for show/hide password

    /**
     * Constructor for LoginPanel.
     * @param parent The main application frame.
     */
    public LoginPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new GridBagLayout());
        // This panel is transparent to show the parent's gradient background
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(25, 25, 25, 25); // Increased padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create a panel for the login form to give it a card-like appearance
        JPanel loginFormPanel = new JPanel(new GridBagLayout());
        loginFormPanel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        loginFormPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true), // Softer, rounded border
                new EmptyBorder(40, 40, 40, 40) // Internal padding
        ));
        loginFormPanel.putClientProperty("JComponent.roundRect", true); // Nimbus specific for rounded corners

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(15, 15, 15, 15); // Consistent spacing
        formGbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Admin Login", SwingConstants.CENTER);
        titleLabel.setFont(PortfolioAdminApp.FONT_SUBTITLE);
        titleLabel.setForeground(PortfolioAdminApp.TEXT_DARK);
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.gridwidth = 2;
        loginFormPanel.add(titleLabel, formGbc);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(PortfolioAdminApp.FONT_BODY);
        usernameLabel.setForeground(PortfolioAdminApp.TEXT_DARK);
        formGbc.gridx = 0;
        formGbc.gridy = 1;
        formGbc.gridwidth = 1;
        loginFormPanel.add(usernameLabel, formGbc);

        usernameField = createStyledTextField();
        formGbc.gridx = 1;
        formGbc.gridy = 1;
        loginFormPanel.add(usernameField, formGbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(PortfolioAdminApp.FONT_BODY);
        passwordLabel.setForeground(PortfolioAdminApp.TEXT_DARK);
        formGbc.gridx = 0;
        formGbc.gridy = 2;
        loginFormPanel.add(passwordLabel, formGbc);

        passwordField = createStyledPasswordField();
        formGbc.gridx = 1;
        formGbc.gridy = 2;
        loginFormPanel.add(passwordField, formGbc);

        // Show Password Checkbox
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setFont(PortfolioAdminApp.FONT_BODY);
        showPasswordCheckBox.setForeground(PortfolioAdminApp.TEXT_DARK);
        showPasswordCheckBox.setOpaque(false); // Make transparent to show gradient
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0); // Show characters
            } else {
                passwordField.setEchoChar('*'); // Hide characters
            }
        });
        formGbc.gridx = 1; // Align under the password field
        formGbc.gridy = 3;
        formGbc.gridwidth = 1;
        formGbc.anchor = GridBagConstraints.WEST; // Align to the left
        formGbc.insets = new Insets(5, 15, 10, 15); // Adjust padding
        loginFormPanel.add(showPasswordCheckBox, formGbc);

        // Login Button
        JButton loginButton = createStyledButton(
            "Login",
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_END,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_END
        );
        loginButton.setFont(PortfolioAdminApp.FONT_BUTTON);
        loginButton.addActionListener(e -> attemptLogin());
        parentFrame.getRootPane().setDefaultButton(loginButton);
        formGbc.gridx = 0;
        formGbc.gridy = 4; // Shifted down due to new checkbox
        formGbc.gridwidth = 2;
        formGbc.fill = GridBagConstraints.HORIZONTAL; // Ensure button fills horizontally
        formGbc.insets = new Insets(25, 15, 15, 15); // More top padding for button
        loginFormPanel.add(loginButton, formGbc);

        // Message Label for feedback
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(PortfolioAdminApp.ACCENT_RED);
        messageLabel.setFont(PortfolioAdminApp.FONT_SMALL_ITALIC);
        formGbc.gridx = 0;
        formGbc.gridy = 5; // Shifted down
        formGbc.gridwidth = 2;
        formGbc.insets = new Insets(5, 15, 0, 15); // Less top padding for message
        loginFormPanel.add(messageLabel, formGbc);

        // Add the login form panel to the main LoginPanel
        add(loginFormPanel, gbc);
    }

    /** Helper method to create a styled JTextField. */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(PortfolioAdminApp.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12) // Increased padding
        ));
        return field;
    }

    /** Helper method to create a styled JPasswordField. */
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(PortfolioAdminApp.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12) // Increased padding
        ));
        field.setEchoChar('*'); // Ensure it starts with hidden characters
        return field;
    }

    /** Helper method to create a styled GradientButton. */
    private GradientButton createStyledButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd) {
        return new GradientButton(text, start, end, hoverStart, hoverEnd);
    }

    /**
     * Attempts to log in the user using provided credentials.
     * Authenticates against the database and navigates to the dashboard on success.
     */
    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (DatabaseManager.authenticateUser(username, password)) {
            messageLabel.setText("");
            parentFrame.showAdminDashboard();
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }
}

/**
 * Admin Dashboard Panel.
 * Provides navigation buttons to different management sections.
 */
class AdminDashboardPanel extends JPanel {
    private PortfolioAdminApp parentFrame;

    /**
     * Constructor for AdminDashboardPanel.
     * @param parent The main application frame.
     */
    public AdminDashboardPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new GridBagLayout()); // Use GridBagLayout for precise control
        setOpaque(false); // Make transparent to show background gradient
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Overall padding around the dashboard content

        GridBagConstraints gbc = new GridBagConstraints();
        // gbc.insets will be set per component for fine-tuning.

        // --- Row 0: Title and Logout Button ---

        // Create a wrapper panel for the title to control its top padding independently
        JPanel titleContainerPanel = new JPanel(new GridBagLayout());
        titleContainerPanel.setOpaque(false);
        GridBagConstraints titleGbc = new GridBagConstraints();
        titleGbc.insets = new Insets(60, 0, 10, 0); // Significant top padding for the title
        titleGbc.gridx = 0;
        titleGbc.gridy = 0;
        titleGbc.weightx = 1.0;
        titleGbc.fill = GridBagConstraints.HORIZONTAL; // Allow title label to fill its width
        
        JLabel titleLabel = new JLabel("Welcome to the Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(PortfolioAdminApp.FONT_TITLE);
        titleLabel.setForeground(PortfolioAdminApp.TEXT_DARK);
        titleContainerPanel.add(titleLabel, titleGbc); // Add title label to its container

        // Add the title container to the main AdminDashboardPanel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across two conceptual columns for centering
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Ensure the container fills its horizontal grid space
        gbc.anchor = GridBagConstraints.NORTH; // Anchor to the top of its cell
        add(titleContainerPanel, gbc);


        // Logout Button (top-right, placed in a separate grid cell, same row as title)
        JButton logoutButton = createStyledButton(
            "Logout",
            PortfolioAdminApp.GRADIENT_ACCENT_RED_START,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_END,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_HOVER_END
        );
        logoutButton.setFont(PortfolioAdminApp.FONT_BUTTON);
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(220, 220, 220, 80), new Color(80, 80, 80, 80)),
                new EmptyBorder(8, 15, 8, 15)
        ));
        logoutButton.addActionListener(e -> parentFrame.showLoginPanel());

        // Wrapper panel for logout button to enforce right alignment and top padding
        JPanel logoutButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); // No internal padding
        logoutButtonWrapper.setOpaque(false);
        logoutButtonWrapper.add(logoutButton);

        gbc.gridx = 1; // Second conceptual column (right side)
        gbc.gridy = 0; // Same row as title
        gbc.gridwidth = 1; // Reset to single column
        gbc.weightx = 0; // Don't allow it to stretch horizontally
        gbc.fill = GridBagConstraints.NONE; // Don't fill, use preferred size
        gbc.anchor = GridBagConstraints.NORTHEAST; // Anchor to top-right of its cell
        gbc.insets = new Insets(20, 20, 0, 20); // Top and right padding relative to the main panel's border
        add(logoutButtonWrapper, gbc);


        // --- Row 1: Buttons Section (Centered vertically stacked) ---
        JPanel buttonColumnPanel = new JPanel(new GridBagLayout()); // Inner panel for column of buttons
        buttonColumnPanel.setOpaque(false); // Make transparent
        
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.insets = new Insets(15, 0, 15, 0); // Vertical spacing between buttons, no horizontal padding
        btnGbc.gridx = 0; // All buttons in a single column within this inner panel
        btnGbc.fill = GridBagConstraints.NONE; // Do not fill cell, let preferred size control button size
        btnGbc.anchor = GridBagConstraints.CENTER; // Center buttons within their cells

        // Adding the dashboard buttons
        JButton portfolioButton = createDashboardButton("Manage Portfolio Projects", e -> parentFrame.showProjectManagement());
        btnGbc.gridy = 0;
        buttonColumnPanel.add(portfolioButton, btnGbc);

        JButton experienceButton = createDashboardButton("Manage Experience", e -> parentFrame.showExperienceManagement());
        btnGbc.gridy = 1;
        buttonColumnPanel.add(experienceButton, btnGbc);

        JButton aboutButton = createDashboardButton("Manage About Me", e -> parentFrame.showAboutManagement());
        btnGbc.gridy = 2;
        buttonColumnPanel.add(aboutButton, btnGbc);

        JButton contactsButton = createDashboardButton("Manage Contacts", e -> parentFrame.showContactManagement());
        btnGbc.gridy = 3;
        buttonColumnPanel.add(contactsButton, btnGbc);

        // Add the button container to the main dashboard panel, centered horizontally and vertically
        gbc.gridx = 0;
        gbc.gridy = 1; // This is the new row for the buttons
        gbc.gridwidth = 2; // Span across the two conceptual columns to allow horizontal centering
        gbc.weightx = 1.0; // Allow this cell to take horizontal space
        gbc.weighty = 1.0; // Give it vertical weight so it expands and pushes content to vertical center
        gbc.fill = GridBagConstraints.NONE; // Don't fill the cell, allow inner panel to self-center
        gbc.anchor = GridBagConstraints.CENTER; // Center the buttonColumnPanel within its cell
        gbc.insets = new Insets(40, 0, 40, 0); // Vertical padding around the button group relative to its cell
        add(buttonColumnPanel, gbc);
    }

    /**
     * Helper method to create styled dashboard buttons with a gradient background and hover.
     * @param text The text to display on the button.
     * @param listener The ActionListener for the button.
     * @return A styled GradientButton.
     */
    private GradientButton createDashboardButton(String text, ActionListener listener) {
        GradientButton button = new GradientButton(
            text,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_END,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_END
        );
        button.setFont(PortfolioAdminApp.FONT_HEADER); // Use FONT_HEADER for dashboard buttons
        button.setPreferredSize(new Dimension(300, 70)); // Adjusted size for centered vertical stack
        button.addActionListener(listener);
        return button;
    }

    /** Helper method to create a styled GradientButton. */
    private GradientButton createStyledButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd) {
        return new GradientButton(text, start, end, hoverStart, hoverEnd);
    }
}

/**
 * Panel for managing portfolio projects.
 * Allows viewing, adding, editing, and deleting projects.
 * Handles image file upload to server and stores URL in DB.
 */
class ProjectManagementPanel extends JPanel {
    private PortfolioAdminApp parentFrame;
    private DefaultTableModel tableModel;
    private JTable projectTable;
    private JTextField titleField, linkField;
    private JTextArea descriptionArea;
    private JButton addButton, updateButton, deleteButton;
    private JButton uploadImageButton;
    private JLabel imagePreviewLabel; // Image preview for uploaded image
    private File selectedImageFile; // Stores the local file selected by JFileChooser
    private int selectedProjectId = -1; // To store the ID of the selected project for editing/deleting
    private JScrollPane scrollPane; // Made scrollPane a field to control its visibility


    /**
     * Constructor for ProjectManagementPanel.
     * @param parent The main application frame.
     */
    public ProjectManagementPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(20, 20)); // Increased spacing
        // This panel is transparent to show the parent's gradient background
        setOpaque(false);
        setBorder(new EmptyBorder(25, 25, 25, 25)); // Increased padding

        // --- North Panel: Title and Back Button ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false); // Make transparent
        JLabel titleLabel = new JLabel("Manage Portfolio Projects", SwingConstants.CENTER);
        titleLabel.setFont(PortfolioAdminApp.FONT_TITLE);
        titleLabel.setForeground(PortfolioAdminApp.TEXT_DARK);
        northPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createStyledBackButton(
            "⬅ Back to Dashboard",
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_END,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_END,
            e -> parentFrame.showAdminDashboard()
        );
        northPanel.add(backButton, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // --- Main Content Area Panel (holds table and form) ---
        JPanel contentAreaPanel = new JPanel(new BorderLayout(20, 20));
        contentAreaPanel.setOpaque(false); // Make transparent

        // --- Center Panel: Table of Projects ---
        String[] columnNames = {"ID", "Title", "Description", "Image URL", "Link"}; // Added "Image URL"
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells uneditable
            }
        };
        projectTable = new JTable(tableModel);
        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && projectTable.getSelectedRow() != -1) {
                displaySelectedProject();
            }
        });

        // Table styling
        projectTable.setFont(PortfolioAdminApp.FONT_BODY);
        projectTable.setRowHeight(30); // Increased row height
        projectTable.getTableHeader().setFont(PortfolioAdminApp.FONT_HEADER);
        projectTable.getTableHeader().setBackground(PortfolioAdminApp.PRIMARY_BLUE);
        projectTable.getTableHeader().setForeground(Color.WHITE);
        projectTable.setGridColor(PortfolioAdminApp.BORDER_COLOR);
        projectTable.setSelectionBackground(new Color(173, 216, 230, 100)); // Light blue selection with transparency
        projectTable.setSelectionForeground(PortfolioAdminApp.TEXT_DARK);
        projectTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PortfolioAdminApp.BACKGROUND_PANEL : new Color(248, 248, 248)); // Alternating row colors
                }
                c.setForeground(PortfolioAdminApp.TEXT_DARK);
                setBorder(new EmptyBorder(5, 10, 5, 10)); // Cell padding
                return c;
            }
        });

        scrollPane = new JScrollPane(projectTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true), // Rounded border for scroll pane
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Inner padding for scroll pane
        ));
        // Table is now visible by default and loaded on panel display
        contentAreaPanel.add(scrollPane, BorderLayout.CENTER); // Add to contentAreaPanel


        // --- South Panel: Form for Add/Edit ---
        JPanel formPanel = createStyledTitledPanel("Project Details", new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Consistent padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(createStyledLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; titleField = createStyledTextField(); formPanel.add(titleField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(createStyledLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; descriptionArea = createStyledTextArea(4, 20); // Increased rows
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBorder(BorderFactory.createLineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true)); // Border for text area scroll pane
        formPanel.add(descScrollPane, gbc);

        // Link (optional)
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(createStyledLabel("Link (optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; linkField = createStyledTextField(); formPanel.add(linkField, gbc);

        // Upload Image Button
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(createStyledLabel("Upload Image:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        uploadImageButton = createStyledButton(
            "Choose Image",
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_END,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_END
        );
        uploadImageButton.addActionListener(e -> uploadImage());
        formPanel.add(uploadImageButton, gbc);

        // Image Preview
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(createStyledLabel("Image Preview:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        imagePreviewLabel = new JLabel("No Image", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(200, 120)); // Larger preview
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setFont(PortfolioAdminApp.FONT_SMALL_ITALIC);
        imagePreviewLabel.setForeground(Color.GRAY);
        formPanel.add(imagePreviewLabel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Increased spacing
        buttonPanel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        addButton = createStyledButton(
            "Add Project",
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_START,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_END,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_HOVER_END
        );
        updateButton = createStyledButton(
            "Update Project",
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_END,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_END
        );
        deleteButton = createStyledButton(
            "Delete Project",
            PortfolioAdminApp.GRADIENT_ACCENT_RED_START,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_END,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_HOVER_END
        );
        JButton clearButton = createStyledButton(
            "Clear Form",
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_END,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_END
        );

        addButton.addActionListener(e -> addProject());
        updateButton.addActionListener(e -> updateProject());
        deleteButton.addActionListener(e -> deleteProject());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10); // More top padding for buttons
        formPanel.add(buttonPanel, gbc);
        contentAreaPanel.add(formPanel, BorderLayout.SOUTH); // Add to contentAreaPanel

        add(contentAreaPanel, BorderLayout.CENTER); // Add the main content area to the panel

        loadProjects(); // Load projects immediately when the panel is initialized
        clearForm(); // Set initial button states
    }

    /** Helper method to create a styled JTextField. */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(25); // Slightly wider
        field.setFont(PortfolioAdminApp.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12) // Increased padding
        ));
        return field;
    }

    /** Helper method to create a styled JTextArea. */
    private JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(PortfolioAdminApp.FONT_BODY);
        area.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return area;
    }

    /** Helper method to create a styled GradientButton. */
    private GradientButton createStyledButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd) {
        return new GradientButton(text, start, end, hoverStart, hoverEnd);
    }

    /** Helper method to create a styled back button with a subtle raised effect. */
    private GradientButton createStyledBackButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd, ActionListener listener) {
        GradientButton button = new GradientButton(text, start, end, hoverStart, hoverEnd);
        button.setFont(PortfolioAdminApp.FONT_BUTTON);
        button.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(220, 220, 220, 80), new Color(80, 80, 80, 80)),
                new EmptyBorder(8, 15, 8, 15)
        ));
        button.addActionListener(listener);
        return button;
    }

    /** Helper method to create a styled JLabel. */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(PortfolioAdminApp.FONT_BODY);
        label.setForeground(PortfolioAdminApp.TEXT_DARK);
        return label;
    }

    /** Helper method to create a styled Titled Panel. */
    private JPanel createStyledTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                PortfolioAdminApp.FONT_HEADER,
                PortfolioAdminApp.TEXT_DARK
        ));
        panel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        panel.putClientProperty("JComponent.roundRect", true);
        return panel;
    }

    /**
     * Loads all projects from the database and populates the table.
     */
    private void loadProjects() {
        tableModel.setRowCount(0); // Clear existing data
        String sql = "SELECT id, title, description, image_url, link FROM projects ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String imageUrl = rs.getString("image_url");
                String link = rs.getString("link");
                tableModel.addRow(new Object[]{id, title, description, imageUrl, link});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading projects: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Displays the details of the selected project in the form fields.
     */
    private void displaySelectedProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedProjectId = (int) tableModel.getValueAt(selectedRow, 0);
            titleField.setText((String) tableModel.getValueAt(selectedRow, 1));
            descriptionArea.setText((String) tableModel.getValueAt(selectedRow, 2));
            linkField.setText((String) tableModel.getValueAt(selectedRow, 4)); // Column 4 is 'link'

            // Load image from URL for preview
            String imageUrl = (String) tableModel.getValueAt(selectedRow, 3); // Column 3 is 'image_url'
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    // Fetch image from the URL to display in the preview label
                    BufferedImage img = ImageIO.read(new java.net.URL(imageUrl));
                    displayImagePreview(img);
                    selectedImageFile = null; // Clear any locally selected file if loading from DB
                } catch (IOException e) {
                    e.printStackTrace();
                    imagePreviewLabel.setIcon(null);
                    imagePreviewLabel.setText("Error loading image from URL");
                    selectedImageFile = null;
                }
            } else {
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText("No Image");
                selectedImageFile = null;
            }

            addButton.setEnabled(false); // Disable add when editing
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    /**
     * Adds a new project to the database.
     */
    private void addProject() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String link = linkField.getText().trim();

        if (title.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Description cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String imageUrlForDb = null; // This will store the final URL to save
        if (selectedImageFile != null && selectedImageFile.exists()) {
            try {
                String originalFileName = selectedImageFile.getName();
                String fileExtension = "";
                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileExtension = originalFileName.substring(dotIndex + 1);
                }
                // Generate a unique filename using UUID to prevent collisions
                String uniqueFileName = UUID.randomUUID().toString() + (fileExtension.isEmpty() ? "" : "." + fileExtension);

                File destinationFile = new File(PortfolioAdminApp.PROJECT_IMAGE_BASE_DIR, uniqueFileName);

                // Ensure the destination directory exists
                File parentDir = destinationFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs(); // Create directories if they don't exist
                }

                // Read the image and write it to the destination file on the server
                BufferedImage imageToSave = ImageIO.read(selectedImageFile);
                ImageIO.write(imageToSave, fileExtension, destinationFile);

                // Construct the public URL for the image
                imageUrlForDb = PortfolioAdminApp.PROJECT_IMAGE_BASE_URL + uniqueFileName;

                JOptionPane.showMessageDialog(this, "Image uploaded to server: " + destinationFile.getAbsolutePath(), "Image Upload Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving image to server: " + ex.getMessage(), "Image Save Error", JOptionPane.ERROR_MESSAGE);
                imageUrlForDb = null; // Ensure no invalid URL is saved if upload failed
            }
        }

        String sql = "INSERT INTO projects (title, description, image_url, link) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, imageUrlForDb); // Set the image URL here
            pstmt.setString(4, link.isEmpty() ? null : link); // Store null if link is empty

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        tableModel.addRow(new Object[]{id, title, description, imageUrlForDb, link});
                        JOptionPane.showMessageDialog(this, "Project added successfully!");
                        clearForm();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding project: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates an existing project in the database.
     */
    private void updateProject() {
        if (selectedProjectId == -1) {
            JOptionPane.showMessageDialog(this, "No project selected for update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String link = linkField.getText().trim();

        if (title.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Description cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the existing image URL from the table model (if no new image is selected)
        String imageUrlForDb = (String) tableModel.getValueAt(projectTable.getSelectedRow(), 3);

        // If a new image file is selected, process it
        if (selectedImageFile != null && selectedImageFile.exists()) {
            try {
                String originalFileName = selectedImageFile.getName();
                String fileExtension = "";
                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileExtension = originalFileName.substring(dotIndex + 1);
                }
                String uniqueFileName = UUID.randomUUID().toString() + (fileExtension.isEmpty() ? "" : "." + fileExtension);

                File destinationFile = new File(PortfolioAdminApp.PROJECT_IMAGE_BASE_DIR, uniqueFileName);
                File parentDir = destinationFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                BufferedImage imageToSave = ImageIO.read(selectedImageFile);
                ImageIO.write(imageToSave, fileExtension, destinationFile);

                imageUrlForDb = PortfolioAdminApp.PROJECT_IMAGE_BASE_URL + uniqueFileName; // Use the new URL
                JOptionPane.showMessageDialog(this, "Image updated on server: " + destinationFile.getAbsolutePath(), "Image Upload Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating image on server: " + ex.getMessage(), "Image Save Error", JOptionPane.ERROR_MESSAGE);
                // Decide if you want to proceed without updating the image URL or stop
                // For now, it will keep the old URL if this fails.
            }
        }

        String sql = "UPDATE projects SET title = ?, description = ?, image_url = ?, link = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, imageUrlForDb); // Set the image URL
            pstmt.setString(4, link.isEmpty() ? null : link);
            pstmt.setInt(5, selectedProjectId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Project updated successfully!");
            loadProjects(); // Reload all projects to update the table
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating project: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes the selected project from the database.
     */
    private void deleteProject() {
        if (selectedProjectId == -1) {
            JOptionPane.showMessageDialog(this, "No project selected for deletion.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this project? (Note: Image file on server will NOT be deleted automatically)", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM projects WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedProjectId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Project deleted successfully!");
                clearForm();
                loadProjects(); // Refresh table
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting project: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears the form fields and resets the selected project ID.
     */
    private void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        linkField.setText("");
        imagePreviewLabel.setIcon(null); // Clear image preview
        imagePreviewLabel.setText("No Image"); // Reset text
        selectedImageFile = null; // Clear selected file
        selectedProjectId = -1;
        projectTable.clearSelection(); // Deselect row
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    /**
     * Opens a file chooser dialog to select an image and sets the preview.
     */
    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage originalImage = ImageIO.read(selectedFile);
                if (originalImage != null) {
                    selectedImageFile = selectedFile; // Store the selected file
                    displayImagePreview(originalImage);
                } else {
                    JOptionPane.showMessageDialog(this, "Could not read image file.", "File Error", JOptionPane.ERROR_MESSAGE);
                    selectedImageFile = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage(), "Image Error", JOptionPane.ERROR_MESSAGE);
                selectedImageFile = null;
            }
        }
    }

    /**
     * Displays a scaled image preview in the imagePreviewLabel.
     * @param image The BufferedImage to display.
     */
    private void displayImagePreview(BufferedImage image) {
        if (image == null) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No Image");
            return;
        }
        // Scale image to fit the label
        int labelWidth = imagePreviewLabel.getWidth() > 0 ? imagePreviewLabel.getWidth() : 200;
        int labelHeight = imagePreviewLabel.getHeight() > 0 ? imagePreviewLabel.getHeight() : 120;

        Image scaledImage = image.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
        imagePreviewLabel.setIcon(new ImageIcon(scaledImage));
        imagePreviewLabel.setText(""); // Clear text when image is present
    }
}

/**
 * Panel for managing skills, now renamed to Experience, with categorization.
 * Allows viewing, adding, editing, and deleting experience entries within categories.
 */
class ExperienceManagementPanel extends JPanel {
    private PortfolioAdminApp parentFrame;
    private JTabbedPane tabbedPane;
    private JTextField skillNameField; // Text field for skill name

    // New UI components for dynamic category management
    private JTextField newCategoryNameField;
    private JButton createCategoryButton;
    private JButton deleteCategoryButton;
    private JComboBox<String> categorySelectorForRenameDelete; // To select category to delete

    // Maps to hold table models and selected IDs for each category
    private java.util.Map<String, DefaultTableModel> tableModels = new java.util.HashMap<>();
    private java.util.Map<String, JTable> tables = new java.util.HashMap<>();
    // Keep track of the selected ID for each category (tab).
    // This map stores the ID of the currently selected experience entry for each category.
    private java.util.Map<String, Integer> selectedExperienceIds = new java.util.HashMap<>();


    private JButton addButton, updateButton, deleteButton, clearButton;

    /**
     * Constructor for ExperienceManagementPanel.
     * @param parent The main application frame.
     */
    public ExperienceManagementPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- North Panel: Title and Back Button ---
        JPanel northPanel = new JPanel(new BorderLayout()); // Use BorderLayout for this panel
        northPanel.setOpaque(false);

        // Back Button
        JButton backButton = createStyledBackButton(
            "⬅ Back to Dashboard",
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_END,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_END,
            e -> parentFrame.showAdminDashboard()
        );
        northPanel.add(backButton, BorderLayout.WEST); // Add back button to the left

        // Title Label
        JLabel titleLabel = new JLabel("Manage Experience", SwingConstants.CENTER);
        titleLabel.setFont(PortfolioAdminApp.FONT_TITLE);
        titleLabel.setForeground(PortfolioAdminApp.TEXT_DARK);
        northPanel.add(titleLabel, BorderLayout.CENTER); // Add title to the center

        add(northPanel, BorderLayout.NORTH); // Add the combined north panel to the main panel


        // --- Category Management Panel (above the tabbed pane) ---
        JPanel categoryManagementPanel = createStyledTitledPanel("Manage Categories", new GridBagLayout());
        GridBagConstraints categoryGbc = new GridBagConstraints();
        categoryGbc.insets = new Insets(8, 8, 8, 8); // Padding for category management components
        categoryGbc.fill = GridBagConstraints.HORIZONTAL;

        // Add New Category Row
        categoryGbc.gridx = 0; categoryGbc.gridy = 0; categoryManagementPanel.add(createStyledLabel("New Category Name:"), categoryGbc);
        categoryGbc.gridx = 1; categoryGbc.gridy = 0; categoryGbc.weightx = 1.0;
        newCategoryNameField = createStyledTextField();
        categoryManagementPanel.add(newCategoryNameField, categoryGbc);
        categoryGbc.gridx = 2; categoryGbc.gridy = 0; categoryGbc.weightx = 0;
        createCategoryButton = createStyledButton(
            "Add New Category",
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_START,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_END,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_HOVER_END
        );
        createCategoryButton.addActionListener(e -> addNewCategory());
        categoryManagementPanel.add(createCategoryButton, categoryGbc);

        // Delete Category Row
        categoryGbc.gridx = 0; categoryGbc.gridy = 1; categoryManagementPanel.add(createStyledLabel("Select Category to Delete:"), categoryGbc);
        categoryGbc.gridx = 1; categoryGbc.gridy = 1;
        categorySelectorForRenameDelete = createStyledComboBox(new String[]{}); // Will be populated dynamically
        categoryManagementPanel.add(categorySelectorForRenameDelete, categoryGbc);

        JPanel deleteButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        deleteButtonPanel.setOpaque(false);
        deleteCategoryButton = createStyledButton(
            "Delete Selected Category",
            PortfolioAdminApp.GRADIENT_ACCENT_RED_START,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_END,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_HOVER_END
        );
        deleteCategoryButton.addActionListener(e -> deleteSelectedCategory());
        deleteButtonPanel.add(deleteCategoryButton);
        categoryGbc.gridx = 2; categoryGbc.gridy = 1;
        categoryManagementPanel.add(deleteButtonPanel, categoryGbc);


        // Add category management panel to the top part of the center
        add(categoryManagementPanel, BorderLayout.CENTER); // Changed from NORTH to CENTER to allow northPanel to be in NORTH

        // --- Center Panel: Tabbed Pane for Categories ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(PortfolioAdminApp.FONT_HEADER); // Style the tab titles
        tabbedPane.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        tabbedPane.setForeground(PortfolioAdminApp.TEXT_DARK);
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));

        // Add change listener to tabbed pane to update form when tab changes
        tabbedPane.addChangeListener(e -> {
            clearForm(); // Clear form when switching tabs
        });

        // Add tabbed pane to main panel's center-south (below category management)
        // To achieve this, we need an intermediate panel for CENTER
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10)); // Container for category mgmt and tabs
        centerContainer.setOpaque(false);
        centerContainer.add(categoryManagementPanel, BorderLayout.NORTH); // Category management at top of center
        centerContainer.add(tabbedPane, BorderLayout.CENTER); // Tabs below category management

        add(centerContainer, BorderLayout.CENTER); // Add the center container to the main panel's center


        // --- South Panel: Form for Add/Edit Experience ---
        JPanel experienceFormPanel = createStyledTitledPanel("Experience Entry Details", new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Skill Name
        gbc.gridx = 0; gbc.gridy = 0; experienceFormPanel.add(createStyledLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        skillNameField = createStyledTextField();
        experienceFormPanel.add(skillNameField, gbc);

        // Buttons for Experience Entries
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        addButton = createStyledButton(
            "Add Entry",
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_START,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_END,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_HOVER_END
        );
        updateButton = createStyledButton(
            "Update Entry",
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_END,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_END
        );
        deleteButton = createStyledButton(
            "Delete Entry",
            PortfolioAdminApp.GRADIENT_ACCENT_RED_START,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_END,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_RED_HOVER_END
        );
        clearButton = createStyledButton(
            "Clear Form",
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_END,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_END
        );

        addButton.addActionListener(e -> addExperience());
        updateButton.addActionListener(e -> updateExperience());
        deleteButton.addActionListener(e -> deleteExperience());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        experienceFormPanel.add(buttonPanel, gbc);
        add(experienceFormPanel, BorderLayout.SOUTH);

        // Load data on panel initialization
        loadCategoriesAndExperiences();
        clearForm(); // Set initial button states for experience entry form
    }

    /**
     * Creates a panel for a specific experience category, including its table.
     * This method is now called dynamically when categories are loaded/created.
     * @param category The name of the category.
     * @return A JPanel containing the table for the given category.
     */
    private JPanel createCategoryPanel(String category) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false); // Transparent to show background gradient

        String[] columnNames = {"ID", "Name"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        tableModels.put(category, model);
        tables.put(category, table);
        selectedExperienceIds.put(category, -1); // Initialize selected ID for this category

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            // Ensure this listener only acts on the currently visible tab
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1 && tabbedPane.getSelectedComponent() == panel) {
                displaySelectedExperience(category);
            }
        });
        // Table styling
        table.setFont(PortfolioAdminApp.FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(PortfolioAdminApp.FONT_HEADER);
        table.getTableHeader().setBackground(PortfolioAdminApp.PRIMARY_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(PortfolioAdminApp.BORDER_COLOR);
        table.setSelectionBackground(new Color(173, 216, 230, 100));
        table.setSelectionForeground(PortfolioAdminApp.TEXT_DARK);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PortfolioAdminApp.BACKGROUND_PANEL : new Color(248, 248, 248));
                }
                c.setForeground(PortfolioAdminApp.TEXT_DARK);
                setBorder(new EmptyBorder(5, 10, 5, 10)); // Cell padding
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /** Helper method to create a styled JTextField. */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(25);
        field.setFont(PortfolioAdminApp.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    /** Helper method to create a styled JComboBox. */
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(PortfolioAdminApp.FONT_BODY);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return comboBox;
    }

    /** Helper method to create a styled GradientButton. */
    private GradientButton createStyledButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd) {
        return new GradientButton(text, start, end, hoverStart, hoverEnd);
    }

    /** Helper method to create a styled back button with a subtle raised effect. */
    private GradientButton createStyledBackButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd, ActionListener listener) {
        GradientButton button = new GradientButton(text, start, end, hoverStart, hoverEnd);
        button.setFont(PortfolioAdminApp.FONT_BUTTON);
        button.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(220, 220, 220, 80), new Color(80, 80, 80, 80)),
                new EmptyBorder(8, 15, 8, 15)
        ));
        button.addActionListener(listener);
        return button;
    }

    /** Helper method to create a styled JLabel. */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(PortfolioAdminApp.FONT_BODY);
        label.setForeground(PortfolioAdminApp.TEXT_DARK);
        return label;
    }

    /** Helper method to create a styled Titled Panel. */
    private JPanel createStyledTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                PortfolioAdminApp.FONT_HEADER,
                PortfolioAdminApp.TEXT_DARK
        ));
        panel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        panel.putClientProperty("JComponent.roundRect", true);
        return panel;
    }

    /**
     * Loads categories from the database and then loads all experience entries.
     * This rebuilds the tabs and populates the tables.
     */
    private void loadCategoriesAndExperiences() {
        // Clear existing tabs and models
        tabbedPane.removeAll();
        tableModels.clear();
        tables.clear();
        // Do NOT clear selectedExperienceIds here.
        // It should persist selected IDs for each category even if tabs are rebuilt.

        List<String> categories = getDistinctExperienceCategories();
        // Sort categories alphabetically for consistent display
        Collections.sort(categories);

        if (categories.isEmpty()) {
            // Add a default category if none exist, or display a message
            addNewCategory("General"); // Add a default "General" category if DB is empty
            categories = getDistinctExperienceCategories(); // Reload after adding default
        }

        // Recreate tabs for each category found in the database
        for (String category : categories) {
            JPanel categoryPanel = createCategoryPanel(category);
            tabbedPane.addTab(category, categoryPanel);
        }

        loadAllExperienceEntries(); // Now load all actual experience entries into the correct tables
        updateCategorySelector(); // Update the JComboBox for delete actions
    }

    /**
     * Fetches distinct categories from the 'skills' table.
     * @return A list of distinct category names.
     */
    private List<String> getDistinctExperienceCategories() {
        List<String> distinctCategories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM skills WHERE category IS NOT NULL AND category != '' ORDER BY category";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                distinctCategories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching categories: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return distinctCategories;
    }

    /**
     * Loads all experience entries from the database into their respective category tables.
     */
    private void loadAllExperienceEntries() {
        // Clear all existing table models before reloading
        for (DefaultTableModel model : tableModels.values()) {
            model.setRowCount(0);
        }

        String sql = "SELECT id, name, category FROM skills ORDER BY category, name"; // -- Removed 'level' from SELECT
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                // REMOVED: String level = rs.getString("level"); // This line should be gone
                String category = rs.getString("category");

                DefaultTableModel model = tableModels.get(category);
                if (model != null) {
                    // CORRECTED: Only 'id' and 'name' are added to the row
                    model.addRow(new Object[]{id, name}); //
                } else {
                    System.err.println("Warning: Experience entry found with unknown category '" + category + "'. Skipping for display.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading experience entries: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the ComboBox with the current list of categories.
     */
    private void updateCategorySelector() {
        categorySelectorForRenameDelete.removeAllItems();
        List<String> categories = getDistinctExperienceCategories();
        for (String cat : categories) {
            categorySelectorForRenameDelete.addItem(cat);
        }
        if (categorySelectorForRenameDelete.getItemCount() > 0) {
            categorySelectorForRenameDelete.setSelectedIndex(0);
            deleteCategoryButton.setEnabled(true); // Enable delete if categories exist
        } else {
            deleteCategoryButton.setEnabled(false); // Disable if no categories
        }
    }


    /**
     * Displays the details of the selected experience in the form fields.
     * @param category The category of the selected experience.
     */
    private void displaySelectedExperience(String category) {
        JTable currentTable = tables.get(category);
        DefaultTableModel currentModel = tableModels.get(category);

        if (currentTable == null || currentModel == null) return;

        int selectedRow = currentTable.getSelectedRow();
        if (selectedRow != -1) {
            int experienceId = (int) currentModel.getValueAt(selectedRow, 0);
            String experienceName = (String) currentModel.getValueAt(selectedRow, 1);

            // Store selected ID for this specific category
            selectedExperienceIds.put(category, experienceId);
            skillNameField.setText(experienceName);

            addButton.setEnabled(false); // Disable add when editing
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else {
            // If selection is cleared within a tab (e.g., user clicks off the row)
            clearForm();
        }
    }

    /**
     * Adds a new experience entry to the database based on the currently selected tab.
     */
   private void addExperience() {
        String name = skillNameField.getText().trim();
        // REMOVED: String level = (String) levelComboBox.getSelectedItem(); // This line should be gone
        int selectedTabIndex = tabbedPane.getSelectedIndex();

        if (selectedTabIndex == -1) {
             JOptionPane.showMessageDialog(this, "Please select an experience category tab first.", "Selection Error", JOptionPane.WARNING_MESSAGE);
             return;
        }
        String category = tabbedPane.getTitleAt(selectedTabIndex).trim(); // Get current tab category

        // Adjusted validation: 'level' check removed.
        if (name.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and category cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // CORRECTED SQL: No 'level' column in INSERT, adjusted parameter count.
        String sql = "INSERT INTO skills (name, category) VALUES (?, ?)"; //
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            // REMOVED: pstmt.setString(2, level); // This line should be gone
            pstmt.setString(2, category); // -- This index changed from 3 to 2
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Experience added successfully to " + category + "!");
            clearForm();
            loadAllExperienceEntries(); // Refresh all entries, affecting only the relevant table
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding experience: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Updates an existing experience entry in the database.
     */
    private void updateExperience() {
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        if (selectedTabIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select an experience category tab first.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String category = tabbedPane.getTitleAt(selectedTabIndex);
        int selectedId = selectedExperienceIds.getOrDefault(category, -1); // Get selected ID for current category

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "No experience selected for update in " + category + ".", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = skillNameField.getText().trim();
        // REMOVED: String level = (String) levelComboBox.getSelectedItem(); // This line should be gone

        // Adjusted validation: 'level' check removed.
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // CORRECTED SQL: No 'level' column in UPDATE, adjusted parameter count.
        String sql = "UPDATE skills SET name = ? WHERE id = ?"; //
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            // REMOVED: pstmt.setString(2, level); // This line should be gone
            pstmt.setInt(2, selectedId); // -- This index changed from 3 to 2
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Experience updated successfully in " + category + "!");
            clearForm();
            loadAllExperienceEntries(); // Refresh entries
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating experience: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes the selected experience entry from the database.
     */
    private void deleteExperience() {
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        if (selectedTabIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select an experience category tab first.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String category = tabbedPane.getTitleAt(selectedTabIndex);
        int selectedId = selectedExperienceIds.getOrDefault(category, -1); // Get selected ID for current category

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "No experience selected for deletion in " + category + ".", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this experience from " + category + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM skills WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Experience deleted successfully from " + category + "!");
                clearForm();
                loadAllExperienceEntries(); // Refresh entries
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting experience: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears the form fields for adding/editing experience entries and resets selected states.
     * This method is called when switching tabs or clearing the form manually.
     */
    private void clearForm() {
        skillNameField.setText("");

        // Get the currently selected category tab to clear its selection.
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        if (selectedTabIndex != -1) {
            String currentCategory = tabbedPane.getTitleAt(selectedTabIndex);
            // Clear the selected ID specifically for the current category in the map.
            selectedExperienceIds.put(currentCategory, -1);

            // Clear selection in the currently visible table (if any row was selected)
            JTable currentTable = tables.get(currentCategory);
            if (currentTable != null) {
                currentTable.clearSelection();
            }
        }

        // Reset button states for adding new entries
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    /**
     * Adds a new category to the database and reloads all categories and experiences.
     */
    private void addNewCategory() {
        String newCategory = newCategoryNameField.getText().trim();
        if (newCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> existingCategories = getDistinctExperienceCategories();
        if (existingCategories.contains(newCategory)) {
            JOptionPane.showMessageDialog(this, "Category '" + newCategory + "' already exists.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // CORRECTED SQL: No 'level' column in INSERT.
        String sql = "INSERT INTO skills (name, category) VALUES (?, ?)"; //
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "New Entry"); // Dummy name
            // REMOVED: pstmt.setString(2, "Beginner"); // This line should be gone
            pstmt.setString(2, newCategory); // -- This index changed from 3 to 2
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Category '" + newCategory + "' added successfully!");
            newCategoryNameField.setText("");
            loadCategoriesAndExperiences(); // Reload all categories and experiences
            int newTabIndex = tabbedPane.indexOfTab(newCategory);
            if (newTabIndex != -1) {
                tabbedPane.setSelectedIndex(newTabIndex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding new category: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Overloaded method to add a category without user input (e.g., for default category).
     */
    private void addNewCategory(String categoryName) {
        String newCategory = categoryName.trim();
        if (newCategory.isEmpty()) {
            return;
        }

        List<String> existingCategories = getDistinctExperienceCategories();
        if (existingCategories.contains(newCategory)) {
            return;
        }

        // CORRECTED SQL: No 'level' column in INSERT.
        String sql = "INSERT INTO skills (name, category) VALUES (?, ?)"; //
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Sample Experience"); // Dummy data for the new category
            // REMOVED: pstmt.setString(2, "Beginner"); // This line should be gone
            pstmt.setString(2, newCategory); // -- This index changed from 3 to 2
            pstmt.executeUpdate();
            System.out.println("Default category '" + newCategory + "' added.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error adding default category: " + e.getMessage());
        }
    }

    /**
     * Deletes the currently selected category and all its associated experience entries.
     */
    private void deleteSelectedCategory() {
        String categoryToDelete = (String) categorySelectorForRenameDelete.getSelectedItem();
        if (categoryToDelete == null || categoryToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Deleting category '" + categoryToDelete + "' will permanently delete ALL associated experience entries.\nAre you sure you want to proceed?",
                "Confirm Category Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM skills WHERE category = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, categoryToDelete);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Category '" + categoryToDelete + "' and all its entries deleted successfully!");
                loadCategoriesAndExperiences(); // Reload all categories and experiences to update UI
                clearForm(); // Clear the experience entry form
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting category: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}


/**
 * Panel for managing the 'About Me' content.
 * Allows viewing and editing the 'About Me' text.
 */
class AboutManagementPanel extends JPanel {
    private PortfolioAdminApp parentFrame;
    private JTextArea aboutContentArea;
    private JButton saveButton;
    private int aboutId = -1; // To store the ID of the about entry (should be 1)

    /**
     * Constructor for AboutManagementPanel.
     * @param parent The main application frame.
     */
    public AboutManagementPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(20, 20));
        // This panel is transparent to show the parent's gradient background
        setOpaque(false);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- North Panel: Title and Back Button ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false); // Make transparent
        JLabel titleLabel = new JLabel("Manage About Me Content", SwingConstants.CENTER);
        titleLabel.setFont(PortfolioAdminApp.FONT_TITLE);
        titleLabel.setForeground(PortfolioAdminApp.TEXT_DARK);
        northPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createStyledBackButton(
            "⬅ Back to Dashboard",
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_END,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_END,
            e -> parentFrame.showAdminDashboard()
        );
        northPanel.add(backButton, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // --- Center Panel: About Me Content Area ---
        JPanel contentPanel = createStyledTitledPanel("About Me Text", new BorderLayout(10, 10));
        aboutContentArea = createStyledTextArea(15, 50);
        JScrollPane scrollPane = new JScrollPane(aboutContentArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // --- South Panel: Save Button ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL); // Match panel background
        saveButton = createStyledButton(
            "Save About Me",
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_END,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_END
        );
        saveButton.addActionListener(e -> saveAboutContent());
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadAboutContent(); // Load content when panel is initialized
    }

    /** Helper method to create a styled JTextArea. */
    private JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(PortfolioAdminApp.FONT_BODY);
        area.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return area;
    }

    /** Helper method to create a styled GradientButton. */
    private GradientButton createStyledButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd) {
        return new GradientButton(text, start, end, hoverStart, hoverEnd);
    }

    /** Helper method to create a styled back button with a subtle raised effect. */
    private GradientButton createStyledBackButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd, ActionListener listener) {
        GradientButton button = new GradientButton(text, start, end, hoverStart, hoverEnd);
        button.setFont(PortfolioAdminApp.FONT_BUTTON);
        button.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(220, 220, 220, 80), new Color(80, 80, 80, 80)),
                new EmptyBorder(8, 15, 8, 15)
        ));
        button.addActionListener(listener);
        return button;
    }

    /** Helper method to create a styled JLabel. */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(PortfolioAdminApp.FONT_BODY);
        label.setForeground(PortfolioAdminApp.TEXT_DARK);
        return label;
    }

    /** Helper method to create a styled Titled Panel. */
    private JPanel createStyledTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                PortfolioAdminApp.FONT_HEADER,
                PortfolioAdminApp.TEXT_DARK
        ));
        panel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        panel.putClientProperty("JComponent.roundRect", true);
        return panel;
    }

    /**
     * Loads the 'About Me' content from the database.
     */
    private void loadAboutContent() {
        String sql = "SELECT id, content FROM about LIMIT 1"; // Assuming only one 'about' entry
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                aboutId = rs.getInt("id");
                aboutContentArea.setText(rs.getString("content"));
            } else {
                // If no 'about' entry exists, create a default one
                String insertSql = "INSERT INTO about (content) VALUES (?)";
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertPstmt.setString(1, "No about info yet. Please edit this section.");
                    insertPstmt.executeUpdate();
                    ResultSet generatedKeys = insertPstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        aboutId = generatedKeys.getInt(1);
                    }
                    aboutContentArea.setText("No about info yet. Please edit this section.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading about content: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Saves (updates) the 'About Me' content to the database.
     */
    private void saveAboutContent() {
        String content = aboutContentArea.getText();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "About Me content cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql;
        if (aboutId != -1) {
            sql = "UPDATE about SET content = ? WHERE id = ?";
        } else {
            sql = "INSERT INTO about (content) VALUES (?)"; // Fallback if ID somehow lost
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, content);
            if (aboutId != -1) {
                pstmt.setInt(2, aboutId);
            }
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "About Me content saved successfully!");
            // Re-load to ensure ID is correct if it was an insert
            loadAboutContent();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving about content: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

/**
 * Panel for managing contacts.
 * Allows viewing, adding, editing, soft deleting, restoring, and hard deleting contacts.
 */
class ContactManagementPanel extends JPanel {
    private PortfolioAdminApp parentFrame;
    private DefaultTableModel activeTableModel, deletedTableModel;
    private JTable activeContactTable, deletedContactTable;
    private JTextField linkField;
    private JComboBox<String> platformComboBox;
    private JButton addButton, updateButton, softDeleteButton, restoreButton, hardDeleteButton, clearButton;
    private int selectedContactId = -1; // For active contacts
    private int selectedDeletedContactId = -1; // For deleted contacts

    // Combined platform options, including "Other" for custom entries
    private static final String[] PLATFORMS = {"", "Email", "Phone", "LinkedIn", "GitHub", "Website", "Twitter", "Facebook", "Instagram", "Discord", "Telegram", "WhatsApp", "YouTube", "Blog", "Other"};

    /**
     * Constructor for ContactManagementPanel.
     * @param parent The main application frame.
     */
    public ContactManagementPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(20, 20));
        // This panel is transparent to show the parent's gradient background
        setOpaque(false);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- North Panel: Title and Back Button ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false); // Make transparent
        JLabel titleLabel = new JLabel("Manage Contacts", SwingConstants.CENTER);
        titleLabel.setFont(PortfolioAdminApp.FONT_TITLE);
        titleLabel.setForeground(PortfolioAdminApp.TEXT_DARK);
        northPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createStyledBackButton(
            "⬅ Back to Dashboard",
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_END,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_END,
            e -> parentFrame.showAdminDashboard()
        );
        northPanel.add(backButton, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // --- Center Panel: Tables for Active and Deleted Contacts ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5); // Distribute space evenly
        splitPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default border

        // Active Contacts Table
        JPanel activePanel = createStyledTitledPanel("Active Contacts", new BorderLayout());
        String[] activeColumnNames = {"ID", "Platform", "Link"};
        activeTableModel = new DefaultTableModel(activeColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        activeContactTable = new JTable(activeTableModel);
        activeContactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activeContactTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && activeContactTable.getSelectedRow() != -1) {
                displaySelectedActiveContact();
                // Clear selection in deleted table
                deletedContactTable.clearSelection();
                selectedDeletedContactId = -1;
                restoreButton.setEnabled(false);
            }
        });
        // Table styling
        activeContactTable.setFont(PortfolioAdminApp.FONT_BODY);
        activeContactTable.setRowHeight(30);
        activeContactTable.getTableHeader().setFont(PortfolioAdminApp.FONT_HEADER);
        activeContactTable.getTableHeader().setBackground(PortfolioAdminApp.PRIMARY_BLUE);
        activeContactTable.getTableHeader().setForeground(Color.WHITE);
        activeContactTable.setGridColor(PortfolioAdminApp.BORDER_COLOR);
        activeContactTable.setSelectionBackground(new Color(173, 216, 230, 100));
        activeContactTable.setSelectionForeground(PortfolioAdminApp.TEXT_DARK);
        activeContactTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PortfolioAdminApp.BACKGROUND_PANEL : new Color(248, 248, 248));
                }
                c.setForeground(PortfolioAdminApp.TEXT_DARK);
                setBorder(new EmptyBorder(5, 10, 5, 10)); // Cell padding
                return c;
            }
        });

        JScrollPane activeScrollPane = new JScrollPane(activeContactTable);
        activeScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border
        activePanel.add(activeScrollPane, BorderLayout.CENTER);
        splitPane.setTopComponent(activePanel);

        // Deleted Contacts Table
        JPanel deletedPanel = createStyledTitledPanel("Deleted Contacts (Trash)", new BorderLayout());
        String[] deletedColumnNames = {"ID", "Platform", "Link"};
        deletedTableModel = new DefaultTableModel(deletedColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deletedContactTable = new JTable(deletedTableModel);
        deletedContactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deletedContactTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && deletedContactTable.getSelectedRow() != -1) {
                displaySelectedDeletedContact();
                // Clear selection in active table
                activeContactTable.clearSelection();
                selectedContactId = -1;
                updateButton.setEnabled(false);
                softDeleteButton.setEnabled(false);
            }
        });
        // Table styling
        deletedContactTable.setFont(PortfolioAdminApp.FONT_BODY);
        deletedContactTable.setRowHeight(30);
        deletedContactTable.getTableHeader().setFont(PortfolioAdminApp.FONT_HEADER);
        deletedContactTable.getTableHeader().setBackground(PortfolioAdminApp.PRIMARY_BLUE);
        deletedContactTable.getTableHeader().setForeground(Color.WHITE);
        deletedContactTable.setGridColor(PortfolioAdminApp.BORDER_COLOR);
        deletedContactTable.setSelectionBackground(new Color(173, 216, 230, 100));
        deletedContactTable.setSelectionForeground(PortfolioAdminApp.TEXT_DARK);
        deletedContactTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PortfolioAdminApp.BACKGROUND_PANEL : new Color(248, 248, 248));
                }
                c.setForeground(PortfolioAdminApp.TEXT_DARK);
                setBorder(new EmptyBorder(5, 10, 5, 10)); // Cell padding
                return c;
            }
        });

        JScrollPane deletedScrollPane = new JScrollPane(deletedContactTable);
        deletedScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border
        deletedPanel.add(deletedScrollPane, BorderLayout.CENTER);
        splitPane.setBottomComponent(deletedPanel);

        add(splitPane, BorderLayout.CENTER);

        // --- South Panel: Form for Add/Edit/Restore/Delete ---
        JPanel formPanel = createStyledTitledPanel("Contact Details / Actions", new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Platform
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(createStyledLabel("Platform:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        platformComboBox = createStyledComboBox(PLATFORMS);
        formPanel.add(platformComboBox, gbc);

        // Link
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(createStyledLabel("Link:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; linkField = createStyledTextField(); formPanel.add(linkField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        addButton = createStyledButton(
            "Add Contact",
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_START,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_END,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_GREEN_HOVER_END
        );
        updateButton = createStyledButton(
            "Update Contact",
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_END,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_START,
            PortfolioAdminApp.GRADIENT_PRIMARY_BLUE_HOVER_END
        );
        softDeleteButton = createStyledButton(
            "Soft Delete",
            PortfolioAdminApp.GRADIENT_ACCENT_ORANGE_START,
            PortfolioAdminApp.GRADIENT_ACCENT_ORANGE_END,
            PortfolioAdminApp.GRADIENT_ACCENT_ORANGE_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_ORANGE_HOVER_END
        );
        restoreButton = createStyledButton(
            "Restore Contact",
            PortfolioAdminApp.GRADIENT_ACCENT_CYAN_START,
            PortfolioAdminApp.GRADIENT_ACCENT_CYAN_END,
            PortfolioAdminApp.GRADIENT_ACCENT_CYAN_HOVER_START,
            PortfolioAdminApp.GRADIENT_ACCENT_CYAN_HOVER_END
        );
        hardDeleteButton = createStyledButton(
            "Hard Delete",
            Color.DARK_GRAY, Color.BLACK, Color.GRAY, Color.DARK_GRAY
        ); // A more somber color for permanent delete
        clearButton = createStyledButton(
            "Clear Form",
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_END,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_START,
            PortfolioAdminApp.GRADIENT_NEUTRAL_GREY_HOVER_END
        );

        addButton.addActionListener(e -> addContact());
        updateButton.addActionListener(e -> updateContact());
        softDeleteButton.addActionListener(e -> softDeleteContact());
        restoreButton.addActionListener(e -> restoreContact());
        hardDeleteButton.addActionListener(e -> hardDeleteContact()); // Add action listener for hard delete
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(softDeleteButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(hardDeleteButton); // Add hard delete button
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(buttonPanel, gbc);
        add(formPanel, BorderLayout.SOUTH);

        loadContacts(); // Load data when panel is initialized
        clearForm(); // Set initial button states
    }

    /** Helper method to create a styled JTextField. */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(25);
        field.setFont(PortfolioAdminApp.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    /** Helper method to create a styled JComboBox. */
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(PortfolioAdminApp.FONT_BODY);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return comboBox;
    }

    /** Helper method to create a styled GradientButton. */
    private GradientButton createStyledButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd) {
        return new GradientButton(text, start, end, hoverStart, hoverEnd);
    }

    /** Helper method to create a styled back button with a subtle raised effect. */
    private GradientButton createStyledBackButton(String text, Color start, Color end, Color hoverStart, Color hoverEnd, ActionListener listener) {
        GradientButton button = new GradientButton(text, start, end, hoverStart, hoverEnd);
        button.setFont(PortfolioAdminApp.FONT_BUTTON);
        button.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(220, 220, 220, 80), new Color(80, 80, 80, 80)),
                new EmptyBorder(8, 15, 8, 15)
        ));
        button.addActionListener(listener);
        return button;
    }

    /** Helper method to create a styled JLabel. */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(PortfolioAdminApp.FONT_BODY);
        label.setForeground(PortfolioAdminApp.TEXT_DARK);
        return label;
    }

    /** Helper method to create a styled Titled Panel. */
    private JPanel createStyledTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(PortfolioAdminApp.BORDER_COLOR, 1, true),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                PortfolioAdminApp.FONT_HEADER,
                PortfolioAdminApp.TEXT_DARK
        ));
        panel.setBackground(PortfolioAdminApp.BACKGROUND_PANEL);
        panel.putClientProperty("JComponent.roundRect", true);
        return panel;
    }

    /**
     * Loads active and deleted contacts from the database and populates their respective tables.
     */
    private void loadContacts() {
        activeTableModel.setRowCount(0);
        deletedTableModel.setRowCount(0);

        // Load active contacts
        String activeSql = "SELECT id, platform, link FROM contacts WHERE deleted = 0";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(activeSql)) {
            while (rs.next()) {
                activeTableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("platform"), rs.getString("link")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading active contacts: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Load deleted contacts
        String deletedSql = "SELECT id, platform, link FROM contacts WHERE deleted = 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(deletedSql)) {
            while (rs.next()) {
                deletedTableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("platform"), rs.getString("link")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading deleted contacts: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Displays the details of the selected active contact in the form fields.
     */
    private void displaySelectedActiveContact() {
        int selectedRow = activeContactTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedContactId = (int) activeTableModel.getValueAt(selectedRow, 0);
            platformComboBox.setSelectedItem((String) activeTableModel.getValueAt(selectedRow, 1));
            linkField.setText((String) activeTableModel.getValueAt(selectedRow, 2));
            addButton.setEnabled(false);
            updateButton.setEnabled(true);
            softDeleteButton.setEnabled(true);
            restoreButton.setEnabled(false); // Cannot restore an active contact
            hardDeleteButton.setEnabled(true); // Can hard delete an active contact
        }
    }

    /**
     * Displays the details of the selected deleted contact in the form fields.
     */
    private void displaySelectedDeletedContact() {
        int selectedRow = deletedContactTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedDeletedContactId = (int) deletedTableModel.getValueAt(selectedRow, 0);
            platformComboBox.setSelectedItem((String) deletedTableModel.getValueAt(selectedRow, 1));
            linkField.setText((String) deletedTableModel.getValueAt(selectedRow, 2));
            addButton.setEnabled(false);
            updateButton.setEnabled(false); // Cannot update a deleted contact
            softDeleteButton.setEnabled(false); // Cannot soft delete a deleted contact again
            restoreButton.setEnabled(true);
            hardDeleteButton.setEnabled(true); // Can hard delete a deleted contact
        }
    }

    /**
     * Adds a new contact to the database.
     */
    private void addContact() {
        String platform = (String) platformComboBox.getSelectedItem();
        String link = linkField.getText();

        if (platform == null || platform.isEmpty() || link.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Platform and link cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // FIX: The original code in PortfolioAdminApp.java had an issue with 'type' column
        // We will assume 'platform' is what should be stored for the type/value.
        // The table creation only defines 'platform' and 'link'. No 'type' or 'value' column.
        // So, we'll store only platform and link.
        String sql = "INSERT INTO contacts (platform, link, deleted) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platform);
            pstmt.setString(2, link);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Contact added successfully!");
            clearForm();
            loadContacts(); // Refresh tables
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding contact: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates an existing contact in the database.
     */
    private void updateContact() {
        if (selectedContactId == -1) {
            JOptionPane.showMessageDialog(this, "No active contact selected for update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String platform = (String) platformComboBox.getSelectedItem();
        String link = linkField.getText();

        if (platform == null || platform.isEmpty() || link.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Platform and link cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE contacts SET platform = ?, link = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platform);
            pstmt.setString(2, link);
            pstmt.setInt(3, selectedContactId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Contact updated successfully!");
            clearForm();
            loadContacts(); // Refresh tables
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating contact: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Performs a soft delete on the selected active contact.
     */
    private void softDeleteContact() {
        if (selectedContactId == -1) {
            JOptionPane.showMessageDialog(this, "No active contact selected for soft deletion.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Removed the restriction check for "Email" or "Phone" platforms
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to soft delete this contact?", "Confirm Soft Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "UPDATE contacts SET deleted = 1 WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedContactId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Contact soft deleted successfully!");
                clearForm();
                loadContacts(); // Refresh tables
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error soft deleting contact: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Restores a soft-deleted contact.
     */
    private void restoreContact() {
        if (selectedDeletedContactId == -1) {
            JOptionPane.showMessageDialog(this, "No deleted contact selected for restoration.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to restore this contact?", "Confirm Restore", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "UPDATE contacts SET deleted = 0 WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedDeletedContactId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Contact restored successfully!");
                clearForm();
                loadContacts(); // Refresh tables
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error restoring contact: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Permanently deletes the currently selected contact (either active or deleted) from the database.
     */
    private void hardDeleteContact() {
        int idToDelete = -1;
        String contactType = "";

        if (selectedContactId != -1) {
            idToDelete = selectedContactId;
            contactType = "active";
        } else if (selectedDeletedContactId != -1) {
            idToDelete = selectedDeletedContactId;
            contactType = "deleted";
        } else {
            JOptionPane.showMessageDialog(this, "Please select a contact to hard delete.", "No Contact Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Removed the restriction check for "Email" or "Phone" platforms
        int confirm = JOptionPane.showConfirmDialog(this, "WARNING: This will permanently delete the " + contactType + " contact. Are you sure?", "Confirm Hard Delete", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM contacts WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idToDelete);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Contact permanently deleted successfully!");
                clearForm();
                loadContacts(); // Refresh tables
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error permanently deleting contact: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears the form fields and resets selected contact IDs.
     */
    private void clearForm() {  
        platformComboBox.setSelectedIndex(0);
        linkField.setText("");
        selectedContactId = -1;
        selectedDeletedContactId = -1;
        activeContactTable.clearSelection();
        deletedContactTable.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        softDeleteButton.setEnabled(false);
        restoreButton.setEnabled(false);
        hardDeleteButton.setEnabled(false); // Disable hard delete by default until a contact is selected
    }
}
