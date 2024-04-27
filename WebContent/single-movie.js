function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleSingleMovieResult(resultData) {
    console.log("handleSingleMovieResult: populating single movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#single_movie_table_body");

    // Concatenate the html tags with resultData jsonObject
    let row_HTML= "";
    row_HTML += "<tr>";
    //row_HTML += "<th>" + resultData[i]["movie_title"] + "</th>";
    row_HTML += "<th>" + '<a href="single-movie.html?id=' + resultData[0]["movie_id"] + '">' + resultData[0]["movie_title"] + '</a>' + "</th>";
    row_HTML += "<th>" + resultData[0]["movie_year"] + "</th>";
    row_HTML += "<th>" + resultData[0]["movie_director"] + "</th>";

    // Handle Genres
    let genresandId = resultData[0]["genres"].split(",");
    row_HTML += "<th>";
    let count = 0;
    for (let genrePair of genresandId) {
        let genreIdArr = genrePair.split(":");
        let genreName = genreIdArr[0];
        let genreId = genreIdArr[1];
        if (count !== 0) {
            row_HTML += ", ";
        }
        row_HTML += '<a href="index.html?genreId=' + genreId + '">' + genreName + '</a>';
        count += 1;
    }
    row_HTML += "</th>";

    // Handle Stars
    let starsAndID = resultData[0]["stars"].split(",");
    row_HTML += "<th>";
    count = 0
    for (let starIdPair of starsAndID) {
        let starIdArr = starIdPair.split(":");
        let starName = starIdArr[0];
        let starID = starIdArr[1];
        if (count !== 0) {
            row_HTML += ", ";
        }

        row_HTML += '<a href="single-star.html?id=' + starID + '">' + starName + '</a>'
        count += 1;
    }
    row_HTML += "</th>";


    row_HTML += "<th>" + resultData[0]["rating"] + "</th>";
    row_HTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(row_HTML);

}

let movieId = getParameterByName('id');

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSingleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});