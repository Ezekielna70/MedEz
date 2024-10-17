package main

import (
	"fmt"
	"log"

	"backend/add_data"  // Correct import for add_data package
	"backend/get_data"  // Correct import for get_data package
)

func main() {
	// Call the function to add data
	err := add_data.AddData()
	if err != nil {
		log.Fatalf("Failed to add data: %v", err)
	}

	// Call the function to get data based on DeviceID
	deviceID := "12545"  // Example DeviceID
	err = get_data.GetDataByDeviceID(deviceID)
	if err != nil {
		log.Fatalf("Failed to get data: %v", err)
	}

	fmt.Println("All operations completed.")
}
