// services/patient.go
package services

import (
    "context"
    "github.com/Ezekielna70/Backend/models"
    "google.golang.org/api/iterator"
)

func AddPatient(patient models.Patient) error {
    // Create a new document reference which will generate a unique ID
    docRef := client.Collection("patient").NewDoc()

    // Use the generated document ID as CareID
    patient.PatID = docRef.ID

    // Prepare the data with the auto-generated CareID
    data := map[string]interface{}{
        "PatID":       patient.PatID,
        "PatUsername": patient.PatUsername,
        "PatEmail":    patient.PatEmail,
        "PatPassword": patient.PatPassword,
        "PatAge":      patient.PatAge,
    }

    // Save the data with the generated ID
    _, err := docRef.Set(context.Background(), data)
    return err
}

func PatientExists(email string) (bool, error) {
    iter := client.Collection("patient").Where("PatEmail", "==", email).Documents(context.Background())
    defer iter.Stop()

    _, err := iter.Next()
    return err != iterator.Done, nil
}

func GetPatientByEmail(email string) (*models.Patient, error) {
    iter := client.Collection("patient").Where("PatEmail", "==", email).Documents(context.Background())
    defer iter.Stop()
    
    doc, err := iter.Next()
    if err == iterator.Done {
        return nil, nil
    }
    if err != nil {
        return nil, err
    }
    
    var patient models.Patient
    err = doc.DataTo(&patient)
    return &patient, err
}
