package models

type Device struct {
    DevID       string      `json:"dev_id" firestore:"DevID"`
    DevUsername string      `json:"dev_username" firestore:"DevUsername"`
    DevStatus   string      `json:"dev_status" firestore:"DevStatus"`
    DevTime     string      `json:"dev_time" firestore:"DevTime"`
}
