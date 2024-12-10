package routes

import (
    "github.com/Ezekielna70/Backend/controller" 
    "github.com/gofiber/fiber/v2"
)


func Setup(app *fiber.App) {
    // Caregiver routes
    app.Post("/caregiver/signup", controllers.CaregiverSignup)
    app.Post("/caregiver/login", controllers.CaregiverLogin)
    app.Post("/caregiver/addmed", controllers.CaregiverAddMedicine)
    app.Get("/caregiver/getall/:care_id", controllers.CaregiverGetAll)
    app.Get("/caregiver/getmed/:pat_id", controllers.CaregiverGetMedicines) // Add :pat_id here

    // Patient routes
    app.Post("/patient/signup", controllers.PatientSignup)
    app.Post("/patient/login", controllers.PatientLogin)


    // Device routes
    app.Post("/device/store", controllers.DeviceStore)
    app.Get("/getMed/:dev_id", controllers.GetMedByDevice)

    // Schedule routes
    //app.Get("/schedule/fromdevice", controllers.DeviceStore)

}
