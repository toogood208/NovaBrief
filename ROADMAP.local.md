# NovaBrief - UI First Build Plan

This project will be built in small, production-ready steps.

We are prioritizing:
- scalable architecture
- predictable state management
- clear folder structure
- navigation that grows with features
- dependency injection for ViewModels
- UI-first delivery now, API layer integration next

## Build Strategy

We will execute one step at a time.

1. Step 1: Folder structure and package boundaries
2. Step 2: App shell and navigation graph setup
3. Step 3: Design system foundation (theme, typography, reusable UI primitives)
4. Step 4: Feature contract pattern (UiState, UiEvent, UiEffect)
5. Step 5: ViewModel + DI wiring (Hilt)
6. Step 6: Home news UI screen implementation (static/fake data)
7. Step 7: Feature expansion scaffolding (Detail, Saved, Settings)
8. Step 8: Data layer placeholders for future API integration
9. Step 9: Testing baseline (ViewModel and UI tests)

---

## Step 1 - Folder Structure (Current Step)

### Goal
Create a feature-first package structure that supports clean architecture without over-engineering the project at this stage.

### Why this is the best approach
- It keeps code close to the feature where it is used.
- It reduces cross-package coupling and accidental dependencies.
- It makes migration to multi-module straightforward when the app grows.
- It allows UI-first development now while preserving a clear path for domain and data layers later.

### Target package layout

~~~text
com.example.novabrief
  app
    NovaBriefApp.kt
  core
    common
      result
      utils
    designsystem
      components
      theme
    navigation
      AppNavHost.kt
      Routes.kt
  feature
    home
      presentation
        HomeScreen.kt
        HomeViewModel.kt
        HomeContract.kt
      domain
        model
        usecase
      data
        repository
        datasource
    detail
      presentation
      domain
      data
    saved
      presentation
      domain
      data
    settings
      presentation
      domain
      data
~~~

### Code shape for Step 1
Use package declarations that mirror folders exactly.

~~~kotlin
package com.example.novabrief.feature.home.presentation
~~~

~~~kotlin
package com.example.novabrief.core.navigation
~~~

Why this code style is best:
- predictable imports
- easy IDE navigation
- avoids package mismatch build issues

### Step 1 completion checklist
- [ ] Create all core and feature packages listed above
- [ ] Add placeholder files so folders are committed and visible
- [ ] Move theme files from ui.theme to core.designsystem.theme
- [ ] Keep MainActivity minimal until Step 2 navigation wiring

---

## Step 2 - App Shell + Navigation

### What we add
- single app navigation host
- typed routes
- one start destination (Home)

### Why this is the best approach
- one source of truth for screen flow
- easier deeplink support later
- no navigation logic inside reusable UI components

### Starter snippet

~~~kotlin
sealed interface AppRoute {
    data object Home : AppRoute
    data class Detail(val articleId: String) : AppRoute
}
~~~

---

## Step 3 - Design System Foundation

### What we add
- custom light and dark color schemes
- typography and spacing tokens
- base reusable components

### Why this is the best approach
- prevents inconsistent styling across features
- keeps visual identity stable instead of relying on dynamic color
- supports fast UI iteration with consistent tokens

### Starter snippet

~~~kotlin
@Composable
fun NovaBriefTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = NovaTypography,
        content = content
    )
}
~~~

---

## Step 4 - State Contract Pattern (UDF)

### What we add
- UiState for rendering
- UiEvent for user input
- UiEffect for one-time actions

### Why this is the best approach
- deterministic UI updates
- easier testing and debugging
- avoids one-off mutable state spread across composables

### Starter snippet

~~~kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val articles: List<ArticleUi> = emptyList(),
    val error: String? = null
)

sealed interface HomeUiEvent {
    data object OnRefresh : HomeUiEvent
}

sealed interface HomeUiEffect {
    data class ShowError(val message: String) : HomeUiEffect
}
~~~

---

## Step 5 - Dependency Injection + ViewModels

### What we add
- Hilt app setup
- constructor-injected ViewModels
- module bindings for repositories/use cases

### Why this is the best approach
- makes business logic testable and replaceable
- avoids manual object graph wiring
- enables smooth switch from fake to real data sources later

### Starter snippet

~~~kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopNews: GetTopNewsUseCase
) : ViewModel()
~~~

---

## Step 6 - Home News UI (Static Data First)

### What we add
- article hero card
- title, summary, actions
- loading and empty states

### Why this is the best approach
- validates layout and theme before backend integration
- avoids API noise while tuning UX
- allows quick iteration from design screenshots

---

## Step 7 - Additional Screens Scaffolding

### What we add
- Detail, Saved, Settings route shells
- contracts and placeholder screens

### Why this is the best approach
- keeps navigation future-proof
- allows parallel UI development
- prevents late architecture rewrites

---

## Step 8 - Data Layer Placeholders for API Phase

### What we add
- repository interfaces in domain
- fake repository in data
- DTO and mapper placeholders

### Why this is the best approach
- makes API integration incremental
- isolates network concerns from UI and ViewModels
- preserves stable domain model contracts

---

## Step 9 - Testing Baseline

### What we add
- ViewModel unit tests
- Compose UI tests for key states

### Why this is the best approach
- catches regressions early
- gives confidence during rapid UI iterations
- keeps architecture enforceable over time

---

## Working Agreement

For each step, we will do this flow:
1. Define exact files and snippets
2. Explain why each snippet is the best approach
3. Implement only that step
4. Verify build
5. Move to next step

Current position: Step 1 ready to execute.
