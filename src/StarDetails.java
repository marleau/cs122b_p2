

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
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

			// Declare our statement
			Statement statement = dbcon.createStatement();
			String query = "SELECT * FROM stars s, stars_in_movies si, movies m "
					+ "WHERE si.star_id=s.id "
					+ "AND si.movie_id=m.id "
					+ "AND s.id ='" + request.getParameter("id")+"' ORDER BY year";

			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) {
				String starName = rs.getString("first_name") + " " + rs.getString("last_name");
				String starIMG = rs.getString("photo_url");
				String dob = rs.getString("dob");

				out.println("<HTML><HEAD><TITLE>FabFlix -- " + starName + "</TITLE></HEAD>");
				out.println("<BODY><H1>" + starName + "</H1><br>" + "<img src=\"" + starIMG + "\">"
						+ "<br>" + "Date of Birth: " + dob + "<br><br>Starred in:<br>");

				do {
					String title = rs.getString("title");
					Integer year = rs.getInt("year");
					Integer movieID = rs.getInt("movie_id");

					String bannerURL = rs.getString("banner_url");
					
					out.println("<a href=\"MovieDetails?id="
							+ movieID + "\"><img src=\"" + bannerURL + "\">"
							+ title+" ("+year+")"+"</a><br><br>");
					
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
