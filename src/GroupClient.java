/* Implements the GroupClient Interface */

import java.util.ArrayList;
import java.util.List;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.*;

public class GroupClient extends Client implements GroupClientInterface
{
    public static final int SERVER_PORT = 8765;

    public UserToken getToken(String userName, Key publicKey)
    {
        try
        {
            UserToken token = null;
            Envelope message = null, response = null;

            //Tell the server to return a token.
            message = new Envelope("GET");
            message.addObject(userName); //Add user name string
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

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

    public boolean unlockUser(String username, String password, String requester, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;
            message = new Envelope("UNLOCKUSER");
            message.addObject(username); //Add user name string
            message.addObject(password);
            message.addObject(requester); //Add the requester
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

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

    public boolean createUser(String userName, String password, String requester, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to create a user
            message = new Envelope("CUSER");
            message.addObject(userName); //Add user name string
            message.addObject(password); //Add the new user's password
            message.addObject(requester); //Add the requester
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

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

    public boolean deleteUser(String userName, String requester, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;

            //Tell the server to delete a user
            message = new Envelope("DUSER");
            message.addObject(userName); //Add user name
            message.addObject(requester);  //Add requester's token

            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

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

    public boolean createGroup(String groupName, String userName, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to create a group
            message = new Envelope("CGROUP");
            message.addObject(groupName); //Add the group name string
            message.addObject(userName); //Add the requester's userName
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

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

    public boolean deleteGroup(String groupName, String requester, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to delete a group
            message = new Envelope("DGROUP");
            message.addObject(groupName); //Add group name string
            message.addObject(requester); //Add requester's token
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());
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
    public List<String> listMembers(String group, String requester, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to return the member list
            message = new Envelope("LMEMBERS");
            message.addObject(group); //Add group name string
            message.addObject(requester); //Add requester's token
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

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

    public boolean addUserToGroup(String userName, String groupName, String requester, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to add a user to the group
            message = new Envelope("AUSERTOGROUP");
            message.addObject(userName); //Add user name string
            message.addObject(groupName); //Add group name string
            message.addObject(requester); //Add requester's token
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());
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

    public boolean deleteUserFromGroup(String userName, String groupName, String requester, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;
            //Tell the server to remove a user from the group
            message = new Envelope("RUSERFROMGROUP");
            message.addObject(userName); //Add user name string
            message.addObject(groupName); //Add group name string
            message.addObject(requester); //Add requester's token
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());
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

	public boolean authChallenge(EncryptionSuite userKeys) throws Exception
	{

		// 1) Generate a challenge.

		// TODO: add hmac
		// EncryptionSuite hmac = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES);
		SecureRandom prng = new SecureRandom();
        byte[] challenge = new byte[16];
        prng.nextBytes(challenge);

        try
        {
            Envelope message = null, response = null;
            message = new Envelope("AUTHCHALLENGE");
			message.addObject(challenge); // Add the nonce to our message
			// message.addObject(hmac.getEncryptionKey()); // add the hmac key to our message

            // 2) Encrypt challenge with GS public key
			Envelope encryptedMessage = this.groupServerPublicKey.getEncryptedMessage(message);
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
                    System.out.println("Group Server successfully completed challenge!");
                }
                else
                {
                    System.out.println("Server failed to complete challenge! Session may have been hijacked!");
                    return false;
                }

                // 4) Store new shared key in sessionKey ES object
                Key sessionKey = (Key)response.getObjContents().get(1); // New session key from grp server
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

	public boolean authLogin(EncryptionSuite userKeys) throws Exception
	{
		// 1) Enter userName and password (SECURELY. SANITIZE INPUTS)

	    System.out.println("Enter username to login: ");
	    UserClient.userName = UserClient.inputValidation(UserClient.in.readLine());
        System.out.println("Enter Password: ");
	    String password = UserClient.inputValidation(UserClient.in.readLine());

        try
        {
            Envelope message = null, response = null;
            //Tell the server to return its public key
            message = new Envelope("AUTHLOGIN");
			message.addObject(UserClient.userName);
            message.addObject(password);
            // Get encrypted message from our EncryptionSuite
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT: Server needs to know which user's session key to decrypt with
            message.addObject(userKeys.getEncryptionKey().hashCode()); //Add user public key hash
            output.writeObject(message);


            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

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

	public boolean authenticateGroupServer(EncryptionSuite userKeys) throws Exception
	{
        // Get group server public key
        Key groupServerPublicKey = this.getGroupServerPublicKey(userKeys);
		// this.groupServerPublicKey.setEncryptionKey(groupServerPublicKey);
        this.groupServerPublicKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA, groupServerPublicKey, null);
        // Generate new object for encryption / decryption with gs public key
		if (this.authChallenge(userKeys) && this.authLogin(userKeys))
			return true;
		else
			return false;
	}

    public boolean isAdmin(String userName, Key publicKey)
    {
        try
        {
            Envelope message = null, response = null;
            message = new Envelope("ISADMIN");
			message.addObject(userName);
            message = this.sessionKey.getEncryptedMessage(message);
            // SESSION KEY MANAGEMENT. Server needs to know which user's session key to decrypt with
             //Add user public key hash
            output.writeObject(message);

            //Get the response from the server
            response = this.sessionKey.getDecryptedMessage((Envelope)input.readObject());

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

}
