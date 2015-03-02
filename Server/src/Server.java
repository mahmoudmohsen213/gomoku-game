import com.gomoku.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

@SuppressWarnings("unused")
public class Server extends Thread {
	public boolean serverStatus;
	private ServerSocket serverListner;
	private Hashtable<Integer, Client> clients;
	private boolean idFound;
	
	private UIFrame1 uiframe;
	
	public Server(Hashtable<Integer, Client> c){
		clients = c;
		try { serverListner = new ServerSocket(49999); serverStatus = true; }
		catch (Exception e) { e.printStackTrace(); serverStatus = false; }
		
		this.start();
	}

	@Override
	public void run(){
		while(true){
			try { Listen(); }
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public void SetUIFrameRef(UIFrame1 _uiframe){
		uiframe = _uiframe;
	}
	
	public void ShutDown(){
		try { serverListner.close(); serverStatus = false; }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private void Listen() throws Exception{
		Socket newSocket = serverListner.accept();
		ObjectOutputStream outputStream = new ObjectOutputStream(newSocket.getOutputStream());
		ObjectInputStream inputStream = new ObjectInputStream(newSocket.getInputStream());
		idFound = false;
		int id = GenerateID(2147483647, 1073741823);
		outputStream.writeChar('i');
		outputStream.writeInt(id);
		outputStream.flush();
		char flag = inputStream.readChar();
		Player newPlayer = (Player)inputStream.readObject();
		
		Client newClient = new Client(newSocket, newPlayer, clients, outputStream, inputStream, uiframe);
		clients.put(id, newClient);
		
		uiframe.print("connection with " + id);
	}
	
	private int GenerateID(int id, int half){
		if(clients.get(id) == null){
			idFound = true;
			return id;
		}
		
		if(idFound) return -1;
		if(half == 0) return -1;
		
		int goodID = GenerateID(id-half,(half/2));
		if(goodID != -1 ) return goodID;
		goodID = GenerateID(id+half,(half/2));
		return goodID;
	}
}
