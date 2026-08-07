# strava-plus

Strava-plus is a project that allows you to analyze your cycling training. The main idea was to integrate many functions of the popular application "Strava", because some features of the app are only available with a subscription.

The project is written in Java, CSS, HTML, and SQL.

# There are five main files:

* SimplePostServer.java
This file enables the application to work. It contains a simple server that starts a website at http://localhost:8000/upload-html. Without starting this file, none of the project functions can be used.
* FileUploader.java
This file connects the user with the server. Its main task is to upload the GPX file provided by the user and send it to the server and the XmlReader.java file.
* SQLite.java
This file is responsible for communication with the database. It saves, searches, and loads data from the database. It works as a connection layer between the application and the database.
* XmlReader.java
This file processes the GPX file uploaded by the user. It reads the data, calculates different values that will later be displayed on the website, and creates the route visualization for the map.
* WorkoutAnalyzer.java
This file is responsible for analyzing training data. It mainly processes heart rate statistics and calculates different training parameters.

How to use the app:
1. Start the file SimplePostServer.java.
2. Start the file FileUploader.java.
3. Open the website: http://localhost:8000/upload-html
4. Choose the GPX file of your bike ride.
5. Click the button "Send to server".
