import groovy.transform.NamedParam
import groovy.transform.NamedVariant

@NamedVariant
def call(@NamedParam String name, @NamedParam String value) {

    echo "Name: ${name},  Value: ${value}"
}
