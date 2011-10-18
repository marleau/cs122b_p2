<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>
<% ShoppingCart.initCart(request, response); %>

<% session.setAttribute("title", "Home"); %>

<%@ include file="header.jsp" %>

	<h1>Welcome, <%= session.getAttribute("user.name") %></h1>
	
	<% if ( ShoppingCart.isCartEmpty(request, response) ) { %>
		Your cart is empty. 
		<br>
		<a href="/project2_10/ListResults">Browse</a> for some movies.
	<% } else { %>
		Don't forget to checkout the movies in your cart!
	<% } %>

<%@ include file="footer.jsp" %>
