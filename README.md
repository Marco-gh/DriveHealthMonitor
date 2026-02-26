# CarApp

Android smartwatch project developed for my BSc thesis.

**Title:** *Design and develop an Android smart watch app to monitor and gather vital signs while driving*  
**Degree:** Bachelor’s Degree in Computer Engineering — UNIVAQ  
**Author:** Marco Silveri — 261195  
**Rapporteur:** Ing. Tarquini Francesco  
**Co-rapporteur:** Ing. D’Errico Leonardo  

---

## Context
CarApp is a **Wear OS + Android** system designed to **collect physiological and motion data during a driving session** and make them available on a smartphone and (optionally) on a remote database through a minimal web endpoint.

---

## System architecture (high level)
Data flow follows this pipeline:

1. **Sensor signals**
2. **Smartwatch app** (data acquisition)
3. **Transfer to smartphone** (Wear OS Data Layer)
4. **Smartphone app** (local persistence)
5. **HTTP communication** with server
6. **Database storage** (MySQL)

---

## What it collects
For each driving session record:
- `bpm` (heart rate)
- `o2InBlood` (blood oxygen)
- `accelerationX`, `accelerationY`, `accelerationZ` (accelerometer)

---

## App components
### Smartwatch side (data acquisition)
- Uses Android sensor APIs:
  - `SensorManager`, `SensorEventListener`, `Sensor`, `SensorEvent`
- Sensor sampling configured through `SENSOR_DELAY_*` policies
- Session design:
  - **A:** start session
  - **B:** stop sampling
  - **C:** end session

### Watch → Phone (data transfer)
- Uses **Wear OS Data Layer**
- Data is sent via **`DataClient`** as a **`DataItem`**
- Intended for payloads **> 100 KB** and **immediate synchronization** to listening nodes
- Smartphone receives data through a **`WearableListenerService`**

### Smartphone side
- **Local persistence:** Android **Room Database** (Entities + DAO)
- **Server communication:** Android **Volley** (HTTP requests)

---

## Server (minimal)
A PHP script stores and returns records using a MySQL table `health_records`.

**Fields**
- `deviceID`, `date`, `bpm`, `o2InBlood`, `accelerationX`, `accelerationY`, `accelerationZ`

**Supported actions (GET)**
- Insert a record: `action=insert`
- Query records: `action=query`  
  - `all=true` (all records)
  - `only_date=YYYY-MM-DD` (filter by day)
  - `days=true` (list unique days)

---
