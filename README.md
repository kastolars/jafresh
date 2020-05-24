# JaFresh

It is estimated that 40% of food produced in the United States goes uneaten. A major contributor to this problem
is consumers throwing away food that has expired. The motivation behind this app is to create a simple way for
users to be able to create reminders based on expiration dates so that an expiring product will not be wasted.

Source: [How Food Expiration Dates Contribute to Food Waste](https://www.foodindustry.com/articles/how-food-expiration-dates-contribute-food-waste/)

Currently this project only works for Android devices; however a cross device project is a possibility in the future.

# Installation

To install the project, simply run `gradlew assembleDebug` at the root of the project. An APK file will be generated
in `ExpirationReminderProject\app\build\outputs\apk\debug`, which can be installed on any Android device.

# Usage

JaFresh has two reminder creation options. One is manual, where a user can input the name and expiration date
of an item themselves and add it to their list. Another uses text recognition to extract the expiration date
automatically and all that is required is a name. Once a reminder is created, it is stored in a list on the main
screen ordered from soonest to latest expiration. Reminders are automatically set for one week before, two days before,
one day before, and day of expiration at 05:00 local time. A user can swipe right to delete any item on the list.

# Roadmap

- MVP - complete
- Limit detection area to capture box - started
- Improve OCR detection regular expression robustness - not started
- Support for iOS devices; either project duplication in Swift or leverage React Native - not started

# License

[MIT](https://choosealicense.com/licenses/mit/)
