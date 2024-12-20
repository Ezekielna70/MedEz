// services/caregiver.go
package services

import (
    "context"
    "github.com/Ezekielna70/Backend/models"
    "google.golang.org/api/iterator"
    "fmt"
    "log"
)

func AddCaregiver(caregiver models.Caregiver) error {
   
    docRef := client.Collection("caregiver").NewDoc()

    // Use the generated document ID as CareID
    caregiver.CareID = docRef.ID

    // Prepare the data with the auto-generated CareID
    data := map[string]interface{}{
        "CareID":       caregiver.CareID,
        "CareUsername": caregiver.CareUsername,
        "CareEmail":    caregiver.CareEmail,
        "CarePassword": caregiver.CarePassword,
        "CareAge":      caregiver.CareAge,
    }

    // Save the data with the generated ID
    _, err := docRef.Set(context.Background(), data)
    return err
}

func CaregiverExists(email string) (bool, error) {
    iter := client.Collection("caregiver").Where("CareEmail", "==", email).Documents(context.Background())
    defer iter.Stop()

    _, err := iter.Next()
    return err != iterator.Done, nil
}

func GetCaregiverByEmail(email string) (*models.Caregiver, error) {
    iter := client.Collection("caregiver").Where("CareEmail", "==", email).Documents(context.Background())
    defer iter.Stop()
    
    doc, err := iter.Next()
    if err == iterator.Done {
        return nil, nil
    }
    if err != nil {
        return nil, err
    }
    
    var caregiver models.Caregiver
    err = doc.DataTo(&caregiver)
    return &caregiver, err
}

func CareCheckPat(patID string) (bool, error) {
    iter := client.Collection("patient").Where("PatID", "==", patID).Documents(context.Background())
    defer iter.Stop()

    // Check if any document exists with the given patID
    _, err := iter.Next()
    if err == iterator.Done {
        return false, nil // No document found
    }
    if err != nil {
        return false, err // Some other error occurred
    }

    return true, nil // Document exists
}




func AddMedicineToPatient(patID string, medicine models.Medicine) (string, error) {
    ctx := context.Background()

    // Get a new document reference in the "medicine" subcollection under the specified patient
    medicineRef := client.Collection("patient").Doc(patID).Collection("medicine").NewDoc()

    // Use the auto-generated document ID as MedID
    medicine.MedID = medicineRef.ID

    // Prepare the medicine data to be saved
    data := map[string]interface{}{
        "MedID":           medicine.MedID,
        "MedUsername":     medicine.MedUsername,
        "MedDosage":       medicine.MedDosage,
        "MedFunction":     medicine.MedFunction,
        "MedRemaining":    medicine.MedRemaining,
        "ConsumptionTimes": medicine.ConsumptionTimes,
        "MedSlot":         medicine.MedSlot, // Ensure this is correctly set
        "MedStatus":       "Not Taken",
    }

    log.Printf("Data to Firestore: %+v", data)

    // Save the medicine data with the generated MedID
    _, err := medicineRef.Set(ctx, data)
    if err != nil {
        log.Printf("Error saving medicine to Firestore: %v", err)
        return "", err
    }

    return medicine.MedID, nil
}




func GetMedicinesByPatientID(patID string) ([]models.Medicine, error) {
    var medicines []models.Medicine

    // Get a reference to the "medicine" subcollection for the patient
    iter := client.Collection("patient").Doc(patID).Collection("medicine").Documents(context.Background())
    defer iter.Stop()

    for {
        doc, err := iter.Next()
        if err == iterator.Done {
            break
        }
        if err != nil {
            return nil, err
        }

        var medicine models.Medicine
        err = doc.DataTo(&medicine)
        if err != nil {
            return nil, err
        }
        medicines = append(medicines, medicine)
    }

    return medicines, nil
}

func GetPatientsByDevice(devID string) ([]map[string]interface{}, error) {
	var patients []map[string]interface{}

	iter := client.Collection("patient").Where("DevID", "==", devID).Documents(context.Background())
	defer iter.Stop()

	for {
		doc, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return nil, err
		}

		data := doc.Data()
		delete(data, "PatPassword") // Exclude sensitive data
		patients = append(patients, data)
	}
	return patients, nil
}

func GetPatientsByCaregiverID(careID string) ([]map[string]interface{}, error) {
	var patients []map[string]interface{}

	// Construct the caregiver document path
	caregiverDoc := client.Doc(fmt.Sprintf("caregiver/%s", careID))

	// Fetch the caregiver document snapshot
	docSnapshot, err := caregiverDoc.Get(context.Background())
	if err != nil {
		// Return meaningful errors
		return nil, fmt.Errorf("error retrieving caregiver with ID %s: %v", careID, err)
	}

	// Check if the caregiver document exists
	if !docSnapshot.Exists() {
		return nil, fmt.Errorf("caregiver with ID %s does not exist", careID)
	}

	// Retrieve the devices subcollection
	devicesIter := caregiverDoc.Collection("devices").Documents(context.Background())
	defer devicesIter.Stop()

	for {
		deviceDoc, err := devicesIter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return nil, fmt.Errorf("error retrieving devices for caregiver %s: %v", careID, err)
		}

		// Extract DevID from the device document
		devID, ok := deviceDoc.Data()["DevID"].(string)
		if !ok || devID == "" {
			continue // Skip if DevID is missing or invalid
		}

		// Fetch patients associated with the DevID
		matchedPatients, err := GetPatientsByDevice(devID)
		if err != nil {
			return nil, fmt.Errorf("error retrieving patients for DevID %s: %v", devID, err)
		}

		// Append the matched patients to the result
		patients = append(patients, matchedPatients...)
	}

	return patients, nil
}

func DeleteMedicine(patID, medID string) error {
    // Check if the patient document exists
    patientRef := client.Collection("patient").Doc(patID)
    patientDoc, err := patientRef.Get(context.Background())
    if err != nil {
        if err.Error() == "rpc error: code = NotFound desc = Document not found" {
            return fmt.Errorf("patient with ID %s does not exist", patID)
        }
        return fmt.Errorf("error retrieving patient document: %v", err)
    }

    if !patientDoc.Exists() {
        return fmt.Errorf("patient with ID %s does not exist", patID)
    }

    // Check if the medicine document exists in the patient's "medicine" subcollection
    medicineRef := patientRef.Collection("medicine").Doc(medID)
    medicineDoc, err := medicineRef.Get(context.Background())
    if err != nil {
        if err.Error() == "rpc error: code = NotFound desc = Document not found" {
            return fmt.Errorf("medicine with ID %s does not exist for patient %s", medID, patID)
        }
        return fmt.Errorf("error retrieving medicine document: %v", err)
    }

    if !medicineDoc.Exists() {
        return fmt.Errorf("medicine with ID %s does not exist for patient %s", medID, patID)
    }

    // Perform the deletion if both documents exist
    _, err = medicineRef.Delete(context.Background())
    if err != nil {
        return fmt.Errorf("failed to delete medicine: %v", err)
    }

    return nil
}
