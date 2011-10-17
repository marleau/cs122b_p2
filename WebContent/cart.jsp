<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>

<%@ include file="header.jsp" %>

<% Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart"); %>

<h1>Shopping Cart</h1>

<ul class="cart">
<% if (ShoppingCart.isCartEmpty(request, response)) { %>
	<li>Your cart is empty.</li>
<% } else { %>
	<form class="cart" action="cart" method="post">
	<% for (Map.Entry entry : cart.entrySet() ) { %>
		<li><label>Movie: <%= entry.getKey() %>  Quantity: </label><input class="cart" type="text" name="<%= entry.getKey() %>" value="<%= entry.getValue() %>"> <a href="cart?remove=<%= entry.getKey() %>">Remove</a></li>
	<% } %>
</ul>
	<a href="cart?clear=1">Empty cart</a>
	<input type="hidden" name="updateCart" value="1">
	<input type="submit" value="submit">
	</form>
<% } %>

<%@ include file="footer.jsp" %>