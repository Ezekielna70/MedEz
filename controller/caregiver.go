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
	log.Printf("Received caregiver signup request from: %s", c.IP())

	var caregiver models.Caregiver

	// Parse request body into Caregiver struct
	if err := c.BodyParser(&caregiver); err != nil {
		log.Printf("Error parsing request body: %v", err)
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid request format",
			"error":   err.Error(),
		})
	}

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
		})
	}

	if exists {
		log.Printf("Caregiver already exists with email: %s", caregiver.CareEmail)
		return c.Status(http.StatusConflict).JSON(fiber.Map{
			"status":  "error",
			"message": "Caregiver with this email already exists",
		})
	}

	// Add the new caregiver
	if err := services.AddCaregiver(caregiver); err != nil {
		log.Printf("Error adding caregiver to database: %v", err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Failed to register caregiver",
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

	var loginRequest struct {
		Email    string `json:"care_email"`
		Password string `json:"care_password"`
	}

	// Parse request body
	if err := c.BodyParser(&loginRequest); err != nil {
		log.Printf("Error parsing login request: %v", err)
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid request format",
		})
	}

	// Validate required fields
	if loginRequest.Email == "" || loginRequest.Password == "" {
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Email and password are required",
		})
	}

	// Attempt to get caregiver by email
	caregiver, err := services.GetCaregiverByEmail(loginRequest.Email)
	if err != nil || caregiver == nil {
		log.Printf("Login failed: caregiver not found with email: %s", loginRequest.Email)
		return c.Status(http.StatusUnauthorized).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid credentials",
		})
	}

	// Verify password
	if caregiver.CarePassword != loginRequest.Password {
		return c.Status(http.StatusUnauthorized).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid credentials",
		})
	}

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

// CaregiverAddMedicine adds medicine to a patient
func CaregiverAddMedicine(c *fiber.Ctx) error {
	log.Printf("Received caregiver add medicine request from: %s", c.IP())

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

	// Parse request body
	if err := c.BodyParser(&request); err != nil {
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Invalid request format",
		})
	}

	// Validate required fields
	if request.PatID == "" || request.Medicine.MedUsername == "" || request.Medicine.MedDosage == "" || request.Medicine.MedFunction == "" {
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Missing required fields",
		})
	}

	// Check if patient exists
	exists, err := services.CareCheckPat(request.PatID)
	if err != nil || !exists {
		return c.Status(http.StatusNotFound).JSON(fiber.Map{
			"status":  "error",
			"message": "Patient not found",
		})
	}

	medicine := models.Medicine{
		MedUsername:     request.Medicine.MedUsername,
		MedDosage:       request.Medicine.MedDosage,
		MedFunction:     request.Medicine.MedFunction,
		MedRemaining:    request.Medicine.MedRemaining,
		ConsumptionTimes: request.Medicine.ConsumptionTimes,
	}

	// Add medicine to the patient
	medID, err := services.AddMedicineToPatient(request.PatID, medicine)
	if err != nil {
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Failed to add medicine",
		})
	}

	return c.Status(http.StatusCreated).JSON(fiber.Map{
		"status":  "success",
		"message": "Medicine added successfully",
		"med_id":  medID,
	})
}

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

func CaregiverGetAll(c *fiber.Ctx) error {
	log.Printf("Received request to get patients linked to caregiver devices for care_id: %s", c.Params("care_id"))

	// Get the care_id from the URL
	careID := c.Params("care_id")
	if careID == "" {
		return c.Status(http.StatusBadRequest).JSON(fiber.Map{
			"status":  "error",
			"message": "Caregiver ID is required",
		})
	}

	// Call the service layer to get patients linked to the caregiver
	patients, err := services.GetPatientsByCaregiverID(careID)
	if err != nil {
		log.Printf("Error retrieving patients for care_id %s: %v", careID, err)
		return c.Status(http.StatusInternalServerError).JSON(fiber.Map{
			"status":  "error",
			"message": "Failed to retrieve patients",
		})
	}

	return c.Status(http.StatusOK).JSON(fiber.Map{
		"status":   "success",
		"patients": patients,
	})
}




