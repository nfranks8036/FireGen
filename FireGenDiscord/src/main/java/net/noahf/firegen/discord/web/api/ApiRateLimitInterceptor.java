package net.noahf.firegen.discord.web.api;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.noahf.firegen.discord.users.FireGenUser;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRateLimitInterceptor implements HandlerInterceptor {

    public static final int RATE_LIMIT_CAPACITY = 30;
    public static final int RATE_LIMIT_REFILL = RATE_LIMIT_CAPACITY;
    public static final int TOKENS_FULLY_REGEN_SECONDS = 60;




    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket create(int capacity, int refill, int durationSeconds) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refill, Duration.ofSeconds(durationSeconds))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof FireGenUser<?> user) {
            return "user:" + user.getId();
        }

        return "ip:" + request.getRemoteAddr();
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String clientKey = getClientKey(request);

        int capacity = RATE_LIMIT_CAPACITY;
        int refill = RATE_LIMIT_REFILL;
        int durationSeconds = TOKENS_FULLY_REGEN_SECONDS;

        String endpointKey = request.getRequestURI();
        if (handler instanceof HandlerMethod method) {
            endpointKey = method.getMethod().getName();

            ApiRateLimit annotation = method.getMethodAnnotation(ApiRateLimit.class);

            if (annotation != null) {
                capacity = annotation.capacity();
                refill = annotation.refill();
                durationSeconds = annotation.durationSeconds();
            }
        }

        final int finalCapacity = capacity;
        final int finalRefill = refill;
        final int finalDurationSeconds = durationSeconds;

        Bucket bucket = buckets.computeIfAbsent(
                clientKey + ":" + endpointKey,
                key -> create(finalCapacity, finalRefill, finalDurationSeconds)
        );

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader(
                    "X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens())
            );

            return true;
        }

        long retryAfter = Math.max(
                1,
                Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds()
        );

        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfter));

        response.getWriter().write(String.format("""
                {
                    "status": 429,
                    "success": false,
                    "timestamp": "%s",
                    "response": "API Rate Limit Exceeded"
                }
                """, LocalDateTime.now()));

        return false;
    }
}
