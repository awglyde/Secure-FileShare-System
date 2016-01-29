import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Database
{
    private String usersDB;
    private String groupDB;

    public Database(String usersPath, String groupPath)
    {
        this.usersDB = usersPath;
        this.groupDB = groupPath;
    }

    public HashMap<String, User> UserDB() throws IOException, ParseException
    {
        HashMap<String, User> userMap = new HashMap<>();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("db/users.json"));
        JSONArray users = (JSONArray) jsonObject.get("users");

        for(Object user : users)
        {
            JSONObject jo = (JSONObject) user;
            String name = jo.get("name").toString();
            String password = jo.get("password").toString()
            userMap.put(name, new User(name, password));

        }

        return userMap;
    }

    public HashMap<String, Group> GroupDB()
    {

        return null;
    }


    public void addGroup()
    {

    }

    public void removeGroup()
    {

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

    public ArrayList<Group> getGroups()
    {
        return null;
    }

    public User getUser()
    {
        return null;
    }

    public ArrayList<String> getUsers()
    {
        return null;
    }

}

class User
{
    String username;
    String password;

    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

}

class Group
{

}