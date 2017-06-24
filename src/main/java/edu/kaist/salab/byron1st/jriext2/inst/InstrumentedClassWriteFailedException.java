package edu.kaist.salab.byron1st.jriext2.inst;

import java.io.IOException;

/**
 * Created by util on 2017. 6. 20..
 */
public class InstrumentedClassWriteFailedException extends InstrumentationException {
    public InstrumentedClassWriteFailedException() {
    }

    public InstrumentedClassWriteFailedException(String message) {
        super(message);
    }

    public InstrumentedClassWriteFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstrumentedClassWriteFailedException(Throwable cause) {
        super(cause);
    }
}
