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

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: 'api/genre',
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});