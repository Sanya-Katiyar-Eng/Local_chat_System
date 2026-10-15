# Linkora — Java-Based Real-Time Chat Application

**Connect Instantly. Communicate Seamlessly.**

Linkora is a desktop-based real-time chat application developed using Java Swing and socket programming. It provides a user-friendly graphical interface for connecting with other users and communicating through chat rooms.

## Overview

Linkora is designed to demonstrate real-time communication using Java networking and GUI technologies. The application follows a client-server architecture, where multiple clients communicate through a central server.

## Features

- **User-Friendly GUI:** Interactive desktop interface built with Java Swing.
- **Real-Time Messaging:** Exchange messages through socket-based communication.
- **Chat Rooms:** Create and join chat rooms.
- **Room Access:** Support for room-based access using room credentials, as implemented in the application.
- **Message Management:** Delete your own messages, where supported by the application.
- **Typing Indicator:** Display typing activity.
- **Client-Server Architecture:** Separate client and server components.
- **Desktop Application:** Runs on systems with a compatible Java Development Kit (JDK).

## Technology Stack

- **Programming Language:** Java
- **GUI:** Java Swing and AWT
- **Networking:** Java TCP Sockets
- **Architecture:** Client-Server
- **Development Environment:** Visual Studio Code
- **Version Control:** Git and GitHub

## Project Structure

```text
Linkora/
├── src/
│   ├── client/
│   │   └── ChatClient.java
│   ├── gui/
│   │   ├── ChatFrame.java
│   │   ├── LoginFrame.java
│   │   └── Theme.java
│   ├── model/
│   │   └── Message.java
│   ├── server/
│   │   ├── ChatServer.java
│   │   └── ClientHandler.java
│   └── util/
│       └── ChatHistory.java
├── .gitignore
├── README.md
└── out/
```

*Note: The structure may vary slightly depending on the current project files. Compiled output files in `out/` are not required to build the application.*

## Prerequisites

Before running Linkora, make sure you have:

- Java JDK 17 or a compatible version
- Visual Studio Code or another Java IDE
- Git (optional, for cloning the repository)

Verify your Java installation:

```bash
java -version
javac -version
```

## Installation and Setup

### 1. Clone the Repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
cd Linkora
```

Replace `YOUR_GITHUB_REPOSITORY_URL` with your actual GitHub repository URL.

### 2. Compile the Source Code

Run the following command from