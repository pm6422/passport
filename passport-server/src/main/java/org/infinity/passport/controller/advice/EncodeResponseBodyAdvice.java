//package org.infinity.passport.controller.advice;
// 未测试
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.infinity.passport.utils.AesUtils;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
//
//import java.io.IOException;
//
// https://www.toutiao.com/i6937532406995976712/
//@ControllerAdvice
//@Slf4j
//public class EncodeResponseBodyAdvice implements ResponseBodyAdvice<ResponseEntity<Object>> {
//
//    private static final String       AES_PASSWORD = "9f5d54580044d478";
//    private final        ObjectMapper mapper       = new ObjectMapper();
//
//    @Override
//    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
//    }
//
//    @SneakyThrows
//    @Override
//    public ResponseEntity<Object> beforeBodyWrite(ResponseEntity<Object> body,
//                                                  MethodParameter returnType,
//                                                  MediaType selectedContentType,
//                                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
//                                                  ServerHttpRequest request,
//                                                  ServerHttpResponse response) {
//        Object data = body.getBody();
//        if (data != null) {
//            return ResponseEntity.status(body.getStatusCode()).headers(body.getHeaders()).body(this.encode(data));
//        }
//        return body;
//    }
//
//    private String encode(Object data) throws IOException {
//        String jsonValue = mapper.writeValueAsString(data);
//        return AesUtils.encrypt(jsonValue, AES_PASSWORD);
//    }
//}