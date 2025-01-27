/**
 * CONCORDIA UNIVERSITY
 * DEPARTMENT OF COMPUTER SCIENCE AND SOFTWARE ENGINEERING
 * COMP 6231, Summer 2019 Instructor: Sukhjinder K. Narula
 * ASSIGNMENT 1
 * Issued: May 14, 2019 Due: Jun 3, 2019
 */
package Server;

import javax.xml.ws.Endpoint;
import ServerImpl.MontrealServerImpl;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import static CommonUtils.CommonUtils.MONTREAL_SERVER_PORT;

/**
 *
 * @author Gursimran Singh
 */
public class MontrealServer {

    public static MontrealServerImpl montrealServerStub;
    
    public static void main(String[] args)
    {
        montrealServerStub = new MontrealServerImpl();
        Endpoint endpoint = Endpoint.publish("http://localhost:8080/montreal", montrealServerStub);
        Runnable runnable = () ->
        {
            receiveRequestsFromOthers(montrealServerStub);
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private static void receiveRequestsFromOthers(MontrealServerImpl monStub)
    {
        DatagramSocket aSocket = null;
        try
        {
            aSocket = new DatagramSocket(MONTREAL_SERVER_PORT);
            byte[] buffer = new byte[1500];
            System.out.println("Montreal server started.....");
            //Server waits for the request
            while (true)
            {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String response = requestsFromOthers(new String(request.getData()), monStub);
                if(response == null) response = "";
                response = response.trim().replaceAll("[^a-zA-Z0-9]", " ");
                DatagramPacket reply = new DatagramPacket(response.getBytes(StandardCharsets.UTF_8), response.length(), request.getAddress(),
                        request.getPort());
                //reply sent
                aSocket.send(reply);
            }
        }
        catch (SocketException e)
        {
            System.out.println("Socket: " + e.getMessage());
        }
        catch (IOException e)
        {
            System.out.println("IO: " + e.getMessage());
        }
        finally
        {
            if (aSocket != null)
            {
                aSocket.close();
            }
        }
    }

    //clientudp
    public static String requestsFromOthers(String data, MontrealServerImpl montrealServer)
    {
        try
        {
            String[] receivedDataString = data.split(" ");
            String userId = receivedDataString[0];
            String eventID = receivedDataString[1];
            String methodNumber = receivedDataString[2].trim();
            String eventType = receivedDataString[3].trim();
            String bookingCapacity = receivedDataString[4].trim();
            String managerID = receivedDataString[5].trim();
            String newEventID = receivedDataString[6].trim();
            String newEventType = receivedDataString[7].trim();

            switch (methodNumber)
            {
                case "1":
                    return montrealServer.addEvent(eventID, eventType, bookingCapacity, userId);
                case "2":
                    return montrealServer.removeEvent(eventID, eventType, userId);
                case "3":
                    return montrealServer.listEventAvailability(eventType, managerID);
                case "4":
                    return montrealServer.bookEvent(userId, eventID, eventType, bookingCapacity);
                case "5":
                    return montrealServer.getBookingSchedule(userId,managerID);
                case "6":
                    return montrealServer.cancelEvent(userId, eventID, eventType);
                case "7":
                    return montrealServer.nonOriginCustomerBooking(userId, eventID);
                case "8":
                    return montrealServer.swapEvent(userId, newEventID, newEventType, eventID, eventType);
                case "9":
                    return montrealServer.eventAvailable(newEventID, newEventType);
                case "10":
                    return montrealServer.validateBooking(userId, eventID, eventType);
            }
        }
        catch (Exception e)
        {
            
        }
        return "Incorrect";
    }
}