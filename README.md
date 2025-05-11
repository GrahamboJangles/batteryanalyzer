# Battery Analyzer

Battery Analyzer is a simple Android application that displays a pie chart showing battery usage by app. The app helps users identify which applications consume the most battery over the longest period possible.

## Features

- Displays a pie chart of battery usage by app
- Shows the percentage of battery each app consumes
- Analyzes data over the longest historical period available (up to 1 year)
- Requires usage access permission to access battery statistics
- Simple and clean user interface

## Setup

1. Clone this repository
2. Open the project in Android Studio
3. Build and run the app on your device

## Permissions Required

The app requires the following permissions:

- `BATTERY_STATS` - To access battery usage statistics
- `PACKAGE_USAGE_STATS` - To access app usage statistics

These are system permissions that require special access. The app will guide users to grant these permissions when first launched.

## How It Works

The app uses the UsageStatsManager API to collect app usage statistics. It analyzes this data to determine which apps have been consuming the most battery over time. The analysis is based on the amount of time each app has spent in the foreground, which is a good proxy for energy consumption.

The results are displayed in a pie chart showing the top 10 battery-consuming applications with their respective percentages.

## Libraries Used

- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - For displaying the pie chart

## License

This project is licensed under the MIT License - see the LICENSE file for details. 