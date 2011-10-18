<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>
<% ShoppingCart.initCart(request, response); %>
<% Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart"); %>

<%@ include file="header.jsp" %>

<h1>Checkout</h1>

<% if (!(Boolean)session.getAttribute("processed")) { %>

	<div class="cart">
	<h3>Your cart</h3>
	
	<% if (session.getAttribute("updated") != null) { %>
	<p class="success">Your cart has been updated.</p>
	<% } %>
	
	<ul>
		<% if (ShoppingCart.isCartEmpty(request, response)) { %>
			<li>Your cart is empty.</li>
		<% } else { %>
			<form action="checkout" method="post">
			<% for (Map.Entry entry : cart.entrySet() ) { %>
				<li><label>Movie: <%= entry.getKey() %>  Quantity: </label><input type="text" name="<%= entry.getKey() %>" value="<%= entry.getValue() %>"> <a href="cart?remove=<%= entry.getKey() %>">Remove</a></li>
			<% } %>
		</ul>
			<a href="cart?clear=1">Empty cart</a>
			<input type="hidden" name="updateCart" value="1">
			<input type="submit" value="submit">
			</form>
		<% } %>
	
	</div>
	
	<br><br>

	<form method="post" action="checkout">
		<h3>Credit Card Information</h3>
		
		<label>First Name</label>
		<input type="text" name="firstName" />
		
		<label>Last Name</label>
		<input type="text" name="lastName" />
		
		<label>Card Number</label>
		<input type="text" name="id" />
		
		<p><b>Expiration Date</b></p>
		
		<label>Month</label>
		<input type=text" name="month" />
		
		<label>Day</label><input type=text" name="day" />
		
		<label>Year</label>
		<input type=text" name="year" />
		
		<button type="submit" value="submit">Process Order</button>
	
	</form>

<% } else { %>
	Your order has been processed.
<% } %>


<%@ include file="footer.jsp" %>