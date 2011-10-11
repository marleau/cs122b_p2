package Fabflix;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Servlet implementation class LoginPage
 */
public class LoginPage extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginPage() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(true);// Get client session

		String email = request.getParameter("email");
		String password = request.getParameter("password");

		if (!validUser(request, email, password)) {
			out.println("<HTML><HEAD><TITLE>Login Failed</TITLE></HEAD>");
			out.println("<BODY>Your email and password are invalid.<BR>");
			out.println("<A HREF=\"/Fabflix/\">try again</A>");
			out.println("</BODY></HTML>");
		} else {
			session = request.getSession();
			session.setAttribute("user.login", email);
			try {
				String target = (String) session.getAttribute("user.dest");
				// retrieve address if user goes to a page w/o logging in
				if (target != null) {
					session.removeAttribute("user.dest");
					// redirect to page the user was originally trying to go to
					response.sendRedirect(target);
					return;
				}
			} catch (Exception ignored) {
			}

			// Couldn't redirect to the target. Redirect to the site's homepage.
			response.sendRedirect("/Fabflix/ListResults");
//			response.sendRedirect("/Fabflix/Home");
			// TODO Go to home page once designed

		}

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		HttpSession session = request.getSession();// Get client session
//		String user = (String) session.getAttribute("user.login");
//		// Check login
//		if (user == null ) {// FIXME
			response.sendRedirect("/Fabflix/login.jsp");
//		} else {
//			PrintWriter out = response.getWriter();
//			out.println("You are already logged in.");
//		}
	}

	private boolean validUser(HttpServletRequest request, String email, String password) {
		// Validate user
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
			
			HttpSession session = request.getSession();// Get client session

			

			Statement statement = dbcon.createStatement();
			String query = "SELECT * FROM customers c WHERE email = '" + email + "' AND password = '" + password + "'";

			ResultSet rs = statement.executeQuery(query);
			if (rs.next()) {// IF person exists with that password
				session.setAttribute("user.name", rs.getString("first_name") + " " + rs.getString("last_name"));
				session.setAttribute("user.id", rs.getString("id"));
				return true;// then log in
			}

		} catch (SQLException ex) {
			System.out.println("<HTML><HEAD><TITLE>MovieDB: Error</TITLE></HEAD><BODY>");
			while (ex != null) {
				System.out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} // end while
			System.out.println("</BODY></HTML>");
		} // end catch SQLException
		catch (java.lang.Exception ex) {
			System.out.println("<HTML>" + "<HEAD><TITLE>" + "MovieDB: Error" + "</TITLE></HEAD>\n<BODY>" + "<P>SQL error in doGet: " + ex.getMessage() + "<br>"
					+ ex.toString() + "</P></BODY></HTML>");
		}

		return false;
	}

	public static void kickNonUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Validate user
		HttpSession session = request.getSession();// Get client session

		String user = (String) session.getAttribute("user.login");
		// Check login
		if (user == null) {
			String URL = request.getRequestURL().toString();
			String qs = request.getQueryString();
			if (qs != null) {
				URL += "?" + qs;
			}
			// Save destination till after logged in
			session.setAttribute("user.dest", URL);
			// send to login page if not logged in
			response.sendRedirect("/Fabflix/LoginPage");
		}
	}
}
