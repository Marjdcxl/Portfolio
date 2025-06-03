<?php
session_start();
require 'db.php';

if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}

// Get skill ID
$id = $_GET['id'] ?? null;
if (!$id) {
    echo "Skill ID is missing.";
    exit();
}

// Handle form submission
if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $name = $_POST['name'];
    $level = $_POST['level'];

    $stmt = $conn->prepare("UPDATE skills SET name = ?, level = ? WHERE id = ?");
    $stmt->bind_param("ssi", $name, $level, $id);
    $stmt->execute();

    header("Location: skills_list.php");
    exit();
}

// Load skill data
$stmt = $conn->prepare("SELECT * FROM skills WHERE id = ?");
$stmt->bind_param("i", $id);
$stmt->execute();
$result = $stmt->get_result();
$skill = $result->fetch_assoc();

if (!$skill) {
    echo "Skill not found.";
    exit();
}
?>

<!DOCTYPE html>
<html>
<head>
  <title>Edit Skill</title>
  <style>
    body { font-family: Arial; padding: 30px; background: #f9f9f9; }
    form { background: white; padding: 20px; max-width: 500px; margin: auto; border-radius: 10px; box-shadow: 0 0 10px #aaa; }
    input { width: 100%; padding: 10px; margin-bottom: 15px; }
    button { padding: 10px 15px; background: #333; color: white; border: none; border-radius: 5px; }
    a { display: inline-block; margin-top: 10px; color: #2563eb; text-decoration: none; }
  </style>
</head>
<body>

<h2 style="text-align:center;">Edit Skill</h2>

<form method="POST">
  <label>Skill Name</label>
  <input type="text" name="name" value="<?= htmlspecialchars($skill['name']) ?>" required>

  <label>Skill Level</label>
  <input type="text" name="level" value="<?= htmlspecialchars($skill['level']) ?>" required>

  <button type="submit">Update Skill</button>
</form>

<a href="skills_list.php">⬅ Back to Skills</a>

</body>
</html>
