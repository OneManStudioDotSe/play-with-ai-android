# play-with-ai-android

Welcome to my showcase app. Here you can see and dig deep into my preferred way of working. 

My approach is a holistic one: Work in a clean, simple and scalable way that spans beyond the basic coding. 
I look at every project from all the angles that matter:
- Coding
- Design
- Performance
- Scalability
- Future-proofing
- The little extra

## Coding
I strive to write code that is clean, easy to understand and follows a cohesive and logical way of doing things.
I use the latest and  greatest of programming guidelines when it comes to Android so expect to see Compose, Kotlin, 
coroutines, MVVM and clean architecture. Consistent naming of files and variable, proper structure of folders and 
properly indented and lean code are crucial as well.

## Design
In the year 2025, an app should not be just screens with lists and details but it should also communicate an "air" 
of personality. For that reason, every one of my apps, including this one has its own little design system that I 
use consistently across it. A whole suite of custom fonts, text styles, color palette and UI components are utilized
at the app in order to show the importance of a consistent design system and the modern ways to use such. Of course, 
the whole DS is plug-n-play meaning that it is very straigh-forward to update or change it completely, indicating the
importance of a modular codebase.

## Performance
Apart from how an app looks and feels, it is equally important for it to work smoothly and efficiently. For that 
reason, I optimize my code to use the optimal amount of memory, avoid memory leaks, make the most out of DI, avoid 
unnecessary libraries or permissions and "weigh" just as much as it is needed. Performance though is not only at the 
user level but also at the developer experience, so for that, I organize the project into modules, based on the 
functionality they hold. The modularity of the project results in optimized build times as every consequent build doesn't
require the full build of the project 

## Scalability
Projects on Android Studio have the tendency to grow as time goes by, therefore a scalable approach to the project's 
structure is necessary in order to keep the developer experience and maintanability of the project possible. For that
reason, breaking a big monolith of a project into smaller modules that each one focus on one and only thing is a desired 
approch so we don't end up with a tangled mess of a project.
In this showcase project you can see that I have 3 core modules:
-core-data', which contains the data layer of the app that is responsible for handling data that exist at remote or local locations
-'core-theme', which contains the theme and design language of the app
-'core-ui', which contains the ui components that are specific to the theme (called 'SoFa' in this case) and other views I use

There is also a module of modules called 'feature', inside which resides a module called 'chat' which holds the core
functionality of the project. The idea is that every potential feature would live inside that module on its own place, 
using only the dependencies that it needs. Such a project structure makes it much easier for multiple teams to work on the
project, without worrying about steping on each other's code or dealing with endless git conflicts at the PRs.

## Future-proofing
An android app or project is in most cases part of a larger ecosystem where automations take place right in the form of 
automated scripts, CI/CD, publishing and report generation. To illustrate the consideration of these steps, I have also 
added support for running some basic maintenance and quality-of-code-related scripts when a PR is opened towards the 
develop branch of this project. This makes sure that all future development will abide to the team's or company's 
pipelines and will not break future releases.

## The little extra
To differentiate myself from the rest of the Android Engineers out there, I do the "little extra" that adds a nice layer
of polishing on my apps. Things like a custom app icon, this README, a splash screen at the app and a few animations here
are there, a11y, localization, are all things that I considerable valuable as they can separate a generic app from a nice
app. Enjoy!


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
[x] Create a custom theme in a `:core:theme` module
[x] Build a library of custom Composables for your app
[x] Implement a dark theme and consider dynamic theming


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
[x] Set up a CI/CD pipeline using GitHub Actions.
[x] Static Analysis: Integrate `ktlint` and `detekt`.
