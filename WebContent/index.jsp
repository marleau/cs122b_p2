<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>

<%@ include file="header.jsp" %>

<div class="content">

	<h1>Welcome, <%= session.getAttribute("user.name") %></h1>
	
	<% if ( ShoppingCart.isCartEmpty(request, response) ) { %>
		Your cart is empty. Get shopping!
		<!-- SHOW SOME RANDOM MOVIES -->
		<a href="" onclick="window.open('/Fabflix/cart?add=Elektra');">Add to Cart</a>
	<% } else { %>
		<a href="" onclick="window.open('/Fabflix/cart?clear=1');">Empty Cart</a>
	<%} %>

</div>

<%@ include file="footer.jsp" %>
