# BuRST Grails Plugin

[![Build Status](https://travis-ci.org/olliefreeman/BuRST.svg?branch=develop)](https://travis-ci.org/olliefreeman/BuRST)
[![Master](http://img.shields.io/badge/master-1.0-green.svg)](https://github.com/olliefreeman/BuRST/tree/master/grails-burst-plugin)
[![License](http://img.shields.io/badge/license-Apache_License_v2-lightgrey.svg)](https://github.com/olliefreeman/BuRST/blob/develop/grails-burst-plugin/LICENSE)

This is a Grails 3 plugin which extends the Rabbitmq Native Grails Plugin and adds in BuRST capability for rabbit message consumers and web based controllers.

For more information on how to use the Rabbitmq Native Plugin see the [available documentation](http://budjb.github.io/grails-rabbitmq-native/doc/manual/index.html).

**NOTE**: Currently the Rabbitmq Native Plugin is built for Grails 2 and has a beta release version for Grails 3.0.x. This plugin infact makes use of the updated plugin which source code is available [here](https://github.com/olliefreeman/grails-rabbitmq-native]) under branch `grails-3.1.x`, and is added using the gradle dependency `compile group: 'org.grails.plugins', name: 'rabbitmq-native3', version: '3.1.3'`.

## How to use

1. Add as a dependency to a Grails 3 application.
1. Configure the Rabbit connections in `application.yml`. See [sample configurations](# Sample Configurations) for a few ways to do this.
1. Use the controllers or consumers.
1. Provide the topics which any message should include
1. If desired override the source from the grails application name to something more relevant.

## Controllers

Currently the plugin only adds support for Restful controllers however implementation is easy.

### Restful Controllers
Extend the `RestfulControllerBurstCapable` class and set as the superclass in the `@resource` annotation for any restful resources.

Any save or update calls will be handled as expected however any exceptions or errors will cause a message to be sent to the `burst` queue.

## Rabbit Consumers

Any rabbit consumers should be constructed using the rabbitmq-native3 plugin functionality, and then implement one of the available traits.

The plugin currently provides 3 consumer options:
* MessageConsumerBurstCapable
* ResourceMessageConsumerBurstCapable
* XmlResourceMessageConsumerBurstCapable

Each trait adds the required `handleMessage()` methods.

The `MessageConsumerBurstCapable` trait then requires the implementation of a `processMessage()` method to handle the defined type.

The other 2 traits actually take care of the processing by expecting any incoming message to be data bound and then saved, this follows the pattern of the RestfulController.

The only methods which must be defined are the `topics` list and the extraction of metadata where applicable.


## Sample Configurations

For more information see the [rabbitmq-native plugin documentation](http://budjb.github.io/grails-rabbitmq-native/doc/manual/guide/configuration.html).

### Exchange configuration
```yml
rabbitmq:
    connection:
        host: 'localhost'
        username: 'guest'
        password: 'guest'
        isDefault: true
        name: 'default'
    queues:
        exchange_test:
            type: 'topic'
            autoDelete: true
            queues:
                queue_burst:
                    autoDelete: true
                    binding: '#.burst'
```

### Direct configuration
```yml
rabbitmq:
    connection:
        host: 'localhost'
        username: 'guest'
        password: 'guest'
        isDefault: true
        name: 'default'
    queues:
        queue_burst:
            autoDelete: true
            binding: '#.burst'
```
