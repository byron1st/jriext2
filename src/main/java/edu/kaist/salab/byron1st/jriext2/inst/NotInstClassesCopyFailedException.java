package edu.kaist.salab.byron1st.jriext2.inst;

/**
 * Created by util on 2017. 6. 20..
 */
public class NotInstClassesCopyFailedException extends Throwable {
    public NotInstClassesCopyFailedException() {
    }

    public NotInstClassesCopyFailedException(String message) {
        super(message);
    }

    public NotInstClassesCopyFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotInstClassesCopyFailedException(Throwable cause) {
        super(cause);
    }
}
