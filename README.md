# iotKafkaIndoorSensors

This is a more real-world example of data processing. The other processors are basically cookie-cutter with 
custom data structures for their Topic. For this one, however, I'm using small Arduino boards with ESP32 
WiFi. They send messages to an MQTT bridge which then forwards the data to Kafka.

I intentionally do minimal data modification at the MQTT bridge (though there is a strong case for 
converting the data to protobuf there, maybe I'll do that later) and just pass it on "as is" to Kafka.
I'm also working on a UDP-based bridge that will do the same thing as MQTT.

Because I've built those indoor sensors with different components, they provide different data. I'd like 
to say I did that to challenge myself and provide more interesting data, but the reality is I built the 
sensors with the parts I had and there were inconsistencies.

So for this, I use [Circe's "Optics" libraries](https://circe.github.io/circe/optics.html) which converts
 the JSON into my Class structures. It gets a bit complicated/convoluted but I use Optionals to filter
 out sensors that don't exist in the source  

The data is then packaged up for Influx using a string builder function that I kinda hate but have to
use for Influx.

The bad news is that Circe's Optics uses Scala's ["Dynamic" feature](https://stackoverflow.com/questions/15799811/how-does-type-dynamic-work-and-how-to-use-it) 
which means it won't tell you that you've made a typo or missed a field. So test this carefully and 
thoroughly.
