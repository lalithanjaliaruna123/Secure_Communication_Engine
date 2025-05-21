import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyGen {
    public static void main(String[] args) {
        try {
            // Generate RSA key pair (2048-bit)
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            PublicKey publicKey = pair.getPublic();
            PrivateKey privateKey = pair.getPrivate();

            // Save the public key to a file
            try (FileOutputStream pubOut = new FileOutputStream("receiver_public.key")) {
                pubOut.write(publicKey.getEncoded());
                System.out.println("Public key saved to receiver_public.key");
            }

            // Save the private key to a file
            try (FileOutputStream privOut = new FileOutputStream("receiver_private.key")) {
                privOut.write(privateKey.getEncoded());
                System.out.println("Private key saved to receiver_private.key");
            }

        } catch (Exception e) {
            System.err.println("Key generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
