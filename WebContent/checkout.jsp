<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>
<% ShoppingCart.initCart(request, response); %>
<% Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart"); %>

<%@ include file="header.jsp" %>

<h1>Checkout</h1>

<% if (!(Boolean)session.getAttribute("processed")) { %>

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

<div class="ccinfo">
	
	<br><br>

	<form method="post" action="checkout">
		<h3>Credit Card Information</h3>
		
		<% if (session.getAttribute("ccError") != null) { %>
			<p class="error">Your credit card information is not valid.</p>
		<% } %>
		
		<label>First Name</label><input type="text" name="firstName" />
		
		<br>
		
		<label>Last Name</label><input type="text" name="lastName" />
		
		<br>
		
		<label>Card Number</label><input type="text" name="id" />
		
		<br>
		
		<p><b>Expiration Date</b></p>
		
		<label>Month</label><input type=text" name="month" />
		
		<br>
		
		<label>Day</label><input type=text" name="day" />
		
		<br>
		
		<label>Year</label><input type=text" name="year" />
		
		<br>
		
		<button type="submit" value="submit">Process Order</button>
	
	</form>
</div>

<% } else { %>
	<p class="success">Your order has been processed.</p>
<% } %>


<%@ include file="footer.jsp" %>
