package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void writeMessage(String message){
		System.out.println(message);
	}

	public static String readString(){
		while (true){
			try {
				String line = reader.readLine();
				return line;
			} catch (IOException e) {
				System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
			}
		}
	}

	public static int readInt(){
		while (true) {
			try {
				int a = Integer.parseInt(readString());
				return a;
			} catch (NumberFormatException e) {
				System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
			}
		}
	}
}
