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
	String cha = "����";
	String[] grp;
	JList<String> userlist;

	int phase=5;
	int[] xval = {35, 35, 35, 35, 35};// @@�׸��� ��ǥ���̴�.
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

	/// ���⼭����
	public void start() {
		try {
			while(true)	//!!��ȭ�� �Է¹޴� �κ�  ��ȭ���� �� �ʿ��ϹǷ� �Է¾����� �ݺ������� �ް� �س���. �׸��� ��Ҹ� �����ٸ� Ŭ�� ������ ��
			{
				name = JOptionPane.showInputDialog(null, "��ȭ���� �Է��ϼ��� : ", "Login", JOptionPane.QUESTION_MESSAGE);
				if(name==null)
					System.exit(0);
				else if (name.equals(""))
					continue;
				break;
			}
			socket = new Socket(serverIp, 8);
			JOptionPane.showMessageDialog(null, "������ ����Ǿ����ϴ�\n������ ������ 5���̸� ������ ���۵˴ϴ�");

			ClientReceiver clientReceiver = new ClientReceiver(socket);		//������� �����κ��� ������ �޴� ������, ������ �����ϴ� �����带 ����
			ClientSender clientSender = new ClientSender(socket);

			clientReceiver.start();
			clientSender.start();
		} catch (IOException e) {
		}
	}

	class ClientReceiver extends Thread {		//�޴� ������. �����κ��� �±װ� �ٿ��� �� ��Ʈ���� split �Լ��� ���ؼ� ������ �����ϰ� �� �±׿� �°� ������ �̿���
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
					if (grp[0].equals("1")) // 1�� ��ǥ�±�
					{
						chatting.append(grp[1] + "\n");
					} else if (grp[0].equals("9")) { // 9�� ó���� setinfo() 5�������
						// ���� �Ҵ�
						setInfo();
					} 
					else if(grp[0].equals("11")){		//11�� ĳ���� �ؿ� �ִ� JTextField�� �̸� �Ҵ�
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
					else if (grp[0].equals("5"))// �� //5�� ���� �˷��ִ� �±�
					{
						setImage(5);
						phase = 5;

					} else if (grp[0].equals("2"))// ��ǥ //2�� ��ǥ�ð� �±�
					{
						setImage(2);
						phase = 2;
						flag = 0;
						isvoted = false;
					} else if (grp[0].equals("4"))// �� //4�� �� �ð� �±�
					{
						setImage(4);
						phase = 4;
						flag = 0;
						isvoted = false;
					} else if (grp[0].equals("6"))// ���� //6�� �÷��̾� ���϶� �±�
					{
						// ���� �ڱ� ��ȣ�� ������ �������
						int i = Integer.parseInt(grp[1]);
						if(i == 0)//@@���� ��� �̹����� �ٲ۴�. ���������� ��ǥ���� �ٲ۴�.
						{
							icon = new ImageIcon("grave.png");
							xval[0] = 27;//@@���� �̹��� ��ǥ�� 37�ε� �������� �ٲ�鼭 x��ǥ�� 27�� �ٲ���.
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
							if (job.equals("���Ǿ�")) {
								flag = 55;//@@�ڱ��ڽ��� �׾��µ� �ڱ������� ���Ǿ��ΰ�� flag 55�� �����ش�. 55�� ��� �ù��� �¸���¶��ε� ���Ǿ�Ŭ�� ���������ָ� ����� sendtoall ���ִ� ����̴�.
								new ClientSender(socket).start();
							}
						}
						diedp[i] = 1;
						for (int j = 0; j < 5; j++) {
							if (diedp[j] == 1) {
								tmp++;//tmp�� ���� �������� üũ�Ѵ�.
							}
						}
						if (tmp == 3 && isalive == true && job.equals("���Ǿ�")) {// ��������� 3�� �� 2����, �ڽ��� ���Ǿ��ε� ����ִ°��� ���Ǿ� �¸����, flag44
							flag = 44;
							new ClientSender(socket).start();
						} else
							tmp = 0;
					} else if (grp[0].equals("3"))// ���� //3�� �÷��̾� ���� �˻� �±�
					{
						// ������ ��ų�� �� ����� ��ȣ
						int i = Integer.parseInt(grp[1]); // 0���Ǿ� 1���� 2�ù�

						if (job.equals("����"))//��ĵ����� �ڽ��� �����϶��� ���δ�.
							if (i == 0) {
								System_message.append("�״� ���Ǿ��Դϴ�\n\n");
							} else if (i == 1) {
								System_message.append("�״� �����Դϴ�\n\n");
							} else if (i == 2) {
								System_message.append("�״� �ù��Դϴ�\n\n");
							}
					} else if (grp[0].equals("7")) // 7�� �ý��� �޼��� ������Ʈ �±�
						System_message.append(grp[1] + "\n\n");
					else if (grp[0].equals("00")) // 7�� �ý��� �޼��� ������Ʈ �±�
					{
						Object [] option={"Ȯ��"};
						int choice2 = JOptionPane.showOptionDialog(null, grp[1], "���ڴ�!!", JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE, null, option, option[0]);

						if(choice2 == 0)
							System.exit(0);
					}
					else if (grp[0].equals("8")) // 8�� list�� �÷��̾� �̸��� ���� �ϴ� �±�
						// Ŭ�� ����� 5���̸� ���� 9���� ���
					{
						for (int z = 0; z < 5; z++)
							player[z] = grp[z + 1];
						repaint();

					}
					else if (grp[0].equals("77"))	//�г��� �ߺ��� �� ������ �ؽ��ʿ��� ���� �Ұ� ���� �ߺ��� �г����� �ڿ� ���ڸ� �ٿ��� ����������
					{
						name = grp[1];
					}
					else
						chatting.append(message + "\n"); // �±׾��°��� ä��â�� �˷��� ex)
					// ���� �������ϴ� ������ ��

					chatting.setCaretPosition(chatting.getDocument().getLength());
					System_message.setCaretPosition(System_message.getDocument().getLength());	//ä��â�� �ý��۸޼���â���� ���ο� �ؽ�Ʈ �߰��Ǹ� �ٷ� �� �������� �����ϴ� �κ�. GUI
				} catch (IOException e) {
				}
			}
		}
	}

	class ClientSender extends Thread {			//Ŭ�� ������ ������ ���������ϴ� ������.flag�� ���鿡���� Ŭ�� ������ ������ ������ �ٸ�.
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
					output.writeUTF("44/" + mychanumber); // �ڽ��� �ѹ��� �Ѱ��ִ� ������ ���Ǿư����������� �÷��̾�鿡�� �˷��ֱ� ����.
				} else if (flag == 55) {
					output.writeUTF("55/" + mychanumber);
				}
				else if (flag == 1) {
					msg = chat.getText();

					chat.setText(null);

					if (msg.equals("exit"))
						System.exit(0);

					output.writeUTF("1/" + "[" + name + "]" + "[" + cha + "]" + msg); // 1��
					// ä��
					// �±�

					// flag=0;
				} else if (flag == 3) {
					if (phase == 2) // ��ǥ�ð��϶�
						output.writeUTF("2/" + choice); // 2/ �� ��ǥ�±�
					else if (phase == 4) // ���϶�
						output.writeUTF("4/" + choice); // 4/�� ���Ǿ� ��ų �±�
				} else if (flag == 4) {
					if (phase == 4) // ���϶�
						output.writeUTF("3/" + choice); // 3/�� ���� ��ų �±�
				}
				else
					output.writeUTF(name);

				flag = 0;
			} catch (IOException e) {
			}
		}
	}

	public void setInfo() {		//�����κ��� ���� ������ �� Ŭ�󿡰� ���� �Ҵ��ϴ� �Լ�.������ Ŭ��� job,cha,mychanumber ���� ������������
		if (grp[1].equals("0")) {
			job = "���Ǿ�";
		} else if (grp[1].equals("1")) {
			job = "����";
		} else
			job = "�ù�";
		if (grp[2].equals("0")) {
			mychanumber = 0;
			cha = "����";
		} else if (grp[2].equals("2")) {
			mychanumber = 2;
			cha = "��Ų���";
		} else if (grp[2].equals("1")) {
			mychanumber = 1;
			cha = "������";
		} else if (grp[2].equals("3")) {
			mychanumber = 3;
			cha = "������";
		} else {
			mychanumber = 4;
			cha = "��������";
		}
		my_info.setText("���� : " + job + "\n\n" + "ĳ����: " + cha + "\n\n" + "id: " + name);
	}

	public MainFrame() {

		super("Mafia Game");
		back = new ImageIcon("frame.png");
		JPanel background = new JPanel(){
			public void paintComponent(Graphics g) {
				g.drawImage(back.getImage(), 0, 0, null);
				setOpaque(false); //�׸��� ǥ���ϰ� ����,�����ϰ� ����
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
		// ** 1�� �г� . ����ڵ��� ĳ���Ϳ� ID ��Ÿ���� Ŭ�������� ��ǥ�Ǵ� ��� ���� **//
		contentPane = new JPanel(){};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		contentPane.setBounds(10, 150, 350, 555);
		contentPane.setLayout(new GridLayout(3, 2));
		background.add(contentPane);
		contentPane.setOpaque(false);

		// ** 2�� �г� . ���� �������� �ڽ� 3�� ���� �ϳ��� ���� **//
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
		my_info.setText("���� : ?? \n\n" + "ĳ���� : ??\n\n" + "id : ");
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

		currentlabel = new JLabel("            ���� ��Ȳ");
		currentlabel.setBounds(6, 5, 165, 30);
		currentlabel.setForeground(Color.WHITE);
		currentlabel.setFont(new Font("", Font.BOLD, 14));
		contentPane2.add(currentlabel);

		JLabel label2 = new JLabel("           �ڱ� ����");
		label2.setBounds(6, 220, 165, 30);
		label2.setForeground(Color.WHITE);
		label2.setFont(new Font("", Font.BOLD, 14));
		contentPane2.add(label2);

		JLabel label3 = new JLabel("���ӿ� ������ �÷��̾��");
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
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null); // @@����� �г�ũ��� �÷��� �ֱ�.
				g.drawImage(icon.getImage(), xval[0], 6, null); // @@�̹��� ����������� �ֱ�
			}
		};
		imgp2 = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null);
				g.drawImage(icon2.getImage(), xval[1], 5, null); //�̹��� ����������� �ֱ�
			}
		};
		imgp3 = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null);
				g.drawImage(icon3.getImage(), xval[2], 5, null); //�̹��� ����������� �ֱ�
			}
		};
		imgp4 = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null);
				g.drawImage(icon4.getImage(), xval[3], 5, null); //�̹��� ����������� �ֱ�
			}
		};
		imgp5 = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(panel.getImage(), 0, 0, d.width, d.height, null);
				g.drawImage(icon5.getImage(), xval[4], 5, null); //�̹��� ����������� �ֱ�
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
		name1.setText("����");
		name1.setOpaque(false);
		name1.setHorizontalAlignment(name1.CENTER);
		name1.setForeground(Color.WHITE);
		imgp.add(name1, BorderLayout.SOUTH);
		contentPane.add(imgp);
		imgp.addMouseListener(listen);

		imgp2.setLayout(new BorderLayout());
		imgp2.setOpaque(false);
		name2 = new JTextField();
		name2.setText("������");
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
		name3.setText("��Ų���");
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
		name4.setText("������");
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
		name5.setText("��������");
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
		votesend = new JButton("��ǥ ����");
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
						System_message.append("���� �ƴϸ� ��ȭ�� �� �����ϴ�\n");
				}
			}
		});

		send = new JButton("����");
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
					System_message.append("���� �ƴϸ� ��ȭ�� �� �����ϴ�\n");
			}
		});
		repaint();// �±�
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
						imgp.add(voteimg, BorderLayout.CENTER);//�̹����� �г����� �׷��ִ°�.
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
						System_message.append("�̹� ���� ��� �Դϴ�. �ٽü������ּ���\n");
					}

				} else if (phase == 4 && !isvoted)
				{
					if (job.equals("���Ǿ�")) {
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
							System_message.append("�̹� ���� ��� �Դϴ�. �ٽü������ּ���\n");
						}
					} else if (job.equals("����")) {
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
							System_message.append("�̹� ���� ��� �Դϴ�. �ٽü������ּ���\n");
						}
					} else {
						System_message.append("����� ���Ǿ� or ������ �ƴմϴ�\n");
					}
				}
			} else
				System_message.append("����� �׾����ϴ�. ä�� ���� ����� �� �� �����ϴ�\n");

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
			currentlabel.setText(("         ���� ��Ȳ - ��"));
		}
		if(phase ==2 )
		{
			daytime.setVisible(false);
			nighttime.setVisible(false);
			votetime.setVisible(true);
			currentlabel.setText(("       ���� ��Ȳ - ��ǥ"));
		}
		if(phase ==4 )
		{
			daytime.setVisible(false);
			nighttime.setVisible(true);
			votetime.setVisible(false);
			currentlabel.setText(("         ���� ��Ȳ - ��"));
		}

		voteimg.setVisible(false);
		bullet.setVisible(false);
		scan.setVisible(false);
		repaint();
	}

}