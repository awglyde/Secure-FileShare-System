import java.util.ArrayList;
import java.util.Hashtable;

/* This list represents the users on the server */

public class UserList implements java.io.Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 7600343803563417992L;
    private Hashtable<String, User> list = new Hashtable<String, User>();

    public synchronized void addUser(String username, byte[] saltedPwHash, byte[] salt)
    {
        User newUser = new User();
        newUser.setPasswordHash(saltedPwHash);
        newUser.setPasswordSalt(salt);
        this.list.put(username, newUser);
    }

    public synchronized void deleteUser(String username)
    {
        this.list.remove(username);
    }

    // TODO: rename to isGroup (ALSO NEED TO RENAME GROUPLIST checkUser method)
    public synchronized boolean checkUser(String username)
    {
        if(this.list.containsKey(username))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public synchronized User getUser(String username)
    {
        return this.list.get(username);
    }

    public synchronized boolean isLocked(String username)
    {
        return this.list.get(username).isLocked();
    }

    public synchronized void failedLogin(String username)
    {
        this.list.get(username).failedLogin();
    }

    public synchronized void unlockUser(String username)
    {
        this.list.get(username).unlock();
    }

    public synchronized byte[] getPasswordHash(String username)
    {
        return this.getUser(username).getPasswordHash();
    }

    public synchronized byte[] getPasswordSalt(String username)
    {
        return this.getUser(username).getPasswordSalt();
    }

    public synchronized ArrayList<String> getUserGroups(String username)
    {
        return this.list.get(username).getGroups();
    }

    public synchronized ArrayList<String> getUserOwnership(String username)
    {
        return this.list.get(username).getOwnership();
    }

    public synchronized boolean addGroup(String user, String groupname)
    {
        return this.list.get(user).addGroup(groupname);
    }

    public synchronized boolean removeGroup(String user, String groupname)
    {
        return this.list.get(user).removeGroup(groupname);
    }

    public synchronized boolean addOwnership(String user, String groupname)
    {
        return this.list.get(user).addOwnership(groupname);
    }

    public synchronized boolean removeOwnership(String user, String groupname)
    {
        return this.list.get(user).removeOwnership(groupname);
    }

    // Method removes association from all users with this group
    public synchronized void removeAssociation(String groupname)
    {
        for (User user : this.list.values())
        {
            // If the user is the owner af the  group, remove their ownership
            if (user.getOwnership().contains(groupname))
                user.removeOwnership(groupname);

            // If the user is simply a member of the group, remove their membership
            if (user.getGroups().contains(groupname))
                user.removeGroup(groupname);
        }
    }


    public static class User implements java.io.Serializable
    {
        private static final long serialVersionUID = -6699986336399821598L;
        private ArrayList<String> groups;
        private ArrayList<String> ownership;
        private byte[] passwordHash = null;
        private byte[] passwordSalt = null;

        private int numLogin = 0;

        String username = "";
        Token userToken = null;

        public User()
        {
            this.groups = new ArrayList<String>();
            this.ownership = new ArrayList<String>();
        }

        public void failedLogin()
        {
            numLogin++;
        }

        public boolean isLocked()
        {
            boolean locked = false;
            if(numLogin >= 3)
            {
                locked = true;
            }
            return locked;
        }

        public void unlock()
        {
            numLogin = 0;
        }

        // if the user is locked return null so that they cannot login
        public byte[] getPasswordHash()
        {
            return this.passwordHash;
        }

        public void setPasswordHash(byte[] hash)
        {
            this.passwordHash = hash;
        }

        public byte[] getPasswordSalt()
        {
            return this.passwordSalt;
        }

        public void setPasswordSalt(byte[] salt)
        {
            this.passwordSalt = salt;
        }

        public ArrayList<String> getGroups()
        {
            return this.groups;
        }

        public ArrayList<String> getOwnership()
        {
            return this.ownership;
        }

        public boolean addGroup(String group)
        {
            return this.groups.add(group);
        }

        public boolean removeGroup(String group)
        {
            if(!this.groups.isEmpty())
            {
                if(this.groups.contains(group))
                {
                    return this.groups.remove(group);
                }
            }

            return false;
        }

        public boolean addOwnership(String group)
        {
            return this.ownership.add(group);
        }

        public boolean removeOwnership(String group)
        {
            if(!this.ownership.isEmpty())
            {
                if(this.ownership.contains(group))
                {
                    return this.ownership.remove(group);
                }
            }
            return false;
        }

    }

}
