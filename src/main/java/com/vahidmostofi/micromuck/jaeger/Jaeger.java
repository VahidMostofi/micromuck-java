package com.vahidmostofi.micromuck.jaeger;

import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Jaeger {

    @Value("${app.service_name}")
    String serviceName;

    private io.opentracing.Tracer tracer;

    @PostConstruct
    private void init(){
        getTracer();
    }

    public Tracer getTracer(){
        if (tracer == null){
            System.out.println("INITIALIZING JAGER TRACER");
//            long zero = System.currentTimeMillis();
            io.jaegertracing.Configuration.SamplerConfiguration samplerConfig = io.jaegertracing.Configuration.SamplerConfiguration.fromEnv()
                    .withType(ConstSampler.TYPE)
                    .withParam(1);
//            System.out.println("A " + (System.currentTimeMillis() - zero));
            io.jaegertracing.Configuration.ReporterConfiguration reporterConfig = io.jaegertracing.Configuration.ReporterConfiguration.fromEnv()
                    .withLogSpans(true);
//            System.out.println("A " + (System.currentTimeMillis() - zero));
            io.jaegertracing.Configuration config = new io.jaegertracing.Configuration(serviceName)
                    .withSampler(samplerConfig)
                    .withReporter(reporterConfig);
//            System.out.println("A " + (System.currentTimeMillis() - zero));
            tracer = config.getTracer();
        }
        return tracer;
    }
}
