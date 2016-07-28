package cluedo.utility;

import java.util.Iterator;
import java.util.List;

/**
 * Class modified from Stack Overflow: https://stackoverflow.com/questions/20343265/looping-data-structure-in-java
 * Iterator that loops around in cycles so that it will never end
 * @param <E>
 */
public class Turn<E> implements Iterator<E>, Cloneable
{
    private final List<E> list;
    private int pos;

    public Turn(List<E> list)
    {
    	ensureNotNull(list);
    	ensureNotEmpty(list); // A turn must have at least one player.
    	ensureNotContainNullItem(list); // Turns may not contain null players.
    	
        this.list = list;
        pos = 0;
    }
    public Turn(List<E> list, int pos)
    {
    	ensureNotNull(list);
    	ensureNotEmpty(list); // A turn must have at least one player.
    	ensureNotContainNullItem(list); // Turns may not contain null players.
    	
        this.list = list;
        if(pos < 0)
        {
        	throw new IllegalArgumentException("Starting position of iterator must be zero or greater");
        }
            this.pos = pos;
    }

	public boolean hasNext()
    {
    	return list.size() >= 1;
    }

    public E next()
    {
    	if (!hasNext())
    	{
    		throw new IllegalStateException("No next item");
    	}
        E nextItem = list.get(pos);
        pos = (pos + 1) % list.size();
        return nextItem;
    }
   
    public void remove()
    {
         throw new RuntimeException("Cannot remove items from iterator");
    }
    

    // These methods throw illegal argument exceptions if their respective conditions aren't met.
    // The order of calls are important, as (e.g. ensureNotEmpty(list) assumes that the list is non null).
    private void ensureNotNull(List<E> list)
	{
		if (list == null)
		{
			throw new IllegalArgumentException("List must be non null");
		}
	}
    
    private void ensureNotEmpty(List<E> list)
	{
		if (list.size() <= 0)
		{
			throw new IllegalArgumentException("List must be non null, with at least one item");
		}
	}
    
    private void ensureNotContainNullItem(List<E> list)
	{
    	for (E item : list) 
    	{
    		if (item == null)
    		{
    			throw new IllegalArgumentException("List may not contain null items");
    		}
    	}
	}

	public int getPos() 
	{
		return pos;
	}
	public List<E> getList() 
	{
		return list;
	}
}
