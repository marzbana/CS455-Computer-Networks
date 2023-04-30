//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class User extends Thread {

	// The user socket
	private static Socket userSocket = null;
	// The output stream
	private static PrintStream output_stream = null;
	// The input stream
	private static BufferedReader input_stream = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		// The default port.
		int portNumber = 8000;
		// The default host.
		String host = "localhost";

		if (args.length < 2) {
			System.out
					.println("Usage: java User <host> <portNumber>\n"
							+ "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try {
			userSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output_stream = new PrintStream(userSocket.getOutputStream());
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		/*
		 * If everything has been initialized then we want to write some data to the
		 * socket we have opened a connection to on port portNumber.
		 */
		if (userSocket != null && output_stream != null && input_stream != null) {
			try {
				/* Create a thread to read from the server. */
				new Thread(new User()).start();

				// Get user name and join the social net
				System.out.print("What is your name? ");
				String userName = inputLine.readLine();
				output_stream.println("#join " + userName);

				while (!closed) {
					String userInput = inputLine.readLine().trim();

					// Read user input and send protocol message to server
					if (userInput.equals("Exit")) {
						output_stream.println("#Bye");
						closed = true;
					}
					// else if user input is @connect send friend request to friend
					else if (userInput.startsWith("@connect")) {
						String[] tokens = userInput.split(" ");
						String friend = tokens[1];
						output_stream.println("#friendme " + friend);
					}
					// else if user input starts with @deny send friend request denial to friend
					else if (userInput.startsWith("@deny")) {
						String[] tokens = userInput.split(" ");
						String friend = tokens[1];
						output_stream.println("#DenyFriendRequest " + friend);
					}
					// else if user input starts with @friend send #friends + friendname to server
					else if (userInput.startsWith("@friend")) {
						String[] tokens = userInput.split(" ");
						String friend = tokens[1];
						output_stream.println("#friends " + friend);
						// if the if user input starts with @disconnect send #unfriend + friendname to
						// server: very sad :(
					} else if (userInput.startsWith("@disconnect")) {
						String[] tokens = userInput.split(" ");
						String friend = tokens[1];
						output_stream.println("#unfriend " + friend);
						// should be happy you're talking to your friend
					} else {
						output_stream.println("#status " + userInput);
					}

				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		/*
		 * Keep on reading from the socket till we receive a Bye from the
		 * server. Once we received that then we want to break.
		 */
		String responseLine;

		try {
			while (true) {
				// reads response from server during initial connection
				responseLine = input_stream.readLine();
				// it can be busy and then you automatically have no friends; maybe one will
				// exit and then you can try again later!
				if (responseLine.equals("#busy")) {
					System.out.println("Server is busy. Please try again later.");
					break;
					// this is happiness! you're in the social network!
				} else if (responseLine.equals("#welcome")) {
					System.out.println("Welcome to the social network!");
					break;

				}

			}

			while (!closed && (responseLine = input_stream.readLine()) != null) {

				// Display on console based on what protocol message we get from server.

				// else if #statusPosted
				if (responseLine.equals("#statusPosted")) {
					System.out.println(responseLine.split(" ")[1] + "received your message.");
				}
				// else if #newUser
				else if (responseLine.startsWith("#newuser")) {
					String[] tokens = responseLine.split(" ");
					String user = tokens[1];
					System.out.println(user + " has joined the social network!");
				}
				// else if #bye
				else if (responseLine.equals("#Bye")) {
					System.out.println("Bye!");
					break;
				}
				// else if #Leave
				else if (responseLine.startsWith("#Leave")) {
					String[] tokens = responseLine.split(" ");
					String user = tokens[1];
					System.out.println(user + " has left the social network!");
				}
				// else if user input starts with #OKfriends print "friend accepted your friend
				// request" very happy :)!!!!!!!!!!!
				else if (responseLine.startsWith("#OKFriends")) {
					String[] tokens = responseLine.split(" ");
					String friend = tokens[1];
					System.out.println(friend + " and " + tokens[2] + " are now friends!");
				}
				// else if user input starts with #FriendRequestDenied print "friend denied your
				// friend request"
				else if (responseLine.startsWith("#FriendRequestDenied")) {
					String[] tokens = responseLine.split(" ");
					String friend = tokens[1];
					System.out.println(friend + " denied your friend request!");
				}
				// else if user input starts with #friendMe print friend wants to be your friend
				else if (responseLine.startsWith("#friendme")) {
					String[] tokens = responseLine.split(" ");
					String friend = tokens[1];
					System.out.println(friend + " wants to be your friend. Type @friend " + friend
							+ " to accept or @deny " + friend + " to deny.");

				}
				// else if responseline starts with #NotFriends print you are no longer friends
				// with friend :c
				else if (responseLine.startsWith("#NotFriends")) {
					String[] tokens = responseLine.split(" ");
					String friend = tokens[1];
					System.out.println("You are no longer friends with " + friend);
				}
				// else if responseline starts with status print <friendname> sent you a
				// message:
				// message
				else if (responseLine.startsWith("#status")) {
					String[] tokens = responseLine.split(" ");
					String friend = tokens[1];
					String message = tokens[2];
					System.out.println(friend + " sent you a message: " + message);
				}
			}
			closed = true;
			output_stream.close();
			input_stream.close();
			userSocket.close();
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}
