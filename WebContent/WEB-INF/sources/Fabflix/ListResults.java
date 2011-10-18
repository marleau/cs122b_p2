package Fabflix;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

	public ListResults() {
		super();
	}
	
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
				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT * FROM movies) AS results";
			} else if (searchBy.equals("genre")) {
				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m LEFT OUTER JOIN genres_in_movies g ON g.movie_id=m.id LEFT OUTER JOIN genres gr ON g.genre_id=gr.id WHERE name = '"
						+ arg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m LEFT OUTER JOIN genres_in_movies g ON g.movie_id=m.id LEFT OUTER JOIN genres gr ON g.genre_id=gr.id WHERE name = '" + arg + "') as results";
			} else if (searchBy.equals("letter")) {
				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE title REGEXP '^" + arg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '^" + arg + "') as results";
			} else if (searchBy.equals("title")) {
				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE title REGEXP '" + arg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '" + arg + "') as results";
			} else if (searchBy.equals("first_name") || searchBy.equals("last_name")) {
				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE "+searchBy+" = '"+ arg +"' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE "+searchBy+" = '"+ arg +"') as results";
			} else {
				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE " + searchBy + " = '" + arg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
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

			HttpSession session = request.getSession();
			session.setAttribute("title", "Search by " + searchBy + ": " + arg);
			
			
//			out.println("<HTML><HEAD><TITLE>FabFlix -- Search by " + searchBy + ": " + arg + "</TITLE></HEAD><BODY>");
			out.println(header(session));
			// BODY

//			header(request, out, resultsPerPage);

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
				out.println("<BR>");
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

				out.println("<BR><a href=\"MovieDetails?id=" + movieID + "\"><h2>" + title + " (" + year + ")</h2><img src=\"" + bannerURL + "\"></a><BR><BR>");

				addToCart(out, movieID);
				
				out.println("<BR><BR>ID: <a href=\"MovieDetails?id=" + movieID + "\">" + movieID + "</a><BR>");
				listByYearLink(out, year,resultsPerPage);

				out.println("<BR>");
				
				listByDirectorLink(out, director,resultsPerPage);

				out.println("<BR>");

				listGenres(out, dbcon, resultsPerPage, movieID);

				out.println("<BR>");

				listStars(out, dbcon, resultsPerPage, movieID);
				
//				String target = (String) session.getAttribute("user.dest");


				out.println("<BR><BR><HR>");
			}

			if (numberOfResults > 0) {
				// show prev/next
				if (numberOfPages > 1) {
					showPageControls(out, searchBy, arg, order, page, resultsPerPage, numberOfPages);
					out.println("<BR>");
				}

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

			out.close();

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

	public static void addToCart(PrintWriter out, Integer movieID) {
		out.println("<a href=\"cart?add="+movieID+"\">Add to Cart</a>");
	}

	public static void footer(PrintWriter out, Connection dbcon, Integer resultsPerPage) throws SQLException, UnsupportedEncodingException {
		browseGenres(out, dbcon, resultsPerPage);

		out.println("<HR>");

		browseTitles(out, resultsPerPage);
		
		out.println("		</div>	</body></html>");
	}

	public static void header(HttpServletRequest request, PrintWriter out, Integer resultsPerPage) {
//		out.println("<a href=\"/project2_10/ListResults\"><H1>FabFlix</H1></a>");
		// Fabflix link home
		out.println("<a href=\"/project2_10/Home\"><H1>FabFlix</H1></a>");
		HttpSession session = request.getSession();
		ListResults.searchTitlesBox(out, resultsPerPage);
		out.println("Welcome, "+session.getAttribute("user.name") + "! ");
		Logout.button(out);
		out.println("<HR>");
	}
	
	public static String header(HttpSession session){
		String rtn = "<!DOCTYPE html>" +
				"		<html>" +
				"			<head>" +
//				"		        <!--<base href=\"${pagecontext.request.contextpath}\" />-->" +
				"		        <title>Fabflix - "+ session.getAttribute("title")+"</title>" +
				"			</head>" +
				"			<body>" +
				"					<style>" +
//				"				<%@ include file=\"css/style.css\" %>" +
				style() +
				"			</style>" +
//				"						<%@ include file=\"menu.jsp\" %>" +
				"<div class=\"menu\">	<ul class=\"main\">		<li class=\"first\"><a href=\"/project2_10\" class=\"first\">Fabflix</a></li>		<li><a href=\"/project2_10/ListResults\">Browse</a></li>		<li><FORM ACTION=\"ListResults\" METHOD=\"GET\">				<INPUT TYPE=\"TEXT\" NAME=\"arg\">				<INPUT TYPE=\"HIDDEN\" NAME=rpp VALUE=\"5\">				<input TYPE=\"SUBMIT\" VALUE=\"Search Movies\">			</FORM>		</li>		<li class=\"last\"><a href=\"AdvancedSearch\">Advanced Search</a></li>		<li><a href=\"/project2_10/cart\">View Cart</a></li>		<li><a href=\"/project2_10/checkout\">Check out</a></li>		<li><a href=\"/project2_10/logout\">Logout</a></li>	</ul></div>" +
				"			<div class=\"content\">";
		return rtn;
	}
	
	public static String style(){
		return "/* NORMALIZE */* {	margin: 0;	padding: 0;}ul {	list-style-type: none;}/* GLOBAL */body {    font-family: Helvetica;    font-size: 16 px;    color: #666666;}/* MENU */div.menu {	width: 100%;	height: 40 px;	background-color: #333333;	color: #eeeeee;	overflow: hidden;}div.menu a {	display: block;	text-decoration: none;	padding: 10px;	color: #999999;}div.menu a:hover {	color: #ffffff;}div.menu a.first {	color: #1e9184;}div.menu ul {	list-style-type: none;}div.menu ul li {	padding: 10 px;	background-color: #333333;}div.menu ul.main {	height: 39px;}div.menu ul.main li {	float: left;	display: inline;	border-right: 1px solid #999999;}div.menu li.first {	padding-right: 100px;}div.menu li.last {	float: right;	border: 0;}div.menu form {	padding: 10px;}div.menu input {}div.menu button {}/*div.menu ul.sub {	float: right;	background-color: #333333;}div.menu ul.sub li {	float: left;	display: inline;} *//* CONTENT */div.content {	clear: both;	padding: 20px;	line-height: 150%;}div.content form {	width:400px;}/*div.content label{	display:block;	text-align:right;	width:140px;	float:left;}div.content input{	float:left;	font-size:12px;	padding:4px 2px;	border:solid 1px #95e1d8;	width:200px;	margin:2px 0 20px 10px;}div.content button{	clear:both;	float: left;	margin-left:150px;	width:125px;	height:31px;	background:#666666;	border: 0;	text-align:center;	line-height:31px;	color:#FFFFFF;}*/h1, h2, h3 {	margin-bottom: 15px;}ul.cart {	margin-left: 20px;}p {	margin-bottom: 10px;}.error {	margin: 15px;	text-align: center;	width: 400px;	padding: 10px;	background: #fdd5d3;	border: 1px solid #f26a63;}.success {	margin: 15px;	text-align: center;	width: 400px;	padding: 10px;	background: #d4fcd9;	border: 1px solid #6af263;}div.cart {	/* border: 1px dotted green; */	width: 600px;}div.cart form {	width: 600px;}div.cart label {	margin-right: 10px;}div.cart input {	float: right;}div.cart input.qty {	width: 20px;	float: none;}div.cart li {	float: left;	display: inline;	padding-left: 10px;}div.cart li.first {	width: 250px;	padding-left: 0 px;}div.content a {	color: #1e9184;	text-decoration: none;	font-weight: bold;}div.content a:hover {	color: #f6b546;	text-decoration: underline;}hr {	height: 1px;	background: #1e9184;	border: 0;	margin: 20px 0px;}div.ccinfo label, div.ccinfo input {	margin: 10px;}";
	}

	public static void listByYearLink(PrintWriter out, Integer year) {
		listByYearLink(out, year, 0);
	}

	public static void listByYearLink(PrintWriter out, Integer year, Integer rpp) {
		out.println("Year: <a href=\"ListResults?by=year&arg=" + year + "&rpp=" + rpp + "\">" + year + "</a>");
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

	public static void browseGenres(PrintWriter out, Connection dbcon) throws SQLException, UnsupportedEncodingException {
		browseGenres(out, dbcon, 0);// Default results per page
	}

	public static void browseGenres(PrintWriter out, Connection dbcon, Integer resultsPerPage) throws SQLException, UnsupportedEncodingException {
		Statement statement = dbcon.createStatement();
		// ===GENRE browser
		out.println("Browse Genres: <BR>");
		int col = 0; // fix width of display
		ResultSet allGenre = statement.executeQuery("SELECT DISTINCT name FROM genres g, genres_in_movies gi WHERE gi.genre_id=g.id ORDER BY name");
		if (allGenre.next()) {
			String genreName = allGenre.getString("name");
			col += genreName.length();
			out.println("<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
			while (allGenre.next()) {
				genreName = allGenre.getString("name");
				col += genreName.length();
				out.println(" | <a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
				if (col >= 75 && allGenre.next()) { // column character width
					genreName = allGenre.getString("name");
					out.println("<br><a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
					col = genreName.length();
				}// 10 items per row
			}
		}
		allGenre.close();
		statement.close();
	}

	public static void browseTitles(PrintWriter out) throws UnsupportedEncodingException {
		browseTitles(out, 0);// Default results per page
	}

	public static void browseTitles(PrintWriter out, Integer resultsPerPage) throws UnsupportedEncodingException {
		// ===Letter Browser
		out.println(browseTitles(resultsPerPage));
	}
	
	public static String browseTitles(Integer resultsPerPage) throws UnsupportedEncodingException{
		String rtn = "Browse Titles: <BR>";
		String alphaNum = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i = 0; i < alphaNum.length(); i++) {
			if (i != 0) {
				rtn += " - ";
			}
			rtn += "<a href=\"ListResults?by=letter&arg=" + java.net.URLEncoder.encode(alphaNum.substring(i,i+1), "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + alphaNum.charAt(i) + "</a>";
		}
		return rtn;
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
		ResultSet stars = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 WHERE s.movie_id=m.id AND s.star_id=s1.id AND m.id = '" + movieID + "' ORDER BY last_name");
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

			out.println("<a href=\"MovieDetails?id=" + movieID + "\"><img src=\"" + bannerURL + "\">" + title + " (" + year + ")" + "</a> (");
			ListResults.addToCart(out, movieID);
			out.println(")<BR><BR>");
		}

	}

	public static void listGenres(PrintWriter out, Connection dbcon, Integer movieID) throws SQLException, UnsupportedEncodingException {
		listGenres(out, dbcon, 0, movieID);// Default results per page
	}

	public static void listGenres(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException, UnsupportedEncodingException {
		// ===GENRES; comma separated list
		out.println("Genre: ");
		Statement statement = dbcon.createStatement();
		ResultSet genres = statement.executeQuery("SELECT DISTINCT name FROM movies m, genres_in_movies g, genres g1 WHERE g.movie_id=m.id AND g.genre_id=g1.id AND m.id ='" + movieID + "' ORDER BY name");
		if (genres.next()) {
			String genre = genres.getString("name").trim();
			out.println("<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genre, "UTF-8") + "&rpp=" + rpp + "\">" + genre + "</a>");
			while (genres.next()) {
				genre = genres.getString("name").trim();
				out.println(", <a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genre, "UTF-8") + "&rpp=" + rpp + "\">" + genre + "</a>");
			}
		}
		genres.close();
		statement.close();
	}

}
