<?php
session_start();
require 'db.php';

if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}

// Load current about content
$about = "";
$result = $conn->query("SELECT * FROM about LIMIT 1");
if ($row = $result->fetch_assoc()) {
    $about = $row['content'];
}

// Save updates
if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $newContent = $_POST['content'];

    if ($result->num_rows > 0) {
        // Update existing
        $stmt = $conn->prepare("UPDATE about SET content = ? WHERE id = ?");
        $stmt->bind_param("si", $newContent, $row['id']);
    } else {
        // Insert if no about exists yet
        $stmt = $conn->prepare("INSERT INTO about (content) VALUES (?)");
        $stmt->bind_param("s", $newContent);
    }

    $stmt->execute();
    header("Location: admin_dashboard.php");
    exit();
}
?>

<!DOCTYPE html>
<html>
<head>
  <title>Edit About Me</title>
  <style>
    body { font-family: Arial; padding: 30px; background: #f0f0f0; }
    form { background: white; padding: 20px; max-width: 600px; margin: auto; border-radius: 10px; box-shadow: 0 0 10px #ccc; }
    textarea { width: 100%; height: 200px; padding: 10px; margin-bottom: 15px; }
    button { padding: 10px 20px; background: #333; color: white; border: none; border-radius: 5px; }
    a { display: inline-block; margin-top: 10px; text-decoration: none; color: #2563eb; }
  </style>
</head>
<body>

<h2 style="text-align:center;">Edit "About Me"</h2>

<form method="POST">
  <label>About Me Content:</label>
  <textarea name="content"><?= htmlspecialchars($about) ?></textarea>
  <button type="submit">Save</button>
</form>

<a href="admin_dashboard.php">⬅ Back to Dashboard</a>

</body>
</html>
