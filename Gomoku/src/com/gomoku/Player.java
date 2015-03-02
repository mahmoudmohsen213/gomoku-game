package com.gomoku;

import java.io.Serializable;


public class Player implements Serializable {
	private static final long serialVersionUID = -8522848963107939621L;
	
	private String name;
	private int id;
	private int wins;
	private int loses;
	private boolean inMatchStatus;
	
	public Player(String n, int i, int w, int l){
		name = n;
		id = i;
		wins = w;
		loses = l;
		inMatchStatus = false;
	}
	
	public void SetName(String n){
		name = n;
	}
	
	public String GetName(){
		return name;
	}
	
	
	public void SetId(int i){
		id = i;
	}
	
	public int GetId(){
		return id;
	}
	
	public void IncWins(){
		wins++;
	}
	
	public void IncLoses(){
		loses++;
	}
	
	public int GetWins(){
		return wins;
	}
	
	public int GetLoses(){
		return loses;
	}
	
	public void SetStatus(boolean s){
		inMatchStatus = s;
	}
	
	public boolean GetStatus(){
		return inMatchStatus;
	}
}
