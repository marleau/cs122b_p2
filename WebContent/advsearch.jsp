<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>

<% session.setAttribute("title", "Advanced Search"); %>

<%@ include file="header.jsp" %>

<h1>Advanced Search</h1>

<FORM ACTION="AdvancedSearch" METHOD="GET">
	<label>Title: </label><INPUT TYPE="TEXT" NAME="t"><BR>
	<label>Year: </label><INPUT TYPE="TEXT" NAME="y"><BR>
	<label>Director: </label><INPUT TYPE="TEXT" NAME="d"><BR>
	<label>Star's First Name: </label><INPUT TYPE="TEXT" NAME="fn"><BR>
	<label>Star's Last Name: </label><INPUT TYPE="TEXT" NAME="ln"><BR>
	<label>Substring Search: </label><INPUT TYPE="CHECKBOX" NAME="sub"><BR>
	<INPUT TYPE="HIDDEN" NAME=rpp VALUE="5">
	<INPUT TYPE="SUBMIT" VALUE="Search">
	<INPUT TYPE="RESET" VALUE="Reset"> 
</FORM>


<%@ include file="footer.jsp" %>
