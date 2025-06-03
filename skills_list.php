<?php
session_start();
require 'db.php';

if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}

// Delete skill
if (isset($_GET['delete'])) {
    $id = intval($_GET['delete']);
    $stmt = $conn->prepare("DELETE FROM skills WHERE id = ?");
    $stmt->bind_param("i", $id);
    $stmt->execute();
    header("Location: skills_list.php");
    exit();
}

// Get all skills
$result = $conn->query("SELECT * FROM skills");
?>

<!DOCTYPE html>
<html>
<head>
  <title>Manage Skills</title>
  <style>
    body { font-family: Arial; padding: 30px; background: #f9f9f9; }
    .skill { background: white; border: 1px solid #ddd; padding: 15px; margin-bottom: 10px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.05); }
    a { margin-right: 10px; text-decoration: none; color: #2563eb; }
    a:hover { text-decoration: underline; }
  </style>
</head>
<body>

<h2>All Skills</h2>
<a href="admin_dashboard.php">⬅ Back to Dashboard</a> | 
<a href="skills_add.php">➕ Add New Skill</a>
<br><br>

<?php while ($row = $result->fetch_assoc()): ?>
  <div class="skill">
    <strong><?= htmlspecialchars($row['name']) ?></strong> – <?= htmlspecialchars($row['level']) ?>
    <br>
    <a href="skills_edit.php?id=<?= $row['id'] ?>">✏️ Edit</a>
    <a href="skills_list.php?delete=<?= $row['id'] ?>" onclick="return confirm('Delete this skill?');">🗑️ Delete</a>
  </div>
<?php endwhile; ?>

</body>
</html>
