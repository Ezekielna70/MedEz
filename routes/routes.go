package routes

import (
    "github.com/Ezekielna70/Backend/controller" // Update to reflect your Go module
    "github.com/gofiber/fiber/v2"
)


func Setup(app *fiber.App) {
    // Caregiver routes
    app.Post("/caregiver/signup", controllers.CaregiverSignup)
    app.Post("/caregiver/login", controllers.CaregiverLogin)
    app.Post("/caregiver/addmed", controllers.CaregiverAddMedicine)
    app.Get("/caregiver/getmed/:pat_id", controllers.CaregiverGetMedicines) // Add :pat_id here

    // Patient routes
    app.Post("/patient/signup", controllers.PatientSignup)
    app.Post("/patient/login", controllers.PatientLogin)
    //app.Post("/patient/add/caregiver")
    //app.Post("/patient/add/device")

    // Device routes
    app.Post("/device/store", controllers.DeviceStore)

    // Schedule routes
    app.Get("/schedule/fromdevice", controllers.DeviceStore)
    //app.Post("/schedule/fromapp")
}
