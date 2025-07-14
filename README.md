# DataShift: Automated Compliance Evidence Extraction

DataShift is a full-stack web application designed to streamline and automate the process of finding and managing evidence for compliance controls within large sets of documents. It allows users to define controls and associated keywords via an Excel upload, and then automatically scans uploaded PDF documents to identify and report on relevant evidence. This significantly reduces manual effort in auditing and compliance verification.

## Key Features

-   **Control Management**: Define compliance controls, descriptions, and keywords by uploading a single Excel file.
-   **PDF Document Analysis**: Upload multiple PDF documents, and the system will automatically scan them for predefined keywords.
-   **Evidence Mapping**: Intelligently maps found evidence (keywords) back to specific controls, providing document names and exact page numbers.
-   **Interactive Frontend**: A user-friendly Angular interface for uploading files, viewing results, and interacting with documents.
-   **Embedded PDF Viewer**: An integrated, feature-rich PDF viewer (`ngx-extended-pdf-viewer`) to display source documents, making it easy to verify evidence in context.
-   **Secure Access**: A straightforward user registration and login system to protect access to the application.
-   **RESTful API**: A robust Spring Boot backend providing a clear and extensible API for all functionalities.

## How It Works

1.  **Define Controls**: The user uploads an Excel spreadsheet containing a list of compliance controls (e.g., `Control ID`, `Control Name`, `Description`). The system processes this file to establish the basis for the audit.
2.  **Upload Documents**: The user uploads one or more PDF documents (e.g., policy documents, system reports, audit logs) that need to be checked for evidence.
3.  **Process & Extract**: The user triggers the analysis. The backend scans the PDFs, searching for keywords associated with the defined controls.
4.  **View Results**: The frontend displays a comprehensive dashboard showing which controls have evidence, in which documents, and on which pages. Users can click to view the document directly in the browser and verify the findings.

## Tech Stack

-   **Backend**:
    -   Java
    -   Spring Boot (Web, Data JPA)
    -   Apache POI (for Excel processing)
    -   Maven
-   **Frontend**:
    -   Angular
    -   TypeScript
    -   `ngx-extended-pdf-viewer`
    -   HTML5 / CSS3
-   **Database**:
    -   Any relational database supported by JPA (e.g., PostgreSQL, MySQL, H2).

## Getting Started

### Prerequisites

-   JDK 17 or later
-   Maven 3.8+
-   Node.js and npm
-   An SQL database server (e.g., PostgreSQL)

### Backend Setup (`data_shift` directory)

1.  Clone the repository.
2.  Navigate to the `data_shift` directory.
3.  Update the `src/main/resources/application.properties` file with your database connection details (URL, username, password).
4.  Run the application:
    ```bash
    mvn spring-boot:run
    ```
5.  The backend will be running on `http://localhost:8080`.

### Frontend Setup (`data_shift_web` directory)

1.  Navigate to the `data_shift_web` directory.
2.  Install the dependencies:
    ```bash
    npm install
    ```
3.  Run the development server:
    ```bash
    ng serve
    ```
4.  The frontend will be available at `http://localhost:4200`.

## API Endpoints

The backend exposes several REST endpoints for the frontend to consume.

-   `POST /addUser`: Register a new user.
-   `POST /loginUser`: Authenticate a user.
-   `POST /api/data-shift/upload`: Upload a PDF file.
-   `DELETE /api/data-shift/delete`: Delete an uploaded PDF file.
-   `POST /data/path`: Upload and process the controls Excel file.
-   `POST /data/pdf`: Trigger data extraction for a specific uploaded PDF.
-   `GET /main-data`: Fetch the main processed data for display.
-   `GET /extracted-data`: Fetch raw extracted data points.

---

This project serves as a powerful tool for any organization looking to enhance its compliance and auditing workflow through automation.
