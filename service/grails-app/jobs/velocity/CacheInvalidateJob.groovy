package velocity
/**
 * Created by Tiger on 25.09.14.
 */
class CacheInvalidateJob {

    def dbCache

    static triggers = {
        cron name: 'cronTrigger', startDelay: 0, cronExpression: '0 0/1 * * * ?'
    }

    def execute() {
        dbCache.invalidate()
    }
}