SET10113 Secure Software Development — CW2 README for Codex

Project context

This coursework is for SET10113 Secure Software Development.

The main target application is BankWebsite / Bank_Attack, an intentionally vulnerable Java web application simulating a banking system.

Environment:
	•	Java + Servlets (Jetty)
	•	URL: http://localhost:15000/welcome
	•	Roles: admin and normal users

⸻

Coursework 1 (COMPLETED)

Vulnerabilities identified and exploited
	1.	SQL Injection (CWE-89)
	2.	Stored XSS (CWE-79)
	3.	Improper Authorization / Information Exposure (CWE-285 / CWE-200)
	4.	Unrestricted File Upload (CWE-434)
	5.	CSRF (CWE-352)

Summary of exploitation
	•	SQL Injection: login bypass using ’ OR 1=1 –
	•	Stored XSS: script execution in profile description
	•	Info Exposure: users can view other users’ card numbers
	•	File Upload: unsafe SVG accepted and rendered
	•	CSRF: transfer triggered via external HTML page

⸻

Additional Vulnerabilities (CW2)

Selected from coursework list:
	1.	Path Traversal
	2.	XML Injection

IMPORTANT IMPLEMENTATION DECISION

These MUST follow CW1 plan:
	•	XML Injection → implemented as a webpage added to BankWebsite
	•	Path Traversal → implemented as a separate standalone application

This means:
	•	The vulnerabilities are NOT combined into one feature
	•	They will be demonstrated separately

⸻

Coursework 2 Requirements

You must:
	1.	Fix all 5 CW1 vulnerabilities in BankWebsite
	2.	Implement 2 new vulnerabilities (Path Traversal + XML Injection)
	3.	Mitigate both new vulnerabilities
	4.	Demonstrate everything clearly

⸻

CRITICAL REQUIREMENTS (MUST FOLLOW)

For EACH additional vulnerability:
	1.	Show normal behaviour
	2.	Show successful attack
	3.	Apply mitigation
	4.	Show attack fails after fix
	5.	Show normal behaviour still works

A VIDEO is mandatory:
	•	3 minutes per vulnerability
	•	total ~6 minutes

If video is missing → 0 marks for that section

⸻

XML Injection (BankWebsite Feature)

Feature: XML Statement Viewer

Add a new webpage inside BankWebsite:

Route example:
	•	/xml-statement

Normal behaviour
	•	User submits valid XML
	•	System parses expected fields
	•	Data displayed correctly

Vulnerable behaviour
	•	XML is processed without validation
	•	Malicious XML changes output or structure

Example attack
	•	Inject unexpected nodes
	•	Modify structure

Mitigation
	•	Validate XML structure (schema or allowlist)
	•	Reject unexpected elements
	•	Sanitize parsed data

⸻

Path Traversal (Standalone Application)

Feature: File Reader Application

A small Java application that:
	•	reads a file based on user input
	•	displays file contents

Normal behaviour
	•	User requests safe file
	•	File is read from allowed directory

Vulnerable behaviour
	•	User input is used directly as file path

Example attack:
../../../../etc/passwd

Mitigation
	•	Canonicalize path
	•	Restrict to base directory
	•	Allowlist filenames

⸻

Tasks for Codex

Part A — Additional vulnerabilities
	1.	XML Injection (BankWebsite)
	•	add servlet
	•	implement vulnerable parsing
	•	implement secure parsing
	2.	Path Traversal (Standalone app)
	•	simple Java file reader
	•	vulnerable version
	•	secure version

⸻

Part B — Fix CW1 vulnerabilities

Fix:
	1.	SQL Injection → prepared statements
	2.	Stored XSS → encoding/sanitization
	3.	Authorization → role checks
	4.	File Upload → type validation
	5.	CSRF → tokens

Each fix must:
	•	stop the attack
	•	keep normal behaviour working

⸻

Constraints for Codex
	•	Do NOT rewrite the whole project
	•	Make minimal changes
	•	Keep code simple for demo
	•	Separate vulnerable vs secure clearly
	•	Ensure everything is demonstrable

⸻

NEXT STEP FOR CODEX
	1.	Analyse project structure
	2.	Propose implementation for:
	•	XML Injection servlet
	•	Path Traversal standalone app
	3.	List files to create/modify
	4.	Provide step-by-step plan

DO NOT generate code yet

⸻

FINAL SUMMARY

CW1 DONE:
	•	5 vulnerabilities exploited

CW2 GOAL:
	•	Fix 5 vulnerabilities
	•	Implement XML Injection (webpage)
	•	Implement Path Traversal (standalone app)
	•	Mitigate both
	•	Record video