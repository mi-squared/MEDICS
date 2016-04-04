package maqs.ehs.form;

import maqs.ehs.patient.Field;
import maqs.ehs.patient.FieldSvc;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public interface MedicsFormManager {

    public MedicsFormManager getInstance( JPanel parentContainer );

    public Component getParentContainer();

    public void populatePanel( MedicsForm displayPane, int maxRowsPerColumn, List<Field> list, ResultCollector resultCollector  );

    public MedicsBlockFormRow createBlockFormRow();

    public MedicsLabelValueFormRow createLabelValueFormRow();

    public JPanel createSpacerFormRow();

    public Font getDefaultLabelFont();

    public Font getRequiredLabelFont();

    public Font getRequiredAsterixFont();

    public Font getTextStyle( Field field );

    public Dimension getSingleColumnRowDimensions();

    Dimension getValueColumnDimensions();
}
