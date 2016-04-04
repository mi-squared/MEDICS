package maqs.ehs.form;

public enum PatientAction {
    NEW,
    SAVE,
    DELETE,
    EXPORT,
    IMPORT,
    BATCH_EXPORT,
    PUBLISH_EXPORTED,
    NOOP,
    EDIT,
    QUIT;

    public boolean isSave() {
        return this == SAVE;
    }

    public boolean isNew() {
        return this == NEW;
    }

    public boolean isDelete() {
        return this == DELETE;
    }

    public boolean isEdit() {
        return this == EDIT;
    }

    public boolean isExport() {
        return this == EXPORT;
    }

    public boolean isNoop() {
        return this == NOOP;
    }

    public boolean isQuit() {
        return this == QUIT;
    }

    public boolean isImport() {
        return this == IMPORT;
    }

    public boolean isBatchExport() {
        return this == BATCH_EXPORT;
    }
    public boolean isPublishExported() {
        return this == PUBLISH_EXPORTED;
    }
}
