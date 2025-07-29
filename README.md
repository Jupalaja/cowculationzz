# Cowculationzz v1.0

## 📖 Overview

Cowculationzz is a smart nutrition assistant available via Telegram. It helps you track your protein intake by simply telling it what you ate, either by text or voice message. Powered by Google's Gemini AI, it understands natural language, gets the nutritional facts, and provides a clear summary in Spanish.

## ✨ Features

-   **Natural Language Understanding**: Describe your meal in plain language (e.g., "I had a chicken breast with a side of rice").
-   **Voice Recognition**: Send a voice message instead of typing. The bot will transcribe it and process your meal.
-   **Protein Calculation**: Automatically calculates the total protein content of your meal.
-   **Telegram Integration**: Easily accessible on the popular messaging platform.
-   **Spanish Summaries**: Get a clear, formatted summary of your protein intake in Spanish.

## ⚙️ How It Works

The application follows a simple yet powerful workflow to provide nutritional information:

1.  **User Input**: A user sends a text or voice message to the Cowculationzz Telegram bot describing their meal.
2.  **Transcription (for voice)**: If the input is a voice message, it's sent to the Gemini API for accurate transcription into text.
3.  **Query Processing**: The text (either from a direct message or transcription) is sent to the Gemini API. A specialized prompt instructs the model to extract the food items and quantities, creating a clean query for the nutrition API (e.g., "2 boiled eggs and a slice of bread").
4.  **Data Retrieval**: The processed query is sent to the CalorieNinjas API, which returns detailed nutritional information for each food item.
5.  **Summary Generation**: The nutritional data is passed back to the Gemini API with another prompt, instructing it to generate a user-friendly summary in Spanish. This summary lists each item's protein content and provides the total.
6.  **Response**: The final summary is sent back to the user in the Telegram chat.

## 🛠️ Tech Stack

-   **Backend**: Kotlin, Spring Boot
-   **AI & NLP**: Google Gemini API
-   **Nutrition Data**: CalorieNinjas API
-   **Bot Framework**: Kotlin Telegram Bot

## 🚀 Getting Started

### Prerequisites

-   Java 21 or higher
-   Gradle

### Configuration

The application requires the following environment variables to be set:

-   `CALORIE_NINJAS_API_KEY`: Your API key for the CalorieNinjas API.
-   `GEMINI_API_KEY`: Your API key for the Google Gemini API.
-   `TELEGRAM_BOT_TOKEN`: Your token for the Telegram bot.

These can be set in your environment or in the `src/main/resources/application.properties` file for local development.

### Running the Application

You can run the application using the Spring Boot Gradle plugin:

```bash
./gradlew bootRun
```

## 🔮 Future Features

### 📸 Image-Based Nutrition Analysis (v2.0)

The next major version will introduce the ability to analyze meals from images. Users will be able to simply take a picture of their food, and the bot will:

1.  Use a multimodal AI model (like Gemini) to identify all food items in the image.
2.  Estimate the quantities of each item.
3.  Retrieve nutritional information and provide the same detailed summary as the text/voice feature.

This will make tracking nutrition even easier and more intuitive.
