import java.util.ArrayList;


public class Envelope implements java.io.Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = -7726335089122193103L;
    private String msg;
    private ArrayList<Object> objContents = new ArrayList<Object>();

    public Envelope(String text)
    {
        this.msg = text;
    }

    public String getMessage()
    {
        return this.msg;
    }

    public ArrayList<Object> getObjContents()
    {
        return this.objContents;
    }

    public void addObject(Object object)
    {
        this.objContents.add(object);
    }
}
