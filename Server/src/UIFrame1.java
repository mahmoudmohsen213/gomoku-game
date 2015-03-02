import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


@SuppressWarnings("serial")
public class UIFrame1 extends JFrame implements ActionListener {
	private Program p;
	
	private JLabel serverStatusLabel;
	private JTextArea serverConsole;
	private JButton closeBtn;
	private JButton clearBtn;
	
	
	public UIFrame1(Program _p, boolean serverStatus){
		p = _p;
		
		getContentPane().setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(450, 250, 500, 300);
		
		serverStatusLabel = new JLabel();
		if(serverStatus) serverStatusLabel.setText("server online");
		else serverStatusLabel.setText("server offline");
		serverStatusLabel.setBounds(10, 11, 164, 14);
		
		closeBtn = new JButton("Server Shut Down");
		closeBtn.setBounds(320, 238, 164, 23);
		closeBtn.addActionListener(this);
		
		clearBtn = new JButton("Clear");
		clearBtn.setBounds(10, 238, 89, 23);
		clearBtn.addActionListener(this);
		
		serverConsole = new JTextArea();
		serverConsole.setFont(new Font("Courier New", Font.PLAIN, 14));
		serverConsole.setForeground(Color.GREEN);
		serverConsole.setBackground(Color.BLACK);
		serverConsole.setEditable(false);
		
		JScrollPane sPane = new JScrollPane(serverConsole);
		sPane.setBounds(10, 36, 474, 191);
		
		getContentPane().add(serverStatusLabel);
		getContentPane().add(closeBtn);
		getContentPane().add(clearBtn);
		getContentPane().add(sPane);
		
		setResizable(false);
	}
	
	public void print(String s){
		serverConsole.append(s + '\n');
		serverConsole.setCaretPosition(serverConsole.getText().length());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == closeBtn) closeBtnOnClick();
		else clearBtnOnClick();
	}
	
	private void closeBtnOnClick(){
		p.closeProgram();
		serverStatusLabel.setText("server offline");
	}
	
	private void clearBtnOnClick(){
		serverConsole.setText("");
	}
	
}
