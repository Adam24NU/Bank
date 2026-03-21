/**
 * Used for account page
 *
 * Handle two button clicks and send one POST request 
 *
 * @link   /account
 * @file   AccountPageServlet.java
 * @author Emma He
 */

// Select all buttons in the account page, 
// Once it is clicked, show an inputfield
var buttonEdit = document.querySelector("#edit");

/*
 * This function is called when edit/cancel button is clicked
 * It changes the visibility of the input field
 */

// Select the file input and the upload button
var buttonUploadPic = document.querySelector("#uploadPic");
var profilePicInput = document.querySelector("#profilePic");

/*
 * This function is called when the upload button is clicked.
 * It checks the file type and sends the image to the server via AJAX
 */
buttonUploadPic.addEventListener("click", function (event) {
	event.preventDefault();  // Prevent the form from submitting traditionally

	var file = profilePicInput.files[0]; // Get the selected file
	if (file) {
		// Check if the file is an image (not an .exe file)
		var fileType = file.type.split('/')[0]; // Get the file type (e.g., "image")
		if (fileType === "exe") {
			alert("Please upload a valid image file.");
		} else {
			// Send the file to the server
			var formData = new FormData();
			formData.append("profilePic", file);
			ajaxUploadRequest("/account", formData);
		}
	} else {
		alert("No file selected.");
	}
});

/*
 * This function handles the AJAX request to upload the image.
 * It sends the image to the server.
 */
function ajaxUploadRequest(reqURL, formData) {
	var xhr = new XMLHttpRequest();
	xhr.open("POST", reqURL, true);

	// Send the form data (including the file)
	xhr.onreadystatechange = function () {
		if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
			console.log(xhr.responseText);
			alert("Profile picture uploaded successfully. refresh the page");
		}
	};
	xhr.send(formData);
}

// select the submit button and profile input field
var buttonSubmit = document.querySelector("#submit");
var profileField = document.querySelector("#profile");

/*
 * This function is called when the submit button is cliekd.
 * It will send a POST request with content of profile using Ajax
 */
buttonSubmit.addEventListener("click", function () {
	var x = document.querySelector("textarea").value;
	//console.log(x.replace(new RegExp('\n', 'g'), "<br>"));
	//profileField.innerHTML = x.replace(new RegExp("\n", 'g'), "<br>");
	/*this.previousElementSibling.classList.add("inputField");
	this.previousElementSibling.classList.remove("inputDisplay");
	this.nextElementSibling.innerHTML = "Edit";
	this.nextElementSibling.style.marginLeft = "80%";
	this.style.display = "none";*/
	ajaxSyncRequest("/account", x);
});


/*
 * This function is called when submit button is clicked to trigger
 * a POST request.
 * @param reqURL: the URL to send the POST request
 * @param parameter: the content of profile
 */
function ajaxSyncRequest(reqURL, parameter) {
	var xhs;
	if (window.XMLHttpRequest) {
		xhs = new XMLHttpRequest();
	} else {
		xhs = new ActiveXObject("Microsoft.XMLHTTP");
	}

	xhs.open("POST", reqURL, true);
	xhs.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

	xhs.onreadystatechange = function () {
		if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
			console.log(xhs.responseText);
			console.log(parameter);
			document.querySelector("#profile").innerHTML = parameter;
			alert("Profile Description updated.");
		}
		else {
			console.log('Still Waiting!!');
		}


	};
	xhs.send("value=" + parameter);
}
