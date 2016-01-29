import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class Database
{
    HashMap<String, Group> groups = new HashMap<String, Group>();
    HashMap<String, User> users = new HashMap<String, User>();

    /*
      Default Constructor for Database
    */
    public Database()
    {

    }

    /*
      Adds a group to the list of groups. Will return true if successful otherwise
      it will return false. groupName and ownerOfGroup cannot be null or made completely
      of whitespace.
    */
    public boolean addGroup(String groupName, String ownerOfGroup)
    {
      boolean success = true;

      if(!this.isStringNullOrWhiteSpace(groupName) && !this.isStringNullOrWhiteSpace(ownerOfGroup))
      {
        // create new Group object
        Group newGroup = new Group(groupName, ownerOfGroup, null);

        // add to list of groups hashmap
        groups.put(groupName, newGroup);
      }
      else
      {
        success = false;
      }

      return success;
    }

    public boolean removeGroup(String groupName)
    {
        boolean success = true;

        if(!this.isStringNullOrWhiteSpace(groupName))
        {
          // add to list of groups hashmap
          groups.remove(groupName);
        }
        else
        {
          success = false;
        }

        return success;
    }

    public void addUser()
    {

    }

    public void removeUser()
    {

    }

    public void createUser()
    {

    }

    public void deleteUser()
    {

    }

    public void changeOwner()
    {

    }

    public Group getGroup()
    {
        return null;
    }

    /*
      Returns a list of Group objects. If there are no groups, returns null.
    */
    public ArrayList<Group> getGroups()
    {
        ArrayList<Group> groupList = null;

        if(groups.size() > 0)
        {
          groupList = new ArrayList<Group>(groups.values());
        }

        return groupList;
    }

    public User getUser()
    {
        return null;
    }

    public ArrayList<String> getUsers()
    {
        return null;
    }

    /*
      Used to verify if a String is null or completely whitespace. Returns true
      if the string is null or made up of only whitespace. Otherwise returns false.
    */
    private boolean isStringNullOrWhiteSpace(String name)
    {
      boolean isNullOrWhiteSpace = false;

      if(name == null)
      {
        isNullOrWhiteSpace = true;
      } else {
        String nameWithoutWhiteSpace = name.replaceAll(" ", "");
        if (nameWithoutWhiteSpace.length() == 0)
        {
          isNullOrWhiteSpace = true;
        }
      }

      return isNullOrWhiteSpace;
    }

}

class User
{
  String name; // user name, must be unique
  String password; //store SHA-1
}

class Group
{
  String name; // group name, must be unqiue
  String owner; // user name that is the owner of the groups
  ArrayList<String> users; // list of users that have access to the group

  /*
    Default Constructor for Group
  */
  public Group()
  {
    name = "";
    owner = "";
    users = new ArrayList<String>();
  }

  /*
    Constructor for Group
  */
  public Group(String groupName, String ownerName, ArrayList<String> listOfUsers)
  {
    name = groupName;
    owner = ownerName;
    users = listOfUsers;
  }

  public String toString()
  {
    String printString = name + " : " + owner;
    return printString;
  }
}
