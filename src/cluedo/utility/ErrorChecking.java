package cluedo.utility;

import java.util.Collection;

public class ErrorChecking
{
	public static void ensureNonEmpty(Collection<?>... collection)
	{
		for (Collection<?> c : collection)
		{
			if (c == null || c.isEmpty())
			{
				throw new IllegalArgumentException("Collection may not be null or empty");
			}
		}
	}
}
