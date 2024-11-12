# CSC330--Android-Programming
Android Applications made for CSC 330

## App 1: TicTacToc
TicTacToc is an enhanced version of the well known Tic-Tac-Toe game. TicTacToc is an Android app that allows two people to play the game on a phone, with the additional feature that each play has to be done within a limited amout of time ... if a player does not play within the time limit they lose their turn. The app keeps track of the players' scores - the first player to win 5 games is the winner of the tournament. The following details are easy to understand if you download, install, and run the TicTacToc app.

The app starts on the scoring screen, which identifies the players by red and green buttons, and their scores as RatingBars. The "Start" button starts the play activity. The choice of which player goes first is random, but biased so that it is unlikely for one player to go first too many times in a row (more details below). The play interface indicates which player has to play, by displaying either a red button on the top left or a green button on top right of the screen. Below that is a ProgressBar that shows the time left for the current player. Finally the 3x3 board is below that, as a grid of buttons.

When a player selects a unplayed position in the grid, that button is colored to the player's color. If the play results in a win for that player, then the activity ends and returns to the scoring screen, where the RatingBar for the winning player is incremented. If a player reaches 5 wins, then the "Start" button is removed. If the play results in a draw, or the user uses the device's back button, then the activity then the activity ends and returns to the scoring screen, where nothing happens. If the play does not result in a win, then the player indicator is switched, the timer is reset, and the game continues. If a player fails to make a play within the time limit, then the player indicator is switched, the timer is reset, and the game continues.

## App 2: Talking Pictures List
The Talking Picture List app allows the user to associate a text description with each image in the phone's mediastore, and have that text spoken while viewing the image. The app must have an SQLite database (using the Room abstraction) that has entries that hold MediaStore image _IDs and the images' descriptive texts.

The app starts by selecting a random song from the phone's audio library and playing it in looping mode. While the music is playing a list of the images' micro-thumbnails and their descriptive texts is shown on the screen. When the user clicks on an element of the list the song is paused, and

If the image has an entry in the database then the descriptive text is spoken, and a dialog is raised showing the full size image. The dialog has a "Dismiss" button to close the dialog.
If the image does not have an entry in the database the a new activity is started to show the image while the user enters descriptive text. Before the activity ends the database is updated.
When the dialog is dismissed or the activity returns, the speaking is stopped (if it is still going) and the song is resumed. If the database was updated in the new activity, the list view is updated.

## App 3: Phlogging
The Phlogging app allows the user to store "phlog" entries on their phone, recording information about some event, observation, thought, etc. Phlog entries can be created, viewed, edited, deleted, etc., in the usual idiom of blogging. Each phlog entry must be capable of storing:
1. A title
2. The date and time the entry was created
3. Text
4. A photo taken with the camera
5. The location (lat/long)

This was the basic requirement for the assignment, the actual app has some other features added as well.

