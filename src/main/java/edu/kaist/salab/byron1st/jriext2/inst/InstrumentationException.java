package edu.kaist.salab.byron1st.jriext2.inst;

/**
 * Created by util on 2017. 6. 20..
 */
public class InstrumentationException extends Exception {
    public InstrumentationException() {
    }

    public InstrumentationException(String message) {
        super(message);
    }

    public InstrumentationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstrumentationException(Throwable cause) {
        super(cause);
    }
}
