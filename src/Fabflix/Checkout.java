package Fabflix;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
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
		// validate credit card
		if (isValid(request, response)) {
			session.setAttribute("validCC", true);
		} else {
			session.setAttribute("validCC", false);
		}
		//response.sendRedirect("/Fabflix/checkout");
		request.getRequestDispatcher("/checkout.jsp");
	}

	private boolean isValid(HttpServletRequest request, HttpServletResponse response) {
		Connection db = connectToDB();

		try {
			Statement statement = db.createStatement();
			String firstName = request.getParameter("firstName");
			String lastName = request.getParameter("lastName");
			String id = request.getParameter("id");
			String expiration = request.getParameter("expiration");
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

	private Connection connectToDB() {
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
	
	private void disconnectFromDB(Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
		}
	}
}