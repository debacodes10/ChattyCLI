import socket
import threading
import signal
import sys

class ChatServer:
    def __init__(self, host='127.0.0.1', port=12345):
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.bind((host, port))
        self.server.listen(5)
        self.clients = {}
        self.running = True
        print(f"Server started on {host}:{port}")

    def broadcast(self, message, client):
        for c in self.clients.keys():
            if c != client:
                try:
                    c.send(message)
                except:
                    c.close()
                    del self.clients[c]

    def handle_client(self, client):
        while self.running:
            try:
                message = client.recv(1024).decode()
                if message:
                    if client not in self.clients:
                        # Check for unique username
                        if message in self.clients.values():
                            client.send("Username already taken. Disconnecting...".encode())
                            client.close()
                            break
                        self.clients[client] = message  # First message is the username
                        welcome_message = f"{message} has joined the chat!"
                        self.broadcast(welcome_message.encode(), client)
                        print(welcome_message)
                    else:
                        username = self.clients[client]
                        if message == '/shutdown':
                            self.running = False
                            break
                        formatted_message = f"{username}: {message}"
                        self.broadcast(formatted_message.encode(), client)
                        print(formatted_message)
            except:
                username = self.clients[client]
                goodbye_message = f"{username} has left the chat!"
                print(goodbye_message)
                self.broadcast(goodbye_message.encode(), client)
                client.close()
                del self.clients[client]
                break

    def run(self):
        print("Server running...")
        while self.running:
            client, addr = self.server.accept()
            print(f"New connection from {addr}")
            threading.Thread(target=self.handle_client, args=(client,)).start()

    def shutdown(self):
        self.running = False
        self.server.close()
        print("Server shut down.")

def signal_handler(signal, frame):
    print('You pressed Ctrl+C!')
    server.shutdown()
    sys.exit(0)

if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal_handler)
    server = ChatServer()
    server.run()
