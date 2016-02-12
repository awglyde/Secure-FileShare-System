import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserClient
{

    static String serverName = "localhost";
    static GroupClient groupClient = new GroupClient();
    static String username = "";
    static UserList.User user;

    public static void connectGroupServer() throws IOException
    {
        //TODO
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        groupClient.connect(serverName,  GroupServer.SERVER_PORT);
        String choice = "";
        if (groupClient.isConnected())
        {
            System.out.println("Welcome to the group server! Please choose from the list of options.\n\n");

            while (true)
            {
                System.out.println("1. \tAdd (create) a user");
                System.out.println("2. \tRemove (delete) a user");
                System.out.println("3. \tAdd (create) a group");
                System.out.println("4. \tRemove (delete) a group");
                System.out.println("5. \tAdd a user to a group");
                System.out.println("6. \tRemove a user from a group");
                System.out.println("7. \tList all of the members of a group");
                System.out.println("0. \tDisconnect from group server");
                System.out.print(user.username+" >> ");

                try
                {
                    choice = in.readLine();
                }
                catch(IOException e)
                {
                    System.out.println("Error parsing username. Exiting...");
                }

                switch(choice)
                {
                    case "1":
                        if (user.isAdmin())
                            System.out.println("User is admin! great");
                        break;
                    case "2":
                        if (user.isAdmin())
                            System.out.println("User is admin! great");
                        break;
                    case "3":
                        break;
                    case "4":
                        break;
                    case "5":
                        break;
                    case "6":
                        break;
                    case "7":
                        break;
                    case "0":
                        return;
                    case "-help":
                        System.out.println("You're screwed. Sorry...");
                        break;
                    default:
                        System.out.println("Command not recognized");

                }
            }
        }
        else
        {
            System.out.println("System error. Group Server is not running.");
        }
        in.close();
        groupClient.disconnect();
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

    public static void chooseServer(UserList.User user) throws IOException
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
        in.close();
    }

    public static void main(String args[]) throws IOException
    {
        System.out.println("Enter username to login: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String username = "";
        try
        {
            username = in.readLine();
            user = new UserList.User(username, getToken(username));
            chooseServer(user);
        }
        catch(IOException e)
        {
            System.out.println("Error parsing username. Exiting...");
        }

        in.close();

    }

} //-- end class UserClient
