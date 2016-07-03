/* Implements the GroupClient Interface */
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Arrays;
import java.util.List;
import java.lang.Math;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.*;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import javax.xml.bind.DatatypeConverter;

public class GroupClient extends Client implements GroupClientInterface
{
    public static final int SERVER_PORT = 8765;

    @SuppressWarnings("unchecked") public Pair < UserToken, Hashtable < String, ArrayList < Key >>> getToken(String userName, Key fileServerPublicKey) {
        try
        {
            Envelope message = null, response = null;

            // Tell the server to return a token.
            message = new Envelope("GET");
            message.addObject(this.session.getSequenceNum());

            message.addObject(userName); // Add user name string
            // add file serverpublic key, used to uniquely associate this token w a file server
            message.addObject(fileServerPublicKey);

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);
            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // Successful response
            if (response.getMessage().equals("OK"))
            {
                // If there is a token in the Envelope, return it
                ArrayList<Object> temp = null;
                temp = response.getObjContents();

                if (temp.size() == 2)
                {
                    UserToken token = (UserToken)temp.get(0);

                    Hashtable<String, ArrayList<Key> > keyRing = (Hashtable<String, ArrayList<Key> >) this.session.getObjectFromBytes((byte[])temp.get(1));

                    // return a pair of the user token and the keyRing
                    return new Pair < UserToken, Hashtable < String, ArrayList < Key >>> (token, keyRing);
                }
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return null;
            }

            return null;
        }
        catch (Exception e)
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
            message.addObject(username); // Add user name string
            message.addObject(password);
            message.addObject(requester); // Add the requester

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);

            return false;
        }
    }

    public boolean createUser(String userName, String email, String password, String requester)
    {
        try
        {
            Envelope message = null, response = null;
            // Tell the server to create a user
            message = new Envelope("CUSER");
            message.addObject(this.session.getSequenceNum());
            message.addObject(userName); // Add user name string
            message.addObject(email); // Add user email string
            message.addObject(password); // Add the new user's password
            message.addObject(requester); // Add the requester

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }

            return false;
        }
        catch (Exception e)
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

            // Tell the server to delete a user
            message = new Envelope("DUSER");
            message.addObject(this.session.getSequenceNum());
            message.addObject(userName); // Add user name
            message.addObject(requester);  // Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }

            return false;
        }
        catch (Exception e)
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
            // Tell the server to create a group
            message = new Envelope("CGROUP");
            message.addObject(this.session.getSequenceNum());
            message.addObject(groupName); // Add the group name string
            message.addObject(userName); // Add the requester's userName

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }

            return false;
        }
        catch (Exception e)
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
            // Tell the server to delete a group
            message = new Envelope("DGROUP");
            message.addObject(this.session.getSequenceNum());
            message.addObject(groupName); // Add group name string
            message.addObject(requester); // Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }

            return false;
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);

            return false;
        }
    }

    @SuppressWarnings("unchecked") public List<String> listMembers(String group, String requester)
    {
        try
        {
            Envelope message = null, response = null;
            // Tell the server to return the member list
            message = new Envelope("LMEMBERS");
            message.addObject(this.session.getSequenceNum());
            message.addObject(group); // Add group name string
            message.addObject(requester); // Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return the member list
            if (response.getMessage().equals("OK"))
            {
                return (List<String>)response.getObjContents().get(0);  // This cast creates compiler warnings. Sorry.
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return null;
            }

            return null;
        }
        catch (Exception e)
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
            // Tell the server to add a user to the group
            message = new Envelope("AUSERTOGROUP");
            message.addObject(this.session.getSequenceNum());
            message.addObject(userName); // Add user name string
            message.addObject(groupName); // Add group name string
            message.addObject(requester); // Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }

            return false;
        }
        catch (Exception e)
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
            // Tell the server to remove a user from the group
            message = new Envelope("RUSERFROMGROUP");
            message.addObject(this.session.getSequenceNum());
            message.addObject(userName); // Add user name string
            message.addObject(groupName); // Add group name string
            message.addObject(requester); // Add requester's token

            // Generate HMAC for message and add it to the envelope
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);

            output.writeObject(message);

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }

            return false;
        }
        catch (Exception e)
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

            // Get the response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Verify the sequence number sent by the group server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);

            return false;
        }

        return false;
    }

    public boolean authChallenge(EncryptionSuite userKeys) throws Exception
    {
        // 1) Generate a challenge & set HMAC
        this.session.setHmacKey();
        SecureRandom prng = new SecureRandom();
        byte[] challenge = new byte [16];
        prng.nextBytes(challenge);

        try
        {
            Envelope message = null, response = null;
            message = new Envelope("AUTHCHALLENGE");

            System.out.println("Enter username to receive an authentication email: ");
            UserClient.userName = UserClient.inputValidation(UserClient.in.readLine());

            message.addObject(challenge); // Add the nonce to our message
            message.addObject(this.session.getHmacKey().getEncryptionKey()); // add the hmac key to our message
            message.addObject(UserClient.userName);
            // Add an HMAC of our message created using the user's public RSA key
            message.addObject(userKeys.generateHmac(this.session.getBytes(message)));

            // 2) Encrypt challenge with GS public key
            Envelope encryptedMessage = this.session.getEncryptedMessageTargetKey(message);
            encryptedMessage.addObject(userKeys.getEncryptionKey());
            output.writeObject(encryptedMessage);

            // 3) Receive completed challenge and shared AES key
            response = userKeys.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
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
            else if (response.getMessage().equals("FAIL"))
            {
                return false;
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);

            return false;
        }

        return false;
    }

    public boolean authLogin(EncryptionSuite userKeys) throws Exception
    {
        // 1) Enter username and password
        SecureRandom prng = new SecureRandom();
        int sequenceNumber = prng.nextInt();

        this.session.setSequenceNum(Math.abs(sequenceNumber));

        System.out.println("Enter Authentication Code: ");
        String authCode = UserClient.inputValidation(UserClient.in.readLine());

        try
        {
            // Parse hex binary from user input of auth code
            byte[] encryptedAuthCodeBytes = DatatypeConverter.parseHexBinary(authCode);

            // Decrypt the auth code from user's email
            byte[] decryptedAuthCode = userKeys.decryptBytes(encryptedAuthCodeBytes);

            // Get the Integer object back from the bytes
            Integer finalAuthCode = (Integer)session.getObjectFromBytes(decryptedAuthCode);

            System.out.println("Enter password to complete login: ");
            String password = UserClient.inputValidation(UserClient.in.readLine());

            Envelope message = null, response = null;
            // Tell the server to return its public key
            message = new Envelope("AUTHLOGIN");
            // Add our new sequence number to the message
            message.addObject(this.session.getSequenceNum());
            // Add the auth code
            message.addObject(finalAuthCode);
            // Add the password
            message.addObject(password);
            // Generate an HMAC for the server to verify
            message.addObject(session.generateHmac(message));

            // Get encrypted message from our EncryptionSuite
            message = this.session.getEncryptedMessage(message);
            output.writeObject(message);

            // Get the decrypted response from the server
            response = this.session.getDecryptedMessage((Envelope)input.readObject());

            // Verify the HMAC sent by the group server
            response = this.session.clientHmacVerify(response);

            // Validate the sequence number from the server
            response = this.session.clientSequenceNumberHandler(response);

            // If server indicates success, return true
            if (response.getMessage().equals("OK"))
            {
                return true;
            }
        }
        catch (Exception e)
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
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
