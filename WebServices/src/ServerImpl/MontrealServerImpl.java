/**
 * CONCORDIA UNIVERSITY
 * DEPARTMENT OF COMPUTER SCIENCE AND SOFTWARE ENGINEERING
 * COMP 6231, Summer 2019 Instructor: Sukhjinder K. Narula
 * ASSIGNMENT 1
 * Issued: May 14, 2019 Due: Jun 3, 2019
 */
package ServerImpl;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import ServerInterfaces.WebInterface;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static CommonUtils.CommonUtils.*;

/**
 *
 * @author Gursimran Singh, Natheepan Ganeshamoorthy
 */

@WebService(endpointInterface = "ServerInterfaces.WebInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)

public class MontrealServerImpl implements WebInterface {

    private static HashMap<String, HashMap< String, String>> databaseMontreal = new HashMap<>();
    private static HashMap<String, HashMap<String, HashMap< String, Integer>>> customerEventsMapping = new HashMap<>();
    private static Logger logger;

    //Events Database
    {
        //item1
        databaseMontreal.put(CONFERENCE, new HashMap<>());
        databaseMontreal.get(CONFERENCE).put("MTLM121219", "999");
        databaseMontreal.get(CONFERENCE).put("MTLE121219", "60");
        databaseMontreal.get(CONFERENCE).put("MTLA121219", "90");
        databaseMontreal.get(CONFERENCE).put("MTLM130722", "60");
        databaseMontreal.get(CONFERENCE).put("MTLM130720", "60");

        //item2
        databaseMontreal.put(SEMINAR, new HashMap<>());
        databaseMontreal.get(SEMINAR).put("MTLM310522", "20");
        databaseMontreal.get(SEMINAR).put("MTLE999999", "999");
        databaseMontreal.get(SEMINAR).put("MTLA201121", "50");

        //item6
        databaseMontreal.put(TRADESHOW, new HashMap<>());
        databaseMontreal.get(TRADESHOW).put("MTLM190124", "50");
        databaseMontreal.get(TRADESHOW).put("MTLE201123", "40");
        databaseMontreal.get(TRADESHOW).put("MTLA999999", "999");
    }

    public MontrealServerImpl()
    {
        super();
        logger = Logger.getLogger(MontrealServerImpl.class.getName());
        try
        {
            addFileHandler(logger, "Montreal_Server");
        }
        catch (SecurityException | IOException ex)
        {
            Logger.getLogger(MontrealServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized String addEvent(String eventID, String eventType, String bookingCapacity, String managerID)
    {
        String message = null;
        
        if(!eventID.substring(0, 3).equals(MONTREAL))
        {
            message = "Operations Unsuccessful!. Event Not Added in Montreal Server "
                    + "for Event ID: " + eventID + " Event Type: " + eventType + " because the Event ID: " + eventID + ""
                    + " is not of Montreal format (MTL)";
            logger.info(message);

            return message.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
        
        logger.info("Received request to add an event with event id " + eventID + " , Event Type" + eventType
                + " & Booking Capacity " + bookingCapacity);
        if (!databaseMontreal.get(eventType).containsKey(eventID))
        {
            databaseMontreal.get(eventType).put(eventID, bookingCapacity);
            message = "Operations Successful!. Event Added in Montreal Server for Event ID: "
                    + eventID + " Event Type: " + eventType + " Booking Capacity: " + bookingCapacity;
            logger.info(message);

            return message.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
        else
        {
            databaseMontreal.get(eventType).replace(eventID, bookingCapacity);
            message = "Operations Unsuccessful!. Event Not Added in Montreal Server "
                    + "for Event ID: " + eventID + " Event Type: " + eventType + " because the Event ID: " + eventID + ""
                    + " is already added for the Event Type: " + eventType + ". But, the Booking Capacity is updated to " + bookingCapacity;
            logger.info(message);

            return message.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
    }

    @Override
    public synchronized String removeEvent(String eventID, String eventType, String managerID)
    {
        String message = null;
        if (databaseMontreal.get(eventType).containsKey(eventID))
        {
            if (customerEventsMapping != null)
            {
                for (String customer : customerEventsMapping.keySet())
                {
                    if (customerEventsMapping.get(customer).containsKey(eventType))
                    {
                        if (customerEventsMapping.get(customer).get(eventType).containsKey(eventID))
                        {
                            message += "NATCustomer ID: " + customer + " for event id " + eventID + " event Type " + eventType + " with customer booking of " + customerEventsMapping.get(customer).get(eventType).get(eventID) + " who was booked in this event has been removed from record.";
                            customerEventsMapping.get(customer).get(eventType).remove(eventID);
                        }
                    }
                }
            }
            databaseMontreal.get(eventType).remove(eventID);

            message = "NATOperations Successful!. Event Removed in Montreal Server by Manager: " + managerID + " for Event ID: "
                    + eventID + " Event Type: " + eventType;
            logger.info(message);
            return message.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
        else
        {
            message = "Operations Unsuccessful!. Event Not Removed in Montreal Server by Manager: " + managerID + " f"
                    + "or Event ID: " + eventID + " Event Type: " + eventType + " because the Event ID: " + eventID
                    + " does not exist";
            logger.info(message);
            return message.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
    }

    @Override
    public synchronized String listEventAvailability(String eventType, String managerID)
    {
        //Eg: Seminars - MTLE130519 3, OTWA060519 6, TORM180519 0, MTLE190519 2.
        String message = null;
        StringBuilder returnMessage = new StringBuilder();
        if (managerID.substring(0, 3).equals(MONTREAL))
        {
            logger.info("Requesting other server from Server: " + TORONTO_SERVER_NAME);
            String torrontoEvents = requestToOtherServers(managerID, null, null, 3, eventType, TORONTO_SERVER_PORT, null, null, null);
            logger.info("Requesting other server from Server: " + OTTAWA_SERVER_NAME);
            String ottawaEvents = requestToOtherServers(managerID, null, null, 3, eventType, OTTAWA_SERVER_PORT, null, null, null);
            returnMessage.append(torrontoEvents).append("NATNAT").append(ottawaEvents).append("NATNAT");

        }
        if (managerID.substring(0, 3).equals(TORONTO))
        {
            logger.info("Requesting other server from Server: " + MONTREAL_SERVER_NAME);
            String montrealEvents = requestToOtherServers(managerID, null, null, 3, eventType, MONTREAL_SERVER_PORT, null, null, null);
            logger.info("Requesting other server from Server: " + OTTAWA_SERVER_NAME);
            String ottawaEvents = requestToOtherServers(managerID, null, null, 3, eventType, OTTAWA_SERVER_PORT, null, null, null);

            returnMessage.append(ottawaEvents).append("NATNAT").append(montrealEvents).append("NATNAT");
        }
        if (managerID.substring(0, 3).equals(OTTAWA))
        {
            logger.info("Requesting other server from Server: " + MONTREAL_SERVER_NAME);
            String montrealEvents = requestToOtherServers(managerID, null, null, 3, eventType, MONTREAL_SERVER_PORT, null, null, null);
            logger.info("Requesting other server from Server: " + TORONTO_SERVER_NAME);
            String torrontoEvents = requestToOtherServers(managerID, null, null, 3, eventType, TORONTO_SERVER_PORT, null, null, null);

            returnMessage.append(torrontoEvents).append("NATNAT").append(montrealEvents).append("NATNAT");
        }

        if (!databaseMontreal.get(eventType).isEmpty())
        {
            for (Map.Entry<String, String> entry : databaseMontreal.get(eventType).entrySet())
            {
                returnMessage.append("EventID: ").append(entry.getKey()).append("| Booking Capacity ").append(entry.getValue()).append("NAT");
            }
            message = "Operation Successful, List of events retrieved for Event Type: " + eventType + " by Manager: " + managerID + "in server";
            logger.info(message);

            return returnMessage.toString().trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
        else
        {
            message = "Operation UnSuccessful, List of events not retrieved for Event Type: " + eventType + " by Manager: " + managerID + " in server ";
            logger.info(message);
            return message.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
    }

    @Override
    public synchronized String bookEvent(String customerID, String eventID, String eventType, String bookingAmount)
    {
        String newMsg = "";
        if (!customerID.substring(0, 3).equals(MONTREAL) && !customerID.substring(0, 3).equals(eventID.substring(0, 3)))
        {
            int customerBookingsCurrent = Integer.parseInt(this.nonOriginCustomerBooking(customerID, eventID));
            int customerBookingsOther = customerID.substring(0, 3).equals(OTTAWA) ? Integer.parseInt(requestToOtherServers(customerID, eventID, null, 7, null, TORONTO_SERVER_PORT, null, null, null).trim())
                    : Integer.parseInt(requestToOtherServers(customerID, eventID, null, 7, null, OTTAWA_SERVER_PORT, null, null, null).trim());

            if (customerBookingsCurrent + customerBookingsOther >= 3)
            {
                logger.log(Level.INFO, "Operation Unsuccessful, Book Event Requested by {0} for Event Type {1} with Event ID {2} cannot be booked. Customer can book as many events in his/her own city, but only at most 3 events from other cities overall in a month", new Object[]
                {
                    customerID, eventType, eventID
                });
                newMsg =  "Operation Unsuccessful, Book Event Requested by " + customerID + " for Event Type " + eventType + " with Event ID " + eventID + " cannot be booked. Customer can book as many events in his/her ownNAT"
                        + "city, but only at most 3 events from other cities overall in a month";
                return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
            }
        }
        
        if (eventID.substring(0, 3).equals(MONTREAL))
        {
            logger.log(Level.INFO, "Book Event Requested by {0} for Event Type {1} with Event ID {2}", new Object[]
            {
                customerID, eventType, eventID
            });
            HashMap< String, String> event = databaseMontreal.get(eventType);
            if (event.containsKey(eventID))
            {
                if (customerEventsMapping.containsKey(customerID) && customerEventsMapping.get(customerID).containsKey(eventType))
                {
                    if (customerEventsMapping.get(customerID).get(eventType).containsKey(eventID))
                    {
                        logger.log(Level.INFO, "Operation Unsuccessful, Book Event Requested by {0} for Event Type {1} with Event ID {2} cannot be booked. Customer already booked for this event.", new Object[]
                        {
                            customerID, eventType, eventID
                        });
                        newMsg =  "Operation Unsuccessful, Book Event Requested by " + customerID + " for Event Type " + eventType + " with Event ID " + eventID + " cannot be booked. Customer already booked for this event.";
                        return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
                    }
                }
                int bookingLeft = Integer.parseInt(event.get(eventID).trim());
                String tempBookingAmount = bookingAmount.replaceAll("[^\\d.]", "");
                int bookingRequested = Integer.parseInt(tempBookingAmount);
                if (bookingLeft >= bookingRequested)
                {
                    bookingLeft -= bookingRequested;
                    event.put(eventID, "" + bookingLeft);

                    customerEventsMapping.putIfAbsent(customerID, new HashMap<>());
                    customerEventsMapping.get(customerID).putIfAbsent(eventType, new HashMap<>());
                    customerEventsMapping.get(customerID).get(eventType).put(eventID, bookingRequested);

                    logger.log(Level.INFO, "Operation Successful, Book Event Requested by {0} for Event Type {1} with Event ID {2} has been booked.", new Object[]
                    {
                        customerID, eventType, eventID
                    });
                    newMsg =  "Operation Successful, Book Event Requested by " + customerID + " for Event Type " + eventType + " with Event ID " + eventID + " has been booked.";
                    return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
                }
                else
                {
                    logger.log(Level.INFO, "Operation Unsuccessful, Book Event Requested by {0} for Event Type {1} with Event ID {2} cannot be booked. Event Capacity < Booking Capacity Requested", new Object[]
                    {
                        customerID, eventType, eventID
                    });
                    newMsg =  "Operation Unsuccessful, Book Event Requested by " + customerID + " for Event Type " + eventType + " with Event ID " + eventID + " cannot be booked. Event Capacity < Booking Capacity Requested";
                    return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
                }

            }
            else
            {
                logger.log(Level.INFO, "Operation Unsuccessful, Book Event Requested by {0} for Event Type {1} with Event ID {2} cannot be booked. Event Does Not Exist.", new Object[]
                {
                    customerID, eventType, eventID
                });
                newMsg =  "Operation Unsuccessful, Book Event Requested by " + customerID + " for Event Type " + eventType + " with Event ID " + eventID + " cannot be booked. Event Does Not Exist.";
                return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
            }
        }
        if (eventID.substring(0, 3).equals(TORONTO))
        {
            newMsg =  requestToOtherServers(customerID, eventID, bookingAmount, 4, eventType, TORONTO_SERVER_PORT, null, null, null);
            return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
        if (eventID.substring(0, 3).equals(OTTAWA))
        {
            newMsg =  requestToOtherServers(customerID, eventID, bookingAmount, 4, eventType, OTTAWA_SERVER_PORT, null, null, null);
            return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
        return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
    }

    @Override
    public synchronized String getBookingSchedule(String customerID, String managerID)
    {
        String returnMsg = "";
        if(managerID == null || managerID.equalsIgnoreCase("Default")) managerID = "null";
        if(managerID.equals("null"))
            logger.log(Level.INFO, "Booking Schedule Requested by {0}", customerID);
        else
            logger.log(Level.INFO, "Booking Schedule Requested by {0} for customer {1}", new Object[] {managerID, customerID});
        HashMap<String, HashMap< String, Integer>> customerEvents = customerEventsMapping.get(customerID);

        if ((customerID.substring(0, 3).equals(MONTREAL) && managerID.equals("null"))||(!managerID.equals("null") && managerID.substring(0, 3).equals(MONTREAL)))
        {
            returnMsg += requestToOtherServers(customerID, null, null, 5, null, TORONTO_SERVER_PORT, "null", null, null);
            returnMsg += requestToOtherServers(customerID, null, null, 5, null, OTTAWA_SERVER_PORT, "null", null, null);
        }
        if (customerEvents != null && !customerEvents.isEmpty())
        {
            HashMap< String, Integer> customerConferenceEventID = customerEvents.get(CONFERENCE);
            HashMap< String, Integer> customerSeminarEventID = customerEvents.get(SEMINAR);
            HashMap< String, Integer> customerTradeshowEventID = customerEvents.get(TRADESHOW);

            if (customerConferenceEventID != null && !customerConferenceEventID.isEmpty())
            {
                returnMsg += "NATFor Conference Events in Montreal: ";
                for (String event : customerConferenceEventID.keySet())
                {
                    returnMsg += "NATEvent ID: " + event + " Booking for " + customerConferenceEventID.get(event);
                }
            }
            if (customerSeminarEventID != null && !customerSeminarEventID.isEmpty())
            {
                returnMsg += "NATFor Seminar Events in Montreal: ";
                for (String event : customerSeminarEventID.keySet())
                {
                    returnMsg += "NATEvent ID: " + event + " Booking for " + customerSeminarEventID.get(event);
                }
            }
            if (customerTradeshowEventID != null && !customerTradeshowEventID.isEmpty())
            {
                returnMsg += "NATFor Tradeshow Events in Montreal: ";
                for (String event : customerTradeshowEventID.keySet())
                {
                    returnMsg += "NATEvent ID: " + event + " Booking for " + customerTradeshowEventID.get(event);
                }
            }
            if (!returnMsg.trim().equals(""))
            {
                logger.log(Level.INFO, "Operation Sucessful. Records for {0} have been found", customerID);
            }
        }
        if (returnMsg.trim().equals(""))
        {
            logger.log(Level.INFO, "Records for {0} do not exist.", customerID);
            if ((customerID.substring(0, 3).equals(MONTREAL) && managerID.equals("null"))||(!managerID.equals("null") && managerID.substring(0, 3).equals(MONTREAL)))
            {
                returnMsg += "NATRecords for " + customerID + " do not exist.";
            }
        }

        return returnMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
    }

    @Override
    public synchronized String cancelEvent(String customerID, String eventID, String eventType)
    {
        String newMsg = "";
        switch (eventID.substring(0, 3))
        {
            case MONTREAL:
                if (customerEventsMapping.containsKey(customerID))
                {
                    if (customerEventsMapping.get(customerID).containsKey(eventType) && customerEventsMapping.get(customerID).get(eventType).containsKey(eventID))
                    {
                        Integer bookValue = customerEventsMapping.get(customerID).get(eventType).remove(eventID);
                        Integer currentValue = 0;
                        Integer sum = 0;
                        
                        if (databaseMontreal.get(CONFERENCE).containsKey(eventID))
                        {
                            currentValue = Integer.parseInt(databaseMontreal.get(CONFERENCE).get(eventID));
                            sum = currentValue + bookValue;
                            databaseMontreal.get(CONFERENCE).put(eventID, sum.toString());
                        }
                        else if (databaseMontreal.get(SEMINAR).containsKey(eventID))
                        {
                            currentValue = Integer.parseInt(databaseMontreal.get(SEMINAR).get(eventID));
                            sum = currentValue + bookValue;
                            databaseMontreal.get(SEMINAR).put(eventID, sum.toString());
                        }
                        else if (databaseMontreal.get(TRADESHOW).containsKey(eventID))
                        {
                            currentValue = Integer.parseInt(databaseMontreal.get(TRADESHOW).get(eventID));
                            sum = currentValue + bookValue;
                            databaseMontreal.get(TRADESHOW).put(eventID, sum.toString());
                        }
                        logger.log(Level.INFO, "This event has been removed from customer record.");
                        newMsg = "This event has been removed from customer record.";
                        return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
                    }
                }
                else
                {
                    logger.log(Level.INFO, "This event does not exist in customer record.");
                    newMsg = "This event does not exist in customer record.";
                    return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
                }
                break;
            case TORONTO:
                newMsg = requestToOtherServers(customerID, eventID, null, 6, eventType, TORONTO_SERVER_PORT, null, null, null);
                break;
            case OTTAWA:
                newMsg = requestToOtherServers(customerID, eventID, null, 6, eventType, OTTAWA_SERVER_PORT, null, null, null);
                break;
            default:
                break;
        }
        return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
    }

    @Override
    public synchronized String nonOriginCustomerBooking(String customerID, String eventID)
    {
        int numberOfCustomerEvents = 0;
        if (customerEventsMapping.containsKey(customerID) && !customerID.substring(0, 3).equals(MONTREAL))
        {
            if (customerEventsMapping.get(customerID).containsKey(CONFERENCE))
            {
                for (String currentEventID : customerEventsMapping.get(customerID).get(CONFERENCE).keySet())
                {
                    if (eventID.substring(6).equals(currentEventID.substring(6)))
                    {
                        numberOfCustomerEvents++;
                    }
                }
            }
            if (customerEventsMapping.get(customerID).containsKey(SEMINAR))
            {
                for (String currentEventID : customerEventsMapping.get(customerID).get(SEMINAR).keySet())
                {
                    if (eventID.substring(6).equals(currentEventID.substring(6)))
                    {
                        numberOfCustomerEvents++;
                    }
                }
            }
            if (customerEventsMapping.get(customerID).containsKey(TRADESHOW))
            {
                for (String currentEventID : customerEventsMapping.get(customerID).get(TRADESHOW).keySet())
                {
                    if (eventID.substring(6).equals(currentEventID.substring(6)))
                    {
                        numberOfCustomerEvents++;
                    }
                }
            }
        }
        return "" + numberOfCustomerEvents;
    }

    public synchronized String requestToOtherServers(String userID, String eventID, String bookingCapacity, int serverNumber, String eventType, int serPort, String managerId, String newEventID, String newEventType)
    {
        int serverPort = serPort;
        String stringServer = Integer.toString(serverNumber);
        DatagramSocket aSocket = null;
        String response = null;
        String userIDName = userID != null ? userID : "Default";
        String eventTypeName = eventType != null ? eventType : "Default";
        String eventIDName = eventID != null ? eventID : "Default";
        String bookingCap = bookingCapacity != null ? bookingCapacity : "Default";
        String managerID = managerId != null ? managerId : "Default";
        String new_EventID = newEventID != null ? newEventID : "Default";
        String new_EventType = newEventType != null ? newEventType : "Default";

        try
        {
            aSocket = new DatagramSocket();
            String message = userIDName.concat(" ").concat(eventIDName).concat(" ").concat(stringServer).concat(" ").concat(eventTypeName).concat(" ").concat(bookingCap).concat(" ").concat(managerID).concat(" ").concat(new_EventID).concat(" ").concat(new_EventType);
            InetAddress host = InetAddress.getByName("localhost");
            DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), host, serverPort);
            aSocket.send(sendPacket);
            logger.info("Request send " + sendPacket.getData());
            byte[] receiveBuffer = new byte[1500];
            DatagramPacket recievedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            aSocket.receive(recievedPacket);
            response = new String(recievedPacket.getData());
            logger.info("Reply received" + response);
        }
        catch (IOException e)
        {

        }
        finally
        {
            if (aSocket != null)
            {
                aSocket.close();
            }
        }
        return response;
    }

    @Override
    public synchronized String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType)
    {
        if (!newEventID.substring(0, 3).equals(MONTREAL) && customerID.substring(0, 3).equals(MONTREAL) && oldEventID.substring(0, 3).equals(MONTREAL))
        {
            int customerBookings1 = Integer.parseInt(requestToOtherServers(customerID, newEventID, null, 7, null, TORONTO_SERVER_PORT, null, null, null).trim());
            int customerBookings2 = Integer.parseInt(requestToOtherServers(customerID, newEventID, null, 7, null, OTTAWA_SERVER_PORT, null, null, null).trim());
            boolean maxAllowableInMonth = (customerBookings1 + customerBookings2 >= 3);
            if (maxAllowableInMonth)
            {
                return "Operation Unsuccessful Max 3 bookings in a given month";
            }
        }
        
        String newMsg = "";
        boolean isNewEventValid = false;
        boolean isOldEventValid = false;
        boolean isCustomerEligibleToBook = true;

        if (newEventID.substring(0, 3).equals(MONTREAL))
        {
            isNewEventValid = eventAvailable(newEventID, newEventType).trim().equals("1");
        }
        else
        {
            isNewEventValid = requestToOtherServers(customerID, oldEventID, null, 9, oldEventType, newEventID.substring(0, 3).equals(OTTAWA) ? OTTAWA_SERVER_PORT : TORONTO_SERVER_PORT, null, newEventID, newEventType).trim().equals("1");
        }

        if (!isNewEventValid)
        {
            logger.log(Level.INFO, "Operation Unsuccessful, Swap Event Requested by {0} for New Event Type {1} with New Event ID {2} with Old Event Type {3} with old Event ID {4}  cannot be swaped. "
                    + "New Event is Invalid", new Object[]
                    {
                        customerID, newEventType, newEventID, oldEventType, oldEventID
                    });
            newMsg =  "Operation Unsuccessful, Swap Event Requested by " + customerID + " for New Event Type " + newEventType + " with New Event ID " + newEventID + " with Old Event Type " + oldEventType + " with old Event ID " + oldEventID + " cannot be swaped. "
                    + "NATNew Event is Invalid";
            return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }

        if (oldEventID.substring(0, 3).equals(MONTREAL))
        {
            isOldEventValid = validateBooking(customerID, oldEventID, oldEventType).trim().equals("1");
        }
        else
        {
            isOldEventValid = requestToOtherServers(customerID, oldEventID, null, 10, oldEventType, oldEventID.substring(0, 3).equals(OTTAWA) ? OTTAWA_SERVER_PORT : TORONTO_SERVER_PORT, null, newEventID, newEventType).trim().equals("1");
        }

        if (!isOldEventValid)
        {
            logger.log(Level.INFO, "Operation Unsuccessful, Swap Event Requested by {0} for New Event Type {1} with New Event ID {2} with Old Event Type {3} with old Event ID {4}  cannot be swaped. "
                    + "Old Event is Invalid", new Object[]
                    {
                        customerID, newEventType, newEventID, oldEventType, oldEventID
                    });
            newMsg =  "Operation Unsuccessful, Swap Event Requested by " + customerID + " for New Event Type " + newEventType + " with New Event ID " + newEventID + " with Old Event Type " + oldEventType + " with old Event ID " + oldEventID + " cannot be swaped. "
                    + "NATOld Event is Invalid";
            return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
        }
        
        if(customerID.substring(0, 3).equals(MONTREAL) && newEventID.substring(0, 3).equals(MONTREAL)) isCustomerEligibleToBook = true;
        else if(!customerID.substring(0, 3).equals(oldEventID.substring(0, 3)) && !oldEventID.substring(0, 3).equals(MONTREAL)) isCustomerEligibleToBook = true;
        else if (!customerID.substring(0, 3).equals(MONTREAL) && !customerID.substring(0, 3).equals(newEventID.substring(0, 3)))
        {
            int customerBookingsCurrent = Integer.parseInt(this.nonOriginCustomerBooking(customerID, newEventID));
            int customerBookingsOther = customerID.substring(0, 3).equals(OTTAWA) ? Integer.parseInt(requestToOtherServers(customerID, newEventID, null, 7, null, TORONTO_SERVER_PORT, null, null, null).trim())
                    : Integer.parseInt(requestToOtherServers(customerID, newEventID, null, 7, null, OTTAWA_SERVER_PORT, null, null, null).trim());

            if (customerBookingsCurrent + customerBookingsOther >= 3)
            {
                isCustomerEligibleToBook = false;
                logger.log(Level.INFO, "Operation Unsuccessful, Swap Event Requested by {0} for New Event Type {1} with New Event ID {2} with Old Event Type {3} with old Event ID {4}  cannot be swaped. "
                                     + "Customer can book as many events in his/her own city, but only at most 3 events from other cities overall in a month", new Object[]
                {
                    customerID, newEventType, newEventID, oldEventType, oldEventID
                });
                newMsg =  "Operation Unsuccessful, Swap Event Requested by " + customerID + " for New Event Type " + newEventType + " with New Event ID " + newEventID + " with Old Event Type " + oldEventType + " with old Event ID " + oldEventID + " cannot be swaped. "
                        + "NATCustomer can book as many events in his/her own city, but only at most 3 events from other cities overall in a month";
                return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
            }
        }

        if (isNewEventValid && isOldEventValid && isCustomerEligibleToBook)
        {
            String msg = "";
            try
            {
                msg = cancelEvent(customerID, oldEventID, oldEventType) + "NAT" + bookEvent(customerID, newEventID, newEventType, "1") + "NAT Events Have Been Swapped";
                logger.log(Level.INFO, msg);
                logger.log(Level.INFO, "Operation successful, Swap Event Requested by {0} for New Event Type {1} with New Event ID {2} with Old Event Type {3} with old Event ID {4}  has been swaped. ", new Object[]
                {
                    customerID, newEventType, newEventID, oldEventType, oldEventID
                });
                newMsg =  "NATOperation successful, Swap Event Requested by " + customerID + " for New Event Type " + newEventType + " with New Event ID " + newEventID + " with Old Event Type " + oldEventType + " with old Event ID " + oldEventID + " has been swaped. ";
                return newMsg.trim().replaceAll("[^a-zA-Z0-9]", " ");
            }
            catch (Exception ex)
            {

            }
        }

        return "Operation Unsuccessful";
    }

    @Override
    public synchronized String eventAvailable(String eventID, String eventType)
    {
        eventType = eventType.substring(0,3).equalsIgnoreCase("CON")? CONFERENCE : eventType.substring(0,3).equalsIgnoreCase("SEM")? SEMINAR : TRADESHOW;
        return (databaseMontreal.containsKey(eventType) && databaseMontreal.get(eventType).containsKey(eventID) && Integer.parseInt(databaseMontreal.get(eventType).get(eventID)) > 0) ? "1" : "0";
    }

    @Override
    public synchronized String validateBooking(String customerID, String eventID, String eventType)
    {
        eventType = eventType.substring(0,3).equalsIgnoreCase("CON")? CONFERENCE : eventType.substring(0,3).equalsIgnoreCase("SEM")? SEMINAR : TRADESHOW;
        return (customerEventsMapping.containsKey(customerID) && customerEventsMapping.get(customerID).containsKey(eventType)  && customerEventsMapping.get(customerID).get(eventType).containsKey(eventID)) ? "1" : "0";
    }
}