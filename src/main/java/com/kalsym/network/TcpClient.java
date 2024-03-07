package com.kalsym.network;

import com.kalsym.security.CustomEncodeDecode;
import com.kalsym.utility.XMLReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;


/**
 *
 * @author Zeeshan Ali
 */
public class TcpClient {

    /**
     * Sends Request to Server and append \n\n with Message
     *
     * @param serverAddr
     * @param requestMsg
     * @param refId
     * @param timeoutConnectMs
     * @param timeoutSoMs
     * @param failedRetry
     * @return
     * @throws java.lang.Exception
     */
    public static RequestResult doBroadcast(String[] serverAddr,
            String requestMsg, String refId, int timeoutConnectMs,
            int timeoutSoMs, int failedRetry) throws Exception {
        RequestResult result = new RequestResult();
        String[] responseCode = new String[serverAddr.length];
        String[] responseString = new String[serverAddr.length];

        for (int i = 0; i < serverAddr.length; i++) {
            try {
                String replyMsg = "";
                int count = 0;
                String[] IpsPort = serverAddr[i].split(":");
                String IP = IpsPort[0];
                int Port = Integer.parseInt(IpsPort[1]);
                while (true) {
                    if (replyMsg.equalsIgnoreCase("") && count != failedRetry) {
                        Thread.sleep(1000);
                        count++;
                        replyMsg = sendBroadcastRequest(IP, Port, requestMsg, refId, timeoutConnectMs, timeoutSoMs);
                    } else {
                        break;
                    }
                }
                XMLReader reader = new XMLReader(replyMsg);
                reader.load();
                try {
                    responseCode[i] = (reader.readOneElement("result"));
                } catch (Exception ex) {
                    responseCode[i] = "0";
                }
                responseString[i] = (replyMsg);
            } catch (Exception ex) {
                responseCode[i] = "-1";
                responseString[i] = "Exception Error";
                throw ex;
            }
        }
        result.broadcastResultCode = responseCode;
        result.broadcastResponseString = responseString;
        return result;
    }

    public static String sendBroadcastRequest(String IP, int Port,
            String requestMsg, String refId, int timeoutConnectMs,
            int timeoutSoMs) throws Exception {
        DataInputStream incomingStream = null;
        DataOutputStream outgoingStream = null;
        Socket clientsock = null;
        String replyMsg = "";
        try {
            SocketAddress sockaddr = new InetSocketAddress(IP, Port);

            clientsock = new Socket();

            clientsock.setSoTimeout(timeoutSoMs);
            clientsock.setSoLinger(true, 0);
            clientsock.connect(sockaddr, timeoutConnectMs);
//            writeLog("[" + refId + "][TCPClient] Socket connected, Sending Data:" + requestMsg, LogType.DEBUG);

            outgoingStream = new DataOutputStream(clientsock.getOutputStream());

            String strToSend = requestMsg + "\n\n";
            byte[] outputBuf = strToSend.getBytes();
            outgoingStream.write(outputBuf);
            outgoingStream.flush();

            incomingStream = new DataInputStream(clientsock.getInputStream());
//            writeLog("[" + refId + "][TCPClient] Buffer sent, Waiting reply...", LogType.DEBUG);
            int token;
            while ((token = incomingStream.read()) != -1) {
                char ch = (char) token;
                replyMsg = replyMsg + (char) token;
                if (ch == '\n') {
                    break;
                }
            }
//            writeLog("[" + refId + "][TCPClient] Response:" + replyMsg, LogType.DEBUG);
            incomingStream.close();
            outgoingStream.close();
            clientsock.close();
//            writeLog("[" + refId + "][TCPClient] [Connection closed]", LogType.DEBUG);
            // extract result code from response
            return replyMsg;
        } catch (Exception ex) {
//            writeLog("[" + refId + "][TCPClient] [Exception]", LogType.DEBUG);
            throw new Exception(ex);
            // return "";
        } finally {
            try {
                closeSilently(clientsock);
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    /**
     * Sends Request to Server and append \n\n with Message
     *
     * @param IP
     * @param port
     * @param requestMsg
     * @param refId
     * @param timeoutConnectMs
     * @param timeoutSoMs
     * @return
     * @throws java.lang.Exception
     */
    public static RequestResult sendRequest(String IP, int port,
            String requestMsg, String refId, int timeoutConnectMs,
            int timeoutSoMs) throws Exception {
        RequestResult result = new RequestResult();
        DataInputStream incomingStream = null;
        DataOutputStream outgoingStream = null;
        String replyMsg = "";
        Socket clientsock = null;
        try {
            SocketAddress sockaddr = new InetSocketAddress(IP, port);
            clientsock = new Socket();

            clientsock.setSoTimeout(timeoutSoMs);
            clientsock.setSoLinger(true, 0);
            clientsock.connect(sockaddr, timeoutConnectMs);
//            writeLog("[" + refId + "][TCPClient] Socket connected, Sending Data:" + requestMsg, LogType.DEBUG);

            outgoingStream = new DataOutputStream(clientsock.getOutputStream());

            String strToSend = requestMsg + "\n\n";
            byte[] outputBuf = strToSend.getBytes();
            outgoingStream.write(outputBuf);
            outgoingStream.flush();

            incomingStream = new DataInputStream(clientsock.getInputStream());
//            writeLog("[" + refId + "][TCPClient] Buffer sent, Waiting reply...", LogType.DEBUG);
            int token;
            while ((token = incomingStream.read()) != -1) {
                char ch = (char) token;
                replyMsg = replyMsg + (char) token;
                if (ch == '\n') {
                    break;
                }
            }
//            writeLog("[" + refId + "][TCPClient] Response:" + replyMsg, LogType.DEBUG);
            incomingStream.close();
            outgoingStream.close();
            clientsock.close();
//            writeLog("[" + refId + "][TCPClient] [Connection closed]", LogType.DEBUG);
            // extract result code from response
            XMLReader reader = new XMLReader(replyMsg);
            reader.load();
            try {
                result.requestResultCode = Integer.parseInt(reader.readOneElement("result"));
            } catch (Exception ex) {
                result.requestResultCode = 0;
            }
            result.responseString = replyMsg;
        } catch (Exception ex) {
//            logger.error("[" + refId + "][TCPClient] Exception error ", ex);
            result.requestResultCode = -1;
            result.responseString = "Exception Error";
            throw ex;
        } finally {
            try {
                closeSilently(clientsock);
            } catch (Exception ex) {
                throw ex;
            }
        }
        return result;
    }

    /**
     * Sends Request to Server and appends \n\n with Message expects response
     * with \r\n, response should be in UTF character sets
     *
     * @param IP
     * @param port
     * @param requestMsg
     * @param refId
     * @param timeoutConnectMs
     * @param timeoutSoMs
     * @return
     * @throws java.lang.Exception
     */
    public static RequestResult sendUTFRequest(String IP, int port,
            String requestMsg, String refId, int timeoutConnectMs,
            int timeoutSoMs) throws Exception {
        RequestResult result = new RequestResult();
        InputStream incomingStream = null;
        DataOutputStream outgoingStream = null;
        String replyMsg = "";
        Socket clientsock = null;
        try {
            SocketAddress sockaddr = new InetSocketAddress(IP, port);
            clientsock = new Socket();

            clientsock.setSoTimeout(timeoutSoMs);
            clientsock.setSoLinger(true, 0);
            clientsock.connect(sockaddr, timeoutConnectMs);
//            writeLog("[" + refId + "][TCPClient] Socket connected, Sending Data:" + requestMsg, LogType.DEBUG);

            outgoingStream = new DataOutputStream(clientsock.getOutputStream());

            String strToSend = requestMsg + "\n\n";
            byte[] outputBuf = strToSend.getBytes();
            outgoingStream.write(outputBuf);
            outgoingStream.flush();

            incomingStream = clientsock.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(
                    incomingStream, Charset.defaultCharset()
            );

//            writeLog("[" + refId + "][TCPClient] Buffer sent, Waiting reply...", LogType.DEBUG);
            int token;
            //HE\rte\r\n\r\n
            /**
             * Boolean to store if last character was a return character
             */
            boolean isLastCharReturn = false;
            while ((token = inputStreamReader.read()) != -1) {
//                System.out.println(token);
                char ch = (char) token;
//                replyMsg = replyMsg + (char) token;
                if (ch == '\r') {
                    isLastCharReturn = true;
                } else if (ch != '\n') {
                    isLastCharReturn = false;
                    replyMsg = replyMsg + (char) token;
                }

                if (ch == '\n') {
                    replyMsg = replyMsg + (char) token;
                    if (isLastCharReturn) {
                        break;
                    }
                }
            }
//            writeLog("[" + refId + "][TCPClient] Response:" + replyMsg, LogType.DEBUG);
            incomingStream.close();
            outgoingStream.close();
            clientsock.close();
//            writeLog("[" + refId + "][TCPClient] [Connection closed]", LogType.DEBUG);
            // extract result code from response
            XMLReader reader = new XMLReader(replyMsg);
            reader.load();
            try {
                result.requestResultCode = Integer.parseInt(reader.readOneElement("result"));
            } catch (Exception ex) {
                result.requestResultCode = 0;
            }
            result.responseString = replyMsg;
        } catch (Exception ex) {
            System.out.println("[" + refId + "][TCPClient] Exception error " + ex);
            result.requestResultCode = -1;
            result.responseString = "Exception Error";
            throw ex;
        } finally {
            try {
                closeSilently(clientsock);
            } catch (Exception ex) {
                throw ex;
            }
        }
        return result;
    }

    /**
     * Sends encoded Request to Server and append \n\n with Message
     *
     * @param IP
     * @param port
     * @param requestMsg must be encoded using
     * com.kalsym.security.CustomEncodeDecode.encodeMessage(msg)
     * @param refId
     * @param timeoutConnectMs
     * @param timeoutSoMs
     * @return returns decoded message
     * @throws java.lang.Exception
     */
    public static RequestResult sendEncodedRequest(String IP, int port,
            String requestMsg, String refId, int timeoutConnectMs,
            int timeoutSoMs) throws Exception {
        RequestResult result = new RequestResult();
        DataInputStream incomingStream = null;
        DataOutputStream outgoingStream = null;
        String replyMsg = "";
        Socket clientsock = null;
        try {
            SocketAddress sockaddr = new InetSocketAddress(IP, port);
            clientsock = new Socket();

            clientsock.setSoTimeout(timeoutSoMs);
            clientsock.setSoLinger(true, 0);
            clientsock.connect(sockaddr, timeoutConnectMs);
//            writeLog("[" + refId + "][TCPClient] Socket connected, Sending Data:" + requestMsg, LogType.DEBUG);

            outgoingStream = new DataOutputStream(clientsock.getOutputStream());

            String strToSend = requestMsg + "\n\n";
            byte[] outputBuf = strToSend.getBytes();
            outgoingStream.write(outputBuf);
            outgoingStream.flush();

            incomingStream = new DataInputStream(clientsock.getInputStream());
//            writeLog("[" + refId + "][TCPClient] Buffer sent, Waiting reply...", LogType.DEBUG);
            int token;
            while ((token = incomingStream.read()) != -1) {
                char ch = (char) token;
                replyMsg = replyMsg + (char) token;
                if (ch == '\n') {
                    break;
                }
            }
//            writeLog("[" + refId + "][TCPClient] Response:" + replyMsg, LogType.DEBUG);
            incomingStream.close();
            outgoingStream.close();
            clientsock.close();
//            writeLog("[" + refId + "][TCPClient] [Connection closed]", LogType.DEBUG);
            // Decode response
            try {
                replyMsg = CustomEncodeDecode.decodeEncodedMessage(replyMsg);
            } catch (Exception ex) {
                result.requestResultCode = -1;
                result.responseString = "Exception while decoding response!";
                throw ex;
            }
 //           writeLog("[" + refId + "][TCPClient] response after decode:" + replyMsg, LogType.DEBUG);

            // extract result code from response
            XMLReader reader = new XMLReader(replyMsg);
            reader.load();
            try {
                result.requestResultCode = Integer.parseInt(reader.readOneElement("result"));
            } catch (Exception ex) {
                result.requestResultCode = 0;
            }
            result.responseString = replyMsg;
        } catch (Exception ex) {
//            logger.error("[" + refId + "][TCPClient] Exception error ", ex);
            result.requestResultCode = -1;
            result.responseString = "Exception Error";
            throw ex;
        } finally {
            try {
                closeSilently(clientsock);
            } catch (Exception ex) {
                throw ex;
            }
        }
        return result;
    }

    private static void closeSilently(Socket s) throws Exception {
        if (s != null) {
            try {
                s.close();
            } catch (Exception ex) {
 //               writeLog("Exception while closing silently :" + ex, LogType.ERROR);
                throw new Exception();
            }
        }
    }

//    /**
//     *
//     * @param logMsg
//     * @param logType
//     */
//    private static void writeLog(String logMsg, LogType logType) throws Exception {
//        try {
//            //LogProperties.WriteLog(logMsg);
//        } catch (Exception ex) {
////            try {
////                logger.error("Exception", ex);
////            } catch (Exception e) {
////            }
//            throw new Exception();
//        }
//    }
}
