<%@ page import="Fabflix.*, java.util.*" %>

<% ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart"); %>

<h3>Shopping Cart</h3>

<% if (ShoppingCart.isCartEmpty(request, response)) { %>
	Your cart is empty.
<% } else { %>
	<ul>
		<% for (String item : cart ) { %>
			<li><%= item %> <a href="cart?remove=<%= item %>">Remove</a></li>
		<% } %>
	</ul>
	
	<a href="cart?clear=1">Empty cart</a>
<% } %>