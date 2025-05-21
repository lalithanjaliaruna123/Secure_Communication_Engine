# 🔐 Secure Communication Engine

**Secure Communication Engine** is a Java-based system enabling secure message and file transmission over a network. It features a graphical sender built with JavaFX and a command-line receiver. The project uses AES encryption for confidentiality, RSA for key exchange, and HMAC for integrity verification.

---

## 📦 Project Components

### 🧑‍💻 Sender Application (GUI)
- Built using JavaFX
- Allows users to input messages or select files to send securely
- Encrypts content using AES-128 with random IVs
- Protects AES key using RSA public key encryption
- Verifies data integrity using HMAC-SHA256
- Transmits data to the receiver over a socket connection

### 🖥️ Receiver Application (Console)
- Listens on a configured port for incoming connections
- Decrypts the AES key using RSA private key
- Verifies HMAC before decrypting the message or file
- Displays plain text messages in the console
- Saves received files locally

### 🔐 Key Generation Script
- Generates RSA key pairs for secure asymmetric encryption
- Public key is shared with the sender
- Private key remains confidential with the receiver

---

## ⚙️ Requirements

- Java 11 or higher
- JavaFX (for GUI support on the sender)
- Pre-generated RSA key pair (public and private keys)
- Network access between sender and receiver machines

---

## 🚀 How to Use

1. **Generate RSA Key Pair**  
   Generate a 2048-bit RSA key pair and share the public key with the sender.

2. **Configure the Applications**  
   - Update file paths and IP/port in the source code if necessary.
   - Place the public key file in the expected location for the sender.
   - Ensure the receiver is running before sending any messages.

3. **Run the Receiver**  
   Start the receiver on the destination machine to listen for incoming encrypted messages or files.

4. **Launch the Sender**  
   Use the JavaFX GUI to input your nickname, write a message, or select a file and securely transmit it to the receiver.

---

## 🔐 Security Features

| Feature              | Implementation         |
|---------------------|------------------------|
| Encryption          | AES-128 (CBC mode)     |
| Key Exchange        | RSA-2048               |
| Integrity Check     | HMAC-SHA256            |
| Metadata            | Timestamp + UUID       |
| Asynchronous Sending| Background threads      |

---

## ⚠️ Known Limitations

- IP address and key file paths are hardcoded in the sender source code.
- No sender authentication or digital signatures (yet).
- No TLS — security is implemented at the application layer.
- Minimal input validation; not ready for production deployment.

---

## 📁 Repository Structure
/SecureCommunicationEngine
├── Hello.java # Sender (JavaFX)
├── Receiver.java # Receiver (console-based)
├── KeyGen.java # RSA key generation
├── receiver_public.key # RSA public key
├── receiver_private.key # RSA private key (keep secret)
└── README.md


---
Clone link:
git clone https://github.com/lalithanjaliaruna123/Secure_Communication_Engine.git

## 📜 License

This project is released under the MIT or Apache 2.0 license — choose the one that fits your needs.

---

## ✍️ Author

**Lalithanjaliaruna** – CSE-Cybersecurity student passionate about building secure, real-world applications.  
Built for exploration, learning, and practical cryptography implementation.

---

