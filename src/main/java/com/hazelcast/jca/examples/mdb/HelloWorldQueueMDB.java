package com.hazelcast.jca.examples.mdb;

import com.hazelcast.core.TransactionalMap;
import com.hazelcast.jca.HazelcastConnection;
import com.hazelcast.jca.examples.CustomRuntimeException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.sql.DataSource;

@MessageDriven(name = "HelloWorldQueueMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/HELLOWORLDMDBQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class HelloWorldQueueMDB implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(HelloWorldQueueMDB.class.toString());

    @Resource(mappedName = "java:jboss/datasources/ExampleDS")
    protected DataSource h2Datasource;


    @Resource(mappedName = "java:/HazelcastCF")
    protected ConnectionFactory connectionFactory;

    protected HazelcastConnection getConnection() throws ResourceException {
        HazelcastConnection c = (HazelcastConnection) connectionFactory.getConnection();
        return c;
    }


    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message rcvMessage) {
        TextMessage msg = null;
        HazelcastConnection hzConn = null;
        try {
            if (rcvMessage instanceof TextMessage) {
                msg = (TextMessage) rcvMessage;

                hzConn = getConnection();

                TransactionalMap<Object,Object> txmap = hzConn.getTransactionalMap("txmap");

                if(txmap != null){
                    int counter = msg.getIntProperty("counter");
                    txmap.put(counter,msg.getText());

                    insertSqlData(counter);

                    displaySQLdata();
                    if(counter <0){
                       throw new CustomRuntimeException("ROLLBACK WAS A DREAM, BUT BECOME REALITY");
                    }

                    LOGGER.info("MAP Size: " + txmap.size());
                }

                LOGGER.info("Received Message from queue: " + msg.getText());
            } else {
                LOGGER.warning("Message of wrong type: " + rcvMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        } catch (ResourceException er) {
            throw new RuntimeException(er);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {

            if (hzConn != null) {
                try {
                    hzConn.close();
                } catch (ResourceException e) {
                    LOGGER.warning("CANNOT CLOSE HZ CONN");
                }
            }
        }

    }

    private void displaySQLdata() throws NamingException, SQLException {

        Connection con = h2Datasource.getConnection();
        Statement stmt = null;

        try {
            stmt = con.createStatement();

            ResultSet resultSet = null;
            try {
                resultSet = stmt.executeQuery("SELECT * FROM TEST");


                while (resultSet.next()){
                    LOGGER.info(resultSet.getRow()+"..."+resultSet.getString("A"));
                }
            } catch (SQLException e) {
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            con.close();
        }
    }
    private void insertSqlData(int data) throws NamingException, SQLException {

        Connection con = h2Datasource.getConnection();
        Statement stmt = null;
        try {
            stmt = con.createStatement();

            try {
                stmt.execute("create table TEST (A varchar(50))");
            } catch (SQLException e) {
                //ignore table exists
            }

            stmt = con.createStatement();
            stmt.execute("INSERT INTO TEST VALUES ('"+data+"')");
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            con.close();
        }
    }
}
