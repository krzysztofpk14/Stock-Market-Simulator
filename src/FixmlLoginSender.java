import fixml.generated.FIXML;
import fixml.generated.UserReq;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;

public class FixmlLoginJaxbClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 24445;

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream()), true)) {

            System.out.println("Connected to " + host + ":" + port);

            // === Create FIXML root object ===
            FIXML fixml = new FIXML();
            fixml.setV("5.0");
            fixml.setR("20080317");
            fixml.setS("20080314");

            // === Create UserReq ===
            UserReq userReq = new UserReq();
            userReq.setUserReqID("0");
            userReq.setUserReqTyp(1); // 1 = logon
            userReq.setUsername("BOS");
            userReq.setPassword("BOS");

            // Add UserReq as the main content of FIXML
            fixml.setUserReq(userReq);

            // === Marshal to XML string ===
            JAXBContext context = JAXBContext.newInstance(FIXML.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            StringWriter sw = new StringWriter();
            marshaller.marshal(fixml, sw);
            String xml = sw.toString();

            System.out.println("Sending FIXML login:\n" + xml);

            // === Send over socket ===
            out.println(xml);
            out.flush();
            System.out.println("Login message sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
