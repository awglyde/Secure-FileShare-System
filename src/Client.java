import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class Client
{

    /* protected keyword is like private but subclasses have access
     * Socket and input/output streams
     */
    protected Socket sock;
    protected ObjectOutputStream output;
    protected ObjectInputStream input;
	public EncryptionSuite groupServerPublicKey;
    public EncryptionSuite fileServerPublicKey;
	public EncryptionSuite sessionKey;

    public static enum Privilege {
        USER,
        GROUPMEMBER,
        GROUPOWNER,
        ADMIN,
        ADMINOWNER
    }

    public boolean connect(final String serverName, final int port)
    {
        System.out.println("attempting to connect");
        try
        {
            this.sock = new Socket(serverName, port);

            // TODO: ask professor why output and input need to be in this specific order
            // open the input/output stream objects to communicate with the server
            this.output = new ObjectOutputStream(sock.getOutputStream());
            this.input = new ObjectInputStream(sock.getInputStream());

            System.out.println("Connected to " + serverName + " on port " + port);
            return true;
        }
        catch(IOException err)
        {
            System.out.println("Couldn't connect to Server.\n" + err);
        }

        return false;
    }

    public boolean isConnected()
    {
        if(this.sock == null || !this.sock.isConnected())
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public void reset()
    {
        try
        {
            // flushes / resets the output stream
            this.output.flush();
            this.output.reset();
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public void disconnect()
    {
        if(isConnected())
        {
            try
            {
                Envelope message = new Envelope("DISCONNECT");
                this.output.writeObject(message);

                // close the socket and the input/output streams connecting to the server
                this.output.close();
                this.input.close();
                this.sock.close();
            }
            catch(Exception e)
            {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }
}
