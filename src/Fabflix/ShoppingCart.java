package Fabflix;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ShoppingCart extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// get cart
		HttpSession session = request.getSession();
		ArrayList<String> cart = (ArrayList<String>)session.getAttribute("cart");
		
		session.setAttribute("title", "Shopping Cart");
		
		// if no cart, create one
		if (cart == null)
			initCart(request, response);
		
		// add an item to the cart
		if (request.getParameter("add") != null)
			addItem(request, response);
		
		// empty the cart
		if ( request.getParameter("clear") != null )
			clearCart(request, response);
		
		// remove an item
		if ( request.getParameter("remove") != null )
			removeItem(request, response);
		
		if ( request.getParameter("stopgap") == null )
			response.sendRedirect("/Fabflix/cart.jsp");
		
	}
	
	public void addItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart");

		String newItem = request.getParameter("add");
		
		synchronized(cart) {
			if (newItem != null && !cart.contains(newItem))
				cart.add(newItem);
		}
	}
	
	public void removeItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart");
		String item = request.getParameter("remove");
		synchronized(cart) {
			cart.remove(item);
		}
	}
	
	public static void clearCart(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart");
		cart.clear();
	}
	
	public static void initCart(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		ArrayList<String> cart;
		
		synchronized(session) {
			cart = (ArrayList<String>) session.getAttribute("cart");
			// if no cart, make new
			if (cart == null) {
				cart = new ArrayList<String>();
				session.setAttribute("cart", cart);
			}
		}
	}
	
	public static boolean isCartEmpty(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart");	
		if (cart == null)
			initCart(request, response);
		return cart.isEmpty();
	}
	
}
