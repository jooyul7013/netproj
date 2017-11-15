package netproj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
	private HashMap<String, DataOutputStream> clients;
	private HashMap<Integer, String> cha ;
	private ServerSocket serverSocket;
	int voteCnt = 0;
	int maxpeople = 5;
	int killed = 99;
	int ii=2;
	int[] player = {0,0,0,0,0};
	int[] diedp = {0,0,0,0,0};
	Timer timer;
	Timer timer2;

	int[] jobb = new int[5];
	int [] chaa = new int [5];
	int cnt = 0;

	String [] nameset = new String[10];
	String [] nameset2 = new String[10];
	int count =0;
	public static void main(String[] args) {
		new Server().start();
	}

	public Server() {
		clients = new HashMap<String, DataOutputStream>();
		cha = new HashMap<Integer,String>();
		cha.put(0, "����");
		cha.put(1, "������");
		cha.put(2, "��Ų���");
		cha.put(3, "������");
		cha.put(4, "��������");

		// ���� �����忡�� ������ ���̹Ƿ� ����ȭ
		Collections.synchronizedMap(clients);
		Collections.synchronizedMap(cha);
	}

	public void start() {
		try {
			Socket socket;

			// ������ ���� ����
			serverSocket = new ServerSocket(8);
			System.out.println("������ ���۵Ǿ����ϴ�.");

			// Ŭ���̾�Ʈ�� ����Ǹ�
			while (true) {
				// ��� ������ �����ϰ� ������ ����
				socket = serverSocket.accept();
				ServerReceiver receiver = new ServerReceiver(socket);
				receiver.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ServerReceiver extends Thread {
		Socket socket;
		DataInputStream input;
		DataOutputStream output;
		int phase;

		public ServerReceiver(Socket socket) {
			this.socket = socket;
			try {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
			}
		}

		@Override
		public void run() {
			String name = "";
			try {
				// Ŭ���̾�Ʈ�� ������ �����ϸ� ��ȭ�濡 �˸���.
				name = input.readUTF();

				Iterator<String> it = clients.keySet().iterator();
				while(it.hasNext())
				{
					if(name.equals(it.next()))
					{
						name = name+ii;
						ii++;
						output.writeUTF("7/�г��� �ߺ����� �г����� �ٲ���ϴ�");
						output.writeUTF("77/"+name);
						break;
					}
				}

				nameset[count]  = name;

				count++;

				sendToAll("#" + name + "[" + socket.getInetAddress() + ":"
						+ socket.getPort() + "]" + "���� ��ȭ�濡 �����Ͽ����ϴ�.");

				clients.put(name, output);

				System.out.println(name + "[" + socket.getInetAddress() + ":"
						+ socket.getPort() + "]" + "���� ��ȭ�濡 �����Ͽ����ϴ�.");
				System.out.println("���� " + clients.size() + "���� ��ȭ�濡 ���� ���Դϴ�.");

				if(clients.size()==5){
					sendjob_cha();
					timer = new Timer();
					timer2 = new Timer();
					GamePhase t = new GamePhase();
					AlarmPhase a = new AlarmPhase();
					timer.schedule(t,0,30000);
					timer2.schedule(a,20000,30000);
					sendToAll("8/"+nameset[0]+"/" +nameset[1]+"/" +nameset[2]+"/" +nameset[3]+"/" +nameset[4]);

				}

				while (input != null) {		//���⿡ ������ 1,2,3������ �����ؼ� �����ִ� �κ� �ؾ��ҵ�
					String buffer = input.readUTF();

					String []grp = buffer.split("/");

					if(grp[0].equals("1"))
						sendToAll(buffer);

					if(grp[0].equals("2"))
					{
						sendToAll("��ǥ�ϼ̽��ϴ�.");
						voteCnt++;
						if(grp[1].equals("0"))
						{
							player[0]++;//player[] �迭�� ī��Ʈ�Ѵٴ� ���� ��ǥ ���� �����ص�.
						}
						else if(grp[1].equals("1"))
						{
							player[1]++;
						}
						else if(grp[1].equals("2"))
						{
							player[2]++;
						}
						else if(grp[1].equals("3"))
						{
							player[3]++;
						}
						else if(grp[1].equals("4"))
						{
							player[4]++;
						}
						
					}
					if(grp[0].equals("3"))
					{
						for(int i=0; i < 5; i ++)
						{
							if(grp[1].equals(String.valueOf(chaa[i])))
							{
								sendToAll("7/������ ��ų�� ����Ͽ����ϴ�.");
								sendToAll("3/"+jobb[i]);
							}
						}
					}
					if(grp[0].equals("4"))
					{
						for(int i=0; i < 5; i ++)
						{
							if(grp[1].equals(String.valueOf(i)))
							{
								sendToAll("7/���Ǿư� �������� ǥ������ ��ҽ��ϴ�.");
								killed = i;
								diedp[i] = 1;
							}
						}
					}
					if(grp[0].equals("44"))//flag 44�� ��� ���Ǿ� �¸�.
					{
						timer.cancel();
						//timer.purge();
						timer2.cancel();
						sendToAll("7/���Ǿƴ� "+cha.get(Integer.parseInt(grp[1]))+" �����ϴ�. ���Ǿ��� �¸��Դϴ�.");
						sendToAll("00/���Ǿƴ� "+cha.get(Integer.parseInt(grp[1]))+" �����ϴ�.\n���Ǿ��� �¸��Դϴ�.");
					//	maxpeople = 5;
						ii=0;
					}
					if(grp[0].equals("55"))
					{
						timer.cancel();
						timer2.cancel();
						sendToAll("7/ó������ "+cha.get(Integer.parseInt(grp[1]))+"�� ���Ǿƿ����ϴ�. �ùΰ� ������ �¸��Դϴ�.");
						sendToAll("00/ó������ "+cha.get(Integer.parseInt(grp[1]))+"�� ���Ǿƿ����ϴ�.\n�ùΰ� ������ �¸��Դϴ�.");
					//	maxpeople = 5;
						ii=0;
					}
				}
			} catch (IOException e) {
			} finally {
				// ������ ����Ǹ�
				count--;
				clients.remove(name);
				sendToAll("#" + name + "[" + socket.getInetAddress() + ":"
						+ socket.getPort() + "]" + "���� ��ȭ�濡�� �������ϴ�.");
				System.out.println(name + "[" + socket.getInetAddress() + ":"
						+ socket.getPort() + "]" + "���� ��ȭ�濡�� �������ϴ�.");
				System.out.println("���� " + clients.size() + "���� ��ȭ�濡 ���� ���Դϴ�.");
			}
		}

		public void sendToAll(String message) {
			Iterator<String> it = clients.keySet().iterator();

			while (it.hasNext()) {
				try {
					DataOutputStream dos = clients.get(it.next());
					dos.writeUTF(message);
				} catch (Exception e) {
				}
			}
		}

		class GamePhase extends TimerTask
		{
			@Override
			public void run()
			{
				phase ++;
				if((phase % 3) ==  1)
				{
					if(phase == 1)
						;
					else{
						if(killed == 99)
						{
							sendToAll("7/����� ��û�� ���Ǿư� �ƹ��� ������ �ʾҽ��ϴ�.");
						}
						else
						{
							sendToAll("7/���Ǿư� ����� "+cha.get(killed)+"�� �׿����ϴ�. \n"+cha.get(killed)+"�� �׾����ϴ�.");
							sendToAll("6/"+killed);
							killed = 99;
						//	maxpeople--;
						}
					}
					sendToAll("5");//��
					sendToAll("7/���̵Ǿ����ϴ�.");//��

					voteCnt = 0;
					for(int i  = 0; i <5; i++)
					{
						player[i] = 0;
					}
					cnt =0;

				}
				else if((phase % 3) == 2)
				{
					sendToAll("2");//��ǥ
					sendToAll("7/������ �ð��� �Ǿ����ϴ�. \nó���� ĳ���͸� Ŭ���Ͽ� ��ǥ�ϼ���");
				}
				else if((phase % 3) == 0)
				{

					int tmp = 99;
					int tmp2 = 0;
					cnt = 0;
					for(int i = 0; i <5; i++)
					{
						if(player[i]>tmp2)
						{
							tmp2 = player[i];
							tmp = i;
						}
					}
					for(int i  = 0; i <5; i++)
					{
						if(player[i] == tmp2)
						{
							cnt++;
						}
					}
					if(cnt > 1)
					{
						sendToAll("7/�ִ� ��ǥ�� 2�� �̻��̹Ƿ� �̹����� �ƹ��� ���� �ʾҽ��ϴ�.");
						voteCnt = 0;
						for(int i  = 0; i <5; i++)
						{
							player[i] = 0;
						}
					}
					else
					{
						sendToAll("7/�ִ� ��ǥ�ڴ� "+cha.get(tmp)+"�Դϴ�. \n"+cha.get(tmp)+"�� �׾����ϴ�.");
						sendToAll("6/"+(tmp));
						diedp[tmp] = 1;//1�� �ٲ� �÷��̾ �׾��ٴ� ���� ��Ÿ���� 5���ڸ� �迭
					}

					voteCnt = 0;
					for(int i  = 0; i <5; i++)
					{
						player[i] = 0;
					}
					cnt =0;
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					sendToAll("4");//��
					sendToAll("7/���� �Ǿ����ϴ�.\n���Ǿƴ� ���̰� ���� ĳ���͸� Ŭ���Ͽ� ��ǥ�ϼ��� \n������ ��ų�� �� ����� Ŭ���ϼ���");
				}
			}
		}
		class AlarmPhase extends TimerTask// �ι�° TimerTask 
		{
			@Override
			public void run()
			{
				if((phase % 3) ==  1)
				{
					sendToAll("7/��ǥ�ð��� �Ǳ� 10�� ���Դϴ�.");//��
				}
				else if((phase % 3) == 2)
				{
					sendToAll("7/���� �Ǳ� 10�� ���Դϴ�. \nó���� ĳ���͸� Ŭ���Ͽ� ��ǥ�ϼ���");
				}
				else if((phase % 3) == 0)
				{
					sendToAll("7/���� �Ǳ� 10�� ���Դϴ�.");
				}
			}
		}
	}

	public void sendjob_cha(){
		Iterator<String> it = clients.keySet().iterator();
		Random job2 = new Random();
		Random cha2 = new Random();
		for(int i=0;i<5;i++){
			jobb[i]=job2.nextInt(5);
			chaa[i]=cha2.nextInt(5);
			for(int j=0;j<i;j++){
				if(jobb[i]==jobb[j]){
					i=i-1;
					break;
				}
			}
			for(int k=0;k<i;k++){
				if(chaa[i]==chaa[k])
				{
					i=i-1;
					break;
				}
			}
		}
		for(int i=0;i<5;i++){
			if(jobb[i]>1){
				jobb[i]=2;
			}
		}
		int n=0;
		while(it.hasNext()){
			try {
				nameset2[n] = it.next();
				DataOutputStream dos = clients.get(nameset2[n]);
				dos.writeUTF("9/"+jobb[n]+"/"+chaa[n]+"/"+nameset2[n]);

				n++;
			} catch (IOException e) {e.printStackTrace();}
		}			

		it = clients.keySet().iterator();

		while(it.hasNext()){
			try {
				DataOutputStream dos = clients.get(it.next());
				for(int k = 0 ; k<5;k++)
					dos.writeUTF("11/"+chaa[k]+"/"+nameset2[k]);
			} catch (IOException e) {e.printStackTrace();}
		}

	}
}