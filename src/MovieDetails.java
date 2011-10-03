import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

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

			String movieID = request.getParameter("id");
			// Declare our statement			
			Statement statement = dbcon.createStatement();
			String query = "SELECT * FROM stars s, stars_in_movies si, movies m "
					+ "WHERE si.star_id=s.id "
					+ "AND si.movie_id=m.id "
					+ "AND m.id ='" + movieID+"' ORDER BY last_name";

			ResultSet rs = statement.executeQuery(query);
			
			

			if (rs.next()) {

				String title = rs.getString("title");
				Integer year = rs.getInt("year");
				String director = rs.getString("director");
				String bannerURL = rs.getString("banner_url");
				String trailerURL = rs.getString("trailer_url");

				out.println("<HTML><HEAD><TITLE>FabFlix -- " + title + "</TITLE></HEAD>");
				out.println("<BODY><H1>" + title + "</H1><br>" + "<a href=\""
						+ trailerURL + "\"><img src=\"" + bannerURL + "\">"
						+ "<br>Trailer</a><br><br>" + "Year: <a href=\"ListResults?by=year&arg=" + year +"\">"+ year+ "</a><br>"
						+ "Director: <a href=\"ListResults?by=director&arg="+director+"\">"+ director + "</a>");
				
				//===Genres
				out.println("<br>Genre: ");
				statement = dbcon.createStatement();
				ResultSet genres = statement.executeQuery("SELECT DISTINCT name " +
						"FROM movies m, genres_in_movies g, genres g1 " +
						"WHERE g.movie_id=m.id " +
						"AND g.genre_id=g1.id " +
						"AND m.id ='" + movieID+"'");
				if (genres.next()){
					String genre = genres.getString("name").trim();
					out.println("<a href=\"ListResults?by=genre&arg="+genre+"\">" + genre + "</a>");
					while (genres.next()){
						genre = genres.getString("name").trim();
						out.println(", <a href=\"ListResults?by=genre&arg="+genre+"\">" + genre + "</a>");			
					}
				}
				
				
				out.println("<br><br>Stars:<br>");
				
				do {
					String starName = rs.getString("first_name") + " " + rs.getString("last_name");
					String starIMG = rs.getString("photo_url");
					Integer starID = rs.getInt("star_id");
					
					out.println("<a href=\"StarDetails?id="
							+ starID + "\"><img src=\"" + starIMG + "\">"
							+ starName +"</a><br><br>");
					
				} while (rs.next());
				out.println("</BODY></HTML>");
	              rs.close();
	              statement.close();
	              dbcon.close();
			} else {
				String title = "FabFlix -- Movie Not Found";
				out.println("<HTML><HEAD><TITLE>" + title + "</TITLE></HEAD>");
				out.println("<BODY><H1>" + title + "</H1></BODY></HTML>");
			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);// post same as get
	}

}
