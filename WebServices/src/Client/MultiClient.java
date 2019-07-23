/**
 * CONCORDIA UNIVERSITY
 * DEPARTMENT OF COMPUTER SCIENCE AND SOFTWARE ENGINEERING
 * COMP 6231, Summer 2019 Instructor: Sukhjinder K. Narula
 * ASSIGNMENT 1
 * Issued: May 14, 2019 Due: Jun 3, 2019
 */
package Client;

import CommonUtils.CommonUtils;
import static CommonUtils.CommonUtils.MONTREAL;
import static CommonUtils.CommonUtils.TORONTO;
import ServerInterfaces.WebInterface;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 *
 * @author Natheepan Ganeshamoorthy
 */
public class MultiClient {

    public static void main(String[] args)
    {
        Runnable runnable1 = () ->
        {
            try
            {
                String response = getService("MTLC1212").bookEvent("MTLC1212", "MTLE031219", CommonUtils.CONFERENCE, "1");
                System.out.println(Thread.currentThread().getName() + ": " + " Response from server: " + response.replaceAll("NAT", "\n"));
            }
            catch (MalformedURLException e)
            {

            }
        };

        Runnable runnable2 = () ->
        {
            try
            {
                String response = getService("MTLC1212").bookEvent("MTLC1212", "MTLM130722", CommonUtils.CONFERENCE, "1");
                System.out.println(Thread.currentThread().getName() + ": " + " Response from server: " + response.replaceAll("NAT", "\n"));
            }
            catch (MalformedURLException e)
            {

            }
        };

        Runnable runnable3 = () ->
        {
            try
            {
                String response = getService("MTLC1212").swapEvent("MTLC1212", "MTLM130722", CommonUtils.CONFERENCE, "MTLE031219", CommonUtils.CONFERENCE);
                System.out.println(Thread.currentThread().getName() + ": " + " Response from server: " + response.replaceAll("NAT", "\n"));
            }
            catch (MalformedURLException e)
            {

            }
        };

        Runnable runnable4 = () ->
        {
            try
            {
                String response = getService("MTLC1212").swapEvent("MTLC1212", "MTLM130720", CommonUtils.CONFERENCE, "MTLM130722", CommonUtils.CONFERENCE);
                System.out.println(Thread.currentThread().getName() + ": " + " Response from server: " + response.replaceAll("NAT", "\n"));
            }
            catch (MalformedURLException e)
            {

            }
        };

        Runnable runnable5 = () ->
        {
            try
            {
                String response = getService("MTLC1212").cancelEvent("MTLC1212", "MTLE031219", CommonUtils.CONFERENCE);
                System.out.println(Thread.currentThread().getName() + ": " + " Response from server: " + response.replaceAll("NAT", "\n"));
            }
            catch (MalformedURLException e)
            {

            }
        };

        Runnable runnable6 = () ->
        {
            try
            {
                String response = getService("MTLC1212").cancelEvent("MTLC1212", "MTLM130722", CommonUtils.CONFERENCE);
                System.out.println(Thread.currentThread().getName() + ": " + " Response from server: " + response.replaceAll("NAT", "\n"));
            }
            catch (MalformedURLException e)
            {

            }
        };

        Thread thread1 = new Thread(runnable1);
        thread1.setName("Thread 1");

        Thread thread2 = new Thread(runnable2);
        thread2.setName("Thread 2");

        Thread thread3 = new Thread(runnable3);
        thread3.setName("Thread 3");

        Thread thread4 = new Thread(runnable4);
        thread4.setName("Thread 4");

        thread1.start();
        thread2.start();
//        thread3.start();
//        thread4.start();
        while (thread1.isAlive())
        {
        }
        while (thread2.isAlive())
        {
        }

        thread3.start();
        thread4.start();

//        Thread thread5 = new Thread(runnable5);
//        thread5.setName("Thread 5");
//
//        Thread thread6 = new Thread(runnable6);
//        thread6.setName("Thread 6");
//        
//        thread5.start();
//        thread6.start();
    }

    private static WebInterface getService(String clientID) throws MalformedURLException
    {
        String url = "http://localhost:808" + (clientID.substring(0, 3).equals(MONTREAL) ? "0/montreal" : clientID.substring(0, 3).equals(TORONTO) ? "2/toronto" : "1/ottawa") + "?wsdl";
        URL addURL = new URL(url);
        QName addQName = new QName("http://ServerImpl/", (clientID.substring(0, 3).equals(MONTREAL) ? "MontrealServerImplService" : clientID.substring(0, 3).equals(TORONTO) ? "TorontoServerImplService" : "OttawaServerImplService"));
        Service service = Service.create(addURL, addQName);
        WebInterface webInterface = service.getPort(WebInterface.class);
        return webInterface;
    }

}
