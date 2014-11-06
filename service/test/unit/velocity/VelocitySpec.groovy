package velocity

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Velocity)
class VelocitySpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test creating"() {
        when: 'Velocity metric created'
        //def p = new Velocity()

        then: 'validation should pass'
        //p.validate()
    }
}
