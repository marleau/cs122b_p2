
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String title = "FabFlix Login";
		String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
				+ "Transitional//EN\">\n";
		out.println(docType + "<HTML>\n" + "<HEAD><TITLE>" + title
				+ "</TITLE></HEAD>\n" + "<BODY BGCOLOR=\"#FDF5E6\">\n" + "<H1>"
				+ title + "</H1>");

		HttpSession session = request.getSession(true);

		String account = request.getParameter("account");
		String password = request.getParameter("password");

		if (!validUser(account, password)) {
			out.println("<HTML><HEAD><TITLE>Access Denied</TITLE></HEAD>");
			out.println("<BODY>Your login and password are invalid.<BR>");
			out
					.println("You may want to <A HREF=\"/login.html\">try again</A>");
			out.println("</BODY></HTML>");
		} else {
			// Valid login. Make a note in the session object.
			session = request.getSession();
			session.setAttribute("logon.isDone", account);
			// Try redirecting the client to the page he first tried to access
			try {
				String target = (String) session.getAttribute("login.target");
				if (target != null) {
					response.sendRedirect(target);
					return;
				}
			} catch (Exception ignored) {
			}

			// Couldn't redirect to the target. Redirect to the site's home
			// page.
			response.sendRedirect("/Fabflix/ListResults");

		}

	}
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response); //DEBUG must only do post
	}

	private boolean validUser(String account, String password) {
		// TODO Auto-generated method stub
		return true;
	}
}
