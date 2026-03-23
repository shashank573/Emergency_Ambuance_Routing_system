# Emergency Ambulance Routing System

Production-style full-stack project with Java Spring Boot backend and Leaflet/OpenStreetMap frontend.

## Tech Stack

- Backend: Java 17+, Spring Boot, Spring Web, Spring Data JPA, H2
- Frontend: HTML, CSS, JavaScript, Leaflet, OpenStreetMap
- Algorithms: Haversine distance, Dijkstra shortest path, priority queue, greedy assignment, sorting/searching

## Project Structure

```text
src/main/java/com/emergency/routing/
  controller/
  service/
  repository/
  model/
  util/
  dto/
  config/
frontend/
  index.html
  style.css
  script.js
```

## Features Implemented

- Emergency request intake (`location + severity + specialization`)
- Nearest available ambulance search
- Hospital filtering (`beds > 0` + specialization match)
- Greedy hospital ranking (distance, then beds)
- Dijkstra-based route generation for:
  - ambulance -> patient
  - patient -> hospital
- ETA estimation
- Double-booking prevention (synchronized emergency assignment and status locking)
- Admin stats dashboard and controls
- Patient handover module
- Real-time updates through polling every 5 seconds
- Browser notifications for assignment and ETA
- Preloaded dataset: 20 Delhi hospitals + 4 ambulances

## API Endpoints

### Emergency
- `POST /emergency/request`
- `GET /emergency/status/{id}`

### Hospital
- `GET /hospitals`
- `PUT /hospitals/{id}/beds`

### Ambulance
- `GET /ambulances`
- `PUT /ambulances/{id}/status`

### Handover
- `POST /handover`
- `GET /handover/{hospitalId}`

### Admin
- `GET /admin/stats`

## Run Instructions (VS Code Friendly)

### 1) Backend

```bash
cd emergency-ambulance-system
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`.

Optional H2 console:
- URL: `http://localhost:8080/h2-console`
- JDBC: `jdbc:h2:mem:emergencydb`
- User: `sa`
- Password: *(empty)*

### 2) Frontend

Open a second terminal:

```bash
cd emergency-ambulance-system/frontend
python3 -m http.server 5500
```

Open:
- `http://localhost:5500`

## How To Use

1. Click map to pick patient location.
2. Choose severity and required specialization.
3. Click **Request Ambulance**.
4. System assigns ambulance + hospital and draws both route legs.
5. Use admin controls to update hospital beds and ambulance status.
6. Submit patient handover form for a selected hospital.

## Sample Postman Requests

### 1) Emergency request

`POST http://localhost:8080/emergency/request`

```json
{
  "latitude": 28.6200,
  "longitude": 77.2300,
  "severity": "CRITICAL",
  "requiredSpecialization": "TRAUMA"
}
```

### 2) Emergency status

`GET http://localhost:8080/emergency/status/1`

### 3) Update hospital beds

`PUT http://localhost:8080/hospitals/1/beds`

```json
{
  "availableBeds": 18
}
```

### 4) Update ambulance status

`PUT http://localhost:8080/ambulances/1/status`

```json
{
  "status": "AVAILABLE"
}
```

### 5) Create handover

`POST http://localhost:8080/handover`

```json
{
  "patientName": "Rahul Verma",
  "bp": "130/85",
  "hr": 95,
  "o2": 96,
  "diagnosis": "Road traffic trauma, stabilized",
  "status": "PENDING",
  "hospitalId": 5
}
```

### 6) Admin stats

`GET http://localhost:8080/admin/stats`

## Notes

- For production deployment, switch H2 to MySQL in `application.properties`.
- Polling is used for real-time UI updates; you can upgrade to WebSocket/SSE later.
- Frontend and backend are intentionally decoupled for easy local development.
