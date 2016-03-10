import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class UserClient
{
	static GroupClient groupClient;
	static FileClient fileClient;
    static String userName = "";
    static UserToken userToken = null;
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static void connectGroupServer(String groupServerName, int groupPort, EncryptionSuite userKeys) throws Exception
    {
        groupClient.connect(groupServerName,  groupPort);
        if (groupClient.isConnected())
        {
			if (groupClient.authenticateGroupServer(userKeys))
			{
                System.out.println("Logged in and authenticated group server successfully!");
	            groupOptions(userKeys.getEncryptionKey());
			}
			else
			{
				System.out.println("Failed to authenticate group server. :(");
			}
        }
        else
        {
            System.out.println("System error. Group Server is not running.");
        }

        groupClient.disconnect(userKeys.getEncryptionKey());
    }

    public static void groupOptions(Key publicKey) throws IOException
    {
        String choice = "";
        System.out.println("Welcome to the group server! Please choose from the list of options.\n\n");

        String[] menuOptions = new String[]{"Disconnect from group server",
											"Retrieve a token",
                                            "Add (create) a group",
                                            "Remove (delete) a group",
                                            "Add a user to a group",
                                            "Remove a user from a group",
                                            "List all of the members of a group (that you are a member of)"};

        String[] adminOptions = new String[]{"Add (create) a user",
                                             "Remove (delete) a user",
 											 "Unlock a user"};


        while (true)
        {
            // reset the group client to reset the state of objects on the stream
            groupClient.reset();

            for (int i = 0; i < menuOptions.length; i++)
            {
                System.out.println(i+". \t"+menuOptions[i]);
            }

            // print admin options if they are an admin
            if (groupClient.isAdmin(userName, publicKey))
            {
                for(int i = 0; i < adminOptions.length; i++){
                    System.out.println((i+menuOptions.length) + ". \t" + adminOptions[i]);
                }

            }

            System.out.print(userName+" >> ");

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
            if(groupClient.isAdmin(userName, publicKey)){
                switch(choice)
                {
                    case "7": // Create a user
                        System.out.println("Enter username of new user to create: ");
                        String newUserName = inputValidation(in.readLine());
                        System.out.println("Enter password for "+newUserName +":");
                        String password = inputValidation(in.readLine());

						// verify if the password follows the requirements specified by
						// the encryption suite
						if(!EncryptionSuite.verifyPassword(newUserName, password))
						{
							System.out.println("Invalid Password. \n" + EncryptionSuite.PASSWORD_INFO);
						}
						else
						{

	                        if (groupClient.createUser(newUserName, password, userName, publicKey))
	                        {
	                            System.out.println("User created successfully!");
	                        }
	                        else
	                        {
	                            System.out.println("User creation failed!");
	                            System.out.println("You cannot create a user with a duplicate username.");
	                        }
	                        selectedAdminOption = true;
						}
                        break;
                    case "8": // Delete a user
                        System.out.println("Enter username of user to delete: ");
                        String userToDelete = inputValidation(in.readLine());
                        if (groupClient.deleteUser(userToDelete, userName, publicKey))
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
					case "9":
						System.out.println("Enter username of the user to unlock:");
						String userToUnlock = inputValidation(in.readLine());

						System.out.println("Enter new password of the user to unlock:");
						String newPassword = inputValidation(in.readLine());

						// verify if the password follows the requirements specified by
						// the encryption suite
						if(!EncryptionSuite.verifyPassword(userToUnlock, newPassword))
						{
							System.out.println("Invalid Password. \n" + EncryptionSuite.PASSWORD_INFO);
						}
						else
						{
							if(groupClient.unlockUser(userToUnlock, newPassword, userName, publicKey))
							{
								System.out.println("User unlocked!");
							}
							else
							{
								System.out.println("User not unlocked.");
							}
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
				case "1":
        			userToken = (Token) groupClient.getToken(userName, publicKey);

					if(userToken != null)
					{
						System.out.println("Token received!");
					}
					else
					{
						System.out.println("No token received... :(");
					}

					break;
                case "2": // Create a group
                    System.out.println("Enter a group name: ");
                    String newGroupName = inputValidation(in.readLine());
                    if (groupClient.createGroup(newGroupName, userName, publicKey))
                    {
                        System.out.println("Group "+newGroupName+" created successfully!");
                    }
                    else
                    {
                        System.out.println("Group creation failed. :(");
                        System.out.println("There may be a group with a duplicate name.");
                    }
                    break;
                case "3": // Delete a group
                    System.out.println("Enter a group name to delete: ");
                    String groupToDelete = inputValidation(in.readLine());

                    if (groupClient.deleteGroup(groupToDelete, userName, publicKey))
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
                case "4": // Add a user to a group
                    // GET the owner of the group specified
                    // Make sure the userToken matches the owner of the group
                    // then add the user to the group
                    System.out.println("Enter a user name to add: ");
                    String userToAdd = inputValidation(in.readLine());

                    System.out.println("Enter a group to add "+userToAdd+" to: ");
                    String groupToAddUserTo = inputValidation(in.readLine());

                    if (groupClient.addUserToGroup(userToAdd, groupToAddUserTo, userName, publicKey))
                    {
                        System.out.println(userToAdd+" added successfully to "+groupToAddUserTo+".");
                    }
                    else
                    {
                        System.out.println("Failed to add user to group.");
                        System.out.println("The user may not exist.");
                        System.out.println("The group may not exist.");
                        System.out.println("The user may already be a member of the group");
                        System.out.println("You must be the owner of a group to add a user.");
                    }
                    break;
                case "5": // Remove a user from a group

                    System.out.println("Enter a group name that you're an owner of: ");
                    String group = inputValidation(in.readLine());

                    System.out.println("Enter the name of the user in "+group+" that you'd like to remove: ");
                    String userToRemove = inputValidation(in.readLine());

                    if (groupClient.deleteUserFromGroup(userToRemove,group, userName, publicKey))
                        System.out.println("Successfully deleted "+userToRemove+" from "+group+"!");
                    else
                    {
                        System.out.println("Deletion of "+userToRemove+" from "+group+" failed.");
                        System.out.println("Are you sure the group exists?");
                        System.out.println("Are you sure "+userToRemove+" is a member of the group?");
                        System.out.println("You must be the owner of a group to remove a user from it.");
                    }

                    break;
                case "6": // List all the members of a group
                    System.out.println("Enter a group name you're a member of to list: ");
                    String groupName = inputValidation(in.readLine());
                    List<String> groupMembers = groupClient.listMembers(groupName, userName, publicKey);
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

    public static void connectFileServer(String groupSeverName, int groupPort,
                                         String fileServerName, int filePort,
										 EncryptionSuite userKeys) throws Exception
    {
        fileClient.connect(fileServerName,  filePort);
        if (fileClient.isConnected())
        {
			if (fileClient.authenticateFileServer(userKeys, userToken))
			{
				System.out.println("Successfully authenticated file server!");
            	fileOptions(groupSeverName, groupPort, userKeys.getEncryptionKey());
			}
        }
        else
        {
            System.out.println("System error. File Server is not running.");
        }

        fileClient.disconnect(userKeys.getEncryptionKey());
    }
    public static void fileOptions(String groupSeverName, int groupPort, Key publicKey) throws Exception
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

            for (int i = 0; i < menuOptions.length; i++)
            {
                System.out.println(i+". \t"+menuOptions[i]);
            }
            System.out.print(userName+" >> ");

            try
            {
                choice = inputValidation(in.readLine());
            }
            catch(IOException e)
            {
                System.out.println("Error parsing input. Exiting...");
            }

			// if the user token is expire the user must go back to the group server to get a new token
			if(userToken != null && userToken.isExpired())
			{
				System.out.println("User Token is expired. Contact the Group Server to recieve a new token.");
			}

            switch(choice)
            {
                case "0":
                    return;
                case "1":
                    System.out.println("List of files: \n");
                    List<String> fileList = fileClient.listFiles(userToken, publicKey);
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


                    if (fileClient.upload(srcFile, destFile, groupName, userToken, publicKey))
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

                    if (fileClient.download(srcFile, destFile, userToken, publicKey))
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
                    if (fileClient.delete(fileName, userToken, publicKey))
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

    public static void chooseServer(String groupServerName, int groupPort, String fileServerName, int filePort) throws Exception
    {

        // Generate users private / public key pair
        EncryptionSuite userKeys = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA);

        try
        {
            while(true)
            {
                System.out.println("1. \tGroup Server");
                System.out.println("2. \tFile Server");
                System.out.println("0. \tExit");
                System.out.print(userName+" >> ");

                String choice = inputValidation(in.readLine());

                switch(choice)
                {
                    case "1":
                        System.out.println("Group Server");
                        connectGroupServer(groupServerName, groupPort, userKeys);

                        break;
                    case "2":
                        System.out.println("File Server");
                        connectFileServer(groupServerName, groupPort, fileServerName, filePort, userKeys);

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

    public static void main(String args[]) throws Exception
    {

	    groupClient = new GroupClient();
	    fileClient = new FileClient();

        String groupServerName = "localhost";
        int groupPort = GroupClient.SERVER_PORT;

        String fileServerName = "localhost";
        int filePort = FileClient.SERVER_PORT;

        try
        {
            // if we pass in more than 1 parameter, set the values for each parameter
            if(args.length > 0)
            {
                groupServerName = args[0];
                groupPort = Integer.parseInt(args[1]);
                fileServerName = args[2];
                filePort = Integer.parseInt(args[3]);
            }
        }
        catch (Exception e)
        {
            System.out.println("Invalid Program Parameters. Please try again.");
        }

        try
        {
            chooseServer(groupServerName, groupPort, fileServerName, filePort);
        }
        catch(Exception e)
        {
            System.out.println("Failed to connect to group server\n"+
                                "Possibly invalid username,\n"+
                                "or failed to get group server public key.");
        }

        in.close();
    }

} //-- end class UserClient
