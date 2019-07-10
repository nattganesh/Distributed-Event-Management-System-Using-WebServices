package ServerInterfaces;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)

public interface WebInterface 
{

    String addEvent(String eventID, String eventType, String bookingCapacity, String managerID);

    String removeEvent(String eventID, String eventType, String managerID);

    String listEventAvailability(String eventType, String managerID);

    String bookEvent(String customerID, String eventID, String eventType, String bookingAmount);

    String getBookingSchedule(String customerID, String managerID);

    String cancelEvent(String customerID, String eventID, String eventType);

    String nonOriginCustomerBooking(String customerID, String eventID);

    String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType);

    String eventAvailable(String eventID, String eventType);

    String validateBooking(String customerID, String eventID, String eventType);

}
