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
		
			//===Search By
			try {
				if (!(searchBy.equals("title") 
						|| searchBy.equals("letter")
						|| searchBy.equals("genre") 
						|| searchBy.equals("year") 
						|| searchBy.equals("director"))) {
					searchBy = "title";
				}
			} catch (NullPointerException e) {
				searchBy = "title";
			}
			
			//===Argument value
			if (arg == null || arg.isEmpty()){
				arg = " ";
			}
			
			//===SORT
			String sortBy="";
			try{
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
			}catch (NullPointerException e){
				sortBy = "ORDER BY title"; // DEFAULT to title ascending
				order = "t_a";
			}
			
			//===Paging
			try{
				page = Integer.valueOf(request.getParameter("page"));
			} catch (NumberFormatException e){
				page=1;
			}catch (NullPointerException e){
				page=1;
			}
			
			//===Results per page
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
			Statement fullStatement = dbcon.createStatement();
			String query;
			String fullQuery;//full search to count results
			if (searchBy.equals("genre")) {
				query = "SELECT DISTINCT * FROM movies m, genres_in_movies g, genres g1 "
						+ "WHERE g.movie_id=m.id "
						+ "AND g.genre_id=g1.id "
						+ "AND name = '" + arg + "' "+sortBy+" LIMIT "+ listStart + "," +resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m, genres_in_movies g, genres g1 "
					+ "WHERE g.movie_id=m.id "
					+ "AND g.genre_id=g1.id "
					+ "AND name = '" + arg + "') as results";
			} else if (searchBy.equals("letter")) {
				query = "SELECT DISTINCT * FROM movies m WHERE title REGEXP '^" + arg +"' "+sortBy+" LIMIT "+ listStart + "," +resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '^" + arg +"') as results";
			} else if (searchBy.equals("title")) {
				query = "SELECT DISTINCT * FROM movies m WHERE title REGEXP '" + arg.charAt(0) +"' "+sortBy+" LIMIT "+ listStart + "," +resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '" + arg.charAt(0) +"') as results";
			} else {
				query = "SELECT DISTINCT * FROM movies m WHERE " + searchBy + " = '" + arg + "' "+order+" LIMIT "+ listStart + "," +resultsPerPage;
				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE " + searchBy + " = '" + arg + "') as results";
			}
			ResultSet rs = statement.executeQuery(query);
			
			//Find total number of results
			ResultSet fullCount = fullStatement.executeQuery(fullQuery);
			fullCount.next();
			int numberOfResults = fullCount.getInt(1);
			int numberOfPages = numberOfResults/resultsPerPage + (numberOfResults % resultsPerPage == 0?0:1 );
			
			//===Start Writing Page
			out.println("<HTML><HEAD><TITLE>FabFlix -- Search by "+searchBy+": "+arg+"</TITLE></HEAD><BODY><h1>FabFlix</h1><hr>" );

			searchTitlesBox(out, resultsPerPage);
			
			
			out.println("<br><H1>Search by "+searchBy+": "+arg+"</H1><br>");
			
			
			
			
			listSortOptions(out, searchBy, arg, order, page, resultsPerPage);
			
			out.println("<BR>");
			out.println("Page: "+page+"/"+numberOfPages+" ( "+numberOfResults+" Results : "+resultsPerPage+" results per page )<br><br>");
			
			boolean hadResults = false;
			
			
			while (rs.next()) {//For each movie
				hadResults = true;
				Integer movieID;
				try{
					movieID = Integer.valueOf(rs.getString("id"));
				}catch (Exception e){
					movieID = 0;
				}
				String title = rs.getString("title");
				String year = rs.getString("year");
				String bannerURL = rs.getString("banner_url");
				String director = rs.getString("director");
				out.println("<a href=\"MovieDetails?id=" + movieID + "\"><h2>"
						+ title + " (" + year + ")</h2><img src=\"" + bannerURL
						+ "\"></a><br>Year: <a href=\"ListResults?by=year&arg="+year+"\">" + year + "</a>" +
								"<br>Director: <a href=\"ListResults?by=director&arg="+director+"\">" + director + "</a>");
				
				out.println("<BR>");
				listGenres(out, dbcon, resultsPerPage, movieID);
				
				out.println("<BR>");
				
				listStars(out, dbcon, resultsPerPage, movieID);


				
				out.println("<hr>");
				
				
			}
			
			if (hadResults){
				//===Paging
				
				if (page > 1){
					out.println("<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+(page-1)+"&rpp="+resultsPerPage+"&order="+order+"\">Prev</a>");
				}else{
					out.println("Prev");
				}
				

				out.println("| "+page +"of" + numberOfPages+" |");

				rs.last();
				if (rs.getRow() < resultsPerPage){
					out.println("Next");
				}else{
					out.println("<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+(page+1)+"&rpp="+resultsPerPage+"&order="+order+"\">Next</a>");
				}
				rs.beforeFirst();
				
				listRppOptions(out, searchBy, arg, order, page, resultsPerPage);
								
				out.println("<br><hr>");

			}else{
				out.println("<h3>No Results.</h3><hr>");
			}

			
			
			browseGenres(out, dbcon, resultsPerPage);
			
			out.println("<hr>");
			
			browseTitles(out, resultsPerPage);

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

	private void listSortOptions(PrintWriter out, String searchBy, String arg, String order,
			Integer page, Integer resultsPerPage) {
		//sorting and results per page options
		out.println("Sort by: Title(");
		if (!order.equals("t_a")){
		out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + arg
				+ "&page=" + page + "&rpp=" + resultsPerPage
				+ "&order=t_a\">asc</a>");
		}else{out.println("asc");}
		out.println(")(");
		if (!order.equals("t_d")){
		out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + arg
				+ "&page=" + page + "&rpp=" + resultsPerPage
				+ "&order=t_d\">des</a>");
		}else{out.println("des");}
		out.println(") Year(");
		if (!order.equals("y_a")){
		out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + arg
				+ "&page=" + page + "&rpp=" + resultsPerPage
				+ "&order=y_a\">asc</a>");
		}else{out.println("asc");}
		out.println(")(");
		if (!order.equals("y_d")){
		out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + arg
				+ "&page=" + page + "&rpp=" + resultsPerPage
				+ "&order=y_d\">des</a>");
		}else{out.println("des");}
		out.println(")");
	}

	public void searchTitlesBox(PrintWriter out, Integer resultsPerPage) {
		//===Search Box
		out.println("<FORM ACTION=\"ListResults\" METHOD=\"GET\">  Search Titles: <INPUT TYPE=\"TEXT\" NAME=\"arg\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=rpp VALUE=\""+resultsPerPage+"\"><INPUT TYPE=\"SUBMIT\" VALUE=\"Search\">  </CENTER></FORM>");
	}

	private void listRppOptions(PrintWriter out, String searchBy, String arg,
			String order, Integer page, Integer resultsPerPage) {
		//===Results per page
		//TODO maybe adjust page number when changing number of results to keep centered
		out.println("<br>Results per page: ");
		if (!(resultsPerPage == 5)){
			out.println("<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+page+"&rpp=5&order="+order+"\">5</a>");
		}else{
			out.println("5");
		}
		if (!(resultsPerPage == 25)){
			out.println("<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+page+"&rpp=25&order="+order+"\">25</a>");
		}else{
			out.println("25");
		}
		if (!(resultsPerPage == 100)){
			out.println("<a href=\"ListResults?by="+searchBy+"&arg="+arg+"&page="+page+"&rpp=100&order="+order+"\">100</a>");
		}else{
			out.println("100");
		}
	}

	public void browseGenres(PrintWriter out, Connection dbcon) throws SQLException {	
		browseGenres(out, dbcon, 5);//default rpp=5
	}
	public void browseGenres(PrintWriter out, Connection dbcon,
					Integer resultsPerPage) throws SQLException {
		Statement statement = dbcon.createStatement();
		//===GENRE browser
		out.println("Browse Genres: ");
		int col = 0;
		ResultSet allGenre = statement.executeQuery("SELECT DISTINCT name FROM genres ORDER BY name");
		if(allGenre.next()){
			col++;
			String genreName = allGenre.getString("name");
			out.println("<a href=\"ListResults?by=genre&arg="+genreName+"&page=1&rpp="+resultsPerPage+"\">"+genreName+"</a>");
			while (allGenre.next()){
				col++;
				genreName= allGenre.getString("name");
				out.println(" | <a href=\"ListResults?by=genre&arg="+genreName+"&page=1&rpp="+resultsPerPage+"\">"+genreName+"</a>");
				if (col>=10 && allGenre.next()){
					genreName= allGenre.getString("name");
					out.println("<br><a href=\"ListResults?by=genre&arg="+genreName+"&page=1&rpp="+resultsPerPage+"\">"+genreName+"</a>");
					col=1;
				}//10 items per row 
			}
		}
		allGenre.close();
		statement.close();
	}

	public void browseTitles(PrintWriter out) {
		browseTitles(out, 5);//default rpp=5
	}
	public void browseTitles(PrintWriter out, Integer resultsPerPage) {
		//===Letter Browser
		out.println("Browse Titles: ");
		String alphaNum = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i=0;i<alphaNum.length();i++){
			if(i!=0){out.println("-");}
			out.println("<a href=\"ListResults?by=letter&arg="+alphaNum.charAt(i)+"&page=1&rpp="+resultsPerPage+"\">"+alphaNum.charAt(i)+"</a>");
		}
	}
	
	public void listStars(PrintWriter out, Connection dbcon, Integer resultsPerPage, Integer movieID)  throws SQLException {
		Statement statement = dbcon.createStatement();
		//===STARS; comma separated list
		out.println("Stars: ");
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
		stars.close();
		statement.close();
	}

	public void listGenres(PrintWriter out, Connection dbcon,
			Integer resultsPerPage, Integer movieID) throws SQLException {
		// ===GENRES; comma separated list
		out.println("Genre: ");
		Statement statement = dbcon.createStatement();
		ResultSet genres = statement.executeQuery("SELECT DISTINCT name "
				+ "FROM movies m, genres_in_movies g, genres g1 "
				+ "WHERE g.movie_id=m.id " + "AND g.genre_id=g1.id "
				+ "AND m.id ='" + movieID + "'");
		if (genres.next()) {
			String genre = genres.getString("name").trim();
			out.println("<a href=\"ListResults?by=genre&arg=" + genre + "\">"
					+ genre + "</a>");
			while (genres.next()) {
				genre = genres.getString("name").trim();
				out.println(", <a href=\"ListResults?by=genre&arg=" + genre
						+ "\">" + genre + "</a>");
			}
		}
		genres.close();
		statement.close();
	}

}
