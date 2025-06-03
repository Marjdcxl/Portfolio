<?php
session_start();
require 'db.php';

if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $name = $_POST['name'];
    $level = $_POST['level'];

    $stmt = $conn->prepare("INSERT INTO skills (name, level) VALUES (?, ?)");
    $stmt->bind_param("ss", $name, $level);
    $stmt->execute();

    header("Location: skills_list.php");
    exit();
}
?>

<!DOCTYPE html>
<html>
<head>
  <title>Add Skill</title>
  <style>
    body { font-family: Arial; padding: 30px; background: #f9f9f9; }
    form { background: white; padding: 20px; max-width: 500px; margin: auto; border-radius: 10px; box-shadow: 0 0 10px #aaa; }
    input { width: 100%; padding: 10px; margin-bottom: 15px; }
    button { padding: 10px 15px; background: #333; color: white; border: none; border-radius: 5px; }
    a { display: inline-block; margin-top: 10px; color: #2563eb; text-decoration: none; }
  </style>
</head>
<body>

<h2 style="text-align:center;">Add New Skill</h2>

<form method="POST">
  <label>Skill Name</label>
  <input type="text" name="name" required>

  <label>Skill Level (e.g. Beginner, Intermediate, Expert)</label>
  <input type="text" name="level" required>

  <button type="submit">Add Skill</button>
</form>

<a href="skills_list.php">⬅ Back to Skills</a>

</body>
</html>
