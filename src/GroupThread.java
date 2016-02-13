/* This thread does all the work. It communicates with the client through Envelopes.
 *
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

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

                Envelope message = (Envelope) input.readObject();
                System.out.println("Request received: " + message.getMessage());
                Envelope response;

                if(message.getMessage().equals("GET"))//Client wants a token
                {
                    String username = (String) message.getObjContents().get(0); //Get the username
                    if(username == null)
                    {
                        response = new Envelope("FAIL");
                        response.addObject(null);
                        output.writeObject(response);
                    }
                    else
                    {
                        UserToken yourToken = createToken(username); //Create a token

                        //Respond to the client. On error, the client will receive a null token
                        response = new Envelope("OK");
                        response.addObject(yourToken);
                        output.writeObject(response);
                    }
                }
                else if(message.getMessage().equals("CUSER")) //Client wants to create a user
                {
                    if(message.getObjContents().size() < 2) // If we don't get a token and a name, fail
                    {
                        response = new Envelope("FAIL");
                    }
                    else
                    {
                        response = new Envelope("FAIL");

                        // Checking first param isn't null
                        if(message.getObjContents().get(0) != null)
                        {
                            // Checking second param isn't null
                            if(message.getObjContents().get(1) != null)
                            {
                                String username = (String) message.getObjContents().get(0); //Extract the username
                                UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token

                                if(createUser(username, yourToken))
                                {
                                    response = new Envelope("OK"); //Success
                                }
                            }
                        }
                    }

                    output.writeObject(response);
                }
                else if(message.getMessage().equals("DUSER")) //Client wants to delete a user
                {

                    if(message.getObjContents().size() < 2)
                    {
                        response = new Envelope("FAIL");
                    }
                    else
                    {
                        response = new Envelope("FAIL");

                        if(message.getObjContents().get(0) != null)
                        {
                            if(message.getObjContents().get(1) != null)
                            {
                                String username = (String) message.getObjContents().get(0); //Extract the username
                                UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token

                                if(deleteUser(username, yourToken))
                                {
                                    response = new Envelope("OK"); //Success
                                }
                            }
                        }
                    }

                    output.writeObject(response);
                }
                else if(message.getMessage().equals("CGROUP")) //Client wants to create a group
                {
                    if(message.getObjContents().size() < 2) // If we don't get a token and a name, fail
                    {
                        response = new Envelope("FAIL");
                    }
                    else
                    {
                        response = new Envelope("FAIL");

                        // Checking first param isn't null
                        if(message.getObjContents().get(0) != null)
                        {
                            // Checking second param isn't null
                            if(message.getObjContents().get(1) != null)
                            {
                                String groupname = (String) message.getObjContents().get(0); //Extract the username
                                UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token

                                if(createGroup(groupname, yourToken))
                                {
                                    response = new Envelope("OK"); //Success
                                }
                            }
                        }
                    }

                    output.writeObject(response);
                }
                else if(message.getMessage().equals("DGROUP")) //Client wants to delete a group
                {
                    /* TODO:  Write this handler */
                }
                else if(message.getMessage().equals("LMEMBERS")) //Client wants a list of members in a group
                {
                    if(message.getObjContents().size() < 2) // If we don't get a token and a name, fail
                    {
                        response = new Envelope("FAIL");
                    }
                    else
                    {
                        response = new Envelope("FAIL");

                        // Checking first param isn't null
                        if(message.getObjContents().get(0) != null)
                        {
                            // Checking second param isn't null
                            if(message.getObjContents().get(1) != null)
                            {
                                String groupname = (String) message.getObjContents().get(0); //Extract the username
                                UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token

                                ArrayList<String> members = listMembers(groupname, yourToken);
                                if(members != null)
                                {
                                    response = new Envelope("OK"); //Success
                                    response.addObject(members);
                                }
                            }
                        }
                    }

                    output.writeObject(response);
                }
                else if(message.getMessage().equals("AUSERTOGROUP")) //Client wants to add user to a group
                {
                    if(message.getObjContents().size() < 3) // If we don't get a token and a name, fail
                    {
                        response = new Envelope("FAIL");
                    }
                    else
                    {
                        response = new Envelope("FAIL");

                        // Checking params aren't null
                        if( message.getObjContents().get(0) != null ||
                            message.getObjContents().get(1) != null ||
                            message.getObjContents().get(2) != null )
                        {
                            // Checking second param isn't null
                            String userName = (String) message.getObjContents().get(0); //Extract the userName
                            String groupName = (String) message.getObjContents().get(1); //Extract the groupName
                            UserToken ownerToken = (UserToken) message.getObjContents().get(2); //Extract the owner token

                            if(addUserToGroup(userName, groupName, ownerToken))
                            {
                                response = new Envelope("OK"); //Success
                            }
                        }
                    }

                    output.writeObject(response);
                }
                else if(message.getMessage().equals("RUSERFROMGROUP")) //Client wants to remove user from a group
                {
                    if(message.getObjContents().size() < 3) // If we don't get a token and a name, fail
                    {
                        response = new Envelope("FAIL");
                    }
                    else
                    {
                        response = new Envelope("FAIL");

                        // Checking params aren't null
                        if( message.getObjContents().get(0) != null ||
                            message.getObjContents().get(1) != null ||
                            message.getObjContents().get(2) != null )
                        {
                            // Checking second param isn't null
                            String userName = (String) message.getObjContents().get(0); //Extract the userName
                            String groupName = (String) message.getObjContents().get(1); //Extract the groupName
                            UserToken ownerToken = (UserToken) message.getObjContents().get(2); //Extract the owner token

                            // If we're in the special case where the username being removed
                            // is the owner of the group they're being removed from, then delete the group
                            if(my_gs.groupList.getGroupOwnership(groupName).equals(userName))
                            {
                                if (deleteGroup(groupName, ownerToken))
                                {
                                    response = new Envelope("OK"); // success
                                }
                            }
                            else if(deleteUserFromGroup(userName, groupName, ownerToken))
                            {
                                response = new Envelope("OK"); //Success
                            }
                        }
                    }

                    output.writeObject(response);
                }
                else if(message.getMessage().equals("DISCONNECT")) //Client wants to disconnect
                {
                    socket.close(); //Close the socket
                    proceed = false; //End this communication loop
                }
                else
                {
                    response = new Envelope("FAIL"); //Server does not understand client request
                    output.writeObject(response);
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
    private UserToken createToken(String username)
    {
        //Check that user exists
        if(my_gs.userList.checkUser(username))
        {
            //Issue a new token with server's name, user's name, and user's groups
            UserToken yourToken = new Token(my_gs.name, username, my_gs.userList.getUserGroups(username));
            return yourToken;
        }
        else
        {
            return null;
        }
    }


    //Method to create a user
    private boolean createUser(String username, UserToken yourToken)
    {
        String requester = yourToken.getSubject();

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
                    my_gs.userList.addUser(username);
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
    private boolean deleteUser(String username, UserToken yourToken)
    {
        String requester = yourToken.getSubject();

        //Does requester exist?
        if(my_gs.userList.checkUser(requester))
        {
            ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
            //requester needs to be an administer
            if(temp.contains("ADMIN"))
            {
                //Does user exist?
                if(my_gs.userList.checkUser(username))
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

                    //Delete owned groups
                    /*for(int index = 0; index < deleteOwnedGroup.size(); index++)
                    {
                        //Use the delete group method. Token must be created for this action
                        // TODO: deleteGroup is defined in the GroupClient object
                        // deleteGroup(deleteOwnedGroup.get(index), new Token(my_gs.name, username, deleteOwnedGroup));
                    }*/

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

    private boolean createGroup(String groupName, UserToken yourToken)
    {
        // creator of group
        String requester = yourToken.getSubject();

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

    public boolean deleteGroup(String groupname, UserToken token)
    {
        // TODO implement this
        return false;
    }

    private ArrayList<String> listMembers(String groupName, UserToken yourToken)
    {
        // Member of group
        String requester = yourToken.getSubject();

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

    public boolean addUserToGroup(String userName, String groupName, UserToken ownerToken)
    {
        // Owner of group (HOPEFULLY)
        String requester = ownerToken.getSubject();

        //Check if group exists & requester is owner
        if(my_gs.groupList.checkGroup(groupName) && my_gs.groupList.getGroup(groupName).isOwner(requester))
        {
            // Check if user exists && is not already a member of the group
            if(!my_gs.userList.checkUser(userName) && !my_gs.groupList.getGroup(groupName).isMember(userName))
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

    public boolean deleteUserFromGroup(String username, String groupname, UserToken token)
    {
        // Owner of group (HOPEFULLY)
        String requester = token.getSubject();

        // Check to make sure the group is NOT THE ADMIN GROUP AND
        // make sure user being removed is not the OWNER OF THE ADMIN GROUP
        // It's okay if we're removing a user from the Admin group as long as it's not the owner
        // It's also okay if we're the owner of the admin group as long as we're not removing
        // the owner of the admin group itself
        if( !(my_gs.groupList.getGroup(groupname).equals("ADMIN") &&
            my_gs.groupList.getGroup("ADMIN").getOwnerName().equals(username)) )
        {
            // Check if group exists & requester is owner
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
            return false; //
        }
    }

}
