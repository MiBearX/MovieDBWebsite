$(document).ready(function() {
    $('#employee_login_form').submit(function(event) {
        event.preventDefault(); // Prevent the default form submission

        var recaptchaResponse = $('#g-recaptcha-response').val();

        /*if (!recaptchaResponse) {
            $('#error_message').text("Please complete the reCAPTCHA challenge.");
            return;
        }*/

        var formData = {
            email: $('#email').val(),
            password: $('#password').val(),
            'g-recaptcha-response': recaptchaResponse
        };

        // Store username in localStorage
        localStorage.setItem('sessionusername', formData.email);


        $.ajax({
            type: "POST",
            url: "api/dashboard", // Adjust if your servlet URL is different
            data: formData,
            success: function(data) {
                if (data != null) {
                    if (data.status === "fail") {
                        $('#error_message').text(data.message); // Show error message
                        grecaptcha.reset();
                        return;
                    }
                    $('#employee_login').hide();
                    $('#dashboard').show();
                    displayDashboard(data);
                } else {
                    $('#error_message').text(data.message); // Show error message
                    grecaptcha.reset();
                }
            },
            error: function() {
                $('#error_message').text("Failed to connect to server."); // Handle server connection error
            },
            complete: function() {
                $('#submit_button').prop('disabled', false).text('Login');
            },
            dataType: "json"
        });
    });

    $('#insertStarForm').submit(function(event) {
        event.preventDefault();
        var formData = {
            starName: $('#starName').val(),
            starYear: $('#starYear').val()
        };
        console.log("Form data works");
        $.ajax({
            type: "POST",
            url: "api/insertStar", // Adjust if your servlet URL is different
            data: formData,
            success: function(data) {
                let msg = "New star inserted successfully. ID: " + data["starID"];
                alert(msg);
            },
            error: function() {
                $('#error_message').text("Failed to insert star."); // Handle server connection error
                alert("Failed to insert star");
            },
            dataType: "json"
        });

    });

    $('#addMovieForm').submit(function(event) {
        event.preventDefault();

        var formData = {
            title: $('#title').val(),
            myear: $('#year').val(),
            director: $('#director').val(),
            starName: $('#mStarName').val(),
            starYear: $('#starBirthYear').val(),
            genre: $('#genreName').val()
        };

        $.ajax({
            type: 'POST',
            url: 'api/addMovie',
            data: formData,
            success: function(response) {
                if (response["status"] === "fail") {
                    alert(response["message"]);
                    return;
                }
                let msg = "Movie added successfully. Movie ID: " + response["movieID"];
                msg += " Star ID: " + response["starID"] + " Genre ID: " + response["genreID"];
                alert(msg);
            },
            error: function(xhr, status, error) {
                console.error(xhr.responseText);
                alert(xhr.message);
                //alert('Error: ' + xhr.responseText);
            }
        });
    });
});

function displayDashboard(data) {
    let metadataTableBodyElement = jQuery("#db_metadata_table_body");
    //var parsedJson = JSON.parse(data);
    let rowHTML = "";
    rowHTML += "<tr>";
    for (tableName in data) {
        rowHTML += "<tr>" + "<th>" + tableName + "</th>";
        if (data.hasOwnProperty(tableName)) {
            var columnArray = data[tableName];
            columnArray.forEach(function(column) {
                rowHTML += "<td>" + column.name + "</td>";
                rowHTML += "<td>" + column.type + "</td>";
            });
        }
        rowHTML += "</tr>";
    }
    metadataTableBodyElement.append(rowHTML);
}

