package ox.softeng.burst.grails.plugin.test

class Test {

    String type
    String name
    boolean failSave = false

    static constraints = {
        name blank: false
    }
}
