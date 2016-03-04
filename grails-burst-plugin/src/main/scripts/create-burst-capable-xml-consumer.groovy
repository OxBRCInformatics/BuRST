description "Creates a new Rabbit-native Burst Capable Xml Consumer class.", {
    usage "grails create-consumer [consumer class name]"
    argument name: 'Consumer Class Name', description: 'The fully qualified name of the consumer class without "Consumer"'
}

model = model(args[0])
render template: 'XmlConsumer.groovy',
       destination: file("grails-app/rabbit-consumers/$model.packagePath/${model.simpleName}Consumer.groovy"),
       model: model