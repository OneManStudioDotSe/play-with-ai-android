# play-with-ai-android

Welcome to my showcase app. Here you can see and dig deep into my preferred way of working. 

My approach is a holistic one: Work in a clean, simple and scalable way that spans beyond the basic coding. 
I look at every project from all the angles that matter:
- Coding
- Design
- Performance
- Scalability
- Future-proofing
- Those little extra things

## Coding
I strive to write code that is clean, easy to understand and follows a cohesive and logical way of doing things.
I use the latest and greatest of programming guidelines when it comes to Android:
- **Clean Architecture**: A strict separation of concerns with layers that containing the logic and functionality that they should contain
- **MVVM**: Decoupled UI logic using ViewModels
- **Lifecycle Awareness**: Efficient state collection to optimize resource consumption
- **Kotlin Coroutines**: Manage asynchronous tasks with structured concurrency
- **Consistent Naming**: Cohesive naming conventions for files, variables, and resources

## Design
Nowadays, an app should not be just screens with lists and details, but it should also communicate an "air" 
of personality. For that reason, every one of my apps, including this one, has its own little design system that I 
use consistently across it. A whole suite of custom fonts, text styles, color palette and UI components are utilized
at the app in order to show the importance of a consistent design system and the modern ways to use it. Of course, 
the whole DS is plug-n-play meaning that it is straightforward to update or change it completely, indicating the
importance of a modular codebase.

## Performance
Apart from how an app looks and feels, it is equally important for it to work smoothly and efficiently. 
- **Compose Stability**: Optimize Composables for performance by ensuring UI states are `@Immutable` where need and minimize unnecessary recompositions
- **Modularity**: The project is organized into modules to optimize build times and enforce clear boundaries.
- **Resource Management**: Avoiding memory leaks, making the most out of DI, and avoiding unnecessary libraries or permissions.

## Scalability
Projects on Android Studio have the tendency to grow as time goes by, therefore a scalable approach to the project's 
structure is necessary in order to keep the developer experience and maintainability possible. 
In this showcase project you can see a highly modular structure:
- **`:core-domain`**: Pure Kotlin module holding business logic, domain models, and repository definitions
- **`:core-data`**: Implementation of repositories, handling remote (Gemini AI) and local (Room) data sources
- **`:core-theme`**: Centralized theme and design language (colors, typography, etc.)
- **`:core-ui`**: Reusable UI components specific to the 'SoFa' design system
- **`:feature:chat`**: Feature module for the AI interaction experience
- **`:feature:map`**: Feature module for location-based services and route visualization

This structure allows potentially multiple teams to work on separate features without conflicts and ensures that each module only depends on what it absolutely needs.

## Future-proofing
An android app or project is in most cases part of a larger ecosystem where automations take place right in the form of 
automated scripts, CI/CD, publishing and report generation. To illustrate the consideration of these steps, I have also 
added support for running some basic maintenance and quality-of-code-related scripts when a PR is opened. This makes sure that all future development will abide to the team's or company's 
pipelines and will not break future releases.

## The little extra
To differentiate myself from the rest of the Android Engineers out there, I do the "little extra" that adds a nice layer
of polishing:
- **Localization**: Ready-to-go string extraction for internationalization
- **A11y**: Proper semantics, localized content descriptions, and support for dynamic font sizes
- **UX Polish**: Smooth typewriter animations with smart auto-scrolling and intelligent map auto-zooming
- **Personality**: A unique "AI Overlord" persona for the assistant to make interactions more engaging

Core Functionality:
- **AI Chat**: Send prompts to an AI (Gemini) with support for image and document attachments
- **Smart History**: Access and reuse previous prompts stored locally
- **Map Interaction**: Discover and filter locations (scooters/bikes) with optimal route calculation

Technical Details:
- **Jetpack Compose**: Native Android UI
- **Hilt**: Dependency Injection
- **Retrofit & OkHttp**: Networking
- **Room**: Local persistence
- **Firebase**: Realtime Database, authentication, and storage
