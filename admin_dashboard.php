<?php
session_start();
require 'db.php';

if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: login.php");
    exit();
}
?>
<!DOCTYPE html>
<html>
<head>
  <title>Admin Dashboard</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background: #f2f4f8;
      margin: 0;
      padding: 30px;
    }

    h1, h2, h3 {
      color: #333;
    }

    .section {
      background: #ffffff;
      padding: 20px;
      margin-bottom: 30px;
      border-radius: 10px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    a.button, button {
      display: inline-block;
      padding: 10px 15px;
      background-color: #333;
      color: #fff;
      text-decoration: none;
      border: none;
      border-radius: 5px;
      margin-top: 10px;
      cursor: pointer;
    }

    a.button:hover, button:hover {
      background-color: #555;
    }

    form {
      margin-bottom: 15px;
    }

    input[type="text"], input[type="url"], select {
      padding: 8px;
      width: 200px;
      margin-right: 10px;
      border: 1px solid #ccc;
      border-radius: 4px;
    }

    input[disabled] {
      background-color: #eee;
    }

    .delete-link {
      color: red;
      margin-left: 10px;
      text-decoration: none;
    }

    .delete-link:hover {
      text-decoration: underline;
    }

    .logout-container {
      text-align: right;
      margin-bottom: 20px;
    }

    .logout-container .button {
      background-color: #d9534f;
    }
  </style>
</head>
<body>

  <h1>Welcome to the Admin Dashboard</h1>

  <div class="logout-container">
    <a href="logout.php" class="button">Logout</a>
  </div>

  <!-- Portfolio Section -->
  <div class="section">
    <h2>Manage Portfolio Projects</h2>
    <a href="portfolio_list.php" class="button">View Projects</a>
    <a href="portfolio_add.php" class="button">Add Project</a>
  </div>

  <!-- Skills Section -->
  <div class="section">
    <h2>Manage Skills</h2>
    <a href="skills_list.php" class="button">View Skills</a>
    <a href="skills_add.php" class="button">Add Skill</a>
  </div>

  <!-- About Me Section -->
  <div class="section">
    <h2>Manage About Me</h2>
    <a href="about_edit.php" class="button">Edit About</a>
  </div>

  <!-- Contacts Section -->
  <div class="section">
    <h2>Manage Contacts</h2>

    <!-- Add Contact -->
    <form action="add_contact.php" method="POST">
      <select name="platform" required>
        <option value="">Select Platform</option>
        <option value="Facebook">Facebook</option>
        <option value="Phone">Phone</option>
        <option value="Email">Email</option>
        <option value="Other">Other</option>
      </select>
      <input type="text" name="link" placeholder="Enter URL, phone, or email" required>
      <button type="submit">Add Contact</button>
    </form>

    <!-- Existing Contacts -->
    <h3>Existing Contacts</h3>
    <?php
    $result = $conn->query("SELECT * FROM contacts WHERE is_deleted = 0 ORDER BY id DESC");
    if ($result->num_rows > 0):
        while ($row = $result->fetch_assoc()):
    ?>
        <form action="edit_contact.php" method="POST">
            <input type="hidden" name="id" value="<?= $row['id'] ?>">
            <input type="text" name="platform" value="<?= htmlspecialchars($row['platform']) ?>" required>
            <input type="text" name="link" value="<?= htmlspecialchars($row['link']) ?>" required>
            <button type="submit">Save</button>
            <a href="delete_contact.php?id=<?= $row['id'] ?>" onclick="return confirm('Delete this contact?')" class="delete-link">Delete</a>
        </form>
    <?php
        endwhile;
    else:
        echo "<p>No contacts found.</p>";
    endif;
    ?>

    <!-- Deleted Contacts -->
    <h3>Deleted Contacts</h3>
    <?php
    $trashed = $conn->query("SELECT * FROM contacts WHERE is_deleted = 1 ORDER BY id DESC");
    if ($trashed->num_rows > 0):
        while ($row = $trashed->fetch_assoc()):
    ?>
        <form action="restore_contact.php" method="POST">
            <input type="hidden" name="id" value="<?= $row['id'] ?>">
            <input type="text" value="<?= htmlspecialchars($row['platform']) ?>" disabled>
            <input type="text" value="<?= htmlspecialchars($row['link']) ?>" disabled>
            <button type="submit">Restore</button>
        </form>
    <?php
        endwhile;
    else:
        echo "<p>No deleted contacts.</p>";
    endif;
    ?>
  </div>

</body>
</html>
