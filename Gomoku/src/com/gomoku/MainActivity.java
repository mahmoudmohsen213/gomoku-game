package com.gomoku;

import java.io.DataOutputStream;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	Program runningProgram;
	
	// UserDataView components
	private Button okButton;
	private TextView userNameTextField;
	
	// MainMenu components
	private Button newGameButton;
	private Button exitButton;
	public TextView mainMenuMsgTextView;
	
	// PlayersBrowser components
	private AtomicInteger remotePlayerID;
	private Vector<Player> playersList;
	private ServerDataLink serverLink;
	private Button refreshBtn;
	private Button backBtn;
	private Button playerBtns[];
	
	// GameBoard Components
	private TextView localPlayerInfoTextView;
	private TextView remotePlayerInfoTextView;
	private Button quitBtn;
	private TextView boardTextView[][];
	private GameEngine gameEngine;
	
	// InvitationDialog Components
	private Button yesBtn;
	private Button noBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		runningProgram = new Program(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void SetServerLink(ServerDataLink s){
		serverLink = s;
	}
	
	public void SetRemotePlayerID(AtomicInteger r){
		remotePlayerID = r;
	}
	
	public void ChangeView(int flag){
		switch (flag) {
		case 0:
			Process.killProcess(Process.myPid());
			break;
		case 1:
			StartUserDataView();
			break;
		case 2:
			StartMainMenu();
			break;
		case 3:
			StartPlayersBrowser();
			break;
		case 4:
			StartGameBoard(true);
			break;
		case 5:
			StartInvitationDialog();
		}
	}
	
	private void StartUserDataView(){
		setContentView(R.layout.activity_user_data_view);
		// Initializing UserDataView components
		okButton = (Button)findViewById(R.id.okBtn);
		okButton.setOnClickListener(okButtonClick);
		userNameTextField = (TextView)findViewById(R.id.userNameTextField);
	}
	
	private void StartMainMenu(){
		setContentView(R.layout.activity_main_menu);
		// Initializing MainMenu components
		newGameButton = (Button)findViewById(R.id.newGameButton);
		newGameButton.setOnClickListener(newGameButtonClick);
		
		exitButton = (Button)findViewById(R.id.exitButton);
		exitButton.setOnClickListener(exitButtonClick);
		
		mainMenuMsgTextView = (TextView)findViewById(R.id.connectionStatusText);
		mainMenuMsgTextView.setText("");
	}
	
	private void StartPlayersBrowser(){
		setContentView(R.layout.activity_players_browser);
		// Initializing PlayersBrowser components
		playersList = null;
		refreshBtn = (Button)findViewById(R.id.refreshButton);
		backBtn = (Button)findViewById(R.id.backButton);
		
		playerBtns = new Button[6];
		playerBtns[0] = (Button)findViewById(R.id.playerButton1);
		playerBtns[1] = (Button)findViewById(R.id.playerButton2);
		playerBtns[2] = (Button)findViewById(R.id.playerButton3);
		playerBtns[3] = (Button)findViewById(R.id.playerButton4);
		playerBtns[4] = (Button)findViewById(R.id.playerButton5);
		playerBtns[5] = (Button)findViewById(R.id.playerButton6);
		
		refreshBtn.setOnClickListener(refreshBtnClick);
		backBtn.setOnClickListener(backBtnClick);
		
		for(int i=0;i<6;i++) {
			playerBtns[i].setVisibility(Button.INVISIBLE);
			playerBtns[i].setText("");
			playerBtns[i].setOnClickListener(playerBtnsClick);
		}
	}
	
	private void StartGameBoard(boolean flag){
		serverLink.UpdateStatus(1);
		
		RelativeLayout gameBoardLayout = new RelativeLayout(this);
		int backgroundColor = Color.rgb(0, 0, 102);
		int frontColor = Color.rgb(102, 178, 255);

		@SuppressWarnings("deprecation")
		int layoutWidth = getWindowManager().getDefaultDisplay().getWidth();
		int cellDimention = (int)((layoutWidth-15)/15);
		
		// Initializing GameBoard components
		localPlayerInfoTextView = new TextView(this);
		localPlayerInfoTextView.setX(10);
		localPlayerInfoTextView.setY(10);
		localPlayerInfoTextView.setBackgroundColor(frontColor);
		gameBoardLayout.addView(localPlayerInfoTextView, layoutWidth-20, 75);
		
		
		remotePlayerInfoTextView = new TextView(this);
		remotePlayerInfoTextView.setX(10);
		remotePlayerInfoTextView.setY(95);
		remotePlayerInfoTextView.setBackgroundColor(frontColor);
		gameBoardLayout.addView(remotePlayerInfoTextView, layoutWidth-20, 75);
		
		quitBtn = new Button(this);
		quitBtn.setX(10);
		quitBtn.setY((cellDimention*15)+215);
		quitBtn.setText("Quit");
		quitBtn.setOnClickListener(quitBtnClick);
		gameBoardLayout.addView(quitBtn, layoutWidth - 20, 100);
		
		
		boardTextView = new TextView[15][15];
		
		for(int i=0;i<15;i++){
			for(int j=0;j<15;j++){
				boardTextView[i][j] = new TextView(this);
				boardTextView[i][j].setId((i*100)+j);
				boardTextView[i][j].setText("");
				boardTextView[i][j].setX((cellDimention*i)+10);
				boardTextView[i][j].setY((cellDimention*j)+185);
				boardTextView[i][j].setBackgroundColor(backgroundColor);
				boardTextView[i][j].setTextColor(frontColor);
				boardTextView[i][j].setGravity(Gravity.CENTER);
				boardTextView[i][j].setOnClickListener(cellClick);
				gameBoardLayout.addView(boardTextView[i][j], cellDimention-5, cellDimention-5);
			}
		}
		
		setContentView(gameBoardLayout);
		ActivateGameBoard(false);
		
		if(flag) gameEngine = new GameEngine(this, true);
		
		gameEngine.SetBoardTextView(boardTextView);
	}
	
	private void StartInvitationDialog(){
		setContentView(R.layout.activity_invitation_dialog);
		
		yesBtn = (Button)findViewById(R.id.yesBtn);
		yesBtn.setOnClickListener(yesBtnClick);
		
		noBtn = (Button)findViewById(R.id.noBtn);
		noBtn.setOnClickListener(noBtnClick);
		
		gameEngine = new GameEngine(this, false);
	}
	
	// UserDataView Listener
	
	View.OnClickListener okButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(userNameTextField.getText().length() == 0) return;
			try{
				DataOutputStream outFile = new DataOutputStream(openFileOutput("player.data", Context.MODE_PRIVATE));
				outFile.writeInt(userNameTextField.getText().length());
				outFile.writeChars(userNameTextField.getText().toString());
				outFile.writeInt(0);
				outFile.writeInt(0);
				outFile.close();
				int flag = runningProgram.LoadPlayerData();
				if(flag == 1) runningProgram.CheckProgramState(2);
				else runningProgram.CheckProgramState(0);
			}
			catch(Exception e) { e.printStackTrace(); }
		}
	};
	
	// MainMenu Listeners
	View.OnClickListener newGameButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			runningProgram.CheckProgramState(3);
		}
	};
	
	View.OnClickListener exitButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			runningProgram.CheckProgramState(0);
		}
	};
	
	
	// PlayersBrowser functions
	
	private void DisplayPlayers(){
		for(int i=0;i<6;i++) playerBtns[i].setVisibility(Button.INVISIBLE);
		
		playersList = serverLink.GetPlayersList();
		if(playersList == null) return;
		for(int i=0;i<playersList.size();i++){
			String temp = "Name: " + playersList.get(i).GetName() + "   ";
			temp += "wins: " + playersList.get(i).GetWins() + "   ";
			temp += "loses: " + playersList.get(i).GetLoses();
			playerBtns[i].setText(temp);
			playerBtns[i].setVisibility(Button.VISIBLE);
		}
	}
	
	View.OnClickListener refreshBtnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if(serverLink.GetConnectionStatus()) DisplayPlayers();
			else runningProgram.CheckProgramState(2);
		}
	};
	
	View.OnClickListener backBtnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			runningProgram.CheckProgramState(2);
		}
	};
	
	View.OnClickListener playerBtnsClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			for(int i=0;i<6;i++){
				if(v.getId() == playerBtns[i].getId()){
					remotePlayerID.set(playersList.get(i).GetId());
					runningProgram.CheckProgramState(4);
				}
			}
		}
	};
	
	// GameBoard functions
	
	public void PassPeerConnection(PeerDataLink pc){
		gameEngine.SetPeerLink(pc);
	}
	
	public void PassLocalPlayer(Player localPlayer){
		gameEngine.SetLocalPlayer(localPlayer);
	}
	
	public void ActivateGameBoard(boolean flag){
		for(int i=0;i<15;i++){
			for(int j=0;j<15;j++){
				boardTextView[i][j].setEnabled(flag);
				boardTextView[i][j].setClickable(flag);
			}
		}
		
		quitBtn.setEnabled(flag);
	}
	
	public void DisplayPlayerInfo(Player localPlayer, Player remotePlayer){
		String temp = "Name: " + localPlayer.GetName() + "   ";
		temp += "wins: " + localPlayer.GetWins() + "   ";
		temp += "loses: " + localPlayer.GetLoses();
		localPlayerInfoTextView.setText(temp);
		
		temp = "Name: " + remotePlayer.GetName() + "   ";
		temp += "wins: " + remotePlayer.GetWins() + "   ";
		temp += "loses: " + remotePlayer.GetLoses();
		remotePlayerInfoTextView.setText(temp);
		
		gameEngine.PassLocalInfo(localPlayer);
		gameEngine.WaitInvitationConfirmation();
	}
	
	public void DisplayPlayerInfo(Player localPlayer){
		String temp = "Name: " + localPlayer.GetName() + "   ";
		temp += "wins: " + localPlayer.GetWins() + "   ";
		temp += "loses: " + localPlayer.GetLoses();
		localPlayerInfoTextView.setText(temp);
		
		Player remotePlayer = gameEngine.GetRemoteInfo();
		temp = "Name: " + remotePlayer.GetName() + "   ";
		temp += "wins: " + remotePlayer.GetWins() + "   ";
		temp += "loses: " + remotePlayer.GetLoses();
		remotePlayerInfoTextView.setText(temp);
	}
	
	public void EndGame(int flag){
		gameEngine = null;
		serverLink.UpdateStatus(0);
		runningProgram.CheckProgramState(2);
		
		if(flag == 0) mainMenuMsgTextView.setText("The openent has quit the game, you win.");
		else if(flag == 1) mainMenuMsgTextView.setText("The openent has declined the invitation.");
		else if(flag == 2) mainMenuMsgTextView.setText("you won.");
		else if(flag == 3) mainMenuMsgTextView.setText("you lose.");
	}
	
	View.OnClickListener quitBtnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			gameEngine.EndGame(4);
			serverLink.UpdateStatus(0);
			runningProgram.CheckProgramState(2);
			mainMenuMsgTextView.setText("you quit the game, you lose.");
		}
	};
	
	View.OnClickListener cellClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			for(int i=0;i<15;i++){
				for(int j=0;j<15;j++){
					if(v.getId() == boardTextView[i][j].getId()){
						gameEngine.MakeMove(i, j);
					}
				}
			}
		}
	};
	
	// InvitationDialog listeners
	
	View.OnClickListener yesBtnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			StartGameBoard(false);
			DisplayPlayerInfo(gameEngine.GetLocalPlayer());
			gameEngine.SendInvitationConfirmation(1);
			gameEngine.ReceiveInitialMove();
		}
	};
	
	View.OnClickListener noBtnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			gameEngine.SendInvitationConfirmation(0);
			serverLink.UpdateStatus(0);
			runningProgram.CheckProgramState(2);
		}
	};
}
