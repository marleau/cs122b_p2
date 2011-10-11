package Fabflix;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HomePage extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public HomePage() {
        super();
    }

    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LoginPage.kickNonUsers(request, response);
    }
    
    public static void test(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	HttpSession session = request.getSession();// Get client session
		String user = (String) session.getAttribute("user.login");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.print("Welcome! " + user);
        out.close();   
    }
}
