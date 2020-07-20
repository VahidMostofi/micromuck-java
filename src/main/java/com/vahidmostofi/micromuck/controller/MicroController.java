package com.vahidmostofi.micromuck.controller;

import com.vahidmostofi.micromuck.entity.MicroResult;
import com.vahidmostofi.micromuck.jaeger.Jaeger;
import com.vahidmostofi.micromuck.service.MicroEndpoint;
import com.vahidmostofi.micromuck.service.MicroService;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.*;

@RestController
public class MicroController {

    @Autowired
    private Environment env;

    @Autowired
    Jaeger jaeger;

    @Autowired
    MicroService microService;

    @Value("${app.service_name}")
    String serviceName;

    @Value("${app.endpoints}")
    String endpointsEnv;

    private HashMap<String, Endpoint> endpoints;

    @PostConstruct
    public void init(){

        String[] temps = endpointsEnv.split("\\|\\|");
        endpoints = new HashMap<String, Endpoint>();
        for (String temp : temps){
            Endpoint me = new Endpoint();

            me.prePrimeValue = Integer.parseInt(temp.split("\\|")[1]);
            me.postPrimeValue = Integer.parseInt(temp.split("\\|")[3]);

            if (temp.split("\\|")[2].length() > 1){
                String[] endpointsStrs = temp.split("\\|")[2].split("_");
                for(String endpointStr : endpointsStrs){
                    int prob = Integer.parseInt(endpointStr.split(";")[0]);
                    String path = endpointStr.split(";")[1];
                    me.microEndpoints.add(new MicroEndpoint(prob, path));
                }
            }
            endpoints.put(temp.split("\\|")[0],me);
            System.out.println();
        }
    }

    @RequestMapping(value="/{endPoint}", method= RequestMethod.GET)
    ResponseEntity<MicroResult>  get(@PathVariable String endPoint, HttpServletRequest request, @RequestHeader HttpHeaders headers){
        SpanContext parentContext = ((Span)request.getAttribute("span")).context();
        Span span = jaeger.getTracer().buildSpan(endPoint).asChildOf(parentContext).start();

        // add some headers to the response for debugging and information gathering
        HttpHeaders responseHeaders = new HttpHeaders();
        // adding trace-id (jaeger trace id)
        responseHeaders.set("jaeger-id",span.toString().split("-")[0].trim().split(":")[0]);
        // adding debug-id (I used this for monitoring requests using tcpdump)
        if (headers.get("debug-id") != null && headers.get("debug-id").size() > 0){
            responseHeaders.set("deubg-id", headers.get("debug-id").get(0));
        }

        Endpoint e = this.endpoints.get(endPoint);
        if (e == null){
            return ResponseEntity.notFound().headers(responseHeaders).build();
        }
        // behaviour of each service for now is pre-sub-request, sub-request, post-sub-request
        microService.behave(e.prePrimeValue);
        microService.MakeRequest(jaeger.getTracer(), span, e.microEndpoints);
        microService.behave(e.postPrimeValue);

        span.finish();
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(null);
    }

    private class Endpoint{
        int prePrimeValue;
        int postPrimeValue;
        ArrayList<MicroEndpoint> microEndpoints;

        public Endpoint(){
            microEndpoints = new ArrayList<MicroEndpoint>();
        }


    }
}
