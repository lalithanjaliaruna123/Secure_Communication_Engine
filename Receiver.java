import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Receiver {
    private static final int PORT = 8766;
    private static final String PRIVATE_KEY_FILE = "receiver_private.key";

    private PrivateKey privateKey;
    private final Set<String> messageIds = new HashSet<>();

    public static void main(String[] args) {
        Receiver receiver = new Receiver();
        try {
            receiver.privateKey = receiver.loadPrivateKey(PRIVATE_KEY_FILE);
            System.out.println("[*] Private key loaded successfully.");
            receiver.startServer();
        } catch (Exception e) {
            System.err.println("[!] Failed to start receiver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[*] Receiver listening on port " + PORT);
        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                System.out.println("[*] Client connected: " + clientSocket.getInetAddress());

                String senderNickname = (String) in.readObject();
                String messageId = (String) in.readObject();
                long timestamp = in.readLong();
                byte[] signature = (byte[]) in.readObject();
                boolean isFile = in.readBoolean();

                byte[] encryptedAesKey = (byte[]) in.readObject();
                byte[] iv = (byte[]) in.readObject();
                byte[] encryptedData = (byte[]) in.readObject();
                byte[] receivedHmac = (byte[]) in.readObject();

                if (messageIds.contains(messageId)) { continue; }
                long now = Instant.now().toEpochMilli();
                if (Math.abs(now - timestamp) > 300_000) { continue; }

                Cipher rsaCipher = Cipher.getInstance("RSA"); rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
                SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

                Mac hmac = Mac.getInstance("HmacSHA256"); hmac.init(new SecretKeySpec(aesKey.getEncoded(), "HmacSHA256"));
                if (!MessageDigest.isEqual(receivedHmac, hmac.doFinal(encryptedData))) { continue; }

                Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
                byte[] plain = aesCipher.doFinal(encryptedData);

                if (isFile) {
                    File outFile = new File("received_" + messageId);
                    try (FileOutputStream fos = new FileOutputStream(outFile)) { fos.write(plain); }
                    System.out.printf("[%s] File saved: %s%n", senderNickname, outFile.getName());
                } else {
                    System.out.printf("[%s] %s%n", senderNickname, new String(plain, StandardCharsets.UTF_8));
                }

                messageIds.add(messageId);
                logMessage(senderNickname, messageId, timestamp, isFile);

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void logMessage(String sender, String msgId, long timestamp, boolean isFile) {
        try (PrintWriter out = new PrintWriter(new FileWriter("receiver_log.txt", true))) {
            out.printf("%s | %s | %s | %s%n", Instant.ofEpochMilli(timestamp), sender, msgId, isFile?"<file>":"<text>");
        } catch (IOException ignored) {}
    }

    private PrivateKey loadPrivateKey(String filepath) throws Exception {
        byte[] keyBytes = java.nio.file.Files.readAllBytes(new File(filepath).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
