<%@ page import="Fabflix.*, java.util.*" %>

<% LoginPage.kickNonUsers(request, response); %>
<% Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart"); %>

<%@ include file="header.jsp" %>

<h1>Checkout</h1>

<% if (!(Boolean)session.getAttribute("validCC")) { %>

	<h3>Your cart</h3>
	
	<ul class="cart">
	<% if (ShoppingCart.isCartEmpty(request, response)) { %>
		<li>Your cart is empty.</li>
	<% } else { %>
		<% for (Map.Entry<String, Integer> entry : cart.entrySet() ) { %>
			<li>Movie: <%= entry.getKey() %>  Quantity: <%= entry.getValue() %> <a href="cart?remove=<%= entry.getKey() %>">Remove</a></li>
		<% } %>
	</ul>
	<a href="cart?clear=1">Empty cart</a>
	<% } %>
	
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
		
		<label>Day</label>
		<input type=text" name="day" />
		
		<label>Year</label>
		<input type=text" name="year" />
		
		<button type="submit">Process Order</button>
	
	</form>

<% } else { %>
	Your order has been processed.
<% } %>


<%@ include file="footer.jsp" %>