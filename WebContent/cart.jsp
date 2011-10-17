<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>

<%@ include file="header.jsp" %>

<% ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart"); %>

<h1>Shopping Cart</h1>

<% if (ShoppingCart.isCartEmpty(request, response)) { %>
	Your cart is empty.
<% } else { %>
	<ul>
		<% for (String item : cart ) { %>
			<!-- TODO: display movie titles, not ids -->
			<li><%= item %> <a href="cart?remove=<%= item %>">Remove</a></li>
		<% } %>
	</ul>
	
	<a href="cart?clear=1">Empty cart</a>
<% } %>

<%@ include file="footer.jsp" %>