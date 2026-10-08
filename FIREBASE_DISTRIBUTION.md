# Firebase App Distribution Setup

Firebase App Distribution is the **easiest way** to distribute your APK to testers without dealing with GitHub Actions build errors!

## 🔥 Setup Steps

### 1. Create Firebase Project

1. Go to: https://console.firebase.google.com/
2. Click **"Add project"**
3. Enter project name: **"Board Games"** or **"naman2548"**
4. Disable Google Analytics (optional for testing)
5. Click **"Create project"**

### 2. Add Android App

1. In your Firebase project, click the **Android icon**
2. Enter package name: **`com.namansuthar.games`**
3. App nickname: **"Board Games"**
4. Click **"Register app"**
5. **Skip** the google-services.json download for now
6. Click **"Next"** → **"Continue to console"**

### 3. Enable App Distribution

1. In Firebase Console, click **"Release & Monitor"** in left sidebar
2. Click **"App Distribution"**
3. Click **"Get started"**

### 4. Build Your APK

**Option A: GitHub Codespaces** (Recommended)
```bash
# In Codespaces terminal:
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

**Option B: Local Android Studio**
```bash
# In project directory:
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 5. Upload to Firebase App Distribution

1. In Firebase Console → App Distribution
2. Click **"Releases"** tab
3. Click **"Get started"**
4. **Drag and drop** your `app-debug.apk` file
5. Add release notes:
   ```
   Initial release v1.0.0
   
   Features:
   - 2048 game with undo and animations
   - Snake game with auto-play and speed controls
   - Tic-Tac-Toe with AI opponent (3 difficulty levels)
   - 3 home screen widgets
   - Material 3 theming
   ```
6. Click **"Next"**

### 6. Add Testers

**Option A: Add yourself**
1. Enter your email: `namansuthar12345@gmail.com`
2. Click **"Add"**

**Option B: Create tester group** (for multiple people)
1. Click **"Create group"**
2. Name: "Alpha Testers"
3. Add email addresses
4. Click **"Save"**

### 7. Distribute!

1. Select testers/groups
2. Click **"Distribute"**
3. You'll get an email with download link! 📧

---

## 📱 Installing from Firebase

### First Time Setup:

1. **Check your email** (namansuthar12345@gmail.com)
2. **Click "Get started"** in the email
3. **Download Firebase App Tester** app from Play Store
4. **Sign in** with your Google account
5. **Accept the invitation**
6. **Download and install** the APK!

### For Updates:

1. Get notified when new builds are uploaded
2. One-tap update from the App Tester app
3. Much easier than manual APK installation!

---

## 🤖 Automated Distribution (Optional)

### Setup GitHub Actions to Auto-Upload

Create `.github/workflows/firebase-distribution.yml`:

```yaml
name: Firebase App Distribution

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build_and_distribute:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Build APK
        run: |
          chmod +x gradlew
          ./gradlew assembleDebug
          
      - name: Upload to Firebase App Distribution
        uses: wzieba/Firebase-Distribution-Github-Action@v1
        with:
          appId: ${{ secrets.FIREBASE_APP_ID }}
          serviceCredentialsFileContent: ${{ secrets.FIREBASE_SERVICE_CREDENTIALS }}
          groups: alpha-testers
          file: app/build/outputs/apk/debug/app-debug.apk
          releaseNotes: "Automated build from commit ${{ github.sha }}"
```

### Get Firebase Credentials:

1. **Firebase App ID**:
   - Firebase Console → Project Settings
   - Scroll to "Your apps"
   - Copy the App ID (looks like `1:123456789:android:abc123...`)

2. **Service Account**:
   - Firebase Console → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Download the JSON file

3. **Add to GitHub Secrets**:
   - GitHub repo → Settings → Secrets and variables → Actions
   - Add `FIREBASE_APP_ID` with your App ID
   - Add `FIREBASE_SERVICE_CREDENTIALS` with the JSON content

---

## 🎯 Benefits Over GitHub Actions

✅ **No build errors** - Upload pre-built APKs  
✅ **Easy distribution** - Share via email/link  
✅ **Automatic updates** - Testers get notified  
✅ **Testing feedback** - Collect crash reports  
✅ **Version management** - Track all releases  
✅ **Professional** - Same tool used by real apps  

---

## 📊 Testing Workflow

### For You (Developer):

1. Build APK (Codespaces or locally)
2. Upload to Firebase App Distribution
3. Add release notes
4. Click distribute
5. Done! ✅

### For Testers:

1. Get email notification
2. Open Firebase App Tester app
3. Tap "Update"
4. Test and report feedback
5. Get next update automatically

---

## 🐛 Troubleshooting

**"App not found in tester app"**
- Make sure you accepted the invitation email
- Check you're signed in with the right Google account

**"Can't install"**
- Enable "Install from unknown sources"
- Make sure Firebase App Tester has install permissions

**"Upload failed"**
- Check APK is valid (try installing locally first)
- Make sure package name matches: `com.namansuthar.games`

---

## 📝 Quick Start Summary

1. **Create Firebase project** (2 minutes)
2. **Build APK** in Codespaces (5 minutes)
3. **Upload to Firebase** (1 minute)
4. **Add yourself as tester** (1 minute)
5. **Install from email link** (2 minutes)

**Total: ~10 minutes to test your app!** 🚀

---

## Resources

- Firebase Console: https://console.firebase.google.com/
- Firebase App Distribution Docs: https://firebase.google.com/docs/app-distribution
- Firebase App Tester (Play Store): https://play.google.com/store/apps/details?id=com.google.firebase.appdistribution

---

**This is WAY easier than fixing GitHub Actions build errors!** 🎉
