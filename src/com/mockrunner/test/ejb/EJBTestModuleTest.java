package com.mockrunner.test.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.TestCase;

import org.mockejb.TransactionPolicy;
import org.mockejb.jndi.MockContextFactory;

import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestModule;
import com.mockrunner.mock.ejb.EJBMockObjectFactory;
import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockQueue;
import com.mockrunner.mock.jms.MockQueueConnection;
import com.mockrunner.mock.jms.MockQueueConnectionFactory;
import com.mockrunner.mock.jms.MockQueueReceiver;
import com.mockrunner.mock.jms.MockQueueSession;
import com.mockrunner.mock.jms.MockTextMessage;
import com.mockrunner.mock.jms.MockTopic;
import com.mockrunner.mock.jms.MockTopicConnection;
import com.mockrunner.mock.jms.MockTopicConnectionFactory;
import com.mockrunner.mock.jms.MockTopicSession;
import com.mockrunner.mock.jms.MockTopicSubscriber;

public class EJBTestModuleTest extends TestCase
{
    private EJBMockObjectFactory ejbMockFactory;
    private EJBTestModule ejbModule;
    private JMSMockObjectFactory jmsMockFactory;
    private JMSTestModule jmsModule;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        ejbMockFactory = new EJBMockObjectFactory();
        ejbModule = new EJBTestModule(ejbMockFactory);
        jmsMockFactory = new JMSMockObjectFactory();
        jmsModule = new JMSTestModule(jmsMockFactory);
    }
    
	protected void tearDown() throws Exception
	{
		super.tearDown();
		MockContextFactory.revertSetAsInitial();
	}
    
    public void testLookupSessionBean() throws Exception
    {
        ejbModule.deploySessionBean("com/MyLookupTest", TestSessionBean.class);
        try
        {
            ejbModule.lookupBean("com/MyLookupTestTest");
            fail();
        }
        catch(RuntimeException exc)
        {
            //should throw exception
        }
        Object bean = ejbModule.lookupBean("com/MyLookupTest");
        assertTrue(bean instanceof TestSession);
        bean = ejbModule.lookupBean("com/MyLookupTest", new Object[] {new Integer(1)});
        assertTrue(bean instanceof TestSession);
        bean = ejbModule.lookupBean("com/MyLookupTest", new Object[] {new Integer(1), new Boolean(true)});
        assertTrue(bean instanceof TestSession);
        bean = ejbModule.lookupBean("com/MyLookupTest", "createWithPostfix", new Object[] {new Integer(1), new Boolean(true)});
        assertTrue(bean instanceof TestSession);
        assertNull(ejbModule.lookupBean("com/MyLookupTest", "createWithPostfiy", new Object[] {new Integer(1), new Boolean(true)}));
        try
        {
            ejbModule.lookupBean("com/MyLookupTestTest", new Object[] {new Boolean(true), new Integer(1)});
            fail();
        }
        catch(RuntimeException exc)
        {
            //should throw exception
        }
    }
    
	public void testLookupEntityBean() throws Exception
	{
		ejbModule.setBusinessInterfaceSuffix("Bean");
		ejbModule.setImplementationSuffix("EJB");
		ejbModule.deployEntityBean("com/AnEntityBean", TestEntityEJB.class);
		try
		{
			ejbModule.lookupBean("com/AnEntity");
			fail();
		}
		catch(RuntimeException exc)
		{
			//should throw exception
		}
		Object bean = ejbModule.lookupBean("com/AnEntityBean");
		assertTrue(bean instanceof TestEntityBean);
		bean = ejbModule.lookupBean("com/AnEntityBean", "createWithName", new Object[] {"xyz"});
		assertTrue(bean instanceof TestEntityBean);
		assertNull(ejbModule.lookupBean("com/AnEntityBean", new Object[] {"xyz"}));
	}
        
    public void testDeploySessionBeanClass() throws Exception
    {
        try
        {
            ejbModule.deploySessionBean("test", EJBTestModuleTest.class);
            fail();
        }
        catch(RuntimeException exc)
        {
            //should throw exception
        }
        try
        {
            ejbModule.setHomeInterfacePackage("com.mockrunner.test");
            ejbModule.deploySessionBean("test", TestSessionBean.class);
            fail();
        }
        catch(RuntimeException exc)
        {
            //should throw exception
        }
        try
        {
            ejbModule.setHomeInterfacePackage("com.mockrunner.test.ejb");
            ejbModule.setBusinessInterfacePackage("com.mockrunner.test");
            ejbModule.deploySessionBean("test", TestSessionBean.class);
            fail();
        }
        catch(RuntimeException exc)
        {
            //should throw exception
        }
        try
        {
            ejbModule.setInterfacePackage("com.mockrunner.test.ejb");
            ejbModule.setHomeInterfaceSuffix("Factory");
            ejbModule.deploySessionBean("test", TestSessionBean.class);
            fail();
        }
        catch(RuntimeException exc)
        {
            //should throw exception
        }
        try
        {
            ejbModule.setHomeInterfaceSuffix("Home");
            ejbModule.setBusinessInterfaceSuffix("Business");
            ejbModule.deploySessionBean("test", TestSessionBean.class);
            fail();
        }
        catch(RuntimeException exc)
        {
            //should throw exception
        }
        try
        {
            ejbModule.setBusinessInterfaceSuffix("");
            ejbModule.setImplementationSuffix("Impl");
            ejbModule.deploySessionBean("test", TestSessionBean.class);
            fail();
        }
        catch(RuntimeException exc)
        {
            //should throw exception
        }
        ejbModule.setImplementationSuffix("Bean");
        ejbModule.deploySessionBean("test", TestSessionBean.class);
        InitialContext context = new InitialContext();
        Object home = context.lookup("test");
        assertNotNull(home);
        assertTrue(home instanceof TestSessionHome);
    }
    
	public void testDeploySessionBeanObject() throws Exception
	{
		try
		{
			ejbModule.deploySessionBean("test", "abc");
			fail();
		}
		catch(RuntimeException exc)
		{
			//should throw exception
		}
		try
		{
			ejbModule.setHomeInterfacePackage("com.mockrunner.test");
			ejbModule.deploySessionBean("test", new TestSessionBean());
			fail();
		}
		catch(RuntimeException exc)
		{
			//should throw exception
		}
		try
		{
			ejbModule.setInterfacePackage("com.mockrunner.test.ejb");
			ejbModule.setHomeInterfaceSuffix("xyz");
			ejbModule.deploySessionBean("test", new TestSessionBean());
			fail();
		}
		catch(RuntimeException exc)
		{
			//should throw exception
		}
		ejbModule.setHomeInterfaceSuffix("Home");
		ejbModule.setImplementationSuffix("Bean");
		ejbModule.deploySessionBean("test", new TestSessionBean());
		InitialContext context = new InitialContext();
		Object home = context.lookup("test");
		assertNotNull(home);
		assertTrue(home instanceof TestSessionHome);
	}
    
	public void testDeployEntityBean() throws Exception
	{
		try
		{
			ejbModule.deployEntityBean("test", String.class);
			fail();
		}
		catch(RuntimeException exc)
		{
			//should throw exception
		}
		try
		{
			ejbModule.setHomeInterfacePackage("com.mockrunner");
			ejbModule.deployEntityBean("test", TestEntityEJB.class);
			fail();
		}
		catch(RuntimeException exc)
		{
			//should throw exception
		}
		try
		{
			ejbModule.deployEntityBean("test", TestEntityEJB.class);
			fail();
		}
		catch(RuntimeException exc)
		{
			//should throw exception
		}
		try
		{
			ejbModule.setBusinessInterfaceSuffix("");
			ejbModule.setImplementationSuffix("EJB");
			ejbModule.deployEntityBean("test", TestEntityEJB.class);
			fail();
		}
		catch(RuntimeException exc)
		{
			//should throw exception
		}
		ejbModule.setImplementationSuffix("EJB");
		ejbModule.setBusinessInterfaceSuffix("Bean");
		ejbModule.setHomeInterfacePackage("com.mockrunner.test.ejb");
		ejbModule.deployEntityBean("test", TestEntityEJB.class);
		InitialContext context = new InitialContext();
		Object home = context.lookup("test");
		assertNotNull(home);
		assertTrue(home instanceof TestEntityHome);
	}
	
	public void testDeployMessageBeanBoundToContext() throws Exception
	{
		MockQueueConnectionFactory queueFactory = jmsMockFactory.getMockQueueConnectionFactory();
		MockQueue queue = jmsMockFactory.getDestinationManager().createQueue("queue");
		ejbModule.deployMessageBean("factoryJNDIQueueFactory", "destinationJNDIQueue", queueFactory, queue, new TestMessageBean());
		assertSame(queueFactory, ejbModule.lookup("factoryJNDIQueueFactory"));
		assertSame(queue, ejbModule.lookup("destinationJNDIQueue"));
		MockTopicConnectionFactory topicFactory = jmsMockFactory.getMockTopicConnectionFactory();
		MockTopic topic = jmsMockFactory.getDestinationManager().createTopic("topic");
		ejbModule.deployMessageBean("factoryJNDITopicFactory", "destinationJNDITopic", topicFactory, topic, new TestMessageBean());
		assertSame(topicFactory, ejbModule.lookup("factoryJNDITopicFactory"));
		assertSame(topic, ejbModule.lookup("destinationJNDITopic"));
	}
	
	public void testDeployMessageBeanQueueConnectionCreated() throws Exception
	{
		MockQueueConnectionFactory queueFactory = jmsMockFactory.getMockQueueConnectionFactory();
		MockQueue queue = jmsMockFactory.getDestinationManager().createQueue("queue");
		Object messageBean = new TestMessageBean();
		ejbModule.deployMessageBean("factoryJNDIQueueFactory", "destinationJNDIQueue", queueFactory, queue, messageBean);
		assertNotNull(queueFactory.getConnection(0));
		assertTrue(queueFactory.getConnection(0) instanceof MockQueueConnection);
		MockQueueConnection queueConnection = queueFactory.getLatestQueueConnection();
		assertNotNull(queueConnection.getQueueSession(0));
		assertTrue(queueConnection.getQueueSession(0) instanceof MockQueueSession);
		MockQueueSession queueSession = (MockQueueSession)queueConnection.getQueueSession(0);
		MockQueueReceiver queueReceiver = queueSession.getQueueTransmissionManager().getQueueReceiver("queue");
		assertNotNull(queueReceiver);
		assertNotNull(queueReceiver.getMessageListener());
	}
	
	public void testDeployMessageBeanTopicConnectionCreated() throws Exception
	{
		MockTopicConnectionFactory topicFactory = jmsMockFactory.getMockTopicConnectionFactory();
		MockTopic topic = jmsMockFactory.getDestinationManager().createTopic("topic");
		Object messageBean = new TestMessageBean();
		ejbModule.deployMessageBean("factoryJNDITopicFactory", "destinationJNDITopic", topicFactory, topic, messageBean);
		assertNotNull(topicFactory.getConnection(0));
		assertTrue(topicFactory.getConnection(0) instanceof MockTopicConnection);
		MockTopicConnection topicConnection = topicFactory.getLatestTopicConnection();
		assertNotNull(topicConnection.getTopicSession(0));
		assertTrue(topicConnection.getTopicSession(0) instanceof MockTopicSession);
		MockTopicSession topicSession = (MockTopicSession)topicConnection.getTopicSession(0);
		MockTopicSubscriber topicSubscriber = topicSession.getTopicTransmissionManager().getTopicSubscriber(0);
		assertNotNull(topicSubscriber);
		assertNotNull(topicSubscriber.getMessageListener());
	}
	
	public void testDeployMessageBeanSendMessage() throws Exception
	{
		MockQueueConnectionFactory queueFactory = jmsMockFactory.getMockQueueConnectionFactory();
		MockQueue queue = jmsMockFactory.getDestinationManager().createQueue("queue");
		TestMessageBean messageBean = new TestMessageBean();
		ejbModule.deployMessageBean("factoryJNDIQueueFactory", "destinationJNDIQueue", queueFactory, queue, messageBean);
		QueueSession session = queueFactory.createQueueConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		QueueSender sender = session.createSender(queue);
		MockTextMessage message = new MockTextMessage("message");
		sender.send(message);
		assertSame(message, messageBean.getMessage());
	}
		
    public void testTransaction() throws Exception
    {
        ejbModule.deploySessionBean("mybean", new TestSessionBean(), TransactionPolicy.REQUIRED);
        InitialContext context = new InitialContext();
        Object home = context.lookup("mybean");
        TestSessionHome testHome = (TestSessionHome)PortableRemoteObject.narrow(home, TestSessionHome.class);
        TestSession testBean = (TestSession)testHome.create();
        testBean.test(false);
        ejbModule.verifyCommitted();
        ejbModule.verifyNotMarkedForRollback();
        ejbModule.verifyNotRolledBack();
        ejbModule.resetUserTransaction();
        testBean.test(true);
        ejbModule.verifyNotCommitted();
        ejbModule.verifyMarkedForRollback();
        ejbModule.verifyRolledBack();
		ejbModule.setBusinessInterfaceSuffix("Bean");
		ejbModule.setImplementationSuffix("EJB");
		ejbModule.deployEntityBean("myEntityBean", TestEntityEJB.class, TransactionPolicy.REQUIRED);
		ejbModule.resetUserTransaction();
		home = context.lookup("myEntityBean");
		TestEntityHome testEntityHome = (TestEntityHome)PortableRemoteObject.narrow(home, TestEntityHome.class);
		TestEntityBean testEntity = (TestEntityBean)testEntityHome.create();
		testEntity.setName("aName");
		ejbModule.verifyCommitted();
		ejbModule.verifyNotMarkedForRollback();
		ejbModule.verifyNotRolledBack();
    }
    
	public void testTransactionMessageBean() throws Exception
	{
		MockTopicConnectionFactory topicFactory = jmsMockFactory.getMockTopicConnectionFactory();
		MockTopic topic = jmsMockFactory.getDestinationManager().createTopic("topic");
		Object messageBean = new TestMessageBean();
		ejbModule.deployMessageBean("factoryJNDITopicFactory", "destinationJNDITopic", topicFactory, topic, messageBean, TransactionPolicy.REQUIRED);
		TopicSession session = topicFactory.createTopicConnection().createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicPublisher publisher = session.createPublisher(topic);
		MockTextMessage message = new MockTextMessage("message");
		publisher.publish(message);
		ejbModule.verifyCommitted();
		ejbModule.verifyNotMarkedForRollback();
		ejbModule.verifyNotRolledBack();
		ejbModule.resetUserTransaction();
		message = new MockTextMessage("doRollback");
		publisher.publish(message);
		ejbModule.verifyNotCommitted();
		ejbModule.verifyMarkedForRollback();
		ejbModule.verifyRolledBack();
	}
    
    public static class TestSessionBean implements SessionBean
    {
        private SessionContext sessionContext;
        
        public void test(boolean rollback)
        {
            if(rollback) sessionContext.setRollbackOnly();
        }
        
        public void ejbCreate() throws CreateException
        {
    
        }
        
        public void ejbCreate(int testInt) throws CreateException
        {

        }
        
        public void ejbCreate(int testInt, Boolean testBoolean) throws CreateException
        {

        }
        
        public void ejbCreateWithPostfix(int testInt, Boolean testBoolean) throws CreateException
        {

        }
        
        public void ejbActivate() throws EJBException, RemoteException
        {

        }

        public void ejbPassivate() throws EJBException, RemoteException
        {

        }

        public void ejbRemove() throws EJBException, RemoteException
        {

        }

        public void setSessionContext(SessionContext context) throws EJBException, RemoteException
        {
            sessionContext = context;
        }
    }
    
    public static interface TestSession extends javax.ejb.EJBObject
    {
        public void test(boolean rollback) throws RemoteException;
    }
    
    public static interface TestSessionHome extends javax.ejb.EJBHome
    {
        public TestSession create() throws CreateException, RemoteException;
        
        public TestSession create(int testInt) throws CreateException, RemoteException;
        
        public TestSession create(int testInt, Boolean testBoolean) throws CreateException, RemoteException;
    
        public TestSession createWithPostfix(int testInt, Boolean testBoolean) throws CreateException, RemoteException;
    }
    
	public static abstract class TestEntityEJB implements EntityBean
	{
		private EntityContext entityContext;
		
		public abstract String getName();
		public abstract void setName(String name);
    
		public String ejbCreate() throws CreateException
		{
			return "testPk";
		}
		
		public void ejbPostCreate() throws CreateException
		{

		}
		
		public String ejbCreateWithName(String name) throws CreateException
		{
			return name;
		}
		
		public void ejbPostCreateWithName(String name) throws CreateException
		{

		}
    
		public void ejbActivate() throws EJBException, RemoteException
		{

		}

		public void ejbPassivate() throws EJBException, RemoteException
		{

		}

		public void ejbRemove() throws EJBException, RemoteException
		{

		}

		public void setEntityContext(EntityContext context) throws EJBException, RemoteException
		{
			entityContext = context;
		}
		
		public void unsetEntityContext() throws EJBException, RemoteException
		{
			entityContext = null;
		}
		
		public void ejbLoad() throws EJBException, RemoteException
		{

		}

		public void ejbStore() throws EJBException, RemoteException
		{

		}
	}

	public static interface TestEntityBean extends javax.ejb.EJBObject
	{
		public String getName() throws RemoteException;
		public void setName(String name) throws RemoteException;
	}

	public static interface TestEntityHome extends javax.ejb.EJBHome
	{
		public TestEntityBean create() throws CreateException, RemoteException;
		public TestEntityBean createWithName(String name) throws CreateException, RemoteException;
	}
	
	public static class TestMessageBean implements MessageDrivenBean, MessageListener
	{
		private MessageDrivenContext messageContext;
		private Message message;
		
		public void onMessage(Message message)
		{
			try
			{
				if((message instanceof TextMessage) && (((TextMessage)message).getText().equals("doRollback")))
				{
					messageContext.setRollbackOnly();
				}
			}
			catch(JMSException exc)
			{
				throw new RuntimeException(exc);
			}
			this.message = message;
		}
		
		public Message getMessage()
		{
			return message;
		}
		
		public void ejbCreate()
		{
	    
		}
	   
		public void ejbRemove()
		{
	    
		}

		public void setMessageDrivenContext(MessageDrivenContext context) throws EJBException
		{
			messageContext = context;
		}
	}
}
