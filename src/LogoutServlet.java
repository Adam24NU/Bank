import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * Java servlet for logging out the account and displaying the welcome page.
 *
 * @author Emma He
 */
public class LogoutServlet extends HttpServlet {
	
	/*
	 * (non-Javadoc)
	 * 
	 * * @see
         * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
         * javax.servlet.http.HttpServletResponse)
	 *
	 * Invalidate the session and redirect to the welcome page
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		req.getSession().invalidate();
		
		res.sendRedirect("/welcome");
	}


}
