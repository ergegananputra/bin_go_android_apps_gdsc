# Bin-Go
![bingo 500x500](https://github.com/ergegananputra/bin_go_android_apps_gdsc/assets/126530940/8b0d3f22-dd6d-4103-942d-82e5e6c01a12)

BIN-GO! is a mobile application  that aims to address the waste crisis in Jogja that integrates waste recycling site locators via Google Maps, a forum for community engagement on waste management topics, and a gamification feature incentivizing user participation. By empowering users with information, fostering community interaction, and gamifying sustainable behaviors, the app seeks to promote responsible waste disposal practices and contribute to a cleaner, more environmentally friendly Jogja.

Bin-Go utilizes various Google technologies, including Android, Firebase (Firebase Authentication, Firebase Firestore, Firebase Storage), Google Maps, and Google Cloud Platform.

## About This Repository
This repository is the primary one used to develop the Bin-Go Android mobile application.

## Download
The BinGo APK can be found in the APK folder directory here. 
(https://github.com/ergegananputra/bin_go_android_apps_gdsc/tree/main/apk)

## How to Use This Repo
It’s recommended to have Android Studio installed and to install any SDK packages that it recommends. 
This project is built based on Android API version 34.

In Android Studio:
1. Click "Main Menu" (alt + \\) to open Main Menu
2. Click on "File" menu, which will show a sub-menu
3. Click on the "New" option in the sub-menu, which will show another sub-menu
4. Click "Project from Version Control.."
5. Click the Version Control input fields and choose "Git"
6. in the URL input field, paste the following code:
   ```bash
   https://github.com/ergegananputra/bin_go_android_apps_gdsc
   ```
7. Choose the Directory where you want to save the copy of the repository on your local machine.
8. Click "Clone"


Alternatively, you can clone this repo using the CLI:
```bash
git clone https://github.com/ergegananputra/bin_go_android_apps_gdsc.git
```

Then, open Android Studio and click “Open an existing Android Studio Project”. Locate the cloned folder and wait for the Gradle to build.


## Preparation

Make sure you add in your `local.properties` file the location of your sdk `sdk.dir=C\:\\`, `MAPS_API_KEY` and `MAPS_PLACE_API_KEY` in it.

<img width="446" alt="Screenshot 2024-02-21 224011" src="https://github.com/ergegananputra/bin_go_android_apps_gdsc/assets/79182959/452befd5-87d7-49bf-a2e9-41c9cce62c28">

## Environment Variables

Make sure before you run this project, you will need to add the following environment variables to your `google.services.json` file


 `YOUR_API_KEY` you can build in your project `[console.google.com](https://console.cloud.google.com/apis/dashboard)` an then copy your api_key to this file

<div style="text-align:center;">
  <img width="300" alt="Screenshot 2024-02-21 224231" src="https://github.com/ergegananputra/bin_go_android_apps_gdsc/assets/79182959/fa69c729-e0e1-4a55-8b4a-b937766b1976" style="display: inline-block;">
  <img width="400" alt="Screenshot 2024-02-21 224011" src="https://github.com/ergegananputra/bin_go_android_apps_gdsc/assets/79182959/976dd015-68b6-4a65-b4c5-f0683ee4c7ab" style="display: inline-block;">
</div>


Note: Don't forget to connect `console.firebase.google.com` in your project to connect to your project's storage database

## Demo Application
Youtube : https://youtu.be/zqYfTEWjGGE\

## Authors

- [@ergegananputra](https://github.com/ergegananputra)
- [@aldyardiansyah](https://github.com/ItsmeAldy17)
- [@salmanataya](https://github.com/salmanataya)
- [@danielwinstonmandela](https://github.com/danielwinstonmandela)


