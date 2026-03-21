import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

/**
 * Java servlet for displaying the ranking data retrieved from database.
 * 
 * @author Emma He
 */
public class CustomersListPageServlet extends HttpServlet {

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     * 
     * Retrieve the session from HttpServletRequest.
     * Retrieve the data from the database.
     * Display the top five ranking on the page. 
     * 
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HttpSession session = req.getSession(false);

        if (session == null) {
            res.sendRedirect("/welcome");
        } else {
            // Set up the response content
            PrintWriter content = res.getWriter();
            res.setContentType("text/html; charset=utf-8");
            res.setStatus(HttpServletResponse.SC_OK);

            String username = (String) session.getAttribute("username");

            // Get the data from the database 
            ArrayList<String[]> customersList = Database.getCustomersList();

            // Header of the HTML page, declares the title and css files
            content.println("<!DOCTYPE html>");
            content.println("<html lang='en'>");
            content.println("<head>");
            content.println("<meta charset='UTF-8'>");
            content.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            content.println("<title>Customers Balance Page</title>");
            content.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./CustomersList.css\">");
            content.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./Navbar.css\">");
            content.println("</head>");

            // Body of the HTML page
            content.println("<body>");

            // Header Section
            content.println("<header id='headerNav'>");
            content.println("<div class='header-container'>");
            content.println("<img src='./logo.png' alt='Logo' class='logo' width='150' height='50'>");
            content.println("<span class='hello'>Welcome, " + username + "</span>");
            content.println("<nav class='navbar'>");
            content.println("<a class='nav' href='account'>My Account</a>");
            content.println("<a class='nav' href='transfer'>Transfer</a>");
            content.println("<a class='nav' href='balance'>Customers</a>");
            content.println("<form action='logout' method='POST' class='logoutForm'>");
            content.println("<input value='Log Out' type='submit' class='logoutInput nav'>");
            content.println("</form>");
            content.println("</nav>");
            content.println("</div>");
            content.println("</header>");

            // Headline and the ranking list
            content.println("<h1 class='balance-heading'>Customers Balance</h1>");
            content.println("<div id='container'>");
            content.println("<ul>");
       
            content.println("<li class='ranking-item'>");
            content.println("<div class='ranking-info'>");
            content.println("<span class='ranking-num'>#</span>");
            content.println("<span class='ranking-link'>username</span>");
            if (Database.getType(username).equals("admin")) {
                content.println("<span class='ranking-credits'>Balance</span>");
            } else {
                content.println("<span class='ranking-credits'>Card Number</span>");
            }
            content.println("</div>");
            content.println("</li>");

            for (int i = 0; i < customersList.size(); i++) {
                String inTableUsername = customersList.get(i)[0];
                String userType = Database.getType(inTableUsername);

                content.println("<li class='ranking-item'>");
                content.println("<div class='ranking-info'>");
                content.println("<span class='ranking-num'>" + (i + 1) + "</span>");
                content.println("<a href='account?username=" + inTableUsername + "' class='ranking-link'>" + inTableUsername + "</a>");
                if (Database.getType(username).equals("admin")) {
                    content.println("<span class='ranking-credits'>" + customersList.get(i)[1] + "$</span>");
                } else {
                    content.println("<span class='ranking-credits'>" + Database.getCardId(inTableUsername) + "</span>");
                }
                content.println("</div>");
                content.println("</li>");
            }

            content.println("</ul>");
            content.println("</div>");

            // Close the tags
            content.println("</body>");
            content.println("</html>");
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get session information
        HttpSession session = request.getSession(false);

        // If no session, redirect to the welcome page; otherwise, proceed with updating account data
        if (session == null) {
            response.sendRedirect("/welcome");
        } else {
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                if (paramName.startsWith("userTypeToggle")) {
                    String username = request.getParameter("username");
                    String userType = request.getParameter(paramName);
                    if ("admin".equals(userType)) {
                        Database.setUserType(username, "admin");
                    } else {
                        Database.setUserType(username, "normal");
                    }
                }
            }
        }
	}
    
}
