package maqs.ehs.form;

import maqs.ehs.util.AppProperties;

import java.io.File;
import java.util.List;

class PgpKeyUploadHelper {

    static void handlePgpUpload() {

        boolean needsReminder = AppProperties.isPgpKeyUploadedReminder();
        if ( !needsReminder ) {
            return;
        }
        boolean needsExport = !AppProperties.isPgpKeyUploaded();
        if ( !needsExport ) {
            return;
        }

        // ping
        RemotePublisher publisher = new FtpRemotePublisher();
        boolean isReachable = publisher.isConnectionAvailable(AppProperties.getFtpKeyServerInfo() );

        if ( isReachable ) {
            PgpKeyUploader uploader = new PgpKeyUploader();
        }
    }

    public static boolean doPgpUpload( List<String> msg ) {
        RemotePublisher publisher = new FtpRemotePublisher();
        File localPublicKeyFile = new File( AppProperties.getLocalPublicKeyFilePath() );
        ResultCollector collector = new DefaultResultCollector();
        boolean success = publisher.publishRemote( localPublicKeyFile, collector, AppProperties.getFtpKeyServerInfo(), "" );
        if ( !success ) {
            for ( ProcessingResult result : collector.getResultsWithHigherSeverityThan( ResultType.WARNING ) ) {
                msg.add( result.getDetail() );
            }
        }
        return success;
    }
}
