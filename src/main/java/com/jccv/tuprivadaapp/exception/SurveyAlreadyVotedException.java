package com.jccv.tuprivadaapp.exception;

public class SurveyAlreadyVotedException extends RuntimeException {
    public SurveyAlreadyVotedException(String message) {
        super(message);
    }
}
