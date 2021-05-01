package org.infinity.passport.async;

import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.utils.TraceIdUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Async task executor with exception handling.
 */
@Slf4j
public class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor, InitializingBean, DisposableBean {

    private final AsyncTaskExecutor executor;

    public ExceptionHandlingAsyncTaskExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(@NonNull Runnable task) {
        executor.execute(createWrappedRunnable(task, MDC.getCopyOfContextMap()));
    }

    @Override
    public void execute(@NonNull Runnable task, long startTimeout) {
        executor.execute(createWrappedRunnable(task, MDC.getCopyOfContextMap()), startTimeout);
    }

    private <T> Callable<T> createCallable(final Callable<T> task, final Map<String, String> context) {
        return () -> {
            try {
                TraceIdUtils.setParentMdcToChild(context);
                return task.call();
            } catch (Exception e) {
                handle(e);
                throw e;
            } finally {
                TraceIdUtils.remove();
            }
        };
    }

    private Runnable createWrappedRunnable(final Runnable task, final Map<String, String> context) {
        return () -> {
            try {
                TraceIdUtils.setParentMdcToChild(context);
                task.run();
            } catch (Exception e) {
                handle(e);
            } finally {
                TraceIdUtils.remove();
            }
        };
    }

    protected void handle(Exception e) {
        log.error("Caught async exception", e);
    }

    @Override
    @NonNull
    public Future<?> submit(@NonNull Runnable task) {
        return executor.submit(createWrappedRunnable(task, MDC.getCopyOfContextMap()));
    }

    @Override
    @NonNull
    public <T> Future<T> submit(@NonNull Callable<T> task) {
        return executor.submit(createCallable(task, MDC.getCopyOfContextMap()));
    }

    @Override
    public void destroy() throws Exception {
        if (executor instanceof DisposableBean) {
            DisposableBean bean = (DisposableBean) executor;
            bean.destroy();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (executor instanceof InitializingBean) {
            InitializingBean bean = (InitializingBean) executor;
            bean.afterPropertiesSet();
        }
    }
}
