// services/caregiver.go
package services

import (
    "context"
    "github.com/Ezekielna70/Backend/models"
    "google.golang.org/api/iterator"
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
	}

	// Save the medicine data with the generated MedID
	_, err := medicineRef.Set(context.Background(), data)
	if err != nil {
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


