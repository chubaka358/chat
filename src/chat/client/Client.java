package chat.client;

import chat.Connection;
import chat.ConsoleHelper;
import chat.Message;
import chat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
	private volatile boolean clientConnected = false;

	protected Connection connection;

	public class SocketThread extends Thread{
		@Override
		public void run() {
			try {
				String serverAddress = getServerAddress();
				int serverPort = getServerPort();
				Socket socket = new Socket(serverAddress, serverPort);
				connection = new Connection(socket);
				clientHandshake();
				clientMainLoop();
			} catch (IOException | ClassNotFoundException e) {
				notifyConnectionStatusChanged(false);
			}
		}

		protected void processIncomingMessage(String message){
			ConsoleHelper.writeMessage(message);
		}

		protected void informAboutAddingNewUser(String userName){
			ConsoleHelper.writeMessage(userName + " присоединился к чату.");
		}

		protected void informAboutDeletingNewUser(String userName){
			ConsoleHelper.writeMessage(userName + " покинул чат.");
		}

		protected void notifyConnectionStatusChanged(boolean clientConnected){
			Client.this.clientConnected = clientConnected;
			synchronized (Client.this){
				Client.this.notify();
			}
		}

		protected void clientHandshake() throws IOException, ClassNotFoundException{
			while (true){
				Message message = connection.receive();
				if (message.getType() == MessageType.NAME_REQUEST){
					String userName = getUserName();
					Message answer = new Message(MessageType.USER_NAME, userName);
					connection.send(answer);
				} else if (message.getType() == MessageType.NAME_ACCEPTED) {
					notifyConnectionStatusChanged(true);
					break;
				} else {
					throw new IOException("Unexpected MessageType");
				}
			}
		}

		protected void clientMainLoop() throws IOException, ClassNotFoundException{
			while (true){
				Message message = connection.receive();
				if (message.getType() == MessageType.TEXT){
					processIncomingMessage(message.getData());
				} else if (message.getType() == MessageType.USER_ADDED) {
					informAboutAddingNewUser(message.getData());
				} else if (message.getType() == MessageType.USER_REMOVED) {
					informAboutDeletingNewUser(message.getData());
				} else {
					throw new IOException("Unexpected MessageType");
				}
			}
		}
	}

	protected String getServerAddress(){
		ConsoleHelper.writeMessage("Введите ip сервера");
		return ConsoleHelper.readString();
	}

	protected int getServerPort(){
		ConsoleHelper.writeMessage("Введите порт сервера");
		return ConsoleHelper.readInt();
	}

	protected String getUserName(){
		ConsoleHelper.writeMessage("Введите имя пользователя");
		return ConsoleHelper.readString();
	}

	protected boolean shouldSendTextFromConsole(){
		return true;
	}

	protected SocketThread getSocketThread(){
		return new SocketThread();
	}

	protected void sendTextMessage(String text){
		try {
			connection.send(new Message(MessageType.TEXT, text));
		} catch (IOException e) {
			clientConnected = false;
			ConsoleHelper.writeMessage("Произошла ошибка при отправке сообщения");
		}
	}

	public void run(){
		SocketThread socketThread = getSocketThread();
		socketThread.setDaemon(true);
		socketThread.start();
		try {
			synchronized (this) {
				wait();
			}
			if (clientConnected)
				ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
			else
				ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
			while (clientConnected){
				String messageText = ConsoleHelper.readString();
				if (messageText.equals("exit"))
					break;
				if(shouldSendTextFromConsole())
					sendTextMessage(messageText);
			}
		} catch (InterruptedException e) {
			ConsoleHelper.writeMessage("Упс, произошло InterruptedException");
		}
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.run();
	}
}
