package chat.client;

import chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client{
	public class BotSocketThread extends SocketThread{
		@Override
		protected void clientMainLoop() throws IOException, ClassNotFoundException {
			sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, " +
					"год, время, час, минуты, секунды.");
			super.clientMainLoop();
		}

		@Override
		protected void processIncomingMessage(String message) {
			ConsoleHelper.writeMessage(message);
			if (!message.contains(": ") || message == null)
				return;
			String userName = message.split(": ")[0];
			String messageText = message.split(": ")[1];
			StringBuilder sb = new StringBuilder(String.format("Информация для %s: ", userName));
			Date date = Calendar.getInstance().getTime();
			if (messageText.equals("дата")){
				sb.append(new SimpleDateFormat("d.MM.YYYY").format(date));
			} else if (messageText.equals("день")){
				sb.append(new SimpleDateFormat("d").format(date));
			} else if (messageText.equals("месяц")){
				sb.append(new SimpleDateFormat("MMMM").format(date));
			} else if (messageText.equals("год")){
				sb.append(new SimpleDateFormat("YYYY").format(date));
			} else if (messageText.equals("время")){
				sb.append(new SimpleDateFormat("H:mm:ss").format(date));
			} else if (messageText.equals("час")){
				sb.append(new SimpleDateFormat("H").format(date));
			} else if (messageText.equals("минуты")){
				sb.append(new SimpleDateFormat("m").format(date));
			} else if (messageText.equals("секунды")){
				sb.append(new SimpleDateFormat("s").format(date));
			} else {
				return;
			}
			sendTextMessage(sb.toString());
		}
	}

	@Override
	protected SocketThread getSocketThread() {
		return new BotSocketThread();
	}

	@Override
	protected boolean shouldSendTextFromConsole() {
		return false;
	}

	@Override
	protected String getUserName() {
		return "date_bot_" + (int)(Math.random() * 100);
	}

	public static void main(String[] args) {
		BotClient botClient = new BotClient();
		botClient.run();
	}
}
