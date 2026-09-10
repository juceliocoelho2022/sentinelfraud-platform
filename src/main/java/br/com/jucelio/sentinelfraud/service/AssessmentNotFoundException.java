package br.com.jucelio.sentinelfraud.service;
public class AssessmentNotFoundException extends RuntimeException {
    public AssessmentNotFoundException(String id) { super("Assessment not found for transaction: " + id); }
}
