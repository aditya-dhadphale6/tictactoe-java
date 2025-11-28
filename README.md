# 🕹️ Tic Tac Toe — Java + Vanilla JS + Docker + Render Deployment

This is a simple full-stack **Tic Tac Toe** game built using **Java (HttpServer)** for the backend and **HTML, CSS, and JavaScript** for the frontend. It provides a clean UI, lightweight server logic, and a fully REST-based interaction model between the UI and backend. The project is packaged using **Docker** and deployed live on **Render**, making it accessible online and easy to run anywhere.

🔗 **Live Demo:** [https://tictactoe-java-1.onrender.com](https://tictactoe-java-1.onrender.com)

---

## 🚀 Tech Stack

**Backend:**

* Java 24
* Built-in Java HttpServer
* JSON-based API

**Frontend:**

* HTML
* CSS
* JavaScript (fetch API)

**Deployment / DevOps:**

* Docker (multi-stage build)
* Render (Web Service)
* Git & GitHub

---

## ✨ Features

* Fully playable 3×3 Tic Tac Toe
* Player turns (X and O)
* Winner detection (rows, columns, diagonals)
* Draw detection
* Reset button
* Responsive UI
* Lightweight backend (no frameworks)
* Packaged and deployed using Docker

---

## 📁 Project Structure

```
tictactoe-java/
 ├── src/main/java/com/example/tictactoe/
 │     ├── Game.java        # Game logic
 │     └── Main.java        # Server + API + static file handling
 ├── static/
 │     ├── index.html
 │     ├── styles.css
 │     └── app.js
 ├── Dockerfile
 └── .dockerignore
```

---

## 🛠️ Run Locally

To run the backend manually using Java:

```sh
javac -d out src/main/java/com/example/tictactoe/*.java
java -cp out com.example.tictactoe.Main
```

Then open:

```
http://localhost:8000
```

---

## 🐳 Docker

Build the Docker image:

```sh
docker build -t tictactoe .
```

Run the container:

```sh
docker run -p 8000:8000 tictactoe
```

---

## ☁️ Deployment (Render)

To deploy this project on Render:

1. Push code to GitHub
2. Go to Render → **New Web Service**
3. Choose **Docker** environment
4. Connect your GitHub repo
5. Select the free tier
6. Deploy 🎉

---

## 👤 Author

**Aditya Dhadphale**
