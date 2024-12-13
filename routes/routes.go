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
    app.Post("/device/store", controllers.DeviceStore)
    app.Delete("/caregiver/deletemed/:pat_id/:med_id", controllers.DeleteMed)

    // Patient routes
    app.Post("/patient/signup", controllers.PatientSignup)
    app.Post("/patient/login", controllers.PatientLogin)


    // Device routes
    app.Get("/getMed/:dev_id", controllers.GetMedByDevice)
    app.Post("/decreasemed", controllers.UpdateMedRemaining)

    // Schedule routes
    //app.Get("/schedule/fromdevice", controllers.DeviceStore)

}
