import java.io.BufferedReader;
import java.io.InputStreamReader;

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
        Token userToken = null;

        try
        {
            GroupClient groupClient = new GroupClient();
            FileClient fileClient = new FileClient();

            groupClient.connect(serverName,  GroupServer.SERVER_PORT);

            if (groupClient.isConnected())
            {
                System.out.println("Enter username to get token: ");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                userToken = (Token) groupClient.getToken(in.readLine()); // Create token for this user

                if(userToken == null)
                {
                    System.out.println("Your username was not recognized.");
                }
                else
                {
                    System.out.println("Username Accepted!");
                }

                groupClient.disconnect();

            }
            else
            {
                System.out.println("System error. Group Server is not running.");
            }

        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    } //-- end main(String[])

} //-- end class UserClient
