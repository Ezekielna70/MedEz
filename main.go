package main

import (
    "github.com/Ezekielna70/Backend/routes"
    "github.com/Ezekielna70/Backend/services"
    "github.com/gofiber/fiber/v2"
    "github.com/gofiber/fiber/v2/middleware/cors"
    "log"
    "os"
)

func main() {
    app := fiber.New()

    // Add CORS middleware
    app.Use(cors.New(cors.Config{
        AllowOrigins: "*",
        AllowHeaders: "Origin, Content-Type, Accept",
    }))

    // Initialize Firestore
    projectID := os.Getenv("FIREBASE_PROJECT_ID")
    if projectID == "" {
        projectID = "protelmedez"
    }
    
    // Initialize Firestore with error handling
    if err := services.InitFirestore(projectID); err != nil {
        log.Fatalf("Failed to initialize Firestore: %v", err)
    }

    // Setup routes
    routes.Setup(app)

    // Get port from environment variable or default to 8080
    port := os.Getenv("PORT")
    if port == "" {
        port = "8080"
    }

    // Start server
    log.Printf("Server starting on port %s", port)
    if err := app.Listen(":" + port); err != nil {
        log.Fatalf("Error starting server: %v", err)
    }
}