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
					// reads input from user
					String userInput = inputLine.readLine().trim();

					// Read user input and send protocol message to server
					// if they want to leave send bye to server
					if (userInput.equals("Exit")) {
						output_stream.println("#Bye");
						closed = true;
					} else {
						// or the other option is a status update which is sent to server
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
				// reads server input
				responseLine = input_stream.readLine();
				if (responseLine.equals("#busy")) {
					// if server is busy then send a message to user
					System.out.println("Server is busy. Please try again later.");
					break;

				} else if (responseLine.equals("#welcome")) {
					// prints welcome to the server
					System.out.println("Welcome to the social network!");
					break;

				}

			}

			while (!closed && (responseLine = input_stream.readLine()) != null) {

				// Display on console based on what protocol message we get from server.

				// if message is #newStatus send the status out to the user
				if (responseLine.startsWith("#newStatus")) {
					String[] tokens = responseLine.split(" ");
					String user = tokens[1];
					String message = tokens[2];
					System.out.println(user + " says: " + message);
				}
				// else if #statusPosted
				else if (responseLine.equals("#statusPosted")) {
					System.out.println("Status posted!");
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
