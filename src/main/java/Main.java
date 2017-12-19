import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public class Main {

    public static void main(String[] args) throws JaxmppException {

        User user1 = new User("aws.ildar0@localhost");
        User user2 = new User("aws.ubuntuvm0@localhost");

        new Thread(user1).start();
        new Thread(user2).start();

    }


}
