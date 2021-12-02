# Popular Movies Android App

It is an android based application that displays information from TheMovieDB.org on  Most Popular or Top Rated movies (according to the criteria selected by the user). It also contains a section to display the user's "Favorite" movies.<br>
## Download the Application from [Playstore](https://play.google.com/store/apps/details?id=eu.anifantakis.popularmovies)    <img src="https://github.com/kazi123kazi/Popular-Movies/blob/master/play_store_512.png" alt="logo" width="25" height="25" style="margin:5px">

![Master Activity](https://raw.githubusercontent.com/test2209/assets-udacity-and/master/project-2-popular-movies-2/screenshot-1.gif)   ![Detail Activity](https://raw.githubusercontent.com/test2209/assets-udacity-and/master/project-2-popular-movies-2/screenshot-2.gif)

![Detail Activity](https://raw.githubusercontent.com/test2209/assets-udacity-and/master/project-2-popular-movies-2/screenshot-3.gif)   ![Detail Activity](https://raw.githubusercontent.com/test2209/assets-udacity-and/master/project-2-popular-movies-2/screenshot-4.gif)

### These are the different features you will find on this app:

1. When you first access the app's main screen, you will see a circular progress bar indicating data is being fetched. When data has been received and it starts to load, a custom placeholder image will be shown on each spot where there will be a movie poster but it will fade out to show the actual poster after it's completely loaded.

2. Movie posters are displayed on a grid. The number of columns will adjust automatically depending on the device width and orientation. 

3. You can select the sorting criteria you would like to browse through by clicking or tapping on the side menu that shows the current selection. A dropdown menu will be displayed with the options available. Options are: "Most Popular" movies, "Top rated" movies or "Favorites".
The user interface will update automatically to respond to this new selection.

4. Once you've browsed through the posters and found a movie that you would like to learn more about, clicking or tapping on a poster will take you to a movie details screen where you can find additional data. These are arranged in Tab Layout. Three Tabs are there named as Movie Info, Trailers, Review.

5. “Movie Info” will show the movie's poster , title, release date, rating,synopsis.

6. Trailer tab will show different trailer images, title  in list view. If you click on a trailer thumbnail, an app will be launched to play its corresponding youtube video. 

7. If you click on " Reviews", a new tab screen will be displayed with the movie reviews by different users, name of reviewer etc.

8. You can always go back to the posters section and select a new movie.

### Special features:

1. The user can select a movie as a "Favorite" in the movie's details activity by clicking on a floating action button with a heart symbol. This selection can be toggled with the same button.

2. On device rotation on the main posters grid, the scroll position will be maintained so the user doesn't have to scroll down again to find the movie he/she was interested in. i.e. state is saved in bundles.

3. The movie details section has a collapsing toolbar(animation) that will show the movie's title in a smaller font for better user experience when scrolling down.

4. Infinite Scroll is there . Based on `onScrolllistener` we updated the data from rest api.


### Technical features:

1. Movie posters are loaded using the  **Picasso** library that handles image loading.

2. The user's favorite movie data is saved to an **SQLite database** through a Service, including images (poster, backdrop, trailer thumbnails). Images are first downloaded through Picasso's `.get( )` method and then they are saved to internal storage and their path is updated in the database.
3. Others android components used are  Tab layout,Coordinate Layout ,Recyclerview etc.
  
### Handling errors

1. No internet connection: If there is no internet connection available, a dialog box will alert the user. If the app has loaded data previously, this data will remain responsive but the user won't be able to request new data until there is an internet connection. The "Favorites' category will be displayed automatically when there is no internet connection.

2. If the user had no internet connection and no data was loaded previously but he/she reconnects and restarts the app, data will be fetched automatically.

3. Poster didn't load correctly: An error poster will be shown instead. On the main screen, the error poster will prompt the user to click if he/she wants to access the movie data, even if the poster is not available. Once inside the movie details screen, a second error poster will display a message saying that the poster couldn't be loaded.

4. No favorite movies selected: If the user tries to access the "Favorites" section without selecting favorite movies first, a dialog will warn him/her and the "Most Popular" section will be selected automatically so the user can select favorite movies.

5. No reviews available: A dialog will be displayed and the user will be taken automatically to the corresponding movie details activity.

6. No trailer available: A dialog will be displayed and the user will be taken automatically to the corresponding movie details activity.

### How to use ? 

Go to the `strings.xml` file and find the line

`<string name="network.api_key">PUT YOUR API KEY HERE</string>`

There you should replace this line with your actual **API KEY** and the application will be functional.

The API Key can be generated from the TMDB Website.

