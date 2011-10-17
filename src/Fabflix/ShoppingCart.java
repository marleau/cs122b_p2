package Fabflix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ShoppingCart extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// get cart
		HttpSession session = request.getSession();
		Map<String, Integer> cart = (Map<String, Integer>)session.getAttribute("cart");
		
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
		
		if (request.getParameter("updateCart") != null) {
			updateCart(request, response);	
			session.setAttribute("updated", 1);
		} else
			session.removeAttribute("updated");
		
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}
	
	public static void updateCart(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
		int qty;
		synchronized(cart) {
			for (Map.Entry<String, Integer> entry : cart.entrySet()) {
				qty = Integer.valueOf(request.getParameter(entry.getKey())) ;
				if (qty > 0)
					cart.put(entry.getKey(), qty);
				else
					cart.remove(entry.getKey());
			}
		}
	}
	
	public void addItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");

		String newItem = request.getParameter("add");
		
		synchronized(cart) {
			if (newItem != null)
				//cart.add(newItem);
				if (!cart.containsKey(newItem))
					cart.put(newItem, 1);
				else
					cart.put(newItem, cart.get(newItem) + 1);
		}
	}
	
	public void removeItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
		String item = request.getParameter("remove");
		synchronized(cart) {
			cart.remove(item);
		}
	}
	
	public static void clearCart(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
		cart.clear();
	}
	
	public static void initCart(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		Map<String, Integer> cart;
		
		synchronized(session) {
			cart = (Map<String, Integer>) session.getAttribute("cart");
			// if no cart, make new
			if (cart == null) {
				cart = new HashMap<String, Integer>();
				session.setAttribute("cart", cart);
			}
		}
	}
	
	public static boolean isCartEmpty(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");	
		if (cart == null)
			initCart(request, response);
		return cart.isEmpty();
	}
	
}
