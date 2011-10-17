<%@page import="Fabflix.*" %>

<% ShoppingCart.initCart(request, response); %>

<%@ include file="header.jsp" %>

<h1>Please Login</h1>

<% if ( session.getAttribute("login") != null) { 
	boolean login = (Boolean) session.getAttribute("login"); 
	if (!login) { %>
		<p class="error">Your email or password is invalid.</p>
	<% }
 } %>

<form action="login" method="post">
	<label>Username</label> <input type="text" name="email" /> <br />
	<label>Password</label> <input type="password" name="password" /> <br />
	<input type="submit" value="Login">
</form>

<%@ include file="footer.jsp" %>
