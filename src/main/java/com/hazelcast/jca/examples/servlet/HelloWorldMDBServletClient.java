//package com.hazelcast.jca.examples.servlet;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.Random;
//
//import javax.annotation.Resource;
//import javax.jms.Connection;
//import javax.jms.ConnectionFactory;
//import javax.jms.Destination;
//import javax.jms.JMSException;
//import javax.jms.MessageProducer;
//import javax.jms.Queue;
//import javax.jms.Session;
//import javax.jms.TextMessage;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//
//@WebServlet("/HelloWorldMDBServletClient")
//public class HelloWorldMDBServletClient extends HttpServlet {
//
//    private static final long serialVersionUID = -8314035702649252239L;
//
//    private static final int MSG_COUNT = 1;
//
//    @Resource(mappedName = "java:/ConnectionFactory")
//    private ConnectionFactory connectionFactory;
//
//    @Resource(mappedName = "java:/queue/HELLOWORLDMDBQueue")
//    private Queue queue;
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Random random=new Random(System.currentTimeMillis());
//        resp.setContentType("text/html");
//        PrintWriter out = resp.getWriter();
//        Connection connection = null;
//        out.write("<h1>Example of Hazelcast JCA with MDB and H2 DB,  demonstrates usage of XA transactions</h1>");
//        try {
//            Destination destination=queue;
//            int k=Math.abs(random.nextInt());
//            if (req.getParameterMap().keySet().contains("rollback")) {
//                k=-1*k;
//            }
//
//            out.write("<p>Sending messages to <em>" + destination + "</em></p>");
//            connection = connectionFactory.createConnection();
//            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//            MessageProducer messageProducer = session.createProducer(destination);
//            connection.start();
//            out.write("<h2>Following messages will be send to the destination:</h2>");
//            TextMessage message = session.createTextMessage();
//            for (int i = 0; i < MSG_COUNT; i++) {
//                message.setText("This is message " + k);
//                message.setIntProperty("counter",k);
//                messageProducer.send(message);
//                out.write("Message (" + k + "): " + message.getText() + "</br>");
//            }
//            out.write("<p><i>Go to your JBoss EAP server console or log to see the result of messages processing</i></p>");
//
//        } catch (JMSException e) {
//            e.printStackTrace();
//            out.write("<h2>A problem occurred during the delivery of this message</h2>");
//            out.write("</br>");
//            out.write("<p><i>Go your the JBoss EAP server console or log to see the error stack trace</i></p>");
//        } finally {
//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (JMSException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (out != null) {
//                out.close();
//            }
//        }
//    }
//
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        doGet(req, resp);
//    }
//
//}
