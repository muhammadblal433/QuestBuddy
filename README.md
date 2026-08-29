# QuestBuddy

A comprehensive quest, task, and trip management application built as a group project for **CS 3090** at **Iowa State University**.

## About

QuestBuddy is a full-stack web application that helps users organize, track, and complete tasks, events, and trips collaboratively. The platform features user authentication, real-time notifications via WebSocket, trip management with member invitations, task scheduling, calendar events, and integrated payment processing for premium features.

## Demo

Watch the project demo: [YouTube Video](https://www.youtube.com/watch?v=Sk_AcKkCVSI)

## Features

- **User Management** - Registration, login, profile management with email/username authentication
- **Task Management** - Create, update, delete, and track tasks with due dates and status
- **Trip Planning** - Organize trips with collaborative member management and invitations
- **Event Scheduling** - Calendar events with date/time tracking and trip-specific events
- **Real-time Notifications** - WebSocket-powered notification system with event-driven updates
- **Direct Messaging** - Private messaging between users with message history
- **Friendship System** - Friend requests, connections, and blocked user management
- **Premium Billing** - Stripe integration for premium subscriptions
- **Image Handling** - Upload and manage images via multipart requests
- **Responsive Frontend** - Mobile and desktop support with Android integration

## Tech Stack

### Backend
- **Framework:** Spring Boot 3.4.3
- **Language:** Java 17
- **Build Tool:** Maven (POM-based dependency management)
- **Database:** MySQL / MariaDB (Remote hosted at ISU)
- **ORM:** JPA/Hibernate with Spring Data
- **Real-time:** WebSocket (STOMP protocol) for notifications
- **Authentication:** Spring Security with BCrypt password hashing
- **Payment:** Stripe API integration
- **API Documentation:** SpringDoc OpenAPI (Swagger UI)

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Styling and responsive design
- **JavaScript** - Client-side interactivity
- **Android** - Native Android app support (Volley HTTP library)

### Configuration & Infrastructure
- **YAML/XML** - Spring Boot configuration files
- **SQL** - Database schema and JPA queries
- **Properties** - Environment-specific configuration
- **Docker/CI-CD** - Deployment ready (AWS server: coms-3090-026.class.las.iastate.edu)

## Project Structure

```
QuestBuddy/
├── Backend/                          # Spring Boot REST API
│   ├── src/main/java/com/questbuddy/
│   │   ├── billing/                  # Stripe payment processing
│   │   ├── calendar/                 # Event management
│   │   ├── friends/                  # Friendship system
│   │   ├── messages/                 # Direct messaging
│   │   ├── notification/             # Real-time notifications
│   │   ├── task/                     # Task CRUD operations
│   │   ├── trip/                     # Trip planning & management
│   │   ├── tripmember/               # Trip membership & invites
│   │   ├── user/                     # User authentication & profiles
│   │   └── events/                   # Event handling
│   ├── pom.xml                       # Maven dependencies
│   └── src/main/resources/
│       └── application.properties    # Database & server config
├── Frontend/                         # HTML/CSS/JavaScript UI
│   └── AndroidExample/               # Android native app
└── Experiments/                      # Learning modules & tutorials
```

## Key API Endpoints (Sample)

- `POST /api/v*/users/auth/signup` - User registration
- `POST /api/v*/users/auth/login` - User login
- `GET/POST /api/v*/tasks` - Task CRUD
- `GET/POST /api/v*/trips` - Trip management
- `GET/POST /api/v*/events` - Event scheduling
- `GET/POST /api/v*/notifications` - Notification handling
- `POST /api/v15/payments/checkout/premium/{userId}` - Premium subscription
- `WS /ws/notifications` - WebSocket notifications

## Team Contributions

- **Muhammad Blal** - Backend development + Full-stack frontend integration
- **Ayaan Syed** - Backend development (database, APIs, services)
- **Aparneesh Patil** - Frontend development (UI/UX, Android integration)
- **Aniroop Naladala** - Frontend development (Android, responsive design)

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL/MariaDB server
- Node.js (optional, for frontend tools)

### Backend Setup

```bash
cd Backend
mvn clean install
mvn spring-boot:run
```

Server runs on `http://localhost:8080`

### Database Configuration

Update `Backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://[HOST]:3306/questbuddy
spring.datasource.username=[USER]
spring.datasource.password=[PASSWORD]
spring.jpa.hibernate.ddl-auto=update
```

### Frontend Setup

1. Open `Frontend/` in a web browser or
2. Build the Android app from `Frontend/AndroidExample/`

## Notes

- This is an academic project completed for CS 3090 at Iowa State University
- Database is hosted on ISU's course server
- Stripe keys are configured for test mode
- WebSocket notifications require an active connection to `/ws/notifications`
- All endpoints support CORS for multi-platform access

## Learning Resources

See `/Experiments/` directory for individual team member learning tutorials on:
- Spring Boot fundamentals
- JPA one-to-one relationships
- WebSocket implementation
- Image handling
- Android Volley networking

---

**Last Updated:** August 2026  
**Status:** Complete - Submitted for CS 3090
