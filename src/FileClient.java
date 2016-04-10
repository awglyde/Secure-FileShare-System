/* FileClient provides all the client functionality regarding the file server */

import java.io.File;
import java.io.RandomAccessFile;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
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

            env.addObject(this.session.generateHmac(env));
            // Get encrypted message from our EncryptionSuite
            env = this.session.getEncryptedMessage(env);
            output.writeObject(env);


            //Get the response from the server
            env = this.session.getDecryptedMessage((Envelope)input.readObject());

			// Verify the HMAC sent by the file server
            env = this.session.clientHmacVerify(env);

			// Verify the sequence number sent by the file server
            env = this.session.clientSequenceNumberHandler(env);

            if(env.getMessage().equals("OK"))
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

    public boolean download(String sourceFile, String destFile, UserToken token, Hashtable<String, ArrayList<Key>> keyRing) throws Exception
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

                Envelope env = new Envelope("DOWNLOADF"); //Success
                // Add the most recent sequence number to our message
                env.addObject(this.session.getSequenceNum());
                env.addObject(sourceFile);
                env.addObject(token);
                // Generate an HMAC of our message for file server to verify
                env.addObject(session.generateHmac(env));
                // Get encrypted message from our EncryptionSuite
                env = this.session.getEncryptedMessage(env);
                // Write the message to the server
                output.writeObject(env);

                //Get the decrypted response from the server
                env = this.session.getDecryptedMessage((Envelope)input.readObject());

    			// Verify the HMAC sent by the file server
                env = this.session.clientHmacVerify(env);

    			// Verify the sequence number sent by the file server
                env = this.session.clientSequenceNumberHandler(env);

                if (env.getMessage().equals("FILE"))
                {
                    // We got a fle, so create a new file
                    file.createNewFile();

                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] encryptedFileBytes = (byte[])env.getObjContents().get(0);
                    // Group that owns the file we downloaded. Used to find the right key
                    String group = (String)env.getObjContents().get(1);
                    // Retrieve the relevant version number. Used to find the right key
                    int keyVersion = (int)env.getObjContents().get(2);
                    // Retrieve the actual size of the file (without padding)
                    int fileSize = (int)env.getObjContents().get(3);
                    // Retrieve the hmac of the file
                    byte[] fileHmac = (byte[])env.getObjContents().get(4);

                    // Make sure our key ring is not null before we try to decrypt anything
                    if (!(keyRing == null) && !(keyRing.get(group) == null))
                    {
                        // With the right key version, decrypt the file retrieved from the file server
                        byte[] decryptedFileBytes = EncryptionSuite.decryptFile(keyRing.get(group), keyVersion, encryptedFileBytes, fileSize);

                        // Verify the file hasn't been tampered with
                        if(EncryptionSuite.verifyFileHmac(keyRing.get(group), keyVersion, fileHmac, decryptedFileBytes))
                        {
                            fos.write(decryptedFileBytes);
                            fos.close();

                            return true;
                        }
                        else
                        {
                            System.out.println("File was modified! Download failed.");
                            return false;
                        }

                    }
                    else
                    {
                        System.out.println("Failed to unencrypt file. Please retrieve a token and keyring from group server and try again.");
                        return false;
                    }
                }
                else if (env.getMessage().equals("FAIL"))
                {
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

        return false;
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

            // Generate an HMAC of our message for server to verify
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

			// Verify the HMAC sent by the file server
            response = this.session.clientHmacVerify(response);

			// Verify the sequence number sent by the file server
            response = this.session.clientSequenceNumberHandler(response);

            //If server indicates success, return the member list
            if(response.getMessage().equals("OK"))
            {
                return (List<String>) response.getObjContents().get(0); //This cast creates compiler warnings. Sorry.
            }
            else if (response.getMessage().equals("FAIL"))
            {
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

    public boolean upload(String sourceFile, String destFile, String group,
                          UserToken token, Hashtable<String, ArrayList<Key>> keyRing) throws Exception
    {
        if(destFile.charAt(0) != '/')
        {
            destFile = "/" + destFile;
        }

        try
        {

            Envelope message = null, env = null;
            byte[] fileBytes = null;
            //Tell the server to return the member list
            message = new Envelope("UPLOADF");
            message.addObject(this.session.getSequenceNum());
            message.addObject(destFile);
            message.addObject(group);
            message.addObject(token); //Add requester's token

            File f = new File(sourceFile);
            // Check to make sure f exists before we read its bytes
            if(f.exists() && !f.isDirectory())
            {
                Path path = Paths.get(sourceFile);
                fileBytes = Files.readAllBytes(path);
            }
            else
            {
                return false;
            }

            // Can't encrypt file if our keyring is empty, file is null, we're not a member of the group we're trying to upload to.
            // Need to retrieve one from group server if so
            if (!(keyRing == null) && !(fileBytes == null) && !(keyRing.get(group) == null))
            {
                message.addObject(EncryptionSuite.encryptFile(keyRing.get(group), fileBytes)); // add file bytes to message

                // add the version number of the encrypted file
                message.addObject(keyRing.get(group).size()-1);

                // add the size of the unencrypted file
                message.addObject(fileBytes.length);

                message.addObject(EncryptionSuite.generateFileHmac(keyRing.get(group), fileBytes));
            }
            else
            {
                System.out.println("Keyring was empty, file is null, or you're not a member of the group you were uploading to. Please retrieve a token from the group server before attempting to add or download a file.");
                return false;
            }


            // Generate an HMAC of our message for server to verify
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get decrypted response from server
            env = this.session.getDecryptedMessage((Envelope)input.readObject());

			// Verify the HMAC sent by the file server
            env = this.session.clientHmacVerify(env);

			// Verify the sequence number sent by the file server
            env = this.session.clientSequenceNumberHandler(env);

            //If server indicates success, return the member list
            if(env.getMessage().equals("OK"))
            {
                return true;
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
		// 1) Generate a challenge and a new HMAC key
        this.session.setHmacKey();
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
			message.addObject(this.session.getHmacKey().getEncryptionKey()); // add the hmac key to our message

            // Add an HMAC of our message created using the user's public RSA key
            message.addObject(userKeys.generateHmac(this.session.getBytes(message)));

            // 2) Encrypt challenge with GS public key
			Envelope encryptedMessage = this.session.getEncryptedMessageTargetKey(message);
            encryptedMessage.addObject(userKeys.getEncryptionKey());
            output.writeObject(encryptedMessage);

            // 3) Receive completed challenge and shared AES key
            response = userKeys.getDecryptedMessage((Envelope)input.readObject());

            // Verify the hmac sent by the file server
            response = this.session.clientHmacVerify(response);

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
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
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
