import java.util.*;

public class Sender {

	public static void main(String[] args) {
		if (args.length == 0)
			return;
		Scanner scanner = new Scanner(System.in);
		Client chatter = new Client(new MessageHandler(){
			public void handle(String message) {
				return;
			}
		});
		chatter.connectTo("127.0.0.1", Integer.parseInt(args[0]));
		while(true) {
			System.out.print("> ");
			chatter.send(scanner.nextLine());
		}
	}

}