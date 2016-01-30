/*
  To Test the Database with this class perform the following commands from the
  main directory of the project (you should see the folders "src" and "test"):

    >> javac -d "test" "src/Database.java"

  Then move to the "test" folder and compile this test program:

    >> javac testDatabase.java
*/
import java.util.ArrayList;
import java.util.Arrays;

public class testDatabase
{
    public static void main(String[] args)
    {
      Database db = new Database();
      boolean add = db.addGroup("Group1", "Owner1");
      System.out.println("Add Successful: " + add);

      ArrayList<Group> availableGroups = db.getGroups();
      printGroups(availableGroups);

      boolean remove = db.removeGroup("Group1");
      System.out.println("Remove Successful: " + remove);
      availableGroups = db.getGroups();
      printGroups(availableGroups);
    }

    public static void printGroups(ArrayList<Group> list)
    {
      if(list != null)
      {
        for(Group g: list)
        {
          System.out.println(g);
        }
      }
    }
}
