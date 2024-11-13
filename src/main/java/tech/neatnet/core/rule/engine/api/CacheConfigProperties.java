package tech.neatnet.core.rule.engine.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "rule.engine.caches")
public class CacheConfigProperties {
    private List<CacheConfigProperty> cacheConfigs;
}
