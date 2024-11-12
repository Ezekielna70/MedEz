package models

type Caregiver struct {
    CareID       string `json:"care_id" firestore:"CareID"`
    CareUsername string `json:"care_username" firestore:"CareUsername"`
    CareEmail    string `json:"care_email" firestore:"CareEmail"`
    CarePassword string `json:"care_password" firestore:"CarePassword"`
    CareAge      int    `json:"care_age" firestore:"CareAge"`
}