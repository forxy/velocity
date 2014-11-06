package velocity

import grails.plugin.redis.RedisService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(VelocityService)
@TestMixin(DBCache)
@Mock([RedisService])
class VelocityServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        when:''
        /*service.updateMetrics([
                'Email': ['test@gmail.com'] as String[],
                'FormOfPayment': ['Visa', 'MasterCard'] as String[],
                'CreditCard' : ['1111222233334444', '2222333344445555'] as String[],
                'Amount' : ['500.00'] as String[]
        ])*/
        then:''
    }
}
