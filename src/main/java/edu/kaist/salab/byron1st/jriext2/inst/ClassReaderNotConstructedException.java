package edu.kaist.salab.byron1st.jriext2.inst;

/**
 * Created by byron1st on 2017. 6. 20..
 */
public class ClassReaderNotConstructedException extends InstrumentationException {
    public ClassReaderNotConstructedException() {
    }

    public ClassReaderNotConstructedException(String message) {
        super(message);
    }

    public ClassReaderNotConstructedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassReaderNotConstructedException(Throwable cause) {
        super(cause);
    }
}
