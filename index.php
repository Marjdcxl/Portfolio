<?php
require 'db.php'; // Include your database connection file

// Automatically create 'skills' table if it doesn't exist (from your original index.php)
$conn->query("CREATE TABLE IF NOT EXISTS skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    level VARCHAR(20) NOT NULL
)");

// Automatically create 'about' table if it doesn't exist (from your original index.php)
$conn->query("CREATE TABLE IF NOT EXISTS about (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL
)");

// Automatically create 'projects' table if it doesn't exist (from your original index.php)
$conn->query("CREATE TABLE IF NOT EXISTS projects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    link VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)");

// Automatically create 'contacts' table if it doesn't exist (from your original index.php)
$conn->query("CREATE TABLE IF NOT EXISTS contacts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    platform VARCHAR(100) DEFAULT NULL,
    link VARCHAR(255) DEFAULT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0
)");


// Fetch projects from database
$projects = [];
// Ensure 'projects' table exists before querying
if ($conn->query("SHOW TABLES LIKE 'projects'")->num_rows > 0) {
    $project_result = $conn->query("SELECT * FROM projects ORDER BY created_at DESC");
    if ($project_result && $project_result->num_rows > 0) {
        while ($row = $project_result->fetch_assoc()) {
            $projects[] = $row;
        }
    }
}


// Fetch contact info from database
$contacts = [];
// Ensure 'contacts' table exists before querying
if ($conn->query("SHOW TABLES LIKE 'contacts'")->num_rows > 0) {
    $contacts_result = $conn->query("SELECT * FROM contacts WHERE deleted = 0");
    if ($contacts_result && $contacts_result->num_rows > 0) {
        while ($row = $contacts_result->fetch_assoc()) {
            $contacts[] = $row;
        }
    }
}

// Fetch skills from database
$skills = [];
// Ensure 'skills' table exists before querying
if ($conn->query("SHOW TABLES LIKE 'skills'")->num_rows > 0) {
    $skills_result = $conn->query("SELECT * FROM skills");
    if ($skills_result && $skills_result->num_rows > 0) {
        while ($row = $skills_result->fetch_assoc()) {
            $skills[] = $row; // Fetch full skill object for name and level
        }
    }
}


// Fetch about me content from database
$about = "No about info yet.";
// Ensure 'about' table exists before querying
if ($conn->query("SHOW TABLES LIKE 'about'")->num_rows > 0) {
    $about_result = $conn->query("SELECT content FROM about LIMIT 1");
    if ($about_result && $about_result->num_rows > 0) {
        $row = $about_result->fetch_assoc();
        $about = $row['content'];
    }
}
?>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>My Portfolio</title>
    <link rel="stylesheet" href="style.css" />
    <!-- mediaqueries.css content is now merged into style.css -->
  </head>
  <body>
    <!-- Desktop Navigation -->
    <nav id="desktop-nav">
      <div class="logo">Benjie Juabot</div>
      <div>
        <ul class="nav-links">
          <li><a href="#about">About</a></li>
          <li><a href="#experience">Experience</a></li>
          <li><a href="#projects">Projects</a></li>
          <li><a href="#contact">Contact</a></li>
        </ul>
      </div>
    </nav>

    <!-- Hamburger Navigation for Mobile -->
    <nav id="hamburger-nav">
      <div class="logo">Benjie Juabot</div>
      <div class="hamburger-menu">
        <div class="hamburger-icon" onclick="toggleMenu()">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <div class="menu-links">
          <li><a href="#about" onclick="toggleMenu()">About</a></li>
          <li><a href="#experience" onclick="toggleMenu()">Experience</a></li>
          <li><a href="#projects" onclick="toggleMenu()">Projects</a></li>
          <li><a href="#contact" onclick="toggleMenu()">Contact</a></li>
        </div>
      </div>
    </nav>

    <!-- Profile Section -->
    <section id="profile">
      <div class="section__pic-container">
        <!-- Using the uploaded image for the profile picture -->
        <img src="./assets/profile-pic.png" alt="John Doe profile picture" />
      </div>
      <div class="section__text">
        <p class="section__text__p1">Hello, I'm</p>
        <h1 class="title">Benjie Juabot</h1>
        <p class="section__text__p2">Computer Engineer</p>
        <div class="btn-container">
          <button
            class="btn btn-color-2"
            onclick="window.open('./assets/cert.pdf')"
          >
            Certificates
          </button>
          <button class="btn btn-color-1" onclick="location.href='./#contact'">
            Contact Info
          </button>
        </div>
        <div id="socials-container">
          <!-- Static social icons as per original index.html -->
          <img
            src="./assets/linkedin.png"
            alt="My LinkedIn profile"
            class="icon"
            onclick="location.href='https://www.linkedin.com/in/benjie-juabot-2a909a299/'"
          />
          <img
            src="./assets/github.png"
            alt="My Github profile"
            class="icon"
            onclick="location.href='https://github.com/'"
          />
          <img
            src="./assets/facebook.png"
            alt="My Facebook profile"
            class="icon"
            onclick="location.href='https://www.facebook.com/BJ.Juabot14'"
          />
        </div>
      </div>
    </section>

    <!-- About Section -->
    <section id="about">
      <p class="section__text__p1">Get To Know More</p>
      <h1 class="title">About Me</h1>
      <div class="section-container">
        <div class="section__pic-container">
          <!-- Assuming about-pic.png is in assets folder -->
          <img
            src="./assets/about-pic.png"
            alt="Profile picture"
            class="about-pic"
          />
        </div>
        <div class="about-details-container">
          <div class="about-containers">
            <div class="details-container">
              <!-- Assuming experience.png is in assets folder -->
              <img
                src="./assets/experience.png"
                alt="Experience icon"
                class="icon"
              />
              <h3>Experience</h3>
              <p>2+ years <br />Frontend Development</p>
            </div>
            <div class="details-container">
              <!-- Assuming education.png is in assets folder -->
              <img
                src="./assets/education.png"
                alt="Education icon"
                class="icon"
              />
              <h3>Education</h3>
              <p>B.S.CpE. Bachelors Degree<br />M.Sc. Masters Degree</p>
            </div>
          </div>
          <div class="text-container">
            <p>
              <?php echo nl2br(htmlspecialchars($about)); ?>
            </p>
          </div>
        </div>
      </div>
      <!-- Assuming arrow.png is in assets folder -->
      <img
        src="./assets/arrow.png"
        alt="Arrow icon"
        class="icon arrow"
        onclick="location.href='./#experience'"
      />
    </section>

    <!-- Experience (Skills) Section -->
    <section id="experience">
      <p class="section__text__p1">Explore My</p>
      <h1 class="title">Experience</h1>
      <div class="experience-details-container">
        <div class="about-containers">
          <div class="details-container">
            <h2 class="experience-sub-title">Skills</h2>
            <div class="article-container">
              <?php if (count($skills) > 0): ?>
                  <?php foreach ($skills as $skill): ?>
                      <article>
                        <!-- Assuming checkmark.png is in assets folder -->
                        <img
                          src="./assets/checkmark.png"
                          alt="Experience icon"
                          class="icon"
                        />
                        <div>
                          <h3><?= htmlspecialchars($skill['name']) ?></h3>
                          <p><?= htmlspecialchars($skill['level']) ?></p>
                        </div>
                      </article>
                  <?php endforeach; ?>
              <?php else: ?>
                  <p>No skills added yet.</p>
              <?php endif; ?>
            </div>
          </div>
          <!-- You can add another details-container here for a second column of skills if needed -->
          <!-- For example, if you categorize skills as Frontend/Backend -->
          <!-- <div class="details-container">
            <h2 class="experience-sub-title">Backend Development</h2>
            <div class="article-container">
              <article>
                <img src="./assets/checkmark.png" alt="Experience icon" class="icon" />
                <div>
                  <h3>Node JS</h3>
                  <p>Intermediate</p>
                </div>
              </article>
            </div>
          </div> -->
        </div>
      </div>
      <!-- Assuming arrow.png is in assets folder -->
      <img
        src="./assets/arrow.png"
        alt="Arrow icon"
        class="icon arrow"
        onclick="location.href='./#projects'"
      />
    </section>

    <!-- Projects Section -->
    <section id="projects">
      <p class="section__text__p1">Browse My Recent</p>
      <h1 class="title">Projects</h1>
      <div class="experience-details-container">
        <div class="about-containers">
          <?php if (count($projects) > 0): ?>
              <?php foreach ($projects as $project): ?>
                  <div class="details-container color-container">
                    <div class="article-container">
                      <?php if (!empty($project['image_url'])): ?>
                          <img
                            src="<?= htmlspecialchars($project['image_url']) ?>"
                            alt="<?= htmlspecialchars($project['title']) ?>"
                            class="project-img"
                            onerror="this.onerror=null;this.src='https://placehold.co/400x300/e0e0e0/000000?text=No+Image'"
                            alt="No image available" <!-- Assuming placeholder.png is a valid placeholder image -->
                          />
                      <?php else: ?>
                          <img
                            src="https://placehold.co/400x300/e0e0e0/000000?text=No+Image"
                            alt="No image available"
                            class="project-img"
                          />
                      <?php endif; ?>
                    </div>
                    <h2 class="experience-sub-title project-title"><?= htmlspecialchars($project['title']) ?></h2>
                    <p><?= htmlspecialchars($project['description']) ?></p>
                    <div class="btn-container">
                      <?php if (!empty($project['link'])): ?>
                          <button
                            class="btn btn-color-2 project-btn"
                            onclick="window.open('<?= htmlspecialchars($project['link']) ?>', '_blank')"
                          >
                            Live Demo
                          </button>
                      <?php endif; ?>
                      <!-- You can add a Github button here if your projects table includes a github_link -->
                      <!-- <button
                        class="btn btn-color-2 project-btn"
                        onclick="location.href='https://github.com/'"
                      >
                        Github
                      </button> -->
                    </div>
                  </div>
              <?php endforeach; ?>
          <?php else: ?>
              <p>No projects found.</p>
          <?php endif; ?>
        </div>
      </div>
      <!-- Assuming arrow.png is in assets folder -->
      <img
        src="./assets/arrow.png"
        alt="Arrow icon"
        class="icon arrow"
        onclick="location.href='./#contact'"
      />
    </section>

    <!-- Contact Section -->
    <section id="contact">
      <p class="section__text__p1">Get in Touch</p>
      <h1 class="title">Contact Me</h1>
      <div class="contact-info-upper-container">
        <?php if (count($contacts) > 0): ?>
            <?php foreach ($contacts as $contact): ?>
                <div class="contact-info-container">
                    <?php
                    $icon_src = '';
                    $alt_text = '';
                    $link_prefix = '';
                    switch ($contact['platform']) {
                        case 'Email':
                            $icon_src = './assets/email.png';
                            $alt_text = 'Email icon';
                            $link_prefix = 'mailto:';
                            break;
                        case 'Phone':
                            $icon_src = './assets/phone.png'; // Assuming you have a phone icon
                            $alt_text = 'Phone icon';
                            $link_prefix = 'tel:';
                            break;
                        case 'Facebook':
                            $icon_src = './assets/facebook.png'; // Assuming you have a facebook icon
                            $alt_text = 'Facebook icon';
                            break;
                        default:
                            $icon_src = './assets/link.png'; // Generic link icon
                            $alt_text = 'Link icon';
                            break;
                    }
                    ?>
                    <?php if (!empty($icon_src)): ?>
                        <img
                          src="<?= htmlspecialchars($icon_src) ?>"
                          alt="<?= htmlspecialchars($alt_text) ?>"
                          class="icon contact-icon <?= strtolower($contact['platform']) ?>-icon"
                          onerror="this.onerror=null;this.src='https://placehold.co/24x24/e0e0e0/000000?text=Icon';"
                        />
                    <?php endif; ?>
                    <p>
                        <?php if (!empty($contact['link'])): ?>
                            <a href="<?= htmlspecialchars($link_prefix . $contact['link']) ?>" target="_blank">
                                <?= htmlspecialchars($contact['link']) ?>
                            </a>
                        <?php else: ?>
                            <?= htmlspecialchars($contact['platform']) ?>: Not available
                        <?php endif; ?>
                    </p>
                </div>
            <?php endforeach; ?>
        <?php else: ?>
            <p>No contact information available.</p>
        <?php endif; ?>
      </div>
    </section>

    <!-- Footer -->
    <footer>
      <nav>
        <div class="nav-links-container">
          <ul class="nav-links">
            <li><a href="#about">About</a></li>
            <li><a href="#experience">Experience</a></li>
            <li><a href="#projects">Projects</a></li>
            <li><a href="#contact">Contact</a></li>
          </ul>
        </div>
      </nav>
      <p>Copyright &#169; 2025 Benjie Juabot. All Rights Reserved.</p>
    </footer>

    <script>
      // JavaScript for Hamburger Menu (from script.js)
      function toggleMenu() {
        const menu = document.querySelector(".menu-links");
        const icon = document.querySelector(".hamburger-icon");
        menu.classList.toggle("open");
        icon.classList.toggle("open");
      }

      // Optional: Smooth scroll for internal links (from your latest index.php, adapted)
      document.querySelectorAll('a[href^="#"]').forEach(anchor => {
          anchor.addEventListener('click', function (e) {
              e.preventDefault();
              document.querySelector(this.getAttribute('href')).scrollIntoView({
                  behavior: 'smooth'
              });
          });
      });
    </script>
  </body>
</html>
