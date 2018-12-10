import java.util.*;

public class Chat {

	private final String PROMPT = ">";

	private List messages;
	private int lastReadMessage;

	public Chat() {
		messages = new ArrayList();
		lastReadMessage = -1;
	}

	public synchronized void setMessage(String message, String sender) {
		messages.add(sender + PROMPT + " " + message);
		notifyAll();
	}

	public synchronized String getMessage() {
		// Finché non ci sono nuovi messaggi
		try {
			while(lastReadMessage == messages.size() - 1) {
				wait();
			}
		} catch(InterruptedException exception) {
			return "";
		}
		return (String) messages.get(++lastReadMessage);
	}

	public static void main(String[] args) {
		Chat chat = new Chat();
		Client client = new Client(new MessageHandler(){
			public void handle(String message) {
				/*String[] elems = message.split("|+^^?!");
				chat.setMessage(elems[1], elems[0]);*/
				chat.setMessage(message, "Nicco");
			}
		});
		Client sender = new Client(new MessageHandler(){
			public void handle(String message) {
				client.send(message);
				chat.setMessage(message, "Tu");
			}
		});

		if (args.length == 0) {
			System.out.println("Le opzioni sono:");
			System.out.println("java Chat port           --> si mette in ascolto su port");
			System.out.println("java Chat ip port        --> si connette a ip port");
			System.out.println("java Chat port1 port2    --> si mette in ascolto su port1 e attende un sender su port2");
			System.out.println("java Chat ip port1 port2 --> si connette a ip port1 e attende un sender su port2");
			return;
		}
		// Se si specifica solo la porta allora si mette in ascolto
		else if (args.length == 1)
			client.listen(Integer.parseInt(args[0]));
		// Se si specificano ip e porta tenta di connettersi a quell'host
		// oppure si specificano due porte una per ascolto e una per un sender
		else if (args.length == 2) {
			if (args[0].contains(".")) // Il primo argomento è un ip
				client.connectTo(args[0], Integer.parseInt(args[1]));
			else {
				client.listen(Integer.parseInt(args[0]));
				sender.listen(Integer.parseInt(args[1]));
			}
		}
		// Se si specifica un'ulteriore porta questa viene usata per associare un sender
		else if (args.length == 3) {
			client.connectTo(args[0], Integer.parseInt(args[1]));
			sender.listen(Integer.parseInt(args[2]));
		}

		// Mostro sul terminale i messaggi
		while(true) {
			System.out.println(chat.getMessage());
		}
	}

}
