/* Group server. Server loads the users from UserList.bin.
 * If user list does not exists, it creates a new list and makes the user the server administrator.
 * On exit, the server saves the user list to file.
 */

/*
 * TODO: This file will need to be modified to save state related to
 *       groups that are created in the system
 *
 */

import java.io.*;
import java.security.Key;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.ArrayList;

public class GroupServer extends Server
{
    public static final int SERVER_PORT = 8765;
    public UserList userList;
    public GroupList groupList;
    protected EncryptionSuite groupServerKeys;
    HashMap<Integer, Key> clientCodeToKey =  new HashMap<Integer, Key>();
    protected EncryptionSuite sessionKey = null;

    public GroupServer() throws Exception
    {
        super(SERVER_PORT, "ALPHA");
        groupServerKeys = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA);
    }

    public GroupServer(int _port) throws Exception
    {
        super(_port, "ALPHA");
        groupServerKeys = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA);
    }

    public Key getPublicKey()
    {
        return groupServerKeys.getEncryptionKey();
    }

    public Key getSessionKey()
    {
        if(sessionKey != null)
        {
            return sessionKey.getEncryptionKey();
        } else
        {
            return null;
        }
    }

    public void start() throws Exception
    {

        // Overwrote server.start() because if no user file exists, initial admin account needs to be created
        System.out.println("Group Server Online");
        String userFile = "UserList.bin";
        String groupFile = "GroupList.bin";
        // Scanner console = new Scanner(System.in);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        ObjectInputStream userStream;
        ObjectInputStream groupStream;
        String username = "";
        String password = "";

        //This runs a thread that saves the lists on program exit
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new ShutDownListener(this));

        //Open user file to get user list
        try
        {
            FileInputStream file_fis = new FileInputStream(userFile);
            userStream = new ObjectInputStream(file_fis);
            this.userList = (UserList) userStream.readObject();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("UserList File Does Not Exist. Creating UserList...");
            System.out.println("No users currently exist. Your account will be the administrator.");
            System.out.print("Enter your username: ");
            username = in.readLine();
            System.out.print("Enter your password: ");
            password = in.readLine();

            //Create a new list, add current user to the ADMIN group. They now own the ADMIN group.
            this.userList = new UserList();

            // Generate salt
            byte[] tempSalt = this.groupServerKeys.generateSalt();
            // salt and hash the password
            byte[] saltedPwHash = this.groupServerKeys.saltAndHashPassword(password, tempSalt);
            // Add user with their hashed and salted password
            this.userList.addUser(username, saltedPwHash, tempSalt);
            this.userList.addGroup(username, "ADMIN");
            this.userList.addOwnership(username, "ADMIN");
        }
        catch(IOException e)
        {
            System.out.println("Error reading from UserList file");
            System.exit(-1);
        }
        catch(ClassNotFoundException e)
        {
            System.out.println("Error reading from UserList file");
            System.exit(-1);
        }

        //Open group file to get group list
        try
        {
            FileInputStream group_fis = new FileInputStream(groupFile);
            userStream = new ObjectInputStream(group_fis);
            this.groupList = (GroupList) userStream.readObject();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("GroupList File Does Not Exist. Creating GroupList...");

            // Create a new group list. Initialize the ADMIN group and its owner / member
            this.groupList = new GroupList();
            this.groupList.addGroup(username, "ADMIN");

        }
        catch(IOException e)
        {
            System.out.println("Error reading from GroupList file");
            System.exit(-1);
        }
        catch(ClassNotFoundException e)
        {
            System.out.println("Error reading from GroupList file");
            System.exit(-1);
        }

        //Autosave Daemon. Saves lists every 5 minutes
        AutoSave aSave = new AutoSave(this);
        aSave.setDaemon(true);
        aSave.start();

        //This block listens for connections and creates threads on new connections
        try
        {

            final ServerSocket serverSock = new ServerSocket(port);

            Socket sock = null;
            GroupThread thread = null;

            while(true)
            {
                sock = serverSock.accept();
                thread = new GroupThread(sock, this);
                thread.start();
            }
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    }

}


//This thread saves the user list
class ShutDownListener extends Thread
{
    public GroupServer my_gs;

    public ShutDownListener(GroupServer _gs)
    {
        this.my_gs = _gs;
    }

    public void run()
    {
        System.out.println("Shutting down server");
        ObjectOutputStream outStream;
        try
        {
            outStream = new ObjectOutputStream(new FileOutputStream("UserList.bin"));
            outStream.writeObject(this.my_gs.userList);

            outStream = new ObjectOutputStream(new FileOutputStream("GroupList.bin"));
            outStream.writeObject(this.my_gs.groupList);
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}

class AutoSave extends Thread
{
    public GroupServer my_gs;

    public AutoSave(GroupServer _gs)
    {
        this.my_gs = _gs;
    }

    public void run()
    {
        do
        {
            try
            {
                // TODO: change this back to 5 minutes
                // Thread.sleep(300000); //Save group and user lists every 5 minutes
                Thread.sleep(10000); //Save group and user lists every 5 minutes
                System.out.println("Autosave group and user lists...");
                ObjectOutputStream outStream;
                try
                {
                    outStream = new ObjectOutputStream(new FileOutputStream("UserList.bin"));
                    outStream.writeObject(this.my_gs.userList);


                    outStream = new ObjectOutputStream(new FileOutputStream("GroupList.bin"));
                    outStream.writeObject(this.my_gs.groupList);
                }
                catch(Exception e)
                {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace(System.err);
                }

            }
            catch(Exception e)
            {
                System.out.println("Autosave Interrupted");
            }
        } while(true);
    }
}
