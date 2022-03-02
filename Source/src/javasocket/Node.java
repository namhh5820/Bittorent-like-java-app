/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javasocket;

/**
 *
 * @author namhh
 */
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static javasocket.Node.MergeFile;

class RunThread extends Thread {
    private int threadID;
    private int threadNum;
    private String filedownload;
    
    RunThread (int id,int num, String filename){
        threadID = id;
        threadNum = num;
        filedownload =  filename;
    }

    public void run() {

        //Gui broadcast tim chuck
        //Chon node download
        System.out.println("Thread " + threadID + " Running...");
        
        int num_file = 0;
        String DIR_NAME = "src/javasocket/downloads/";
        String FILE_NAME = "src/javasocket/downloads/" + filedownload;
        
        //Doc file chunk
        String fileName = DIR_NAME + filedownload+ ".chunk";
        List<String> list_chunk = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {

            //br returns as stream and convert it into a List
            list_chunk = br.lines().collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("Hash: " + list_chunk.get(0).substring(2));
        num_file = list_chunk.size(); // Set num_file
        //System.out.println("Size: " + list_chunk.size());
                      
        //Doc file node        
        fileName = DIR_NAME + "nodes.map";
        List<String> list_node = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {

            //br returns as stream and convert it into a List
            list_node = br.lines().collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        //list.forEach(System.out::println);
        //System.out.println("Number of node: " + list_node.get(0));
        // Luu thong tin cac node vao mang
        int so_node = Integer.parseInt(list_node.get(0));

        // Code etc
        String ip=null;
        String MY_IP=null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    if(ip.contains("192.168.81"))
                    {
                        //System.out.println("IP: " + ip);
                        MY_IP = ip;
                    }
                    
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        
        //Lay ID node
        String id_node = null;
        for (int i = 1; i <= so_node; i++) {
            String nodei = list_node.get(i);
            int dodai = nodei.length();
            String addr = nodei.substring(2, dodai - 5);
            if (addr.contentEquals(MY_IP)) {
                id_node = list_node.get(i).substring(0, 1);
            }
        } 
        
        // System.out.println("ID my node: " + id_node);
        // Lay danh sach cach node
        int[][] node_arr = new int[2][so_node];
        int k=0;
        for(int i = so_node + 1; i< list_node.size(); i++)
        {
            int dodai = list_node.get(i).length();
            String id1 = list_node.get(i).substring(0,1);
            String id2 = list_node.get(i).substring(2,3);
            String cost = list_node.get(i).substring(4);
            
            //System.out.println("Node ID2 :" + id2);
            if(id1.contentEquals(id_node))
            {
                node_arr[k][0] = Integer.parseInt(id2);
                node_arr[k][1] = Integer.parseInt(cost);
                k++;
             
            }
            if(id2.contentEquals(id_node))
            {
                node_arr[k][0] = Integer.parseInt(id1);
                node_arr[k][1] = Integer.parseInt(cost);
                k++;
            }
        }
        //Sap xep theo cost
        for(int i=0;i<so_node-2;i++)
        {
            if(node_arr[i][1] > node_arr[i+1][1])
            {
                int tem1 = node_arr[i][0];
                int tem2 = node_arr[i][1];
                node_arr[i][0] = node_arr[i+1][0];
                node_arr[i][1] = node_arr[i+1][1];
                node_arr[i+1][0] = tem1;
                node_arr[i+1][1] = tem2;
            }
        }
        
        //In ra danh sach node
        String[][] node_sort = new String[2][so_node];
        for(int i=0;i<so_node-1;i++)
        {
            //System.out.println( i + " Node : " + node_arr[i][0] + "Cost " + node_arr[i][1]);
            int nodetmp = node_arr[i][0];
            for (int j = 1; j <= so_node; j++) {
                            String nodei = list_node.get(j);
                            int dodai = nodei.length();
                            String idtmp = nodei.substring(0,1);
                            int id = Integer.parseInt(idtmp);
                            if(id == nodetmp)
                            {
                                String port = nodei.substring(dodai - 4);
                                String addr = nodei.substring(2, dodai - 5);
                                node_sort[i][1] = port;
                                node_sort[i][0] = addr;
                            }
                            
            }
            

        }
        //In ra danh sach node duoc sap xep
        //for(int i=0;i<so_node-1;i++)
        //{
        //    System.out.println("Node : " + node_arr[i][0] + " IP " + node_sort[i][0] + " Port " + node_sort[i][1]);
        //}
        


            // Khai bao
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            //System.out.println("File download:");
            //String sentence = inFromUser.readLine();
            //sendData = sentence.getBytes();
            
            int finish = 0;
            while (finish == 0) {
                
                try {
                    for (int i = 0; i < num_file; i++) {

                        // Kiem tra part da duoc download hay chua?
                        String path = FILE_NAME + ".part" + i;
                        File checkf = new File(path);
                        if (!checkf.exists() && i % threadNum == threadID) {
                            //System.out.println("Data: " + sendData.length);
                            int check = 0;
                            DatagramSocket clientSocket = new DatagramSocket(3730 + threadID);
                            InetAddress IP_ketnoi = null;
                            int Port_ketnoi = 6789;

                            // Gui boadcast goi tin
                            for (int j = 0; j < so_node - 1; j++) {
                                try {
                                    check = 0;
                                    //String nodei = list_node.get(j + 1);
                                    //int dodai = nodei.length();
                                    //Chon node theo cost
                                    String port = node_sort[j][1];
                                    int Portnode = Integer.parseInt(port);
                                    String addr = node_sort[j][0];

                                    //System.out.println("Addr: " + addr);
                                    //System.out.println("Port: " + port);
                                    InetAddress IPnode = InetAddress.getByName(addr);

                                    // Gui cho server yeu cau goi tin
                                    String SYN = "SYN-" + i;
                                    sendData = SYN.getBytes();
                                    DatagramPacket sendPacketSYN = new DatagramPacket(sendData, sendData.length, IPnode, Portnode);
                                    clientSocket.send(sendPacketSYN);
                                    clientSocket.setSoTimeout(2000);
                                    
                                    try {
                                        // Nhan ket qua tu server
                                        DatagramPacket receivePacketSYN = new DatagramPacket(receiveData, receiveData.length);
                                        clientSocket.receive(receivePacketSYN);
                                        String ACK = new String(receivePacketSYN.getData());

                                        ACK = ACK.substring(0, 3);
                                        //System.out.println("Client: " + ACK);
                                        if (ACK.contains("YES")) {
                                            // Lay thong tin node ket noi
                                            IP_ketnoi = receivePacketSYN.getAddress();
                                            Port_ketnoi = receivePacketSYN.getPort();
                                            check = 1;
                                            break;

                                        } else {
                                            System.out.println("Server " + addr + " khong co gói tin: " + i + " Thread: " + threadID);
                                        }
                                    } catch (SocketTimeoutException e) {
                                        // timeout exception.
                                        System.out.println("Timeout reached!!! " + e);
                                        clientSocket.close();
                                    }
                                    
                                } catch (SocketException e1) {
                                    // TODO Auto-generated catch block
                                    //e1.printStackTrace();
                                    System.out.println("Socket closed " + e1);

                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                

                            }

                        // Thuc hien ket noi toi node va download du lieu
                        if (check == 1) {
                            System.out.println("Goi tin " + i + ": " + IP_ketnoi.toString() + " Port: " + Port_ketnoi + " Thread: " + threadID);
                            // Lay du lieu data
                            // Gui cho server
                            String sentence = "part" + Integer.toString(i);
                            sendData = sentence.getBytes();

                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP_ketnoi, Port_ketnoi);
                            clientSocket.send(sendPacket);

                            // Nhan ket qua tu server
                            FileOutputStream f = new FileOutputStream(FILE_NAME + "." + sentence);
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            clientSocket.receive(receivePacket);

                            f.write(receivePacket.getData(), 0, receivePacket.getLength());
                            f.flush();
                            f.close();

                            // Kiem tra du lieu so sanh hash
                            String hash_org = list_chunk.get(i).substring(2);
                            SHACheckSum chk = new SHACheckSum();
                            String hash = chk.SHACheckSum(FILE_NAME + "." + sentence);

                            if (hash_org.equals(hash)) {
                                System.out.println("Hash OK");
                            }
                            else
                            {
                                checkf.delete();
                                System.out.println("Hash checksum fail, file removed");
                            }
                                
                        }
                        
                        clientSocket.close();

                    }

                }
                
                // Kiem tra cac part da duoc download hoan tat
                int kt=0;
                for (int i=0;i<num_file;i++)
                {
                    String path = FILE_NAME + ".part" + i;
                    File checkf = new File(path);
                    if (!checkf.exists())
                        kt++;
                }
                
                if(kt==0)
                    finish = 1;
                
                } catch (IOException e) {
                System.out.println("Exception: " + e.getMessage());
                } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(RunThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            

            // Noi file
            File checkdl = new File(FILE_NAME);
             if (!checkdl.exists() && threadID == 0)
             {
                System.out.print("Download các chunk hoàn tất, thưc hiện nối file :");
                Scanner in = new Scanner(System.in);
                String namhh = in.nextLine();
                MergeFile(FILE_NAME, num_file);
             
             }
       
    }

}

class Node {
    
    public static void MergeFile(String FILE_NAME, int num) {
        File ofile = new File(FILE_NAME);
        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytes;
        int bytesRead = 0;

        List<File> list = new ArrayList<File>();
        for (int i = 0; i < num; i++) {
            list.add(new File(FILE_NAME + ".part" + i));
        }

        try {
            fos = new FileOutputStream(ofile, true);
            for (File file : list) {
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                bytesRead = fis.read(fileBytes, 0, (int) file.length());
                assert (bytesRead == fileBytes.length);
                assert (bytesRead == (int) file.length());
                fos.write(fileBytes);
                fos.flush();
                fileBytes = null;
                fis.close();
                fis = null;
            }
            fos.close();
            fos = null;
            System.out.println("File download hoàn tất!!!");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void SplitFile(String FILE_NAME) {
        
        String CHUCK_FILE_NAME = FILE_NAME + ".chunk";
        int PART_SIZE = 1024;
        File inputFile = new File(FILE_NAME);
        FileInputStream inputStream;
        String newFileName;
        FileOutputStream filePart;
        int fileSize = (int) inputFile.length();
        int nChunks = 0, read = 0, readLength = PART_SIZE;
        byte[] byteChunkPart;
        try {
            inputStream = new FileInputStream(inputFile);
            PrintWriter writer = new PrintWriter(CHUCK_FILE_NAME, "UTF-8");
            while (fileSize > 0) {
                if (fileSize <= PART_SIZE) {
                    readLength = fileSize;
                }
                byteChunkPart = new byte[readLength];
                read = inputStream.read(byteChunkPart, 0, readLength);
                fileSize -= read;
                assert (read == byteChunkPart.length);
                nChunks++;
                newFileName = FILE_NAME + ".part"
                        + Integer.toString(nChunks - 1);
                filePart = new FileOutputStream(new File(newFileName));
                filePart.write(byteChunkPart);
                filePart.flush();
                filePart.close();
                byteChunkPart = null;
                filePart = null;
                
                SHACheckSum chk = new SHACheckSum();
                String hash =  chk.SHACheckSum(newFileName);
                writer.println(nChunks + ":" + hash);
                
            }
            inputStream.close();
            writer.close();
            System.out.println("File split completed");
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) throws Exception {

        int mode;
        int num_thread;
        String file = "logo.png";
        String FILE_NAME = "src/javasocket/files/" + file;
        System.out.print("Chọn mode 0(Download) 1(Upload) :");
        Scanner in = new Scanner(System.in);
        mode = in.nextInt();

        if (mode == 0) {
            System.out.print("Chọn số thread download đồng thời :");
            num_thread = in.nextInt();
            for (int i = 0; i < num_thread; i++) {
                RunThread download = new RunThread(i, num_thread, file);
                download.start();

                //System.out.println("Thread " + i + " Running...");
            }

        } else {
            SplitFile(FILE_NAME);
            System.out.print("Upload running ...");
        }

        DatagramSocket serverSocket = new DatagramSocket(6789);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        

        while (true) {

            //Nhan yeu cau
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData());
            //System.out.println("RECEIVED: " + sentence);

            InetAddress IPclient = receivePacket.getAddress();
            int port = receivePacket.getPort();

            // Tra ve du lieu yeu cau
            if (sentence.contains("SYN")) {
                //Kiem tra server co du lieu part nay hay khong
                //System.out.println("SYN: " + sentence);

                int kt = 0;
                String path = FILE_NAME + ".part" + sentence.substring(4, 5);
                path = path.trim();
                //System.out.println("File: " + path);

                // Tra ve "YES" or "NO"
                File f = new File(path);

                if (f.exists()) {
                    //System.out.println("File existed");
                    kt = 1;
                }

                if (kt == 1) {
                    String ACK = "YES";
                    sendData = ACK.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPclient, port);
                    serverSocket.send(sendPacket);
                } else {
                    String ACK = "NON";
                    sendData = ACK.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPclient, port);
                    serverSocket.send(sendPacket);
                }

            } else {
                // Tra ve du lieu
                sentence = sentence.substring(0, 5);
                String path = FILE_NAME + "." + sentence;
                FileInputStream f = new FileInputStream(path.trim());
                byte[] Data = new byte[1024];
                int i = 0;
                while (f.available() != 0) {
                    Data[i] = (byte) f.read();
                    i++;
                }
                f.close();

                DatagramPacket sendPacket = new DatagramPacket(Data, i, IPclient, port);
                serverSocket.send(sendPacket);

            }

        }
    }

}
