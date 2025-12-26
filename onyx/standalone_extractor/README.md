# StreamFlix Standalone Extractor

This module contains the logic to extract video links (m3u8, mp4) from SFlix and Rabbitstream.

## How to use

1.  **Copy** this `standalone_extractor` folder to your new project's root directory.
2.  **Edit** your project's `settings.gradle` (or `settings.gradle.kts`) and add:
    ```gradle
    include ':standalone_extractor'
    ```
3.  **Sync** Gradle.
4.  **Add dependency** in your app's `build.gradle`:
    ```gradle
    implementation project(':standalone_extractor')
    ```

## Code Usage

```kotlin
import com.extractor.StreamFlixExtractor
import com.extractor.models.VideoType

// 1. Get Servers
val servers = StreamFlixExtractor.getServers(isMovie = true, id = "movie/free-interstellar-08779")

// 2. Extract Video from a Server
val video = StreamFlixExtractor.extractVideo(servers.first())

println("Video URL: ${video.source}")
```

## Dependencies used
- Retrofit
- OkHttp (with DoH)
- Jsoup
- Gson
