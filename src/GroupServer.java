/* Group server. Server loads the users from UserList.bin.
 * If user list does not exists, it creates a new list and makes the user the server administrator.
 * On exit, the server saves the user list to file.
 */

import java.io.*;
import java.util.*;
import java.security.Key;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.ArrayList;
import javax.mail.*;
import javax.mail.internet.*;

public class GroupServer extends Server
{
    public static final int SERVER_PORT = 8765;
    public UserList userList;
    public GroupList groupList;
    private String SERVER_EMAIL = "groupserver1653@gmail.com";
    private String SERVER_PASS = "@Lexishere1";
    private String SUBJECT = "Group Server Authentication Code";

    public GroupServer() throws Exception
    {
        super(SERVER_PORT, "ALPHA");
        serverRSAKeys = new EncryptionSuite("group_server_config/group_server_pub", "group_server_config/group_server_priv");
    }

    public GroupServer(int _port) throws Exception
    {
        super(_port, "ALPHA");
        serverRSAKeys = new EncryptionSuite("group_server_config/group_server_pub", "group_server_config/group_server_priv");
    }

    public void sendAuthEmail(String recipient, String authCode) throws Exception
    {
        System.out.println("Recipient: "+recipient);
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", this.SERVER_EMAIL);
        props.put("mail.smtp.password", this.SERVER_PASS);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        javax.mail.Session session = javax.mail.Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(this.SERVER_EMAIL));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

        message.setSubject(this.SUBJECT);
        message.setText(authCode);
        Transport transport = session.getTransport("smtp");
        transport.connect(host, this.SERVER_EMAIL, this.SERVER_PASS);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
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
        String email = "";
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
            System.out.print("Enter your email: ");
            email = in.readLine();

            do{
                System.out.print("Enter your password (q to quit): ");
                password = in.readLine();

                if(password.equalsIgnoreCase("q"))
                    break;

                if(!EncryptionSuite.verifyPassword(username, password))
                {
                    System.out.println("Invalid Password. \n" + EncryptionSuite.PASSWORD_INFO);
                }

            }
            while(!EncryptionSuite.verifyPassword(username, password));

            //Create a new list, add current user to the ADMIN group. They now own the ADMIN group.
            this.userList = new UserList();

            // Generate salt
            byte[] tempSalt = this.serverRSAKeys.generateSalt();
            // salt and hash the password
            byte[] saltedPwHash = this.serverRSAKeys.saltAndHashPassword(password, tempSalt);
            // Add user with their hashed and salted password
            this.userList.addUser(username, email, saltedPwHash, tempSalt);
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
                Thread.sleep(300000); //Save group and user lists every 5 minutes
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
