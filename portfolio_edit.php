<?php
session_start();
if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}

$conn = new mysqli("localhost", "root", "", "portfolio_db");
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get project ID
$id = $_GET['id'] ?? null;
if (!$id) {
    echo "Project ID not found.";
    exit();
}

// Handle form submission
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $title = $_POST['title'];
    $desc = $_POST['description'];
    $image = $_POST['image_url'];

    $stmt = $conn->prepare("UPDATE portfolio SET title = ?, description = ?, image_url = ? WHERE id = ?");
    $stmt->bind_param("sssi", $title, $desc, $image, $id);
    $stmt->execute();

    header("Location: portfolio_list.php");
    exit();
}

// Fetch current project data
$stmt = $conn->prepare("SELECT * FROM portfolio WHERE id = ?");
$stmt->bind_param("i", $id);
$stmt->execute();
$result = $stmt->get_result();
$project = $result->fetch_assoc();

if (!$project) {
    echo "Project not found.";
    exit();
}
?>

<!DOCTYPE html>
<html>
<head>
  <title>Edit Project</title>
  <style>
    body { font-family: Arial; padding: 30px; background: #f9f9f9; }
    form { background: white; padding: 20px; max-width: 500px; margin: auto; border-radius: 10px; box-shadow: 0 0 10px #aaa; }
    input, textarea { width: 100%; padding: 10px; margin-bottom: 15px; }
    button { padding: 10px 15px; background: #333; color: white; border: none; border-radius: 5px; }
    a { display: inline-block; margin-top: 10px; text-decoration: none; color: #2563eb; }
  </style>
</head>
<body>

<h2 style="text-align:center;">Edit Project</h2>

<form method="POST">
  <label>Title</label>
  <input type="text" name="title" value="<?= htmlspecialchars($project['title']) ?>" required>

  <label>Description</label>
  <textarea name="description" required><?= htmlspecialchars($project['description']) ?></textarea>

  <label>Image URL</label>
  <input type="text" name="image_url" value="<?= htmlspecialchars($project['image_url']) ?>" required>

  <button type="submit">Update Project</button>
</form>

<a href="portfolio_list.php">⬅ Back to Projects</a>

</body>
</html>
