import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public class Main {

    public static void main(String[] args) throws JaxmppException {

        String jid1 = "user1@localhost";
        String jid2 = "user2@localhost";

        User user1 = new User(jid1, jid2);
        User user2 = new User(jid2, jid1);

        new Thread(user1).start();
        new Thread(user2).start();

    }


}
