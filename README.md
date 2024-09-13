# Clock-in application

This project implements an electronic time clock system using a microservices architecture with Java and Spring Boot,
integrated with Keycloak for authentication and authorization.

## Overview

The system allows employees to efficiently and securely record their check-in and check-out times, while also offering
functionality for managers and HR personnel.

### Key Features

- Time clock recording (check-in/check-out)
- Manage extra hours
- Time record history viewing
- Report generation
- User and permission management

## Architecture

The project consists of some microservices, each responsible for a specific set of functionalities:

1. Time clock Service
2. Employee Service
3. User Management Service
4. API Gateway

## Technologies Used

- Java 17
- Spring Cloud (Eureka, Gateway)
- Spring Boot
- Spring Security
- Keycloak
- PostgreSQL
- Docker
- RBAC

## Pre-requisites

- JDK 17
- Maven
- Docker
- Keycloak Server

## Setup and Execution

1. Clone the repository
2. Configure Keycloak
3. Set up necessary environment variables
4. Run `mvn clean install` in the project root
5. Docker Compose: `docker-compose up`