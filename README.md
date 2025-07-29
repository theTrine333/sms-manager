# expo-sms-manager

Custom Expo module (Android only) to manage SMS:

- ✅ `getAllSms()`: fetch all SMS entries
- ✅ `getRecentThreads()`: fetch most recent message per thread, with contact name
- ✅ `sendSms(number, message)`
- ✅ `deleteSms(id)`

## Usage

Add permissions to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.WRITE_SMS" />
```

1. Place this folder under your monorepo’s `packages/`.
2. Run `yarn` or `npm install`, then build via `expo prebuild`.
3. Request runtime permissions in your React Native code using your preferred permissions lib.
4. Use `getRecentThreads()` on your React Native UI to get contact + last message details.

Happy to extend!
