package edu.kaist.salab.byron1st.jriext2.inst;

import java.io.IOException;

/**
 * Created by byron1st on 2017. 6. 20..
 */
public class WritingInstrumentedClassFailedException extends InstrumentationException {
    public WritingInstrumentedClassFailedException() {
    }

    public WritingInstrumentedClassFailedException(String message) {
        super(message);
    }

    public WritingInstrumentedClassFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WritingInstrumentedClassFailedException(Throwable cause) {
        super(cause);
    }
}
