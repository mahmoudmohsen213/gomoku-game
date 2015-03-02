package com.gomoku;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;


public class PeerDataLink extends Thread{
	public static final int WAIT = 0;
	public static final int RECEIVE_INVITATION_CONFIRMATION = 1;
	public static final int SEND_MOVE = 2;
	public static final int RECEIVE_MOVE = 3;
	public static final int END_CONNECTION = 4;
	
	private GameEngine gameEngine;
	
	private Socket peerConnection;
	private ObjectInputStream inLink;
	private ObjectOutputStream outLink;
	public boolean connectionStatus;
	
	public AtomicInteger xCoordinate;
	public AtomicInteger yCoordinate;
	public AtomicInteger functionFlag;
	public AtomicInteger invitationConfirmation;

	public PeerDataLink(InetAddress remotePlayerIP){
		try{
			peerConnection = new Socket(remotePlayerIP, 44444);
			outLink = new ObjectOutputStream(peerConnection.getOutputStream());
			inLink = new ObjectInputStream(peerConnection.getInputStream());
			connectionStatus = true;
			
			char flag = inLink.readChar();
			if(flag == 'c'){
				connectionStatus = false;
				inLink.close();
				outLink.close();
				peerConnection.close();
				connectionStatus = false;
				return;
			}
			
			xCoordinate = new AtomicInteger(); 
			yCoordinate = new AtomicInteger();
			functionFlag = new AtomicInteger();
			invitationConfirmation = new AtomicInteger();
		} 
		catch(Exception e) { connectionStatus = false; e.printStackTrace(); }
	}
	
	public PeerDataLink(Socket pc, ObjectOutputStream _outLink, ObjectInputStream _inLink){
		try{
			peerConnection = pc;
			outLink = _outLink;
			inLink = _inLink;
			connectionStatus = true;
			xCoordinate = new AtomicInteger();
			yCoordinate = new AtomicInteger();
			functionFlag = new AtomicInteger();
			invitationConfirmation = new AtomicInteger();
		}
		catch(Exception e) { connectionStatus = false; e.printStackTrace(); }
	}
	
	
	public void SetGameEngine(GameEngine ge){
		gameEngine = ge;
	}
	
	public void SendLocalInfo(Player localPlayer){
		try {
			outLink.writeObject(localPlayer);
			outLink.flush();
		} catch (Exception e) { connectionStatus = false; e.printStackTrace(); }
	}
	
	public Player ReceiveRemoteInfo(){
		Player remotePlayer = null;
		try {
			remotePlayer = (Player) inLink.readObject();
		} catch (Exception e) {  connectionStatus = false; e.printStackTrace(); return null; }
		
		return remotePlayer;
	}
	
	public void SendInvitationConfirmation(int flag){
		try {
			outLink.writeInt(flag);
			outLink.flush();
		} catch (Exception e) { connectionStatus = false; e.printStackTrace(); }
	}
	
	public void ReceiveInvitationConfirmation(){
		functionFlag.set(WAIT);
		
		int flag = 0;
		try {
			flag = inLink.readInt();
		} catch (Exception e) { connectionStatus = false; e.printStackTrace(); invitationConfirmation.set(0); }
		
		invitationConfirmation.set(flag);
		gameEngine.ConfirmInvitaion();
	}
	
	@Override
	public void run(){
		while(connectionStatus){
			if(functionFlag.get() == 0) continue;
			else if(functionFlag.get() == 1) ReceiveInvitationConfirmation();
			else if(functionFlag.get() == 2) SendMove();
			else if(functionFlag.get() == 3) ReceiveMove();
			else if(functionFlag.get() == 4) EndConnection();
		}
		
		if(functionFlag.get() != 4) gameEngine.EndGame(0);
	}
	
	public boolean EndConnection(){
		if(!connectionStatus) return true;
		
		try {
			outLink.writeChar('t');
			outLink.flush();
			inLink.close();
			outLink.close();
			peerConnection.close();
			
			peerConnection = null;
			inLink = null;
			outLink = null;
			
			connectionStatus = false;
		} catch(Exception e) { e.printStackTrace(); return false; }
		
		return true;
	}
	
	public boolean SendMove(){
		functionFlag.set(WAIT);
		try {
			outLink.writeChar('u');
			outLink.writeInt(xCoordinate.get());
			outLink.writeInt(yCoordinate.get());
			outLink.flush();
		} catch (Exception e) { connectionStatus = false; e.printStackTrace(); return false; }
		
		return ReceiveMove();
	}
	
	public boolean ReceiveMove(){
		functionFlag.set(WAIT);
		try {
			char temp = inLink.readChar();
			if(temp == 't'){
				inLink.close();
				outLink.close();
				peerConnection.close();
				
				peerConnection = null;
				inLink = null;
				outLink = null;
				
				connectionStatus = false;
				return true;
			}
			
			xCoordinate.set(inLink.readInt());
			yCoordinate.set(inLink.readInt());
			gameEngine.Notify();
		} catch (Exception e) { connectionStatus = false; e.printStackTrace(); return false; }
		
		return true;
	}
	
}

