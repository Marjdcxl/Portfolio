  <?php session_start(); ?>
<!DOCTYPE html>
<html>
<head>
  <title>Admin Login</title>
  <style>
    body { font-family: Arial; display: flex; justify-content: center; align-items: center; height: 100vh; background: #f0f0f0; }
    form { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px #aaa; }
    input { display: block; margin-bottom: 15px; width: 90%; padding: 8px; }
    button { padding: 10px; width: 100%; background: #333; color: white; border: none; cursor: pointer; }
  </style>
</head>
<body>
  <form action="check_login.php" method="POST">
    <h2>Admin Login</h2>
    <input type="text" name="username" placeholder="Username" required />
    <input type="password" name="password" placeholder="Password" required />
    <button type="submit">Login</button>
  </form>
</body>
</html>
