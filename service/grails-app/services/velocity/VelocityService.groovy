package velocity

import groovyx.gpars.GParsPool
import jsr166y.ForkJoinPool
import org.joda.time.DateTime
import redis.clients.jedis.Jedis
import redis.clients.jedis.Pipeline

import static groovyx.gpars.GParsPool.withExistingPool
import static groovyx.gpars.GParsPool.withPool

class VelocityService {

    def dbCache
    def redisService

    void updateMetrics(Map<String, String[]> velocityRQ) {
        Long now = DateTime.now().millis
        redisService.withRedis { Jedis redis ->
            Long transactionID = redis.incr('id:transactions')
            redisService.withPipeline() { Pipeline pipe ->
                velocityRQ.each { metricType, metricValues ->
                    pipe.lpush("transaction:$transactionID:$metricType", metricValues as String[])
                    metricValues?.each { metricValue ->
                        pipe.zadd("$metricType:$metricValue:history", now, transactionID as String)
                    }
                }
            }
        }

        withPool() { ForkJoinPool pool ->
            dbCache.configs?.eachParallel { VelocityConfig config ->
                withExistingPool(pool) {
                    GParsPool.runForkJoin(config, config.primaryMetrics.asList(), 0, null, null, velocityRQ) {
                        conf, List<String> primaryMetrics, int index, Set<String> tranIds, String key, rq ->
                            if (index < primaryMetrics.size()) {
                                String metric = primaryMetrics[index]
                                rq[metric].each {
                                    String newKey = "$metric:$it"
                                    String fullKey = key ? "$key:$newKey" : newKey

                                    Set<String> tranIDsIntersection = []
                                    redisService.withRedis { Jedis redis ->
                                        Set<String> newIds = redis.zrangeByScore("$newKey:history", now - config.period, now)
                                        tranIDsIntersection = tranIds ? tranIds.intersect(newIds) : newIds
                                    }
                                    if (index + 1 < primaryMetrics.size()) {
                                        forkOffChild(config, primaryMetrics, index + 1, tranIDsIntersection, fullKey, rq)
                                    } else {
                                        config.aggregationConfigs?.each { AggregationConfig aggConf ->
                                            List<String> history = []

                                            redisService.withRedis { Jedis redis ->
                                                tranIDsIntersection.each {
                                                    history += redis.lrange("transaction:$it:$aggConf.secondaryMetric", 0, -1);
                                                }
                                                Double result = aggConf.aggregation.apply history
                                                redis.hset("$fullKey:metrics:$aggConf.secondaryMetric",
                                                        aggConf.aggregation as String, result as String)
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    def getMetrics(Map<String, String[]> velocityRQ) {
        def velocityMetrics = []
        withPool() {
            dbCache.configs.each { VelocityConfig config ->
                velocityMetrics += GParsPool.runForkJoin(config, config.primaryMetrics.asList(), 0, [:], null, velocityRQ) {
                    VelocityConfig conf, List<String> primaryMetrics, int index, Map<String, String> key,
                    String metricKey, Map<String, String[]> rq ->
                        def metrics = [:]
                        if (index < primaryMetrics.size()) {
                            String metric = primaryMetrics[index]
                            rq[metric].each {
                                key << [(metric): it]
                                String newKey = "$metric:$it"
                                String fullKey = metricKey ? "$metricKey:$newKey" : newKey
                                if (index + 1 < primaryMetrics.size()) {
                                    forkOffChild(conf, primaryMetrics, index + 1, key.clone(), fullKey, rq)
                                } else {
                                    def velocityKey = key.clone()
                                    metrics << [(velocityKey): [:]]
                                    redisService.withRedis { Jedis redis ->
                                        conf.aggregationConfigs.each {
                                            metrics[velocityKey] <<
                                                    [(it.secondaryMetric): redis.hgetAll("$fullKey:metrics:$it.secondaryMetric")]
                                        }
                                    }
                                }
                            }
                            childrenResults?.each {
                                metrics += it
                            }
                        }
                        return metrics
                }
            }
        }
        withPool() {
            Closure updateMetrics = {updateMetrics(velocityRQ)}
            updateMetrics.callAsync()
        }
        return velocityMetrics
    }
}
