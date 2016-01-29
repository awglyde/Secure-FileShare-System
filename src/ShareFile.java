public class ShareFile implements java.io.Serializable, Comparable<ShareFile>
{

    /**
     *
     */
    private static final long serialVersionUID = -6699986336399821598L;
    private String group;
    private String path;
    private String owner;

    public ShareFile(String _owner, String _group, String _path)
    {
        this.group = _group;
        this.owner = _owner;
        this.path = _path;
    }

    public String getPath()
    {
        return this.path;
    }

    public String getOwner()
    {
        return this.owner;
    }

    public String getGroup()
    {
        return this.group;
    }

    public int compareTo(ShareFile rhs)
    {
        if(this.path.compareTo(rhs.getPath()) == 0) return 0;
        else if(this.path.compareTo(rhs.getPath()) < 0) return -1;
        else return 1;
    }


}	
