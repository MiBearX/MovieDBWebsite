$(document).ready(function() {
    $('#login_form').submit(function(event) {
        event.preventDefault(); // Prevent the default form submission

        console.log("Form submission initiated...");

        // Update UI to indicate processing
        $('#submit_button').prop('disabled', true).text('Logging in...');

        var formData = {
            email: $('#email').val(),
            password: $('#password').val()
        };

        $.ajax({
            type: "POST",
            url: "api/login", // Adjust if your servlet URL is different
            data: formData,
            success: function(data) {
                console.log("AJAX request successful.");
                if (data.status === "success") {
                    console.log("Login successful, redirecting...");
                    window.location.replace("mainPage.html"); // Redirect to Main Page
                } else {
                    console.log("Login failed: " + data.message);
                    $('#error_message').text(data.message); // Show error message
                }
            },
            error: function() {
                console.log("AJAX request failed.");
                $('#error_message').text("Failed to connect to server."); // Handle server connection error
            },
            complete: function() {
                $('#submit_button').prop('disabled', false).text('Login'); // Restore button text and state
            },
            dataType: "json"
        });
    });
});
