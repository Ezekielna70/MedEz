package models

type Patient struct {
    PatID       string    `json:"pat_id" firestore:"PatID"`
    PatUsername string `json:"pat_username" firestore:"PatUsername"`
    PatEmail    string `json:"pat_email" firestore:"PatEmail"`
    PatPassword string `json:"pat_password" firestore:"PatPassword"`
    PatAge      int    `json:"pat_age" firestore:"PatAge"`
    CareID      string `json:"care_id" firestore:"CareID"`
}
