package com.vahidmostofi.micromuck.controller;

import com.vahidmostofi.micromuck.entity.MicroResult;
import com.vahidmostofi.micromuck.jaeger.Jaeger;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    @GetMapping("/")
    ResponseEntity<MicroResult>  get(HttpServletRequest request, @RequestHeader HttpHeaders headers){
        SpanContext parentContext = ((Span)request.getAttribute("controller")).context();
        Span span = jaeger.getTracer().buildSpan(serviceName).asChildOf(parentContext).start();

        // behaviour of each service for now is pre-sub-request, sub-request, post-sub-request
        microService.PreRequestBehaviour();
        microService.MakeRequest(jaeger.getTracer(), span);
        microService.PostRequestBehaviour();


        // add some headers to the response for debugging and information gathering
        HttpHeaders responseHeaders = new HttpHeaders();
        // adding trace-id (jaeger trace id)
        responseHeaders.set("jaeger-id",span.toString().split("-")[0].trim().split(":")[0]);
        // adding debug-id (I used this for monitoring requests using tcpdump)
        if (headers.get("debug-id") != null && headers.get("debug-id").size() > 0){
            responseHeaders.set("deubg-id", headers.get("debug-id").get(0));
        }

        span.finish();
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(null);
    }
}
