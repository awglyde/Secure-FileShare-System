/* T
 * his this.list represents the files on the server */

public class FileList implements java.io.Serializable
{

    /*Serializable so it can be stored in a file for persistence */
    private static final long serialVersionUID = -8911161283900260136L;
    private ArrayList<ShareFile> list;

    public FileList()
    {
        this.list = new ArrayList<ShareFile>();
    }

    public synchronized void addFile(String owner, String group, String path)
    {
        ShareFile newFile = new ShareFile(owner, group, path);
        this.list.add(newFile);
    }

    public synchronized void removeFile(String path)
    {
        for (int i = 0; i < this.list.size(); i++)
        {
            if (this.list.get(i).getPath().compareTo(path) == 0)
            {
                this.list.remove(i);
            }
        }
    }

    public synchronized boolean checkFile(String path)
    {
        for (int i = 0; i < this.list.size(); i++)
        {
            if (this.list.get(i).getPath().compareTo(path) == 0)
            {
                return true;
            }
        }
        return false;
    }

    public synchronized ArrayList<ShareFile> getFiles()
    {
        Collections.sort(this.list);
        return this.list;
    }

    public synchronized ShareFile getFile(String path)
    {
        for (int i = 0; i < this.list.size(); i++)
        {
            if (this.list.get(i).getPath().compareTo(path) == 0)
            {
                return this.list.get(i);
            }
        }
        return null;
    }
}	
