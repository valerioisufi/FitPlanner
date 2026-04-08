# FitPlanner

## Prerequisites
Before downloading the project, please ensure the following are installed on your computer:
- **Java / JDK** (version 21 or higher)
- **Maven** (version 3.9 or higher)

## Getting Started

### 1. Clone and Build
```bash
# Clone the repository
git clone [https://github.com/valerioisufi/FitPlanner.git](https://github.com/valerioisufi/FitPlanner.git)
# Navigate into the project root directory
cd FitPlanner

# Clean and install all dependencies
mvn clean install
```

### 2. Running the Server (Backend)
```bash
# Navigate to the server directory
cd fitplanner-server
# Run the Spring Boot backend
mvn spring-boot:run
```

### 3. Running the Client (Frontend)
```bash
# Navigate to the client directory
cd fitplanner-client
# Run the JavaFX frontend
mvn clean javafx:run
```

## Project Structure
The project is divided into modules to clearly separate responsibilities:
- fitplanner-common: Shared Bean (POJO) classes used by both the client and the server.
- fitplanner-server: RESTful backend developed with Spring Boot.
- fitplanner-client: Desktop user interface developed with JavaFX.