package netproj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainFrame extends JFrame {

	private String name;
	private Socket socket;
	private String serverIp = "127.0.0.1";
	public String message;
	public String msg;
	JTextArea chatting;
	JTextField chat;
	JTextPane current_status;
	JTextArea System_message;
	JButton send;
	private JPanel contentPane;
	private JPanel contentPane2;

	String[] player = { "1", "2", "3", "4", "5" };
	JTextPane my_info;
	String job;
	String cha = "유령";
	String[] grp;
	JList<String> userlist;

	int phase=5;
	int[] xval = {35, 35, 35, 35, 35};// @@그림의 좌표값이다.
	int[] diedp = new int[5];
	int choice;
	JLabel img = null, img2 = null, img3 = null, img4 = null, img5 = null, voteimg = null, bullet = null, scan = null;
	JPanel imgp = null, imgp2 = null, imgp3 = null, imgp4 = null, imgp5 = null;
	JTextField name1 = null , name2 =null, name3=null, name4=null, name5=null;
	JButton votesend;
	ImageIcon icon, icon2,icon3,icon4,icon5,night,panel,back;

	JLabel daytime=null, nighttime=null,votetime=null;
	JPanel present = null;
	JLabel currentlabel = null;
	int flag;

	int mychanumber;
	boolean isalive = true;
	boolean isvoted = false;

	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
		frame.start();
	}

	/// 여기서부터
	public void start() {
		try {
			while(true)	//!!대화명 입력받는 부분  대화명이 꼭 필요하므로 입력없으면 반복문으로 받게 해놨다. 그리고 취소를 누른다면 클라가 꺼지게 함
			{
				name = JOptionPane.showInputDialog(null, "대화명을 입력하세요 : ", "Login", JOptionPane.QUESTION_MESSAGE);
				if(name==null)
					System.exit(0);
				else if (name.equals(""))
					continue;
				break;
			}
			socket = new Socket(serverIp, 8);
			JOptionPane.showMessageDialog(null, "서버와 연결되었습니다\n접속한 유저가 5명이면 게임이 시작됩니다");

			ClientReceiver clientReceiver = new ClientReceiver(socket);		//스레드로 서버로부터 정보를 받는 스레드, 정보를 전달하는 스레드를 만듦
			ClientSender clientSender = new ClientSender(socket);

			clientReceiver.start();
			clientSender.start();
		} catch (IOException e) {
		}
	}

	class ClientReceiver extends Thread {		//받는 스레드. 서버로부터 태그가 붙여서 온 스트링을 split 함수를 통해서 정보를 분할하고 각 태그에 맞게 정보를 이용함
		Socket socket;
		DataInputStream input;

		public ClientReceiver(Socket socket) {
			this.socket = socket;
			try {
				input = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
			}
		}

		@Override
		public void run() {
			while (input != null) {
				try {
					message = input.readUTF();
					//	System.out.println(message);

					grp = message.split("/");
					if (grp[0].equals("1")) // 1은 투표태그
					{
						chatting.append(grp[1] + "\n");
					} else if (grp[0].equals("9")) { // 9는 처음에 setinfo() 5명들어오면
						// 정보 할당
						setInfo();
					} 
					else if(grp[0].equals("11")){		//11은 캐릭터 밑에 있는 JTextField에 이름 할당
						if(grp[1].equals("0")) 
							name1.setText(grp[2]);
						else if(grp[1].equals("1"))
							name2.setText(grp[2]);
						else if(grp[1].equals("2")) 
							name3.setText(grp[2]);
						else if(grp[1].equals("3")) 
							name4.setText(grp[2]);
						else if(grp[1].equals("4")) 
							name5.setText(grp[2]);
					}
					else if (grp[0].equals("5"))// 낮 //5는 낮을 알려주는 태그
					{
						setImage(5);
						phase = 5;

					} else if (grp[0].equals("2"))// 투표 //2는 투표시간 태그
					{
						setImage(2);
						phase = 2;
						flag = 0;
						isvoted = false;
					} else if (grp[0].equals("4"))// 밤 //4는 밤 시간 태그
					{
						setImage(4);
						phase = 4;
						flag = 0;
						isvoted = false;
					} else if (grp[0].equals("6"))// 죽음 //6은 플레이어 죽일때 태그
					{
						// 만약 자기 번호랑 같으면 죽음모드
						int i = Integer.parseInt(grp[1]);
						if(i == 0)//@@죽은 사람 이미지를 바꾼다. 무덤사진의 좌표값을 바꾼다.
						{
							icon = new ImageIcon("grave.png");
							xval[0] = 27;//@@원래 이미지 좌표가 37인데 무덤으로 바뀌면서 x좌표를 27로 바꿔줌.
						}
						else if(i == 1)
						{
							icon2 = new ImageIcon("grave.png");
							xval[1] = 27;
						}
						else if(i == 2)
						{
							icon3 = new ImageIcon("grave.png");
							xval[2] = 27;
						}
						else if(i == 3)
						{
							icon4 = new ImageIcon("grave.png");
							xval[3] = 27;
						}
						else if(i == 4)
						{
							icon5 = new ImageIcon("grave.png");
							xval[4] = 27;
						}
						int tmp = 0;
						if (mychanumber == i) {
							isalive = false;
							if (job.equals("마피아")) {
								flag = 55;//@@자기자신이 죽었는데 자기직업이 마피아인경우 flag 55를 보내준다. 55의 경우 시민의 승리라는뜻인데 마피아클라가 서버에쏴주면 써버가 sendtoall 해주는 방식이다.
								new ClientSender(socket).start();
							}
						}
						diedp[i] = 1;
						for (int j = 0; j < 5; j++) {
							if (diedp[j] == 1) {
								tmp++;//tmp로 죽은 사람명수를 체크한다.
							}
						}
						if (tmp == 3 && isalive == true && job.equals("마피아")) {// 죽은사람이 3명 즉 2명남고, 자신이 마피아인데 살아있는경우는 마피아 승리경우, flag44
							flag = 44;
							new ClientSender(socket).start();
						} else
							tmp = 0;
					} else if (grp[0].equals("3"))// 경찰 //3은 플레이어 직업 검사 태그
					{
						// 경찰이 스킬을 쓸 대상의 번호
						int i = Integer.parseInt(grp[1]); // 0마피아 1경찰 2시민

						if (job.equals("경찰"))//스캔결과는 자신이 경찰일때만 보인다.
							if (i == 0) {
								System_message.append("그는 마피아입니다\n\n");
							} else if (i == 1) {
								System_message.append("그는 경찰입니다\n\n");
							} else if (i == 2) {
								System_message.append("그는 시민입니다\n\n");
							}
					} else if (grp[0].equals("7")) // 7은 시스템 메세지 업데이트 태그
						System_message.append(grp[1] + "\n\n");
					else if (grp[0].equals("00")) // 7은 시스템 메세지 업데이트 태그
					{
						Object [] option={"확인"};
						int choice2 = JOptionPane.showOptionDialog(null, grp[1], "승자는!!", JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE, null, option, option[0]);

						if(choice2 == 0)
							System.exit(0);
					}
					else if (grp[0].equals("8")) // 8은 list에 플레이어 이름들 띄우게 하는 태그
						// 클라 사이즈가 5명이면 수행 9번과 비슷
					{
						for (int z = 0; z < 5; z++)
							player[z] = grp[z + 1];
						repaint();

					}
					else if (grp[0].equals("77"))	//닉네임 중복일 시 서버의 해쉬맵에서 구분 불가 따라서 중복된 닉네임은 뒤에 숫자를 붙여서 구분짓게함
					{
						name = grp[1];
					}
					else
						chatting.append(message + "\n"); // 태그없는것은 채팅창에 알려줌 ex)
					// 누가 나갔습니다 같은게 뜸

					chatting.setCaretPosition(chatting.getDocument().getLength());
					System_message.setCaretPosition(System_message.getDocument().getLength());	//채팅창과 시스템메세지창에서 새로운 텍스트 추가되면 바로 그 지점으로 가게하는 부분. GUI
				} catch (IOException e) {
				}
			}
		}
	}

	class ClientSender extends Thread {			//클라가 서버에 정보를 보내도록하는 스레드.flag의 값들에따라서 클라가 서버에 보내는 정보가 다름.
		Socket socket;
		DataOutputStream output;

		public ClientSender(Socket socket) {
			this.socket = socket;
			try {
				output = new DataOutputStream(socket.getOutputStream());
			} catch (Exception e) {
			}
		}

		@Override
		public void run() {

			try {
				if (flag == 44) {
					output.writeUTF("44/" + mychanumber); // 자신의 넘버를 넘겨주는 이유는 마피아가누구였는지 플레이어들에게 알려주기 위함.
				} else if (flag == 55) {
					output.writeUTF("55/" + mychanumber);
				}
				else if (flag == 1) {
					msg = chat.getText();

					chat.setText(null);

					if (msg.equals("exit"))
						System.exit(0);

					output.writeUTF("1/" + "[" + name + "]" + "[" + cha + "]" + msg); // 1은
					// 채팅
					// 태그

					// flag=0;
				} else if (flag == 3) {
					if (phase == 2) // 투표시간일때
						output.writeUTF("2/" + choice); // 2/ 는 투표태그
					else if (phase == 4) // 밤일때
						output.writeUTF("4/" + choice); // 4/는 마피아 스킬 태그
				} else if (flag == 4) {
					if (phase == 4) // 밤일때
						output.writeUTF("3/" + choice); // 3/는 경찰 스킬 태그
				}
				else
					output.writeUTF(name);

				flag = 0;
			} catch (IOException e) {
			}
		}
	}

	public void setInfo() {		//서버로부터 받은 정보를 각 클라에게 각각 할당하는 함수.각각의 클라는 job,cha,mychanumber 등의 고유값을가짐
		if (grp[1].equals("0")) {
			job = "마피아";
		} else if (grp[1].equals("1")) {
			job = "경찰";
		} else
			job = "시민";
		if (grp[2].equals("0")) {
			mychanumber = 0;
			cha = "보스";
		} else if (grp[2].equals("2")) {
			mychanumber = 2;
			cha = "스킨헤드";
		} else if (grp[2].equals("1")) {
			mychanumber = 1;
			cha = "중절모";
		} else if (grp[2].equals("3")) {
			mychanumber = 3;
			cha = "멋쟁이";
		} else {
			mychanumber = 4;
			cha = "터프가이";
		}
		my_info.setText("직업 : " + job + "\n\n" + "캐릭터: " + cha + "\n\n" + "id: " + name);
	}

	public MainFrame() {

		super("Mafia Game");
		back = new ImageIcon("frame.png");
		JPanel background = new JPanel(){
			public void paintComponent(Graphics g) {
				g.drawImage(back.getImage(), 0, 0, null);
				setOpaque(false); //그림을 표시하게 설정,투명하게 조절
				super.paintComponent(g);
			}
		};
		setContentPane(background);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(200, 50, 950, 800);
		setVisible(true);
		setLayout(null);
		night = new ImageIcon("night.png");
		panel = new ImageIcon("panel.png");
		// ** 1번 패널 . 사용자들의 캐릭터와 ID 나타내고 클릭했을때 투표되는 기능 구현 **//
		contentPane = new JPanel(){};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		contentPane.setBounds(10, 150, 350, 555);
		contentPane.setLayout(new GridLayout(3, 2));
		background.add(contentPane);
		contentPane.setOpaque(false);

		// ** 2번 패널 . 각종 정보들의 박스 3개 만들어서 하나씩 구현 **//
		contentPane2 = new JPanel();
		contentPane2.setBounds(360, 150, 180, 555);
		contentPane2.setLayout(null);
		background.add(contentPane2);
		contentPane2.setOpaque(false);


		daytime = new JLabel(new ImageIcon("day2.png"));
		nighttime = new JLabel(new ImageIcon("night2.png"));
		votetime = new JLabel(new ImageIcon("vote2.png"));

		present = new JPanel();
		present.setBackground(new Color(0,0,0,0));
		present.setBounds(6, 35, 165, 180);
		present.add(daytime);
		present.add(nighttime);
		present.add(votetime);
		present.setOpaque(false);

		nighttime.setVisible(false);
		votetime.setVisible(false);
		contentPane2.add(present);

		my_info = new JTextPane();
		my_info.setText("직업 : ?? \n\n" + "캐릭터 : ??\n\n" + "id : ");
		my_info.setEditable(false);
		my_info.setBackground(new Color(0,0,0,232));
		my_info.setOpaque(true);
		my_info.setForeground(Color.WHITE);
		JScrollPane my_info_scroll = new JScrollPane(my_info);
		my_info_scroll.setBounds(6, 250, 165, 130);
		contentPane2.add(my_info_scroll);

		JList userlist = new JList<>(player);
		JScrollPane scroll = new JScrollPane(userlist);
		scroll.setBounds(6, 415, 165, 110);
		userlist.setBackground(new Color(0,0,0,232));
		userlist.setOpaque(true);
		userlist.setForeground(Color.WHITE);
		contentPane2.add(scroll);

		currentlabel = new JLabel("            현재 상황");
		currentlabel.setBounds(6, 5, 165, 30);
		currentlabel.setForeground(Color.WHITE);
		currentlabel.setFont(new Font("", Font.BOLD, 14));
		contentPane2.add(currentlabel);

		JLabel label2 = new JLabel("           자기 정보");
		label2.setBounds(6, 220, 165, 30);
		label2.setForeground(Color.WHITE);
		label2.setFont(new Font("", Font.BOLD, 14));
		contentPane2.add(label2);

		JLabel label3 = new JLabel("게임에 참여한 플레이어들");
		label3.setBounds(6, 385, 165, 30);
		label3.setForeground(Color.WHITE);
		label3.setFont(new Font("", Font.BOLD, 13));
		contentPane2.add(label3);

		icon = new ImageIcon("1.png");
		icon2 = new ImageIcon("2.png");
		icon3 = new ImageIcon("3.png");
		icon4 = new ImageIcon("4.png");
		icon5 = new ImageIcon("5.png");

		imgp = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null); // @@배경을 패널크기로 늘려서 넣기.
				g.drawImage(icon.getImage(), xval[0], 6, null); // @@이미지 원래사이즈로 넣기
			}
		};
		imgp2 = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null);
				g.drawImage(icon2.getImage(), xval[1], 5, null); //이미지 원래사이즈로 넣기
			}
		};
		imgp3 = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null);
				g.drawImage(icon3.getImage(), xval[2], 5, null); //이미지 원래사이즈로 넣기
			}
		};
		imgp4 = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null);
				g.drawImage(icon4.getImage(), xval[3], 5, null); //이미지 원래사이즈로 넣기
			}
		};
		imgp5 = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null);
				g.drawImage(icon5.getImage(), xval[4], 5, null); //이미지 원래사이즈로 넣기
			}
		};
		voteimg = new JLabel(new ImageIcon("vote.png"));
		bullet = new JLabel(new ImageIcon("bullet.png"));
		scan = new JLabel(new ImageIcon("scan.png"));

		ClickListener listen = new ClickListener();

		imgp.setLayout(new BorderLayout());
		imgp.setOpaque(false);
		name1 = new JTextField();
		name1.setBorder(new EmptyBorder(5, 5, 5, 5));
		name1.setText("보스");
		name1.setOpaque(false);
		name1.setHorizontalAlignment(name1.CENTER);
		name1.setForeground(Color.WHITE);
		imgp.add(name1, BorderLayout.SOUTH);
		contentPane.add(imgp);
		imgp.addMouseListener(listen);

		imgp2.setLayout(new BorderLayout());
		imgp2.setOpaque(false);
		name2 = new JTextField();
		name2.setText("중절모");
		name2.setOpaque(false);
		name2.setHorizontalAlignment(name2.CENTER);
		name2.setBorder(new EmptyBorder(5, 5, 5, 5));
		name2.setForeground(Color.WHITE);
		imgp2.add(name2, BorderLayout.SOUTH);
		contentPane.add(imgp2);
		imgp2.addMouseListener(listen);

		imgp3.setLayout(new BorderLayout());
		imgp3.setOpaque(false);
		name3 = new JTextField();
		name3.setText("스킨헤드");
		name3.setOpaque(false);
		name3.setHorizontalAlignment(name3.CENTER);
		name3.setBorder(new EmptyBorder(5, 5, 5, 5));
		name3.setForeground(Color.WHITE);
		imgp3.add(name3, BorderLayout.SOUTH);
		contentPane.add(imgp3);
		imgp3.addMouseListener(listen);

		imgp4.setLayout(new BorderLayout());
		imgp4.setOpaque(false);
		name4 = new JTextField();
		name4.setText("멋쟁이");
		name4.setOpaque(false);
		name4.setHorizontalAlignment(name4.CENTER);
		name4.setBorder(new EmptyBorder(5, 5, 5, 5));
		name4.setForeground(Color.WHITE);
		imgp4.add(name4, BorderLayout.SOUTH);
		contentPane.add(imgp4);
		imgp4.addMouseListener(listen);

		imgp5.setLayout(new BorderLayout());
		imgp5.setOpaque(false);
		name5 = new JTextField();
		name5.setText("터프가이");
		name5.setOpaque(false);
		name5.setHorizontalAlignment(name5.CENTER);
		name5.setBorder(new EmptyBorder(5, 5, 5, 5));
		name5.setForeground(Color.WHITE);
		imgp5.add(name5, BorderLayout.SOUTH);
		contentPane.add(imgp5);
		imgp5.addMouseListener(listen);

		JPanel votep = new JPanel();
		contentPane.add(votep);
		votep.setLayout(new BorderLayout());
		votep.setOpaque(false);
		votesend = new JButton("투표 전송");
		votesend.setBackground(new Color(255,255,255));
		votep.add(votesend, BorderLayout.SOUTH);
		votesend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isvoted=true;
				new ClientSender(socket).start();
			}
		});

		System_message = new JTextArea(); 
		System_message.setBackground(new Color(0,0,0,232));
		System_message.setOpaque(true);
		System_message.setForeground(Color.WHITE);
		System_message.setText("System Message :               		                        \n\n");
		System_message.setEditable(false);

		JScrollPane System_message_scroll = new JScrollPane(System_message);
		System_message_scroll.setBounds(545, 150, 370, 200);
		add(System_message_scroll);

		chatting = new JTextArea();
		chatting.setEditable(false);
		chatting.setBackground(new Color(0,0,0,232));
		chatting.setOpaque(true);
		chatting.setForeground(Color.WHITE);
		JScrollPane chatting_scroll = new JScrollPane(chatting);
		chatting_scroll.setBounds(545, 355, 370, 310);
		add(chatting_scroll);

		chat = new JTextField();
		chat.setBounds(545, 675, 300, 30);
		chat.setBackground(new Color(0,0,0,232));
		chat.setOpaque(true);
		chat.setForeground(Color.WHITE);
		add(chat);

		chat.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}

			public void keyReleased(KeyEvent e) {}

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyChar() == KeyEvent.VK_ENTER)
				{
					if(phase == 5)
					{
						flag = 1;
						new ClientSender(socket).start();
					}
					else
						System_message.append("낮이 아니면 대화할 수 없습니다\n");
				}
			}
		});

		send = new JButton("전송");
		send.setBounds(850, 675, 60, 30);
		add(send);

		send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(phase==5)
				{
					flag = 1;
					new ClientSender(socket).start();
				}
				else
					System_message.append("낮이 아니면 대화할 수 없습니다\n");
			}
		});
		repaint();// 굿굿
	}

	class ClickListener implements MouseListener {
		public void Sound(String file,boolean Loop)
		{
			Clip clip;
			try {
				AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
				clip = AudioSystem.getClip();
				clip.open(ais);
				clip.start();
				if(Loop) 
					clip.loop(-1);
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			if (isalive) {
				if (phase == 2 && !isvoted) {
					if (e.getSource() == imgp && diedp[0] != 1) {
						voteimg.setVisible(false);
						choice = 0;
						flag = 3;
						imgp.add(voteimg, BorderLayout.CENTER);//이미지를 패널위에 그려주는것.
						voteimg.setVisible(true);
					} else if (e.getSource() == imgp2 && diedp[1] != 1) {
						voteimg.setVisible(false);
						choice = 1;
						flag = 3;
						imgp2.add(voteimg, BorderLayout.CENTER);
						voteimg.setVisible(true);
					} else if (e.getSource() == imgp3 && diedp[2] != 1) {
						voteimg.setVisible(false);
						choice = 2;
						flag = 3;
						imgp3.add(voteimg, BorderLayout.CENTER);
						voteimg.setVisible(true);
					} else if (e.getSource() == imgp4 && diedp[3] != 1) {
						voteimg.setVisible(false);
						choice = 3;
						flag = 3;
						imgp4.add(voteimg, BorderLayout.CENTER);
						voteimg.setVisible(true);
					} else if (e.getSource() == imgp5 && diedp[4] != 1) {
						voteimg.setVisible(false);
						choice = 4;
						flag = 3;
						imgp5.add(voteimg, BorderLayout.CENTER);
						voteimg.setVisible(true);
					} else {
						System_message.append("이미 죽은 사람 입니다. 다시선택해주세요\n");
					}

				} else if (phase == 4 && !isvoted)
				{
					if (job.equals("마피아")) {
						if (e.getSource() == imgp && diedp[0] != 1) {
							Sound("shotgun.wav", false);
							bullet.setVisible(false);
							choice = 0;
							flag = 3;
							imgp.add(bullet, BorderLayout.CENTER);
							bullet.setVisible(true);
						} else if (e.getSource() == imgp2 && diedp[1] != 1) {
							Sound("shotgun.wav", false);
							bullet.setVisible(false);
							choice = 1;
							flag = 3;
							imgp2.add(bullet, BorderLayout.CENTER);
							bullet.setVisible(true);
						} else if (e.getSource() == imgp3 && diedp[2] != 1) {
							Sound("shotgun.wav", false);
							bullet.setVisible(false);
							choice = 2;
							flag = 3;
							imgp3.add(bullet, BorderLayout.CENTER);
							bullet.setVisible(true);
						} else if (e.getSource() == imgp4 && diedp[3] != 1) {
							Sound("shotgun.wav", false);
							bullet.setVisible(false);
							choice = 3;
							flag = 3;
							imgp4.add(bullet, BorderLayout.CENTER);
							bullet.setVisible(true);
						} else if (e.getSource() == imgp5 && diedp[4] != 1) {
							Sound("shotgun.wav", false);
							bullet.setVisible(false);
							choice = 4;
							flag = 3;
							imgp5.add(bullet, BorderLayout.CENTER);
							bullet.setVisible(true);
						} else {
							System_message.append("이미 죽은 사람 입니다. 다시선택해주세요\n");
						}
					} else if (job.equals("경찰")) {
						if (e.getSource() == imgp && diedp[0] != 1) {
							Sound("scan.wav", false);
							scan.setVisible(false);
							choice = 0;
							flag = 4;
							imgp.add(scan, BorderLayout.CENTER);
							scan.setVisible(true);
						} else if (e.getSource() == imgp2 && diedp[1] != 1) {
							Sound("scan.wav", false);
							scan.setVisible(false);
							choice = 1;
							flag = 4;
							imgp2.add(scan, BorderLayout.CENTER);
							scan.setVisible(true);
						} else if (e.getSource() == imgp3 && diedp[2] != 1) {
							Sound("scan.wav", false);
							scan.setVisible(false);
							choice = 2;
							flag = 4;
							imgp3.add(scan, BorderLayout.CENTER);
							scan.setVisible(true);
						} else if (e.getSource() == imgp4 && diedp[3] != 1) {
							Sound("scan.wav", false);
							scan.setVisible(false);
							choice = 3;
							flag = 4;
							imgp4.add(scan, BorderLayout.CENTER);
							scan.setVisible(true);
						} else if (e.getSource() == imgp5 && diedp[4] != 1) {
							Sound("scan.wav", false);
							scan.setVisible(false);
							choice = 4;
							flag = 4;
							imgp5.add(scan, BorderLayout.CENTER);
							scan.setVisible(true);
						} else {
							System_message.append("이미 죽은 사람 입니다. 다시선택해주세요\n");
						}
					} else {
						System_message.append("당신은 마피아 or 경찰이 아닙니다\n");
					}
				}
			} else
				System_message.append("당신은 죽었습니다. 채팅 외의 기능을 할 수 없습니다\n");

		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}
	}

	public void setImage(int phase) {
		if(phase ==5 )
		{
			daytime.setVisible(true);
			nighttime.setVisible(false);
			votetime.setVisible(false);
			currentlabel.setText(("         현재 상황 - 낮"));
		}
		if(phase ==2 )
		{
			daytime.setVisible(false);
			nighttime.setVisible(false);
			votetime.setVisible(true);
			currentlabel.setText(("       현재 상황 - 투표"));
		}
		if(phase ==4 )
		{
			daytime.setVisible(false);
			nighttime.setVisible(true);
			votetime.setVisible(false);
			currentlabel.setText(("         현재 상황 - 밤"));
		}

		voteimg.setVisible(false);
		bullet.setVisible(false);
		scan.setVisible(false);
		repaint();
	}

}