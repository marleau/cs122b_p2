<%@page import="Fabflix.*" %>

<%@ include file="header.html" %>

<H1>Please Login</H1><HR>

<%= new java.util.Date() %> <br/ ><br />

<form action="login" method="post">
	Please enter your username <input type="text" name="email" /> <br />
	Please enter your password <input type="password" name="password" /> <br />
	<input type="submit" value="submit">
</form>

<%@ include file="footer.html" %>
