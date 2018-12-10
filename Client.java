import java.net.*;
import java.io.*;
import java.util.*;

public class Client extends Thread {

	public final int BACKLOG = 10;

	private ServerSocket serverSocket;
	private Socket socket;
	private boolean hasStopped;
	private ObjectOutputStream out;
	private MessageHandler handler;

	public Client() {
		this(null);
	}

	public Client(MessageHandler h) {
		hasStopped = false;
		handler = h;
	}

	// Si connette ad un determinato host, se non e' raggiungibile
	// aspetta su un altro thread
	public void connectTo(String host, int port) {
		new Thread(()->{
			while(true) {
				try {
					socket = new Socket(host, port);
					out = new ObjectOutputStream(socket.getOutputStream());
					break;
				} catch(IOException exception) {
					System.out.println("--Impossibile connettersi all'host riprovo...");
					socket = null;
					out = null;
				}
			}
			startClient();
		}).start();
	}

	private void startClient() {
		super.setDaemon(true);
		super.start();
	}

	// Aspetta che qualcuno si connetta (1 solo)
	public void listen(int port) {
		// Aspetto su un altro thread così il chiamante non si blocca
		// sulla accept()
		new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						serverSocket = new ServerSocket(port);
						break;
					} catch(IOException exception) {
						System.out.println("--Impossibile aprire server socket");
					}
				}
				while(true) {
					try {
						socket = serverSocket.accept();
						out = new ObjectOutputStream(socket.getOutputStream());
						break;
					} catch(IOException exception) {
						System.out.println("--Impossibile stabilire connection con host, ascolto...");
						serverSocket = null;
						socket = null;
						out = null;
					}
				}
				startClient();
			}
		}).start();
	}

	public synchronized boolean hasStopped() {
		return hasStopped;
	}

	// Controlla se ci sono messaggi
	public void run() {
		try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
			while(true) {
				String message = (String) in.readObject();
				if (handler != null)
					handler.handle(message);
				else
					System.out.println(message);
			}
		} catch(IOException exception) {
			System.out.println("--Errore di comunicazione con host");
		} catch(ClassNotFoundException exception) {
			System.out.println("--Errore di conversione in stringa");
		} finally {
			out = null;
			socket = null;
			serverSocket = null;
			synchronized(this) {
				hasStopped = true;
			}
		}
	}

	// Manda un messaggio
	public void send(String message) {
		if(out == null)
			return;
		try {
			out.writeObject(message);
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Client client = new Client(new MessageHandler(){
			public void handle(String message) {
				System.out.println(message);
			}
		});
		Scanner scanner = new Scanner(System.in);

		if (args.length == 0) return; // Utilizzo senza argomenti errato
		// Se si specifica solo la porta allora si mette in ascolto
		else if (args.length == 1)
			client.listen(Integer.parseInt(args[0]));
		// Se si specificano ip e porta tenta di connettersi a quell'host
		else if (args.length == 2)
			client.connectTo(args[0], Integer.parseInt(args[1]));

		while(!client.hasStopped()) {
			client.send(scanner.nextLine());
		}
	}

}
