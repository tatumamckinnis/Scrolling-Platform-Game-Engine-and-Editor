/**
 * This is the server supports multiplayer games. Run this application to start the server.
 *
 * @author Aksel Bell
 */
const WebSocket = require("ws");
const {parse} = require("node:url");

class Server {
  constructor(port, xmlFilepath) {
    this.port = port;
    this.xmlPath = xmlFilepath;
    this.clients = new Set();
    this.webserver = new WebSocket.Server({ port: this.port });

    console.log("Server running on:", this.port);

    this.webserver.on('connection', (socket, request) => {
      const parsedUrl = parse(request.url, true);
      const clientPath = parsedUrl.query.filepath;

      this.handleConnection(socket, clientPath);
    });  }

  handleMessage(socket, message) {
    console.log('Received: ' + message);

    for (const client of this.clients) {
      if (client !== socket && client.readyState === WebSocket.OPEN) {
        client.send("" + message);
      }
    }
  }

  handleDisconnect(socket) {
    this.clients.delete(socket);
    console.log('Client disconnected');
  }

  handleConnection(socket, filepath) {
    if(filepath !== this.xmlPath) {
      socket.send(JSON.stringify({
        type: "error",
        message: String("Attempting to join wrong game type.")
      }));

      socket.close();
      return;
    }

    console.log('Client connected!');
    this.clients.add(socket);

    socket.on('message', (message) => this.handleMessage(socket, message));
    socket.on('close', () => this.handleDisconnect(socket));
  }
}

const PORT = process.argv[2];
const gamePath = process.argv[3];
new Server(parseInt(PORT), gamePath);