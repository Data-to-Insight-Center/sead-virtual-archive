package org.seadva.bagit.Exception;

/**
 * Created by Sucharitha on 5/8/2014.
 */
public class SEADInvalidOREException extends Exception {
    //Parameterless Constructor
    public SEADInvalidOREException() {}

    //Constructor that accepts a message
    public SEADInvalidOREException(String message)
    {
        super(message);
    }
}
