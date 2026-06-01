# CampusBridge

CampusBridge is a college mentorship and community platform designed to connect students, mentors, and administrators. The platform provides mentorship opportunities, discussion forums, resource sharing, placement support, and secure user management.

## Features

### Authentication & Security
- User Registration
- User Login
- JWT Authentication
- Role-Based Access Control (Student, Mentor, Admin)
- Password Encryption using BCrypt
- Spring Security Integration

### User Management
- Student Profiles
- Mentor Profiles
- Profile Updates
- Department & Batch Management

### Dashboard
- Personalized Dashboard
- Community Statistics
- Resource Tracking
- Mentor Connection Overview

### Forum Module
- Create Forum Posts
- View Public Discussions
- Add Comments
- Community Interaction

### Mentor Module
- Mentor Discovery
- Mentor Profiles
- Mentor-Student Connections

### Resource Module
- Educational Resource Sharing
- Learning Material Repository

### Placement Module
- Placement Drive Management
- Placement Stories

### Messaging Module
- User Conversations
- Direct Messaging Support

---

## Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- JWT Authentication
- Maven

### Database
- MySQL

### Development Tools
- IntelliJ IDEA
- Postman
- Git & GitHub

---

## Project Structure

```text
src/main/java/com/mgr/campusbridge
│
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── exception
├── repository
├── security
├── service
└── CampusbridgeApplication.java
```

---

## Implemented APIs

### Authentication
- Register User
- Login User

### Dashboard
- Get Dashboard Data

### Profile
- View Profile
- Update Profile

### Forum
- Create Post
- View Public Posts
- Add Comments

### Mentors
- Get Mentor List

### Resources
- Get Resources

### Placements
- Placement APIs

### Messaging
- Conversation APIs
- Messaging APIs

---

## Testing Status

Successfully tested using Postman:

- User Registration ✅
- User Login ✅
- JWT Authentication ✅
- Dashboard API ✅
- Profile API ✅
- Mentor API ✅
- Forum API ✅
- Resource API ✅
- Database Integration ✅

---

## Database

MySQL database is used for storing:

- Users
- Mentor Profiles
- Forum Posts
- Forum Comments
- Resources
- Messages
- Placement Drives
- Placement Stories

---

## Setup Instructions

### Clone Repository

```bash
git clone https://github.com/shaikmofficial-ai/CampusBridge.git
cd CampusBridge
```

### Configure Database

Create a MySQL database:

```sql
CREATE DATABASE campusbridge;
```

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campusbridge
spring.datasource.username=root
spring.datasource.password=your_password
```

### Run Application

```bash
mvn spring-boot:run
```

Application will run on:

```text
http://localhost:8080
```

---

## Future Enhancements

- Real-time Chat using WebSockets
- File Upload System
- Notification Service
- Placement Analytics
- Admin Dashboard Improvements
- Frontend Integration
- Docker Deployment

---

## Project Status

Backend Development: **85% Complete** ✅

Current Progress:
- Authentication Completed
- Security Completed
- Core APIs Completed
- Database Integration Completed
- API Testing Completed
- Frontend Integration In Progress

---

## Author

**Shaik Abdulla**

CampusBridge - College Mentorship & Community Platform
