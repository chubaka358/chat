package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
	private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

	private static class Handler extends Thread{
		private Socket socket;

		private Handler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			SocketAddress socketAddress =  socket.getRemoteSocketAddress();
			System.out.println(socketAddress);
			ConsoleHelper.writeMessage("Установлено соединение с удаленным сервером.");
			try {
				Connection connection = new Connection(socket);
				String userName = serverHandshake(connection);
				sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
				notifyUsers(connection, userName);
				serverMainLoop(connection, userName);
				connectionMap.remove(userName);
				sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
				ConsoleHelper.writeMessage("Соединение с удаленным сервисом закрыто.");
			} catch (IOException e) {
				ConsoleHelper.writeMessage("Упс, IOException");
			} catch (ClassNotFoundException e){
				ConsoleHelper.writeMessage("Упс, ClassNotFoundException");
			}
		}

		private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
			while(true) {
				connection.send(new Message(MessageType.NAME_REQUEST));
				Message clientAnswer = connection.receive();
				if ( (clientAnswer.getType() == MessageType.USER_NAME) && (!clientAnswer.getData().isEmpty()) &&
						(!connectionMap.containsKey(clientAnswer.getData())) ){
					connectionMap.put(clientAnswer.getData(), connection);
					connection.send(new Message(MessageType.NAME_ACCEPTED));
					return clientAnswer.getData();
				}
			}
		}

		private void notifyUsers(Connection connection, String userName) throws IOException{
			for(Map.Entry<String, Connection> entry : connectionMap.entrySet()){
				if (entry.getKey() != userName){
					connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
				}
			}
		}

		private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
			while (true){
				Message receivedMessage = connection.receive();
				if (receivedMessage.getType() == MessageType.TEXT){
					sendBroadcastMessage(new Message(MessageType.TEXT,
							String.format("%s: %s", userName, receivedMessage.getData())));
				} else {
					ConsoleHelper.writeMessage("Ого, ошибка! Сообщение не является текстовым:С");
				}
			}
		}
	}

	public static void sendBroadcastMessage(Message message){
		for (Connection connection : connectionMap.values()) {
			try {
				connection.send(message);
			} catch (IOException e) {
				System.out.println("Не получилось отправить сообщение:(");
			}
		}
	}

	public static void main(String[] args) {
		int port = ConsoleHelper.readInt();
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ConsoleHelper.writeMessage("Сервер запущен");
			while (true){
				Socket clientSocket = serverSocket.accept();
				new Handler(clientSocket).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
