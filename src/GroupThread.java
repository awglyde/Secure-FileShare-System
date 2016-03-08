/* This thread does all the work. It communicates with the client through Envelopes.
 *
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.security.Key;

public class GroupThread extends Thread
{
    private final Socket socket;
    private GroupServer my_gs;

    public GroupThread(Socket _socket, GroupServer _gs)
    {
        socket = _socket;
        my_gs = _gs;
    }

    public void run()
    {
        boolean proceed = true;

        try
        {
            //Announces connection and opens object streams
            System.out.println("*** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + "***");
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            do
            {

                // flushes / resets the output stream
                output.flush();
                output.reset();

                // Receives message. Potentially encrypted or unencrypted
                Envelope message = (Envelope) input.readObject();
                EncryptionSuite sessionKey = null;
				if (message.getMessage().equals("ENCRYPTEDENV"+EncryptionSuite.ENCRYPTION_RSA))
                {
                    // Decrypt message with group server's private key
            		message = my_gs.serverRSAKeys.getDecryptedMessage(message);
                }
                else if (message.getMessage().equals("ENCRYPTEDENV"+EncryptionSuite.ENCRYPTION_AES))
                {
                    // Decrypt message with shared AES key
                    Integer clientPubHash = (Integer)message.getObjContents().get(1);

                    // gets shared AES for the correct client
                    sessionKey = my_gs.getSessionES(clientPubHash);
            		message = my_gs.getSessionES(clientPubHash).getDecryptedMessage(message);
                }

                System.out.println("Request received: " + message.getMessage());

                Envelope response = new Envelope("FAIL");

                if(message.getMessage().equals("UNLOCKUSER"))
                {
                    if(message.getObjContents().size() >= 3)
                    {
                        if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null
                                                                   && message.getObjContents().get(2) != null)
                        {
                            String username = (String) message.getObjContents().get(0); // Extract the username
                            String password = (String) message.getObjContents().get(1); // Extract the password
                            String requester = (String) message.getObjContents().get(2); // Extract the requester
                            if(unlockUser(username, password, requester, sessionKey))
                            {
                                response = new Envelope("OK");
                            }
                        }
                    }

                    // Encrypting it all and sending it along
                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if (message.getMessage().equals("GPUBLICKEY"))
                {
                    if(message.getObjContents().size() >= 1)
                    {
                        // Checking first param isn't null
                        if(message.getObjContents().get(0) != null)
                        {
							// Map the client's hash of their key to their key, so we know who we're talking to in the future
                            Key key = (Key)message.getObjContents().get(0);
		                    my_gs.mapClientCodeToPublicKey((Integer)key.hashCode(), key);
		                    response = new Envelope("OK");
							// Add the server's public key to the envelope and send it back.
		                    response.addObject(my_gs.getPublicKey());
						}
					}

                    output.writeObject(response);
                }
                else if (message.getMessage().equals("AUTHCHALLENGE"))
                {
					EncryptionSuite clientKeys = null;
                    if(message.getObjContents().size() >= 2)
                    {
                        // Checking first param isn't null
                        if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
                        {
		                    byte[] challenge = (byte[])message.getObjContents().get(0); // User's challenge R
		                    Integer clientPubHash = (Integer)message.getObjContents().get(1); // Hash of users pub key

		                    // Retrieving the client's public key from our hashmap
		                    Key clientPubKey = my_gs.getClientPublicKey(clientPubHash);
                            my_gs.removePublicKeyMapping(clientPubHash);

		                    // Generating a new AES session key
		                    sessionKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES);
                            // Adding session key to our mapping (Allows multiple users)
                            my_gs.mapSessionES(clientPubKey.hashCode(), sessionKey);

		                    // Making a temporary client key ES object to encrypt the session key with
		                    clientKeys = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA, clientPubKey, null);

		                    // Constructing the envelope
		                    response = new Envelope("OK");
		                    // Adding completed challenge
		                    response.addObject(sessionKey.hashBytes(challenge));
		                    // Adding new AES session key
		                    response.addObject(sessionKey.getEncryptionKey());
						}
					}

                    // Encrypting it all and sending it along
                    output.writeObject(clientKeys.getEncryptedMessage(response));
                }
                else if (message.getMessage().equals("AUTHLOGIN"))
                {
                    if(message.getObjContents().size() >= 2)
                    {
                        // Verifying the parameters passed in aren't null
                        if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
                        {
                            String username = (String) message.getObjContents().get(0); //Extract the username
                            String password = (String) message.getObjContents().get(1); //Extract the password
                            if(sessionKey.verifyUserPassword(password, my_gs.userList.getPasswordHash(username), my_gs.userList.getPasswordSalt(username))
                                && !my_gs.userList.isLocked(username))
                            {

                                System.out.println("SUCCESSFULLY VERIFIED USER PASSWORD!");
                                response = new Envelope("OK");
                            }
                            else
                            {
                                // keep track of the number of failed login attempts
                                my_gs.userList.failedLogin(username);
                                response = new Envelope("FAIL");
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("ISADMIN"))
                {
                    if(message.getObjContents().size() >= 1)
                    {
                        // Checking first param isn't null
                        if(message.getObjContents().get(0) != null)
                        {
                                String username = (String) message.getObjContents().get(0); //Extract the username
                                if(my_gs.groupList.isAdmin(username))
                                    response = new Envelope("OK"); //Success
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("GET"))//Client wants a token
                {
                    UserToken yourToken = null;
                    if(message.getObjContents().size() >= 1)
                    {
                        if(message.getObjContents().get(0) != null)
                        {
                            String username = (String) message.getObjContents().get(0); //Get the username
                            response = new Envelope("OK");
                            yourToken = createToken(username);
                        }
                    }

                    response.addObject(yourToken);
                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("CUSER")) //Client wants to create a user
                {
                    if(message.getObjContents().size() >= 3)
                    {
                        if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null
                                                                   && message.getObjContents().get(2) != null)
                        {
                            String username = (String) message.getObjContents().get(0); //Extract the username
                            String password = (String) message.getObjContents().get(1); //Extract the password
                            String requester = (String) message.getObjContents().get(2); //Extract the requester

                            if(!EncryptionSuite.verifyPassword(username, password))
                            {
                                response = new Envelope("BADPWD");
                            }
                            else
                            {
                                if(createUser(username, password, requester, sessionKey))
                                {
                                    response = new Envelope("OK"); //Success
                                }
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("DUSER")) //Client wants to delete a user
                {
                    if(message.getObjContents().size() >= 2)
                    {
                        if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
                        {
                            String username = (String) message.getObjContents().get(0); //Extract the username
                            String requester = (String) message.getObjContents().get(1); //Extract the requester

                            if(deleteUser(username, requester))
                            {
                                response = new Envelope("OK"); //Success
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("CGROUP")) //Client wants to create a group
                {
                    if(message.getObjContents().size() >= 2)
                    {
                        if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
                        {
                            String groupName = (String) message.getObjContents().get(0); //Extract the username
                            String requester = (String) message.getObjContents().get(1); //Extract the requester

                            if(createGroup(groupName, requester))
                            {
                                response = new Envelope("OK"); //Success
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("DGROUP")) //Client wants to delete a group
                {
                    if(message.getObjContents().size() >= 2)
                    {
                        if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
                        {
                            String groupName = (String) message.getObjContents().get(0); //Extract the username
                            String requester = (String) message.getObjContents().get(1); //Extract the requester

                            if(deleteGroup(groupName, requester))
                            {
                                response = new Envelope("OK"); //Success
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("LMEMBERS")) //Client wants a list of members in a group
                {
                    if(message.getObjContents().size() >= 2)
                    {
                        if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
                        {
                            String groupname = (String) message.getObjContents().get(0); //Extract the username
                            String requester = (String) message.getObjContents().get(1); //Extract the requester

                            ArrayList<String> members = listMembers(groupname, requester);
                            if(members != null)
                            {
                                response = new Envelope("OK"); //Success
                                response.addObject(members);
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("AUSERTOGROUP")) //Client wants to add user to a group
                {
                    if(message.getObjContents().size() >= 3)
                    {
                        if( message.getObjContents().get(0) != null && message.getObjContents().get(1) != null
                                                                    && message.getObjContents().get(2) != null)
                        {
                            // Checking second param isn't null
                            String userName = (String) message.getObjContents().get(0); //Extract the userName
                            String groupName = (String) message.getObjContents().get(1); //Extract the groupName
                            String requester = (String) message.getObjContents().get(2); //Extract the requester

                            if(addUserToGroup(userName, groupName, requester))
                            {
                                response = new Envelope("OK"); //Success
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("RUSERFROMGROUP")) //Client wants to remove user from a group
                {
                    if(message.getObjContents().size() >= 3)
                    {
                        if( message.getObjContents().get(0) != null ||
                            message.getObjContents().get(1) != null ||
                            message.getObjContents().get(2) != null )
                        {
                            // Checking second param isn't null
                            String userName = (String) message.getObjContents().get(0); //Extract the userName
                            String groupName = (String) message.getObjContents().get(1); //Extract the groupName
                            String requester = (String) message.getObjContents().get(2); //Extract the requester

                            // If we're in the special case where the username being removed
                            // is the owner of the group they're being removed from, then delete the group
                            if(my_gs.groupList.isGroupOwner(userName, groupName))
                            {
                                if (deleteGroup(groupName, requester))
                                {
                                    response = new Envelope("OK"); // success
                                }
                            }
                            else if(deleteUserFromGroup(userName, groupName, requester))
                            {
                                response = new Envelope("OK"); //Success
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("DISCONNECT")) //Client wants to disconnect
                {

                    if(message.getObjContents().size() >= 1)
                    {

                        if(message.getObjContents().get(0) != null)
                        {
                            Integer clientPubHash = (Integer)message.getObjContents().get(0);
                            // remove session key and public key
                            my_gs.removePublicKeyMapping(clientPubHash);
                            my_gs.removeSessionESMapping(clientPubHash);
                        }
                    }

                    socket.close(); //Close the socket
                    proceed = false; //End this communication loop
                }
                else
                {
                    response = new Envelope("FAIL"); //Server does not understand client request
                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
            } while(proceed);
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    //Method to create tokens
    private UserToken createToken(String username) throws Exception
    {
        //Check that user exists
        if(my_gs.userList.checkUser(username))
        {
            //Issue a new token with server's name, user's name, and user's groups
            UserToken yourToken = new Token(my_gs.name, username, my_gs.userList.getUserGroups(username), my_gs.serverRSAKeys.getEncryptionKey());
			// Hash token, sign hash. Add signed   hash to token
			byte[] tokenHash = my_gs.serverRSAKeys.hashBytes(yourToken.toString().getBytes());
			byte[] signedTokenHash = my_gs.serverRSAKeys.generateSignature(tokenHash);
			yourToken.setSignedHash(signedTokenHash);
            return yourToken;
        }
        else
        {
            return null;
        }
    }

    // Method to unlock a users account
    private boolean unlockUser(String username, String password, String requester, EncryptionSuite sessionKey) throws Exception
    {
        //Check if requester exists
        if(my_gs.userList.checkUser(requester))
        {
            //Get the user's groups
            ArrayList<String> temp = my_gs.userList.getUserGroups(requester);

            // to unlock a user, the requester must be an administer
            if(temp.contains("ADMIN"))
            {
                // Does the user exist
                if(my_gs.userList.checkUser(username))
                {
                    // unlock the user
                    my_gs.userList.unlockUser(username);

                    //upadte their password
                    // Generate salt
                    byte[] tempSalt = sessionKey.generateSalt();
                    // salt and hash the password
                    byte[] saltedPwHash = sessionKey.saltAndHashPassword(password, tempSalt);
                    // Add user with their hashed and salted password
                    my_gs.userList.addUser(username, saltedPwHash, tempSalt);
                    return true;
                }
            }
        }
        return false;
    }

    //Method to create a user
    private boolean createUser(String username, String password, String requester, EncryptionSuite sessionKey) throws Exception
    {

        //Check if requester exists
        if(my_gs.userList.checkUser(requester))
        {
            //Get the user's groups
            ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
            //requester needs to be an administrator
            if(temp.contains("ADMIN"))
            {
                //Does user already exist?
                if(my_gs.userList.checkUser(username))
                {
                    return false; //User already exists
                }
                else
                {
                    // Generate salt
                    byte[] tempSalt = sessionKey.generateSalt();
                    // salt and hash the password
                    byte[] saltedPwHash = sessionKey.saltAndHashPassword(password, tempSalt);
                    // Add user with their hashed and salted password
                    my_gs.userList.addUser(username, saltedPwHash, tempSalt);
                    return true;
                }
            }
            else
            {
                return false; //requester not an administrator
            }
        }
        else
        {
            return false; //requester does not exist
        }
    }

    //Method to delete a user
    private boolean deleteUser(String username, String requester)
    {
        //Does requester exist?
        if(my_gs.userList.checkUser(requester))
        {
            ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
            //requester needs to be an administer
            if(temp.contains("ADMIN"))
            {
                //Does user exist and is the User the owner of ADMIN?
                // if they are the ADMIN owner, they cannot be deleted
                // if the requester is trying to delete them selves do not allow it - leaves program in odd state with requester still logged in
                if(my_gs.userList.checkUser(username) & !my_gs.groupList.isGroupOwner(username, "ADMIN") && !username.equals(requester))
                {
                    //User needs deleted from the groups they belong
                    ArrayList<String> deleteFromGroups = new ArrayList<String>();

                    //This will produce a hard copy of the list of groups this user belongs
                    for(int index = 0; index < my_gs.userList.getUserGroups(username).size(); index++)
                    {
                        deleteFromGroups.add(my_gs.userList.getUserGroups(username).get(index));
                    }

                    //Delete the user from the groups
                    //If user is the owner, removeMember will automatically delete group!
                    for(int index = 0; index < deleteFromGroups.size(); index++)
                    {
                        // Determine if user is the owner of the group we're trying to delete
                        // If so, we need to remove the association with the group from
                        // all other users within the group
                        if (my_gs.groupList.getGroup(deleteFromGroups.get(index)).isOwner(username))
                            my_gs.userList.removeAssociation(deleteFromGroups.get(index));

                        // Next, we want to remove the user as a member from all groups in
                        // the deleteFromGroups. if the user is owner this will also delete the group
                        my_gs.groupList.removeMember(username, deleteFromGroups.get(index));

                    }

                    //If groups are owned, they must be deleted
                    ArrayList<String> deleteOwnedGroup = new ArrayList<String>();

                    //Make a hard copy of the user's ownership list
                    for(int index = 0; index < my_gs.userList.getUserOwnership(username).size(); index++)
                    {
                        deleteOwnedGroup.add(my_gs.userList.getUserOwnership(username).get(index));
                    }

                    //Delete the user from the user list
                    my_gs.userList.deleteUser(username);

                    return true;
                }
                else
                {
                    return false; //User does not exist

                }
            }
            else
            {
                return false; //requester is not an administer
            }
        }
        else
        {
            return false; //requester does not exist
        }
    }

    private boolean createGroup(String groupName, String requester)
    {

        //Check if requester exists (creator of group)
        if(my_gs.userList.checkUser(requester))
        {
            //Does group already exist?
            if(my_gs.groupList.checkGroup(groupName))
            {
                return false; // Group already exists
            }
            else
            {
                my_gs.userList.addGroup(requester, groupName);
                my_gs.userList.addOwnership(requester, groupName);
                my_gs.groupList.addGroup(requester, groupName);
                return true;
            }
        }
        else
        {
            return false; //requester does not exist
        }
    }

    public boolean deleteGroup(String groupName, String requester)
    {

        // Check to make sure we're not deleting the ADMIN group
        if(!groupName.equals("ADMIN"))
        {
            // Check if group exists & requester is owner
            if( my_gs.groupList.checkGroup(groupName) &&
                my_gs.groupList.getGroup(groupName).isOwner(requester))
            {
                // Removes members association with the group being deleted
                my_gs.userList.removeAssociation(groupName);
                return my_gs.groupList.deleteGroup(groupName);
            }
            else
            {
                return false; // Group does not exist or requester is not owner
            }
        }
        else
        {
            return false; //
        }

    }

    private ArrayList<String> listMembers(String groupName, String requester)
    {
        //Check if requester exists (creator of group)
        if(my_gs.userList.checkUser(requester))
        {
            // Does group exist and is the requester a member of the group
            if( my_gs.groupList.getGroup(groupName) == null ||
                !my_gs.groupList.getGroup(groupName).getMemberNames().contains(requester))
            {
                return null; // Requester is NOT a member of the group, no authorization
            }
            else
            {
                return my_gs.groupList.getGroup(groupName).getMemberNames();
            }
        }
        else
        {
            return null; //requester does not exist
        }

    }

    public boolean addUserToGroup(String userName, String groupName, String requester)
    {

        //Check if group exists & requester is owner
        if(my_gs.groupList.checkGroup(groupName) && my_gs.groupList.getGroup(groupName).isOwner(requester))
        {
            // Check if user exists && is not already a member of the group
            // Check if user is the owner of the group, if they are do not allow them to add themselves as a memeber
            if((!my_gs.userList.checkUser(userName) ||
               my_gs.groupList.getGroup(groupName).isMember(userName)) ||
               my_gs.groupList.isGroupOwner(userName, groupName))
            {
                return false; // User name to add to group does not exist
            }
            else
            {
                return  my_gs.userList.addGroup(userName, groupName) &&
                        my_gs.groupList.getGroup(groupName).addMember(userName);
            }
        }
        else
        {
            return false; // Group does not exist or requester is not owner
        }

    }

    public boolean deleteUserFromGroup(String username, String groupname, String requester)
    {
        // Check to make sure the group is NOT THE ADMIN GROUP AND
        // make sure user being removed is not the OWNER OF THE ADMIN GROUP
        // It's okay if we're removing a user from the Admin group as long as it's not the owner
        // It's also okay if we're the owner of the admin group as long as we're not removing
        // the owner of the admin group itself
        if( !(groupname.equals("ADMIN") && my_gs.groupList.isGroupOwner(username, "ADMIN")))
        {
            // Check if group exists & requester is owner & the requester is not trying to delete themselves
            if( my_gs.groupList.checkGroup(groupname) &&
                my_gs.groupList.getGroup(groupname).isOwner(requester))
            {
                // Check if user exists && IS a member of the group
                if( my_gs.userList.checkUser(username) &&
                    my_gs.groupList.getGroup(groupname).isMember(username))
                {
                    return  my_gs.userList.removeGroup(username, groupname) &&
                            my_gs.groupList.getGroup(groupname).removeMember(username);
                }
                else
                {
                    return false;   // User name to add to group does not exist
                                    // OR User is not a member of the group
                }
            }
            else
            {
                return false; // Group does not exist or requester is not owner
            }
        }
        else
        {
            return false;
        }
    }

}
