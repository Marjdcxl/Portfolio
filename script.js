function toggleMenu() {
  const menu = document.querySelector(".menu-links");
  const icon = document.querySelector(".hamburger-icon");
  menu.classList.toggle("open");
  icon.classList.toggle("open");
}

let lastScrollY = 0; // Tracks the previous scroll position
const desktopNav = document.getElementById('desktop-nav'); // Get the desktop navigation bar element
let inactivityTimeout; // Variable to hold the inactivity timer
const INACTIVITY_DELAY = 1500; // 1.5 seconds of inactivity before hiding the nav bar

// Variable to track the hover state of the desktop navigation bar
let isHoveringNav = false;

// Function to hide the desktop navigation bar
function hideDesktopNav() {
    // Get the current scroll position inside this function for robust checks
    const currentScrollPosition = window.scrollY;

    // Do NOT hide the navbar if:
    // 1. The mouse is currently hovering over it.
    // 2. The page is at the very top (scroll position is 0).
    if (isHoveringNav || currentScrollPosition === 0) {
        return; // Exit the function, preventing the navbar from being hidden
    }

    if (desktopNav) { // Ensure the element exists
        desktopNav.classList.add('navbar-hidden');
    }
}

// Function to show the desktop navigation bar
function showDesktopNav() {
    if (desktopNav) { // Ensure the element exists
        desktopNav.classList.remove('navbar-hidden');
        // When shown due to activity (scroll up or mouse enter), always reset the timer.
        // The resetInactivityTimer itself will check if hovering before setting a new timeout.
        resetInactivityTimer();
    }
}

// Function to reset (clear and restart) the inactivity timer
function resetInactivityTimer() {
    clearTimeout(inactivityTimeout); // Clear any existing timer
    // Only set a new timer if the mouse is NOT hovering over the navbar
    // AND the page is not at the very top (to prevent setting a hide timer when it should be visible)
    if (!isHoveringNav && window.scrollY !== 0) {
        inactivityTimeout = setTimeout(hideDesktopNav, INACTIVITY_DELAY); // Set a new timer to hide the nav bar
    }
}

// Ensure desktopNav exists before adding event listeners
if (desktopNav) {
    // Event listener for when the mouse enters the desktop navigation bar
    desktopNav.addEventListener('mouseenter', () => {
        isHoveringNav = true; // Set hover flag to true
        clearTimeout(inactivityTimeout); // Clear the inactivity timer to prevent hiding
        showDesktopNav(); // Ensure the nav bar is visible when hovered
    });

    // Event listener for when the mouse leaves the desktop navigation bar
    desktopNav.addEventListener('mouseleave', () => {
        isHoveringNav = false; // Set hover flag to false
        resetInactivityTimer(); // Restart the inactivity timer now that the mouse has left

        // Immediately check if the navbar should hide after the mouse leaves,
        // based on the current scroll position and direction.
        const currentScrollY = window.scrollY;
        // If currently scrolling down (or stopped after scrolling down) and not at the very top,
        // attempt to hide the navbar.
        if (currentScrollY > lastScrollY && currentScrollY > desktopNav.offsetHeight && currentScrollY !== 0) {
            hideDesktopNav(); // This call will now proceed as isHoveringNav is false
        }
    });

    // Add a scroll event listener to the window
    window.addEventListener('scroll', () => {
        const currentScrollY = window.scrollY; // Get the current vertical scroll position

        // Logic to show/hide based on scroll direction
        if (currentScrollY > lastScrollY && currentScrollY > desktopNav.offsetHeight) {
            // Scrolling down and past the initial height of the navbar
            hideDesktopNav(); // This function will now internally check isHoveringNav and if at top
        } else if (currentScrollY < lastScrollY) {
            // Scrolling up
            showDesktopNav();
        } else if (currentScrollY === 0) {
            // Always show the navbar when at the very top of the page
            showDesktopNav();
        }

        lastScrollY = currentScrollY; // Update the last scroll position
        resetInactivityTimer(); // Reset the inactivity timer on any scroll activity
    });

    // Initial call to start the inactivity timer when the page loads
    // This ensures the navbar can hide if there's no immediate scroll activity after loading,
    // unless it's at the very top.
    resetInactivityTimer();
}
