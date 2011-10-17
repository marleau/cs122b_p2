package Fabflix;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;



public class Checkout extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LoginPage.kickNonUsers(request, response);
		
		HttpSession session = request.getSession();
		session.setAttribute("title", "Checkout");
		
		if (session.getAttribute("validCC") == null)
			session.setAttribute("validCC", false);
		else {
			if ((Boolean)session.getAttribute("validCC"))
				processOrder(request, response);
		}
		
		response.sendRedirect("/Fabflix/checkout.jsp");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);// Get client session
		session.setAttribute("title", "Checkout");
		
		// validate credit card
		if (isValid(request, response)) {
			session.setAttribute("validCC", true);
			processOrder(request, response);
		} else {
			session.setAttribute("validCC", false);
		}
		
		response.sendRedirect("/Fabflix/checkout.jsp");
	}
	
	public void processOrder(HttpServletRequest request, HttpServletResponse response) {
		Connection db = connectToDB();
		
		try {
		
			//ArrayList<String> cart = (ArrayList<String>) request.getAttribute("cart");
			Map<String,Integer> cart = new HashMap<String, Integer>();
			String userID = (String) request.getAttribute("user.id");
			Date date = new Date();
			DateFormat yearFormat = new SimpleDateFormat("yyyy");
			DateFormat monthFormat = new SimpleDateFormat("MM");
			DateFormat dayFormat = new SimpleDateFormat("dd");
			String curDate = yearFormat.format(date) + "-" + monthFormat.format(date) + "-" + dayFormat.format(date);
			
			for (Map.Entry<String, Integer> entry : cart.entrySet()) {
				String movieID = entry.getKey();
				String query = "INSERT INTO sales (customer_id, movie_id, sales_date) VALUES ('" + userID + "', '" + movieID + "', '" + curDate + "');";
				Statement st = db.createStatement();
				st.executeUpdate(query);
			}
			
			cart.clear();
		
		} catch (SQLException e) {
		}
		
		disconnectFromDB(db);
	}

	public static boolean isValid(HttpServletRequest request, HttpServletResponse response) {
		Connection db = connectToDB();

		try {
			Statement statement = db.createStatement();
			String firstName = request.getParameter("firstName");
			String lastName = request.getParameter("lastName");
			String id = request.getParameter("id");
			String expiration = request.getParameter("year") + "-" + request.getParameter("month") + "-" + request.getParameter("day");
			String query = "SELECT * FROM creditcards WHERE first_name='" + firstName + "' AND last_name='" + lastName + "' AND id='" + id + "' AND expiration='" + expiration + "';";
			ResultSet result;
			result = statement.executeQuery(query);
			disconnectFromDB(db);
			return result.next();
		} catch (SQLException e) {
		}
		disconnectFromDB(db);
		return false;
	}

	private static Connection connectToDB() {
		try {
			// Open context for mySQL pooling
			Context initCtx = new InitialContext();

			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
				System.out.println("envCtx is NULL");

			// Look up our data source in context.xml
			DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

			if (ds == null)
				System.out.println("ds is null.");

			Connection dbcon = ds.getConnection();
			if (dbcon == null)
				System.out.println("dbcon is null.");
			// connection is now open

			return dbcon;

		} catch (SQLException ex) {
			System.out.println("MovieDB: Error");
			while (ex != null) {
				System.out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			}
		} catch (java.lang.Exception ex) {
			System.out.println("MovieDB: Error\nSQL error in doGet: " + ex.getMessage() + "\n" + ex.toString());
		}
		return null;
	}
	
	private static void disconnectFromDB(Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
		}
	}
}