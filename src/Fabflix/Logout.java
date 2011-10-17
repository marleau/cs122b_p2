package Fabflix;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public Logout() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.removeAttribute("user.login");
		session.removeAttribute("user.name");
		session.removeAttribute("user.id");
		session.removeAttribute("user.dest");
		session.removeAttribute("login");
		session.setAttribute("validCC", false);
		response.sendRedirect("/Fabflix/");
	}

	public static void button(PrintWriter out) {
		out.println("(<a href=\"Logout\">Log Out</a>)");
	}

}
