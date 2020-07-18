package com.vahidmostofi.micromuck.service;

public class MicroEndpoint {

    private int prob;
    private String path;

    public MicroEndpoint(){

    }

    public MicroEndpoint(int prob, String path) {
        this.prob = prob;
        this.path = path;
    }

    public int getProb() {
        return prob;
    }

    public void setProb(int prob) {
        this.prob = prob;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
