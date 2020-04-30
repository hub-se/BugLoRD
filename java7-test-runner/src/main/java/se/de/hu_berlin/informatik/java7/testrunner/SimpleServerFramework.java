package se.de.hu_berlin.informatik.java7.testrunner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Simple server/client framework that starts a server socket and a listener
 * thread on the receiving end and one may then send data through a socket
 * connection to this server. The receiving application may block until it gets
 * correct data, while the server listener thread runs in the background.
 *
 * <p>
 * The protocol used consists of: 1. the client sending data, 2. the server
 * sending a response based on the data received, 3. the client either accepts
 * or rejects the response, and 4. the server makes the correct data available
 * and sends an acceptance message to the client to inform it that everything
 * went correctly.
 *
 * <p>
 * In the case of errors, the data should be tried to sent again.
 *
 * @author Simon
 */
public class SimpleServerFramework {

    final private static int TIMEOUT = 120000;

    final private static byte ACCEPT = 0;

    final private static byte NULL_DATA = 2;
    final private static byte NORMAL_DATA = 3;

    final private static byte RESET = 4;

    public static int getFreePort() {
        return getFreePort(new Random().nextInt(60536) + 5000);
    }

    public static int getFreePort(final int startPort) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(null);
        } catch (UnknownHostException e1) {
            // should not happen
            return -1;
        }
        // port between 0 and 65535 !
        Random random = new Random();
        int currentPort = startPort;
        int count = 0;
        while (true) {
            if (count > 1000) {
                return -1;
            }
            ++count;
            try {
                new Socket(inetAddress, currentPort).close();
            } catch (final IOException e) {
                // found a free port
                break;
            } catch (IllegalArgumentException e) {
                // should only happen on first try (if argument wrong)
            }
            currentPort = random.nextInt(60536) + 5000;
        }
        return currentPort;
    }

    public static <T extends Serializable, R extends Serializable> ServerSideListener<T, R> startServer() {
        return _startServer(getFreePort());
    }

    public static <T extends Serializable, R extends Serializable> ServerSideListener<T, R> startServer(int port) {
        return _startServer(getFreePort(port));
    }

    private static <T extends Serializable, R extends Serializable> ServerSideListener<T, R> _startServer(int port) {
        try {
            ServerSocket socket = new ServerSocket(port);
            // set a timeout to stop blocking in case of errors occurring...
            // socket.setSoTimeout(10000);
            return startServerListener(socket);
        } catch (Exception e) {
            System.err.println("server error: " + e.getMessage());
        }

        return null;
    }

    private static <T extends Serializable, R extends Serializable> ServerSideListener<T, R> startServerListener(
            ServerSocket socket) {
        ServerSideListener<T, R> serverSideListener = new ServerSideListener<>(socket);
        serverSideListener.run();
        return serverSideListener;
    }

    public static class ServerSideListener<T extends Serializable, R extends Serializable> {

        final private ServerSocket serverSocket;
        private final Semaphore lock = new Semaphore(1);

        private Thread runningThread = null;
        private T data;
        private volatile boolean isShutdown = false;
        private volatile boolean hasNewData = false;

        public ServerSideListener(ServerSocket serverSocket) {
            this.serverSocket = Objects.requireNonNull(serverSocket);
        }

        // set protected to prevent unintended usage
        protected void run() {
            if (this.runningThread == null || !this.runningThread.isAlive()) {
                this.runningThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listenOnSocket(serverSocket);
                    }
                });
                this.runningThread.start();
            }
        }

        @SuppressWarnings("unchecked")
        private void listenOnSocket(ServerSocket serverSocket) {
            while (!isShutdown) {
                // Create the Client Socket
                try (Socket clientSocket = serverSocket.accept()) {
                    // the timeout only matters AFTER a connection is
                    // established
                    clientSocket.setSoTimeout(TIMEOUT);

                    // Log.out(this, "Server Socket Extablished...");
                    // Create input and output streams to client
                    ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());

                    lock.acquireUninterruptibly();
                    try {
                        /*-----------------------------------------
                         * server expects to receive data
                         * --------------------------------------*/
                        // Log.out("server", "reading data from port %d...",
                        // clientSocket.getLocalPort());
                        /* Retrieve data */
                        byte status = inFromClient.readByte();
                        if (status == RESET) {
                            continue;
                        }
                        if (status == NULL_DATA) {
                            this.data = null;
                        } else {
                            this.data = (T) inFromClient.readObject();
                        }

                        /*-----------------------------------------
                         * server sends response to client
                         * based on data
                         * --------------------------------------*/
                        // Log.out("server", "writing data to port %d...",
                        // clientSocket.getLocalPort());
                        if (data == null) {
                            /* we received null data */
                            outToClient.writeByte(NULL_DATA);
                        } else {
                            /*
                             * we received data other than null (TODO: checksum
                             * or something...?)
                             */
                            outToClient.writeByte(NORMAL_DATA);
                        }
                        outToClient.flush();

                        /*-----------------------------------------
                         * server expects ACC response from client;
                         * on reception, the server knows that the
                         * received data was (probably) correct
                         * --------------------------------------*/
                        // Log.out("server", "reading data from port %d...",
                        // clientSocket.getLocalPort());
                        /* Retrieve data */
                        status = inFromClient.readByte();
                        if (status == RESET) {
                            continue;
                        }

                        if (status == ACCEPT) {
                            /*-----------------------------------------
                             * server sends ACC response to client and
                             * may now notify any waiting threads that
                             * there is new data
                             * --------------------------------------*/
                            outToClient.writeByte(ACCEPT);
                            outToClient.flush();
                            // tell any waiting threads that there is new
                            // data...
                            hasNewData = true;
                        } else {
                            /*-----------------------------------------
                             * should not occur, but send reset message
                             * --------------------------------------*/
                            outToClient.writeByte(RESET);
                            outToClient.flush();
                        }
                    } catch (SocketTimeoutException e) {
                        System.err.println("server timeout!");
                        outToClient.writeByte(RESET);
                        outToClient.flush();
                    } finally {
                        lock.release();
                    }
                } catch (Exception e) {
                    // if any exception occurred, the client should now try to
                    // send the data again
                    System.err.println("server error: " + e.getMessage());
                }
            }

        }

        public void shutDown() {
            if (this.runningThread != null && this.runningThread.isAlive()) {
                isShutdown = true;
                boolean received = false;
                int count = 0;
                while (!received && count < 5) {
                    ++count;
                    received = sendToServer(null, serverSocket.getLocalPort(), 1);
                }
                if (received) {
                    while (this.runningThread.isAlive()) {
                        try {
                            this.runningThread.join();
                        } catch (InterruptedException e) {
                            // wait until finished
                        }
                    }
                } else {
                    // just don't wait?...
                }
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                // don't care
            }
        }

        public T getNewData() {
            return getNewData(Long.MAX_VALUE);
        }

        public T getNewData(long timeout) {
            // Log.out(this, "waiting for data...");
            // wait for new data if necessary (should already be available)
            boolean noTimeoutOccurred = true;
            T result = null;
            while (!hasNewData && noTimeoutOccurred) {
                try {
                    noTimeoutOccurred = lock.tryAcquire(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // try again
                } finally {
                    if (!hasNewData && noTimeoutOccurred) {
                        lock.release();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            // do nothing...
                        }
                    }
                }
            }
            // even if a timeout occurred, new data may have been made available
            // just in this moment...
            if (hasNewData) {
                result = data;
                data = null;
                hasNewData = false;
                if (noTimeoutOccurred) {
                    // only need to release the lock
                    // in this case...
                    lock.release();
                }
                return result;
            } else {
                // no new data
                return null;
            }
        }

        public boolean hasNewData() {
            return hasNewData;
        }

        public T getLastData() {
            hasNewData = false;
            return data;
        }

        public void resetListener() {
            hasNewData = false;
        }

        public int getServerPort() {
            return serverSocket.getLocalPort();
        }

    }

    public static <T extends Serializable, R extends Serializable> boolean sendToServer(T data, int port,
                                                                                        int maxTryCount) {
        int count = 0;
        while (count < maxTryCount) {
            ++count;
            // Create the socket
            try (Socket clientSocket = new Socket((String) null, port)) {
                clientSocket.setSoTimeout(TIMEOUT);
                // Log.out("client", "Client Socket initialized...");
                // Create the input & output streams to the server
                ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());

                try {
                    /*-----------------------------------------
                     * client sends data to server
                     * --------------------------------------*/
                    // Log.out("client", "writing data to port %d...", port);
                    /* Send the Message Object to the server */
                    if (data == null) {
                        outToServer.writeByte(NULL_DATA);
                    } else {
                        outToServer.writeByte(NORMAL_DATA);
                        outToServer.writeObject(data);
                    }
                    outToServer.reset();
                    outToServer.flush();

                    /*-----------------------------------------
                     * client expects respective response
                     * from server
                     * --------------------------------------*/
                    // Log.out("client", "reading data from port %d...", port);
                    /* Retrieve the status byte from server */
                    byte status = inFromServer.readByte();
                    if (status == RESET) {
                        continue;
                    }

                    // Log.out("client", "read data from port %d...", port);
                    if ((data == null && status == NULL_DATA) || (data != null && status == NORMAL_DATA)) {
                        /*-----------------------------------------
                         * client acknowledges that server has
                         * received the correct data
                         * --------------------------------------*/
                        outToServer.writeByte(ACCEPT);
                        outToServer.flush();
                    } else {
                        /*-----------------------------------------
                         * client wants to start the process over
                         * --------------------------------------*/
                        System.err.println("client error sending correct data: try " + count);
                        outToServer.writeByte(RESET);
                        outToServer.flush();
                        continue;
                    }

                    /*-----------------------------------------
                     * client expects ACC response from server,
                     * so that it can be sure that the server
                     * provided the new data
                     * --------------------------------------*/
                    // Log.out("client", "reading data from port %d...", port);
                    /* Retrieve the status byte from server */
                    status = inFromServer.readByte();
                    if (status == RESET) {
                        continue;
                    }

                    if (status == ACCEPT) {
                        return true;
                    }

                } catch (SocketTimeoutException e) {
                    System.err.println("client try " + count + ", timeout!");
                    outToServer.writeByte(RESET);
                    outToServer.flush();
                }
            } catch (Exception e) {
                System.err.println("client try " + count + ", error: " + e.getMessage());
            }
        }

        return false;
    }
}
