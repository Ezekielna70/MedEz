package add_data

import (
	"context"
	"fmt"

	firebase "firebase.google.com/go"
	"google.golang.org/api/option"
)

func AddData() error {
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

	// Data to add to Firestore under the "caregiver" collection
	data := map[string]interface{}{
		"DeviceID":  "1313",
		"Email":     "hell@gmail.com",
		"Password":  "hello",
		"Relation":  "Nobody",
		"Username":  "Help",
		"PatientID": "NiHao",
	}

	// Add data to Firestore collection "caregiver" with auto-generated document ID
	_, _, err = client.Collection("caregiver").Add(ctx, data)
	if err != nil {
		return fmt.Errorf("failed to add data: %v", err)
	}

	// Print success message
	fmt.Println("Successfully added data to Firestore.")
	return nil
}
