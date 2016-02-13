import java.util.ArrayList;
import java.util.Hashtable;

/* This list represents the users on the server */

public class GroupList implements java.io.Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 7660378893561417352L;
    private Hashtable<String, Group> list = new Hashtable<String, Group>();

    public synchronized void addGroup(String groupName)
    {
        Group newGroup = new Group();
        this.list.put(groupName, newGroup);
    }

    // TODO: REMOVE ASSOCIATION FROM ALL USERS WITH GROUP
    public synchronized void deleteGroup(String groupName)
    {
        this.list.remove(groupName);
    }

    public synchronized Group getGroup(String groupName)
    {
        return this.list.get(groupName);
    }

    // TODO: rename to isGroup (ALSO NEED TO RENAME USERLIST checkUser method)
    public synchronized boolean checkGroup(String groupName)
    {
        if(this.list.containsKey(groupName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public synchronized String getGroupOwnership(String groupName)
    {
        return this.list.get(groupName).getOwnerName();
    }

    public synchronized void removeMember(String userName, String groupName)
    {
        this.list.get(groupName).removeMember(userName);
        if (this.list.get(groupName).getOwnerName().equals(userName))
            this.deleteGroup(groupName);

    }

    public static class Group implements java.io.Serializable
    {
        private static final long serialVersionUID = -6688886336399711764L;

        private ArrayList<String> groupMembers;
        String owner;

        public Group()
        {
            this.groupMembers = new ArrayList<String>();
            this.owner = null;
        }

        public ArrayList<String> getMemberNames()
        {
            return this.groupMembers;
        }

        public void addMember(String member)
        {
            this.groupMembers.add(member);
        }

        public void removeMember(String member)
        {
            if(!this.groupMembers.isEmpty())
            {
                if(this.groupMembers.contains(member))
                {
                    this.groupMembers.remove(this.groupMembers.indexOf(member));
                }
            }
        }

        public String getOwnerName()
        {
            return this.owner;
        }

        public void setOwner(String owner)
        {
            // Check to make sure the new owner is a member of the Group
            if (this.groupMembers.contains(owner))
                this.owner = owner;
        }

    }

}
