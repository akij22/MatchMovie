# MatchMovie

MatchMovie is an Android app built with Kotlin and Jetpack Compose for searching movies, viewing details, saving a personal movie list, getting recommendations, and chatting with an AI assistant. The app uses a Flask backend for TMDB/AI requests, Room for local data, and image resources from `res/drawable`.

## General Rules

- Keep code simple, readable, and consistent with the existing project style.
- Do not introduce ViewModels: state should stay in Composables through `remember`, `LaunchedEffect`, state hoisting, and callbacks.
- Do not introduce new architectures or extra layers unless explicitly requested.
- Prefer small, local changes over broad refactors.
- Use clear, descriptive names for functions, variables, Composables, and files.

## Composables

### Do

- Create reusable Composables when a UI block is repeated or has a clear standalone responsibility.
- Put shared Composables in `components/`.
- Keep full screen Composables in `screens/`.
- Pass only the data and callbacks a Composable actually needs.
- Add an optional `Modifier` parameter to reusable Composables.
- Split large Composables into smaller private Composables or helper functions when it improves readability.
- Use explicit callbacks such as `onMovieSelected`, `onDeleteClick`, and `onTabSelected`.

### Avoid

- Do not duplicate similar layouts across multiple screens.
- Do not create very large Composables that mix UI, state handling, networking, database operations, and event logic in one place.
- Do not pass unnecessary state or objects to child Composables.
- Do not keep complex business logic directly inside UI rendering code when it can be extracted into a private function.

## State and Navigation

- Do not use ViewModels.
- Use `remember` and `mutableStateOf` for local screen state.
- Use state hoisting when state must survive screen changes or be shared across screens.
- Keep shared state in the nearest common parent Composable, as already done in `MainActivity`.
- Use the `Screen` enum to represent the current screen.
- Do not add a separate navigation system unless explicitly requested.

## Coroutines and Data

- Use `LaunchedEffect` for initial loading and effects tied to a stable key.
- Use `rememberCoroutineScope` for actions triggered by user input, such as clicks or form submissions.
- Run database and network work on `Dispatchers.IO`.
- Keep Retrofit setup and API definitions in `network/`.
- Keep Room database, DAO, and local entities in `database/`.
- Keep network request/response objects in `network/dto/`.
- Keep UI models, support data classes, and mapper functions in `model/`.

## Icons and Images

- Use raster image files from `res/drawable` for icons and illustrations.
- Prefer `painterResource(id = R.drawable.resource_name)` for local icons.
- Use `AsyncImage` for remote movie images.
- Do not draw icons with Compose Canvas.
- Do not manually build icons with Compose drawing primitives when an image resource exists or can be added.
- New icon assets should be clearly named image files, such as `home.png`, `ai_chat.png`, or `mylist_notfound.png`.

## Folder Organization

- `screens/`: complete app screens.
- `components/`: reusable Composables and shared UI components.
- `model/`: UI models, support data classes, and mapper functions.
- `network/`: Retrofit instance, API interface, token handling, and network configuration.
- `network/dto/`: API request and response objects.
- `database/`: Room database, DAO, and local entities.
- `enumentity/`: enums used by the app, such as screens, moods, and message senders.
- `ui/theme/`: colors, theme, and typography.
- `utils/`: constants and generic helpers.

## Kotlin Style

- Use explicit imports at the top of each file.
- Avoid vague or overly abbreviated names.
- Keep comments useful, short, and focused on why something is done.
- Do not comment obvious code.
- Prefer private functions for helpers used only inside one file.
- Do not leave dead code, unused imports, or unused variables.
- Keep standard Kotlin formatting.

## UI Rules

- Reuse colors from `ui/theme/Color.kt`.
- Keep visual style consistent with the existing palette, spacing, typography, and rounded corners.
- Reuse existing components such as `InfoMessage`, `StatusMessage`, cards, and the bottom bar when they fit the use case.
- Handle empty, loading, and error states explicitly.
- Avoid duplicated hardcoded text when the same string is reused often.

## Do Not

- Do not add ViewModels.
- Do not use Compose Canvas for icons.
- Do not move files into folders that do not match their responsibility.
- Do not create premature generic abstractions.
- Do not mix network DTOs, database entities, and UI models without clear mapper functions.
- Do not add new dependencies unless they are genuinely needed.
- Do not modify the backend when the request only concerns the Android UI.
