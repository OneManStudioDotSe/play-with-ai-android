# play-with-ai-android

Core Functionality:
• AI Chat: The central part of the app is a chat screen where users can send prompts to an AI and view the generated responses.
• Multi-modal Input: It's not just limited to text. Users can also provide images and documents as input to the AI for analysis.
• Prompt History: The app keeps a history of the prompts you've used, which you can easily access and reuse.

Technical Details:
• Modern UI: The user interface is built entirely with Jetpack Compose, which is Google's modern toolkit for building native Android UI.
• Architecture: It uses the popular MVVM (Model-View-ViewModel) architecture pattern, which separates the UI from the business logic. This is evident from the use of ViewModel and collectAsState.
• Asynchronous Operations: It relies on Kotlin Coroutines for managing background tasks like making network requests to the AI service.
• Networking: It uses Retrofit and OkHttp, the standard libraries for making network calls in modern Android development.
• Dependency Injection: It appears to be using Hilt for dependency injection, which helps in managing the dependencies between different parts of the application.
• Image Loading: It uses the Coil library to load and display images efficiently.
• Local Storage: It includes the Room library, suggesting it stores data locally on the device, most likely for the chat history.

In short, this is a well-architected, modern Android app that serves as a multi-modal client for an AI service.

---

## Project Enhancement Roadmap

Here is a list of tasks to improve this project and showcase advanced Android development skills:

### Step 1: Solidify the Foundation with a Comprehensive Testing Strategy
*   **Unit Tests:** Write unit tests for your `ViewModels` and `Repositories`.
*   **UI Tests:** Write UI tests for your Jetpack Compose screens.
*   **Integration Tests:** Create tests for critical user flows.

### Step 2: Implement a Scalable Design System
*   **Centralize UI constants:** Create a custom theme in a `:core:ui` module.
*   **Create reusable components:** Build a library of custom Composables for your app.
*   **Support for different themes:** Implement a dark theme and consider dynamic theming.

### Step 3: Refactor to a Modular Architecture
*   Create modules like `:app`, `:core:ui`, `:core:data`, `:core:model`, `:core:domain`, `:feature:chat`, and `:feature:history`.

### Step 4: Handle Edge Cases and Errors Gracefully
*   **Loading States:** Show loading indicators.
*   **Error States:** Implement a clear error handling strategy.
*   **Input Validation:** Handle invalid user input.
*   **Add support for detekt

### Step 5: Tackle Advanced Technical Challenges
*   **Streaming AI Responses:** Stream the AI response to the UI.
*   **Offline Support:** Provide a robust offline experience using Room.
*   **Improve Accessibility:** Add content descriptions, ensure proper focus order, and check touch target sizes.

### Step 6: Automate with CI/CD and Static Analysis
*   **CI/CD Pipeline:** Set up a CI/CD pipeline using GitHub Actions.
*   **Static Analysis:** Integrate `ktlint` and `detekt`.
