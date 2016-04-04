package maqs.ehs.form;

import maqs.ehs.util.AppProperties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingException;
import java.io.File;
import java.util.Properties;
import java.util.Hashtable;

public class EmailRemotePublisherImpl implements RemotePublisher {
    public boolean publishRemote( File file, ResultCollector resultCollector, ServerInfo targetServerInfo, String destinationDir ) {

        String host = targetServerInfo.getHost();

        String from = "aronracho@yahoo.com";
        String to = targetServerInfo.getUsername() + "@" + host;
        String fileAttachment = file.getAbsolutePath();
        host = getMailServer( host );
        System.out.println ( host );

        // Get system properties
        Properties props = System.getProperties();

        // Setup mail server
        props.put( "mail.smtp.host", host );

        // Get session
        Session session =
                Session.getInstance( props, null );

        try {
            // Define message
            MimeMessage message = new MimeMessage( session );
            message.setFrom( new InternetAddress( from ) );
            message.addRecipient( Message.RecipientType.TO, new InternetAddress( to ) );
            message.setSubject( "Record export drop from " + AppProperties.getUsername() );

            // create the message part
            MimeBodyPart messageBodyPart = new MimeBodyPart();

            //fill message
            messageBodyPart.setText( "Records sent by " + AppProperties.getUsername() );

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart( messageBodyPart );

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource( fileAttachment );
            messageBodyPart.setDataHandler( new DataHandler( source ) );
            messageBodyPart.setFileName( fileAttachment );
            multipart.addBodyPart( messageBodyPart );

            // Put parts in message
            message.setContent( multipart );

            // Send the message
            Transport.send( message );
        } catch ( Exception e ) {
            resultCollector.addResult( new ProcessingResult( ResultType.FATAL, file.getName(), "Failed to send " + file.getName() + ": " + e.getMessage() ) );
            return false;
        }
        resultCollector.addResult( new ProcessingResult( ResultType.INFO, file.getName(), "Successfully sent " + file.getName() ) );
        return true;
    }

    private String getMailServer( final String hostName ) {

        String mailHost = null;

        final Hashtable env = new Hashtable();
        env.put( "java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory" );

        try {

            final DirContext ictx = new InitialDirContext( env );
            Attributes attrs = ictx.getAttributes( hostName, new String[]{"MX"} );
            Attribute attr = attrs.get( "MX" );

            // If No MX Record is Found, Use the A Record Instead.
            if ( ( attr == null ) || ( attr.size() == 0 ) ) {
                attrs = ictx.getAttributes( hostName, new String[]{"A"} );
                attr = attrs.get( "A" );
            }

            // Set Mail Exchange Host.
            mailHost = ( String ) attr.get();

        }
        catch ( NamingException e ) {
            System.out.println( "Error Retrieving Mail Host Record: "+ e );
        }

        // If we failed, just prepend mail. and hope it works!
        if ( mailHost == null ) {
            mailHost = "mail." + hostName;
        }

        int spaceIndex = mailHost.lastIndexOf( ' ' );
        if ( spaceIndex > -1 ) {
            mailHost = mailHost.substring( spaceIndex + 1, mailHost.length() );
        }

        return mailHost;

    }


    public boolean isConnectionAvailable( ServerInfo targetServerInfo ) {
        return true;
    }
}
