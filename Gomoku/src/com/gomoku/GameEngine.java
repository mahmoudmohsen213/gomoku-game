package com.gomoku;

import android.graphics.Color;
import android.widget.TextView;

public class GameEngine {
	private Player localPlayer;
	private TextView boardTextView[][];
	private char localSymbol;
	
	private PeerDataLink peerLink;
	private MainActivity mainActivity;
	
	// game logic data
	private char board[][];
	private boolean winFlag;
	
	public GameEngine(MainActivity ma, boolean localPlayerTrun){
		mainActivity = ma;
		
		board = new char[15][15];
		winFlag = false;
		
		if(localPlayerTrun) localSymbol = 'X';
		else localSymbol = 'O';
	}
	
	public void SetPeerLink(PeerDataLink pc){
		peerLink = pc;
		peerLink.SetGameEngine(this);
	}
	
	public void SetLocalPlayer(Player lp){
		localPlayer = lp;
	}
	
	public Player GetLocalPlayer(){
		return localPlayer;
	}
	
	public void PassLocalInfo(Player lp){
		localPlayer = lp;
		peerLink.SendLocalInfo(localPlayer);
	}
	
	public Player GetRemoteInfo(){
		return peerLink.ReceiveRemoteInfo();
	}
	
	public void SetBoardTextView(TextView boardTextViewGUI[][]){
		boardTextView = boardTextViewGUI;
	}
	
	public void SendInvitationConfirmation(int flag){
		peerLink.SendInvitationConfirmation(flag);
	}
	
	public void WaitInvitationConfirmation(){
		peerLink.functionFlag.set(PeerDataLink.RECEIVE_INVITATION_CONFIRMATION);
		peerLink.start();
	}
	
	public void ConfirmInvitaion(){
		mainActivity.runOnUiThread(new Runnable() {
	   	     @Override
	   	     public void run() {
	   	    	if(peerLink.invitationConfirmation.get() == 0) { mainActivity.EndGame(1); }
	   			else { mainActivity.ActivateGameBoard(true); }
	   	     }
		});
	}
	
	public void ReceiveInitialMove(){
		peerLink.functionFlag.set(PeerDataLink.RECEIVE_MOVE);
		peerLink.start();
	}
	
	public void MakeMove(int x, int y){
		if(boardTextView[x][y].getText() != "") return;
		
		mainActivity.ActivateGameBoard(false);
		boardTextView[x][y].setText(Character.toString(localSymbol));
		board[x][y] = localSymbol;
		
		peerLink.xCoordinate.set(x);
		peerLink.yCoordinate.set(y);
		peerLink.functionFlag.set(PeerDataLink.SEND_MOVE);
		
		winFlag = CheckWin(x,y,localSymbol);
	}
	
	public void Notify(){
		mainActivity.runOnUiThread(new Runnable() {
	   	     @Override
	   	     public void run() {
	   	    	int x = peerLink.xCoordinate.get();
	   			int y = peerLink.yCoordinate.get();
	   			
	   			boardTextView[x][y].setBackgroundColor(Color.RED);
	   			boolean flag = false;
	   			
	   			if(localSymbol == 'X'){
	   				boardTextView[x][y].setText(Character.toString('O'));
	   				board[x][y] = 'O';
	   				flag = CheckWin(x,y,'O'); // end game
	   			}
	   			else{
	   				boardTextView[x][y].setText(Character.toString('X'));
	   				board[x][y] = 'X';
	   				flag = CheckWin(x,y,'X'); // end game
	   			}
	   			
	   			if(flag) {
	   				peerLink.functionFlag.set(PeerDataLink.END_CONNECTION);
	   				EndGame(3);
	   			}
	   			else mainActivity.ActivateGameBoard(true);
	   	     }
		});
	}
	
	public void EndGame(int flag){
		if(flag == 0){
			// local player win
			localPlayer.IncWins();
			if(winFlag){
				mainActivity.runOnUiThread(new Runnable() {
			   	     @Override
			   	     public void run() { mainActivity.EndGame(2); }
				});
			}
			else{
				mainActivity.runOnUiThread(new Runnable() {
			   	     @Override
			   	     public void run() { mainActivity.EndGame(0); }
				});
			}
		}
		else if(flag == 3){
			// local player lose
			localPlayer.IncLoses();
			mainActivity.EndGame(3);
		}
		else if(flag == 4){
			// local player lose
			localPlayer.IncLoses();
			peerLink.functionFlag.set(PeerDataLink.END_CONNECTION);
		}
		
	}
	
	private boolean CheckWin(int x, int y, char symbol){
		boolean flag1 = false;
		boolean flag2 = false;
		int temp1 = 0;
		int temp2 = 0;
		int temp3 = 0;
		
		temp3 = Math.min(x,5);
		for(int i = temp3;i>=0;i--){
			temp1 = x - i;
			temp2 = y;
			flag2 = true;
			for(int j=0;j<5;j++){
				if(board[temp1+j][temp2] != symbol) { flag2 = false; break; }
			}
			
			if(flag2) { flag1 = true; break; }
		}
		
		if(flag1) return true;
		
		temp3 = Math.min(y,5);
		for(int i = temp3;i>=0;i--){
			temp1 = x;
			temp2 = y - i;
			flag2 = true;
			for(int j=0;j<5;j++){
				if(board[temp1][temp2+j] != symbol) { flag2 = false; break; }
			}
			
			if(flag2) { flag1 = true; break; }
		}
		
		if(flag1) return true;
		
		temp3 = Math.min(Math.min(x, y),5);
		
		for(int i = temp3;i>=0;i--){
			temp1 = x - i; 
			temp2 = y - i;
			flag2 = true;
			for(int j=0;j<5;j++){
				if(board[temp1+j][temp2+j] != symbol) { flag2 = false; break; }
			}
			
			if(flag2) { flag1 = true; break; }
		}
		
		if(flag1) return true;
		
		temp3 = Math.min(Math.min(14 - x, y),5);
		
		for(int i = temp3;i>=0;i--){
			temp1 = x + i; 
			temp2 = y - i;
			flag2 = true;
			for(int j=0;j<5;j++){
				if(board[temp1-j][temp2+j] != symbol) { flag2 = false; break; }
			}
			
			if(flag2) { flag1 = true; break; }
		}
		
		return flag1;
	}
}
