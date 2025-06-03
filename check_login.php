<?php
session_start();
require 'db.php';

$username = $_POST['username'];
$password = $_POST['password'];

// Hash the user input (admin123) using SHA-256
$hashedPassword = hash('sha256', $password);

// Prepare and execute the SQL
$stmt = $conn->prepare("SELECT * FROM users WHERE username = ? AND password = ?");
$stmt->bind_param("ss", $username, $hashedPassword);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $_SESSION['admin_logged_in'] = true;
    header("Location: admin_dashboard.php");
    exit();
} else {
    echo "❌ Invalid username or password.";
}
?>
