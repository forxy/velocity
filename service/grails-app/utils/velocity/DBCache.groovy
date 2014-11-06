package velocity

/**
 * Created by Tiger on 25.09.14.
 */
class DBCache {
    List<VelocityConfig> configs;

    void invalidate() {
        configs = VelocityConfig.all
    }

    List<VelocityConfig> getConfigs() {
        if (!configs) invalidate()
        return configs
    }
}
