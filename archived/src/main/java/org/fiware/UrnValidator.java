package org.fiware;
import java.util.regex.Pattern;

/**
 *
 *  URN Validator
 *
 *   Copyright (c) 2017-18 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public class UrnValidator {
    public final static Pattern URN_PATTERN = Pattern.compile(
            "^urn:[a-z0-9][a-z0-9-]{0,31}:([a-z0-9()+,\\-.:=@;$_!*']|%[0-9a-f]{2})+$",
            Pattern.CASE_INSENSITIVE);


    public final static boolean isValid(String s) {
        return URN_PATTERN.matcher(s).matches();
    }
}
