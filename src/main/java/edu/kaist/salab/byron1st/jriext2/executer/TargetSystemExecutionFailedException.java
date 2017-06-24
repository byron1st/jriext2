package edu.kaist.salab.byron1st.jriext2.executer;

/**
 * Created by util on 2017. 6. 23..
 */
public class TargetSystemExecutionFailedException extends ExecutionException {
    public TargetSystemExecutionFailedException() {
    }

    public TargetSystemExecutionFailedException(String message) {
        super(message);
    }

    public TargetSystemExecutionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TargetSystemExecutionFailedException(Throwable cause) {
        super(cause);
    }
}
