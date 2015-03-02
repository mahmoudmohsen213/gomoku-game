import com.gomoku.*;

import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.*;

public class Client extends Thread {
	private Socket clientConnection;
	private Player player;
	private boolean connectionStatus;
	private Hashtable<Integer,Client> clients;
	private ObjectInputStream inLink;
	private ObjectOutputStream outLink;
	
	private UIFrame1 uiframe;
	
	public Client(Socket s, Player p, Hashtable<Integer,Client> c, ObjectOutputStream _outLink, ObjectInputStream _inLink, UIFrame1 _uiframe) throws IOException
	{
		clientConnection = s;
		outLink = _outLink;
		inLink = _inLink;
		clients = c;
		uiframe = _uiframe;
		player=p;
		connectionStatus = true;
		this.start();
	}

	public Player getPlayer()
	{
		return player;
	}

	public boolean getConnectionStatus()
	{
		return connectionStatus;
	}

	@Override
	public void run(){
		while(connectionStatus) {
			try{ ListenToRequests(); }
			catch (EOFException e) { connectionStatus = false; }
			catch (Exception e) { e.printStackTrace(); }
		}
	}

	private void ListenToRequests() throws Exception {

		char flag;
		flag = inLink.readChar();
		uiframe.print("ListenToRequest  " + flag + "  " + this.player.GetId());
		if (flag == 's')// start match
		{
			try{ StartMatch(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else if (flag == 'r')// request player list (send list of 6 players)
		{
			try{ SendPlayersList(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else if (flag == 't')// terminate connection
		{
			try{ TerminateConnection(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else if (flag == 'u')// invitation
		{
			try{ UpdateStatus(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else if (flag == 'n')
		{
			try{ SendPlayerInfo(); }
			catch (Exception e) { e.printStackTrace(); }
		}
	}

	private void StartMatch() throws Exception {
		uiframe.print("StartMatch  begin  " + this.player.GetId());
		int playerID = inLink.readInt();
		if (clients.containsKey(playerID) )
		{
			uiframe.print("StartMatch " + playerID + " found " + this.player.GetId());
			if((clients.get(playerID).connectionStatus)&&(!clients.get(playerID).player.GetStatus()))
			{
				// if player are not in game and online
				outLink.writeChar('m'); //player available ,, send IP
				outLink.writeObject( clients.get(playerID).clientConnection.getInetAddress() );
			}
			else outLink.writeChar('b'); // player busy (in match )
		}
		else { outLink.writeChar('f'); } // no player with that ID 
		
		outLink.flush();
		uiframe.print("StartMatch  end  " + this.player.GetId());
	}

	private void SendPlayersList() throws Exception {
		// sending player list
		uiframe.print("SendPlayersList  begin  " + this.player.GetId());
		outLink.writeChar('p');
		Vector<Player> p = new Vector<Player>();

		Enumeration<Integer> IDs= clients.keys();
		for (int i=0 ;  ((IDs.hasMoreElements()) && (i<6)) ; i++)
		{	
			Integer currentID = IDs.nextElement();
			if(currentID == this.player.GetId()) continue;
			p.add(clients.get(currentID).player);
		}
		
		outLink.writeObject(p);
		outLink.flush();
		uiframe.print("SendPlayersList  end  " + this.player.GetId());
	}

	private void TerminateConnection() throws Exception {
		connectionStatus = false ;
		clientConnection.close();
	}
	
	private void UpdateStatus() throws Exception {
		uiframe.print("UpdateStatus  begin  " + this.player.GetId());
		int temp=inLink.readInt();
		if(temp==0) //client not connected->2 clients in match
		{
			player.SetStatus(false);
		}
		else if (temp==1)// client is already connected
		{
			player.SetStatus(true);
		}
		uiframe.print("UpdateStatus  end  " + this.player.GetId());
	}
	
	private void SendPlayerInfo() throws Exception {
		uiframe.print("SendPlayerInfo  begin  " + this.player.GetId());
		int id = inLink.readInt();
		uiframe.print("SendPlayerInfo  " + id + "  " + this.player.GetId());
		Player p = clients.get(id).getPlayer();
		outLink.writeObject(p);
		outLink.flush();
		uiframe.print("SendPlayerInfo  end  " + this.player.GetId());
	}
}
