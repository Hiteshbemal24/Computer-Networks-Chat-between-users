import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 3333);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            Scanner scanner = new Scanner(System.in);
            String input;
            while (true) {
                input = scanner.nextLine();
                if (input.equalsIgnoreCase("sendfile")) {
                    System.out.print("Enter file path: ");
                    String filePath = scanner.nextLine();
                    sendFile(writer, filePath);
                } else {
                    writer.println(input);
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(PrintWriter writer, String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            writer.println("Sending file: " + file.getName());
            writer.flush();

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                // writer.println(Base64.getEncoder().encodeToString(buffer));
                writer.flush();
            }

            writer.println("FileSent");
            writer.flush();

            fileInputStream.close();
            System.out.println("File sent successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
