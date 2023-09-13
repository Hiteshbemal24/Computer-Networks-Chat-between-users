import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class Server {
    private static ArrayList<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(3333);

            Thread sendThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String serverMessage = scanner.nextLine();
                    broadcast("Server: " + serverMessage);
                }
            });
            sendThread.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientWriters.add(writer);

                Thread clientThread = new Thread(new ClientHandler(clientSocket, writer));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;

        public ClientHandler(Socket socket, PrintWriter writer) {
            this.clientSocket = socket;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("Sending file")) {
                        receiveFile(message, reader);
                    } else {
                        System.out.println("Received: " + message);
                        broadcast("Client: " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void receiveFile(String message, BufferedReader reader) {
            try {
                String fileName = message.substring("Sending file: ".length());
                File file = new File(fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                String data;
                while (!(data = reader.readLine()).equals("FileSent")) {
                    byte[] bytes = Base64.getDecoder().decode(data);
                    fileOutputStream.write(bytes);
                }

                fileOutputStream.close();
                System.out.println("File received: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
            writer.flush();
        }
    }
}
