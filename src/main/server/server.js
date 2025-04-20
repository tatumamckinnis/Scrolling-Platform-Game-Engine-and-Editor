/**
 * This is the server supports multiplayer games. Run this application to start the server.
 *
 * @author Aksel Bell
 */
const WebSocket = require("ws");

class Server {
  constructor(port = 3000) {
    this.port = port;
    this.clients = new Set();
    this.webserver = new WebSocket.Server({ port: this.port });

    console.log("Server running on:", this.port);

    this.webserver.on('connection', (socket) => this.handleConnection(socket));
  }

  handleMessage(socket, message) {
    console.log('Received: ' + message);

    for (const client of this.clients) {
      if (client !== socket && client.readyState === WebSocket.OPEN) {
        client.send(message);
      }
    }
  }

  handleDisconnect(socket) {
    this.clients.delete(socket);
    console.log('Client disconnected');
  }

  handleConnection(socket) {
    console.log('Client connected!');
    this.clients.add(socket);

    socket.on('message', (message) => this.handleMessage(socket, message));
    socket.on('close', () => this.handleDisconnect(socket));
  }
}