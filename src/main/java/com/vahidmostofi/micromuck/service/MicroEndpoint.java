package com.vahidmostofi.micromuck.service;

public class MicroEndpoint {

    private float prob;
    private String path;

    public MicroEndpoint(){

    }

    public MicroEndpoint(float prob, String path) {
        this.prob = prob;
        this.path = path;
    }

    public float getProb() {
        return prob;
    }

    public void setProb(float prob) {
        this.prob = prob;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
