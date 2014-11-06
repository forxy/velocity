package velocity

import grails.rest.Resource

@Resource(uri = "/configs", formats = ['json'])
class VelocityConfig {
    Set<String> primaryMetrics;
    Long period;
    Long expiresIn;
    Set<AggregationConfig> aggregationConfigs;
    Date createDate;
    String createdBy;
    Date updateDate;
    String updatedBy;

    static constraints = {
        id blank: false
        period min: 1L
        expiresIn nullable: true
        createDate nullable: true
        createdBy nullable: true
        updateDate nullable: true
        updatedBy blank: false
    }
    static embedded = ['aggregationConfigs']
    static mapWith = "mongo"
}
