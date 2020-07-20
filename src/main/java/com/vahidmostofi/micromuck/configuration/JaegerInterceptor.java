package com.vahidmostofi.micromuck.configuration;

import com.vahidmostofi.micromuck.jaeger.Jaeger;
import com.vahidmostofi.micromuck.service.MicroService;
import io.jaegertracing.internal.utils.Http;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//@Service
public class JaegerInterceptor implements HandlerInterceptor {

    //unimplemented methods comes here. Define the following method so that it
    //will handle the request before it is passed to the controller.


    public JaegerInterceptor(Jaeger jaeger){
        this.jaeger = jaeger;
//        this.serviceName = serviceName;
    }

    private Jaeger jaeger;
//    private String serviceName;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        //your custom logic here.
        HttpHeaders headers = new HttpHeaders();
        for (Enumeration<?> names = request.getHeaderNames(); names.hasMoreElements(); ) {
            String name = (String) names.nextElement();
            for (Enumeration<?> values = request.getHeaders(name); values.hasMoreElements(); ) {
                headers.add(name, (String) values.nextElement());
            }
        }
        System.out.println(jaeger.getTracer());
        SpanContext parentContext = jaeger.getTracer().extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers.toSingleValueMap()));
        Span span = jaeger.getTracer().buildSpan("interceptor").asChildOf(parentContext).start();
//        jaeger.getTracer().inject(
//                span.context(),
//                Format.Builtin.HTTP_HEADERS,
//                new HttpRequestCarrier(request)
//        );
        request.setAttribute("span", span);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,@Nullable ModelAndView modelAndView) throws Exception {
        System.out.println(((Span)request.getAttribute("span")).toString());
        ((Span)request.getAttribute("span")).finish();
    }


    public class HttpRequestCarrier implements io.opentracing.propagation.TextMap {
        private final HttpServletRequest req;

        HttpRequestCarrier(HttpServletRequest req) {
            this.req = req;
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            throw new UnsupportedOperationException("carrier is write-only");
        }

        @Override
        public void put(String key, String value) {
            req.setAttribute(key, value);
        }
    }

}
