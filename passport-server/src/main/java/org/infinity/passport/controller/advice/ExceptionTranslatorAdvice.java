package org.infinity.passport.controller.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.infinity.passport.component.MessageCreator;
import org.infinity.passport.dto.ErrorDTO;
import org.infinity.passport.exception.DuplicationException;
import org.infinity.passport.exception.NoAuthorityException;
import org.infinity.passport.exception.NoDataFoundException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * <p>
 * Exception list refer to {@link org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler}
 */
@ControllerAdvice
@Slf4j
public class ExceptionTranslatorAdvice {

    public static final String INVALID_REQUEST_PARAM_CODE = "EP5000";
    public static final String NO_DATA_FOUND_CODE         = "EP5002";
    public static final String NO_AUTH_CODE               = "EP5011";
    public static final String ACCESS_DENIED_CODE         = "EP5030";
    public static final String DUPLICATED_DATA_CODE       = "EP5101";
    public static final String SYS_ERROR_CODE             = "ES7000";
    public static final String SYS_EXCEPTION_CODE         = "ES7001";
    public static final String CONCURRENCY_EXCEPTION_CODE = "ES7002";

    private final MessageCreator messageCreator;

    public ExceptionTranslatorAdvice(MessageCreator messageCreator) {
        this.messageCreator = messageCreator;
    }

    /**
     * JSR 303 bean validation exception handler
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(processFieldErrors(ex.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processBindException(BindException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(processFieldErrors(ex.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(ex.getMessage()).build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(ex.getMessage()).build());
    }

    @ExceptionHandler(MismatchedInputException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processMismatchedInputException(MismatchedInputException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(ex.getMessage()).build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Found mismatched type request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(ex.getMessage()).build());
    }

    @ExceptionHandler(NumberFormatException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processNumberFormatException(NumberFormatException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(ex.getMessage()).build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(ex.getMessage()).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400
        return ResponseEntity.badRequest().body(ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(ex.getMessage()).build());
    }

    @ExceptionHandler(NoAuthorityException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processNoAuthorityException(NoAuthorityException ex) {
        log.warn("No authority: ", ex);
        ErrorDTO error = ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(messageCreator.getMessage(NO_AUTH_CODE, ex.getUserName())).build();
        // Http status: 400
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DuplicationException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processDuplicationException(DuplicationException ex) {
        log.warn("Found invalid request parameters: ", ex);
        // Http status: 400

        String jsonString = "";
        try {
            jsonString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ex.getFieldMap());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize to json string!", e);
        }
        ErrorDTO error = ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(messageCreator.getMessage(DUPLICATED_DATA_CODE, jsonString)).build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: ", ex);
        ErrorDTO error = ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(messageCreator.getMessage(ACCESS_DENIED_CODE)).build();
        // Http status: 403
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(NoDataFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processNoDataFoundException(NoDataFoundException ex) {
        log.warn("No data found: ", ex);
        ErrorDTO error = ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(messageCreator.getMessage(NO_DATA_FOUND_CODE, ex.getId())).build();
        // Http status: 404
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("Found unsupported http method: ", ex);
        // Http status: 405
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ErrorDTO.builder().code(INVALID_REQUEST_PARAM_CODE).message(ex.getMessage()).build());
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processConcurrencyException(ConcurrencyFailureException ex) {
        log.warn("Found concurrency exception: ", ex);
        ErrorDTO error = ErrorDTO.builder().code(SYS_ERROR_CODE).message(messageCreator.getMessage(CONCURRENCY_EXCEPTION_CODE)).build();
        // Http status: 409
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> processException(Throwable throwable) {
        log.warn("Found exception: ", throwable);
        ErrorDTO error = ErrorDTO.builder().code(SYS_ERROR_CODE).message(messageCreator.getMessage(SYS_EXCEPTION_CODE)).build();
        // Http status: 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ErrorDTO processFieldErrors(List<FieldError> fieldErrors) {
        List<ErrorDTO.ErrorField> errorFields = new ArrayList<>();
        for (FieldError fieldError : fieldErrors) {
            String defaultMessage = fieldError.getDefaultMessage();
            if (StringUtils.isEmpty(defaultMessage)
                    && fieldError.getCodes() != null) {
                defaultMessage = messageCreator.getMessage(fieldError.getCodes()[0], fieldError.getArguments());
            }
            ErrorDTO.ErrorField errorField = ErrorDTO.ErrorField.builder()
                    .field(fieldError.getField())
                    .rejectedValue(fieldError.getRejectedValue())
                    .message(defaultMessage)
                    .build();
            errorFields.add(errorField);
        }
        return ErrorDTO.builder()
                .code(INVALID_REQUEST_PARAM_CODE)
                .message(messageCreator.getMessage(INVALID_REQUEST_PARAM_CODE))
                .errorFields(errorFields)
                .build();
    }
}
