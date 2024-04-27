function handleGenreResult(resultData) {
    let genreList = jQuery("#genreList");
    genreList.empty();
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<li>";
        rowHTML += '<a href="index.html?genreId=' + resultData[i]["genreId"] + '">' + resultData[i]["genreName"] + '</a>';
        rowHTML += "</li>";
        genreList.append(rowHTML);
    }
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: 'api/genre',
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});