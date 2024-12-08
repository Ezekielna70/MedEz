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


func CaregiverAddMedicine(c *fiber.Ctx) error {
	log.Printf("Received caregiver add medicine request from: %s", c.IP())

	// Define a struct to parse the incoming request
	var request struct {
		PatID    string `json:"pat_id"`
		Medicine struct {
			MedUsername     string   `json:"med_username"`
			MedDosage       string   `json:"med_dosage"`
			MedFunction     string   `json:"med_function"`
			MedRemaining    int      `json:"med_remaining"`
			ConsumptionTimes []string `json:"consumption_times"`
		} `json:"medicine"`
	}

	// Parse the JSON request body
	if err := c.BodyParser(&request); err != nil {
		log.Printf("Error parsing request body: %v", err)
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid request format",
			"error":   err.Error(),
		})
	}

	// Validate required fields
	if request.PatID == "" || request.Medicine.MedUsername == "" || request.Medicine.MedDosage == "" || request.Medicine.MedFunction == "" {
		log.Printf("Validation failed: missing required fields")
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Missing required fields",
		})
	}

	// Check if patient exists (call to service layer)
	CareCheckPat, err := services.CareCheckPat(request.PatID)
	if err != nil {
		log.Printf("Error checking patient existence: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Internal server error",
		})
	}
	if !CareCheckPat {
		log.Printf("Patient not found with ID: %s", request.PatID)
		return c.Status(http.StatusNotFound).JSON(fiber.Map{
			"status":  "error",
			"message": "Patient not found",
		})
	}

	// Convert request.Medicine to models.Medicine
	medicine := models.Medicine{
		MedUsername:     request.Medicine.MedUsername,
		MedDosage:       request.Medicine.MedDosage,
		MedFunction:     request.Medicine.MedFunction,
		MedRemaining:    request.Medicine.MedRemaining,
		ConsumptionTimes: request.Medicine.ConsumptionTimes,
	}

	// Call the service layer to add the medicine
	medID, err := services.AddMedicineToPatient(request.PatID, medicine)
	if err != nil {
		log.Printf("Error adding medicine to database: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Failed to add medicine",
			"error":   err.Error(),
		})
	}

	log.Printf("Successfully added medicine with ID: %s for patient: %s", medID, request.PatID)
	return c.Status(http.StatusCreated).JSON(fiber.Map{
		"status":  "success",
		"message": "Medicine added successfully",
		"med_id":  medID,
	})
}


// controllers/caregiver.go
func CaregiverGetMedicines(c *fiber.Ctx) error {
    log.Printf("Received caregiver get medicines request from: %s", c.IP())

    // Get the PatID from the URL parameters
    patID := c.Params("pat_id")
    if patID == "" {
        return c.Status(http.StatusBadRequest).JSON(fiber.Map{
            "status":  "error",
            "message": "Patient ID is required",
        })
    }

    // Call the service to get medicines for the patient
    medicines, err := services.GetMedicinesByPatientID(patID)
    if err != nil {
        log.Printf("Error retrieving medicines for patient %s: %v", patID, err)
        return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
            "status":  "error",
            "message": "Failed to retrieve medicines",
        })
    }

    return c.Status(http.StatusOK).JSON(fiber.Map{
        "status":     "success",
        "medicines": medicines,
    })
}

