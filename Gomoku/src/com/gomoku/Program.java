package com.gomoku;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;


public class Program {
	private MainActivity mainActivity;
	
	private ServerDataLink serverLink;
	private InvitationHandler invitationHandler;
	
	private Player localPlayer;
	private AtomicInteger remotePlayerID;
	
	public Program(MainActivity _mainActivity){
		mainActivity=_mainActivity;
		
		serverLink = new ServerDataLink();
		invitationHandler = new InvitationHandler(this);
		remotePlayerID = new AtomicInteger();
		
		int flag = LoadPlayerData();
		if(flag == 1){
			int id = serverLink.StartConnection(localPlayer);
			localPlayer.SetId(id);
		}
		
		mainActivity.SetServerLink(serverLink);
		mainActivity.SetRemotePlayerID(remotePlayerID);
		if(flag == -1) { CheckProgramState(1); return; }
		if(flag == -2) { CheckProgramState(0);  return; }
		CheckProgramState(2);
		
	}
	
	public void CheckProgramState(int programState){
		if(programState == 1) mainActivity.ChangeView(1);
		else if(programState == 2) DisplayMainMenu(); // navigate to main menu
		else if(programState == 3) DisplayBrowseMenu();
		else if(programState == 4) StartMatch(); // navigate to the game board and start a match
		else if(programState == 0) { // close the application
			
			serverLink.EndConnection();
			SavePlayerData();
			
			// clearing memory
			serverLink = null;
			//invitationHandler = null;
			localPlayer = null;
			remotePlayerID = null;
			
			mainActivity.ChangeView(0);
		}
	}

	private void DisplayMainMenu(){
		invitationHandler.blockInvitations = false;
		mainActivity.ChangeView(2);
		if(!serverLink.GetConnectionStatus())
			mainActivity.mainMenuMsgTextView.setText("unable to connect to server, press new game to try again");
	}
	
	private void DisplayBrowseMenu(){
		if(!serverLink.GetConnectionStatus()){ // check connection and try to connect if not connected
			int id = serverLink.StartConnection(localPlayer);
			if(!serverLink.GetConnectionStatus()) { // if failed again navigate to main menu
				DisplayMainMenu();
				return;
			}
			
			localPlayer.SetId(id); // navigate to players browser menu
		}
		mainActivity.ChangeView(3);
	}
	
	private void StartMatch(){
		InetAddress IP = serverLink.StartMatch(remotePlayerID.get());
		if(IP == null){
			CheckProgramState(2);
			mainActivity.mainMenuMsgTextView.setText("The player you invited is unavailabe.");
			return;
		}
		
		Player remotePlayer = serverLink.GetInfo(remotePlayerID.get());
		
		PeerDataLink peerLink = new PeerDataLink(IP);
		if(peerLink.connectionStatus == false) {
			CheckProgramState(2);
			mainActivity.mainMenuMsgTextView.setText("The player you invited is unavailabe.");
			return;
		}
		
		invitationHandler.blockInvitations = true;
		
		mainActivity.ChangeView(4);
		mainActivity.PassPeerConnection(peerLink);
		mainActivity.DisplayPlayerInfo(localPlayer, remotePlayer);
		
		// saving data after the end of the game
		SavePlayerData();
	}
	
	public void StartMatch(final PeerDataLink peerLink){
		invitationHandler.blockInvitations = true;
		mainActivity.runOnUiThread(new Runnable() {
	   	     @Override
	   	     public void run() {
	   	    	mainActivity.ChangeView(5);
	   			mainActivity.PassPeerConnection(peerLink);
	   			mainActivity.PassLocalPlayer(localPlayer);
	   	     }
		});
		SavePlayerData();
	}
	
	public void SavePlayerData(){
		try{
			DataOutputStream outFile = new DataOutputStream(mainActivity.openFileOutput("player.data", Context.MODE_PRIVATE));
			outFile.writeInt(localPlayer.GetName().length());
			outFile.writeChars(localPlayer.GetName());
			outFile.writeInt(localPlayer.GetWins());
			outFile.writeInt(localPlayer.GetLoses());
			outFile.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public int LoadPlayerData(){
		String name = ""; int wins = 0; int loses = 0;
		try{
			DataInputStream inFile = new DataInputStream(mainActivity.openFileInput("player.data"));
			int siz = inFile.readInt();
			name = ""; 
			for(int i=0;i<siz;i++) name += inFile.readChar();
			wins = inFile.readInt();
			loses = inFile.readInt();
			inFile.close();
		}
		catch(FileNotFoundException e) { e.printStackTrace(); return -1; }
		catch(EOFException e) { e.printStackTrace(); return -1; }
		catch(Exception e) { e.printStackTrace(); return -2; }
		
		localPlayer = new Player(name, 0, wins, loses);
		return 1;
	}
}
