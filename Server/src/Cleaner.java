import java.util.Enumeration;
import java.util.Hashtable;


public class Cleaner extends Thread {
	Hashtable<Integer,Client> clients;

	Cleaner(Hashtable<Integer,Client> c)
	{
		clients = c;
		this.start();
	}

	@Override
	public void run()
	{

		Enumeration<Integer> IDs=clients.keys();
		while (true)
		{
			if (!IDs.hasMoreElements()) { IDs=clients.keys(); continue; }

			int currentID = (int) IDs.nextElement();
			if (!clients.get(currentID).getConnectionStatus())
				clients.remove(currentID);
		}
	}
}
