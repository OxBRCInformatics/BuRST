description "Creates a new Rabbit-native Burst Capable Typed Consumer class.", {
    usage "grails create-consumer [type]"
    argument name: 'Type', description: 'The fully qualified type for the consumer to handle'
}

model = model(args[0])
render template: 'TypedConsumer.groovy',
       destination: file("grails-app/rabbit-consumers/$model.packagePath/${model.simpleName}Consumer.groovy"),
       model: model