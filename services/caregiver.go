// services/caregiver.go
package services

import (
    "context"
    "github.com/Ezekielna70/Backend/models"
    "google.golang.org/api/iterator"
)

func AddCaregiver(caregiver models.Caregiver) error {
    // Create a new document reference which will generate a unique ID
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