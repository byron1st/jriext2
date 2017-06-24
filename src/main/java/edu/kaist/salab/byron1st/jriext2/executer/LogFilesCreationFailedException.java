package edu.kaist.salab.byron1st.jriext2.executer;

/**
 * Created by byron1st on 2017. 6. 24..
 */
public class LogFilesCreationFailedException extends ExecutionException {
    public LogFilesCreationFailedException() {
    }

    public LogFilesCreationFailedException(String message) {
        super(message);
    }

    public LogFilesCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogFilesCreationFailedException(Throwable cause) {
        super(cause);
    }
}
