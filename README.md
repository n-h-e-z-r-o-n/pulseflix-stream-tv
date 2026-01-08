# ONYX Streaming App

![ONYX Logo](../../icon.png)
A premium Android TV streaming application for movies, TV shows, and anime content. Built with modern Android development practices and optimized for the TV viewing experience.

## 🎯 Preview
![Recording 2026-01-09 003513.gif](../../Downloads/Recording%202026-01-09%20003513.gif)
**ONYX** is a feature-rich streaming platform designed specifically for Android TV devices. It provides seamless access to movies, TV shows, and anime content with a beautiful, intuitive interface optimized for remote control navigation.
## 🎯 Overview
### Key Highlights

- 📺 **Android TV Optimized** - Built specifically for big-screen experiences
- 🎬 **Multi-Content Support** - Movies, TV Shows, and Anime streaming
- 🎨 **Modern UI** - Jetpack Compose with Material Design
- 💾 **Offline Support** - Room database for caching and watch history
- 🔐 **User Authentication** - Secure login and profile management
- 💳 **Subscription System** - Integrated payment wall and subscription management
- ▶️ **Advanced Video Player** - ExoPlayer with HLS, DASH, and RTSP support
- 📊 **Continue Watching** - Resume playback from where you left off

## ✨ Features

### Content Discovery
- Browse movies, TV shows, and anime by categories
- Search functionality across all content types
- Actor/cast information pages
- Detailed content metadata from TMDB API
- Season and episode management for TV shows

### Video Playback
- Multiple video format support (HLS, DASH, RTSP, MP4)
- Seamless episode switching
- Watch progress tracking and resume functionality
- Video quality selection
- Subtitle support

### User Experience
- Splash screen with loading animation
- Dark theme optimized for TV viewing
- Custom keyboard manager for TV remote input
- Grid-based content layouts
- Smooth navigation with RecyclerView
- Keep-screen-on during playback

### Data Management
- Room database for local data persistence
- Session management
- Watch history tracking
- Subscription status management
- Offline content caching

## 🏗️ Architecture

ONYX follows modern Android architecture patterns with clean separation of concerns:

![Architecture Diagram](C:/Users/n-h-e-z-r-o-n/.gemini/antigravity/brain/c2ce3fd9-502e-41f6-9ec6-e40b87c15164/onyx_architecture_diagram_1767907369728.png)

### Architecture Layers

#### 1. Presentation Layer
- **Activities**: Traditional Android UI components
  - `MainActivity` - Splash screen and app initialization
  - `Login_Page` - User authentication
  - `Watch_Page`, `Watch_Anime_Page` - Content browsing
  - `Video_payer`, `Anime_Video_Player` - Video playback
  - `Profile_Page` - User profile management
  - `PayWall` - Subscription management
  - `Category_Page`, `Shows_Page`, `Anime_Page` - Content discovery
  - `Actor_Page` - Cast information
  - `TermsAndConditionsActivity` - Legal information

- **Jetpack Compose**: Modern declarative UI toolkit
- **UI Components**: Custom grids, keyboards, and animations

#### 2. Business Logic Layer
- **ViewModels**: State management and use cases
- **API Services**:
  - `TMDBapi` - Movie and TV show data
  - `AnimeApi` - Anime content data
- **Video Extraction**: Custom video URL resolution logic
- **Session Management**: User session handling

#### 3. Data Layer
- **Room Database**: Local data persistence
  - `AppDatabase` - Main database instance
  - Entity definitions for movies, shows, watch history
- **Network Layer**: 
  - Retrofit/OkHttp for API communication
  - Custom SSL configuration for third-party APIs
  - GraphQL clients for specialized data sources

### Design Patterns

- **Repository Pattern**: Data abstraction layer
- **Singleton Pattern**: Database and API instances
- **Observer Pattern**: LiveData/Flow for reactive updates
- **Dependency Injection**: Manual DI with application-level instances

## 🛠️ Technology Stack

### Core Technologies
| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| Minimum SDK | API 21 (Android 5.0) |
| Target SDK | API 36 |
| Build System | Gradle (Kotlin DSL) |

### Android Jetpack
- **Compose** - Modern UI toolkit
- **Room** - Local database
- **Lifecycle** - ViewModel and lifecycle-aware components
- **Core KTX** - Kotlin extensions
- **AppCompat** - Backward compatibility

### Media & Video
- **ExoPlayer (Media3)** - Advanced media playback
  - HLS streaming
  - DASH streaming
  - RTSP protocol
  - OkHttp data source
- **Video extraction** - Custom video URL resolution

### Networking
- **OkHttp** - HTTP client
- **Retrofit** (implied) - REST API client
- **Gson** - JSON parsing
- **Jsoup** - HTML parsing for web scraping

### UI & Images
- **Glide** - Image loading and caching
- **Picasso** - Image loading with custom SSL
- **Material Components** - Material Design UI
- **RecyclerView** - Efficient list rendering
- **CardView** - Card-based layouts
- **ConstraintLayout** - Flexible layouts

### Utilities
- **SDP/SSP** - Scalable size units for TV
- **Core Splashscreen** - Native splash screen API

## 📦 Project Structure

```
onyx/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/onyx/
│   │   │   │   ├── Database/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   └── SessionManger.kt
│   │   │   │   ├── FetchData/
│   │   │   │   │   ├── TMDBapi.kt
│   │   │   │   │   └── AnimeApi.kt
│   │   │   │   ├── OnyxClasses/
│   │   │   │   │   ├── Grid.kt
│   │   │   │   │   ├── Anime_Grid.kt
│   │   │   │   │   └── CustomKeyboardManager.kt
│   │   │   │   ├── OnyxObjects/
│   │   │   │   │   ├── GlobalUtils.kt
│   │   │   │   │   ├── TrustManger.kt
│   │   │   │   │   ├── UnsafeOkHttpClient.kt
│   │   │   │   │   ├── NotificationHelper.kt
│   │   │   │   │   ├── SidbarAction.kt
│   │   │   │   │   └── loadingAnimation.kt
│   │   │   │   ├── ui/
│   │   │   │   ├── videoExtraction/
│   │   │   │   ├── videoResolver/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── OnyxApplication.kt
│   │   │   │   ├── Login_Page.kt
│   │   │   │   ├── Watch_Page.kt
│   │   │   │   ├── Watch_Anime_Page.kt
│   │   │   │   ├── Video_payer.kt
│   │   │   │   ├── Anime_Video_Player.kt
│   │   │   │   ├── Play.kt
│   │   │   │   ├── Category_Page.kt
│   │   │   │   ├── Shows_Page.kt
│   │   │   │   ├── Anime_Page.kt
│   │   │   │   ├── Actor_Page.kt
│   │   │   │   ├── Profile_Page.kt
│   │   │   │   ├── PayWall.kt
│   │   │   │   ├── Instraction.kt
│   │   │   │   └── TermsAndConditionsActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap/
│   │   │   │   ├── values/
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or later
- JDK 11 or higher
- Android SDK with API level 36
- An Android TV device or emulator for testing

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd onyx
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the `onyx` directory

3. **Sync Gradle**
   - Android Studio will automatically prompt to sync
   - Wait for dependencies to download

4. **Configure API Keys**
   
   The app uses external APIs. API keys are configured in `build.gradle.kts`:
   
   - `TM_K` - TMDB API key (already configured)
   - `PA_K` - Payment API key
   - `A_K` - Anime API endpoint

   > ⚠️ **Important**: For production use, move API keys to `local.properties` or use BuildConfig secrets

5. **Build the project**
   ```bash
   ./gradlew build
   ```

6. **Run on device/emulator**
   - Connect your Android TV device or start an emulator
   - Click the "Run" button in Android Studio
   - Select your target device

### Build Variants

- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization
  ```bash
  ./gradlew assembleRelease
  ```

## 🔧 Configuration

### Network Security

The app includes custom network security configuration for third-party API compatibility:

- SSL certificate validation customization
- Cleartext traffic enabled for specific domains
- Custom OkHttp client with trust manager

Configuration: `res/xml/network_security_config.xml`

### File Provider

Secure file sharing is configured for updates and downloads:

Configuration: `res/xml/file_paths.xml`

### Themes

The app uses a custom dark theme optimized for TV:
- `Theme.Onyx.Dark` - Main app theme
- `Theme.Onyx.Splash` - Splash screen theme

## 📱 Supported Platforms

### Android TV
- ✅ Primary target platform
- ✅ Leanback launcher support
- ✅ D-pad navigation
- ✅ Remote control optimized

### Hardware Requirements
- Touchscreen: Not required
- Leanback feature: Optional (app works without it)
- Minimum: Android 5.0 (API 21)
- Recommended: Android 13+ for best performance

## 🎨 User Interface

### Key UI Components

1. **Splash Screen** (10-second loading)
   - Animated logo with Glide
   - App initialization
   - Navigation to login/paywall

2. **Grid Layouts**
   - Custom `Grid.kt` for movies/shows
   - `Anime_Grid.kt` for anime content
   - RecyclerView-based implementation

3. **Video Player**
   - Full-screen playback
   - Episode navigation sidebar
   - Playback controls
   - Progress tracking

4. **Profile Management**
   - User information display
   - Subscription status
   - Watch history

## 🔐 Security & Privacy

- User credentials managed via `SessionManger`
- Secure file access with FileProvider
- Terms and conditions acceptance flow
- Subscription validation
- SSL/TLS for API communications

## 📊 Database Schema

The Room database includes entities for:

- **Movies**: Movie metadata and details
- **TV Shows**: Show information with seasons/episodes
- **Anime**: Anime-specific data
- **Watch History**: Playback position and timestamps
- **User Sessions**: Authentication data
- **Subscriptions**: Payment and subscription status

## 🌐 API Integration

### TMDB API
- Movie and TV show metadata
- Cast and crew information
- Images and posters
- Trending content

### Anime API
- Anime series information
- Episode listings
- Streaming sources
- Series metadata

### Payment System
- Subscription management
- Transaction processing
- Status validation

## 🐛 Known Issues & Limitations

1. **White Screen on Startup**: Brief delay before UI loads (optimization in progress)
2. **Third-party API Dependencies**: App functionality depends on external API availability
3. **Video Source Stability**: Streaming quality depends on third-party providers

## 🔜 Future Enhancements

- [ ] Improve startup performance
- [ ] Add offline download support
- [ ] Implement recommendation algorithm
- [ ] Add multi-profile support
- [ ] Enhance search with filters
- [ ] Add parental controls
- [ ] Support for multiple languages
- [ ] Chromecast integration

## 📄 License

© 2026 ONYX Streaming App. All rights reserved.

See [TermsAndConditionsActivity](file:///c:/Users/n-h-e-z-r-o-n/Desktop/TV%20PROJECT/onyx/app/src/main/java/com/example/onyx/TermsAndConditionsActivity.kt) for detailed terms and conditions.

## 🤝 Contributing

This is a private project. For inquiries, please contact the development team.

## 📞 Support

For technical support or questions about the app:
- Check the in-app instructions (`Instraction.kt`)
- Review the Terms and Conditions
- Contact: support@onyx-streaming.app *(placeholder)*

---

**Built with ❤️ for Android TV**

*Last updated: January 2026*
