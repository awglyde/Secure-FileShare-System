import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UserClient
{

    static String serverName = "localhost";
    static GroupClient groupClient = new GroupClient();
    static FileClient fileClient = new FileClient();
    static String username = "";
    static UserToken userToken = null;
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static void connectGroupServer() throws IOException
    {
        groupClient.connect(serverName,  GroupClient.SERVER_PORT);
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
        String choice = "";
        System.out.println("Welcome to the group server! Please choose from the list of options.\n\n");

        String[] menuOptions = new String[]{"Disconnect from group server",
                                            "Add (create) a group",
                                            "Remove (delete) a group",
                                            "Add a user to a group",
                                            "Remove a user from a group",
                                            "List all of the members of a group (that you are a memeber of)"};

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
                System.out.println("Error parsing input. Exiting...");
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
                            System.out.println("User created successfully!");
                        }
                        else
                        {
                            System.out.println("User creation failed!");
                            System.out.println("You cannot create a user with a duplicate username.");
                        }
                        selectedAdminOption = true;
                        break;
                        // TODO: ONLY OWNER OF ADMIN GROUP CAN DELETE OTHER ADMIN
                    case "7": // Delete a user
                        System.out.println("Enter username of user to delete: ");
                        String userToDelete = inputValidation(in.readLine());
                        if (groupClient.deleteUser(userToDelete, userToken))
                        {
                            System.out.println("User deleted successfully!");
                        }
                        else
                        {
                            System.out.println("User deletion failed!");
                            System.out.println("Does this user exist?");
                            System.out.println("Are you trying to delete the owner of the admin group?");
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
                        System.out.println("Group "+newGroupName+" created successfully!");
                    }
                    else
                    {
                        System.out.println("Group creation failed. :(");
                        System.out.println("There may be a group with a duplicate name.");
                    }
                    break;
                case "2": // Delete a group
                    System.out.println("Enter a group name to delete: ");
                    String groupToDelete = inputValidation(in.readLine());

                    if (groupClient.deleteGroup(groupToDelete, userToken))
                    {
                        System.out.println("Group "+groupToDelete+" deleted successfully!");
                    }
                    else
                    {
                        System.out.println("Failed to delete the group.");
                        System.out.println("Are you sure the group exists?");
                        System.out.println("You must be the owner of a group to delete it.");
                    }

                    break;
                case "3": // Add a user to a group
                    // GET the owner of the group specified
                    // Make sure the userToken matches the owner of the group
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
                        // TODO: Ideally we'd return the SPECIFIC error from the server.
                        System.out.println("Failed to add user to group.");
                        System.out.println("The user may not exist.");
                        System.out.println("The group may not exist.");
                        System.out.println("The user may already be a member of the group");
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
                        System.out.println("Are you sure "+userToRemove+" is a member of the group?");
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
                        System.out.println("Are you sure the group exists?");
                        System.out.println("You may not be a member of the group.");
                        System.out.println("Contact the group owner.");
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

    public static void connectFileServer() throws IOException
    {
        fileClient.connect(serverName,  FileClient.SERVER_PORT);
        if (fileClient.isConnected())
        {
            fileOptions();
        }
        else
        {
            System.out.println("System error. File Server is not running.");
        }

        fileClient.disconnect();
    }
    public static void fileOptions() throws IOException
    {
        String choice = "";
        System.out.println("Welcome to the File Server! Please choose from the list of options.\n\n");

        String[] menuOptions = new String[]{"Disconnect from file server",
                                            "List shared files",
                                            "Upload file to group",
                                            "Download file",
                                            "Delete file"};

        while (true)
        {
            // reset the group client to reset the state of objects on the stream
            fileClient.reset();
            userToken = getToken(username);

            for (int i = 0; i < menuOptions.length; i++)
            {
                System.out.println(i+". \t"+menuOptions[i]);
            }
            System.out.print(username+" >> ");

            try
            {
                choice = inputValidation(in.readLine());
            }
            catch(IOException e)
            {
                System.out.println("Error parsing input. Exiting...");
            }

            switch(choice)
            {
                case "0":
                    return;
                case "1":
                    System.out.println("List of files: \n");
                    List<String> fileList = fileClient.listFiles(userToken);
                    for (String file : fileList)
                     System.out.println(file);

                    System.out.println();
                    break;
                case "2":
                    System.out.println("Enter a source file name to upload: ");
                    String srcFile = inputValidation(in.readLine());

                    System.out.println("Enter a destination file name to add to the server: ");
                    String destFile = inputValidation(in.readLine());

                    System.out.println("Enter a group name to share the file with: ");
                    String groupName = inputValidation(in.readLine());


                    if (fileClient.upload(srcFile, destFile, groupName, userToken))
                        System.out.println("File uploaded successfully!");
                    else
                    {
                        System.out.println("File upload failed.");
                        System.out.println("You must be a member of the group you're uploading to");
                        System.out.println("The file must exist. Please try again.");
                    }

                    break;
                case "3":
                    System.out.println("Enter a source file name from the server: ");
                    srcFile = inputValidation(in.readLine());

                    System.out.println("Enter a destination file name to download to: ");
                    destFile = inputValidation(in.readLine());

                    if (fileClient.download(srcFile, destFile, userToken))
                        System.out.println("File downloaded succesfully!");
                    else
                    {
                        System.out.println("File download failed.");
                        System.out.println("The file must exist. Please try again.");
                    }
                    break;
                case "4":
                    System.out.println("Enter a filename to delete: ");
                    String fileName = inputValidation(in.readLine());
                    if (fileClient.delete(fileName, userToken))
                        System.out.println("File deletion successful.");
                    else
                    {
                        System.out.println("File deletion failed.");
                        System.out.println("The file must exist. Please try again.");
                    }


                    break;
                case "-help":
                    System.out.println("You're screwed. Sorry...");
                    break;
                default:
                    System.out.println("Command not recognized.");
            }
        }
    }

    public static Token getToken(String username)
    {
        groupClient.connect(serverName,  GroupClient.SERVER_PORT);
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
                        System.out.println("File Server");
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
