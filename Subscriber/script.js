let generatedOTP;

function showErrorMessage(elementId, message) {
    let errorElement = document.getElementById(elementId);
    errorElement.innerText = message;
    errorElement.style.display = "block";
}

function clearErrorMessage(elementId) {
    let errorElement = document.getElementById(elementId);
    errorElement.innerText = "";
}

function generateRandomOTP() {
    return Math.floor(100000 + Math.random() * 900000); // Generate a 6-digit OTP
}

document.getElementById('generateOTPBtn').addEventListener('click', function () {
    var mobileNumber = document.getElementById('mobileNumber').value;

    if (/^\d{10}$/.test(mobileNumber)) { // Validate 10-digit mobile number
        clearErrorMessage('mobileError');
        document.getElementById('otp').disabled = false; // Enable OTP input
        document.getElementById('loginBtn').disabled = false; // Enable login button

        // Generate OTP
        generatedOTP = generateRandomOTP();
        document.getElementById('otpDisplay').innerText = generatedOTP; // Display OTP in modal

        // Store mobile number for reference
        localStorage.setItem("userMobileNumber", mobileNumber);

        // Show OTP modal
        var otpModal = new bootstrap.Modal(document.getElementById('otpModal'));
        otpModal.show();

        // Show success message
        document.getElementById('otpSuccess').innerText = 'OTP has been sent to ' + mobileNumber;
    } else {
        showErrorMessage('mobileError', 'Please enter a valid 10-digit mobile number.');
        document.getElementById('otpSuccess').innerText = ""; // Clear success message
    }
});

document.getElementById('loginForm').addEventListener('submit', function (event) {
    event.preventDefault();
    var otp = document.getElementById('otp').value;

    if (otp == generatedOTP) { // Validate OTP
        clearErrorMessage('otpError');
        document.getElementById('otpSuccess').innerText = "Login successful! Redirecting...";
        setTimeout(() => {
            window.location.href = 'customer-home.html'; // Redirect to dashboard
        }, 1000);
    } else {
        showErrorMessage('otpError', 'Invalid OTP. Please try again.');
    }
});

function logout() {
    const confirmation = confirm("Are you sure you want to logout?");
    if (confirmation) {
        window.location.href = 'index.html'; // Redirect to the login page
    } else {
        alert("Logout canceled.");
    }
}
function markAsRead(element) {
    element.parentElement.parentElement.style.display = "none";
}
document.addEventListener("DOMContentLoaded", function () {
    // Retrieve mobile number from localStorage
    let storedMobileNumber = localStorage.getItem("userMobileNumber");

    // Check if a mobile number exists in localStorage
    if (storedMobileNumber) {
        document.getElementById("displayMobile").innerText = storedMobileNumber;
        document.getElementById("mobileNumber").value = storedMobileNumber;
    } else {
        document.getElementById("displayMobile").innerText = "Not Available";
        document.getElementById("mobileNumber").value = "";
    }
});

function logout() {
    const confirmation = confirm("Are you sure you want to logout?");
    if (confirmation) {

        window.location.href = 'customer login.html'; // Redirect to the login page
    } else {
        alert("Logout canceled.");
    }
}

// Function to send verification email (mocked here)
function sendVerificationEmail() {
    const email = document.getElementById("email").value; .23
    alert(`Verification email sent to ${email}. Please check your inbox.`);
    document.getElementById("emailStatus").innerText = "Email verified. Thank you!";
}

function toggleMobileChange() {
    const mobileChangeForm = document.getElementById("mobileChangeForm");
    if (mobileChangeForm.style.display === "none" || mobileChangeForm.style.display === "") {
        mobileChangeForm.style.display = "block";
    } else {
        mobileChangeForm.style.display = "none";
    }
}

function updateMobileNumber() {
    const newMobileNumber = document.getElementById("newMobileNumber").value;
    if (newMobileNumber) {
        document.getElementById("mobileNumber").value = newMobileNumber;
        document.getElementById("newMobileNumber").value = '';
        alert(`Mobile number updated to: ${newMobileNumber}`);
        toggleMobileChange();
    } else {
        alert("Please enter a valid mobile number.");
    }
}

// Function to save profile changes (mocked here)
document.getElementById("profileForm").addEventListener("submit", function (event) {
    event.preventDefault(); // Prevent form from submitting to a server

    // Show Bootstrap modal instead of an alert
    var successModal = new bootstrap.Modal(document.getElementById('successModal'));
    successModal.show();
});


function goBack() {
    window.history.back(); // This takes the user to the previous page in the history stack
}

