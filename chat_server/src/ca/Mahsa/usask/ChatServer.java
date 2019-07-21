	/**
	 * 
	 * @author Mahsa
	 * 
	 * 
	 */
package ca.Mahsa.usask;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
public class ChatServer {		
		// Creating a  Socket server to communicate with socket clients. 
	ServerSocket SRVSOCK = null;
	
		//This thread creates for each chat user, and provide the input and output stream.
	ClientThread clientThread = null;	
		/**
    	""""
		* Creating maps 
		*/
	public static Map<String, Socket> clientSocketUserMap = null;		
	public static Map<String, List<String>> messageChatRoomMap = new HashMap<String, List<String>>();	
	public static Map<String, String> chatRoomUserMap = new HashMap<String, String>();
			
		/**
    	""""
    	Purpose:
			The main method to run chat server.			
		Pre-conditions:
        	:param args 
    	Return
        	:return: void
    	""""
	 	*/
	public static void main(String[] args) throws Exception{
		@SuppressWarnings("unused") 		
	  //@SuppressWarnings("deprecation")   
		ChatServer chatServer = new ChatServer();
	}
	
		/**
    	""""
    	Purpose:
		 	*The constructor method of the class ChatServer.
		 *
	    */
	public ChatServer() {
		try {
			Timer timer = new Timer(); // Instantiate Timer Object
			TimerScheduledPeriod task = new TimerScheduledPeriod(); // Instantiate TimerScheduledPeriod
			timer.schedule(task, 0, 1000); 
			SRVSOCK = new ServerSocket(4321);
			clientSocketUserMap = new HashMap<String, Socket>();
			System.out.println("Waiting for clients...");
			while (true) {
				Socket SOCKs = SRVSOCK.accept();
				@SuppressWarnings("unused")
				ClientThread clientThread = new ClientThread(SOCKs);
			}
		} catch (Exception e) {

		}
	}
	
		/**
		 * 
	 	* @author Mahsa
	 	*
	 	*/

	/**
	 * Purpose:
	 * 		This class extends timer task to be scheduled.
	 */
	public class TimerScheduledPeriod extends TimerTask {
		/**
		 * Purpose:
		 *  	This method performs the task as it runs connectClientwithUserNames method in every 1000 milliseconds.
		 */
		public void run() {
			connectClientwithUserNames();
		}
	}
		/**
		 * Purpose:
		 	* InputStreamReader, BufferedReader, OutputStreamWriter and BufferedWriter class objects are instantiated. 
		 	* used to retrieve input from the client and to send output to the client.
		*/
	class ClientThread extends Thread {				
		BufferedReader BR = null;		
		BufferedWriter BW = null;
		@SuppressWarnings("unused")			
		public ClientThread(Socket client) {
			try {
				if (client != null && !client.isClosed()) {					
					InputStreamReader IR = new InputStreamReader(client.getInputStream());
					OutputStreamWriter OW = new OutputStreamWriter(client.getOutputStream());
					BR = new BufferedReader(IR);
					BW = new BufferedWriter(OW);
					char[] cbuf = new char[250];
					BR.read(cbuf, 0, 250);   
					if (cbuf == null) {
						return;
					}
					int count = 0;
					String loginName = "";
					for (int i = 0; i < 250; i++) {
						if (cbuf[i] != '\u0000') {    
							count++;
							loginName += cbuf[i];
						}
					}
					System.out.println("New connection has been confirmed with: " + loginName + " on " + client);
					clientSocketUserMap.put(loginName.trim(), client);  
					start();
					connectClientwithUserNames();   ////SEND USER NAMES TO CONNECTING CLIENT
				}
			} catch (Exception e) {
			}
		}
		@SuppressWarnings("unused")
		/**
		 * The thread run method to receive message from the clients and store them to the map messageChatRoomMap.
		 * Message is extracted from the string separated by '-'. 
		 * If message has prefix 'changeroom:', it will just change the the chatRoom.
		 * Else, it will split the message to string array
		 */
		public void run() {
			while (true) {
				try {
					char[] cbuf = new char[250];
					BR.read(cbuf, 0, 250);
					if (cbuf == null) {
						return;
					}
					int count = 0;
					String buff = "";
					for (int j = 0; j < 250; j++) {
						if (cbuf[j] != '\u0000') {
							count++;
							buff = buff + cbuf[j];
						}
					}
					if (buff.startsWith("changeroom:")) {
						buff = buff.replace("changeroom:", "");
						String[] chatRoom = buff.split("-");
						chatRoomUserMap.put(chatRoom[0], chatRoom[1]);
						System.out.println(chatRoom[0] + " just switched into the chat room " + chatRoom[1]);
					} else {
						String[] messages = buff.split("-");
						String loginUser = messages[0];
						String chatRoomNumber = messages[1];
						String textChat = messages[2];
						System.out.println("Message from user [" + loginUser + " ] for room " + chatRoomNumber + " : " + textChat);
						chatRoomNumber = chatRoomNumber.trim();
						List<String> messageList;
						if (messageChatRoomMap.containsKey(chatRoomNumber)) {
							messageList = messageChatRoomMap.get(chatRoomNumber);
							messageList.add("message:" + loginUser + " said:" + textChat);
						} else {
							messageList = new ArrayList<String>();
							messageList.add("message:" + loginUser + " said:" + textChat);
						}
						messageChatRoomMap.put(chatRoomNumber, messageList);
					}
				} catch (Exception e) {
					break;
				}
			}
		}
	};
	/**
	""""
	Purpose:
		It iterates elements in a clientSocketUserMap and sends the userNames and messages to each client via BufferedWriter.
		Once client is disconnected, it will remove the key from the clientSocketUserMap., and once get connected it provides last open chatRoom for the user.
	Pre-conditions:
    	:param null 
	Return
    	:return: void
	""""
 	*/
	public static void connectClientwithUserNames() {
		try {
			if (clientSocketUserMap != null) {     
				for (String key : clientSocketUserMap.keySet()) {
					try {
						Socket SOCK = clientSocketUserMap.get(key);
						OutputStreamWriter OWC = new OutputStreamWriter(SOCK.getOutputStream());
						BufferedWriter BWC = new BufferedWriter(OWC);
						if (chatRoomUserMap.containsKey(key)) {
							if (messageChatRoomMap.containsKey(chatRoomUserMap.get(key))) {
								List<String> chatList = messageChatRoomMap.get(chatRoomUserMap.get(key));
								String msgClient = "";
								for (int i = 0; i < chatList.size(); i++) {
									msgClient = msgClient + chatList.get(i) + ",";
								}
								BWC.write(msgClient, 0, msgClient.length());
								BWC.flush();
							}
						}
					} catch (Exception e) {
						System.out.println("Diconnecting..." + key);
						System.out.println(key + " disconnected");
						clientSocketUserMap.remove(key);
					}
				}
			}
		} catch (Exception e) {
			
		}
	}
}
