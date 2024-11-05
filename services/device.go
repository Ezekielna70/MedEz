package services

import (
    "context"
    "errors"
    "github.com/Ezekielna70/Backend/models"
    "cloud.google.com/go/firestore"
    "google.golang.org/api/iterator"
)

// UpdateDevice updates an existing device document with the given DevID
func UpdateDevice(device models.Device) error {
    ctx := context.Background()
    
    // Query for the document with the given DevID
    iter := client.Collection("device").Where("DevID", "==", device.DevID).Documents(ctx)
    defer iter.Stop()

    
    doc, err := iter.Next()
    if err == iterator.Done {
        return errors.New("device with the given DevID does not exist")

    }
    if err != nil {
        return err
    }

    // Get current document data
    docData := doc.Data()


    // Check for CareID update
    if device.CareID != "" {
        if existingCareID, exists := docData["CareID"]; exists && existingCareID != nil && existingCareID.(string) != "" {
            return errors.New("CareID already exists for this device")
        }
    }

    // Prepare update data with explicit field mapping
    updateData := map[string]interface{}{
        "DevID":       device.DevID,
        "DevUsername": device.DevUsername,
        "DevStatus":   device.DevStatus,
        "DevTime":     device.DevTime,
    }

    // Only add PatID and CareID if they are provided

    if device.CareID != "" {
        updateData["CareID"] = device.CareID
    }

    // Update the document with all changes
    _, err = doc.Ref.Set(ctx, updateData, firestore.MergeAll)
    return err
}