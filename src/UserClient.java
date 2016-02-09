import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserClient
{

    /**
     * Main method.
     *
     * @param args (No Arguments)
     */
    static String serverName = "localhost";

    public static void main(String[] args)
    {
        menu();
    }

    public static Token connectGroupServer()
    {

        Token userToken = null;
        GroupClient groupClient = new GroupClient();
        groupClient.connect(serverName,  GroupServer.SERVER_PORT);

        if (groupClient.isConnected())
        {
            System.out.println("Enter username to get token: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            String username = "";
            try
            {
                 username = in.readLine();
            }
            catch(IOException e)
            {
                System.out.println("Error parsing username. Exiting...");
            }

            userToken = (Token) groupClient.getToken(username);

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
        return userToken;
    }

    public static void connectFileServer()
    {
        FileClient fileClient = new FileClient();

    }


    public static void menu()
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try
        {
            while(true)
            {
                System.out.println("1. \tGroup Server");
                System.out.println("2. \tFile Server");
                System.out.println("0. \tExit");

                String choice = in.readLine();

                switch(choice)
                {
                    case "1":
                        System.out.println("Group Server");
                        Token userToken = connectGroupServer();

                        break;
                    case "2":
                        connectFileServer();

                        break;
                    case "0":
                        System.out.println("Exiting");

                        return;
                    case "-help":
                        System.out.println("You're screwed. Sorry...");

                        break;
                    default:
                        System.out.println("Command not recognized");

                }

            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }


    }


} //-- end class UserClient
