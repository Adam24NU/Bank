import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.List;
import java.io.File;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.bson.Document;

/**
 * Java servlet for displaying and editing the account page.
 * 
 * @author Joseph Eichenhofer, Emma He
 */
public class AccountPageServlet extends HttpServlet {
    /*
    * (non-Javadoc)
    * 
    * @see
    * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
    * javax.servlet.http.HttpServletResponse)
    * 
    * Retrieve the session from HttpServletRequest.
	* Get username from session to identify viewer's identity, 
    * and thisUsername from Request to identify whose account page is displaying.
	* If this viewer's identity is the same as the account page, 
    * allow the viewer to edit the account.
	* Otherwise, the viewer can only view the page without editing. 
    * 
    */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        // Get session information
		HttpSession session = req.getSession(false);

		// if no session ,go to welcome page; otherwise, write out the account page.
		if(session == null){
			res.sendRedirect("/welcome");
		} else {
			// Get viewer's name from session, and user of the account page from request
			// then check if this page belongs to the viewer
			String username = (String) session.getAttribute("username");
			String thisUsername = req.getParameter("username");
			String pageUsername = username;
			if(thisUsername != null && !thisUsername.equals(username)){
				pageUsername = thisUsername;
			}

			// Set up the response content
			PrintWriter content = res.getWriter();
			res.setContentType("text/html; charset=htf-8");
			res.setStatus(HttpServletResponse.SC_OK);
			
			// Header ot eh HTML page, declare the title and css files
			content.println("<!DOCTYPE html>");
            content.println("<html lang='en'>");
            content.println("<head>");
            content.println("<meta charset='UTF-8'>");
            content.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            content.println("<title>Account Page</title>");
            content.println("<link rel='stylesheet' type='text/css' href='AccountPage.css'>");
            content.println("<link rel='stylesheet' type='text/css' href='Navbar.css'>");
            content.println("</head>");
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
                    
                    
            // Main Content Section
            content.println("<main>");
            content.println("<section class='profile-section'>");
            content.println("<h1>Welcome to " + pageUsername + "'s Page!</h1>");
            content.println("<div class='wrapper'>");
                    
            content.println("<div class='horizontal-container'>");
            
            if (Database.getType(username).equals("admin") || username.equals(pageUsername)) {
                // Balance Section
                content.println("<div class='container balance-section'>");
                content.println("<h2>" + pageUsername + "'s Balance</h2>");
                content.println("<p class='balance-amount'>" + Database.getBalance(pageUsername) + " $</p>");
                content.println("</div>");
            }

            // Card Number Section
            content.println("<div class='container card-number-section'>");
            content.println("<h2>" + pageUsername + "'s Card Number</h2>");
            content.println("<p class='card-number'>" + Database.getCardId(pageUsername) + "</p>");
            content.println("</div>");
                    
                    
            // Profile Section
            content.println("<div class='container'>");
            content.println("<h2>" + pageUsername + "'s Profile</h2>");
                    
            // Profile Picture Section
            String profilePicture = Database.getProfilePicture(pageUsername);
            String defaultImage = "./avatar.png";
            String imageUrl = (profilePicture == null || profilePicture.isEmpty()) ? defaultImage : "http://localhost:15000/" + profilePicture;
                    
            content.println("<div class='iframe-container'>");
            content.println("<iframe src='" + imageUrl + "' alt='Profile Picture'></iframe>");
            content.println("</div>");
            
			if(thisUsername == null || thisUsername.equals(username)){
                content.println("<div class='upload-container'>");
                content.println("<label for='profilePic' class='upload-label'>Choose Picture</label>");
                content.println("<input type='file' id='profilePic' name='profilePic' accept='image/*'>");
                content.println("<button id='uploadPic'>Upload</button>");
                content.println("</div>");
            }
            content.println("</div>");

            content.println("</div>");
                    
            String profile = Database.getProfile(pageUsername);		
			if(profile == null){
				profile = "";
			}
        	content.println("<div class=\"sevralLine\" id=\"profile\" >" + profile + "</div>");
			if(thisUsername == null || thisUsername.equals(username)){
        		content.println("<textarea placeholder=\"Change Your Profile\" class=\"inputField\">" + profile  + "</textarea>");
				content.println("<button id=\"submit\">Edit Profile Description</button>");
			}
    		content.println("</div>");
                    
                    
            content.println("</div>"); // End of wrapper
            content.println("</section>");
            content.println("</main>");
                    
            content.println("<script src='./Account.js'></script>");
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
         * Retrieve the session from HttpServletRequest.
	 * Retrieve the data from XMLHttpRequest. 
	 * Add the data to the database, and send a message with response.
         * 
         */
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Get session information
        HttpSession session = req.getSession(false);

        // If no session, redirect to the welcome page; otherwise, proceed with updating account data
        if (session == null) {
            res.sendRedirect("/welcome");
        } else {
            // Get the username from the session
            String username = (String) session.getAttribute("username");

            // Handle multipart form-data for profile picture upload
            if (req.getContentType().startsWith("multipart/form-data")) {
                handleFileUpload(req, res, username);
            } else {
                // Handle regular form data (profile text update)
                String params = getRequestData(req);
                String value = params.substring("value=".length(), params.length());
                boolean result = Database.addAccountInfo(username, value);

                // Respond with success or failure
                PrintWriter out = res.getWriter();
                if (result) {
                    out.print("Success!");
                } else {
                    out.print("Fail!");
                }
            }
        }
    }

    // Helper method for reading request data
    private String getRequestData(HttpServletRequest req) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }

    // Helper method for handling file upload
    private void handleFileUpload(HttpServletRequest req, HttpServletResponse res, String username) throws IOException {
        // Use Apache Commons FileUpload to handle file uploads
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            // Parse the request to get the uploaded file
            List<FileItem> items = upload.parseRequest(req);
            for (FileItem item : items) {
                if (!item.isFormField() && item.getFieldName().equals("profilePic")) {
                    // Get the uploaded file name
                    String fileName = item.getName();
                
                    // Sanitize the file name (remove unwanted characters)
                    String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

                    // Get the current project path
                    String projectPath = System.getProperty("user.dir");

                    // Define the path where the file will be saved
                    String filePath = projectPath + File.separator + "WebContext" + File.separator + sanitizedFileName;

                    // Create the directory if it doesn't exist
                    File uploadDir = new File(projectPath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();  // Create the directory if it doesn't exist
                    }

                    // Create the file object for saving the uploaded file
                    File uploadedFile = new File(filePath);

                    // Write the uploaded file to the disk
                    item.write(uploadedFile);

                    // Update the user's profile with the image path in the database
                    Database.updateProfilePicture(username, sanitizedFileName);
                
                    // Send a success response
                    res.getWriter().print("Profile picture uploaded successfully. refresh the page");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.getWriter().print("Error uploading profile picture: " + e.getMessage());
        }
    }

}
