package edu.kaist.salab.byron1st.jriext2.executer;

/**
 * Created by byron1st on 2017. 6. 24..
 */
public class ProcessNotExistException extends ExecutionException {
    public ProcessNotExistException() {
    }

    public ProcessNotExistException(String message) {
        super(message);
    }

    public ProcessNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessNotExistException(Throwable cause) {
        super(cause);
    }
}
