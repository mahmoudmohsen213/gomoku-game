import java.util.Hashtable;

@SuppressWarnings("unused")
public class Program {

	private Server server;
	private Hashtable<Integer,Client> clients;
	private Cleaner cleaner;
	
	public static void main(String[] args)
	{
		Program p = new Program();
	}
	
	Program()
	{
		clients = new Hashtable<Integer,Client>();
		server = new Server(clients);
		cleaner = new Cleaner(clients);
		
		UIFrame1 f1 = new UIFrame1(this,server.serverStatus);
		f1.setVisible(true);
		server.SetUIFrameRef(f1);
	}

	@SuppressWarnings("deprecation")
	public void closeProgram()
	{
		server.stop();
		server.ShutDown();
		
		cleaner = null;
		server = null;
		clients = null;
	}
}
