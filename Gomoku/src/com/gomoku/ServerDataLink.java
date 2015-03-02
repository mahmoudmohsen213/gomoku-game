package com.gomoku;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;


public class ServerDataLink extends Thread{
	private Socket serverConnection;
	private ObjectInputStream inLink;
	private ObjectOutputStream outLink;
	private boolean connectionStatus;
	
	public ServerDataLink(){
		serverConnection = null;
		inLink = null;
		outLink = null;
		connectionStatus = false;
	}
	
	public boolean GetConnectionStatus(){
		return connectionStatus;
	}
	
	@SuppressWarnings("unused")
	public int StartConnection(Player p){
		int id = -1;
		try {
			serverConnection = new Socket(InetAddress.getByName("192.168.1.10"), 49999);
			outLink = new ObjectOutputStream(serverConnection.getOutputStream());
			inLink = new ObjectInputStream(serverConnection.getInputStream());
			
			char flag = inLink.readChar();
			id = inLink.readInt();
			p.SetId(id);
			outLink.writeChar('i');
			outLink.writeObject(p);
			outLink.flush();
			connectionStatus = true;
		} 
		catch (EOFException e) { connectionStatus = false; return -1; }
		catch (Exception e) { connectionStatus = false; e.printStackTrace(); return -1; }
		
		return id;
	}
	
	public boolean EndConnection(){
		if(!connectionStatus) return true;
		
		try {
			outLink.writeChar('t');
			outLink.flush();
			inLink.close();
			outLink.close();
			serverConnection.close();
			
			serverConnection = null;
			inLink = null;
			outLink = null;
			
			connectionStatus = false;
		} 
		catch (EOFException e) { connectionStatus = false; return true; }
		catch(Exception e) { e.printStackTrace(); return false; }
		
		return true;
	}
	
	
	@SuppressWarnings({ "unchecked", "unused" })
	public Vector<Player> GetPlayersList(){
		if(!connectionStatus) return null;
		
		Vector<Player> playersList = null;
		try{
			outLink.writeChar('r');
			outLink.flush();
			char flag = inLink.readChar();
			playersList = (Vector<Player>)inLink.readObject();
		}
		catch (EOFException e) { connectionStatus = false; return null; }
		catch (Exception e) { e.printStackTrace(); return null; }
		
		return playersList;
	}
	
	
	public Player GetInfo(int remotePlayerID){
		if(!connectionStatus) return null;
		
		Player p = null;
		try{
			outLink.writeChar('n');
			outLink.writeInt(remotePlayerID);
			outLink.flush();
			
			p = (Player)(inLink.readObject());
		} 
		catch (EOFException e) { connectionStatus = false; return null; }
		catch(Exception e) { e.printStackTrace(); return null; }
		return p;
	}
	
	public InetAddress StartMatch(int remotePlayerID){
		if(!connectionStatus) return null;
		
		InetAddress remotePlayerAddress = null;
		try{
			outLink.writeChar('s');
			outLink.writeInt(remotePlayerID);
			outLink.flush();
			
			char flag = inLink.readChar();
			if(flag == 'f') return null; // player not online
			else if(flag == 'b') return null; // player busy
			else if(flag == 'm') remotePlayerAddress = (InetAddress)inLink.readObject();
		}
		catch (EOFException e) { connectionStatus = false; return null; }
		catch (Exception e) { e.printStackTrace(); return null; }
		
		return remotePlayerAddress;
	}
	
	public void UpdateStatus(int flag){
		if(!connectionStatus) return;
		
		try{
			outLink.writeChar('u');
			outLink.writeInt(flag);
			outLink.flush();
		} catch (Exception e) { e.printStackTrace(); }
	}
}
