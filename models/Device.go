package models

import "time"

type Device struct {
    DevID       string    `json:"dev_id" firestore:"DevID"`
    DevUsername string    `json:"dev_username" firestore:"DevUsername"`
    DevStatus   string    `json:"dev_status" firestore:"DevStatus"`
    DevTime     time.Time `json:"dev_time" firestore:"DevTime"`
    //PatID       string    `json:"pat_id" firestore:"PatID"`
    CareID      string     `json:"care_id" firestore:"CareID"`
}