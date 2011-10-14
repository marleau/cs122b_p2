<%@ page import="Fabflix.*, java.util.*" %>

<%@ include file="header.jsp" %>

<div class="content">

<h1>Checkout</h1>

<ul>
<% if (ShoppingCart.isCartEmpty(request, response)) { %>
	<li>Your cart is empty.</li>
<% } else { %>

	<% for ( String item : (ArrayList<String>) session.getAttribute("cart") ) { %>
		<li><%= item %></li>
	<% } %>
<% } %>

</ul>

</div>

<%@ include file="footer.jsp" %>