import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class listResults
 */
public class ListResults extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ListResults() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

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
			//connection is now open

			String searchBy = request.getParameter("by");//title,letter,genre,year,director
			String arg = request.getParameter("arg");
			String order = request.getParameter("order");
			Integer page;
			Integer resultsPerPage;
		
			try {
				if (!(searchBy.equals("title") 
						|| searchBy.equals("letter")
						|| searchBy.equals("genre") 
						|| searchBy.equals("year") 
						|| searchBy.equals("director"))) {
					searchBy = "title";
				}// TODO check for empty search and give assorted lists to browse
			} catch (NullPointerException e) {
				searchBy = "title";
			}
			
			if (arg == null){
				arg = " ";
			}
			
			//===SORT
			try{
				if (order.equals("t_d")) {
					order = "ORDER BY title DESC";
				} else if (order.equals("y_d")) {
					order = "ORDER BY year DESC";
				} else if (order.equals("y_a")) {
					order = "ORDER BY year";
				} else {
					order = "ORDER BY title"; // DEFAULT to title ascending
				}
			}catch (NullPointerException e){
				order = "ORDER BY title";
			}
			
			//===Paging
			try{
				page = Integer.valueOf(request.getParameter("page"));
			} catch (NumberFormatException e){
				page=1;
			}catch (NullPointerException e){
				page=1;
			}
			
			//===Reasults per page
			try{
				resultsPerPage = Integer.valueOf(request.getParameter("rpp"));
			} catch (NumberFormatException e){
				resultsPerPage=5;
			}catch (NullPointerException e){
				resultsPerPage=5;
			}
			
			int listStart;
			if (page>0){
				listStart = (page - 1) * resultsPerPage;
			}else{
				listStart = 0;
				page = 1;
			}
			
			

			// Declare our statement
			Statement statement = dbcon.createStatement();
			String query;
			if (searchBy.equals("genre")) {
				query = "SELECT DISTINCT * FROM movies m, genres_in_movies g, genres g1 "
						+ "WHERE g.movie_id=m.id "
						+ "AND g.genre_id=g1.id "
						+ "AND name = '" + arg + "' "+order+" LIMIT "+ listStart + "," +resultsPerPage;
			} else if (searchBy.equals("letter")) {
				query = "SELECT DISTINCT * FROM movies m WHERE title REGEXP '^" + arg +"' "+order+" LIMIT "+ listStart + "," +resultsPerPage;
			} else if (searchBy.equals("title")) {
				query = "SELECT DISTINCT * FROM movies m WHERE title REGEXP '" + arg +"' "+order+" LIMIT "+ listStart + "," +resultsPerPage;
			} else {
				query = "SELECT DISTINCT * FROM movies m WHERE " + searchBy
						+ " = '" + arg + "' "+order+" LIMIT "+ listStart + "," +resultsPerPage;
			}
			ResultSet rs = statement.executeQuery(query);
			
			
			
			out.println("<HTML><HEAD><TITLE>Fabflix -- Search by "+searchBy+": "+arg+"</TITLE></HEAD>");
			out.println("<BODY><H1>Search by "+searchBy+": "+arg+"</H1><br>");
			
			
			
			
			//TODO sorting and results per page options
			out.println("Sort by: Title" +
					"(<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+page+"&rpp="+resultsPerPage+"&order=t_a\">asc</a>)" +
					"(<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+page+"&rpp="+resultsPerPage+"&order=t_d\">des</a>) " +
							"Year" +
							"(<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+page+"&rpp="+resultsPerPage+"&order=y_a\">asc</a>)" +
							"(<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+page+"&rpp="+resultsPerPage+"&order=y_d\">des</a>)<br>");
			

			
			
			//TODO determine a way to find the total number of results/pages
			out.println("Page: "+page+" ("+resultsPerPage+" results per page)<br><br>");
			
			
			
			while (rs.next()) {//For each movie
				String movieID = rs.getString("id");
				String title = rs.getString("title");
				String year = rs.getString("year");
				String bannerURL = rs.getString("banner_url");
				String director = rs.getString("director");
				out.println("<a href=\"MovieDetails?id=" + movieID + "\"><h2>"
						+ title + " (" + year + ")</h2><img src=\"" + bannerURL
						+ "\"></a><br>Year: <a href=\"ListResults?by=year&arg="+year+"\">" + year + "</a>" +
								"<br>Director: <a href=\"ListResults?by=director&arg="+director+"\">" + director + "</a>");
				

				//===GENRES
				out.println("<br>Genre: ");
				statement = dbcon.createStatement();
				ResultSet genres = statement.executeQuery("SELECT DISTINCT name " +
						"FROM movies m, genres_in_movies g, genres g1 " +
						"WHERE g.movie_id=m.id " +
						"AND g.genre_id=g1.id " +
						"AND m.id ='" + movieID+"'");
				if(genres.next()){
					String genre = genres.getString("name").trim();
					out.println("<a href=\"ListResults?by=genre&arg="+genre+"\">" + genre + "</a>");
					while (genres.next()){
						genre = genres.getString("name").trim();
						out.println(", <a href=\"ListResults?by=genre&arg="+genre+"\">" + genre + "</a>");			
					}
				}
				
				//===STARS
				out.println("<br>Stars: ");
				statement = dbcon.createStatement();
				ResultSet stars = statement.executeQuery("SELECT * FROM movies m, stars_in_movies s, stars s1 " +
						"WHERE s.movie_id=m.id " +
						"AND s.star_id=s1.id " +
						"AND m.id = '" + movieID+"'");
				if (stars.next()){
					String starName = stars.getString("first_name") +" " + stars.getString("last_name");
					String starID = stars.getString("star_id");
					out.println("<a href=\"StarDetails?id="+starID+"\">" + starName + "</a>");
					while (stars.next()){
						starName = stars.getString("first_name") +" " + stars.getString("last_name");
						starID = stars.getString("star_id");
						out.println(", <a href=\"StarDetails?id="+starID+"\">" + starName + "</a>");			
					}
				}
				
				
				out.println("<hr>");
			}

			//TODO paging
			
			
			//TODO categories
			


			out.println("</BODY></HTML>");
			rs.close();
			statement.close();
			dbcon.close();
		} catch (SQLException ex) {
			while (ex != null) {
				System.out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} // end while
		} // end catch SQLException

		catch (java.lang.Exception ex) {
			out.println("<HTML>" + "<HEAD><TITLE>" + "MovieDB: Error"
					+ "</TITLE></HEAD>\n<BODY>" + "<P>SQL error in doGet: "
					+ ex.getMessage() + "<br>"+ex.toString()+"</P></BODY></HTML>");
			return;
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
