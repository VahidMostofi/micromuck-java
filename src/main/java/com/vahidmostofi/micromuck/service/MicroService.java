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
import java.util.*;

@Service
public class MicroService {

    @Autowired
    private Environment env;

    @Value("${app.seed}")
    private String seedStr;

    private Random r;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init(){
        r = new Random(Long.parseLong(seedStr));
        httpClient = new OkHttpClient();
    }

    public void MakeRequest(io.opentracing.Tracer tracer, Span span, ArrayList<MicroEndpoint> microEndpoints){
        if (microEndpoints.size() == 0){
            return;
        }

        String url = null;
        int randomValue = r.nextInt(100);
        int sumProb = 0;
        for(MicroEndpoint me : microEndpoints){
            sumProb += me.getProb();
            System.out.println(randomValue + " " + sumProb);
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


    public void behave(int n){
        isPrime(n);
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

    static boolean isPrime(int n)
    {
        // Corner case
        if (n <= 1)
            return false;

        // Check from 2 to n-1
        for (int i = 2; i < n; i++)
            if (n % i == 0)
                return false;

        return true;
    }
}
