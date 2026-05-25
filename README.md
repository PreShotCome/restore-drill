# Lathe

Tinder for your photo gallery.

- Swipe **right** to keep.
- Swipe **left** to trash.
- Tapped through your library? Confirm once, and Android moves the lot to the system Trash (recoverable for 30 days).

Named after the Greek goddess of forgetting — clear what doesn't serve you, keep what does.

## Stack

- Kotlin + Jetpack Compose
- MediaStore for gallery access
- `MediaStore.createTrashRequest` for safe, OS-managed deletion (Android 11+)
- Coil for image loading

## Build

Requires Android Studio Hedgehog+ or `gradle 8.5+` with the Android SDK.

```bash
./gradlew :app:assembleDebug
```

Install on a connected device:

```bash
./gradlew :app:installDebug
```

## Permissions

- `READ_MEDIA_IMAGES` (Android 13+) / `READ_EXTERNAL_STORAGE` (Android 11–12).
- Trash requests prompt the user once per batch — no other permissions needed.

## Min SDK

Android 11 (API 30). `createTrashRequest` requires API 30+.
