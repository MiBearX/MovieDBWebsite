$('#shoppingCartBtn').click(function() {
    window.location.href = 'cart.html';
});

$('#searchForm').submit(function(event) {
    event.preventDefault(); // Prevent default form submission

    // Get values from form inputs
    let title = $('#title').val();
    let year = $('#year').val();
    let director = $('#director').val();
    let star = $('#star').val();
    let url = "index.html?"

    if (title) {
        url += "title=" + title + "&";
    }
    if (year) {
        url += "year=" + year + "&";
    }
    if (director) {
        url += "director=" + director + "&";
    }
    if (star) {
        url += "star=" + star + "&";
    }

    url = url.slice(0, -1);
    window.location.href = url;
});


function handleGenreResult(resultData) {
    let characters = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*';
    let genreList = jQuery("#genreList");
    genreList.empty();
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<li>";
        rowHTML += '<a href="index.html?genreId=' + resultData[i]["genreId"] + '">' + resultData[i]["genreName"] + '</a>';
        rowHTML += "</li>";
        genreList.append(rowHTML);
    }
    let titleList = jQuery("#titleList");
    for (let character of characters) {
        let titleListElem = "<li>" + '<a href="index.html?titleChar=' + character + '">' + character + '</a>' + "</li>";
        titleList.append(titleListElem);
    }
}


function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    // TODO: if you want to check past query results first, you can do it here
    let cachedResult = localStorage.getItem(query);

    if (cachedResult) {
        cachedResult = JSON.parse(cachedResult);
        console.log("Found suggestions in cache")
        console.log(cachedResult)
        doneCallback( { suggestions: cachedResult } );
    } else {
        console.log("Sending request to backend")

        // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
        // with the query data
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "api/autocomplete?query=" + escape(query),
            dataType: 'json',
            "success": function (data) {
                // pass the data, query, and doneCallback function into the success handler
                localStorage.setItem(query, JSON.stringify(data));
                handleLookupAjaxSuccess(data, query, doneCallback)
            },
            "error": function (errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}


function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")
    console.log(data)

    // parse the string into JSON
    //var jsonData = JSON.parse(data);
    //console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}

function handleSelectSuggestion(suggestion) {
    console.log(suggestion)
    let movieTitle = suggestion.data;
    let movieId = suggestion.data.movieId;
    console.log(movieId)
    let url = "single-movie.html?id=" + movieId
    window.location.href = url;
}


$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        if (query.length >= 3) {
            handleLookup(query, doneCallback)
        }

    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});

$(document).ready(function() {
    $('#fullTextForm').submit(function () {
        event.preventDefault();
        console.log("Submitted autocomplete text bar")
        let titleQuery = $('#autocomplete').val();
        console.log(titleQuery)
        let movieListURL = "index.html?titleQuery=" + titleQuery;
        window.location.href = movieListURL;
    })
})

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: 'api/genre',
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});