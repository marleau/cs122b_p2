<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>

<%@ include file="header.jsp" %>

<% Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart"); %>

<h1>Shopping Cart</h1>

<ul class="cart">
<% if (ShoppingCart.isCartEmpty(request, response)) { %>
	<li>Your cart is empty.</li>
<% } else { %>
	<% for (Map.Entry entry : cart.entrySet() ) { %>
		<li>Movie: <%= entry.getKey() %>  Quantity: <%= entry.getValue() %> <a href="cart?remove=<%= entry.getKey() %>">Remove</a></li>
	<% } %>
</ul>
	<a href="cart?clear=1">Empty cart</a>
<% } %>

<%@ include file="footer.jsp" %>