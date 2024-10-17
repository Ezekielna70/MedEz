package get_data

import (
	"context"
	"fmt"

	firebase "firebase.google.com/go"
	"google.golang.org/api/iterator"
	"google.golang.org/api/option"
)

func GetDataByDeviceID(deviceID string) error {
	// Set up Firestore using the service account credentials
	ctx := context.Background()

	// Path to your service account credentials JSON file
	sa := option.WithCredentialsFile("protelmedez-c426bcbe188b.json")

	// Initialize Firebase app with credentials
	app, err := firebase.NewApp(ctx, nil, sa)
	if err != nil {
		return fmt.Errorf("failed to initialize Firebase app: %v", err)
	}

	// Get Firestore client
	client, err := app.Firestore(ctx)
	if err != nil {
		return fmt.Errorf("failed to initialize Firestore client: %v", err)
	}
	defer client.Close()

	// Query to find documents where DeviceID matches the specified value
	iter := client.Collection("caregiver").Where("DeviceID", "==", deviceID).Documents(ctx)
	defer iter.Stop()

	// Iterate over the results
	var found bool
	for {
		doc, err := iter.Next()
		if err == iterator.Done {
			break // No more documents
		}
		if err != nil {
			return fmt.Errorf("failed to get document: %v", err)
		}

		// Extract specific fields from the document data
		data := doc.Data()

		// Access individual fields
		username, _ := data["Username"].(string)
		email, _ := data["Email"].(string)
		password, _ := data["Password"].(string)
		relation, _ := data["Relation"].(string)
		patientID, _ := data["PatientID"].(string)

		// Print each field
		fmt.Printf("Found document with DeviceID %s:\n", deviceID)
		fmt.Printf("Username: %s\n", username)
		fmt.Printf("Email: %s\n", email)
		fmt.Printf("Password: %s\n", password)
		fmt.Printf("Relation: %s\n", relation)
		fmt.Printf("PatientID: %s\n", patientID)
		found = true
	}

	// If no document was found, print a message
	if !found {
		fmt.Printf("No document found with DeviceID: %s\n", deviceID)
	}
	return nil
}
