## AuctionHouse
- Functioning Auction House with Server/Client architecture
- Reads/writes to AWS cloud-hosted Atlas MongoDB database
- Full GUI for client side
- Utility .jars for server side.

![Image Unavailable](login.png?raw=true)

## Usage (Server):
- To start the server, first run “Stop Server.jar” to ensure that any pre-existing instances of the server are closed. If you wish to reset the cloud database, run “Reset Server.jar”. This will delete all existing user accounts and reset the items to a few pre-set item listings. Finally, to start the server itself, run “server.jar”. Now, clients are free to connect using “client.jar”.

## Usage (Client):
- After ensuring that the server is running, connect by running “client.jar”. On the following login screen, you can (1) log in with a previously created account, (2) create an account, (3) log in as a guest user that cannot be logged into again. 
- On the home screen of the auction, all listed items are displayed on the table to the right, from which items can be selected to bring up bidding information on the left. You can use the search bar to filter items and/or select to hide auctions that have ended. The reset button will reset these filters. You can choose to return to this home screen at any time by clicking “Home” on the top bar.
- After selecting an item, you will either see a message explaining how the bid has ended, or information on how to place your bids. The status of the bid (success or fail) will be displayed immediately upon placing bid. A history box for this specific listed item is also displayed, showing all previous bids on the item. The “Auction History” at the bottom of the page displays bid notifications from all users and items. It will also display a notice when new listings are added.
- On clicking the profile button in the top right, a dropdown appears with options to (1) add funds, (2) view account settings, (3) sign out, (4) exit. Sign out will bring you back to the login screen, and exit will terminate the client.
- Choosing add funds will pop up a new window in which *fake* credit card information can be inputted to add funds to the current account. Saving the card will save the information for next time you choose to add funds. The status (success or fail) will be displayed immediately upon adding the funds. You can choose to go back home in the top left.
- Choosing account settings (this can also be accessed from the top bar) will give an option to change the password for the current account. The status (success or fail) of the change will be displayed immediately. You can choose to go back home in the top left.
- Choosing new listing from the top bar will bring you to a page where you can create your own personal listing. No duplicate item names are allowed. The description and image link are optional. The starting prices is required to be lower than the “Buy Now” price. The format for the end time of the auction is provided. The status (success or fail) of the listing creation will be displayed immediately. You can choose to go back home in the top left.


## List of client features:
- Account creation, logging in with these created accounts, temporary guest accounts
- Real-time updates of all items in table and notifications log
- More information on items available once selected from table
- Placing bids (detects invalid inputs and displays appropriate error messages)
- Different minimum prices for each item
- Different auction closing times for each item
- “Buy it now” options for each item
- Bid history available for each item
- Bid history available for entire auction
- Descriptions for each items available along with pictures (if provided when listed)
- Search bar for items
- Personal wallet feature - can add money using *fake* credit card
- Can save credit card information for future use
- Can change password for created accounts
- Passwords are salted and hashed (encrypted)
- Can add personal item listings that will be displayed in the table and can be bid on


## Structure:
The code is structured as two separate independent Java programs, one for the server and one for the client. The server first starts a server socket, then polls for any connection requests from clients. Once connected, the server creates a new socket for the client and a “client handler” specifically for that client. The client handler will send the client all item information at this point. The server will then take any “sign-up” or “login” information from the client, making sure either the account exists or a new account is created (the server logs these accounts on the database). Note that the password information is encrypted using salting and hashing. After logging in, the server waits for commands from clients, such as bidding information, password changes, and new listings. Private information such as password changes and funds added are reported only back to the client the server received the information from, but public information such as bids and new listings are broadcasted to all Observers (clients). When the Observable server updates, all clients will update listing information and history/notifuication logs. When closed, clients notify the server to close the corresponding port. To close the server, “Stop Server.jar” connects as a client, and sends a “terminate server” command that closes the server socket. “Reset Server.jar” connects to the Atlas MongoDB database hosted on AWS and resets the items collection using a backup collection, and deletes all users from the users collection.
