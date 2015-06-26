package com.hazelcast.jca.examples.servlet;

import com.hazelcast.jca.HazelcastConnection;
import com.hazelcast.jca.examples.mdb.ExampleBean;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet("/HelloWorld")
public class HelloWorld extends HttpServlet {

    private final static Logger log = Logger.getLogger(HelloWorld.class.getName());

    private static final long serialVersionUID = -8314035702649252239L;

    @Resource(mappedName = "java:/HazelcastCF")
    protected ConnectionFactory connectionFactory;

    @Inject
    protected ExampleBean exampleBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        exampleBean.insert("key", "val");
        HazelcastConnection connection = getConnection();
        Object val = connection.getMap("txmap").get("key");
        PrintWriter out = resp.getWriter();
        closeConnection(connection);
        out.write("Example: " + val);

    }


    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private HazelcastConnection getConnection() {
        try {
            return (HazelcastConnection) connectionFactory.getConnection();
        } catch (ResourceException e) {
            throw new RuntimeException("Error while getting Hazelcast connection", e);
        }
    }

    private void closeConnection(HazelcastConnection hzConn) {
        if (hzConn != null) {
            try {
                hzConn.close();
            } catch (ResourceException e) {
                log.log(Level.WARNING, "Error while closing Hazelcast connection.", e);
            }
        }
    }

}
