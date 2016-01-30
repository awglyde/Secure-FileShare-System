import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Database
{

    private HashMap<String, User> users;
    private HashMap<String, Group> groups;

    public Database(String usersPath, String groupPath) throws IOException, ParseException
    {
        this.users = UserDB(usersPath);
        this.groups = GroupDB(groupPath);
    }

    public HashMap<String, User> UserDB(String path) throws IOException, ParseException
    {
        HashMap<String, User> userMap = new HashMap<>();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("db/users.json"));
        JSONArray users = (JSONArray) jsonObject.get("users");

        for(Object user : users)
        {
            JSONObject jo = (JSONObject) user;
            String name = jo.get("name").toString();
            String password = jo.get("password").toString();
            userMap.put(name, new User(name, password));
        }

        return userMap;
    }

    public HashMap<String, Group> GroupDB(String path)
    {
        // TODO fill in code to create hashmap from JSON
        return null;
    }

    /*
      Adds a group to the list of groups. Will return true if successful otherwise
      it will return false. groupName and ownerOfGroup cannot be null or made completely
      of whitespace.
    */
    public boolean addGroup(String groupName, String ownerOfGroup)
    {
        groupName = this.cleanInput(groupName);
        ownerOfGroup = this.cleanInput(ownerOfGroup);

        if(groupName == null || ownerOfGroup == null)
        {
            return false;
        }

        // create new Group object
        Group newGroup = new Group(groupName, ownerOfGroup, null);

        // add to list of groups hashmap
        this.groups.put(groupName, newGroup);

        return true;
    }

    public boolean removeGroup(String groupName)
    {
        // TODO
        return false;
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
    private String cleanInput(String input)
    {
        if(input == null) return null;
        String whitespace = "(\\w)";
        return input.replaceAll(whitespace, input);
    }

}

class User
{
    String username;
    String password;

    public User(String username, String password)
    {
        this.username = username; // user name, must be unique
        this.password = password; //store SHA-1
    }
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
