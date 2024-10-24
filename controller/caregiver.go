package controllers

import (
	"log"
	"net/http"

	"github.com/Ezekielna70/Backend/models"
	"github.com/Ezekielna70/Backend/services"
	"github.com/gofiber/fiber/v2"
)

// CaregiverSignup handles new caregiver registration
func CaregiverSignup(c *fiber.Ctx) error {
	// Log the incoming request
	log.Printf("Received caregiver signup request from: %s", c.IP())

	var caregiver models.Caregiver

	// Log raw request body for debugging
	log.Printf("Raw request body: %s", string(c.Body()))

	// Parse request body into Caregiver struct
	if err := c.BodyParser(&caregiver); err != nil {
		log.Printf("Error parsing request body: %v", err)
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid request format",
			"error":   err.Error(),
		})
	}

	// Log parsed caregiver data
	log.Printf("Parsed caregiver data: %+v", caregiver)

	// Validate required fields
	if caregiver.CareEmail == "" || caregiver.CarePassword == "" || caregiver.CareUsername == "" {
		log.Printf("Validation failed: missing required fields")
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Missing required fields",
		})
	}

	// Check if caregiver already exists
	exists, err := services.CaregiverExists(caregiver.CareEmail)
	if err != nil {
		log.Printf("Error checking caregiver existence: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Internal server error",
			"error":   "Error checking caregiver existence",
		})
	}

	if exists {
		log.Printf("Caregiver already exists with email: %s", caregiver.CareEmail)
		return c.Status(http.StatusConflict).JSON(fiber.Map{
			"status":  "error",
			"message": "Caregiver with this email already exists",
		})
	}

	// Attempt to add the new caregiver
	if err := services.AddCaregiver(caregiver); err != nil {
		log.Printf("Error adding caregiver to database: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Failed to register caregiver",
			"error":   err.Error(),
		})
	}

	log.Printf("Successfully registered caregiver with email: %s", caregiver.CareEmail)
	return c.Status(http.StatusCreated).JSON(fiber.Map{
		"status":  "success",
		"message": "Caregiver registered successfully",
	})
}

// CaregiverLogin handles caregiver authentication
func CaregiverLogin(c *fiber.Ctx) error {
	log.Printf("Received caregiver login request from: %s", c.IP())

	// Define login request structure
	var loginRequest struct {
		Email    string `json:"care_email"`
		Password string `json:"care_password"`
	}

	// Log raw request body for debugging
	log.Printf("Raw login request body: %s", string(c.Body()))

	// Parse request body
	if err := c.BodyParser(&loginRequest); err != nil {
		log.Printf("Error parsing login request: %v", err)
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid request format",
			"error":   err.Error(),
		})
	}

	// Validate required fields
	if loginRequest.Email == "" || loginRequest.Password == "" {
		log.Printf("Login validation failed: missing credentials")
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Email and password are required",
		})
	}

	// Attempt to get caregiver by email
	caregiver, err := services.GetCaregiverByEmail(loginRequest.Email)
	if err != nil {
		log.Printf("Error retrieving caregiver data: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Error retrieving caregiver data",
		})
	}

	// Check if caregiver exists and verify password
	if caregiver == nil {
		log.Printf("Login failed: caregiver not found with email: %s", loginRequest.Email)
		return c.Status(http.StatusUnauthorized).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid credentials",
		})
	}

	// Verify password
	if caregiver.CarePassword != loginRequest.Password {
		log.Printf("Login failed: invalid password for email: %s", loginRequest.Email)
		return c.Status(http.StatusUnauthorized).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid credentials",
		})
	}

	log.Printf("Login successful for caregiver: %s", loginRequest.Email)
	return c.Status(http.StatusOK).JSON(fiber.Map{
		"status":  "success",
		"message": "Login successful",
		"data": fiber.Map{
			"care_id":       caregiver.CareID,
			"care_username": caregiver.CareUsername,
			"care_email":    caregiver.CareEmail,
			"care_age":      caregiver.CareAge,
		},
	})
}
