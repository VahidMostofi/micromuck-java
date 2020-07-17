package com.vahidmostofi.micromuck.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Service
public class MicroService {

    private MicroEndpoint[] microEndpoints;
    @Value("${endpoints}")
    private String envEndpoints;

    @Value("${preRequest}")
    private String preRequestStr;

    @Value("${postRequest}")
    private String postRequestStr;

    @Value("${seed}")
    private String seedStr;

    private Random r;

    private final RestTemplate restTemplate;

    public MicroService(RestTemplateBuilder restTemplateBuilder){
        r = new Random();
        r.setSeed(Long.parseLong(seedStr));

        // parse endpoints
        envEndpoints = envEndpoints.trim();
        microEndpoints = new MicroEndpoint[envEndpoints.split("_").length];
        String[] endpointsStrs = envEndpoints.split("_");
        int i = 0;
        float sumProb = 0;
        for(String endpointStr : endpointsStrs){
            float prob = Float.parseFloat(endpointStr.split(":")[0]);
            String path = endpointStr.split(":")[1];
            microEndpoints[i++] = new MicroEndpoint(prob, path);
            sumProb += prob;
        }
        assert Math.abs(sumProb - 1) < 1e-7;

        restTemplate = restTemplateBuilder.build();
    }

    public void PreRequestBehaviour(){
        behave(preRequestStr);
    }

    public void Request(){
        if (microEndpoints.length  == 0){
            return;
        }

        float randomValue = r.nextFloat();
        float sumProb = 0;
        for(MicroEndpoint me : this.microEndpoints){
            sumProb += me.getProb();
            if (randomValue < sumProb){
                makeHttpRequest(me.getPath());
            }
        }
    }

    public void PostRequestBehaviour(){
        behave(postRequestStr);
    }

    private void behave(String code){
        if (code.startsWith("uniform")){ //uniform(100,200)
            int min = Integer.parseInt(code.substring("uniform".length()+1, code.length()-1).split(",")[0]);
            int max = Integer.parseInt(code.substring("uniform".length()+1, code.length()-1).split(",")[1]);
            int duration = r.nextInt(max-min) + min;
            long start = System.currentTimeMillis();
            while(true){
                if (start + duration > System.currentTimeMillis()){
                    break;
                }
            }
        }
    }

    private void makeHttpRequest(String path){
        ResponseEntity<String> response = this.restTemplate.getForEntity(path, String.class);
    }
}
