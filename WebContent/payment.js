$(document).ready(function() {
    $('#payment_form').submit(function(event) {
        event.preventDefault(); // Prevent the default form submission

        var expirationDate = new Date($('#expiration').val());
        if (!expirationDate) {
            console.log("Error: Invalid date format.");
            return;
        }
        var formattedExpiration = expirationDate.toISOString().substring(0, 10); // Converts the date to 'YYYY-MM-DD'

        var paymentData = {
            firstName: $('#firstName').val(),
            lastName: $('#lastName').val(),
            creditCardNumber: $('#creditCardNumber').val(),
            expiration: formattedExpiration,  // Use the correctly formatted date
            sessionusername: localStorage.getItem('sessionusername')  // Corrected to use lowercase 'localStorage'
        };

        // Check for empty required fields
        if (!paymentData.firstName || !paymentData.lastName || !paymentData.creditCardNumber || !paymentData.expiration) {
            console.log("Error: All fields must be filled.");
            return;
        }

        $.ajax({
            type: "POST",
            url: "api/payment",
            data: paymentData,
            success: function(data) {
                console.log("Server response: ", data);
                if (data.status === "success") {
                    console.log("Payment validated successfully! Redirecting...");
                    window.location.replace("mainPage.html"); // Redirect to the main page after successful validation
                } else {
                    console.log("Error: " + data.message);
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                try {
                    // Try to parse the JSON error message from the server
                    var errorData = JSON.parse(jqXHR.responseText);
                    console.log("Error processing request: " + errorData.message);
                } catch (e) {
                    // If parsing fails, use a generic error message
                    console.log("Failed to process payment. Please try again.");
                }
            },
            dataType: "json"
        });
    });
});