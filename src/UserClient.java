import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UserClient
{

    static String serverName = "localhost";
    static GroupClient groupClient = new GroupClient();
    static String username = "";
    static UserToken userToken = null;
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

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

            if (userToken.isAdmin())
            {
                System.out.println("6. \t"+menuOptions[6]);
                System.out.println("7. \t"+menuOptions[7]);
                System.out.print(username+" >> ");

                try
                {
                    choice = inputValidation(in.readLine());
                }
                catch(IOException e)
                {
                    System.out.println("Error parsing username. Exiting...");
                }

                // Admin group options
                switch(choice)
                {

                    case "0":

                        return;
                    case "1": // Create a group
                        System.out.println("Enter a group name: ");
                        String newGroupName = inputValidation(in.readLine());
                        if (groupClient.createGroup(newGroupName, userToken)) {
                            System.out.println("Group creation succeeded!");
                        }
                        else
                            System.out.println("Group creation failed. :(");
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
                        System.out.println("Enter username of new user to create: ");
                        String newUserName = inputValidation(in.readLine());
                        if (groupClient.createUser(newUserName, userToken))
                            System.out.println("User created successfully! great");
                        break;
                    case "7": // Delete a user
                        System.out.println("Enter username of user to delete: ");
                        String userToDelete = inputValidation(in.readLine());
                        if (groupClient.deleteUser(userToDelete, userToken))
                            System.out.println("User deleted successfully! great");
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
                System.out.print(username+" >> ");
                try
                {
                    choice = inputValidation(in.readLine());
                }
                catch(IOException e)
                {
                    System.out.println("Error parsing username. Exiting...");
                }

                // User group options
                switch(choice)
                {
                    case "0":

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
            groupClient.disconnect();
        else
            System.out.println("System error. Group Server is not running.");

        return newToken;
    }

    public static void chooseServer() throws IOException
    {

        try
        {
            while(true)
            {
                System.out.println("1. \tGroup Server");
                System.out.println("2. \tFile Server");
                System.out.println("0. \tExit");
                System.out.print(username+" >> ");

                String choice = inputValidation(in.readLine());

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

    public static String inputValidation(String input)
    {
        // Validate input here with a regex eventually
        return input;
    }

    public static void main(String args[]) throws IOException
    {
        System.out.println("Enter username to login: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try
        {
            username = inputValidation(in.readLine());
            userToken = getToken(username);
            if (userToken != null) {
                System.out.println("Token acquired!");
                chooseServer();
            }
            else
                System.out.println("Your username was not recognized. Contact administrator");
        }
        catch(IOException e)
        {
            System.out.println("Error parsing username. Exiting...");
        }


        in.close();
    }

} //-- end class UserClient
