package com.leyou.common.advice;

import com.leyou.common.Exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 是个 aop切面
 */
@ControllerAdvice
public class BasicExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> exceptionHandle(RuntimeException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> lyExceptionHandle(LyException e){
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }


}
