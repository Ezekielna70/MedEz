package models

type Medicine struct {
    MedID           string   `json:"med_id" firestore:"MedID"`
    MedStatus       string   `json:"med_status" firestore:"MedStatus"`
    MedUsername     string   `json:"med_username" firestore:"MedUsername"`
    MedDosage       int   `json:"med_dosage" firestore:"MedDosage"`
    MedFunction     string   `json:"med_function" firestore:"MedFunction"`
    MedRemaining    int      `json:"med_remaining" firestore:"MedRemaining"`
    ConsumptionTimes []string `json:"consumption_times" firestore:"ConsumptionTimes"` // List of times as strings
    MedSlot         int        `json:"med_slot" firestore:"MedSlot"`
}

type MedicineResponse struct {
    MedID            string   `json:"med_id"`
    ConsumptionTimes []string `json:"consumption_times"`
    MedRemaining    int      `json:"med_remaining" firestore:"MedRemaining"`
    MedSlot         int        `json:"med_slot" firestore:"MedSlot"`
    MedDosage       int   `json:"med_dosage" firestore:"MedDosage"`
}

type MedicineDecrease struct {
    DevID            string   `json:"devID"`
    MedID            string   `json:"med_id"`
    MedRemaining    int      `json:"med_remaining" firestore:"MedRemaining"`
}
