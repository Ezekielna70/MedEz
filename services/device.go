package services

import (
    "context"
    "errors"
    "github.com/Ezekielna70/Backend/models"
    "cloud.google.com/go/firestore"
    "google.golang.org/api/iterator"
    "log"
    "fmt"
)

// UpdateDevice updates an existing device document with the given DevID and manages CareID in nested collections
func UpdateDevice(device models.Device) error {
    ctx := context.Background()

    // Query for the device document with the given DevID
    iter := client.Collection("device").Where("DevID", "==", device.DevID).Documents(ctx)
    defer iter.Stop()

    doc, err := iter.Next()
    if err == iterator.Done {
        return errors.New("device with the given DevID does not exist")
    }
    if err != nil {
        return err
    }

    // Before performing any updates, validate the CareID if provided
    if device.CareID != "" {
        // Check if the CareID exists in the caregiver collection
        caregiverIter := client.Collection("caregiver").Where("CareID", "==", device.CareID).Documents(ctx)
        defer caregiverIter.Stop()

        _, err := caregiverIter.Next()
        if err == iterator.Done {
            return errors.New("CareID does not exist in the caregiver collection")
        }
        if err != nil {
            return err
        }

        // Check if the CareID already exists in the Caregivers subcollection
        caregiverIter = doc.Ref.Collection("Caregivers").Where("CareID", "==", device.CareID).Documents(ctx)
        defer caregiverIter.Stop()

        _, err = caregiverIter.Next()
        if err != iterator.Done {
            return errors.New("CareID already exists for this device in Caregivers collection")
        }

        // If CareID does not exist, add it to the Caregivers subcollection
        _, err = doc.Ref.Collection("Caregivers").Doc(device.CareID).Set(ctx, map[string]interface{}{"CareID": device.CareID})
        if err != nil {
            return err
        }

        // Add device.DevID to the devices subcollection in caregiver
        caregiverDoc := client.Collection("caregiver").Doc(device.CareID)
        _, err = caregiverDoc.Collection("devices").Doc(device.DevID).Set(ctx, map[string]interface{}{"DevID": device.DevID})
        if err != nil {
            return err
        }
    }

    // Prepare update data for the device document fields
    updateData := map[string]interface{}{
        "DevID":       device.DevID,
        "DevUsername": device.DevUsername,
        "DevStatus":   device.DevStatus,
        "DevTime":     device.DevTime,
    }

    // Update device document with provided fields
    _, err = doc.Ref.Set(ctx, updateData, firestore.MergeAll)
    if err != nil {
        return err
    }

    return nil
}

func GetMedicinesByDeviceID(devID string) ([]models.MedicineResponse, error) {
    ctx := context.Background()

    patientIter := client.Collection("patient").Where("DevID", "==", devID).Limit(1).Documents(ctx)
    defer patientIter.Stop()

    patientDoc, err := patientIter.Next()
    if err == iterator.Done {
        return nil, errors.New("no patient found with the given DevID")
    }
    if err != nil {
        return nil, fmt.Errorf("error fetching patient: %v", err)
    }

    patID := patientDoc.Ref.ID
    log.Printf("Found patient with PatID: %s", patID)

    medicinesIter := client.Collection("patient").Doc(patID).Collection("medicine").Documents(ctx)
    defer medicinesIter.Stop()

    var medicineResponses []models.MedicineResponse
    for {
        medDoc, err := medicinesIter.Next()
        if err == iterator.Done {
            break
        }
        if err != nil {
            log.Printf("Error fetching medicine document: %v", err)
            return nil, fmt.Errorf("error fetching medicine: %v", err)
        }

        var medicine models.Medicine
        if err := medDoc.DataTo(&medicine); err != nil {
            log.Printf("Error mapping medicine document to model: %v", err)
            return nil, fmt.Errorf("error mapping medicine document: %v", err)
        }

        // Map to the MedicineResponse struct
        response := models.MedicineResponse{
            MedID:            medicine.MedID,              // Assuming "MedID" is the correct field
            ConsumptionTimes: medicine.ConsumptionTimes,   // Assuming "ConsumptionTimes" is an array of strings
        }

        medicineResponses = append(medicineResponses, response)
    }

    if len(medicineResponses) == 0 {
        log.Printf("No medicines found for patient with PatID: %s", patID)
        return nil, nil // Return an empty slice instead of nil if you prefer
    }

    return medicineResponses, nil
}


