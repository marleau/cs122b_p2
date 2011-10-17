<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>

<% session.setAttribute("title", "Home"); %>

<%@ include file="header.jsp" %>

	<h1>Welcome, <%= session.getAttribute("user.name") %></h1>
	
	<% if ( ShoppingCart.isCartEmpty(request, response) ) { %>
		Your cart is empty. 
		<br>
		<a href="/Fabflix/browse">Browse</a> for some movies.
	<% } else { %>
		Don't forget to checkout the movies in your cart!
	<% } %>

<%@ include file="footer.jsp" %>
