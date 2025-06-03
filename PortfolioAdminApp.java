package portfolioadminapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * Main class for the Portfolio Admin GUI application.
 * This application allows administrators to manage portfolio projects, skills,
 * 'About Me' content, and contact information through a graphical user interface.
 * It connects to a MySQL database using JDBC.
 */
public class PortfolioAdminApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

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
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize panels
        LoginPanel loginPanel = new LoginPanel(this);
        mainPanel.add(loginPanel, "Login");

        // Show the login panel initially
        cardLayout.show(mainPanel, "Login");

        add(mainPanel);
    }

    /**
     * Switches to the Admin Dashboard panel after successful login.
     */
    public void showAdminDashboard() {
        AdminDashboardPanel dashboardPanel = new AdminDashboardPanel(this);
        mainPanel.add(dashboardPanel, "Dashboard");
        cardLayout.show(mainPanel, "Dashboard");
    }

    /**
     * Switches to the Project Management panel.
     */
    public void showProjectManagement() {
        ProjectManagementPanel projectPanel = new ProjectManagementPanel(this);
        mainPanel.add(projectPanel, "Projects");
        cardLayout.show(mainPanel, "Projects");
    }

    /**
     * Switches to the Skill Management panel.
     */
    public void showSkillManagement() {
        SkillManagementPanel skillPanel = new SkillManagementPanel(this);
        mainPanel.add(skillPanel, "Skills");
        cardLayout.show(mainPanel, "Skills");
    }

    /**
     * Switches to the About Me Management panel.
     */
    public void showAboutManagement() {
        AboutManagementPanel aboutPanel = new AboutManagementPanel(this);
        mainPanel.add(aboutPanel, "About");
        cardLayout.show(mainPanel, "About");
    }

    /**
     * Switches to the Contact Management panel.
     */
    public void showContactManagement() {
        ContactManagementPanel contactPanel = new ContactManagementPanel(this);
        mainPanel.add(contactPanel, "Contacts");
        cardLayout.show(mainPanel, "Contacts");
    }

    /**
     * Switches back to the Login panel (for logout).
     */
    public void showLoginPanel() {
        LoginPanel loginPanel = new LoginPanel(this); // Recreate to clear fields
        mainPanel.add(loginPanel, "Login");
        cardLayout.show(mainPanel, "Login");
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
}

/**
 * Manages database connections and operations for the portfolio application.
 * Uses JDBC to interact with a MySQL database.
 */
class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/portfolio_db";
    private static final String DB_USER = "root"; // Your database username
    private static final String DB_PASSWORD = ""; // Your database password

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
 */
class Project {
    private int id;
    private String title;
    private String description;
    private byte[] image; // changed from imageUrl to image bytes for direct image upload
    private String link;

    public Project(int id, String title, String description, byte[] image, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image = image;
        this.link = link;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}

/**
 * Represents a Skill entity with properties corresponding to the 'skills' table.
 */
class Skill {
    private int id;
    private String name;
    private String level;

    public Skill(int id, String name, String level) {
        this.id = id;
        this.name = name;
        this.level = level;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}

/**
 * Represents a Contact entity with properties corresponding to the 'contacts' table.
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

    /**
     * Constructor for LoginPanel.
     * @param parent The main application frame.
     */
    public LoginPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new GridBagLayout());
        setBackground(new Color(230, 240, 250)); // Light blue/grey background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create a panel for the login form to give it a card-like appearance
        JPanel loginFormPanel = new JPanel(new GridBagLayout());
        loginFormPanel.setBackground(Color.WHITE);
        loginFormPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true), // Softer, rounded border
                new EmptyBorder(30, 30, 30, 30) // Internal padding
        ));
        loginFormPanel.putClientProperty("JComponent.roundRect", true); // Nimbus specific for rounded corners

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(10, 10, 10, 10);
        formGbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Admin Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 60, 90));
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.gridwidth = 2;
        loginFormPanel.add(titleLabel, formGbc);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formGbc.gridx = 0;
        formGbc.gridy = 1;
        formGbc.gridwidth = 1;
        loginFormPanel.add(usernameLabel, formGbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true), // Softer, rounded border
                new EmptyBorder(5, 10, 5, 10)
        ));
        formGbc.gridx = 1;
        formGbc.gridy = 1;
        loginFormPanel.add(usernameField, formGbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formGbc.gridx = 0;
        formGbc.gridy = 2;
        loginFormPanel.add(passwordLabel, formGbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true), // Softer, rounded border
                new EmptyBorder(5, 10, 5, 10)
        ));
        formGbc.gridx = 1;
        formGbc.gridy = 2;
        loginFormPanel.add(passwordField, formGbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(new Color(0, 123, 255)); // Bright blue
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25)); // Padding
        loginButton.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific for rounded buttons
        loginButton.addActionListener(e -> attemptLogin());
        formGbc.gridx = 0;
        formGbc.gridy = 3;
        formGbc.gridwidth = 2;
        loginFormPanel.add(loginButton, formGbc);

        // Message Label for feedback
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(new Color(220, 53, 69)); // Red for error messages
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = 4;
        loginFormPanel.add(messageLabel, formGbc);

        // Add the login form panel to the main LoginPanel
        add(loginFormPanel, gbc);
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
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 248, 255)); // Light background for dashboard

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20); // Increased padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title
        JLabel titleLabel = new JLabel("Welcome to the Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36)); // Larger, bold title
        titleLabel.setForeground(new Color(30, 60, 90));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        // Logout Button (top right)
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutButton.setBackground(new Color(220, 53, 69)); // Red for logout
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        logoutButton.addActionListener(e -> parentFrame.showLoginPanel());
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        add(logoutButton, gbc);

        // Reset GBC for main buttons
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Portfolio Button
        JButton portfolioButton = createDashboardButton("Manage Portfolio Projects", e -> parentFrame.showProjectManagement());
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(portfolioButton, gbc);

        // Skills Button
        JButton skillsButton = createDashboardButton("Manage Skills", e -> parentFrame.showSkillManagement());
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(skillsButton, gbc);

        // About Me Button
        JButton aboutButton = createDashboardButton("Manage About Me", e -> parentFrame.showAboutManagement());
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(aboutButton, gbc);

        // Contacts Button
        JButton contactsButton = createDashboardButton("Manage Contacts", e -> parentFrame.showContactManagement());
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(contactsButton, gbc);
    }

    /**
     * Helper method to create styled dashboard buttons.
     * @param text The text to display on the button.
     * @param listener The ActionListener for the button.
     * @return A styled JButton.
     */
    private JButton createDashboardButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Larger, bold font
        button.setBackground(new Color(0, 123, 255)); // Bright blue
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(250, 80)); // Larger buttons
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific for rounded buttons
        button.addActionListener(listener);
        return button;
    }
}

/**
 * Panel for managing portfolio projects.
 * Allows viewing, adding, editing, and deleting projects.
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
    private BufferedImage uploadedImage; // Store uploaded image
    private int selectedProjectId = -1; // To store the ID of the selected project for editing/deleting

    /**
     * Constructor for ProjectManagementPanel.
     * @param parent The main application frame.
     */
    public ProjectManagementPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(15, 15)); // Increased spacing
        setBackground(new Color(245, 245, 245)); // Slightly darker background for contrast
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Increased padding

        // --- North Panel: Title and Back Button ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(245, 245, 245));
        JLabel titleLabel = new JLabel("Manage Portfolio Projects", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32)); // Larger, bold title
        titleLabel.setForeground(new Color(30, 60, 90));
        northPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createStyledBackButton("⬅ Back to Dashboard", e -> parentFrame.showAdminDashboard());
        northPanel.add(backButton, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // --- Center Panel: Table of Projects ---
        String[] columnNames = {"ID", "Title", "Description", "Link"};
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
        projectTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        projectTable.setRowHeight(25);
        projectTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        projectTable.getTableHeader().setBackground(new Color(0, 123, 255));
        projectTable.getTableHeader().setForeground(Color.WHITE);
        projectTable.setGridColor(new Color(220, 220, 220));
        projectTable.setSelectionBackground(new Color(173, 216, 230)); // Light blue selection
        projectTable.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(projectTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)); // Rounded border for scroll pane
        add(scrollPane, BorderLayout.CENTER);

        // --- South Panel: Form for Add/Edit ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true), // Rounded title border
                "Project Details",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(30, 60, 90)
        ));
        formPanel.setBackground(Color.WHITE);
        formPanel.putClientProperty("JComponent.roundRect", true); // Nimbus specific for rounded corners
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Increased padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; titleField = createStyledTextField(); formPanel.add(titleField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; descriptionArea = createStyledTextArea(3, 20);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        formPanel.add(descScrollPane, gbc);

        // Link (optional)
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Link (optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; linkField = createStyledTextField(); formPanel.add(linkField, gbc);

        // Upload Image Button
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Upload Image:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        uploadImageButton = new JButton("Choose Image");
        uploadImageButton.addActionListener(e -> uploadImage());
        formPanel.add(uploadImageButton, gbc);

        // Image Preview
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Image Preview:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        imagePreviewLabel = new JLabel("No Image", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(150, 100));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        imagePreviewLabel.setForeground(Color.GRAY);
        formPanel.add(imagePreviewLabel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Increased spacing
        buttonPanel.setBackground(Color.WHITE);
        addButton = createStyledButton("Add Project", new Color(40, 167, 69)); // Green
        updateButton = createStyledButton("Update Project", new Color(0, 123, 255)); // Blue
        deleteButton = createStyledButton("Delete Project", new Color(220, 53, 69)); // Red
        JButton clearButton = createStyledButton("Clear Form", new Color(108, 117, 125)); // Grey

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
        formPanel.add(buttonPanel, gbc);
        add(formPanel, BorderLayout.SOUTH);

        loadProjects(); // Load data when panel is initialized
        clearForm(); // Set initial button states
    }

    /** Helper method to create a styled JTextField. */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    /** Helper method to create a styled JTextArea. */
    private JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return area;
    }

    /** Helper method to create a styled JButton. */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        return button;
    }

    /** Helper method to create a styled back button. */
    private JButton createStyledBackButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(108, 117, 125)); // Grey
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        button.addActionListener(listener);
        return button;
    }

    /**
     * Loads all projects from the database and populates the table.
     */
    private void loadProjects() {
        tableModel.setRowCount(0); // Clear existing data
        String sql = "SELECT id, title, description, link FROM projects ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String link = rs.getString("link");
                tableModel.addRow(new Object[]{id, title, description, link});
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
            linkField.setText((String) tableModel.getValueAt(selectedRow, 3));

            // Reset uploadedImage and image preview when a project is selected, since we do not retrieve image bytes here
            uploadedImage = null;
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No Image");

            addButton.setEnabled(false); // Disable add when editing
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    /**
     * Adds a new project to the database.
     */
    private void addProject() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String link = linkField.getText();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO projects (title, description, image, link) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            if (uploadedImage != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(uploadedImage, "png", baos);
                pstmt.setBytes(3, baos.toByteArray());
            } else {
                pstmt.setNull(3, Types.BLOB);
            }
            if (link == null || link.trim().isEmpty()) {
                pstmt.setNull(4, Types.VARCHAR);
            } else {
                pstmt.setString(4, link);
            }
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Project added successfully!");
            clearForm();
            loadProjects(); // Refresh table
        } catch (SQLException | IOException e) {
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

        String title = titleField.getText();
        String description = descriptionArea.getText();
        String link = linkField.getText();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql;
        boolean hasImage = uploadedImage != null;
        if (hasImage) {
            sql = "UPDATE projects SET title = ?, description = ?, image = ?, link = ? WHERE id = ?";
        } else {
            sql = "UPDATE projects SET title = ?, description = ?, link = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            int paramIndex = 3;
            if (hasImage) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(uploadedImage, "png", baos);
                pstmt.setBytes(paramIndex++, baos.toByteArray());
            }
            if (link == null || link.trim().isEmpty()) {
                pstmt.setNull(paramIndex++, Types.VARCHAR);
            } else {
                pstmt.setString(paramIndex++, link);
            }
            pstmt.setInt(paramIndex, selectedProjectId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Project updated successfully!");
            clearForm();
            loadProjects(); // Refresh table
        } catch (SQLException | IOException e) {
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

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this project?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
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
        uploadedImage = null;
        selectedProjectId = -1;
        projectTable.clearSelection(); // Deselect row
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        imagePreviewLabel.setIcon(null); // Clear image preview
        imagePreviewLabel.setText("No Image"); // Reset text
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
                uploadedImage = ImageIO.read(selectedFile);
                ImageIcon icon = new ImageIcon(uploadedImage.getScaledInstance(150, 100, Image.SCALE_SMOOTH));
                imagePreviewLabel.setIcon(icon);
                imagePreviewLabel.setText(""); // Clear text if image loads successfully
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage(), "Image Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

/**
 * Panel for managing skills.
 * Allows viewing, adding, editing, and deleting skills.
 */
class SkillManagementPanel extends JPanel {
    private PortfolioAdminApp parentFrame;
    private DefaultTableModel tableModel;
    private JTable skillTable;
    private JComboBox<String> skillComboBox;
    private JComboBox<String> levelComboBox;
    private JButton addButton, updateButton, deleteButton;
    private int selectedSkillId = -1;

    /**
     * Constructor for SkillManagementPanel.
     * @param parent The main application frame.
     */
    public SkillManagementPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- North Panel: Title and Back Button ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(245, 245, 245));
        JLabel titleLabel = new JLabel("Manage Skills", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(30, 60, 90));
        northPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createStyledBackButton("⬅ Back to Dashboard", e -> parentFrame.showAdminDashboard());
        northPanel.add(backButton, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // --- Center Panel: Table of Skills ---
        String[] columnNames = {"ID", "Skill", "Level"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        skillTable = new JTable(tableModel);
        skillTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        skillTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && skillTable.getSelectedRow() != -1) {
                displaySelectedSkill();
            }
        });
        // Table styling
        skillTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        skillTable.setRowHeight(25);
        skillTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        skillTable.getTableHeader().setBackground(new Color(0, 123, 255));
        skillTable.getTableHeader().setForeground(Color.WHITE);
        skillTable.setGridColor(new Color(220, 220, 220));
        skillTable.setSelectionBackground(new Color(173, 216, 230));
        skillTable.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(skillTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        add(scrollPane, BorderLayout.CENTER);

        // --- South Panel: Form for Add/Edit ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                "Skill Details",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(30, 60, 90)
        ));
        formPanel.setBackground(Color.WHITE);
        formPanel.putClientProperty("JComponent.roundRect", true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Skill ComboBox
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Skill:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        String[] skills = {"Java", "HTML", "CSS", "JavaScript", "Python", "C++", "Ruby", "PHP"};
        skillComboBox = new JComboBox<>(skills);
        formPanel.add(skillComboBox, gbc);

        // Level ComboBox
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Level:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        String[] levels = {"Beginner", "Intermediate", "Expert"};
        levelComboBox = new JComboBox<>(levels);
        formPanel.add(levelComboBox, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        addButton = createStyledButton("Add Skill", new Color(40, 167, 69));
        updateButton = createStyledButton("Update Skill", new Color(0, 123, 255));
        deleteButton = createStyledButton("Delete Skill", new Color(220, 53, 69));
        JButton clearButton = createStyledButton("Clear Form", new Color(108, 117, 125));

        addButton.addActionListener(e -> addSkill());
        updateButton.addActionListener(e -> updateSkill());
        deleteButton.addActionListener(e -> deleteSkill());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; formPanel.add(buttonPanel, gbc);
        add(formPanel, BorderLayout.SOUTH);

        loadSkills();
        clearForm();
    }

    /** Helper method to create a styled JTextField. */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    /** Helper method to create a styled JButton. */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        return button;
    }

    /** Helper method to create a styled back button. */
    private JButton createStyledBackButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(108, 117, 125)); // Grey
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        button.addActionListener(listener);
        return button;
    }

    /**
     * Loads all skills from the database and populates the table.
     */
    private void loadSkills() {
        tableModel.setRowCount(0);
        String sql = "SELECT id, name, level FROM skills";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String level = rs.getString("level");
                tableModel.addRow(new Object[]{id, name, level});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading skills: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Displays the details of the selected skill in the form fields.
     */
    private void displaySelectedSkill() {
        int selectedRow = skillTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedSkillId = (int) tableModel.getValueAt(selectedRow, 0);
            String skillName = (String) tableModel.getValueAt(selectedRow, 1);
            String skillLevel = (String) tableModel.getValueAt(selectedRow, 2);
            skillComboBox.setSelectedItem(skillName);
            levelComboBox.setSelectedItem(skillLevel);
            addButton.setEnabled(false);
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    /**
     * Adds a new skill to the database.
     */
    private void addSkill() {
        String skill = (String) skillComboBox.getSelectedItem();
        String level = (String) levelComboBox.getSelectedItem();

        if (skill == null || level == null) {
            JOptionPane.showMessageDialog(this, "Skill and level cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO skills (name, level) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, skill);
            pstmt.setString(2, level);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Skill added successfully!");
            clearForm();
            loadSkills(); // Refresh table
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding skill: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates an existing skill in the database.
     */
    private void updateSkill() {
        if (selectedSkillId == -1) {
            JOptionPane.showMessageDialog(this, "No skill selected for update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String skill = (String) skillComboBox.getSelectedItem();
        String level = (String) levelComboBox.getSelectedItem();

        if (skill == null || level == null) {
            JOptionPane.showMessageDialog(this, "Skill and level cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE skills SET name = ?, level = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, skill);
            pstmt.setString(2, level);
            pstmt.setInt(3, selectedSkillId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Skill updated successfully!");
            clearForm();
            loadSkills();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating skill: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes the selected skill from the database.
     */
    private void deleteSkill() {
        if (selectedSkillId == -1) {
            JOptionPane.showMessageDialog(this, "No skill selected for deletion.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this skill?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM skills WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedSkillId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Skill deleted successfully!");
                clearForm();
                loadSkills();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting skill: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears the form fields and resets the selected skill ID.
     */
    private void clearForm() {
        skillComboBox.setSelectedIndex(0);
        levelComboBox.setSelectedIndex(0);
        selectedSkillId = -1;
        skillTable.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
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
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- North Panel: Title and Back Button ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(245, 245, 245));
        JLabel titleLabel = new JLabel("Manage About Me Content", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(30, 60, 90));
        northPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createStyledBackButton("⬅ Back to Dashboard", e -> parentFrame.showAdminDashboard());
        northPanel.add(backButton, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // --- Center Panel: About Me Content Area ---
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                "About Me Text",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(30, 60, 90)
        ));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.putClientProperty("JComponent.roundRect", true);
        aboutContentArea = createStyledTextArea(15, 50);
        JScrollPane scrollPane = new JScrollPane(aboutContentArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // --- South Panel: Save Button ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 245)); // Match panel background
        saveButton = createStyledButton("Save About Me", new Color(0, 123, 255));
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
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return area;
    }

    /** Helper method to create a styled JButton. */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        return button;
    }

    /** Helper method to create a styled back button. */
    private JButton createStyledBackButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(108, 117, 125)); // Grey
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        button.addActionListener(listener);
        return button;
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
 * Allows viewing, adding, editing, soft deleting, and restoring contacts.
 */
class ContactManagementPanel extends JPanel {
    private PortfolioAdminApp parentFrame;
    private DefaultTableModel activeTableModel, deletedTableModel;
    private JTable activeContactTable, deletedContactTable;
    private JTextField linkField;
    private JComboBox<String> platformComboBox;
    private JButton addButton, updateButton, softDeleteButton, restoreButton, clearButton;
    private int selectedContactId = -1; // For active contacts
    private int selectedDeletedContactId = -1; // For deleted contacts

    /**
     * Constructor for ContactManagementPanel.
     * @param parent The main application frame.
     */
    public ContactManagementPanel(PortfolioAdminApp parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- North Panel: Title and Back Button ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(245, 245, 245));
        JLabel titleLabel = new JLabel("Manage Contacts", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(30, 60, 90));
        northPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createStyledBackButton("⬅ Back to Dashboard", e -> parentFrame.showAdminDashboard());
        northPanel.add(backButton, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // --- Center Panel: Tables for Active and Deleted Contacts ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5); // Distribute space evenly
        splitPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default border

        // Active Contacts Table
        JPanel activePanel = new JPanel(new BorderLayout());
        activePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                "Active Contacts",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(30, 60, 90)
        ));
        activePanel.setBackground(Color.WHITE);
        activePanel.putClientProperty("JComponent.roundRect", true);
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
        activeContactTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        activeContactTable.setRowHeight(25);
        activeContactTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        activeContactTable.getTableHeader().setBackground(new Color(0, 123, 255));
        activeContactTable.getTableHeader().setForeground(Color.WHITE);
        activeContactTable.setGridColor(new Color(220, 220, 220));
        activeContactTable.setSelectionBackground(new Color(173, 216, 230));
        activeContactTable.setSelectionForeground(Color.BLACK);

        JScrollPane activeScrollPane = new JScrollPane(activeContactTable);
        activeScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border
        activePanel.add(activeScrollPane, BorderLayout.CENTER);
        splitPane.setTopComponent(activePanel);

        // Deleted Contacts Table
        JPanel deletedPanel = new JPanel(new BorderLayout());
        deletedPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                "Deleted Contacts (Trash)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(30, 60, 90)
        ));
        deletedPanel.setBackground(Color.WHITE);
        deletedPanel.putClientProperty("JComponent.roundRect", true);
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
        deletedContactTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deletedContactTable.setRowHeight(25);
        deletedContactTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        deletedContactTable.getTableHeader().setBackground(new Color(0, 123, 255));
        deletedContactTable.getTableHeader().setForeground(Color.WHITE);
        deletedContactTable.setGridColor(new Color(220, 220, 220));
        deletedContactTable.setSelectionBackground(new Color(173, 216, 230));
        deletedContactTable.setSelectionForeground(Color.BLACK);

        JScrollPane deletedScrollPane = new JScrollPane(deletedContactTable);
        deletedScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border
        deletedPanel.add(deletedScrollPane, BorderLayout.CENTER);
        splitPane.setBottomComponent(deletedPanel);

        add(splitPane, BorderLayout.CENTER);

        // --- South Panel: Form for Add/Edit/Restore ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                "Contact Details / Actions",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(30, 60, 90)
        ));
        formPanel.setBackground(Color.WHITE);
        formPanel.putClientProperty("JComponent.roundRect", true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Platform
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Platform:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        String[] platforms = {"", "Facebook", "Phone", "Email", "Other"};
        platformComboBox = new JComboBox<>(platforms);
        platformComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        platformComboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(platformComboBox, gbc);

        // Link
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Link:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; linkField = createStyledTextField(); formPanel.add(linkField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        addButton = createStyledButton("Add Contact", new Color(40, 167, 69));
        updateButton = createStyledButton("Update Contact", new Color(0, 123, 255));
        softDeleteButton = createStyledButton("Soft Delete", new Color(255, 193, 7)); // Yellow for soft delete
        restoreButton = createStyledButton("Restore Contact", new Color(23, 162, 184)); // Cyan for restore
        clearButton = createStyledButton("Clear Form", new Color(108, 117, 125));

        addButton.addActionListener(e -> addContact());
        updateButton.addActionListener(e -> updateContact());
        softDeleteButton.addActionListener(e -> softDeleteContact());
        restoreButton.addActionListener(e -> restoreContact());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(softDeleteButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; formPanel.add(buttonPanel, gbc);
        add(formPanel, BorderLayout.SOUTH);

        loadContacts(); // Load data when panel is initialized
        clearForm(); // Set initial button states
    }

    /** Helper method to create a styled JTextField. */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    /** Helper method to create a styled JButton. */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        return button;
    }

    /** Helper method to create a styled back button. */
    private JButton createStyledBackButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(108, 117, 125)); // Grey
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.putClientProperty("JButton.buttonType", "roundRect"); // Nimbus specific
        button.addActionListener(listener);
        return button;
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
    }
}

    