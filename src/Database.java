import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Database
{
    private String usersDB;
    private String groupDB;

    public Database(String usersPath, String groupPath)
    {
        this.usersDB = usersPath;
        this.groupDB = groupPath;
    }

    public HashMap<String, User> UserDB() throws FileNotFoundException, ParseException
    {
        Scanner dbScanner = new Scanner(new File(this.usersDB));
        fileJson = JSONParser().parse(dbScanner.read());
        System.out.println(fileJson);

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

    public static void main(String args[])
    {
        UserDB()
    }
}

class User
{

}

class Group
{

}