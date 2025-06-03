<?php
require 'db.php';

// Automatically create 'skills' table if it doesn't exist
$conn->query("CREATE TABLE IF NOT EXISTS skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
)");

// Automatically create 'about' table if it doesn't exist
$conn->query("CREATE TABLE IF NOT EXISTS about (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL
)");

// Fetch projects
$projects = [];
$project_result = $conn->query("SELECT * FROM projects ORDER BY created_at DESC");
if ($project_result && $project_result->num_rows > 0) {
    while ($row = $project_result->fetch_assoc()) {
        $projects[] = $row;
    }
}

// Fetch contact info from database
$contacts_result = $conn->query("SELECT * FROM contacts");
$contacts = [];
if ($contacts_result && $contacts_result->num_rows > 0) {
    while ($row = $contacts_result->fetch_assoc()) {
        $contacts[] = $row;
    }
}

// Fetch skills
$skills = [];
$skills_result = $conn->query("SELECT * FROM skills");
if ($skills_result && $skills_result->num_rows > 0) {
    while ($row = $skills_result->fetch_assoc()) {
        $skills[] = $row['name'];
    }
}

// Fetch about me
$about = "No about info yet.";
$about_result = $conn->query("SELECT content FROM about LIMIT 1");
if ($about_result && $about_result->num_rows > 0) {
    $row = $about_result->fetch_assoc();
    $about = $row['content'];
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Online Portfolio</title>
    <link rel="stylesheet" href="style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        #preloader {
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: white;
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 1000;
        }
        #loader {
            width: 40px; height: 40px;
            border: 4px solid #ddd;
            border-top: 4px solid #333;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
    </style>
</head>
<body>

<div id="preloader"><div id="loader"></div></div>

<header class="s-header">
    <div class="nav-container">
        <h1><span class="bold">BENJIE JUABOT</span></h1>
        <nav>
            <ul>
                <li><a href="#portfolio" class="smoothscroll">PORTFOLIO</a></li>
                <li><a href="#contact" class="smoothscroll">CONTACT</a></li>
                <li><a href="#skills" class="smoothscroll">SKILLS</a></li>
                <li><a href="#about" class="smoothscroll">ABOUT ME</a></li>
            </ul>
        </nav>
    </div>
</header>

<section id="hero" class="hero">
    <div class="hero-content">
        <div class="image-container">
            <img src="INDEX/BENJIE.png" alt="Benjie Juabot">
            <span class="red-dot"></span>
        </div>
        <div class="text">
            <h2>Hi, I am Benjie, a</h2>
            <h2>Computer Engineer</h2>
            <button class="talk-btn smoothscroll" data-target="#contact">Let's talk</button>
        </div>
    </div>
</section>

<section id="portfolio">
    <h2>Projects</h2>
    <div class="portfolio-gallery">
        <?php if (count($projects) > 0): ?>
            <?php foreach ($projects as $project): ?>
                <div class="project-card">
                    <h3><?= htmlspecialchars($project['title']) ?></h3>
                    <p><?= htmlspecialchars($project['description']) ?></p>
                    <?php if (!empty($project['link'])): ?>
                        <a href="<?= htmlspecialchars($project['link']) ?>" target="_blank">View Project</a>
                    <?php endif; ?>
                </div>
            <?php endforeach; ?>
        <?php else: ?>
            <p>No projects found.</p>
        <?php endif; ?>
    </div>
</section>

<section id="skills">
    <h2>SKILLS</h2>
    <ul>
        <?php
        if (count($skills) > 0):
            foreach ($skills as $skill):
        ?>
            <li><?= htmlspecialchars($skill) ?></li>
        <?php
            endforeach;
        else:
            echo "<p>No skills added yet.</p>";
        endif;
        ?>
    </ul>
</section>

<section id="about">
    <h2>About Me</h2>
    <p><?= nl2br(htmlspecialchars($about)) ?></p>
</section>

<section id="contact">
    <h2>Contact</h2>
    <?php if (count($contacts) > 0): ?>
        <?php foreach ($contacts as $contact): ?>
            <p>
                <?php if ($contact['platform'] == "Facebook"): ?>
                    <i class="fab fa-facebook-f"></i>
                    <a href="<?= htmlspecialchars($contact['link']) ?>" target="_blank"><?= htmlspecialchars($contact['link']) ?></a>
                <?php elseif ($contact['platform'] == "Phone"): ?>
                    <i class="fas fa-phone"></i>
                    <?= htmlspecialchars($contact['link']) ?>
                <?php elseif ($contact['platform'] == "Email"): ?>
                    <i class="fas fa-envelope"></i>
                    <a href="mailto:<?= htmlspecialchars($contact['link']) ?>"><?= htmlspecialchars($contact['link']) ?></a>
                <?php else: ?>
                    <i class="fas fa-link"></i>
                    <?= htmlspecialchars($contact['link']) ?> (<?= htmlspecialchars($contact['platform']) ?>)
                <?php endif; ?>
            </p>
        <?php endforeach; ?>
    <?php else: ?>
        <p>No contact information available.</p>
    <?php endif; ?>
</section>

<button class="ss-go-top">&#9650;</button>

<script>
    // Smooth scroll
    document.querySelectorAll('.smoothscroll').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href') || this.dataset.target;
            if (targetId) {
                document.querySelector(targetId).scrollIntoView({ behavior: 'smooth' });
            }
        });
    });

    // Preloader
    window.addEventListener("load", () => {
        const preloader = document.getElementById("preloader");
        if (preloader) preloader.style.display = "none";
    });
</script>

</body>
</html>
<php>
