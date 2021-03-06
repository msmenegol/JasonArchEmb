import socket
import sys

JAVAPORT = 6969

def decodeSock(str, port):
    return {
        JAVAPORT : str[:-1] #JAVAPORT
    }[port]

def encodeSock(str, port):
    return {
        JAVAPORT : str + '\n'#JAVAPORT
    }[port]

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the port
server_address = ('localhost', JAVAPORT)
#print >>sys.stderr, 'starting up on %s port %s' % server_address
sock.bind(server_address)

# Listen for incoming connections
sock.listen(1)

it=0

while True:
    # Wait for a connection
    print >>sys.stderr, 'waiting for a connection'
    connection, client_address = sock.accept()

    try:
        print >>sys.stderr, 'connection from', client_address

        # Receive the data in small chunks and retransmit it
        while True:
            data = connection.recv(1024)
            print >>sys.stderr, 'received "%s"' % decodeSock(data, JAVAPORT)
            if data:
                print >>sys.stderr, 'sending data back to the client'
                connection.sendall(encodeSock(data, JAVAPORT))
                connection.sendall(encodeSock("lowBat(5)",JAVAPORT))
                while it<100:
                    print >>sys.stderr, 'sending position'
                    msg = "position(" + str(it) + "," + str(it) + "," + str(it) + ")"
                    print >>sys.stderr, msg
                    connection.sendall(encodeSock(msg, JAVAPORT))
                    it = it+10
            else:
                print >>sys.stderr, 'no more data from', client_address
                it=0
                break

    finally:
        # Clean up the connection
        connection.close()
