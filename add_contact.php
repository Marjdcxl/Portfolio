<?php
require 'db.php'; // or db.php, depending on your setup

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $platform = $_POST['platform'];
    $link = $_POST['link'];

    if (!empty($platform) && !empty($link)) {
        $stmt = $conn->prepare("INSERT INTO contacts (platform, link) VALUES (?, ?)");
        $stmt->bind_param("ss", $platform, $link);
        if ($stmt->execute()) {
            header("Location: admin_dashboard.php");
            exit();
        } else {
            echo "Failed to add contact.";
        }
    } else {
        echo "Both fields are required.";
    }
}
?>
