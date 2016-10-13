package se.moneymaker.util;

import com.sun.mail.smtp.SMTPTransport;
import java.security.Security;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import se.moneymaker.enums.LogLevelEnum;

public final class AliveLog {

    private static final String CLASSNAME = AliveLog.class.getName();
    private final long heartbeat;
    private long reportTime;
    private final String message;
    private final String application;

    public AliveLog(long heartbeat, String application) {
        this.heartbeat = heartbeat;
        this.message = application + " is not responding. Exiting application.";
        this.application = application;
        iAmAlive();
    }

    public String getApplication() {
        return application;
    }

    public void iAmAlive() {
        reportTime = Calendar.getInstance().getTimeInMillis();
    }

    public void isAlive() {
        final String METHOD = "isAlive";
        long currentTimeMilliSeconds = Calendar.getInstance().getTimeInMillis();
        if (currentTimeMilliSeconds - reportTime > heartbeat) {
            Log.logMessage(CLASSNAME, METHOD, message, LogLevelEnum.CRITICAL, false);
            try {
                send("aliveobserver", "iamalive", "aliveobserver@gmail.com", "", message, "");
            } catch (MessagingException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
            }
            System.exit(0);
        }
    }

    public void send(final String username, final String password, String recipientEmail, String ccEmail, String title, String message) throws AddressException, MessagingException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.smtps.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtps.auth", "true");

        /*
         If set to false, the QUIT command is sent and the connection is immediately closed. If set 
         to true (the default), causes the transport to wait for the response to the QUIT command.

         ref :   http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
         http://forum.java.sun.com/thread.jspa?threadID=5205249
         smtpsend.java - demo program from javamail
         */
        props.put("mail.smtps.quitwait", "false");

        Session session = Session.getInstance(props, null);

        // -- Create a new message --
        final MimeMessage msg = new MimeMessage(session);

        // -- Set the FROM and TO fields --
        msg.setFrom(new InternetAddress(username + "@gmail.com"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));

        if (ccEmail.length() > 0) {
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail, false));
        }

        msg.setSubject(title);
        msg.setText(message, "utf-8");
        msg.setSentDate(new Date());

        SMTPTransport t = (SMTPTransport) session.getTransport("smtps");

        t.connect("smtp.gmail.com", username, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();
    }
}
