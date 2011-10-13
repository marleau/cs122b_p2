package Fabflix;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;

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
        HttpSession session = request.getSession();
        session.setAttribute("title", "Home");
        response.sendRedirect("/Fabflix/index.jsp");
    }
}
