<%@ page import="java.util.*" %>

<%@ include file="header.jsp" %>

<h3>Some vital stats on your session:</h3>

<ul style="list-style-type: none;">
    <li>Session id: <%= session.getId()%> <i>(keep it secret)</i></li>
    <li>New session? <%= session.isNew() %></li>
    <li>Timeout: <%= session.getMaxInactiveInterval() %> (<%= session.getMaxInactiveInterval() / 60 %>)</li>
    <li>Creation time: <%= session.getCreationTime() %> (<%= new Date(session.getCreationTime()) %>)</li>
    <li>Last access time: <%= session.getLastAccessedTime() %> (<%= new Date(session.getLastAccessedTime()) %>)</li>
    <li>Requested session ID from cookie: <%= request.isRequestedSessionIdFromCookie() %></li>
    <li>Requested session ID from URL: <%= request.isRequestedSessionIdFromURL() %></li>
    <li>Requested session ID valid? <%= request.isRequestedSessionIdValid() %></li>
</ul>

<br>

<h3>Session Attributes:</h3>

<ul>
<%
Enumeration e = session.getAttributeNames();

while (e.hasMoreElements()) {
    String name = (String) e.nextElement();
    out.println("   <li>" + name + ": " + session.getAttribute(name) + "</li>");
}
%>
</ul>

<%@ include file="footer.jsp" %>