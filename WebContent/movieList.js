let currentPage = 1;
let moviesPerPage = 10;
let orderBy = "rating";
let order = "DESC";


var queryString = window.location.search;

// Parse the query string to extract parameters and their values
var urlParams = new URLSearchParams(queryString);

let genreId = urlParams.get('genreId');
let titleChar = urlParams.get('titleChar');
let movieTitle = urlParams.get("title");
let movieYear = urlParams.get("year");
let movieDirector = urlParams.get("director");
let movieStar = urlParams.get("star")

$('#sortingSelect').change(function() {
    orderBy = $(this).val();
    $("#movie_list_table_body").empty();
    fetchMovies();
});

$('#sortingOrderSelect').change(function() {
    order = $(this).val();
    $("#movie_list_table_body").empty();
    fetchMovies();
});


$('#perPageSelect').change(function() {
    moviesPerPage = parseInt($(this).val(), 10);
    currentPage = 1; // Reset to first page
    $("#movie_list_table_body").empty();
    fetchMovies();
});

$('#nextBtn').click(function() {
    currentPage++;
    $("#movie_list_table_body").empty();
    fetchMovies();
});

$('#prevBtn').click(function() {
    if (currentPage > 1) {
        currentPage--;
        $("#movie_list_table_body").empty();
        fetchMovies();
    }
});

function handleMovieListResult(resultData) {
    console.log("handleMovieListResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_list_table_body");

    let movieCount = resultData[0]["moviesPerPage"];

    for (let i = 0; i < Math.min(movieCount, resultData.length); i++) {


        // Concatenate the html tags with resultData jsonObject
        let row_HTML= "";
        row_HTML += "<tr>";
        //row_HTML += "<th>" + resultData[i]["movie_title"] + "</th>";
        row_HTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">' + resultData[i]["movie_title"] + '</a>' + "</th>";
        row_HTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        row_HTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        //row_HTML += "<th>" + resultData[i]["genres"] + "</th>";

        // Handle Genres
        let genresandId = resultData[i]["genres"].split(",")
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
        let starsAndID = resultData[i]["stars"].split(",");
        row_HTML += "<th>";
        count = 0
        for (let starIdPair of starsAndID) {
            let starIdArr = starIdPair.split(":");
            let starName = starIdArr[0];
            let starID = starIdArr[1];
            if (count !== 0) {
                row_HTML += ", ";
            }

            row_HTML += '<a href="single-star.html?id=' + starID + '">' + starName + '</a>';
            count += 1;
        }
        row_HTML += "</th>";


        row_HTML += "<th>" + resultData[i]["rating"] + "</th>";
        row_HTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(row_HTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

function fetchMovies() {
    let apiURL = 'api/movie_list?page=' + currentPage + '&limit=' + moviesPerPage + '&orderBy=' + orderBy + '&order=' + order;
    if (genreId != null) {
        apiURL += '&genreId=' + genreId;
    }
    if (titleChar != null) {
        apiURL += '&titleChar=' + titleChar;
    }
    if (movieTitle != null) {
        apiURL += '&title=' + movieTitle;
    }
    if (movieYear != null) {
        apiURL += '&year=' + movieYear;
    }
    if (movieDirector != null) {
        apiURL += '&director=' + movieDirector;
    }
    if (movieStar != null) {
        apiURL += '&star=' + movieStar;
    }

    $.ajax({
        dataType : "json",
        method: 'GET',
        url: apiURL,
        success: function(resultData) {
            handleMovieListResult(resultData);
        },
        error: function(error) {
            console.error('Error fetching movies:', error);
        }
    });
}
fetchMovies();
// Makes the HTTP GET request and registers on success callback function handleStarResult
/*jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: apiURL,
    success: (resultData) => handleMovieListResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});*/