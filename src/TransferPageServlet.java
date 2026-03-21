import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

/**
 * Java servlet for displaying the transfer page, get user input, and call doTransfer servlet.
 *
 * @author Emma He
 */
public class TransferPageServlet extends HttpServlet {
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
         * javax.servlet.http.HttpServletResponse)
         * 
         * Retrieve the session from HttpServletRequest.
         * Display the transfer result if it can be retreived from the response.
         * Display the transfer page to get three parameters for transferring: 
         * to, the user transferred to,
         * balance, the amount of balance to transfer, 
         * and submit, to call doTransfer servlet to do the transfer. 
         * 
         */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
                // Get session information
		HttpSession session = req.getSession(false);

		// if no session, go to welcome page; otherwise, write out transfer page
		if(session == null){
			res.sendRedirect("/welcome");
		} else {
			// Get the response message from session
			String result = (String) session.getAttribute("result");
			String username = (String) session.getAttribute("username");
			Integer credits = (Integer) session.getAttribute("credit");
		
			// Set up the response content	
			PrintWriter content = res.getWriter();
			res.setContentType("text/html; charset=utf-8");
			res.setStatus(HttpServletResponse.SC_OK);
	
			// Header of the HTML page, declare the title and CSS files
			content.println("<!DOCTYPE html>");
                        content.println("<html lang='en'>");
                        content.println("<head>");
                        content.println("<meta charset='UTF-8'>");
                        content.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
			content.println("<title>Transfer Page</title>");
                	content.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./Transfer.css\">");
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
	
			// Headline
                        content.println("<h1>Transfer Your Balance</h1>");

                        // Form for getting transfer information
                        content.println("<div id=\"container\">");

                        // Display balance based on credits or default from database
                        if (credits != null && credits != -1) {
                            content.println("<div class=\"balance\"><h2>My Balance: " + credits + "$</h2></div>");
                            session.setAttribute("credit", -1);
                        } else {
                            content.println("<div class=\"balance\"><h2>My Balance: " + Database.getBalance(username) + "$</h2></div>");
                        }

                        // Begin the form
                        content.println("<form action=\"transfer\" method=\"POST\" accept-charset=\"utf-8\">");

                        // Display response message if available
                        if (result != null && !result.isEmpty()) {
                            content.println("<div class=\"message\">" + result + "</div>");
                            session.setAttribute("result", "");
                        } else {
                            content.println("<div class=\"message\"></div>");
                        }

                        // Display the 'To Card Number' and 'Amount' inputs
                        content.println("<div class=\"form-group\">");
                        content.println("<label for=\"to\">To Card Number:</label>");
                        content.println("<input type=\"text\" name=\"to\" id=\"to\" />");
                        content.println("</div>");

                        content.println("<div class=\"form-group\">");
                        content.println("<label for=\"transferAmount\">Amount:</label>");
                        content.println("<input type=\"text\" name=\"transferAmount\" id=\"transferAmount\" />");
                        content.println("</div>");

                        // Submit Button
                        content.println("<button id=\"submitButton\" type=\"submit\">Submit</button>");

                        content.println("</form>"); // Close the form
                        content.println("</div>"); // Close the container

			
			// Close the tag for the HTML page
			content.println("</body>");
			content.println("</html>");
		}
	}

	/*
         * (non-Javadoc)
         * 
         * @see
         * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
         * javax.servlet.http.HttpServletResponse)
         * 
         * Retrieve the data from the request
         * Connect to the database to transfer the point 
         * If success, send back success message
         * If fail, send back fail message
	 * 
         */
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		// Get session information
                HttpSession session = req.getSession(false);

                res.setContentType("text/html; charset=utf-8");
                res.setStatus(HttpServletResponse.SC_OK);

                // Get two parameters: the user to whom credits are trasferred and the number of credits to transfer
                String to = req.getParameter("to");
                String transferAmount = req.getParameter("transferAmount");
                int addBalance = 0;
                if(transferAmount != null){
                        try {
                                addBalance = Integer.parseInt(transferAmount);
                        } catch (Exception ex){
                                addBalance = 0;
                        }
                }
                int deductBalance = 0 - addBalance;

                // If session is not valid, go back to welcome page
                if(session == null){
                        res.sendRedirect("/welcome");
                } else {
                        // Connect to the database, complete the transfer and store the result in session
                        session.setAttribute("result", "Fail to transfer.");
                        String from;
                        boolean isToCardExist = false;
                        from = (String) session.getAttribute("username");
                        isToCardExist = Database.isCardExist(to);
                        String fromCard = Database.getCardId(from);
                        int fromBalance = Database.getBalance(from);
                        if(isToCardExist && addBalance != 0 && to != null && addBalance > 0 && from != null && !fromCard.equals(to) && fromBalance >= addBalance){
                                int result = Database.transferBalance(fromCard, to, fromBalance, addBalance);
                                if(result >= 0){
                                        session.setAttribute("result", "Success!");
                                        if (from.equals((String) session.getAttribute("username"))){
                                                session.setAttribute("credit", result);
                                        }
                                }
                        }
                        res.sendRedirect("/transfer");
                }

	}
}
