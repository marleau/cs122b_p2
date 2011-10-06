import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;

import java.sql.*;

/**
 * Servlet implementation class MovieDetails
 */
public class MovieDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MovieDetails() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		LoginPage.kickNonUsers(request, response);//kick if not logged in

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

			// READ movieID
			Integer movieID;
			try {
				movieID = Integer.valueOf(request.getParameter("id"));
			} catch (Exception e) {
				movieID = 0;
			}

			// Declare our statement
			Statement statement = dbcon.createStatement();
			String query = "SELECT DISTINCT * FROM movies m " + "WHERE m.id ='"
					+ movieID + "'";
			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) {

				String title = rs.getString("title");
				Integer year = rs.getInt("year");
				String director = rs.getString("director");
				String bannerURL = rs.getString("banner_url");
				String trailerURL = rs.getString("trailer_url");

				out.println("<HTML><HEAD><TITLE>FabFlix -- " + title
						+ "</TITLE></HEAD><BODY>");// OPEN HTML

				out.println("<H1>FabFlix</H1>");// HEADER
				ListResults.searchTitlesBox(out);
				Logout.button(out);
				out.println("<HR>");
				//Movie Info
				out.println("<H2>" + title + " ("+year+")</H2><BR>");
				out.println("<a href=\"" + trailerURL + "\"><img src=\""
						+ bannerURL + "\"><br>Trailer</a><BR><BR>");
				out.println("ID: "+movieID + "<BR>");
				ListResults.listByYearLink(out, year);
				ListResults.listByDirectorLink(out, director);

				ListResults.listGenres(out, dbcon, movieID);

				out.println("<BR><BR>");

				ListResults.listStarsIMG(out, dbcon, movieID);

				out.println("<HR>");//Footer

				ListResults.browseGenres(out, dbcon);

				out.println("<HR>");

				ListResults.browseTitles(out);

				out.println("</BODY></HTML>");
				rs.close();
				statement.close();
				dbcon.close();
			} else {
				String title = "FabFlix -- Movie Not Found";
				out.println("<HTML><HEAD><TITLE>" + title + "</TITLE></HEAD>");
				out.println("<BODY><H1>" + title + "</H1></BODY></HTML>");
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
			out.println("<HTML>" + "<HEAD><TITLE>" + "MovieDB: Error"
					+ "</TITLE></HEAD>\n<BODY>" + "<P>SQL error in doGet: "
					+ ex.getMessage() + "<br>" + ex.toString()
					+ "</P></BODY></HTML>");
			return;
		}
		out.close();
	}

}
