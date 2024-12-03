// JavaScript for adding products to the cart
let cartCount = 0;
const cartCountElem = document.querySelector('.badge');
const addToCartButtons = document.querySelectorAll('.btn-dark');

// Update cart count on button click
addToCartButtons.forEach(button => {
    button.addEventListener('click', () => {
        cartCount++;
        cartCountElem.textContent = cartCount;
    });
});
