/* FileServer loads files from FileList.bin.  Stores files in shared_files directory. */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.ArrayList;
import java.security.Key;

public class FileServer extends Server
{
    public static final int SERVER_PORT = 4321;
    public static FileList fileList;
    protected EncryptionSuite groupServerPubKey;

    public FileServer() throws Exception
    {
        super(SERVER_PORT, "FilePile");
        serverRSAKeys = new EncryptionSuite("file_server_config/file_server_pub", "file_server_config/file_server_priv");
        groupServerPubKey = new EncryptionSuite("file_server_config/group_server_pub", "");
        System.out.println("Group Server Public Key: \n\n"+ this.groupServerPubKey.encryptionKeyToString());
        System.out.println("File Server Public Key: \n\n"+ this.serverRSAKeys.encryptionKeyToString());
    }

    public FileServer(int _port) throws Exception
    {
        super(_port, "FilePile");
        serverRSAKeys = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA);
    }

    public boolean verifyToken(UserToken token) throws Exception
    {
        return  this.groupServerPubKey.verifySignature(token.getSignedHash(), this.groupServerPubKey.hashBytes(token.toString().getBytes()));
    }

    public void start() throws Exception
    {

        System.out.println("File Server Online");
        String fileFile = "FileList.bin";
        ObjectInputStream fileStream;

        //This runs a thread that saves the lists on program exit
        Runtime runtime = Runtime.getRuntime();
        Thread catchExit = new Thread(new ShutDownListenerFS());
        runtime.addShutdownHook(catchExit);

        //Open user file to get user list
        try
        {
            FileInputStream fis = new FileInputStream(fileFile);
            fileStream = new ObjectInputStream(fis);
            // this.fileList?
            fileList = (FileList) fileStream.readObject();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("FileList Does Not Exist. Creating FileList...");
            // this.fileList?
            fileList = new FileList();

        }
        catch(IOException | ClassNotFoundException e)
        {
            System.out.println("Error reading from FileList file");
            System.exit(-1);
        }

        File file = new File("shared_files");
        if(file.mkdir())
        {
            System.out.println("Created new shared_files directory");
        }
        else if(file.exists())
        {
            System.out.println("Found shared_files directory");
        }
        else
        {
            System.out.println("Error creating shared_files directory");
        }

        //Autosave Daemon. Saves lists every 5 minutes
        AutoSaveFS aSave = new AutoSaveFS();
        aSave.setDaemon(true);
        aSave.start();


        boolean running = true;

        try
        {
            final ServerSocket serverSock = new ServerSocket(port);
            System.out.printf("%s up and running\n", this.getClass().getName());

            Socket sock = null;
            Thread thread = null;

            while(running)
            {
                sock = serverSock.accept();
                thread = new FileThread(sock, this);
                thread.start();
            }

            System.out.printf("%s shut down\n", this.getClass().getName());
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}

//This thread saves user and group lists
class ShutDownListenerFS implements Runnable
{
    public void run()
    {
        System.out.println("Shutting down server");
        ObjectOutputStream outStream;

        try
        {
            outStream = new ObjectOutputStream(new FileOutputStream("FileList.bin"));
            outStream.writeObject(FileServer.fileList);
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}

class AutoSaveFS extends Thread
{
    public void run()
    {
        do
        {
            try
            {
                // Thread.sleep(300000); //Save group and user lists every 5 minutes
                Thread.sleep(10000); //Save group and user lists every 5 minutes
                System.out.println("Autosave file list...");
                ObjectOutputStream outStream;
                try
                {
                    outStream = new ObjectOutputStream(new FileOutputStream("FileList.bin"));
                    outStream.writeObject(FileServer.fileList);
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
