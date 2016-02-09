import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Simple client class.  This class connects to an EchoServer to send
 * text back and forth.  Java message serialization is used to pass
 * Message objects around.
 *
 * @author Adam J. Lee (adamlee@cs.pitt.edu)
 */
public class UserClient
{

    /**
     * Main method.
     *
     * @param args First argument specifies the server to connect to
     */
    public static void main(String[] args)
    {
        String serverName = "localhost";

        // Error checking for arguments
        if (args.length != 1)
        {
            System.err.println("Not enough arguments.\n");
            System.err.println("Usage:  java EchoClient <Server name or IP>\n");
            System.exit(-1);
        }

        try
        {
            GroupClient groupClient = new GroupClient();
            FileClient fileClient = new FileClient();
            // Connect to the specified server
            final Socket sock = new Socket(serverName, EchoServer.SERVER_PORT);
            System.out.println("Connected to " + serverName + " on port " + EchoServer.SERVER_PORT);

            // shut things down
            sock.close();

        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    } //-- end main(String[])


    /**
     * Simple method to print a prompt and read a line of text.
     *
     * @return A line of text read from the console
     */
    private static String readSomeText()
    {
        try
        {
            System.out.println("Enter a line of text, or type \"EXIT\" to quit.");
            System.out.print(" > ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            return in.readLine();
        } catch (Exception e)
        {
            // Uh oh...
            return "";
        }

    } //-- end readSomeText()

} //-- end class EchoClient
