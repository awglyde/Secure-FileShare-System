/* FileClient provides all the client functionality regarding the file server */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.security.Key;
import java.security.SecureRandom;

public class FileClient extends Client implements FileClientInterface
{
    public static final int SERVER_PORT = 4321;

    public boolean delete(String filename, UserToken token, Key publicKey) throws Exception
    {
        String remotePath;
        if(filename.charAt(0) == '/')
        {
            remotePath = filename.substring(1);
        }
        else
        {
            remotePath = filename;
        }
        Envelope env = new Envelope("DELETEF"); //Success
        env.addObject(remotePath);
        env.addObject(token);
        try
        {

            // Get encrypted message from our EncryptionSuite
            env = this.sessionKey.getEncryptedMessage(env);
            // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
            env.addObject(publicKey.hashCode()); //Add user public key hash
            output.writeObject(env);

            //Get the response from the server
            env = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

            if(env.getMessage().compareTo("OK") == 0)
            {
                System.out.printf("File %s deleted successfully\n", filename);
            }
            else
            {
                System.out.printf("Error deleting file %s (%s)\n", filename, env.getMessage());
                return false;
            }
        }
        catch(IOException e1)
        {
            e1.printStackTrace();
        }
        catch(ClassNotFoundException e1)
        {
            e1.printStackTrace();
        }

        return true;
    }

    public boolean download(String sourceFile, String destFile, UserToken token, Key publicKey) throws Exception
    {
        if(sourceFile.charAt(0) == '/')
        {
            sourceFile = sourceFile.substring(1);
        }

        File file = new File(destFile);
        try
        {


            if(!file.exists())
            {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);

                Envelope env = new Envelope("DOWNLOADF"); //Success
                env.addObject(sourceFile);
                env.addObject(token);
                // Get encrypted message from our EncryptionSuite
                env = this.sessionKey.getEncryptedMessage(env);
                // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
                env.addObject(publicKey.hashCode()); //Add user public key hash
                output.writeObject(env);

                //Get the response from the server
                env = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

                while(env.getMessage().compareTo("CHUNK") == 0)
                {
                    fos.write((byte[]) env.getObjContents().get(0), 0, (Integer) env.getObjContents().get(1));
                    System.out.printf(".");
                    env = new Envelope("DOWNLOADF"); //Success
                    // Get encrypted message from our EncryptionSuite
                    env = this.sessionKey.getEncryptedMessage(env);
                    // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
                    env.addObject(publicKey.hashCode()); //Add user public key hash
                    output.writeObject(env);

                    env = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());
                }
                fos.close();

                if(env.getMessage().compareTo("EOF") == 0)
                {
                    fos.close();
                    System.out.printf("\nTransfer successful file %s\n", sourceFile);
                    env = new Envelope("OK"); //Success

                    // Get encrypted message from our EncryptionSuite
                    env = this.sessionKey.getEncryptedMessage(env);
                    // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
                    env.addObject(publicKey.hashCode()); //Add user public key hash
                    output.writeObject(env);
                }
                else
                {
                    System.out.printf("Error reading file %s (%s)\n", sourceFile, env.getMessage());
                    file.delete();
                    return false;
                }
            }
            else
            {
                System.out.printf("Error couldn't create file %s\n", destFile);
                return false;
            }


        }
        catch(IOException e1)
        {

            System.out.printf("Error couldn't create file %s\n", destFile);
            return false;


        }
        catch(ClassNotFoundException e1)
        {
            e1.printStackTrace();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<String> listFiles(UserToken token, Key publicKey) throws Exception
    {
        try
        {
            Envelope message = null, e = null;
            //Tell the server to return the member list
            message = new Envelope("LFILES");
            message.addObject(token); //Add requester's token
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
            message.addObject(publicKey.hashCode()); //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            e = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

            //If server indicates success, return the member list
            if(e.getMessage().equals("OK"))
            {
                return (List<String>) e.getObjContents().get(0); //This cast creates compiler warnings. Sorry.
            }

            return null;

        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }

    public boolean upload(String sourceFile, String destFile, String group,
                          UserToken token, Key publicKey) throws Exception
    {

        if(destFile.charAt(0) != '/')
        {
            destFile = "/" + destFile;
        }

        try
        {

            Envelope message = null, env = null;
            //Tell the server to return the member list
            message = new Envelope("UPLOADF");
            message.addObject(destFile);
            message.addObject(group);
            message.addObject(token); //Add requester's token
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
            message.addObject(publicKey.hashCode()); //Add user public key hash
            output.writeObject(message);

            FileInputStream fis = new FileInputStream(sourceFile);

            env = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

            //If server indicates success, return the member list
            if(env.getMessage().equals("READY"))
            {
                System.out.printf("Meta data upload successful\n");

            }
            else
            {

                System.out.printf("Upload failed: %s\n", env.getMessage());
                return false;
            }


            do
            {
                byte[] buf = new byte[4096];
                if(env.getMessage().compareTo("READY") != 0)
                {
                    System.out.printf("Server error: %s\n", env.getMessage());
                    return false;
                }
                message = new Envelope("CHUNK");
                int n = fis.read(buf); //can throw an IOException
                if(n > 0)
                {
                    System.out.printf(".");
                }
                else if(n < 0)
                {
                    System.out.println("Read error");
                    return false;
                }

                message.addObject(buf);
                message.addObject(new Integer(n));

                // Get encrypted message from our EncryptionSuite
                message = this.sessionKey.getEncryptedMessage(message);
                // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
                message.addObject(publicKey.hashCode()); //Add user public key hash
                output.writeObject(message);

                env = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());


            }
            while(fis.available() > 0);

            //If server indicates success, return the member list
            if(env.getMessage().compareTo("READY") == 0)
            {

                message = new Envelope("EOF");

                // Get encrypted message from our EncryptionSuite
                message = this.sessionKey.getEncryptedMessage(message);
                // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
                message.addObject(publicKey.hashCode()); //Add user public key hash
                output.writeObject(message);

                env = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());
                if(env.getMessage().compareTo("OK") == 0)
                {
                    System.out.printf("\nFile data upload successful\n");
                }
                else
                {

                    System.out.printf("\nUpload failed: %s\n", env.getMessage());
                    return false;
                }

            }
            else
            {

                System.out.printf("Upload failed: %s\n", env.getMessage());
                return false;
            }

        }
        catch(Exception e1)
        {
            System.err.println("Error: " + e1.getMessage());
            e1.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    public Key getFileServerPublicKey(EncryptionSuite userKeys, UserToken userToken) throws Exception
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to return its public key
            message = new Envelope("GPUBLICKEY");
            message.addObject(userKeys.getEncryptionKey());
            output.writeObject(message);

            response = (Envelope) input.readObject();
            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return (Key)response.getObjContents().get(0);
            }
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
        return null;
    }

	public boolean authChallenge(EncryptionSuite userKeys, UserToken userToken) throws Exception
    {
		// 1) Generate a challenge.
		SecureRandom prng = new SecureRandom();
        byte[] challenge = new byte[16];
        prng.nextBytes(challenge);

        try
        {
            Envelope message = null, response = null;
            message = new Envelope("AUTHCHALLENGE");
            // Add challenge and client pub key hash to envelope
			message.addObject(challenge);
            // TODO: Should we use our EncryptionSuite hash method?
            message.addObject(userKeys.getEncryptionKey().hashCode());
            // Add signers public key (for convenience)

            // 2) Encrypt challenge, client's pub key hash with FS public key
            output.writeObject(this.fileServerPublicKey.getEncryptedMessage(message));

            // 3) Receive completed challenge and shared AES key
            response = userKeys.getDecryptedMessage((Envelope)input.readObject());

            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                byte[] completedChallenge = (byte[])response.getObjContents().get(0); // User's completed challenge H(R)
                if (userKeys.verifyChallenge(challenge, completedChallenge))
                {
                    System.out.println("File Server successfully completed challenge!");
                }
                else
                {
                    System.out.println("Server failed to complete challenge! Session may have been hijacked!");
                    return false;
                }

                // 4) Store new shared key in sessionKey ES object
                Key sessionKey = (Key)response.getObjContents().get(1); // New session key from file server
		        this.sessionKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES, sessionKey);
                return true;
            }
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }

		return false;
	}

	public boolean authenticateFileServer(EncryptionSuite userKeys, UserToken userToken) throws Exception
	{

		// Get File server public key
        Key fileServerPublicKey = this.getFileServerPublicKey(userKeys, userToken);
        // Generate new object for encryption / decryption with fs public key
        this.fileServerPublicKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA, fileServerPublicKey, null);
        System.out.println("File Server Public Key: \n\n"+
                            this.fileServerPublicKey.encryptionKeyToString());

        System.out.println("\n\nIs this key authentic? Enter 'y' to continue, 'n' to quit.");
        String choice = UserClient.in.readLine();
		if (choice.equals("y") && this.authChallenge(userKeys, userToken))
			return true;
		else
			return false;

	}

}
