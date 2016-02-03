import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Database
{
    private HashMap<String, User> users;
    private HashMap<String, Group> groups;

    //Read the database from the provided .json files paths.
    public Database(String usersPath, String groupPath) throws IOException, ParseException
    {
        this.readUserDB(usersPath);
        this.readGroupDB(groupPath);
    }

    // If no file paths are provided, create a new database.
    public Database()
    {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
    }

    // Update the users database from file
    private void readUserDB(String path) throws IOException, ParseException
    {
        this.users = new HashMap<>();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(path));
        JSONArray users = (JSONArray) jsonObject.get("users");

        for(Object user : users)
        {
            User newUser = new User((JSONObject) user);
            this.users.put(newUser.name, newUser);
        }
    }

    // Update the group database from file
    private void readGroupDB(String path) throws IOException, ParseException
    {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(path));
        JSONArray groups = (JSONArray) jsonObject.get("groups");

        for(Object group : groups)
        {
            Group newGroup = new Group((JSONObject) group);
            this.groups.put(newGroup.name, newGroup);
        }
    }

    // Used to help cleanse input of whitespace and other special characters
    private String cleanInput(String input)
    {
        if(input == null) return null;
        String whitespace = "(\\w)";
        return input.replaceAll(whitespace, input);
    }

    // Load both the user and group databases using the provided file paths
    public void loadDB(String usersPath, String groupPath)
    {
        try
        {
            this.readUserDB(usersPath);
            this.readGroupDB(groupPath);
        }
        catch(IOException | ParseException e)
        {
            e.printStackTrace();
        }
    }

    // Save both the user and group databases using the provided file paths
    public void saveDB(String usersPath, String groupPath) throws ParseException, IOException
    {
        FileWriter userFile = new FileWriter(new File(usersPath));
        userFile.write(usersJson());

        FileWriter groupFile = new FileWriter(new File(groupPath));
        groupFile.write(groupsJson());
    }

    // Get the JSON representation of the user database
    private String usersJson() throws ParseException
    {
        JSONArray userOut = new JSONArray();

        for(User user : this.users.values())
        {
            JSONParser parser = new JSONParser();
            userOut.add(parser.parse(user.toString()));
        }

        return userOut.toJSONString();
    }

    // Get the JSON representation of the group database
    private String groupsJson() throws ParseException
    {
        JSONArray groupOut = new JSONArray();

        for(Group group : this.groups.values())
        {
            JSONParser parser = new JSONParser();
            groupOut.add(parser.parse(group.toString()));
        }

        return groupOut.toJSONString();
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

    // Removes the given group
    public boolean removeGroup(String groupName)
    {
        return null == this.groups.remove(groupName);
    }

    // Checks if user exists
    public boolean userExists(String userName)
    {
        return this.users.get(userName) == null;
    }

    // Check if group exists
    public boolean groupExists(String groupName)
    {
        return this.users.get(groupName) == null;
    }

    // Checks if user in group
    public boolean userInGroup(String userName, String groupName)
    {
        Group g = this.groups.get(groupName);

        for(int i = 0; i < this.users.size(); i++)
        {
            if(g.users.get(i).equals(userName))
                return true;
        }
        return false;
    }

    // Create new user with the given name and password
    public void createUser(String name, String password)
    {
        this.users.put(name, new User(name, password));
    }

    // Delete given user
    public boolean deleteUser(String userName)
    {
        return null == this.users.remove(userName);
    }

    // Add a given user to the given group
    public boolean addUserToGroup(String userName, String groupName)
    {
        if(!groupExists(groupName))
            return false;

        if(!userExists(userName))
            return false;

        if(userInGroup(userName, groupName))
            return true;

        this.groups.get(groupName).users.add(userName);
        return true;
    }

    // Remove a given user from the given group
    public boolean removeUserFromGroup(String userName, String groupName)
    {
        if(!groupExists(groupName))
            return false;

        if(!userExists(userName))
            return false;

        Group g = this.groups.get(groupName);

        for(int i = 0; i < g.users.size(); i++)
        {
            if(g.users.get(i).equals(userName))
            {
                g.users.remove(i);
                return true;
            }
        }

        return false;
    }

    // Change the owner of a group to the given user. User must already be a member of the group.
    public boolean changeOwner(String userName, String groupName)
    {
        if(!groupExists(groupName))
            return false;
        if(!userExists(userName))
            return false;
        if(!userInGroup(userName, groupName))
            return false;

        this.groups.get(groupName).owner = userName;
        return true;
    }

    // Return the given group
    public Group getGroup(String groupName)
    {
        return this.groups.get(groupName);
    }


    // Returns a list of Group objects. If there are no groups, returns null.
    public ArrayList<Group> getGroups()
    {
        return (ArrayList<Group>) this.groups.values();
    }

    // Returns the given user
    public User getUser(String userName)
    {
        return this.users.get(userName);
    }

    // Returns a list of User objects. If there are no users, returns null.
    public ArrayList<String> getUsers(String groupName)
    {
        if(!groupExists(groupName))
            return null;

        return this.groups.get(groupName).users;
    }
}

class User
{
    String name;
    String password;

    public User(String name, String password)
    {
        this.name = name; // user name, must be unique
        this.password = password; //store SHA-1
    }

    public User(JSONObject userJson)
    {
        this.name = userJson.get("name").toString();
        this.password = userJson.get("password").toString();
    }

    public String toString()
    {
        JSONObject obj = new JSONObject();
        obj.put("name", this.name);
        obj.put("password", this.password);
        return obj.toJSONString();
    }
}

class Group
{
    String name; // group name, must be unqiue
    String owner; // user name that is the owner of the groups
    ArrayList<String> users; // list of users that have access to the group

    public Group(String groupName, String ownerName, ArrayList<String> listOfUsers)
    {
        name = groupName;
        owner = ownerName;
        users = listOfUsers;
    }

    public Group(JSONObject json)
    {
        this.name = (String) json.get("name"));
        this.owner = (String) json.get("owner"));
        for(Object use: (JSONArray) json.get("users"))
        {
            this.users.add((String) use);
        }
    }

    public String toString()
    {
        JSONObject obj = new JSONObject();
        obj.put("name", this.name);
        obj.put("owner", this.name);
        obj.put("users", this.users);

        return obj.toJSONString();
    }
}
