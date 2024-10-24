package routes

import (
    "github.com/Ezekielna70/Backend/controller" // Update to reflect your Go module
    "github.com/gofiber/fiber/v2"
)


func Setup(app *fiber.App) {
    // Caregiver routes
    app.Post("/caregiver/signup", controllers.CaregiverSignup)
    app.Post("/caregiver/login", controllers.CaregiverLogin)

    // Patient routes
    app.Post("/patient/signup", controllers.PatientSignup)
    app.Post("/patient/login", controllers.PatientLogin)

    // Device routes
    //app.Post("/device/store", controllers.DeviceStore)
}
