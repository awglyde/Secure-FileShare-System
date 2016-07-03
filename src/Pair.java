public class Pair<T, E>
{
    private final T firstObj;
    private final E secondObj;

    public Pair(T firstObj, E secondObj)
    {
        this.firstObj = firstObj;
        this.secondObj = secondObj;
    }

    public T getFirst()
    {
        return this.firstObj;
    }

    public E getSecond()
    {
        return this.secondObj;
    }
}
