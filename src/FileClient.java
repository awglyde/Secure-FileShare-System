/* FileClient provides all the client functionality regarding the file server */

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.lang.Math;
import java.security.Key;
import java.security.SecureRandom;

public class FileClient extends Client implements FileClientInterface
{
    public static final int SERVER_PORT = 4321;

    public boolean delete(String filename, UserToken token) throws Exception
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
        Envelope env = new Envelope("DELETEF");
        env.addObject(this.session.getSequenceNum());
        env.addObject(remotePath);
        env.addObject(token);
        try
        {

            // Get encrypted message from our EncryptionSuite
            env = this.session.getEncryptedMessage(env);
            output.writeObject(env);

            //Get the response from the server
            env = this.session.getDecryptedMessage((Envelope)input.readObject());
            env = this.session.clientSequenceNumberHandler(env);

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

    public boolean download(String sourceFile, String destFile, UserToken token) throws Exception
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
                env.addObject(this.session.getSequenceNum());
                env.addObject(sourceFile);
                env.addObject(token);
                // Get encrypted message from our EncryptionSuite
                env = this.session.getEncryptedMessage(env);
                output.writeObject(env);

                //Get the response from the server
                env = this.session.getDecryptedMessage((Envelope)input.readObject());
                env = this.session.clientSequenceNumberHandler(env);

                if (env.getMessage().equals("FILE"))
                {
                    fos.write((byte[])env.getObjContents().get(0));
                    fos.close();
                    System.out.println("SUCCESSFULLY DOWNLOADED THE FILE!");
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
    public List<String> listFiles(UserToken token) throws Exception
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to return the member list
            message = new Envelope("LFILES");
            message.addObject(this.session.getSequenceNum());
            message.addObject(token); //Add requester's token
            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);

            //If server indicates success, return the member list
            if(response.getMessage().equals("OK"))
            {
                return (List<String>) response.getObjContents().get(0); //This cast creates compiler warnings. Sorry.
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
                          UserToken token) throws Exception
    {
        /*Path path = Paths.get(sourceFile);
        byte[] data = Files.readAllBytes(path);
        data = session.getAESKey().encryptFile(data);

        FileOutputStream fout = new FileOutputStream(sourceFile);
        fout.write(data);
        fout.close();*/

        if(destFile.charAt(0) != '/')
        {
            destFile = "/" + destFile;
        }

        try
        {

            Envelope message = null, env = null;
            //Tell the server to return the member list
            message = new Envelope("UPLOADF");
            message.addObject(this.session.getSequenceNum());
            message.addObject(destFile);
            message.addObject(group);
            message.addObject(token); //Add requester's token

            Path path = Paths.get(sourceFile);
            byte[] fileBytes = Files.readAllBytes(path);

            message.addObject(fileBytes); // add file bytes to message

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            env = this.session.getDecryptedMessage((Envelope)input.readObject());
            env = this.session.clientSequenceNumberHandler(env);

            //If server indicates success, return the member list
            if(env.getMessage().equals("OK"))
            {
                System.out.printf("File was uploaded successful\n");
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

    public EncryptionSuite getFileServerPublicKey()
    {
        try
        {
            Envelope message = null, response = null;

            message = new Envelope("GPUBLICKEY");

            output.writeObject(message);

            //Get the response from the server
            response = ((Envelope)input.readObject());

            //If server indicates success, return the file server public key
            if(response.getMessage().equals("OK"))
            {
                EncryptionSuite fileServerPublicKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA,
                (Key)response.getObjContents().get(0));
                System.out.println("File Server Public Key: \n\n"+
                                    fileServerPublicKey.encryptionKeyToString());

                System.out.println("\n\nIs this key authentic? Enter 'y' to continue, 'n' to quit.");
                String choice = UserClient.in.readLine();
                if (choice.equals("y"))
                    return fileServerPublicKey; //This cast creates compiler warnings. Sorry.
                else
                    return null;
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

	public boolean authChallenge(EncryptionSuite userKeys, UserToken userToken, EncryptionSuite fileServerPublicKey) throws Exception
    {
		// 1) Generate a challenge.
		SecureRandom prng = new SecureRandom();
        byte[] challenge = new byte[16];
        prng.nextBytes(challenge);

        int sequenceNumber = prng.nextInt();
        this.session.setSequenceNum(Math.abs(sequenceNumber));

        try
        {
            Envelope message = null, response = null;
            message = new Envelope("AUTHCHALLENGE");

            // Add challenge and client pub key hash to envelope
			message.addObject(challenge);

            // 2) Encrypt challenge with GS public key
			Envelope encryptedMessage = this.session.getEncryptedMessageTargetKey(message);
            encryptedMessage.addObject(userKeys.getEncryptionKey());
            output.writeObject(encryptedMessage);

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

                // 4) Store new shared key in session ES object
                Key session = (Key)response.getObjContents().get(1); // New session key from file server
				this.session.setAESKey(session);
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

	public boolean authenticateFileServer(EncryptionSuite userKeys, UserToken userToken, EncryptionSuite fileServerPublicKey) throws Exception
	{

		this.session = new Session();
		this.session.setTargetKey(fileServerPublicKey.getEncryptionKey());

		if (this.authChallenge(userKeys, userToken, fileServerPublicKey))
			return true;
		else
			return false;

	}

}
