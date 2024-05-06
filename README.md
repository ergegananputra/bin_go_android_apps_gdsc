# BIN-GO!
<p align="center">
  <img src="https://github.com/aldyardnsyh/bin_go_android_apps_gdsc/assets/110091451/60de51ad-b687-4201-974d-9b87faa8891e" alt="bin-go_logo" width="200" height="300">
</p>

BIN-GO! is a mobile application developed to address the waste crisis in Yogyakarta. The app integrates the location of waste recycling centers through Google Maps, so users can easily access the nearest recycling facilities. In addition, the app is also equipped with a forum where people can discuss and share insights on waste management topics, thus fostering a sense of shared responsibility.

The latest addition to BIN-GO! is the report feature, which allows users to report various waste-related issues, such as littering in the neighborhood. This empowers users to take an active role in identifying and addressing environmental challenges within their communities.

In addition, BIN-GO! incorporates gamification elements to encourage user participation in sustainable behavior. Through challenges and rewards, users are motivated to adopt green practices and contribute to a cleaner environment.

By empowering users with information, facilitating community interaction, and gamifying sustainable behaviors, BIN-GO! strives to promote responsible waste management, disposal, and waste management practices and contribute to a cleaner and greener Yogyakarta.
<br>
> [!Note]
For now, the application can only be used in Indonesia, especially in the Special Region of Yogyakarta.

## About This Repository
This repository is the primary one used to develop the Bin-Go Android mobile application.

## Main Features
- Bin-Community
- Bin-Locator
- Bin-Report
- Bin-Point

## Tech Stack
[![Android](https://img.shields.io/badge/android-green.svg?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Firebase](https://img.shields.io/badge/firebase-yellow.svg?style=for-the-badge&logo=firebase&logoColor=white)](https://firebase.google.com/)
[![Kotlin](https://img.shields.io/badge/kotlin-orange.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Google Cloud](https://img.shields.io/badge/Google_Cloud-4285F4?style=for-the-badge&logo=google-cloud&logoColor=white)](https://cloud.google.com/)
[![Google Maps](https://img.shields.io/badge/Google_Maps-red?style=for-the-badge&logo=google-maps&logoColor=white)](https://maps.google.com/)

Bin-Go utilizes various Google technologies, including Android, Firebase (Firebase Authentication, Firebase Firestore, Firebase Storage), Google Maps, and Google Cloud Platform.


## Download
The BinGo Stable Version APK can be found in the APK folder directory here
<br><br>
<a href="https://github.com/ergegananputra/bin_go_android_apps_gdsc/tree/main/apk">
  <img src="https://github.com/aldyardnsyh/bin_go_android_apps_gdsc/assets/110091451/033805be-84f9-4dd8-8ef9-301be0abb4b6" alt="BinGo Version" width="120" height="25">
</a>

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

Make sure you add in your `local.properties` file the location of your SDK `sdk.dir=C\:\\`, `MAPS_API_KEY`, and `MAPS_PLACE_API_KEY` in it. You may also add `keystorePath`, `keystorePassword`, `keyAliasProperties`, and `keyPassword` for build and release configuration and setting up SHA for Firebase Authentication.

<img src="https://github.com/ergegananputra/bin_go_android_apps_gdsc/assets/126530940/8951fb05-d300-4d52-8ed7-752e7c3c5ba4" alt="image" width="600" height="300">
<br><br>

> [!Note]
Don't forget to change `path_to_your_sdk`, `your_place_api_key`, ..., and `your_key_password`.



## Environment Variables

Make sure before you run this project, you will need to add the following environment variables to your `google.services.json` file:

- `YOUR_API_KEY`: You can generate this API key in your [Google Cloud Console](https://console.cloud.google.com/apis/dashboard), and then copy your API key to this file.

<div style="text-align:center;">
  <img width="300" alt="Screenshot 2024-02-21 224231" src="https://github.com/ergegananputra/bin_go_android_apps_gdsc/assets/79182959/fa69c729-e0e1-4a55-8b4a-b937766b1976" style="display: inline-block;">
  <img width="400" alt="Screenshot 2024-02-21 224011" src="https://github.com/ergegananputra/bin_go_android_apps_gdsc/assets/79182959/976dd015-68b6-4a65-b4c5-f0683ee4c7ab" style="display: inline-block;">
</div>

<br>

> [!Note]
Don't forget to connect `console.firebase.google.com` in your project to connect to your project's storage database and sync the Gradle.

### Additional Requirement: Remote Settings

In addition to the environmental variables mentioned above, if you don't have a balance in your Google Cloud, you will need to set up a remote setup for your project like this:

1. Create a collection named `remote_settings` in your Firestore database
   <br><br>
   <img src="https://github.com/aldyardnsyh/bin_go_android_apps_gdsc/assets/110091451/20946ac1-8ef7-4de4-9161-6178aeaa4688" alt="remote_settings" width="550" height="300">
   <br><br>
2. Create a document with the ID `WumOchoPZ3LNf7XE8qMk`
   <br><br>
   <img src="https://github.com/aldyardnsyh/bin_go_android_apps_gdsc/assets/110091451/379c6818-88c1-445b-b442-6f77718d72eb" alt="document_id_remote_settings" width="250" height="110">
   <br><br>
3. Set the following fields in the document:
   <br><br>
   <img src="https://github.com/aldyardnsyh/bin_go_android_apps_gdsc/assets/110091451/c9234262-3f48-457d-9a2b-104d44f48d0c" alt="field_table_remote_settings" width="250" height="140">
   <br>
> [!Note]   
`isMapsEnabled`: Set to `true` if you have a balance in your Google Cloud.

   Example:
   ```json
   {
     "isMapsEnabled": true
   }
   ````
> [!Note]
If you don't have a balance in google cloud, you can also add a custom location for the `bin-locator` feature based on your nearest location by setting the fields and database contents in firestore like this: 
<br>
<p>
Set the <b>geohash</b> initial value to <b>null</b>. Then set the set the <b>isDeveloperMode</b> field in <b>remote_settings</b> collection to <b>true</b>. Run the application and open <b>Bin-Locator</b>. After the <b>geohash</b> filled up automatically, set the <b>isDeveloperMode</b> back to <b>false</b>. </p>
<br>
<img src="https://github.com/aldyardnsyh/bin_go_android_apps_gdsc/assets/110091451/7d835b76-b526-4560-bae0-8192325de313" alt="field_table_remote_settings" width="550" height="300">

## Preview 
[Preview App v.2](https://github.com/aldyardnsyh/bin_go_android_apps_gdsc/assets/110091451/4b077731-b273-4c0e-86f8-cbbb1bf7e00b)


## Demo Application

[Version 1]
<br>
[![Youtube](https://img.shields.io/badge/Youtube-v.1-red?style=for-the-badge&logo=youtube&logoColor=white)](https://youtu.be/zqYfTEWjGGE)
<br>
[Version 2]
<br>
 [![Youtube](https://img.shields.io/badge/Youtube-v.2-red?style=for-the-badge&logo=youtube&logoColor=white)](https://youtu.be/5ZP4Mnl60as)

## Authors
[![Github Erge](https://img.shields.io/badge/AdielBoanergeGananputra-black.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ergegananputra/)
[![LinkedIn Erge](https://img.shields.io/badge/LinkedIn-blue?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/adiel-boanerge-g-b95892275/)
<br>
[![Github Aldy](https://img.shields.io/badge/AldyArdiansyah-black.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ItsmeAldy17/)
[![LinkedIn Aldy](https://img.shields.io/badge/LinkedIn-blue?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/aldyardiansyah17/)
<br>
[![Github Naya](https://img.shields.io/badge/SalmaDewiNataya-black.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/salmanataya/)
[![LinkedIn Naya](https://img.shields.io/badge/LinkedIn-blue?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/salmanataya/)
<br>
[![Github Daniel](https://img.shields.io/badge/DanielWinstonMandela-black.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/danielwinstonmandela/)
[![LinkedIn Daniel](https://img.shields.io/badge/LinkedIn-blue?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/danielwinstonmandela/)


