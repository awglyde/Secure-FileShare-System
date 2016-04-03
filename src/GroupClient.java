/* Implements the GroupClient Interface */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.Math;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.*;

public class GroupClient extends Client implements GroupClientInterface
{
    public static final int SERVER_PORT = 8765;

    public UserToken getToken(String userName, Key fileServerPublicKey)
    {
        try
        {
            UserToken token = null;
            Envelope message = null, response = null;

            //Tell the server to return a token.
            message = new Envelope("GET");
            message.addObject(this.session.getSequenceNum());

            message.addObject(userName); //Add user name string
            // add file serverpublic key, used to uniquely associate this token w a file server
            message.addObject(fileServerPublicKey);

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);
            response = this.session.clientHmacVerify(response);

            //Successful response
            if(response.getMessage().equals("OK"))
            {
                //If there is a token in the Envelope, return it
                ArrayList<Object> temp = null;
                temp = response.getObjContents();

                if(temp.size() == 1)
                {
                    token = (UserToken) temp.get(0);
                    return token;
                }
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

    public boolean unlockUser(String username, String password, String requester)
    {
        try
        {
            Envelope message = null, response = null;
            message = new Envelope("UNLOCKUSER");
            message.addObject(this.session.getSequenceNum());
            message.addObject(username); //Add user name string
            message.addObject(password);
            message.addObject(requester); //Add the requester

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);

            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return true;
            }

            return true;
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    public boolean createUser(String userName, String password, String requester)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to create a user
            message = new Envelope("CUSER");
            message.addObject(this.session.getSequenceNum());
            message.addObject(userName); //Add user name string
            message.addObject(password); //Add the new user's password
            message.addObject(requester); //Add the requester

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);

            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return true;
            }

            return false;
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    public boolean deleteUser(String userName, String requester)
    {
        try
        {
            Envelope message = null, response = null;

            //Tell the server to delete a user
            message = new Envelope("DUSER");
            message.addObject(this.session.getSequenceNum());
            message.addObject(userName); //Add user name
            message.addObject(requester);  //Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);

            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return true;
            }

            return false;
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    public boolean createGroup(String groupName, String userName)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to create a group
            message = new Envelope("CGROUP");
            message.addObject(this.session.getSequenceNum());
            message.addObject(groupName); //Add the group name string
            message.addObject(userName); //Add the requester's userName

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);

            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return true;
            }

            return false;
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    public boolean deleteGroup(String groupName, String requester)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to delete a group
            message = new Envelope("DGROUP");
            message.addObject(this.session.getSequenceNum());
            message.addObject(groupName); //Add group name string
            message.addObject(requester); //Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);
            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return true;
            }

            return false;
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> listMembers(String group, String requester)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to return the member list
            message = new Envelope("LMEMBERS");
            message.addObject(this.session.getSequenceNum());
            message.addObject(group); //Add group name string
            message.addObject(requester); //Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);
            response = this.session.clientHmacVerify(response);

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

    public boolean addUserToGroup(String userName, String groupName, String requester)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to add a user to the group
            message = new Envelope("AUSERTOGROUP");
            message.addObject(this.session.getSequenceNum());
            message.addObject(userName); //Add user name string
            message.addObject(groupName); //Add group name string
            message.addObject(requester); //Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);
            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return true;
            }

            return false;
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    public boolean deleteUserFromGroup(String userName, String groupName, String requester)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to remove a user from the group
            message = new Envelope("RUSERFROMGROUP");
            message.addObject(this.session.getSequenceNum());
            message.addObject(userName); //Add user name string
            message.addObject(groupName); //Add group name string
            message.addObject(requester); //Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);
            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return true;
            }

            return false;
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    public boolean isAdmin(String userName) throws Exception
    {
        try
        {
            Envelope message = null, response = null;
            message = new Envelope("ISADMIN");
            message.addObject(this.session.getSequenceNum());
			message.addObject(userName);

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            //Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);

            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
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

	public boolean authChallenge(EncryptionSuite userKeys) throws Exception
	{

		// 1) Generate a challenge.

		// TODO: add hmac
        this.session.setHmacKey();
		SecureRandom prng = new SecureRandom();
        byte[] challenge = new byte[16];
        prng.nextBytes(challenge);

        try
        {
            Envelope message = null, response = null;
            message = new Envelope("AUTHCHALLENGE");
			message.addObject(challenge); // Add the nonce to our message
			message.addObject(this.session.getHmacKey().getEncryptionKey()); // add the hmac key to our message

            // 2) Encrypt challenge with GS public key
			Envelope encryptedMessage = this.session.getEncryptedMessageTargetKey(message);
            encryptedMessage.addObject(userKeys.getEncryptionKey());
            output.writeObject(encryptedMessage);

            // 3) Receive completed challenge and shared AES key
            response = userKeys.getDecryptedMessage((Envelope)input.readObject());

            response.removeObject(message.getObjContents().size()-1);
            System.out.println("Response bytes: "+javax.xml.bind.DatatypeConverter.printHexBinary(this.session.getEnvelopeBytes(response)));
            System.out.println("Client HMAC Key: "+this.session.getHmacKey().encryptionKeyToString());
            byte[] hmac1 = this.session.generateHmac(response);
            System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(hmac1));

            response = this.session.clientHmacVerify(response);

            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                byte[] completedChallenge = (byte[])response.getObjContents().get(0); // User's completed challenge H(R)
                if (userKeys.verifyChallenge(challenge, completedChallenge))
                {
                    System.out.println("Group Server successfully completed challenge!");
                }
                else
                {
                    System.out.println("Server failed to complete challenge! Session may have been hijacked!");
                    return false;
                }

                // 4) Store new shared key in sessionKey ES object
                Key sessionKey = (Key)response.getObjContents().get(1); // New session key from grp server
				this.session.setAESKey(sessionKey);
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

	public boolean authLogin(EncryptionSuite userKeys) throws Exception
	{
		// 1) Enter userName and password (SECURELY. SANITIZE INPUTS)

		SecureRandom prng = new SecureRandom();
        int sequenceNumber = prng.nextInt();
        this.session.setSequenceNum(Math.abs(sequenceNumber));

	    System.out.println("Enter username to login: ");
	    UserClient.userName = UserClient.inputValidation(UserClient.in.readLine());
        System.out.println("Enter Password: ");
	    String password = UserClient.inputValidation(UserClient.in.readLine());

        try
        {
            Envelope message = null, response = null;
            //Tell the server to return its public key
            message = new Envelope("AUTHLOGIN");
            message.addObject(this.session.getSequenceNum());
			message.addObject(UserClient.userName);
            message.addObject(password);
            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);
            output.writeObject(message);

            response = this.session.getDecryptedMessage((Envelope)input.readObject());
            response = this.session.clientSequenceNumberHandler(response);

            //If server indicates success, return true
            if(response.getMessage().equals("OK"))
            {
                return true;
            }
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
		// 2) Successfully logged in. Able to access group server
		return false;
	}

	public boolean authenticateGroupServer(EncryptionSuite userKeys, EncryptionSuite groupServerPublicKey) throws Exception
	{
		this.session = new Session();
		this.session.setTargetKey(groupServerPublicKey.getEncryptionKey());
        // Generate new object for encryption / decryption with gs public key
		if (this.authChallenge(userKeys) && this.authLogin(userKeys))
			return true;
		else
			return false;
	}


}
