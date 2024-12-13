package controllers

import (
    "github.com/gofiber/fiber/v2"
    "github.com/Ezekielna70/Backend/models"
    "github.com/Ezekielna70/Backend/services"
    "time"
    "log"
    "net/http"
)

func DeviceStore(c *fiber.Ctx) error {
    // Create a struct to parse the incoming JSON with all possible fields
    var requestData struct {
        DevID       string `json:"dev_id"`
        DevUsername string `json:"dev_username"`
        DevStatus   string `json:"dev_status"`
        DevTime     string `json:"dev_time"`
        //PatID       string `json:"pat_id,omitempty"`  // Optional
        CareID      string `json:"care_id"` // Optional
    }

    if err := c.BodyParser(&requestData); err != nil {
        return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
            "error":   "Invalid request body",
            "details": err.Error(),
        })
    }

    // Validate required fields
    if requestData.DevID == "" {
        return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
            "error": "DevID is required",
        })
    }

    if requestData.DevUsername == "" {
        return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
            "error": "DevUsername is required",
        })
    }

    if requestData.DevStatus == "" {
        return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
            "error": "DevStatus is required",
        })
    }

    // Parse time
    var devTime time.Time
    //var err error
    if requestData.DevTime != "" {
        // Try parsing with multiple formats
        formats := []string{
            time.RFC3339,
            "2006-01-02T15:04:05Z07:00",
            "2006-01-02 15:04:05",
            time.RFC3339Nano,
        }

        parsed := false
        for _, format := range formats {
            if t, parseErr := time.Parse(format, requestData.DevTime); parseErr == nil {
                devTime = t
                parsed = true
                break
            }
        }

        if !parsed {
            devTime = time.Now() // Use current time if parsing fails
        }
    } else {
        devTime = time.Now() // Use current time if not provided
    }

    // Create Device struct with all fields
    device := models.Device{
        DevID:       requestData.DevID,
        DevUsername: requestData.DevUsername,
        DevStatus:   requestData.DevStatus,
        DevTime:     devTime,
        //PatID:       requestData.PatID,   // Include PatID if provided
        CareID:      requestData.CareID,  // Include CareID if provided
    }

    // Update device
    if err := services.UpdateDevice(device); err != nil {

        if err.Error() == "CareID already exists for this device" {
            return c.Status(fiber.StatusConflict).JSON(fiber.Map{
                "error": "CareID already exists for this device",
            })
        }
        return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
            "error":   "Failed to store device",
            "details": err.Error(),
        })
    }

    // Return success response with all fields
    response := fiber.Map{
        "dev_id":       device.DevID,
        "dev_username": device.DevUsername,
        "dev_status":   device.DevStatus,
        "dev_time":     device.DevTime.Format(time.RFC3339),
    }

    // Only include PatID and CareID in response if they were provided
    if device.CareID != "" {
        response["care_id"] = device.CareID
    }

    return c.Status(fiber.StatusOK).JSON(fiber.Map{
        "message": "Device stored successfully",
        "device":  response,
    })
}

func GetMedByDevice(c *fiber.Ctx) error {
    log.Printf("Received request to get medicines for device from: %s", c.IP())

    // Get the DevID from the URL parameters
    devID := c.Params("dev_id")
    if devID == "" {
        return c.Status(http.StatusBadRequest).JSON(fiber.Map{
            "status":  "error",
            "message": "Device ID is required",
        })
    }

    // Call the service to get medicines for the device
    medicines, err := services.GetMedicinesByDeviceID(devID)
    if err != nil {
        log.Printf("Error retrieving medicines for device %s: %v", devID, err)
        return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
            "status":  "error",
            "message": err.Error(),
        })
    }

    if medicines == nil {
        return c.Status(http.StatusOK).JSON(fiber.Map{
            "status":    "success",
            "message":   "No medicines found",
            "medicines": []models.MedicineResponse{}, // Return empty array for consistency
        })
    }

    return c.Status(http.StatusOK).JSON(fiber.Map{
        "status":    "success",
        "medicines": medicines,
    })
}

func UpdateMedRemaining(c *fiber.Ctx) error {
    log.Printf("Received request to update med_remaining from: %s", c.IP())

    // Parse the request body
    var requestBody struct {
        DevID        string `json:"dev_id"`
        MedID        string `json:"med_id"`
        MedRemaining int    `json:"med_remaining"`
    }

    if err := c.BodyParser(&requestBody); err != nil {
        return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
            "status":  "error",
            "message": "Invalid request body",
            "details": err.Error(),
        })
    }

    // Debug log the parsed body
    log.Printf("Parsed Request Body: %+v", requestBody)

    // Validate required fields
    if requestBody.DevID == "" {
        log.Println("Missing dev_id in request")
        return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
            "status":  "error",
            "message": "Device ID is required",
        })
    }

    if requestBody.MedID == "" {
        log.Println("Missing med_id in request")
        return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
            "status":  "error",
            "message": "Medicine ID is required",
        })
    }

    if requestBody.MedRemaining == 0 {
        log.Println("Missing med_remaining or value is zero")
        return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
            "status":  "error",
            "message": "Medicine remaining value is required",
        })
    }

    // Call the service function to update med_remaining
    err := services.UpdateMedicineRemaining(requestBody.DevID, requestBody.MedID, requestBody.MedRemaining)
    if err != nil {
        log.Printf("Error updating med_remaining: %v", err)
        return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
            "status":  "error",
            "message": err.Error(),
        })
    }

    return c.Status(fiber.StatusOK).JSON(fiber.Map{
        "status":  "success",
        "message": "med_remaining updated successfully",
    })
}
