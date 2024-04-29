$(document).ready(function() {
    $('#payment_form').submit(function(event) {
        event.preventDefault();

        let orderDetails = JSON.parse(localStorage.getItem('orderDetails'));

        var expirationDate = new Date($('#expiration').val());
        if (!expirationDate) {
            console.log("Error: Invalid date format.");
            return;
        }
        var formattedExpiration = expirationDate.toISOString().substring(0, 10);

        var paymentData = {
            firstName: $('#firstName').val(),
            lastName: $('#lastName').val(),
            creditCardNumber: $('#creditCardNumber').val(),
            expiration: formattedExpiration,
            sessionusername: localStorage.getItem('sessionusername')
        };

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
                    console.log("Payment validated successfully! Sale ID: " + data.saleId);
                    localStorage.setItem('saleId', data.saleId);

                    if (orderDetails) {
                        orderDetails.saleId = data.saleId;

                        localStorage.setItem('orderDetails', JSON.stringify(orderDetails));
                    }

                    window.location.replace("confirmationPage.html");
                } else {
                    alert("Failed to process payment. Please try again.");
                    console.log("Error: " + data.message);
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                try {
                    alert("Failed to process payment. Please try again.");
                    var errorData = JSON.parse(jqXHR.responseText);
                    console.log("Error processing request: " + errorData.message);
                } catch (e) {
                    alert("Failed to process payment. Please try again.");
                    console.log("Failed to process payment. Please try again.");
                }
            },
            dataType: "json"
        });
    });
});