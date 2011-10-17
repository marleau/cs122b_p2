<%@page import="Fabflix.*" %>

<% LoginPage.kickNonUsers(request, response); %>

<%@ include file="header.jsp" %>

<h1>Please Login</h1>

<% if ( session.getAttribute("login") != null) { 
	boolean login = (Boolean) session.getAttribute("login"); 
	if (!login) { %>
		Your email or password is invalid.
	<% }
 } %>

<form action="login" method="post">
	Username: <input type="text" name="email" /> <br />
	Password: <input type="password" name="password" /> <br />
	<input type="submit" value="submit">
</form>

<%@ include file="footer.jsp" %>
