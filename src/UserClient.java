import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
                                            "List all of the members of a group"};

        String[] adminOptions = new String[]{"Add (create) a user",
                                             "Remove (delete) a user"};

        while (true)
        {
            // reset the group client to reset the state of objects on the stream
            groupClient.reset();

            for (int i = 0; i < menuOptions.length; i++)
            {
                System.out.println(i+". \t"+menuOptions[i]);
            }

            // print admin options if they are an admin
            if (userToken.isAdmin())
            {
                for(int i = 0; i < adminOptions.length; i++){
                    System.out.println((i+menuOptions.length) + ". \t" + adminOptions[i]);
                }

            }

            System.out.print(username+" >> ");

            try
            {
                choice = inputValidation(in.readLine());
            }
            catch(IOException e)
            {
                System.out.println("Error parsing username. Exiting...");
            }

            // Options for Admins Only
            boolean selectedAdminOption = false;
            if(userToken.isAdmin()){
                switch(choice)
                {
                    case "6": // Create a user
                        System.out.println("Enter username of new user to create: ");
                        String newUserName = inputValidation(in.readLine());
                        if (groupClient.createUser(newUserName, userToken))
                        {
                            System.out.println("User created successfully! great");
                        }
                        selectedAdminOption = true;
                        break;
                        // TODO: ONLY OWNER OF ADMIN GROUP CAN DELETE OTHER ADMIN
                    case "7": // Delete a user
                        System.out.println("Enter username of user to delete: ");
                        String userToDelete = inputValidation(in.readLine());
                        if (groupClient.deleteUser(userToDelete, userToken))
                        {
                            System.out.println("User deleted successfully! great");
                        }
                        selectedAdminOption = true;
                        break;
                }
            }

            // Options for Any Users
            switch(choice)
            {
                case "0":
                    return;
                case "1": // Create a group
                    System.out.println("Enter a group name: ");
                    String newGroupName = inputValidation(in.readLine());
                    if (groupClient.createGroup(newGroupName, userToken))
                    {
                        System.out.println("Group creation succeeded!");
                    }
                    else
                    {
                        System.out.println("Group creation failed. :(");
                    }
                    break;
                case "2": // Delete a group
                    break;
                case "3": // Add a user to a group
                    // GET the owner of the group specified
                    // Make sure the userTo`ken matches the owner of the group
                    // then add the user to the group
                    System.out.println("Enter a user name to add: ");
                    String userToAdd = inputValidation(in.readLine());

                    System.out.println("Enter a group to add "+userToAdd+" to: ");
                    String groupToAddUserTo = inputValidation(in.readLine());

                    if (groupClient.addUserToGroup(userToAdd, groupToAddUserTo, userToken))
                    {
                        System.out.println(userToAdd+" added successfully to "+groupToAddUserTo+".");
                    }
                    else
                    {
                        // TODO update error warning, could fail if user doesn't exist, etc...
                        System.out.println("Failed to add user to group.");
                        System.out.println("Are you sure the group exists?");
                        System.out.println("You must be the owner of a group to add a user.");
                    }
                    break;
                case "4": // Remove a user from a group
                    System.out.println("Enter a group name that you're an owner of: ");
                    String group = inputValidation(in.readLine());

                    System.out.println("Enter the name of the user in "+group+" that you'd like to remove: ");
                    String userToRemove = inputValidation(in.readLine());

                    if (groupClient.deleteUserFromGroup(userToRemove,group, userToken))
                        System.out.println("Successfully deleted "+userToRemove+" from "+group+"!");
                    else
                    {
                        System.out.println("Deletion of "+userToRemove+" from "+group+" failed.");
                        System.out.println("Are you sure the group exists?");
                        System.out.println("You must be the owner of a group to remove a user from it.");
                    }

                    break;
                case "5": // List all the members of a group
                    System.out.println("Enter a group name you're a member of to list: ");
                    String groupName = inputValidation(in.readLine());
                    List<String> groupMembers = groupClient.listMembers(groupName, userToken);
                    if (groupMembers != null)
                    {
                        System.out.println(groupName+": ");
                        for (String memberName : groupMembers)
                            System.out.println(memberName);
                    }
                    else
                    {
                        System.out.println("Group does not exist");
                    }
                    break;
                case "-help":
                    System.out.println("You're screwed. Sorry...");
                    break;
                default:
                    if(!selectedAdminOption)
                    {
                        System.out.println("Command not recognized.");
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
