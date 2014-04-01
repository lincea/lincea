package com.jiesoon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import com.jiesoon.BusMessage;

public class BusServer {
	private ServerSocket mServerSocket;
	private int mPort = 7798;
	
	private BusManager mBusManager;
	private NewsManager mNewsManager;
	
	public BusServer(){
		try {
			
			mBusManager = new BusManager();
			mNewsManager = new NewsManager();
			
			mBusManager.init();
			mNewsManager.init();
			
			mServerSocket = new ServerSocket(mPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start(){

		while(true){
			Socket client;
			try {
				client = mServerSocket.accept();
				new WorkerThread(client).start();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	class WorkerThread extends Thread{
		private Socket mSocket;
		private BufferedReader mReader;
		private BufferedWriter mWriter;
		
		public WorkerThread(Socket socket){
			mSocket = socket;
			if(mSocket != null){
				try {
					mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
					mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		@Override
		public void run() {
			int errCode = BusMessage.Response.ERR_NONE;
			
			if(mReader != null){

				while(true){
					try {
						String msg = mReader.readLine();
						System.out.println("MSG: " + msg);
						if(msg == null){
							break;
						}

						try {
							JSONObject oMsg = new JSONObject(msg);
							int cmdId = oMsg.getInt(BusMessage.KEY_COMMAND_ID);
							switch(cmdId){
							case BusMessage.CMD_BUS_POS:
								errCode = BusMessage.Response.ERR_NONE;
								
								String track = oMsg.getString(BusMessage.EXTRA_TRACK);
								String busId = oMsg.getString(BusMessage.EXTRA_BUS_ID);
								String stopName = oMsg.getString(BusMessage.EXTRA_BUS_STOP);
								double x = oMsg.getDouble(BusMessage.EXTRA_X);
								double y = oMsg.getDouble(BusMessage.EXTRA_Y);
								
								//Get stop name from (x,y)
								//Send bus stop name to LED screen.
								
								if(track == null || track.trim().length() == 0 ||
								   busId == null || busId.trim().length() == 0 ||
								   stopName == null || stopName.trim().length() == 0
										){
									errCode = BusMessage.Response.ERR_BUS_POS;
								}
								
								
								System.out.println("BusPos: (" + x + "," + y + ")");
								String resp = new BusMessage.Response()
								              .buildResp(errCode, 
								            		     BusMessage.CMD_BUS_POS);
								mWriter.write(resp);
								mWriter.newLine();
								mWriter.flush();
								break;
							case BusMessage.CMD_USER_REGISTER:
								errCode = BusMessage.Response.ERR_NONE;
								
								String name = oMsg.getString(BusMessage.EXTRA_USER_NAME);
								String gender = oMsg.getString(BusMessage.EXTRA_USER_GENDER);
								String dayOfBirth = oMsg.getString(BusMessage.EXTRA_USER_DAY_OF_BIRTH);
								int money = oMsg.getInt(BusMessage.EXTRA_USER_MONEY);
								
								if(name == null || name.trim().length() == 0 ||
								   gender == null || name.trim().length() == 0 ||
								   dayOfBirth == null || dayOfBirth.trim().length() == 0 ||
								   money < 0){
									errCode = BusMessage.Response.ERR_USER_REGISTER;
								}
								

								System.out.println("User register");
								resp = new BusMessage.Response()
								           .buildResp(errCode, 
								        		   BusMessage.CMD_USER_REGISTER);
								mWriter.write(resp);
								mWriter.newLine();
								mWriter.flush();
								break;
							case BusMessage.CMD_USER_UNREGISTER:
								errCode = BusMessage.Response.ERR_NONE;
								
								String id = oMsg.getString(BusMessage.EXTRA_USER_ID);
								
								if(id == null || id.trim().length() == 0){
									errCode = BusMessage.Response.ERR_USER_UNREGISTER;
								}
								
								System.out.println("CMD_USER_UNREGISTER");
								resp = new BusMessage.Response()
						           .buildResp(BusMessage.Response.ERR_USER_UNREGISTER, 
						        		   BusMessage.CMD_USER_UNREGISTER);
								mWriter.write(resp);
								mWriter.newLine();
								mWriter.flush();
						
								break;
							case BusMessage.CMD_USER_ADD:
								errCode = BusMessage.Response.ERR_NONE;
								
								id = oMsg.getString(BusMessage.EXTRA_USER_ID);
								money = oMsg.getInt(BusMessage.EXTRA_USER_MONEY);
								
								if(id == null || id.trim().length() == 0 ||
										money <= 0){
									errCode = BusMessage.Response.ERR_USER_ADD;
								}
								
								System.out.println("CMD_USER_ADD:");
								resp = new BusMessage.Response()
						           .buildResp(errCode, BusMessage.CMD_USER_ADD);
								
								mWriter.write(resp);
								mWriter.newLine();
								mWriter.flush();
								break;
							case BusMessage.CMD_USER_SUB:
								errCode = BusMessage.Response.ERR_NONE;
								
								id = oMsg.getString(BusMessage.EXTRA_USER_ID);
								money = oMsg.getInt(BusMessage.EXTRA_USER_MONEY);
								if(id == null || id.trim().length() == 0 ||
										money <= 0){
									errCode = BusMessage.Response.ERR_USER_SUB;
								}
								
								System.out.println("CMD_USER_SUB:");
								resp = new BusMessage.Response()
						           .buildResp(errCode, BusMessage.CMD_USER_SUB);
								
								mWriter.write(resp);
								mWriter.newLine();
								mWriter.flush();

								break;
							case BusMessage.CMD_USER_GET_TIMETABLE:
								errCode = BusMessage.Response.ERR_NONE;
								
								busId = oMsg.getString(BusMessage.EXTRA_USER_TIMETABLE);
								if(busId == null || busId.trim().length() == 0){
									errCode = BusMessage.Response.ERR_USER_GET_TIMETABLE;
								}

								System.out.println("CMD_USER_GET_TIMETABLE:");
								resp = new BusMessage.Response()
						           .buildTimeTableResp(errCode, BusMessage.CMD_USER_GET_TIMETABLE, "TimeTable:5:30-天安门西 5:45-天安门东 6:00-北京站 6:20-北京西站");
								
								mWriter.write(resp);
								mWriter.newLine();
								mWriter.flush();								
								break;
							case BusMessage.CMD_USER_GET_STOPS:
								errCode = BusMessage.Response.ERR_NONE;
								
								x = oMsg.getDouble(BusMessage.EXTRA_X);
								y = oMsg.getDouble(BusMessage.EXTRA_Y);
								if(x < 0 || y < 0){
									errCode = BusMessage.Response.ERR_USER_GET_STOP;
								}

								System.out.println("CMD_USER_GET_STOPS:");
								resp = new BusMessage.Response()
						           .buildGetStopResp(errCode, 
						        		   BusMessage.CMD_USER_GET_STOPS, "上地东里");

								mWriter.write(resp);
								mWriter.newLine();
								mWriter.flush();
								break;
							case BusMessage.CMD_USER_GET_NEWS:
								System.out.println("CMD_USER_GET_NEWS:");
								
								//Get news from NewsManager.
								String news = "台媒：习近平主动向连战提“习马会”并探询看法";

								resp = new BusMessage.Response()
						           .buildNewsResp(BusMessage.Response.ERR_NONE,
						        		   BusMessage.CMD_SVR_NEWS, news);

								mWriter.write(resp);
								mWriter.newLine();
								mWriter.flush();
								break;
							default:
								System.err.println("Invalid command id");
							}
						} catch (JSONException e) {
							e.printStackTrace();
							continue;
						}

					} catch (IOException e) {
						e.printStackTrace();
						break;
					}					
				}
			}
			
		}
		
	}
}
