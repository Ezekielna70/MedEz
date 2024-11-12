// services/firebase.go
package services

import (
    "context"
    "cloud.google.com/go/firestore"
    firebase "firebase.google.com/go/v4"  // Changed this import
    "google.golang.org/api/option"

)

var client *firestore.Client

func InitFirestore(projectID string) error {
    ctx := context.Background()
    
    // Initialize Firebase App
    conf := &firebase.Config{ProjectID: projectID}
    
    // Use absolute path to your credentials file
    credentialsFile := "D:/Kuliah_ITS/Semester_7/PROTEL/Backend/protelmedez-c426bcbe188b.json"
    app, err := firebase.NewApp(ctx, conf, option.WithCredentialsFile(credentialsFile))
    if err != nil {
        return err
    }

    // Initialize Firestore Client
    firestoreClient, err := app.Firestore(ctx)
    if err != nil {
        return err
    }
    
    client = firestoreClient
    return nil
}