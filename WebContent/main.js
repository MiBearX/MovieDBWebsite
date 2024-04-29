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

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: 'api/genre',
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});