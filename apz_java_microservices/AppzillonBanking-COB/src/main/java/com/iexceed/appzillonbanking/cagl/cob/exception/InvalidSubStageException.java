package com.iexceed.appzillonbanking.cagl.cob.exception;

public class InvalidSubStageException extends RuntimeException {
    public InvalidSubStageException(String subStage) {
        super("Unsupported or unrecognised sub_stage: " + subStage);
    }
}
