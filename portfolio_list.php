<?php
session_start();
require 'db.php';

// Ensure user is logged in
if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}

// Handle delete
if (isset($_GET['delete'])) {
    $id = intval($_GET['delete']);
    $stmt = $conn->prepare("DELETE FROM portfolio WHERE id = ?");
    $stmt->bind_param("i", $id);
    $stmt->execute();
    header("Location: portfolio_list.php");
    exit();
}

$result = $conn->query("SELECT * FROM projects");
?>

<!DOCTYPE html>
<html>
<head>
    <title>Portfolio Projects</title>
    <style>
        body { font-family: Arial; padding: 20px; background: #f9f9f9; }
        .project { border: 1px solid #ccc; background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.05); }
        img { max-width: 200px; margin-top: 10px; }
        a { text-decoration: none; color: #2563eb; margin-right: 10px; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>

<h2>All Projects</h2>
<a href="admin_dashboard.php">⬅ Back to Dashboard</a> | 
<a href="add_project.php">➕ Add New Project</a>
<br><br>

<?php while($row = $result->fetch_assoc()): ?>
    <div class="project">
        <h3><?= htmlspecialchars($row['title']) ?></h3>
        <img src="<?= htmlspecialchars($row['image_url']) ?>" alt="Project image">
        <p><?= htmlspecialchars($row['description']) ?></p>
        <a href="edit_project.php?id=<?= $row['id'] ?>">✏️ Edit</a>
        <a href="portfolio_list.php?delete=<?= $row['id'] ?>" onclick="return confirm('Are you sure you want to delete this project?');">🗑 Delete</a>
    </div>
<?php endwhile; ?>

</body>
</html>
