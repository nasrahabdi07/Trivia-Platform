#  Wireless Trivia Platform

##  Project Overview

**Wireless Trivia Platform** is a robust, multiplayer trivia game system built with **Java** and **JavaFX**. It features a concurrent server capable of handling multiple client connections, real-time game rooms, and a dedicated admin monitoring dashboard.

This project demonstrates advanced concepts in **network programming**, **multithreading**, and **GUI development**.

---

##  Architecture

The system follows a clean **Client-Server** architecture with three main components:

### 1. **The Server (`TriviaServer`)**
- Acts as the central hub using **Java Sockets**.
- Manages multiple **Game Rooms** and connected users.
- Handles user registration, broadcasting messages, and game state management.
- Runs a separate thread for sending **real-time telemetry** (CPU, Memory, Network usage) to admin clients.

### 2. **The Client (`ClientMain`)**
- A **JavaFX** desktop application for players.
- Features:
  - **Login/Register**: Connect to the server.
  - **Lobby Browser**: View and join available game rooms.
  - **Game Interface**: answering trivia questions in real-time.
  - **Leaderboards**: View global high scores.

### 3. **Admin Monitor (`AdminMonitor`)**
- A specialized client for administrators.
- Authenticates using a secure key.
- Displays live server health metrics:
  - **Active Users**: Registered vs. Online.
  - **System Resources**: RAM & CPU usage.
  - **Network Traffic**: Incoming/Outgoing data rates.

---

##  Features

- ** Multiplayer support**: Multiple users can play in different rooms simultaneously.
- ** Real-time updates**: Game state and chat messages are pushed instantly to clients.
- ** Admin Dashboard**: Live graphs and statistics for server monitoring.
- ** Robust Networking**: Custom protocol design for client-server communication.
- ** Dynamic UI**: polished JavaFX interfaces with animations.

---

## Technology Stack

- **Language**: Java 21
- **GUI Framework**: JavaFX 22
- **Build Tool**: Gradle
- **Networking**: `java.net.Socket`, `java.net.ServerSocket`
- **Concurrency**: `ExecutorService`, `Thread`

---

##  How to Run

This project uses **Gradle** for build and execution. You can run all components from the terminal.

### Prerequisites
- JDK 21 or higher installed.

### 1. Start the Server
The server must be running first.
```bash
./gradlew runServer
```
*Server runs on port `5050` by default.*

### 2. Start the Client
Launch the player application. You can run multiple instances to simulate multiple players.
```bash
./gradlew runClient
```

### 3. Start the Admin Monitor
Launch the admin dashboard to view server stats.
```bash
./gradlew runMonitor
```
*Note: The admin dashboard connects automatically and authenticates with the server.*

---

##  Configuration

- **Port**: `5050` (Defined in `TriviaServer.java`)
- **Admin Secret**: `SuperSecretAdmin2025` (Required for Admin Monitor authentication)

---
