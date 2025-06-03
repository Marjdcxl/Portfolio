<?php
session_start();
require 'db.php';

if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $title = $_POST['title'];
    $description = $_POST['description'];
    $image_url = $_POST['image_url'];
    $link = $_POST['link'];

    $stmt = $conn->prepare("INSERT INTO projects (title, description, image_url, link) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("ssss", $title, $description, $image_url, $link);
    $stmt->execute();

    header("Location: portfolio_list.php");
    exit();
}
?>

<!DOCTYPE html>
<html>
<head>
  <title>Add New Project</title>
  <style>
    body { font-family: Arial; padding: 30px; background: #f9f9f9; }
    form { background: white; padding: 20px; max-width: 500px; margin: auto; border-radius: 10px; box-shadow: 0 0 10px #aaa; }
    input, textarea { width: 100%; padding: 10px; margin-bottom: 15px; }
    button { padding: 10px 15px; background: #333; color: white; border: none; border-radius: 5px; }
    a { display: inline-block; margin-top: 10px; color: #2563eb; text-decoration: none; }
  </style>
</head>
<body>

<h2 style="text-align:center;">Add New Project</h2>

<<form method="POST">
  <label>Project Title</label>
  <input type="text" name="title" required>

  <label>Description</label>
  <textarea name="description" required></textarea>

  <label>Image URL</label>
  <input type="text" name="image_url">

  <label>Project Link</label>
  <input type="text" name="link">

  <button type="submit">Add Project</button>
</form>

<a href="portfolio_list.php">⬅ Back to Projects</a>

</body>
</html>
