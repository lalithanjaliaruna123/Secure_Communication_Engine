import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.UUID;

public class Hello extends Application {
    private TextArea inputArea = new TextArea();
    private TextField nicknameField = new TextField("Anonymous");
    private TextArea statusArea = new TextArea();
    private Button sendBtn = new Button("Send Message");
    private Button sendFileBtn = new Button("Send File");

    private static final String RECEIVER_IP = "192.168.56.1";
    private static final int RECEIVER_PORT = 8766;

    private PublicKey receiverPublicKey;

    @Override
    public void start(Stage stage) {
        Label nickLabel = new Label("Your Nickname:");
        Label messageLabel = new Label("Enter message to send:");

        sendBtn.setOnAction(e -> new Thread(() -> {
            try { sendSecure(false, null); }
            catch (Exception ex) { ex.printStackTrace(); appendStatus("Error: " + ex.getMessage()); }
        }).start());

        sendFileBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                new Thread(() -> {
                    try { sendSecure(true, file); }
                    catch (Exception ex) { ex.printStackTrace(); appendStatus("File send error: " + ex.getMessage()); }
                }).start();
            }
        });

        statusArea.setEditable(false);
        VBox root = new VBox(10, nickLabel, nicknameField, messageLabel, inputArea, sendBtn, sendFileBtn, statusArea);
        root.setPadding(new Insets(10));
        stage.setScene(new Scene(root, 600, 450));
        stage.setTitle("Sender - Secure Communication");
        stage.show();

        try {
            receiverPublicKey = loadPublicKey("D:/4610/hackxtreme/receiver/receiver_public.key");
            appendStatus("Receiver public key loaded.");
        } catch (Exception e) {
            appendStatus("Failed to load receiver public key: " + e.getMessage());
        }
    }

    private void appendStatus(String text) {
        Platform.runLater(() -> statusArea.appendText(text + "\n"));
    }

    private void sendSecure(boolean isFile, File file) throws Exception {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) nickname = "Anonymous";

        byte[] data;
        if (isFile) data = java.nio.file.Files.readAllBytes(file.toPath());
        else {
            String msg = inputArea.getText();
            if (msg.isEmpty()) { appendStatus("Message is empty, nothing sent."); return; }
            data = msg.getBytes(StandardCharsets.UTF_8);
        }

        String messageId = UUID.randomUUID().toString();
        long timestamp = Instant.now().toEpochMilli();

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey aesKey = keyGen.generateKey();
        byte[] iv = new byte[16]; new SecureRandom().nextBytes(iv);

        Cipher rsaCipher = Cipher.getInstance("RSA"); rsaCipher.init(Cipher.ENCRYPT_MODE, receiverPublicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] encryptedData = aesCipher.doFinal(data);

        Mac hmac = Mac.getInstance("HmacSHA256"); hmac.init(new SecretKeySpec(aesKey.getEncoded(), "HmacSHA256"));
        byte[] hmacBytes = hmac.doFinal(encryptedData);

        appendStatus("Connecting to receiver...");
        try (Socket socket = new Socket(RECEIVER_IP, RECEIVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(nickname);
            out.writeObject(messageId);
            out.writeLong(timestamp);
            out.writeObject(new byte[0]);
            out.writeBoolean(isFile);
            out.writeObject(encryptedAesKey);
            out.writeObject(iv);
            out.writeObject(encryptedData);
            out.writeObject(hmacBytes);
            out.flush();
            appendStatus((isFile?"File":"Message")+" sent securely!");
        }
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        byte[] keyBytes = java.nio.file.Files.readAllBytes(new File(path).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public static void main(String[] args) { launch(); }
}
