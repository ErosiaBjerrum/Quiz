# REST API Documentation

| Method | URL | Request Body (JSON) | Response (JSON) | Error |
|---|---|---|---|---|
| GET | /questions |  | [question, question, ...] (1) |  |
| POST | /questions/answer | answerRequest (2) | answerResult (3) | (e1) |


## Request Body and Response Formats

### (1) Question format

{
"id": Number,
"text": String,
"difficulty": String,
"category": String,
"answers": [
{
"id": Number,
"text": String
}
]
}

### (2) Answer request format

{
"answerId": Number
}

### (3) Answer result format

{
"correct": Boolean
}


## Errors

### (e1)

```json
{
  "status": 404,
  "msg": "Answer not found"
}