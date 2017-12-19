import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class User implements Runnable{

    private String myJID;

    private String friendsJID;

    private Jaxmpp jaxmpp;

    public User(String jid, String friendsJID){
        System.out.println("Create User "+jid);
        this.myJID = jid;
        this.friendsJID = friendsJID;

        jaxmpp = new Jaxmpp();
        configureConnection();
    }



    @Override
    public void run() {
        System.out.println("run() "+myJID);

        // Presence is important! Because this is the way how XMPP server gets to know that the client is online
        try {
            tigase.jaxmpp.j2se.Presence.initialize(jaxmpp);
        } catch (JaxmppException e) {
            e.printStackTrace();
        }

        jaxmpp.getModulesManager().register(new MessageModule());

        jaxmpp.getEventBus().addHandler(MessageModule.MessageReceivedHandler.MessageReceivedEvent.class,
                new MessageModule.MessageReceivedHandler() {

                    public void onMessageReceived(SessionObject sessionObject, Chat chat, Message message) {
                        try {
                            System.out.println(myJID+" RECEIVED "+message.getBody());
                        } catch (XMLException e) {
                            e.printStackTrace();
                        }
                    }
                });


        try {
            System.out.println(myJID+" Logging in..");
            jaxmpp.login();
        } catch (JaxmppException e) {
            e.printStackTrace();
        }


        // dumb loop
        while (!jaxmpp.isConnected()){
            System.out.println(myJID+" isConnected "+jaxmpp.isConnected());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if (jaxmpp.isConnected()) {
            System.out.println("Sending message from "+myJID+" to "+friendsJID);

            try {
                jaxmpp.getModule(MessageModule.class).sendMessage(JID.jidInstance(friendsJID), "Test", "This is a test from "+myJID);
            } catch (JaxmppException e) {
                e.printStackTrace();
            }

            try {
                jaxmpp.disconnect();
            } catch (JaxmppException e) {
                e.printStackTrace();
            }
        }
    }


    public void configureConnection(){
        jaxmpp.getConnectionConfiguration().setServer(Config.server);
        jaxmpp.getConnectionConfiguration().setPort(Config.port);
        jaxmpp.getConnectionConfiguration().setUseSASL(true);

        jaxmpp.getProperties().setUserProperty(SessionObject.DOMAIN_NAME, Config.domain);
        jaxmpp.getProperties().setUserProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance(myJID));
        jaxmpp.getProperties().setUserProperty(SessionObject.PASSWORD, "passw0rd");


        jaxmpp.getConnectionConfiguration().setDisableTLS(false);

        SSLContext sslContext;
        SSLSocketFactory sslSocketFactory;

        try {
            sslContext = createSSLContext();
            sslSocketFactory = sslContext.getSocketFactory();
            jaxmpp.getProperties().setUserProperty(SocketConnector.SSL_SOCKET_FACTORY_KEY, sslSocketFactory);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }


    private SSLContext createSSLContext() throws KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException {


        // This TrustManager trusts all certificates. For development purposes only.
        TrustManager localTrustManager = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                System.out.println("X509TrustManager#getAcceptedIssuers");
                return new X509Certificate[]{};
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                System.out.println("X509TrustManager#checkServerTrusted");
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                System.out.println("X509TrustManager#checkClientTrusted");
            }
        };


        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{localTrustManager}, new SecureRandom());

        return sslContext;
    }

}