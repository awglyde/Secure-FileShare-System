import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.security.Key;

/* This list represents the users on the server */

public class GroupList implements java.io.Serializable
{
    private static final long serialVersionUID = 7660378893561417352L;
    private Hashtable<String, Group> list = new Hashtable<String, Group>();

    public synchronized boolean isAdmin(String username)
    {
        return this.getGroup("ADMIN").isMember(username);
    }
    public synchronized void addGroup(String owner, String groupName) throws Exception
    {
        Group newGroup = new Group(owner);

        this.list.put(groupName, newGroup);
    }

    public synchronized boolean deleteGroup(String groupName)
    {
        return this.list.remove(groupName) != null;
    }

    public synchronized Group getGroup(String groupName)
    {
        return this.list.get(groupName);
    }

    public synchronized boolean isGroup(String groupName)
    {
        if (this.list.containsKey(groupName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public synchronized boolean isGroupOwner(String userName, String groupName)
    {
        String owner = this.getGroupOwnership(groupName);

        if (owner == null)
        {
            return false;
        }

        return owner.equals(userName);
    }

    public synchronized String getGroupOwnership(String groupName)
    {
        Group group = this.getGroup(groupName);

        if (group == null)
        {
            return null;
        }

        return this.getGroup(groupName).getOwnerName();
    }

    public synchronized boolean removeMember(String userName, String groupName) throws Exception
    {
        boolean ret = this.getGroup(groupName).removeMember(userName);

        if (this.getGroup(groupName).getOwnerName().equals(userName))
        {
            ret &= this.deleteGroup(groupName);
        }

        return ret;
    }

    public synchronized ArrayList<String> getMembers(String groupName)
    {
        if (this.getGroup(groupName) == null)
        {
            return null;
        }

        return this.getGroup(groupName).getMemberNames();
    }

    public synchronized ArrayList<Key> getGroupKeys(String groupName)
    {
        if (this.getGroup(groupName) == null)
        {
            return null;
        }

        return this.getGroup(groupName).getGroupKeys();
    }

    public synchronized Hashtable<String, ArrayList<Key> > getGroupsAndKeys(String userName)
    {
        Hashtable<String, ArrayList<Key> > hashmap = new Hashtable<String, ArrayList<Key> >();

        // generate hash map of group name to key list
        for (Map.Entry<String, Group> entry : this.list.entrySet())
        {
            String groupName = entry.getKey();
            Group group = entry.getValue();

            // verify user is a memeber of the group
            if (group.isMember(userName))
            {
                // add groupname and keys to hashmap
                hashmap.put(groupName, group.getGroupKeys());
            }
        }

        return hashmap;
    }

    public static class Group implements java.io.Serializable
    {
        private static final long serialVersionUID = -6688886336399711764L;

        private ArrayList<String> groupMembers;
        String owner;

        // list of keys, the index will represent the verison number
        private ArrayList<Key> keys;

        public Group(String owner) throws Exception
        {
            this.groupMembers = new ArrayList<String>();
            groupMembers.add(owner);

            // add the first key to the list of keys
            this.keys = new ArrayList<Key>();
            this.addNewKey();

            this.owner = owner;
        }

        public ArrayList<Key> getGroupKeys()
        {
            return this.keys;
        }

        private void addNewKey() throws Exception
        {
            EncryptionSuite newES = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES);

            this.keys.add(newES.getEncryptionKey());
        }

        public boolean isMember(String userName)
        {
            return this.groupMembers.contains(userName);
        }

        public ArrayList<String> getMemberNames()
        {
            return this.groupMembers;
        }

        public boolean addMember(String member)
        {
            return this.groupMembers.add(member);
        }

        public boolean removeMember(String member) throws Exception
        {
            if (!this.groupMembers.isEmpty())
            {
                if (this.groupMembers.contains(member))
                {
                    // if a member was removed add a new key to the groups keys
                    this.addNewKey();

                    // remove the user from the group
                    return this.groupMembers.remove(member);
                }
            }

            return false;
        }

        public boolean isOwner(String userName)
        {
            return this.getOwnerName().equals(userName);
        }

        public String getOwnerName()
        {
            return this.owner;
        }

        public void setOwner(String owner)
        {
            // Check to make sure the new owner is a member of the Group
            if (this.groupMembers.contains(owner))
            {
                this.owner = owner;
            }
        }
    }
}
