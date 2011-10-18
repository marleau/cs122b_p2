<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>

<%@ include file="header.jsp" %>

<% Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart"); %>

<h1>Shopping Cart</h1>

<% if (session.getAttribute("updated") != null) { %>
<p class="success">Your cart has been updated.</p>
<% } %>

<div class="cart">
	
<ul class="cart">
<% if (ShoppingCart.isCartEmpty(request, response)) { %>
	<li>Your cart is empty.</li>
<% } else { %>
	<form class="cart" action="cart" method="post"><!--
		<li><ul class="item"><li class="first">Movie Title</li><li>Quantity</li><li>Remove?</li></ul></li>
	--><% for (Map.Entry entry : cart.entrySet() ) { %>
		<li><!--
			<%= entry.getKey() %><label>Quantity: </label><input class="cart" type="text" name="<%= entry.getKey() %>" value="<%= entry.getValue() %>"> <a href="cart?remove=<%= entry.getKey() %>">Remove</a>
			--><ul class="item">
				<li class="first"><a href="MovieDetails?id=<%= entry.getKey() %>"><%= ShoppingCart.getMovieTitle(request, response, (String)entry.getKey()) %></a></li>
				<li><label>Quantity</label><input class="qty" type="text" name="<%= entry.getKey() %>" value="<%= entry.getValue() %>"></li>
				<li><a href="cart?remove=<%= entry.getKey() %>">Remove</a></li>
			</ul>
		</li>
	<% } %>
</ul>
	<br><br>
	<div style="clear: both;"></div>
	<a style="float: right; margin: 10px;" href="cart?clear=1">Empty cart</a>
	<input style="margin: 10px;" type="hidden" name="updateCart" value="1">
	<input style="margin: 10px;" type="submit" value="Update">
	</form>
<% } %>
</div>

<%@ include file="footer.jsp" %>
