package com.gomoku;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class InvitationHandler extends Thread {
	
	private ServerSocket invitationListener;
    private Program runningProgram;
    public boolean blockInvitations; 
    
    public InvitationHandler(Program p) {
    	blockInvitations = false;
    	try {
			invitationListener = new ServerSocket(44444);
		} catch (Exception e) { e.printStackTrace(); }
    	runningProgram = p;
    	this.start();
    }
    
    @Override
    public void run(){
    	while(true) AcceptInvitation();
    }
    
    public void AcceptInvitation(){
    	Socket peerConnection = null;
    	ObjectOutputStream outLink = null;
    	ObjectInputStream inLink = null;
    	try {
			peerConnection = invitationListener.accept();
    		outLink = new ObjectOutputStream(peerConnection.getOutputStream());
    		inLink = new ObjectInputStream(peerConnection.getInputStream());
	    	if(blockInvitations) {
				outLink.writeChar('c');
				outLink.flush();
				outLink.close();
				peerConnection.close();
				return;
	    	}
	    	else{
	    		outLink.writeChar('a');
	    		outLink.flush();
	    	}
	    } catch (Exception e) { e.printStackTrace(); return; }
    	
    	PeerDataLink peerLink = new PeerDataLink(peerConnection, outLink, inLink);
    	runningProgram.StartMatch(peerLink);
    }
}