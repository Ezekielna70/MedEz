package models

type Medicine struct {
    MedID           string   `json:"med_id" firestore:"MedID"`
    MedUsername     string   `json:"med_username" firestore:"MedUsername"`
    MedDosage       string   `json:"med_dosage" firestore:"MedDosage"`
    MedFunction     string   `json:"med_function" firestore:"MedFunction"`
    MedRemaining    int      `json:"med_remaining" firestore:"MedRemaining"`
    ConsumptionTimes []string `json:"consumption_times" firestore:"ConsumptionTimes"` // List of times as strings
}
