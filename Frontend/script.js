
document.addEventListener("DOMContentLoaded", function () {
    let navLinks = document.querySelectorAll(".navbar-nav .nav-link");

    navLinks.forEach(link => {
        link.addEventListener("click", function () {
            navLinks.forEach(nav => nav.classList.remove("active"));
            this.classList.add("active");
        });
    });
});

function proceedToPlans() {
var mobileNumber = document.getElementById("mobileNumber").value.trim();
var mobileError = document.getElementById("mobileError");

// Reset error message
mobileError.textContent = "";

if (mobileNumber === "") {
    mobileError.textContent = "Mobile number is required.";
    return;
}

if (!/^\d{10}$/.test(mobileNumber)) {
    mobileError.textContent = "Please enter a valid 10-digit mobile number.";
    return;
}

// Store the mobile number in localStorage
localStorage.setItem("userMobileNumber", mobileNumber);

// Redirect to the available plans page with the mobile number as a query parameter
window.location.href = "../Subscriber/availableplan.html?mobile=" + encodeURIComponent(mobileNumber);
}

function clearError() {
document.getElementById("mobileError").textContent = "";
}
