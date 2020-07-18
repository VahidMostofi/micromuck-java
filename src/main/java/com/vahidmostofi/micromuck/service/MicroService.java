package com.vahidmostofi.micromuck.service;

import io.opentracing.Span;
import io.opentracing.propagation.Format;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

@Service
public class MicroService {

    @Autowired
    private Environment env;

    private MicroEndpoint[] microEndpoints;


    @Value("${app.endpoints}")
    private String envEndpoints;

    @Value("${app.pre_request}")
    private String preRequestStr;

    @Value("${app.post_request}")
    private String postRequestStr;

    @Value("${app.seed}")
    private String seedStr;

    private Random r;

    private OkHttpClient httpClient;


    @PostConstruct
    private void init(){
        r = new Random();
        r.setSeed(Long.parseLong(seedStr));

        // parse endpoints
        envEndpoints = envEndpoints.trim();
        if (envEndpoints.length() >1){
            microEndpoints = new MicroEndpoint[envEndpoints.split("_").length];
            String[] endpointsStrs = envEndpoints.split("_");
            int i = 0;

            for(String endpointStr : endpointsStrs){
                int prob = Integer.parseInt(endpointStr.split(";")[0]);
                String path = endpointStr.split(";")[1];
                microEndpoints[i++] = new MicroEndpoint(prob, path);
            }
        }

        httpClient = new OkHttpClient();

    }

    public void PreRequestBehaviour(){
        behave(preRequestStr);
    }

    public void MakeRequest(io.opentracing.Tracer tracer, Span span){
        if (microEndpoints == null){
            return;
        }

        String url = null;
        int randomValue = r.nextInt(100);
        int sumProb = 0;
        for(MicroEndpoint me : this.microEndpoints){
            sumProb += me.getProb();
//            System.out.println(randomValue + " " + sumProb);
            if (randomValue < sumProb){
                url = me.getPath();
                break;
            }
        }
        if (url == null){
            return;
        }

        Request.Builder requestBuilder = new Request.Builder().url(url);
        tracer.inject(
                span.context(),
                Format.Builtin.HTTP_HEADERS,
                new RequestBuilderCarrier(requestBuilder)
        );

        Request request = requestBuilder.build();

        try{
            Span subSpan = tracer.buildSpan("out-service-call").asChildOf(span).start();
            subSpan.setTag("target", url);
            Response response = httpClient.newCall(request).execute();
            subSpan.setTag("status-code", response.code());
            subSpan.finish();
        }catch (Exception e){
            e.printStackTrace();
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
                long current = System.currentTimeMillis();
                if (start + duration < current){
                    break;
                }
            }
        }
    }

    public class RequestBuilderCarrier implements io.opentracing.propagation.TextMap {
        private final Request.Builder builder;

        RequestBuilderCarrier(Request.Builder builder) {
            this.builder = builder;
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            throw new UnsupportedOperationException("carrier is write-only");
        }

        @Override
        public void put(String key, String value) {
            builder.addHeader(key, value);
        }
    }
}
