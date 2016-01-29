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

    public synchronized void addUser(String username)
    {
        User newUser = new User();
        this.list.put(username, newUser);
    }

    public synchronized void deleteUser(String username)
    {
        this.list.remove(username);
    }

    public synchronized boolean checkUser(String username)
    {
        if (this.list.containsKey(username))
        {
            return true;
        } else
        {
            return false;
        }
    }

    public synchronized ArrayList<String> getUserGroups(String username)
    {
        return this.list.get(username).getGroups();
    }

    public synchronized ArrayList<String> getUserOwnership(String username)
    {
        return this.list.get(username).getOwnership();
    }

    public synchronized void addGroup(String user, String groupname)
    {
        this.list.get(user).addGroup(groupname);
    }

    public synchronized void removeGroup(String user, String groupname)
    {
        this.list.get(user).removeGroup(groupname);
    }

    public synchronized void addOwnership(String user, String groupname)
    {
        this.list.get(user).addOwnership(groupname);
    }

    public synchronized void removeOwnership(String user, String groupname)
    {
        this.list.get(user).removeOwnership(groupname);
    }


    class User implements java.io.Serializable
    {

        /**
         *
         */
        private static final long serialVersionUID = -6699986336399821598L;
        private ArrayList<String> groups;
        private ArrayList<String> ownership;

        public User()
        {
            this.groups = new ArrayList<String>();
            this.ownership = new ArrayList<String>();
        }

        public ArrayList<String> getGroups()
        {
            return this.groups;
        }

        public ArrayList<String> getOwnership()
        {
            return this.ownership;
        }

        public void addGroup(String group)
        {
            this.groups.add(group);
        }

        public void removeGroup(String group)
        {
            if (!this.groups.isEmpty())
            {
                if (this.groups.contains(group))
                {
                    this.groups.remove(this.groups.indexOf(group));
                }
            }
        }

        public void addOwnership(String group)
        {
            this.ownership.add(group);
        }

        public void removeOwnership(String group)
        {
            if (!this.ownership.isEmpty())
            {
                if (this.ownership.contains(group))
                {
                    this.ownership.remove(this.ownership.indexOf(group));
                }
            }
        }

    }

}
