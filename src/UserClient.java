import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserClient
{


    static String serverName = "localhost";
    static GroupClient groupClient = new GroupClient();
    static String username = "";

    public static void connectGroupServer()
    {
        //TODO
    }

    public static void connectFileServer()
    {
        //TODO
    }

    public static Token getToken(String username)
    {
        groupClient.connect(serverName,  GroupServer.SERVER_PORT);
        Token newToken = (Token) groupClient.getToken(username);

        if (groupClient.isConnected())
        {


            if(newToken == null)
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
        return newToken;
    }

    public static void chooseServer(UserList.User user)
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
                        connectGroupServer();

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

    public static void main(String args[])
    {
        System.out.println("Enter username to login: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String username = "";
        try
        {
            username = in.readLine();
            UserList.User user = new UserList.User(username, getToken(username));
            chooseServer(user);
        }
        catch(IOException e)
        {
            System.out.println("Error parsing username. Exiting...");
        }


    }

} //-- end class UserClient
