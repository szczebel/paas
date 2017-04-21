package paas.rest.monitoring;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect //todo: should be applied before security aspects, so that unauthorized hits are also monitored
public class MonitoringAspect {

    @Autowired Monitor monitor;

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public Object monitorPost(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitor(joinPoint);
    }

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object monitorGet(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitor(joinPoint);

    }

    private Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        boolean exception = false;
        try {
            return joinPoint.proceed();
        } catch(Throwable t) {
            exception = true;
            throw t;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            monitor.record(method, executionTime, exception);
        }
    }
}
