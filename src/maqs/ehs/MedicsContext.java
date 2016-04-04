package maqs.ehs;

import maqs.ehs.form.DefaultFormManager;
import maqs.ehs.form.MedicsFormManager;

import java.awt.*;

public class MedicsContext {

    private MedicsContext() {
    }

    // singleton
    private static MedicsContext instance;

    // singleton
    private static MedicsFormManager formManager;

    public static MedicsContext get() {
        if ( instance == null ) {
            instance = new MedicsContext();
        }
        return instance;
    }

    public MedicsFormManager getFormManager( Component parentContainer ) {
        // for now, return the DefaultMedicsFormManager
        if ( formManager == null ) {
            formManager = new DefaultFormManager( parentContainer );
        }

        return formManager;
    }

}
