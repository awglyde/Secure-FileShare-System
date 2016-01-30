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

    public boolean removeGroup(String groupName)
    {
        // TODO
        return false;
    }

    public void addUser()
    {
        // TODO
    }

    public void removeUser()
    {
        // TODO
    }

    public void createUser()
    {
        // TODO
    }

    public void deleteUser()
    {
        // TODO
    }

    public void changeOwner()
    {
        // TODO
    }

    public Group getGroup()
    {
        // TODO
        return null;
    }

    /*
      Returns a list of Group objects. If there are no groups, returns null.
    */
    public ArrayList<Group> getGroups()
    {
        //TODO
        return null;
    }

    public User getUser()
    {
        //TODO
        return null;
    }

    public ArrayList<String> getUsers()
    {
        //TODO
        return null;
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
