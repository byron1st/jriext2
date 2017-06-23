package edu.kaist.salab.byron1st.jriext2.inst;

/**
 * Created by util on 2017. 6. 23..
 */
public class ExecutionException extends Exception {
    public ExecutionException() {
    }

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutionException(Throwable cause) {
        super(cause);
    }
}
