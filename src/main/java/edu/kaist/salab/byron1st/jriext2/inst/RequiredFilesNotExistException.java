package edu.kaist.salab.byron1st.jriext2.inst;

/**
 * Created by util on 2017. 6. 23..
 */
public class RequiredFilesNotExistException extends ExecutionException {
    public RequiredFilesNotExistException() {
    }

    public RequiredFilesNotExistException(String message) {
        super(message);
    }

    public RequiredFilesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequiredFilesNotExistException(Throwable cause) {
        super(cause);
    }
}
