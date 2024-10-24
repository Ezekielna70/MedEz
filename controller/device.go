// controllers/device.go
package controllers

import (
    "github.com/gofiber/fiber/v2"
    "github.com/Ezekielna70/Backend/models"
    "github.com/Ezekielna70/Backend/services"
    "time"
)

func DeviceStore(c *fiber.Ctx) error {
    // Create a struct to parse the incoming JSON
    var requestData struct {
        DevID       string `json:"dev_id"`
        DevUsername string `json:"dev_username"`
        DevStatus   string `json:"dev_status"`
        DevTime     string `json:"dev_time"`
    }

    if err := c.BodyParser(&requestData); err != nil {
        return c.Status(400).JSON(fiber.Map{
            "error": "Invalid request body",
            "details": err.Error(),
        })
    }

    // Check if DevID is provided
    if requestData.DevID == "" {
        return c.Status(400).JSON(fiber.Map{
            "error": "DevID is required",
        })
    }

    // Parse the time string into time.Time
    devTime, err := time.Parse(time.RFC3339, requestData.DevTime)
    if err != nil {
        return c.Status(400).JSON(fiber.Map{
            "error": "Invalid time format",
            "details": err.Error(),
        })
    }

    // Create the Device struct
    device := models.Device{
        DevID:       requestData.DevID,
        DevUsername: requestData.DevUsername,
        DevStatus:   requestData.DevStatus,
        DevTime:     devTime,
    }

    // Update or create the device document
    if err := services.UpdateDevice(device); err != nil {
        return c.Status(500).JSON(fiber.Map{
            "error": "Failed to store device",
            "details": err.Error(),
        })
    }

    return c.Status(200).JSON(fiber.Map{
        "message": "Device stored successfully",
        "device": device,
    })
}