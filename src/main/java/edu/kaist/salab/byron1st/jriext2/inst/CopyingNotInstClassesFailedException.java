package edu.kaist.salab.byron1st.jriext2.inst;

import java.io.IOException;

/**
 * Created by byron1st on 2017. 6. 20..
 */
public class CopyingNotInstClassesFailedException extends Throwable {
    public CopyingNotInstClassesFailedException() {
    }

    public CopyingNotInstClassesFailedException(String message) {
        super(message);
    }

    public CopyingNotInstClassesFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CopyingNotInstClassesFailedException(Throwable cause) {
        super(cause);
    }
}
