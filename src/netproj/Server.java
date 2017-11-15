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
		cha.put(0, "보스");
		cha.put(1, "중절모");
		cha.put(2, "스킨헤드");
		cha.put(3, "멋쟁이");
		cha.put(4, "터프가이");

		// 여러 스레드에서 접근할 것이므로 동기화
		Collections.synchronizedMap(clients);
		Collections.synchronizedMap(cha);
	}

	public void start() {
		try {
			Socket socket;

			// 리스너 소켓 생성
			serverSocket = new ServerSocket(8);
			System.out.println("서버가 시작되었습니다.");

			// 클라이언트와 연결되면
			while (true) {
				// 통신 소켓을 생성하고 스레드 생성
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
				// 클라이언트가 서버에 접속하면 대화방에 알린다.
				name = input.readUTF();

				Iterator<String> it = clients.keySet().iterator();
				while(it.hasNext())
				{
					if(name.equals(it.next()))
					{
						name = name+ii;
						ii++;
						output.writeUTF("7/닉네임 중복으로 닉네임을 바꿨습니다");
						output.writeUTF("77/"+name);
						break;
					}
				}

				nameset[count]  = name;

				count++;

				sendToAll("#" + name + "[" + socket.getInetAddress() + ":"
						+ socket.getPort() + "]" + "님이 대화방에 접속하였습니다.");

				clients.put(name, output);

				System.out.println(name + "[" + socket.getInetAddress() + ":"
						+ socket.getPort() + "]" + "님이 대화방에 접속하였습니다.");
				System.out.println("현재 " + clients.size() + "명이 대화방에 접속 중입니다.");

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

				while (input != null) {		//여기에 구분자 1,2,3등으로 구분해서 보내주는 부분 해야할듯
					String buffer = input.readUTF();

					String []grp = buffer.split("/");

					if(grp[0].equals("1"))
						sendToAll(buffer);

					if(grp[0].equals("2"))
					{
						sendToAll("투표하셨습니다.");
						voteCnt++;
						if(grp[1].equals("0"))
						{
							player[0]++;//player[] 배열을 카운트한다는 것은 투표 수를 저장해둠.
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
								sendToAll("7/경찰이 스킬을 사용하였습니다.");
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
								sendToAll("7/마피아가 누군가를 표적으로 삼았습니다.");
								killed = i;
								diedp[i] = 1;
							}
						}
					}
					if(grp[0].equals("44"))//flag 44의 경우 마피아 승리.
					{
						timer.cancel();
						//timer.purge();
						timer2.cancel();
						sendToAll("7/마피아는 "+cha.get(Integer.parseInt(grp[1]))+" 였습니다. 마피아의 승리입니다.");
						sendToAll("00/마피아는 "+cha.get(Integer.parseInt(grp[1]))+" 였습니다.\n마피아의 승리입니다.");
					//	maxpeople = 5;
						ii=0;
					}
					if(grp[0].equals("55"))
					{
						timer.cancel();
						timer2.cancel();
						sendToAll("7/처형당한 "+cha.get(Integer.parseInt(grp[1]))+"는 마피아였습니다. 시민과 경찰의 승리입니다.");
						sendToAll("00/처형당한 "+cha.get(Integer.parseInt(grp[1]))+"는 마피아였습니다.\n시민과 경찰의 승리입니다.");
					//	maxpeople = 5;
						ii=0;
					}
				}
			} catch (IOException e) {
			} finally {
				// 접속이 종료되면
				count--;
				clients.remove(name);
				sendToAll("#" + name + "[" + socket.getInetAddress() + ":"
						+ socket.getPort() + "]" + "님이 대화방에서 나갔습니다.");
				System.out.println(name + "[" + socket.getInetAddress() + ":"
						+ socket.getPort() + "]" + "님이 대화방에서 나갔습니다.");
				System.out.println("현재 " + clients.size() + "명이 대화방에 접속 중입니다.");
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
							sendToAll("7/밤사이 멍청한 마피아가 아무도 죽이지 않았습니다.");
						}
						else
						{
							sendToAll("7/마피아가 밤사이 "+cha.get(killed)+"를 죽였습니다. \n"+cha.get(killed)+"가 죽었습니다.");
							sendToAll("6/"+killed);
							killed = 99;
						//	maxpeople--;
						}
					}
					sendToAll("5");//낮
					sendToAll("7/낮이되었습니다.");//낮

					voteCnt = 0;
					for(int i  = 0; i <5; i++)
					{
						player[i] = 0;
					}
					cnt =0;

				}
				else if((phase % 3) == 2)
				{
					sendToAll("2");//투표
					sendToAll("7/결정의 시간이 되었습니다. \n처형할 캐릭터를 클릭하여 투표하세요");
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
						sendToAll("7/최다 득표가 2명 이상이므로 이번판은 아무도 죽지 않았습니다.");
						voteCnt = 0;
						for(int i  = 0; i <5; i++)
						{
							player[i] = 0;
						}
					}
					else
					{
						sendToAll("7/최다 득표자는 "+cha.get(tmp)+"입니다. \n"+cha.get(tmp)+"가 죽었습니다.");
						sendToAll("6/"+(tmp));
						diedp[tmp] = 1;//1로 바뀐 플레이어가 죽었다는 것을 나타내는 5명자리 배열
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
					
					
					sendToAll("4");//밤
					sendToAll("7/밤이 되었습니다.\n마피아는 죽이고 싶은 캐릭터를 클릭하여 투표하세요 \n경찰은 스킬을 쓸 대상을 클릭하세요");
				}
			}
		}
		class AlarmPhase extends TimerTask// 두번째 TimerTask 
		{
			@Override
			public void run()
			{
				if((phase % 3) ==  1)
				{
					sendToAll("7/투표시간이 되기 10초 전입니다.");//낮
				}
				else if((phase % 3) == 2)
				{
					sendToAll("7/밤이 되기 10초 전입니다. \n처형할 캐릭터를 클릭하여 투표하세요");
				}
				else if((phase % 3) == 0)
				{
					sendToAll("7/낮이 되기 10초 전입니다.");
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