<?php
require 'db.php';

if (isset($_GET['id'])) {
    $id = intval($_GET['id']);

    // Soft delete by setting deleted = 1
    $stmt = $conn->prepare("UPDATE contacts SET deleted = 1 WHERE id = ?");
    $stmt->bind_param("i", $id);

    if ($stmt->execute()) {
        header("Location: edit_contacts.php");
        exit();
    } else {
        echo "Error deleting contact.";
    }
}
?>
