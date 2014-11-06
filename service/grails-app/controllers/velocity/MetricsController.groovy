package velocity

import grails.plugin.gson.converters.GSON
import grails.rest.RestfulController

class MetricsController extends RestfulController {

    VelocityService velocityService

    static allowedMethods = [check: "POST"]

    def check = {
        render velocityService.getMetrics(request.JSON as Map<String, String[]>) as GSON
    }
}
