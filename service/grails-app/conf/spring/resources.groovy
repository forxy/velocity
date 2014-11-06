import grails.rest.render.json.JsonCollectionRenderer
import grails.rest.render.json.JsonRenderer
import velocity.DBCache
import velocity.Velocity

// Place your Spring DSL code here
beans = {
    dbCache(DBCache)

    velocityCollectionRenderer(JsonCollectionRenderer, Velocity) {
        excludes = ['class']
    }

    velocityRenderer(JsonRenderer, Velocity) {
        excludes = ['class']
    }
}
