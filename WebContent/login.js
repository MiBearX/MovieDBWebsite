$(document).ready(function() {
    $('#login_form').submit(function(event) {
        event.preventDefault(); // Prevent the default form submission

        var formData = {
            email: $('#email').val(),
            password: $('#password').val()
        };

        // Store username in localStorage
        localStorage.setItem('sessionusername', formData.email);

        var movielist = ["Movie1", "Movie2"];
        localStorage.setItem('movies', JSON.stringify(movielist));

        $.ajax({
            type: "POST",
            url: "api/login", // Adjust if your servlet URL is different
            data: formData,
            success: function(data) {
                if (data.status === "success") {
                    window.location.replace("main.html"); // Redirect to Main Page
                } else {
                    $('#error_message').text(data.message); // Show error message
                }
            },
            error: function() {
                $('#error_message').text("Failed to connect to server."); // Handle server connection error
            },
            complete: function() {
                $('#submit_button').prop('disabled', false).text('Login'); // Restore button text and state
            },
            dataType: "json"
        });
    });
});