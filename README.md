# Car Workshop System

## Description
The Car Workshop System is a REST API-based application designed to manage car service reservations efficiently. It allows customers to book appointments, track service progress, and receive invoices. The system follows the Scrum framework for agile development and is implemented using Java and MongoDB.

## Features
- **User Registration & Authentication**
- **Car Service Reservation System**
- **Service Status Tracking**
- **Invoice Generation**
- **Admin Dashboard for Workshop Management**

## Tech Stack
- **Programming Language:** Java
- **Frameworks & Tools:** Spring Boot, JavaFX
- **Database:** MongoDB
- **Build Tool:** Maven
- **Version Control:** Git

## Installation Guide
To set up and run the project, follow these steps:

1. **Install MongoDB**: Ensure MongoDB is installed and running on your system.
2. **Set up the database**: run the setupDatabaseAndTableResSystem.sql file
3. **Build the Project with Maven**:
   - Navigate to the project root directory.
   - Run `mvn lifecycle install`.
4. **Start the REST Server**:
   - Run the backend service.
5. **Run the JavaFX Client**:
   - In Maven, go to `Plugins -> javafx -> javafx:run` to start the frontend.

## Usage
- Users can create an account and book car service appointments.
- Workshop admins can manage reservations and track service progress.
- The system generates invoices upon service completion.

## Contributors
- Petar Nedyalkov (Main Developer)
- Group 11 Team Members:
- Amir Amangeldiyev, Anton Averianov, Mher Avetisyan, Noel Leon Görmez, Chingiz Kuanyshbay, Sadeem Nasr, Zeinelabeddine Zayoun


## License
This project is licensed under [MIT License](LICENSE).
