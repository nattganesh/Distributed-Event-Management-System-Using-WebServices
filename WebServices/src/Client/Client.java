/**
 * CONCORDIA UNIVERSITY
 * DEPARTMENT OF COMPUTER SCIENCE AND SOFTWARE ENGINEERING
 * COMP 6231, Summer 2019 Instructor: Sukhjinder K. Narula
 * ASSIGNMENT 1
 * Issued: May 14, 2019 Due: Jun 3, 2019
 */
package Client;

import ServerInterfaces.WebInterface;
import ServerImpl.MontrealServerImpl;
import ServerImpl.OttawaServerImpl;
import ServerImpl.TorontoServerImpl;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static CommonUtils.CommonUtils.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 *
 * @author Natheepan Ganeshamoorthy, Gursimran Singh
 */
public class Client {

    private static Logger LOGGER;
    private static final Scanner scanner = new Scanner(System.in);
    static WebInterface webInterface;

    public static void main(String[] args)
    {
        try
        {
            String id = enterValidID(InputType.CLIENT_ID);
            String url = "http://localhost:808" + (id.substring(0, 3).equals(MONTREAL) ? "0/montreal" : id.substring(0, 3).equals(TORONTO) ? "2/toronto" : "1/ottawa") + "?wsdl";
            URL addURL = new URL(url);
            QName addQName = new QName("http://ServerImpl/", (id.substring(0, 3).equals(MONTREAL) ? "MontrealServerImplService" : id.substring(0, 3).equals(TORONTO) ? "TorontoServerImplService" : "OttawaServerImplService"));
            Service service = Service.create(addURL, addQName);
            webInterface = service.getPort(WebInterface.class);
            clientService(id.substring(0, 3), id.substring(4, 8), id.substring(3, 4), webInterface);
        }
        catch (MalformedURLException e)
        {
            System.out.println("Hello Client exception: " + e);
        }
    }

    private static void clientService(String serverId, String clientID, String clientType, WebInterface webInterface)
    {
        try
        {
            String customerID = capitalize(serverId + clientType + clientID);
            LOGGER = Logger.getLogger(getServerClassName(serverId));
            addFileHandler(LOGGER, customerID);

            if (clientType.equals(CUSTOMER_ClientType))
            {
                System.out.println("Welcome Customer " + customerID);
                runCustomerMenu(webInterface, customerID);
            }
            if (clientType.equals(EVENT_MANAGER_ClientType))
            {
                System.out.println("Welcome Manager " + customerID);
                runManagerMenu(webInterface, customerID);
            }
        }
        catch (IOException e)
        {

        }
    }

    private static void runCustomerMenu(WebInterface server, String customerID)
    {
        String itemNum = "";
        while (!itemNum.equals("0"))
        {
            System.out.println("============================");
            System.out.println("Customer Menu");
            System.out.println("0: Quit");
            System.out.println("1: Book Event");
            System.out.println("2: Get Booking Schedule");
            System.out.println("3: Cancel Event");
            System.out.println("4: Swap Event");
            System.out.println("============================");

            itemNum = scanner.next().trim();

            if (itemNum.matches("^[0-4]$"))
            {
                switch (itemNum)
                {
                    case "0":
                        System.out.println("Good Bye !!!");
                        break;
                    case "1":
                        runBookEvent(server, customerID);
                        break;
                    case "2":
                        runBookingSchedule(server, customerID, "null");
                        break;
                    case "3":
                        System.out.println("Enter Event Type of The Event to Cancel? (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR)");
                        String eventType = getEventType();
//                        System.out.println("Enter Event ID to Cancel: ");
                        String eventID = enterValidID(InputType.EVENT_ID);
                        String response = server.cancelEvent(customerID, eventID, eventType);
                        System.out.println("Response from server: " + response.replaceAll("NAT", "\n"));
                        LOGGER.log(Level.INFO, "Response of server: {0}", response.replaceAll("NAT", "\n"));
                        break;
                    case "4":
                        System.out.println("Enter new Event Type of The Event to Replace? (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR)");
                        String newEventType = getEventType();
                        String newEventID = enterValidID(InputType.EVENT_ID);
                        System.out.println("Enter old Event Type of The Event to Remove? (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR)");
                        String oldEventType = getEventType();
                        String oldEventID = enterValidID(InputType.EVENT_ID);
                        String swap = server.swapEvent(customerID, newEventID, newEventType, oldEventID, oldEventType);
                        System.out.println("Response from server: " + swap.replaceAll("NAT", "\n"));
                        LOGGER.log(Level.INFO, "Response of server: {0}", swap.replaceAll("NAT", "\n"));
                        break;
                    default:
                        System.out.println("Invalid Choice !!!");
                        break;
                }
            }
            else
            {
                System.out.println("Please select a valid choice!");
            }
        }
        scanner.close();
    }

    private static void runManagerMenu(WebInterface server, String managerID)
    {
        String itemNum = "";
        while (!itemNum.equals("0"))
        {
            System.out.println("============================");
            System.out.println("Manager Menu");
            System.out.println("0: Quit");
            System.out.println("1: Add Event");
            System.out.println("2: Remove Event");
            System.out.println("3: List Event Availability");
            System.out.println("4: Book Event");
            System.out.println("5: Get Booking Schedule");
            System.out.println("6: Cancel Event");
            System.out.println("7: Swap Event");
            System.out.println("============================");

            itemNum = scanner.next().trim();

            if (itemNum.matches("^[0-7]$"))
            {
                switch (itemNum)
                {
                    case "0":
                        System.out.println("Good Bye !!!");
                        break;
                    case "1":
                        System.out.println("What event do you wish to add?");
                        managerAddEvent(server, managerID);
                        break;
                    case "2":
                        System.out.println("What event do you wish to remove?");
                        managerRemoveEvent(server, managerID);
                        break;
                    case "3":
                        System.out.println("Which type of event you wish to list? (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR)");
                        managerListEvents(server, managerID);
                        break;
                    case "4":
                        runBookEvent(server, enterValidID(InputType.CLIENT_ID));
                        break;
                    case "5":
                        System.out.println("What customer do you wish to get Booking Schedule for?");
                        runBookingSchedule(server, enterValidID(InputType.CLIENT_ID), managerID);
                        break;
                    case "6":
                        System.out.println("What event do you wish to cancel?");
                        System.out.println("Enter Event Type of The Event to Cancel? (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR)");
                        String eventType = getEventType();
//                        System.out.println("Enter Event ID to Cancel: ");
                        String eventID = enterValidID(InputType.EVENT_ID);
                        String customerID = enterValidID(InputType.CLIENT_ID);
                        String response = server.cancelEvent(customerID, eventID, eventType);
                        System.out.println("Response from server: " + response.replaceAll("NAT", "\n"));
                        break;
                    case "7":
                        System.out.println("Enter new Event Type of The Event to Replace? (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR)");
                        String newEventType = getEventType();
                        String newEventID = enterValidID(InputType.EVENT_ID);
                        System.out.println("Enter old Event Type of The Event to Remove? (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR)");
                        String oldEventType = getEventType();
                        String oldEventID = enterValidID(InputType.EVENT_ID);
                        String customerID2 = enterValidID(InputType.CLIENT_ID);
                        String swap = server.swapEvent(customerID2, newEventID, newEventType, oldEventID, oldEventType);
                        System.out.println("Response from server: " + swap.replaceAll("NAT", "\n"));
                        LOGGER.log(Level.INFO, "Response of server: {0}", swap.replaceAll("NAT", "\n"));
                        break;
                    default:
                        System.out.println("Invalid Choice !!!");
                        break;
                }
            }
            else
            {
                System.out.println("Please select a valid choice!");
            }
        }
        scanner.close();
    }

    private static void managerAddEvent(WebInterface server, String managerID)
    {
        try
        {
            String eventID;
            String eventType;
            String bookingCapacity;
            eventID = enterValidID(InputType.EVENT_ID);
            System.out.println();
            System.out.println("Please enter Event Type: (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR) ");
            eventType = getEventType();
            System.out.println();
            System.out.print("Please enter Booking Capacity: ");
            bookingCapacity = getNumber();
            LOGGER.log(Level.INFO, "Manager: {0} adding a new Event with Event id: {1} ,Event Type: {2} and Booking Capacity: {3}", new Object[]
            {
                managerID, eventID, eventType, bookingCapacity
            });
            String string = server.addEvent(eventID, eventType, bookingCapacity, managerID);
            LOGGER.log(Level.INFO, "Response of server: {0}", string.replaceAll("NAT", "\n"));
            System.out.println("Response of server: " + string.replaceAll("NAT", "\n"));
        }
        catch (Exception ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void managerRemoveEvent(WebInterface server, String managerID)
    {
        String eventID;
        String eventType;
        try
        {
            eventID = enterValidID(InputType.EVENT_ID);
            System.out.println();
            System.out.println("Please enter Event Type: (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR) ");
            eventType = getEventType();

            LOGGER.log(Level.INFO, "Manager {0} removing Event with Event ID {1} of type: {2}", new Object[]
            {
                managerID, eventID, eventType
            });
            String string = server.removeEvent(eventID, eventType, managerID);
            System.out.println("Response of the server: " + string.replaceAll("NAT", "\n"));
            LOGGER.log(Level.INFO, "Response of server: {0}", string.replaceAll("NAT", "\n"));
        }
        catch (Exception ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void managerListEvents(WebInterface server, String customerID)
    {
        try
        {
            String eventType = getEventType();
            String str = server.listEventAvailability(eventType, customerID);
            System.out.println(str.replaceAll("NAT", "\n"));
            LOGGER.log(Level.INFO, "Response of Server: {0}", str.replaceAll("NAT", "\n"));
        }
        catch (Exception ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void runBookEvent(WebInterface server, String customerID)
    {
        System.out.println("What type of event do you wish to book? (Available Options: A: CONFERENCE, B: TRADESHOW, C: SEMINAR)");
        String eventType = getEventType();
        String eventID = enterValidID(InputType.EVENT_ID);
        String msg = server.bookEvent(customerID, eventID, eventType, "1");
        LOGGER.info(msg.replaceAll("NAT", "\n"));
        System.out.println(msg.replaceAll("NAT", "\n"));
    }

    private static void runBookingSchedule(WebInterface server, String customerID, String managerId)
    {
        LOGGER.log(Level.INFO, "Booking Schedule Requested by {0}", customerID);
        System.out.println(customerID + "'s Bookings Schedule");
        String booking = server.getBookingSchedule(customerID, managerId);
        System.out.println(booking.replaceAll("NAT", "\n"));

        if (!booking.equalsIgnoreCase(OPERATIONFAILURE))
        {
            LOGGER.log(Level.INFO, "Operation Sucessful. Records for {0} have been found", customerID);
            LOGGER.info(booking.replaceAll("NAT", "\n"));
        }
        else
        {
            LOGGER.log(Level.INFO, "Operation Failure. Records for {0} do not exist.", customerID);
        }
    }

    private static String getServerClassName(String serverId)
    {
        switch (serverId)
        {
            case TORONTO:  return TorontoServerImpl.class.getName();
            case MONTREAL: return MontrealServerImpl.class.getName();
            case OTTAWA:   return OttawaServerImpl.class.getName();
            default:       return "Server Does Not Exist";
        }
    }

    public static String getNumber()
    {
        String num = scanner.next().trim();
        System.out.println();
        while (!num.matches("^[1-9]\\d*$"))
        {
            System.out.println("Invalid ID !!!\n");
            System.out.print("Please enter Valid Number");
            num = scanner.next().trim();
            System.out.println();
        }
        return num;
    }

    private static String enterValidID(InputType type)
    {
        String msg = "";
        if (InputType.CLIENT_ID == type)
        {
            msg = "Enter Customer ID Number: ";
        }
        else if (InputType.EVENT_ID == type)
        {
            msg = "Enter Event ID Number: ";
        }
        System.out.print(msg);
        String id = capitalize(scanner.next().trim());
        System.out.println();
        while (!isInputValid(id, type))
        {
            System.out.println("Invalid ID !!!\n");
            System.out.print(msg);
            id = capitalize(scanner.next().trim());
            System.out.println();
        }
        return id;
    }

    private static String getEventType()
    {
        String eventType = capitalize(scanner.next().trim());;
        while (!eventType.equals("A") && !eventType.equals("B") && !eventType.equals("C"))
        {
            System.out.println("Select an appropriate option!");
            eventType = capitalize(scanner.next().trim());
        }
        switch (eventType)
        {
            case "A": return CONFERENCE;
            case "B": return TRADESHOW;
            case "C": return SEMINAR;
            default:  return "";
        }
    }
}