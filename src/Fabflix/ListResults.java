package Fabflix;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		LoginPage.kickNonUsers(request, response);// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {

			// Open context for mySQL pooling
			Context initCtx = new InitialContext();
			if (initCtx == null)
				out.println("initCtx is NULL");

			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
				out.println("envCtx is NULL");

			// Look up our data source in context.xml
			DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

			if (ds == null)
				out.println("ds is null.");

			Connection dbcon = ds.getConnection();
			if (dbcon == null)
				out.println("dbcon is null.");
			// connection is now open

			String searchBy = request.getParameter("by");// title,letter,genre,year,director
			String arg = request.getParameter("arg");// search string
			String order = request.getParameter("order");// t_a,t_d,y_a,y_d
			Integer page;
			Integer resultsPerPage;

			// ===Search By
			try {
				if (!(searchBy.equals("title") || searchBy.equals("letter") || searchBy.equals("genre") || searchBy.equals("year") || searchBy.equals("director") || searchBy.equals("first_name") || searchBy.equals("last_name"))) {
					searchBy = "title";
				}
			} catch (NullPointerException e) {
				searchBy = "title";
			}

			// ===Argument value
			if (arg == null) {
				arg = "";
			}
			if (searchBy.equals("letter")) {
				if (arg.isEmpty()) {
					arg = " ";
				} else {
					arg = arg.substring(0, 1);// Only take first character for
					// character search
				}
			}
			if (searchBy.equals("title")) {
				try {
					Pattern.compile(arg);
				} catch (PatternSyntaxException exception) {
					arg = "";
				} 
			}

			// ===SORT
			String sortBy = "";
			try {
				if (order.equals("t_d")) {
					sortBy = "ORDER BY title DESC";
				} else if (order.equals("y_d")) {
					sortBy = "ORDER BY year DESC";
				} else if (order.equals("y_a")) {
					sortBy = "ORDER BY year";
				} else {
					sortBy = "ORDER BY title"; // DEFAULT to title ascending
					order = "t_a";
				}
			} catch (NullPointerException e) {
				sortBy = "ORDER BY title"; // DEFAULT to title ascending
				order = "t_a";
			}

			// ===Paging
			try {
				page = Integer.valueOf(request.getParameter("page"));
				if (page < 1) {
					page = 1;
				}
			} catch (NumberFormatException e) {
				page = 1;
			} catch (NullPointerException e) {
				page = 1;
			}

			// ===Results per page
			try {
				resultsPerPage = Integer.valueOf(request.getParameter("rpp"));
				if (resultsPerPage < 1) {
					resultsPerPage = 5;
				}
			} catch (NumberFormatException e) {
				resultsPerPage = 5;
			} catch (NullPointerException e) {
				resultsPerPage = 5;
			}

			int listStart;
			if (page > 0) {
				listStart = (page - 1) * resultsPerPage;
			} else {
				listStart = 0;
				page = 1;
			}

			// Declare our statement
			Statement statement = dbcon.createStatement();
			Statement fullStatement = dbcon.createStatement();
			String query;
			String fullQuery;// full search to count results
			if (arg.isEmpty()) {
				query = "SELECT DISTINCT * FROM movies " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT * FROM movies) AS results";
			} else if (searchBy.equals("genre")) {
				query = "SELECT DISTINCT * FROM movies m, genres_in_movies g, genres g1 " + "WHERE g.movie_id=m.id " + "AND g.genre_id=g1.id " + "AND name = '"
						+ arg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m, genres_in_movies g, genres g1 " + "WHERE g.movie_id=m.id "
						+ "AND g.genre_id=g1.id " + "AND name = '" + arg + "') as results";
			} else if (searchBy.equals("letter")) {
				query = "SELECT DISTINCT * FROM movies m WHERE title REGEXP '^" + arg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '^" + arg + "') as results";
			} else if (searchBy.equals("title")) {
				query = "SELECT DISTINCT * FROM movies m WHERE title REGEXP '" + arg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '" + arg + "') as results";
			} else if (searchBy.equals("first_name") || searchBy.equals("last_name")) {
				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m, stars_in_movies s, stars s1 WHERE s.movie_id=m.id AND s.star_id=s1.id AND "+searchBy+" = '"+ arg +"' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m, stars_in_movies s, stars s1 WHERE s.movie_id=m.id AND s.star_id=s1.id AND "+searchBy+" = '"+ arg +"') as results";
			} else {
				query = "SELECT DISTINCT * FROM movies m WHERE " + searchBy + " = '" + arg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE " + searchBy + " = '" + arg + "') as results";
			}

			// Get results for this page's display
			ResultSet searchResults = statement.executeQuery(query);

			// Find total number of results
			ResultSet fullCount = fullStatement.executeQuery(fullQuery);
			fullCount.next();
			int numberOfResults = fullCount.getInt(1);
			int numberOfPages = numberOfResults / resultsPerPage + (numberOfResults % resultsPerPage == 0 ? 0 : 1);

			// Adjust page if beyond scope of the results; redirect to last page
			// of search
			if (numberOfResults > 0 && page > numberOfPages) {
				response.sendRedirect("ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + numberOfPages + "&rpp="
						+ resultsPerPage + "&order=" + order);
			}

			// ===Start Writing Page===========================================

			// TITLE
			out.println("<HTML><HEAD><TITLE>FabFlix -- Search by " + searchBy + ": " + arg + "</TITLE></HEAD><BODY>");
			// BODY

			header(request, out, resultsPerPage);

			out.println("<H2>Search by " + searchBy + ": " + arg + "</H2>");

			out.println("<BR>");

			if (numberOfResults > 0) {// if results exist
				out.println("( " + numberOfResults + " Results )");
				showRppOptions(out, searchBy, arg, order, page, resultsPerPage);
				out.println("<BR><BR>");
				if (numberOfPages > 1) {
					showPageControls(out, searchBy, arg, order, page, resultsPerPage, numberOfPages);
					out.println("<BR><BR>");
				}
				showSortOptions(out, searchBy, arg, order, page, resultsPerPage);
			}

			while (searchResults.next()) {// For each movie, DISPLAY INFORMATION
				Integer movieID;
				try {
					movieID = Integer.valueOf(searchResults.getString("id"));
				} catch (Exception e) {
					movieID = 0;
				}
				String title = searchResults.getString("title");
				Integer year = searchResults.getInt("year");
				String bannerURL = searchResults.getString("banner_url");
				String director = searchResults.getString("director");

				out.println("<a href=\"MovieDetails?id=" + movieID + "\"><h2>" + title + " (" + year + ")</h2><img src=\"" + bannerURL + "\"></a><BR>");
				out.println("ID: <a href=\"MovieDetails?id=" + movieID + "\">" + movieID + "</a><BR>");
				listByYearLink(out, year,resultsPerPage);
				listByDirectorLink(out, director,resultsPerPage);

				out.println("<BR>");

				listGenres(out, dbcon, resultsPerPage, movieID);

				out.println("<BR>");

				listStars(out, dbcon, resultsPerPage, movieID);

				out.println("<HR>");
			}

			if (numberOfResults > 0) {
				// show prev/next
				if (numberOfPages > 1) {
					showPageControls(out, searchBy, arg, order, page, resultsPerPage, numberOfPages);
				}
				out.println("<BR>");

				// Results per page Options
				showRppOptions(out, searchBy, arg, order, page, resultsPerPage);

				out.println("<BR><HR>");

			} else {
				out.println("<H3>No Results.</H3><hr>");
			}

			footer(out, dbcon, resultsPerPage);

			searchResults.close();
			statement.close();
			fullStatement.close();
			dbcon.close();

			out.println("</BODY></HTML>");

		} catch (SQLException ex) {
			out.println("<HTML><HEAD><TITLE>MovieDB: Error</TITLE></HEAD><BODY>");
			while (ex != null) {
				out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} // end while
			out.println("</BODY></HTML>");
		} // end catch SQLException
		catch (java.lang.Exception ex) {
			out.println("<HTML><HEAD><TITLE>MovieDB: Error</TITLE></HEAD><BODY><P>SQL error in doGet: " + ex.getMessage() + "<br>" + ex.toString()
					+ "</P></BODY></HTML>");
			return;
		}
		out.close();
	}

	public static void footer(PrintWriter out, Connection dbcon, Integer resultsPerPage) throws SQLException {
		browseGenres(out, dbcon, resultsPerPage);

		out.println("<HR>");

		browseTitles(out, resultsPerPage);
	}

	public static void header(HttpServletRequest request, PrintWriter out, Integer resultsPerPage) {
//		out.println("<a href=\"/Fabflix/ListResults\"><H1>FabFlix</H1></a>");
		// Fabflix link home
		out.println("<a href=\"/Fabflix/Home\"><H1>FabFlix</H1></a>");
		HttpSession session = request.getSession();
		ListResults.searchTitlesBox(out, resultsPerPage);
		out.println("Welcome, "+session.getAttribute("user.name") + "! ");
		Logout.button(out);
		out.println("<HR>");
	}

	public static void listByYearLink(PrintWriter out, Integer year) {
		listByYearLink(out, year, 0);
	}

	public static void listByYearLink(PrintWriter out, Integer year, Integer rpp) {
		out.println("Year: <a href=\"ListResults?by=year&arg=" + year + "&rpp=" + rpp + "\">" + year + "</a><BR>");
	}

	public static void listByDirectorLink(PrintWriter out, String director) throws UnsupportedEncodingException {
		listByDirectorLink(out, director, 0);
	}

	public static void listByDirectorLink(PrintWriter out, String director, Integer rpp) throws UnsupportedEncodingException {
		out.println("Director: <a href=\"ListResults?by=director&arg=" + java.net.URLEncoder.encode(director, "UTF-8") + "&rpp=" + rpp + "\">" + director
				+ "</a>");
	}

	private void showPageControls(PrintWriter out, String searchBy, String arg, String order, Integer page, Integer resultsPerPage, Integer numberOfPages)
			throws UnsupportedEncodingException {
		// ===Paging

		if (page != 1) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=1&rpp=" + resultsPerPage
					+ "&order=" + order + "\">First</a>");
		} else {
			out.println("Last");
		}

		out.println(" | ");

		if (page > 1) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + (page - 1) + "&rpp="
					+ resultsPerPage + "&order=" + order + "\">Prev</a>");
		} else {
			out.println("Prev");
		}

		out.println("| Page: " + page + " of " + numberOfPages + " |");

		if (page >= numberOfPages) {
			out.println("Next");
		} else {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + (page + 1) + "&rpp="
					+ resultsPerPage + "&order=" + order + "\">Next</a>");
		}

		out.println(" | ");

		if (page < numberOfPages) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + numberOfPages + "&rpp="
					+ resultsPerPage + "&order=" + order + "\">Last</a>");
		} else {
			out.println("Last");
		}
	}

	private void showSortOptions(PrintWriter out, String searchBy, String arg, String order, Integer page, Integer resultsPerPage)
			throws UnsupportedEncodingException {
		// sorting and results per page options
		out.println("Sort by: Title(");

		if (!order.equals("t_a")) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp="
					+ resultsPerPage + "&order=t_a\">asc</a>");
		} else {
			out.println("asc");
		}

		out.println(")(");

		if (!order.equals("t_d")) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp="
					+ resultsPerPage + "&order=t_d\">des</a>");
		} else {
			out.println("des");
		}

		out.println(") Year(");

		if (!order.equals("y_a")) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp="
					+ resultsPerPage + "&order=y_a\">asc</a>");
		} else {
			out.println("asc");
		}

		out.println(")(");

		if (!order.equals("y_d")) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp="
					+ resultsPerPage + "&order=y_d\">des</a>");
		} else {
			out.println("des");
		}

		out.println(")");
	}

	public static void searchTitlesBox(PrintWriter out) {
		searchTitlesBox(out, 0);
	}

	public static void searchTitlesBox(PrintWriter out, Integer resultsPerPage) {
		// ===Search Box
		out.println("<FORM ACTION=\"ListResults\" METHOD=\"GET\">  Search Titles (RegEx): <INPUT TYPE=\"TEXT\" NAME=\"arg\">"
				+ "<INPUT TYPE=\"HIDDEN\" NAME=rpp VALUE=\"" + resultsPerPage + "\"><INPUT TYPE=\"SUBMIT\" VALUE=\"Search\">");
		AdvancedSearch.advancedSearchButton(out);
		out.println("</FORM>");
	}

	private void showRppOptions(PrintWriter out, String searchBy, String arg, String order, Integer page, Integer resultsPerPage)
			throws UnsupportedEncodingException {
		// ===Results per page
		out.println("Results per page: ");

		if (!(resultsPerPage == 5)) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=5&order=" + order
					+ "\">5</a>");
		} else {
			out.println("5");
		}

		if (!(resultsPerPage == 25)) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=25&order="
					+ order + "\">25</a>");
		} else {
			out.println("25");
		}

		if (!(resultsPerPage == 100)) {
			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=100&order="
					+ order + "\">100</a>");
		} else {
			out.println("100");
		}
	}

	public static void browseGenres(PrintWriter out, Connection dbcon) throws SQLException {
		browseGenres(out, dbcon, 0);// Default results per page
	}

	public static void browseGenres(PrintWriter out, Connection dbcon, Integer resultsPerPage) throws SQLException {
		Statement statement = dbcon.createStatement();
		// ===GENRE browser
		out.println("Browse Genres: <BR>");
		int col = 0; // fix width of display
		ResultSet allGenre = statement.executeQuery("SELECT DISTINCT name FROM genres ORDER BY name");
		if (allGenre.next()) {
			String genreName = allGenre.getString("name");
			col += genreName.length();
			out.println("<a href=\"ListResults?by=genre&arg=" + genreName + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
			while (allGenre.next()) {
				genreName = allGenre.getString("name");
				col += genreName.length();
				out.println(" | <a href=\"ListResults?by=genre&arg=" + genreName + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
				if (col >= 75 && allGenre.next()) { // column character width
					genreName = allGenre.getString("name");
					out.println("<br><a href=\"ListResults?by=genre&arg=" + genreName + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
					col = genreName.length();
				}// 10 items per row
			}
		}
		allGenre.close();
		statement.close();
	}

	public static void browseTitles(PrintWriter out) {
		browseTitles(out, 0);// Default results per page
	}

	public static void browseTitles(PrintWriter out, Integer resultsPerPage) {
		// ===Letter Browser
		out.println("Browse Titles: <BR>");
		String alphaNum = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i = 0; i < alphaNum.length(); i++) {
			if (i != 0) {
				out.println("-");
			}
			out.println("<a href=\"ListResults?by=letter&arg=" + alphaNum.charAt(i) + "&page=1&rpp=" + resultsPerPage + "\">" + alphaNum.charAt(i) + "</a>");
		}
	}

	public static void listStars(PrintWriter out, Connection dbcon, Integer movieID) throws SQLException {
		listStars(out, dbcon, 0, movieID);// Default results per page
	}

	public static void listStars(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException {
		Statement statement = dbcon.createStatement();
		// ===STARS; comma separated list
		out.println("Stars: ");
		ResultSet stars = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 " + "WHERE s.movie_id=m.id "
				+ "AND s.star_id=s1.id " + "AND m.id = '" + movieID + "' ORDER BY last_name");
		if (stars.next()) {
			String starName = stars.getString("first_name") + " " + stars.getString("last_name");
			String starID = stars.getString("star_id");
			out.println("<a href=\"StarDetails?id=" + starID + "\">" + starName + "</a>");
			while (stars.next()) {
				starName = stars.getString("first_name") + " " + stars.getString("last_name");
				starID = stars.getString("star_id");
				out.println(", <a href=\"StarDetails?id=" + starID + "\">" + starName + "</a>");
			}
		}
		stars.close();
		statement.close();
	}

	public static void listStarsIMG(PrintWriter out, Connection dbcon, Integer movieID) throws SQLException {
		listStarsIMG(out, dbcon, 0, movieID);
	}

	public static void listStarsIMG(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException {
		Statement statement = dbcon.createStatement();
		// ===STARS; list of images
		out.println("Stars: <BR><BR>");
		ResultSet stars = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 " + "WHERE s.movie_id=m.id "
				+ "AND s.star_id=s1.id " + "AND m.id = '" + movieID + "' ORDER BY last_name");
		while (stars.next()) {
			String starName = stars.getString("first_name") + " " + stars.getString("last_name");
			String starIMG = stars.getString("photo_url");
			String starID = stars.getString("star_id");
			out.println("<a href=\"StarDetails?id=" + starID + "\">" + "<img src=\"" + starIMG + "\">" + starName + "</a><BR><BR>");
		}
		stars.close();
		statement.close();
	}

	public static void listMoviesIMG(PrintWriter out, Connection dbcon, Integer starID) throws SQLException {
		listMoviesIMG(out, dbcon, 0, starID);
	}

	public static void listMoviesIMG(PrintWriter out, Connection dbcon, Integer rpp, Integer starID) throws SQLException {
		Statement statement = dbcon.createStatement();
		out.println("Starred in:<BR><BR>");
		ResultSet movies = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 " + "WHERE s.movie_id=m.id "
				+ "AND s.star_id=s1.id " + "AND s1.id = '" + starID + "' ORDER BY year DESC");

		while (movies.next()) {
			String title = movies.getString("title");
			Integer year = movies.getInt("year");
			Integer movieID = movies.getInt("movie_id");
			String bannerURL = movies.getString("banner_url");

			out.println("<a href=\"MovieDetails?id=" + movieID + "\"><img src=\"" + bannerURL + "\">" + title + " (" + year + ")" + "</a><BR><BR>");
		}

	}

	public static void listGenres(PrintWriter out, Connection dbcon, Integer movieID) throws SQLException {
		listGenres(out, dbcon, 0, movieID);// Default results per page
	}

	public static void listGenres(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException {
		// ===GENRES; comma separated list
		out.println("Genre: ");
		Statement statement = dbcon.createStatement();
		ResultSet genres = statement.executeQuery("SELECT DISTINCT name " + "FROM movies m, genres_in_movies g, genres g1 " + "WHERE g.movie_id=m.id "
				+ "AND g.genre_id=g1.id " + "AND m.id ='" + movieID + "' ORDER BY name");
		if (genres.next()) {
			String genre = genres.getString("name").trim();
			out.println("<a href=\"ListResults?by=genre&arg=" + genre + "&rpp=" + rpp + "\">" + genre + "</a>");
			while (genres.next()) {
				genre = genres.getString("name").trim();
				out.println(", <a href=\"ListResults?by=genre&arg=" + genre + "&rpp=" + rpp + "\">" + genre + "</a>");
			}
		}
		genres.close();
		statement.close();
	}

}
