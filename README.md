# SoulMetric

**Package:** `com.nextersolutions.soulmetric`  
**Min SDK:** 28 (Android 9 Pie)  
**Target SDK:** 35  
**Architecture:** MVI + Clean Architecture  
**DI:** Hilt  
**Network:** Ktor  
**DB:** Room  
**UI:** Jetpack Compose + Material 3

---

## Project Structure

```
app/src/main/java/com/nextersolutions/soulmetric/
├── core/
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppPreferences.kt          — DataStore wrapper
│   │   │   ├── dao/Daos.kt               — Room DAOs
│   │   │   ├── database/SoulMetricDatabase.kt
│   │   │   └── entity/Entities.kt        — Room entities
│   │   ├── remote/
│   │   │   ├── api/SurveyApiService.kt   — Ktor API call
│   │   │   └── dto/SurveyDtos.kt         — Kotlinx Serialization DTOs
│   │   └── repository/
│   │       ├── SurveyMapper.kt           — DTO → Domain mapper (locale-aware)
│   │       ├── SurveyRepositoryImpl.kt   — Assets seed + network refresh
│   │       └── SurveyResultRepositoryImpl.kt
│   ├── di/AppModules.kt                  — Hilt modules
│   ├── domain/
│   │   ├── model/Survey.kt               — Domain models
│   │   ├── model/SurveyResult.kt
│   │   ├── repository/Repositories.kt    — Interfaces
│   │   └── usecase/UseCases.kt           — All use cases
│   └── util/LocaleResolver.kt            — System locale → JSON key
├── feature/
│   ├── home/presentation/                — HomeViewModel (MVI) + HomeScreen
│   ├── survey/presentation/             — SurveyViewModel (MVI) + SurveyScreen
│   └── results/presentation/            — ResultsViewModel (MVI) + ResultsScreen
├── navigation/NavGraph.kt               — Type-safe Navigation Compose
├── ui/
│   ├── components/SharedComponents.kt   — GradientButton, SoulMetricCard, etc.
│   └── theme/                           — Color, Typography, Theme
├── MainActivity.kt
└── SoulMetricApp.kt                     — @HiltAndroidApp
```

---

## Survey JSON Format

Surveys are loaded from:
1. **`assets/surveys.json`** — bundled fallback, always available offline
2. **Network** — fetched from `SURVEYS_BASE_URL/surveys/surveys.json` and cached in Room

Every text field is a localization map. Supported locale keys: `en`, `de`, `ua`.
The app resolves the system locale at runtime; Android's `uk` maps to `ua`.
English (`en`) is the required fallback in every map.

```json
{
  "surveys": [{
    "id": "stress_assessment",
    "version": 1,
    "title":       { "en": "Stress Assessment", "de": "Stressbewertung", "ua": "Оцінка стресу" },
    "description": { "en": "...", "de": "...", "ua": "..." },
    "questions": [
      {
        "id": "q_sleep",
        "type": "scale",
        "text": { "en": "How well do you sleep?", "de": "...", "ua": "..." },
        "required": true,
        "scale": {
          "min": 1, "max": 5,
          "labels": {
            "1": { "en": "Very bad",  "de": "...", "ua": "..." },
            "5": { "en": "Excellent", "de": "...", "ua": "..." }
          }
        }
      },
      {
        "id": "q_mood",
        "type": "choice",
        "text": { "en": "How is your mood?" },
        "required": true,
        "options": [
          { "id": "opt_happy", "text": { "en": "Happy", "de": "Glücklich", "ua": "Щасливий" } }
        ]
      },
      {
        "id": "q_notes",
        "type": "text",
        "text": { "en": "Any notes?" },
        "required": false
      }
    ],
    "scoring": {
      "method": "sum",
      "ranges": [
        { "min": 0, "max": 10, "description": { "en": "Low stress", "de": "...", "ua": "..." } }
      ]
    }
  }]
}
```

### Supported question types

| `type`    | Required fields             | Answer stored as      |
|-----------|-----------------------------|-----------------------|
| `scale`   | `scale.min`, `scale.max`    | `Answer.ScaleAnswer`  |
| `choice`  | `options[]`                 | `Answer.ChoiceAnswer` |
| `text`    | —                           | `Answer.TextAnswer`   |

### Adding a new question type

1. Add a new `type` string constant (e.g. `"rating"`)
2. Add optional DTO fields to `QuestionDto`
3. Add a new `Question.Rating` sealed subclass in `Survey.kt`
4. Handle it in `SurveyMapper.QuestionDto.toDomain()`
5. Add a new `Answer.RatingAnswer` in `SurveyResult.kt`
6. Render it in `SurveyScreen.kt → QuestionContent`
7. Handle scoring in `SubmitSurveyUseCase`

---

## Localization

String resources (app UI strings) live in:
- `res/values/strings.xml`        → English
- `res/values-de/strings.xml`     → German
- `res/values-uk/strings.xml`     → Ukrainian (Android uses `uk`, not `ua`)

Survey *content* localization is handled at runtime via `LocaleResolver` and the JSON maps —
no recompilation needed when adding new survey content or locales.

---

## Network

- Base URL configured via `BuildConfig.SURVEYS_BASE_URL` in `app/build.gradle.kts`
- The app **never** blocks on network at startup; it seeds from `assets/surveys.json` first
- Pull-to-refresh in HomeScreen calls `RefreshSurveysUseCase → SurveyApiService → Room cache`
- All cached survey DTOs are stored as JSON strings in `survey_cache` table (easy to inspect/extend)

---

## Setup

### 1. Fonts
Download **Plus Jakarta Sans** from [Google Fonts](https://fonts.google.com/specimen/Plus+Jakarta+Sans) and place the TTF files in `app/src/main/res/font/`. See `res/font/FONTS_README.md` for exact filenames.

### 2. Build
```bash
./gradlew assembleDebug
```

### 3. Configure server URL
Edit `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "SURVEYS_BASE_URL", "\"https://your-server.com/\"")
```
The app expects the surveys JSON at `{BASE_URL}surveys/surveys.json`.

---

## MVI Pattern

Each feature follows strict MVI:
- `State` — immutable data class, observed via `StateFlow`
- `Intent` — sealed class representing all user actions
- `Effect` — one-shot sealed class for navigation/snackbar events
- `ViewModel` — processes intents, emits state and effects

Example flow:
```
User taps card → HomeScreen.onIntent(HomeIntent.OpenSurvey(id))
              → HomeViewModel emits HomeEffect.NavigateToSurvey(id)
              → HomeScreen collects effect → navController.navigate(...)
```

---

## Database Schema

```
survey_cache      id (PK), version, json (full DTO as JSON string), updatedAt
survey_results    id (PK autoincrement), surveyId, surveyTitle, completedAt, score, scoreDescription
survey_answers    id (PK), resultId (FK → survey_results CASCADE), questionId, type, intValue, stringValue
```
