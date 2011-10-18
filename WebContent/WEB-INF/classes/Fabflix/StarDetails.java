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
 * Servlet implementation class StarDetails
 */
public class StarDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StarDetails() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginPage.kickNonUsers(request, response);// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			Context initCtx = new InitialContext();
			if (initCtx == null)
				out.println("initCtx is NULL");

			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
				out.println("envCtx is NULL");

			// Look up our data source
			DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

			if (ds == null)
				out.println("ds is null.");

			Connection dbcon = ds.getConnection();
			if (dbcon == null)
				out.println("dbcon is null.");

			// READ STAR ID
			Integer starID;
			try {
				starID = Integer.valueOf(request.getParameter("id"));
			} catch (Exception e) {
				starID = 0;
			}

			// Declare our statement
			Statement statement = dbcon.createStatement();
			String query = "SELECT DISTINCT * FROM stars WHERE id ='" + starID + "'";

			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) {// Get star if ID exists
				String starName = rs.getString("first_name") + " " + rs.getString("last_name");
				String starIMG = rs.getString("photo_url");
				String dob = rs.getString("dob");

//				out.println("<HTML><HEAD><TITLE>FabFlix -- " + starName + "</TITLE></HEAD><BODY>");// OPEN
																									// HTML

				HttpSession session = request.getSession();
				session.setAttribute("title", starName);
//				ListResults.header(request, out, 0);
				out.println(ListResults.header(session));

				// Star Details
				out.println("<H1>" + starName + "</H1><BR>" + "<img src=\"" + starIMG + "\">" + "<BR>");
				out.println("ID: " + starID + "<BR>");// STAR DETAILS
				out.println("Date of Birth: " + dob + "<BR><BR>");

				ListResults.listMoviesIMG(out, dbcon, starID);

				out.println("<HR>");// Footer

				ListResults.footer(out, dbcon, 0);

				out.println("</BODY></HTML>");
				rs.close();
				statement.close();
				dbcon.close();
			} else {// starID didn't return a star
				String title = "FabFlix -- Star Not Found";
				out.println("<HTML><HEAD><TITLE>" + title + "</TITLE></HEAD>");
				ListResults.header(request, out, 0);
				out.println("<BODY><H1>" + title + "</H1></BODY></HTML>");
				ListResults.footer(out, dbcon, 0);
			}
		} catch (SQLException ex) {
			out.println("<HTML><HEAD><TITLE>MovieDB: Error</TITLE></HEAD><BODY>");
			while (ex != null) {
				out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} // end while
			out.println("</BODY></HTML>");
		} // end catch SQLException
		catch (java.lang.Exception ex) {
			out.println("<HTML>" + "<HEAD><TITLE>" + "MovieDB: Error" + "</TITLE></HEAD>\n<BODY>" + "<P>SQL error in doGet: " + ex.getMessage() + "<br>"
					+ ex.toString() + "</P></BODY></HTML>");
			return;
		}
		out.close();
	}

}
