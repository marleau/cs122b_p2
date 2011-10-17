<%@ page import="Fabflix.*, java.util.*" %>

<%@ include file="header.jsp" %>

<h1>Checkout</h1>

<ul>
<% if (ShoppingCart.isCartEmpty(request, response)) { %>
	<li>Your cart is empty.</li>
<% } else { %>

	<% for ( String item : (ArrayList<String>) session.getAttribute("cart") ) { %>
		<li><%= item %> <a href="" onClick="cart?remove=<%= item %>">Remove</a></li>
	<% } %>
<% } %>

</ul>

<%@ include file="footer.jsp" %>