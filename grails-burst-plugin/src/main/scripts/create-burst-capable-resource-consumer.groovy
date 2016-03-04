description "Creates a new Rabbit-native Burst Capable Resource Consumer class.", {
    usage "grails create-consumer [resource]"
    argument name: 'Resource', description: 'The fully qualified name resource for consumer to process'
}

model = model(args[0])
render template: 'ResourceConsumer.groovy',
       destination: file("grails-app/rabbit-consumers/$model.packagePath/${model.simpleName}Consumer.groovy"),
       model: model