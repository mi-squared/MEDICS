package maqs.ehs.form;

import java.io.File;
import java.util.List;

public interface RemoteImporter {

    public List<File> importRemote( ResultCollector resultCollector, ServerInfo targetServerInfo );

    public boolean isConnectionAvailable( ServerInfo targetServerInfo );

    boolean deleteRemoteFile( String fileName, ServerInfo targetServerInfo );
}