//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

/*
 * A server that delivers status messages to other users.
 */
public class Server {

	// Create a socket for the server
	private static ServerSocket serverSocket = null;
	// Create a socket for the server
	private static Socket userSocket = null;
	// Maximum number of users
	private static int maxUsersCount = 5;
	// An array of threads for users
	private static userThread[] threads = null;

	public static void main(String args[]) {

		// The default port number.
		int portNumber = 8000;
		if (args.length < 2) {
			System.out.println("Usage: java Server <portNumber>\n"
					+ "Now using port number=" + portNumber + "\n" +
					"Maximum user count=" + maxUsersCount);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			maxUsersCount = Integer.valueOf(args[1]).intValue();
		}

		System.out.println("Server now using port number=" + portNumber + "\n" + "Maximum user count=" + maxUsersCount);

		userThread[] threads = new userThread[maxUsersCount];

		/*
		 * Open a server socket on the portNumber (default 8000).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a user socket for each connection and pass it to a new user
		 * thread.
		 */
		while (true) {
			try {
				userSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxUsersCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(userSocket, threads);
						threads[i].start();
						break;
					}
				}
				if (i == maxUsersCount) {
					PrintStream output_stream = new PrintStream(userSocket.getOutputStream());
					output_stream.println("#busy");
					output_stream.close();
					userSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * Threads
 */
class userThread extends Thread {

	private String userName = null;
	private BufferedReader input_stream = null;
	private PrintStream output_stream = null;
	private Socket userSocket = null;
	private final userThread[] threads;
	private int maxUsersCount;
	private final userThread[] friends;

	public userThread(Socket userSocket, userThread[] threads) {
		this.userSocket = userSocket;
		this.threads = threads;
		maxUsersCount = threads.length;
		friends = new userThread[maxUsersCount - 1];
	}

	// method for sending a message to all, this is used when a new user leaves or
	// enters
	public void sendToAll(String msg) {
		synchronized (this.threads) {
			// cycles through all threads looking for user besides me!
			for (int i = 0; i < maxUsersCount; i++) {
				if (threads[i] != null && threads[i] != this) {
					threads[i].output_stream.println(msg);
				}
			}
		}
	}

	// only prints to friends, if you have no friends you will lonely
	public void sendToAllFriends(String msg) {
		// makes sure its synced up
		synchronized (this.threads) {
			// checks for your friends!
			for (int i = 0; i < maxUsersCount; i++) {
				if (friends[i] != null) {
					threads[i].output_stream.println(msg);
				}
			}
		}
	}

	// sends a message to only a specific user, doesn't have to be a friend yet but
	// when the creator originally made this method he misunderstood the directions,
	// he painfully fixed his mistake!
	public void friendMessage(String friendName, String message) {
		// cycles through all the threads to look for the user!
		for (int i = 0; i < maxUsersCount; i++) {
			if (threads[i] != null && threads[i] != this) {
				if (threads[i].userName.equals(friendName)) {
					threads[i].output_stream.println(message);

					break;
				}
			}
		}

	}

	// method to fidn the String name in the threads array and then return the
	// correct thread object
	public userThread findUser(String name) {
		// initializes the thread eventually to be returned
		userThread user = null;
		synchronized (this.threads) {
			for (int i = 0; i < maxUsersCount; i++) {
				if (threads[i] != null && threads[i] != this) {
					if (threads[i].userName.equals(name)) {
						// boom we did it, so we must break, I was getting tired its about time
						user = threads[i];
						break;
					}
				}
			}
		}
		return user;
	}

	// addFriend method where number of friends can't exceed maxUsersCount
	public void addFriend(String friendName) {
		int friend = 0;

		synchronized (this.threads) {
			// searches for the friend in the threads
			for (int i = 0; i < maxUsersCount; i++) {
				if (threads[i] != null && threads[i] != this) {
					if (threads[i].userName.equals(friendName)) {

						friend = i;
						break;

					}
				}
			}
			// seaches for an empty friend, don't want any of those!
			for (int i = 0; i < maxUsersCount - 1; i++) {
				if (friends[i] == null) {
					friends[i] = threads[friend];
					// breaks out of the loop
					break;
				}
			}

		}
	}

	// how to lose a friend :( ; this is a sad method
	public void removeFriend(String friendName) {
		synchronized (this.threads) {
			Thread x = findUser(friendName);
			for (int j = 0; j < maxUsersCount - 1; j++) {
				if (friends[j] != null && friends[j] == x) {
					friends[j] = null;

					break;
				}
			}
		}
	}

	// this is a very sad method, it removes all your friends, you will be lonely
	public void removeFriends() {
		synchronized (this.threads) {
			for (int j = 0; j < maxUsersCount - 1; j++) {
				if (friends[j] != null) {
					friends[j].removeFriend(userName);
				}
			}
		}
	}

	public void run() {
		int maxUsersCount = this.maxUsersCount;
		userThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client.
			 * Read user name.
			 */
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			output_stream = new PrintStream(userSocket.getOutputStream());

			// #fidn the username of the user
			String joinMsg = input_stream.readLine();
			userName = joinMsg.split(" ")[1];

			/* Welcome the new user. */
			output_stream.println("#welcome");
			sendToAll("#newuser " + userName);

			/* Start the conversation. */
			while (true) {
				// Read message from user
				String line = input_stream.readLine();
				// if a friend request is sent we must broadcast it to the correct user
				if (line.startsWith("#friendme")) {
					// breakup line into #friendme and <username>
					String friendName = line.split(" ")[1];
					// a helper buddy to find a new friend
					friendMessage(friendName, "#friendme " + userName);

				} else if (line.startsWith("#Bye")) {
					// if a user leaves :c friends need to know
					sendToAll("#Leave " + userName);
					output_stream.println("#Bye");
					removeFriends();
					break;
				}
				// if message=#unfriend
				else if (line.startsWith("#unfriend")) {
					// wow we are unfriending someone, this is sad
					String friendName = line.split(" ")[1];
					// removing the friend from our friends list
					removeFriend(friendName);
					// removing us from their friends list
					findUser(friendName).removeFriend(userName);
					// letting our once friend down softly
					findUser(friendName).output_stream.println("#NotFriends " + userName);
					// letting us know that we are no longer friends
					output_stream.println("#NotFriends " + friendName);
				}
				// else if message=#status send #status to all friends
				else if (line.startsWith("#status")) {
					// we need to find the message
					String status = line.split(" ")[1];
					synchronized (this.threads) {
						// we need to let out friends know of the good news or bad news: could be any
						// news
						for (int i = 0; i < maxUsersCount - 1; i++) {
							if (friends[i] != null) {
								friends[i].output_stream.println("#status " + userName + " " + status);
							}
						}
					}
					// if the friend request is accepted we must let the appropiate parties know
				} else if (line.startsWith("#friends")) {
					String words[] = line.split(" ");
					// adding our new friend
					addFriend(words[1]);
					// adding oursleves to our new friend's friends list
					findUser(words[1]).addFriend(userName);
					output_stream.println("#OKFriends " + userName + " " + words[1]);
					findUser(words[1]).output_stream.println("#OKFriends " + userName + " " + words[1]);
					// this is sad, we must let the user know that their friend request was denied
				} else if (line.startsWith("#DenyFriendRequest")) {
					String words[] = line.split(" ");
					findUser(words[1]).output_stream.println("#FriendRequestDenied " + words[1]);
				}

			}

			// conversation ended.

			/*
			 * Clean up. Set the current thread variable to null so that a new user
			 * could be accepted by the server.
			 */
			synchronized (userThread.class) {
				for (int i = 0; i < maxUsersCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the socket.
			 */
			input_stream.close();
			output_stream.close();
			userSocket.close();
		} catch (IOException e) {
		}
	}
}
