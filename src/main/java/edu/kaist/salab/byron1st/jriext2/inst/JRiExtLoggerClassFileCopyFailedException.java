package edu.kaist.salab.byron1st.jriext2.inst;

/**
 * Created by util on 2017. 6. 23..
 */
public class JRiExtLoggerClassFileCopyFailedException extends InstrumentationException {
    public JRiExtLoggerClassFileCopyFailedException() {
    }

    public JRiExtLoggerClassFileCopyFailedException(String message) {
        super(message);
    }

    public JRiExtLoggerClassFileCopyFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JRiExtLoggerClassFileCopyFailedException(Throwable cause) {
        super(cause);
    }
}
