
//package Part1;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

public class Server {

    private static ArrayList<UserThread> threads = new ArrayList<UserThread>(5);

    public static void main(String args[]) {

        // UserThread T1 = new UserThread("Thread-1");
        // T1.start();

        // UserThread T2 = new UserThread("Thread-2");
        // T2.start();

    }

    // method for parsing command line prompts
    public static String getSignal(String x) {
        for (int i = 0; i < x.length(); i++) {
            if (x.charAt(i) == ' ') {
                return x.substring(0, i);
            }
        }
        return null;
    }

    public static String getUser(String x) {
        int first_space = 0;
        int second_space = x.length();
        for (int i = 0; i < x.length(); i++) {
            if (x.charAt(i) == ' ') {
                first_space = i;
                break;
            }
        }
        for (int i = first_space + 1; i < x.length(); i++) {
            if (x.charAt(i) == ' ') {
                second_space = i;
            }
        }
        System.out.println(first_space);
        return x.substring(first_space + 1, second_space);
    }

    class UserThread extends Thread {
        private Thread t;
        private String name;
        public static ServerSocket servSock = null;
        public static Socket userSocket = null;
        public static BufferedReader input_stream = null;
        public static PrintStream output_stream = null;

        UserThread(String threadName) {
            this.name = threadName;
            System.out.println("Created the thread: " + this.name);

        }

        public void run(int x) {
            System.out.println("Running " + name);
            try {
                for (int i = 0; i < x; i++) {
                    System.out.println("Thread: " + name + ", " + i);
                    // Let the thread sleep for a while.
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                System.out.println("Thread " + name + " interrupted.");
            }
            System.out.println("Thread " + name + " exiting.");
        }

        public void start() {
            System.out.println("Starting " + name);
            if (t == null) {
                t = new Thread(this, name);
                t.start();
            }
        }
    }
}
