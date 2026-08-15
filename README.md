# HireTrack — Job Recruitment & Application Tracking Platform


HireTrack is a full-stack recruitment and job application tracking platform designed to connect **candidates, recruiters, and administrators** through a secure and structured hiring workflow.


Candidates can discover jobs and apply with their resumes, while recruiters can manage job postings, review applications, evaluate candidate-job skill matches, and update application statuses. Administrators have centralized control over users, jobs, and applications.


## 🚀 Live Demo


**Live Application:**  
https://hiretrack-app.onrender.com/


---


## ✨ Features


### 🔐 Authentication & Authorization


- User registration and login
- JWT-based authentication
- BCrypt password encryption
- Stateless authentication using Spring Security
- Role-based access control
- Candidate, Recruiter, and Admin roles
- Protected REST APIs
- Authentication and authorization error handling
- Secure logout and token management


---


### 👨‍💻 Candidate Features


- Candidate registration and login
- Browse available job opportunities
- View detailed job information
- Apply for jobs
- Upload resumes
- Supported resume formats:
  - PDF
  - DOC
  - DOCX
- Maximum resume size: 5 MB
- Prevent duplicate applications
- View submitted applications
- Track application status
- View application status history
- View application statistics
- Manage candidate profile


---


### 👔 Recruiter Features


- Recruiter registration and login
- Recruiter dashboard
- Create and manage job postings
- View applications for posted jobs
- View candidate application details
- Access candidate resumes
- Update application status
- Track candidate progress through the hiring pipeline
- View recruiter profile
- Manage recruiter information
- Resume-to-job skill matching


---


### 📊 Resume-to-Job Skill Matching


HireTrack includes a resume matching feature that compares the skills extracted from an uploaded resume against the skills required for a job.


#### Workflow


Candidate uploads resume:


    Resume
       ↓
    Text Extraction
       ↓
    Skill Detection
       ↓
    Compare with Job Requirements
       ↓
    Match Score
@PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")

This ensures that sensitive recruiter and administrator operations cannot be accessed by unauthorized users.

🧰 Tech Stack
Backend
Java
Spring Boot
Spring Security
Spring Data JPA
Hibernate
JWT
Maven
Database
MySQL
Frontend
HTML5
CSS3
JavaScript
Resume Processing
Apache PDFBox
Apache POI
Deployment
Render
📁 Project Structure
hiretrack/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hiretrack/
│   │   │           └── hiretrack/
│   │   │               ├── controller/
│   │   │               ├── service/
│   │   │               ├── repository/
│   │   │               ├── entity/
│   │   │               ├── dto/
│   │   │               └── security/
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── html/
│   │       └── application.properties
│   │
│   ├── test/
│
├── uploads/
│   └── resumes/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
⚙️ Running Locally
Prerequisites

Make sure you have installed:

Java 17+
MySQL
Maven (optional because Maven Wrapper is included)
Git
1. Clone the repository
git clone https://github.com/haripriyap07/HireTrack.git
cd HireTrack
2. Create the MySQL database
CREATE DATABASE hiretrack;
3. Configure the database

Update:

src/main/resources/application.properties

Example:

spring.datasource.url=jdbc:mysql://localhost:3306/hiretrack?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD


spring.jpa.hibernate.ddl-auto=update


server.port=9091
4. Run the application

Windows:

.\mvnw.cmd spring-boot:run

Linux/macOS:

./mvnw spring-boot:run
5. Open the application
http://localhost:9091/
🌐 Deployment

The application is deployed using Render.

Live Application

https://hiretrack-app.onrender.com/

The deployed application provides the same candidate, recruiter, and administrator workflows available in the local environment.

🔑 User Roles
Role	Capabilities
Candidate	Browse jobs, apply, upload resume, track applications
Recruiter	Manage jobs, review applications, evaluate candidates, update statuses
Admin	Manage users, jobs, and applications
📌 Key API Endpoints
Authentication
POST /api/auth/register
POST /api/auth/login
Jobs
GET    /api/jobs
GET    /api/jobs/{id}
POST   /api/jobs
PUT    /api/jobs/{id}
DELETE /api/jobs/{id}
Applications
POST /api/applications/apply
GET  /api/applications
GET  /api/applications/mine
GET  /api/applications/stats
GET  /api/applications/{id}
GET  /api/applications/job/{jobId}
GET  /api/applications/{id}/history
PUT  /api/applications/{id}/status
Resume
/api/resume/**
🧠 Engineering Highlights

Some of the key engineering decisions in HireTrack include:

JWT Authentication

JWT allows the backend to authenticate requests without maintaining server-side sessions.

Role-Based Authorization

Different application capabilities are protected according to user roles.

Service Layer Architecture

Business logic is separated from controllers using dedicated service classes.

DTO-Based API Responses

DTOs are used to control the data exposed through REST APIs rather than directly exposing database entities.

Resume Processing

Uploaded resumes are stored separately and processed to extract text and identify relevant skills.

Application History

Application status changes are tracked to provide visibility into the recruitment lifecycle.

🔮 Future Improvements

Possible future enhancements include:

Email notifications for application status changes
Interview scheduling
Recruiter-candidate messaging
Advanced semantic resume matching
Job recommendations
Cloud-based resume storage
Dockerized deployment
Automated testing and CI/CD
Analytics for recruiter hiring pipelines
👩‍💻 Author

Haripriya P

Robotics & Automation Engineering

Project

HireTrack — Job Recruitment & Application Tracking Platform

⭐ Why HireTrack?

HireTrack demonstrates practical full-stack development through:

REST API development
Spring Boot architecture
JWT authentication
Role-based authorization
Database design
File upload and processing
Resume parsing
Skill matching
Application workflow management
Frontend-backend integration
Cloud deployment
