# 🎮 PlayHub: Android App & Python Backend

PlayHub is a full-stack native Android application designed for gamers to discover free-to-play games, manage their favorite titles, and connect with a community of players. This repository contains both the Android client and the Python/Flask backend server in a single monorepo workspace.

## ✨ Features

* **User Authentication:** Secure registration and login using Firebase Authentication.
* **Game Discovery:** Browse a massive library of games using the external FreeToGame API.
* **Smart Filtering:** Search for games by name, or filter by specific genres and platforms.
* **Favorites System:** Like games to save them to a personal favorites list (synced with MongoDB).
* **Social Interactions:** * Read and post comments on specific game pages.
    * Search for other users and "Follow" them to build a network.
* **Profile Management:** Update personal details, change passwords, and upload profile pictures (with automatic EXIF rotation and scaling).

## 🛠️ Tech Stack & Architecture

**Frontend (Android/Java):**
* **UI & Navigation:** Single-Activity Architecture, Jetpack Navigation Component, XML Layouts.
* **Network:** Retrofit2 for REST API calls (Dual API approach: Custom Backend + FreeToGame API).
* **Image Loading:** Glide (for game banners), Base64 Encoding (for user profiles).
* **Architecture:** Package-by-Layer pattern (`ui`, `models`, `network`, `adapters`).

**Backend (Python/Flask) & Database:**
* **Authentication:** Firebase Auth (Client-side validation).
* **Database:** MongoDB (User profiles, social connections, comments, and favorites).
* **Server Framework:** Flask.

## 📁 Project Structure

The repository is structured as a Monorepo, containing both the Android app root and the dedicated backend folder:

    PlayHub/ (Repository Root)
    ├── app/src/main/java/com/example/playhub/  # 📱 Android Source Code
    │   ├── adapters/                           # RecyclerView Adapters
    │   ├── models/                             # Data classes & DTOs
    │   ├── network/                            # Retrofit interfaces
    │   ├── ui/                                 # Fragments (Login, Home, etc.)
    │   └── MainActivity.java                   # Main Entry Point
    │
    ├── backend-server/                         # 🖥️ Python Backend Server
    │   ├── app.py                              # Main Flask API routing
    │   ├── requirements.txt                    # Python dependencies
    │   ├── .env.example                        # Example environment variables
    │   └── .gitignore                          # Backend-specific gitignore
    │
    ├── build.gradle                            # Android Build Config
    └── README.md

## 🔌 Backend & API Reference

The custom Flask server (`app.py`) handles all user data, social features, and comments. Below are the main endpoints:

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/register` | POST | Creates a new user document in MongoDB. |
| `/api/users/<uid>` | GET | Retrieves a specific user's profile and favorites. |
| `/api/users/<uid>` | PUT | Updates user details (nickname, phone, profile image, etc.). |
| `/api/users/search` | GET | Searches for users by nickname for the social feature. |
| `/api/users/follow` | POST | Adds a target user to the current user's 'following' list. |
| `/api/users/unfollow`| POST | Removes a target user from the 'following' list. |
| `/api/favorites/add` | POST | Adds a game ID to the user's favorites array. |
| `/api/favorites/remove`| POST | Removes a game ID from the user's favorites array. |
| `/api/comments/<game_id>`| GET | Fetches all comments for a specific game, sorted by date. |
| `/api/comments` | POST | Posts a new comment to a specific game. |

## 🚀 Getting Started

### Prerequisites
* Android Studio (Latest version)
* Python 3.x
* MongoDB instance (Local or Atlas)
* Firebase Project (for authentication)

### Installation & Setup

1. **Clone the repository:**
   `git clone https://github.com/YourUsername/PlayHub.git`

2. **Firebase Setup:**
    * Create a Firebase project and enable Email/Password authentication.
    * Download the `google-services.json` file and place it in the `app/` directory.

3. **Backend Setup:**
    * Open your terminal and navigate to the backend folder: `cd backend-server`
    * Install requirements: `pip install -r requirements.txt`
    * Create a `.env` file based on `.env.example` and add your `MONGO_URI`.
    * Run the server: `python app.py`

4. **Network Configuration (Android):**
    * Open the Retrofit initialization in your Fragments (e.g., `HomeFragment.java`).
    * Update the `baseUrl` to match your local machine's IPv4 address (e.g., `http://10.0.0.13:5000/`).
    * *Note: Use `http://10.0.2.2:5000/` if testing on the standard Android Emulator.*

5. **Run the App:**
    * Open the project root in Android Studio.
    * Build and run the application on an emulator or physical device.

## 👨‍💻 Author

**Bar Bibi**
* Email: barbibi7556@gmail.com
* LinkedIn: [Bar Bibi](https://www.linkedin.com/in/bar-bibi-computer-science/)