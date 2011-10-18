package Fabflix;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

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
		
//		if ( request.getParameter("stopgap") == null )
//			response.sendRedirect("/Fabflix/cart.jsp");
		
		if (request.getParameter("updateCart") != null) {
			updateCart(request, response);	
			session.setAttribute("updated", 1);
		} else
			session.removeAttribute("updated");
		
		response.sendRedirect("/Fabflix/cart.jsp");
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}
	
	public static String getMovieTitle(HttpServletRequest request, HttpServletResponse response, String movieID) {
		
		String title = null;
		
		try {
		
		Context initCtx = new InitialContext();
			if (initCtx == null)
				System.out.println("initCtx is NULL");

			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
				System.out.println("envCtx is NULL");

			// Look up our data source
			DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

			if (ds == null)
				System.out.println("ds is null.");

			Connection dbcon = ds.getConnection();
			if (dbcon == null)
				System.out.println("dbcon is null.");
			
			// Declare our statement
			Statement statement = dbcon.createStatement();
			String query = "SELECT DISTINCT * FROM movies m " + "WHERE m.id ='" + movieID + "'";
			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) 
				return rs.getString("title");
			
			
		}
		catch (SQLException ex) {}
		catch (java.lang.Exception ex) {} 
		return title;
	}
	
	public static void updateCart(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
		ArrayList<String> zero = new ArrayList<String>();
		int qty;
		synchronized(cart) {
			for (Map.Entry<String, Integer> entry : cart.entrySet()) {
				qty = Integer.valueOf(request.getParameter(entry.getKey())) ;
				if (qty > 0)
					cart.put(entry.getKey(), qty);
				else
					zero.add(entry.getKey());
			}
			
			for (String movieID : zero) {
				cart.remove(movieID);
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
