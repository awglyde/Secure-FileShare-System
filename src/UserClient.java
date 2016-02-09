import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UserClient
{

    /**
     * Main method.
     *
     * @param args (No Arguments)
     */
    public static void main(String[] args)
    {
        String serverName = "localhost";

        try
        {
            GroupClient groupClient = new GroupClient();
            FileClient fileClient = new FileClient();
            // Connect to the specified server
            final Socket sock = new Socket(serverName, GroupServer.SERVER_PORT);
            System.out.println("Connected to " + serverName + " on port " + GroupServer.SERVER_PORT);
            if (groupClient.isConnected())
            {

                System.out.println("Enter username to get token: ");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                groupClient.getToken(in.readLine());
            }
            else
            {
                System.out.println("System error. Group Server is not running.");
            }

            // shut things down
            sock.close();

        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    } //-- end main(String[])

} //-- end class UserClient
