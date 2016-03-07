# SpotifyStreamerApp
### Media player app streaming songs using Spotify API.
#### This App is compatible with Android phones and tablets

This project is based on a MVP architecture.


## What is implemented in this project?

- Service: To handle the media player in background. I handle the queue of message inside the service using a HandlerThread.
  The service sends message back to the activity via broadcast receivers.

- BroadCast receivers: I used them inside the service to broadcast informations inside my fragments.

- Pending intents: I used them for broadcast messages with broadcast receivers and also notifications

- I get artist and artist track using the Spotify API which returns me data via JSON format that I parse inside an Asynctask class using retrofit.

- I bind data receive from the asynctask via a recyclerView Adapter.

- Notification: I have created a custom notification using RemoteViews to notify the user about the current playing song. The user can go to the next or previous song using that notification

- Shared Preferences: I used it to save the settings of the user. The user is able to choose his country.

- Caching: I cache image from the web using Picasso.

- Sharing: User is able to share songs using a sharedActionProvider


![Alt text](https://raw.githubusercontent.com/princeCoder/SpotifyStreamerApp/master/device-2016-03-07-061515.png )

![Alt text](https://raw.githubusercontent.com/princeCoder/SpotifyStreamerApp/master/device-2016-03-07-061622.png )

![Alt text](https://raw.githubusercontent.com/princeCoder/SpotifyStreamerApp/master/device-2016-03-07-062212.png )

![Alt text](https://raw.githubusercontent.com/princeCoder/SpotifyStreamerApp/master/device-2016-03-07-061750.png )

![Alt text](https://raw.githubusercontent.com/princeCoder/SpotifyStreamerApp/master/device-2016-03-07-062301.png)
