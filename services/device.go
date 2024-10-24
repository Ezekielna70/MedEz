// services/device.go
package services

import (
    "context"
    "errors"
    "github.com/Ezekielna70/Backend/models"
    "cloud.google.com/go/firestore"  // Import Firestore package
    "google.golang.org/api/iterator" // Import iterator for query results
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

    // If the document exists, prepare the data to update
    data := map[string]interface{}{
        "DevUsername": device.DevUsername,
        "DevStatus":   device.DevStatus,
        "DevTime":     device.DevTime,
    }

    // Update the existing document with the given DevID
    _, err = doc.Ref.Set(ctx, data, firestore.MergeAll)
    return err
}
