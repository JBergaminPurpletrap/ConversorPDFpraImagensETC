package com.documentconverter.core.models;

/**
 * Registro de uma conversão executada pela aplicação.
 */
public class ConversionHistory {

    private String date;
    private String time;
    private String operation;
    private String source;
    private String destination;
    private String status;

    public ConversionHistory() {
    }

    public ConversionHistory(String date, String time, String operation,
                             String source, String destination, String status) {
        this.date = date;
        this.time = time;
        this.operation = operation;
        this.source = source;
        this.destination = destination;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
