<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>
<% session.setAttribute("title", "Home"); %>

<%@ include file="header.jsp" %>

<div class="content">

	<h1>Welcome, <%= session.getAttribute("user.name") %></h1>
	
	<% if ( ShoppingCart.isCartEmpty(request, response) ) { %>
		Your cart is empty. Get shopping!
		<!-- TODO: SHOW SOME RANDOM MOVIES -->
	<% } else { %>
		
	<% } %>

</div>

<%@ include file="footer.jsp" %>
