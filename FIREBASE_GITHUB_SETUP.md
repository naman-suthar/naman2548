# Firebase App Distribution - GitHub Actions Setup

This guide will help you set up automated APK distribution using GitHub Actions and Firebase App Distribution.

## 🔑 Step 1: Get Firebase App ID

1. Go to Firebase Console: https://console.firebase.google.com/project/app-tester-dcb67/settings/general
2. Scroll down to **"Your apps"** section
3. Find your Android app: **com.namansuthar.games**
4. Copy the **App ID** (looks like `1:376485259173:android:8b848eda1d77f89d172a4c`)

**Your App ID**: `1:376485259173:android:8b848eda1d77f89d172a4c`

## 🔐 Step 2: Generate Firebase Service Account

1. Go to: https://console.firebase.google.com/project/app-tester-dcb67/settings/serviceaccounts/adminsdk
2. Click **"Generate new private key"**
3. Click **"Generate key"** in the confirmation dialog
4. A JSON file will be downloaded (e.g., `app-tester-dcb67-firebase-adminsdk-xxxxx.json`)
5. **Keep this file secure** - it contains credentials!

## 🔒 Step 3: Add Secrets to GitHub

1. Go to your GitHub repository: https://github.com/naman-suthar/naman2548
2. Click **Settings** tab
3. Click **Secrets and variables** → **Actions** (in left sidebar)
4. Click **"New repository secret"**

### Add Secret #1: FIREBASE_APP_ID

- **Name**: `FIREBASE_APP_ID`
- **Value**: `1:376485259173:android:8b848eda1d77f89d172a4c`
- Click **"Add secret"**

### Add Secret #2: FIREBASE_SERVICE_CREDENTIALS

- **Name**: `FIREBASE_SERVICE_CREDENTIALS`
- **Value**: Open the downloaded JSON file and **copy the entire contents**
  ```json
  {
    "type": "service_account",
    "project_id": "app-tester-dcb67",
    "private_key_id": "...",
    "private_key": "-----BEGIN PRIVATE KEY-----\n...",
    ...
  }
  ```
- Paste the entire JSON content into the secret value
- Click **"Add secret"**

## 👥 Step 4: Add Testers in Firebase

1. Go to: https://console.firebase.google.com/project/app-tester-dcb67/appdistribution
2. Click **"Testers & Groups"** tab
3. Click **"Add Group"**
4. Group name: **testers**
5. Add emails:
   - `namansuthar12345@gmail.com`
   - (add more testers as needed)
6. Click **"Save"**

**Important**: The group name must be `testers` to match the workflow configuration!

## ✅ Step 5: Test the Workflow

### Option A: Push to Branch
```bash
git add .
git commit -m "Add Firebase distribution workflow"
git push origin main
```

### Option B: Manual Trigger
1. Go to: https://github.com/naman-suthar/naman2548/actions
2. Click **"Firebase App Distribution"** workflow
3. Click **"Run workflow"** dropdown
4. Select branch: `main`
5. Click **"Run workflow"** button

## 📱 Step 6: Receive the APK

1. **Check your email** (namansuthar12345@gmail.com)
2. You'll receive: "New build available on Firebase App Distribution"
3. **Click "Get started"** in the email
4. **Install Firebase App Tester** from Play Store (first time only)
5. **Download and install** the APK!

## 🔄 How It Works

### Automatic Distribution
Every time you push to `main` or any `claude/*` branch, the workflow will:
1. ✅ Build the debug APK
2. ✅ Upload it as a GitHub artifact
3. ✅ Distribute it to the "testers" group via Firebase
4. ✅ Send email notifications to all testers

### Manual Distribution
You can also trigger the workflow manually from the Actions tab anytime!

## 🐛 Troubleshooting

### "Workflow failed at Firebase Distribution step"
- **Check**: FIREBASE_APP_ID is correct
- **Check**: FIREBASE_SERVICE_CREDENTIALS contains valid JSON
- **Check**: Service account has "Firebase App Distribution Admin" role

### "Testers group not found"
- Make sure you created a group named exactly `testers` (lowercase) in Firebase Console
- Alternative: Change `groups: testers` to `groups: alpha-testers` in the workflow file

### "Build failed"
- Check the build logs in GitHub Actions
- The workflow runs on `ubuntu-latest` which has full internet access (unlike Codespaces)
- It will download all required Android SDK components automatically

## 📊 Workflow Status

View all workflow runs:
- https://github.com/naman-suthar/naman2548/actions

Download artifacts (APK files):
- Click on any workflow run → Scroll to "Artifacts" section → Download `app-debug`

## 🎯 Next Steps

1. ✅ Set up the Firebase credentials (Steps 1-3)
2. ✅ Create the testers group (Step 4)
3. ✅ Push this commit to trigger the workflow (Step 5)
4. ✅ Wait for the email and install the app (Step 6)
5. 🎮 Test all three games and widgets!

---

**Estimated Setup Time**: 10 minutes  
**First Build Time**: 5-8 minutes (subsequent builds: 2-3 minutes with cache)

🚀 **After setup, every commit automatically distributes to your testers!**
