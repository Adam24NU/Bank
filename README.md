# SET10113 Secure Software Development — CW2 README for Codex

## Project context

This coursework is for **SET10113 Secure Software Development**.

The main target application is **BankWebsite / Bank_Attack**, an intentionally vulnerable Java web application simulating a basic banking platform.

Environment used so far:

* Java web app running locally or on the authorised university VM
* Default app URL: `http://localhost:15000/welcome`
* Known roles: `admin` and normal users such as `user1`, `user4`, etc.

## Coursework 1 summary (already completed)

CW1 had two parts:

1. Find and exploit five taught vulnerabilities in BankWebsite
2. Propose two additional vulnerabilities for Coursework 2

### Final five CW1 vulnerabilities chosen and evidenced

1. **SQL Injection** — `CWE-89`
2. **Stored XSS** — `CWE-79`
3. **Improper Authorization / Information Exposure** — `CWE-285` / `CWE-200`
4. **Unrestricted File Upload** — `CWE-434`
5. **Cross-Site Request Forgery (CSRF)** — `CWE-352`

### Brief evidence summary from CW1

* **SQL Injection**: login bypass worked using payloads such as `' OR 1=1 --` and `admin' --`
* **Stored XSS**: JavaScript inserted in the profile description executed when the page was viewed
* **Improper Authorization / Information Exposure**: normal users could access the customers page and view other users' card numbers
* **Unrestricted File Upload**: unsafe file types such as SVG with active content were accepted and rendered in the profile area
* **CSRF**: transfer form submitted only `to` and `transferAmount`; no CSRF token was present, and a separate `csrf.html` page successfully triggered a transfer while the user was logged in

### Additional vulnerabilities selected in CW1 for CW2

These are the two additional vulnerabilities planned for implementation in Coursework 2:

1. **Path Traversal**
2. **XML Injection**

These were selected from the coursework appendix as high-value choices.

## Coursework 2 requirements

CW2 builds on CW1.

### Core tasks

1. **Mitigate the five CW1 BankWebsite vulnerabilities**
2. **Implement the two additional vulnerabilities selected in CW1**
3. **Mitigate those two additional vulnerabilities**
4. **Integrate the two additional vulnerabilities into one application or into BankWebsite** for stronger marks

### Important assessment requirements

For each additional vulnerability in CW2, the coursework requires showing:

1. **Normal behaviour**
2. **Successful attack / exploitation**
3. **Mitigation applied**
4. **Proof that the same attack now fails**
5. **Proof that normal behaviour still works after mitigation**

A **video demonstration is mandatory** for the two additional vulnerabilities.
Without the required video, those sections receive **0 marks**.

## Recommended CW2 strategy

The best strategy is to build **one combined feature inside BankWebsite** that supports both additional vulnerabilities:

* **Path Traversal**
* **XML Injection**

This helps with:

* integration marks
* realism
* easier testing
* easier video recording
* easier report writing

## Recommended combined feature

### Feature idea: XML Statement Viewer / Importer

Add a new BankWebsite feature/page that allows a user or admin to:

* select or request an XML statement file
* load and parse XML transaction or profile data
* display the parsed information in the browser

This single feature can contain both vulnerabilities.

### Vulnerability A: Path Traversal

#### Normal behaviour

The user requests a file from an allowed statements directory, for example:

* `statement1.xml`
* `statement2.xml`

The application loads and displays only files from the safe directory.

#### Vulnerable behaviour

The app trusts a user-controlled filename or path parameter, for example:

* `../../../../etc/passwd`
* `../secret/admin.xml`

The application then reads files outside the intended directory.

#### Typical vulnerable pattern

A likely vulnerable implementation pattern would be:

* user-controlled `file` parameter
* server concatenates `uploads/` or `statements/` with that value
* no canonical path validation

#### Expected mitigation

* canonicalise file path
* restrict file access to a safe base directory
* use allow-list validation for filenames
* reject path separators and traversal sequences

### Vulnerability B: XML Injection

#### Normal behaviour

The application accepts valid XML input, parses expected nodes, and displays correct data.

Example normal use:

* load a valid XML statement
* parse expected fields such as account, amount, transaction date, description
* display them correctly

#### Vulnerable behaviour

The application accepts malicious or malformed XML that changes how data is processed.
Possible attack approaches:

* injecting unexpected XML elements
* altering structure to confuse the parser
* submitting oversized or maliciously structured XML input

#### Expected mitigation

* validate against a schema or strict expected structure
* reject unexpected elements/attributes
* disable unsafe parser features if relevant
* validate and sanitise parsed values before use

## Deliverables Codex should help produce

### 1. Application code changes

Codex should help:

* inspect existing BankWebsite structure
* identify best place to add the new XML statement feature
* implement the vulnerable version first
* then implement the secure version
* keep code changes clear and reviewable

### 2. Clear before/after demos

For **each** additional vulnerability, Codex should help create a workflow for:

* normal operation
* exploitation
* mitigation
* failed exploit after mitigation
* normal operation after mitigation

### 3. Report support

Codex should help prepare concise technical notes for:

* vulnerable code explanation
* attack input used
* mitigation explanation
* before/after comparison
* security rationale

Codex should **not** invent results that were not actually tested.

## What still needs to be done

### Part A — Build and test additional vulnerabilities

1. Inspect BankWebsite codebase and decide exact location for new combined feature
2. Add the new page(s) / servlet(s) / handler(s)
3. Implement vulnerable Path Traversal behaviour
4. Implement vulnerable XML Injection behaviour
5. Test and record **normal behaviour**
6. Test and record **successful attacks**
7. Implement mitigations
8. Re-test attacks and confirm they fail
9. Re-test normal behaviour and confirm it still works
10. Prepare the mandatory video

### Part B — Mitigate existing CW1 vulnerabilities in BankWebsite

Codex should help identify and implement proper mitigations for:

1. SQL Injection
2. Stored XSS
3. Improper Authorization / Information Exposure
4. Unrestricted File Upload
5. CSRF

For each mitigation, we need:

* vulnerable area identified in code
* secure fix
* brief explanation of why the fix is correct
* evidence that the original exploit no longer works
* evidence that normal functionality still works

## Notes about the five CW1 mitigations

### SQL Injection mitigation target

Likely fix:

* use proper prepared statements / parameterised queries
* never build SQL from raw user input

### Stored XSS mitigation target

Likely fix:

* validate or sanitise input
* output encode untrusted data
* avoid rendering raw HTML/JS from profile content

### Improper Authorization / Information Exposure mitigation target

Likely fix:

* enforce role-based checks on customers/balance endpoints
* ensure normal users cannot view other users' sensitive information

### Unrestricted File Upload mitigation target

Likely fix:

* restrict file types
* validate real MIME/content type
* reject SVG or dangerous active content unless sanitised
* store uploads safely

### CSRF mitigation target

Likely fix:

* add anti-CSRF token to forms
* verify token server-side
* optionally strengthen cookie settings

## Constraints and guidance for Codex

* Prefer small, well-contained changes
* Preserve existing normal functionality
* Do not remove app features unless necessary for security
* Avoid speculative rewrites of the whole app
* Make vulnerable version and secure version easy to compare
* Keep any new feature realistic enough for coursework marking
* The final result must support video demonstration

## Desired immediate next step for Codex

Start by:

1. inspecting the BankWebsite project structure
2. identifying where to add a new XML statement viewer/import page
3. proposing a concrete implementation plan for combining **Path Traversal** and **XML Injection** into one feature inside BankWebsite
4. listing exact files likely to be created or modified

## Quick reference summary

### Already done in CW1

* SQL Injection exploited
* Stored XSS exploited
* Info Exposure / Improper Authorization exploited
* Unrestricted File Upload exploited
* CSRF exploited
* Additional vulnerabilities chosen: Path Traversal and XML Injection

### Current CW2 goal

Build and mitigate one combined BankWebsite feature demonstrating:

* Path Traversal
* XML Injection

Then mitigate the 5 previous vulnerabilities.
