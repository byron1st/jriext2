package edu.kaist.salab.byron1st.jriext2app.cli;

/**
 * Created by byron1st on 2017. 6. 28..
 */
public class WrongArgumentsException extends Exception {
    public WrongArgumentsException() {
    }

    public WrongArgumentsException(String message) {
        super(message);
    }

    public WrongArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongArgumentsException(Throwable cause) {
        super(cause);
    }
}
