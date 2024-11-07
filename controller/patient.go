package controllers

import (
	"log"
	"net/http"

	"github.com/Ezekielna70/Backend/models"
	"github.com/Ezekielna70/Backend/services"
	"github.com/gofiber/fiber/v2"
)

// PatientSignup handles new patient registration
func PatientSignup(c *fiber.Ctx) error {
	// Log the incoming request
	log.Printf("Received signup request from: %s", c.IP())

	var patient models.Patient

	// Log raw request body for debugging
	log.Printf("Raw request body: %s", string(c.Body()))

	// Parse request body into Patient struct
	if err := c.BodyParser(&patient); err != nil {
		log.Printf("Error parsing request body: %v", err)
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid request format",
			"error":   err.Error(),
		})
	}

	// Log parsed patient data
	log.Printf("Parsed patient data: %+v", patient)

	// Validate required fields
	if patient.PatEmail == "" || patient.PatPassword == "" || patient.PatUsername == "" || patient.DevID == "" {
		log.Printf("Validation failed: missing required fields")
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Missing required fields",
		})
	}

    existsCare, err := services.LookForCaregiver(patient.DevID)
    if err != nil {
        log.Printf("Error checking Dev existence: %v", err)
        return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
            "status":  "error",
            "message": "Internal server error",
            "error":   "Error checking patient existence",
        })
    }
    
    if existsCare {
        log.Printf("Dev exists: %s", patient.DevID)
    } else {
        log.Printf("Dev does not exist for ID: %s", patient.DevID)
        return c.Status(http.StatusNotFound).JSON(fiber.Map{
            "status":  "error",
            "message": "Device not found",
        })
    }
    
	// Check if patient already exists
	exists, err := services.PatientExists(patient.PatEmail)
	if err != nil {
		log.Printf("Error checking patient existence: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Internal server error",
			"error":   "Error checking patient existence",
		})
	}

	if exists {
		log.Printf("Patient already exists with email: %s", patient.PatEmail)
		return c.Status(http.StatusConflict).JSON(fiber.Map{
			"status":  "error",
			"message": "Patient with this email already exists",
		})
	}

	// Attempt to add the new patient
	if err := services.AddPatient(patient); err != nil {
		log.Printf("Error adding patient to database: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Failed to register patient",
			"error":   err.Error(),
		})
	}

	log.Printf("Successfully registered patient with email: %s", patient.PatEmail)
	return c.Status(http.StatusCreated).JSON(fiber.Map{
		"status":  "success",
		"message": "Patient registered successfully",
	})
}

// PatientLogin handles patient authentication
func PatientLogin(c *fiber.Ctx) error {
	log.Printf("Received login request from: %s", c.IP())

	// Define login request structure
	var loginRequest struct {
		Email    string `json:"pat_email"`
		Password string `json:"pat_password"`
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

	// Attempt to get patient by email
	patient, err := services.GetPatientByEmail(loginRequest.Email)
	if err != nil {
		log.Printf("Error retrieving patient data: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Error retrieving patient data",
		})
	}

	// Check if patient exists and verify password
	if patient == nil {
		log.Printf("Login failed: patient not found with email: %s", loginRequest.Email)
		return c.Status(http.StatusUnauthorized).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid credentials",
		})
	}

	// Verify password
	if patient.PatPassword != loginRequest.Password {
		log.Printf("Login failed: invalid password for email: %s", loginRequest.Email)
		return c.Status(http.StatusUnauthorized).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid credentials",
		})
	}

	log.Printf("Login successful for patient: %s", loginRequest.Email)
	return c.Status(http.StatusOK).JSON(fiber.Map{
		"status":  "success",
		"message": "Login successful",
		"data": fiber.Map{
			"pat_id":       patient.PatID,
			"pat_username": patient.PatUsername,
			"pat_email":    patient.PatEmail,
			"pat_age":      patient.PatAge,
		},
	})
}
