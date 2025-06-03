<?php
session_start();
require 'db.php';

if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}

// Show active (non-deleted) contacts
$result = $conn->query("SELECT * FROM contacts WHERE deleted = 0");
?>

<!DOCTYPE html>
<html>
<head>
    <title>Edit Contacts</title>
    <style>
        body { font-family: Arial; padding: 30px; background: #f9f9f9; }
        .contact-item { background: white; padding: 15px; margin-bottom: 10px; border-radius: 8px; box-shadow: 0 0 5px #ddd; }
        a { margin-right: 10px; text-decoration: none; color: #2563eb; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>

<h2>All Contacts</h2>
<a href="admin_dashboard.php">⬅ Back to Dashboard</a> |
<a href="add_contact.php">➕ Add New Contact</a>
<br><br>

<?php while ($row = $result->fetch_assoc()): ?>
    <div class="contact-item">
        <strong><?= htmlspecialchars($row['platform']) ?></strong>: <?= htmlspecialchars($row['link']) ?><br>
        <a href="edit_contact.php?id=<?= $row['id'] ?>">✏️ Edit</a>
        <a href="delete_contact.php?id=<?= $row['id'] ?>" onclick="return confirm('Are you sure?')">🗑️ Delete</a>
    </div>
<?php endwhile; ?>

</body>
</html>
