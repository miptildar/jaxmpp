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

    String jid;

    Jaxmpp jaxmpp;

    String jid1 = "aws.ubuntuvm0@localhost";
    String jid2 = "aws.ildar0@localhost";

    public User(String jid){
        System.out.println("Create User "+jid);
        this.jid = jid;
        jaxmpp = new Jaxmpp();
        configureConnection();
    }



    @Override
    public void run() {
        System.out.println("run() "+jid);
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
                            System.out.println(jid+" RECEIVED "+message.getBody());
                        } catch (XMLException e) {
                            e.printStackTrace();
                        }
                    }
                });


        try {
            System.out.println(jid+" Logging in..");
            jaxmpp.login();
        } catch (JaxmppException e) {
            e.printStackTrace();
        }







        while (!jaxmpp.isConnected()){
            System.out.println(jid+" isConnected "+jaxmpp.isConnected());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (jaxmpp.isConnected()) {
            String receiver = jid.equals(jid1) ? jid2 : jid1;
            System.out.println("Sending message from "+jid+" to "+receiver);


            try {
                jaxmpp.getModule(MessageModule.class).sendMessage(JID.jidInstance(receiver), "Test", "This is a test from "+jid);
            } catch (JaxmppException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
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
        jaxmpp.getConnectionConfiguration().setServer("localhost");
        jaxmpp.getConnectionConfiguration().setPort(5222);
        jaxmpp.getConnectionConfiguration().setUseSASL(true);

        jaxmpp.getProperties().setUserProperty(SessionObject.DOMAIN_NAME, "localhost");
        jaxmpp.getProperties().setUserProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance(jid));
        jaxmpp.getProperties().setUserProperty(SessionObject.PASSWORD, "ildar");


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