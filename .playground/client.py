import socket
import threading

class ChatClient:
    def __init__(self, username, host='127.0.0.1', port=12345):
        self.client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client.connect((host, port))
        self.username = username
        self.client.send(self.username.encode())
        threading.Thread(target=self.receive_messages).start()

    def receive_messages(self):
        while True:
            try:
                message = self.client.recv(1024).decode()
                if message:
                    print(message)
                if message == "Username already taken. Disconnecting...":
                    print("Disconnected from server")
                    self.client.close()
                    break
            except:
                print("Disconnected from server")
                self.client.close()
                break

    def send_message(self, message):
        self.client.send(message.encode())

if __name__ == "__main__":
    username = input("Enter your username: ")
    client = ChatClient(username)
    print("Enter 'exit' to leave the chat")
    while True:
        msg = input()
        if msg == 'exit':
            client.client.close()
            break
        client.send_message(msg)
