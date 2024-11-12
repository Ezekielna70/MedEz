package models
import "time"

type Schedule struct {
    ScheID      string    	`json:"sche_id" firestore:"ScheID"`
    ScheTime 	string    	`json:"sche_time" firestore:"ScheTime"`
	ScheFreq	string 		`json:"sche_freq" firestore:"ScheFreq"`
	ScheDate	time.Time	`json:"sche_date" firestore:"ScheDate"`
}