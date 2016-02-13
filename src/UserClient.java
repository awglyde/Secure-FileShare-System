import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UserClient
{

    static String serverName = "localhost";
    static GroupClient groupClient = new GroupClient();
    static String username = "";
    static UserList.User user;

    public static void connectGroupServer() throws IOException
    {
        //TODO
        groupClient.connect(serverName,  GroupServer.SERVER_PORT);
        if (groupClient.isConnected())
        {
            groupOptions();
        }
        else
        {
            System.out.println("System error. Group Server is not running.");
        }

        groupClient.disconnect();
    }

    public static void groupOptions() throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String choice = "";
        System.out.println("Welcome to the group server! Please choose from the list of options.\n\n");
        String[] menuOptions = new String[]{"Disconnect from group server",
                                            "Add (create) a group",
                                            "Remove (delete) a group",
                                            "Add a user to a group",
                                            "Remove a user from a group",
                                            "List all of the members of a group",
                                            "Add (create) a user",
                                            "Remove (delete) a user"};

        while (true)
        {

            for (int i = 0; i < menuOptions.length-2; i++)
            {
                System.out.println(i+". \t"+menuOptions[i]);
            }

            if (user.isAdmin())
            {
                System.out.println("6. \t"+menuOptions[6]);
                System.out.println("7. \t"+menuOptions[7]);
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

                    case "0":
                        in.close();
                        return;
                    case "1": // Create a group
                        
                        break;
                    case "2": // Delete a group
                        break;
                    case "3": // Add a user to a group
                        break;
                    case "4": // Remove a user from a group
                        break;
                    case "5": // List all the members of a group
                        break;
                    case "6": // Create a user
                        System.out.println("User is admin! great");
                        break;
                    case "7": // Delete a user
                        System.out.println("User is admin! great");
                        break;
                    case "-help":
                        System.out.println("You're screwed. Sorry...");
                        break;
                    default:
                        System.out.println("Command not recognized");

                }
            }
            else
            {
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
                    case "0":
                        in.close();
                        return;
                    case "1": // Create a group
                        break;
                    case "2": // Delete a group
                        break;
                    case "3": // Add a user to a group
                        break;
                    case "4": // Remove a user from a group
                        break;
                    case "5": // List all the members of a group
                        break;
                    case "-help":
                        System.out.println("You're screwed. Sorry...");
                        break;
                    default:
                        System.out.println("Command not recognized");

                }
            }
        }
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
