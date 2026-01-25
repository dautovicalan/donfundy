# Project 18: Online Donation and Fundraising Platform

## Description
An online fundraising platform for managing campaigns and donations. The system uses Hibernate and Spring Data JPA for campaign and donor data, with JdbcTemplate for bulk donation processing and financial reporting. It supports login via Spring MVC and Thymeleaf, along with secure REST API access using JWT tokens. Scheduled tasks automate donor notifications and campaign updates. The platform supports both English and Spanish languages.

## Roles
- **Donor**: Browse campaigns and make donations  
- **Campaign Manager**: Create and manage fundraising campaigns  
- **Administrator**: Monitor funds and overall system health  

## Functional Requirements
- MVC-based login and session management using Thymeleaf  
- Secure REST API with JWT authentication for external clients and SPA usage  
- CRUD operations for campaigns and donations using Hibernate and JPA  
- Bulk donation processing and financial reporting using JdbcTemplate  
- Scheduled tasks for sending notifications and automating campaign updates  
- Full internationalization (i18n) support for English and Spanish  

## Non-functional Requirements
- Secure, role-based authorization and data privacy  
- Responsive and user-friendly user interface  
- Strong test coverage for backend logic and security layers  
- Easily maintainable and extensible system architecture  

## Detailed Task Breakdown (105 hours)

### Setup and Design (15 hours)
- Project and environment setup (5h)  
- Database schema design and JPA entity modeling (5h)  
- Security setup for MVC login and JWT-based REST API (5h)  

### Backend Development (33 hours)
- MVC login and Thymeleaf-based session management (6h)  
- REST API authentication using JWT tokens (5h)  
- Campaign and donation management with Hibernate/JPA (6h)  
- JdbcTemplate implementation for bulk donation processing and reports (5h)  
- Scheduled jobs for notifications and campaign automation (4h)  
- Input validation and error handling (4h)  
- Backend internationalization (i18n) support (3h)  

### Frontend Development (28 hours)
- Login and registration user interface (5h)  
- Donor campaign browsing and donation pages (6h)  
- Campaign Manager dashboard (6h)  
- Administrator monitoring and reporting interface (5h)  
- Multi-language support with Thymeleaf language toggling (6h)  

### Testing and Quality Assurance (18 hours)
- Unit tests for services and security components (6h)  
- Integration tests for MVC controllers and REST APIs (6h)  
- End-to-end tests simulating donor and campaign manager workflows (6h)  

### Documentation and Finalization (11 hours)
- User, administrator, and API documentation (7h)  
- Final system testing and bug fixes (4h)  
