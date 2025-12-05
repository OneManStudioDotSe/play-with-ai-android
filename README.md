# play-with-ai-android

Core Functionality:
The central part of the app is a chat screen where users can send prompts to an AI and view the generated responses.
Users can also provide images and documents as input to the AI for analysis.
The app keeps a history of the prompts you've used, which you can easily access and reuse.

Technical Details:
The user interface is built entirely with Jetpack Compose, the go-to toolkit for building native Android UI.
It uses the popular MVVM (Model-View-ViewModel) architecture pattern, which separates the UI from the business logic.
It relies on Kotlin Coroutines for managing background tasks like making network requests to the AI service.
It uses Retrofit and OkHttp, the standard libraries for making network calls in modern Android development.
It is using Hilt for dependency injection, which helps in managing the dependencies between different parts of the application.
It uses the Coil library to load and display images efficiently.
It utilizes Room for storing data locally on the device, for the chat history.

## Project Enhancement Roadmap

### Add unit tests
[ ] Write unit tests for your `ViewModels` and `Repositories`
[ ] Write UI tests for your Jetpack Compose screens
[ ] Create tests for critical user flows (integration tests)


### Implement a scalable design system
[ ] Create a custom theme in a `:core:theme` module
[ ] Build a library of custom Composables for your app
[ ] Implement a dark theme and consider dynamic theming


### Refactor to a modular architecture
[x] Create modules like `:app`, `:core:ui`, `:core:data`, `:core:model`, `:core:domain`, `:feature:chat`, and `:feature:history`.


### Handle Edge Cases and Errors Gracefully
[ ] Show loading indicators where needed
[ ] Implement a clear error handling strategy and handle errors at the UI
[ ] Handle invalid user input


### Tackle advanced technical challenges
[ ] Streaming AI Responses: Stream the AI response to the UI
[ ] Offline Support: Provide a robust offline experience using Room
[ ] Improve Accessibility: Add content descriptions, ensure proper focus order, and check touch target sizes
[x] Add support for detekt

 
### Automate with CI/CD and support static analysis
[ ] Set up a CI/CD pipeline using GitHub Actions.
[ ] Static Analysis: Integrate `ktlint` and `detekt`.
