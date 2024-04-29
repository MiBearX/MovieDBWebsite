$(document).ready(function() {
    //let shoppingCart = {};
    let shoppingCart = JSON.parse(localStorage.getItem('shoppingCart')) || {};

    function updateCartDisplay() {
        let cartTableBody = $('#cart_table tbody');
        cartTableBody.empty();
        let totalPrice = 0;

        $.each(shoppingCart, function(movieId, item) {
            let itemTotal = item.price * item.quantity;
            totalPrice += itemTotal;
            cartTableBody.append(`
                <tr>
                    <td>${item.title}</td>
                    <td>$${item.price.toFixed(2)}</td>
                    <td>
                        <button class='decrease' data-movieid='${movieId}'>-</button>
                        ${item.quantity}
                        <button class='increase' data-movieid='${movieId}'>+</button>
                    </td>
                    <td>$${itemTotal.toFixed(2)}</td>
                    <td><button class='delete' data-movieid='${movieId}'>Delete</button></td>
                </tr>
            `);
        });

        $('#total_price').text(`Total Price: $${totalPrice.toFixed(2)}`);
    }

    function adjustQuantity(movieId, delta) {
        if (shoppingCart[movieId]) {
            shoppingCart[movieId].quantity += delta;
            if (shoppingCart[movieId].quantity <= 0) {
                delete shoppingCart[movieId]; // Remove the item if quantity is 0 or less
            }
            updateCartDisplay();
        }
    }

    function removeItem(movieId) {
        if (shoppingCart[movieId]) {
            delete shoppingCart[movieId];
            updateCartDisplay();
        }
    }

    $('#proceed_to_payment').click(function() {
        window.location.href = 'payment.html'; // Redirect to Payment Page
    });

    $('#cart_table').on('click', '.increase', function() {
        let movieId = $(this).data('movieid');
        adjustQuantity(movieId, 1);
    });

    $('#cart_table').on('click', '.decrease', function() {
        let movieId = $(this).data('movieid');
        adjustQuantity(movieId, -1);
    });

    $('#cart_table').on('click', '.delete', function() {
        let movieId = $(this).data('movieid');
        removeItem(movieId);
    });

    // Simulate fetching cart items from server or initialize for demonstration
    /*shoppingCart = {
        101: { title: "Movie A", price: 10, quantity: 2 },
        102: { title: "Movie B", price: 15, quantity: 1 }
    };*/
    updateCartDisplay(); // Initial cart display update
});