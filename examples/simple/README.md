# A "Simple" Example

This example illustrates how to use most of the functionality provided by the
`seuss` crate. It exposes a collection of ComputerSystems that all support the
ComputerSystem.Reset action, which simulate power state operations on the
virtual resources. To run this example, it's recommended to utilize the test
image with Docker. See the `test/` directory in the root of this repository
for more information.

Once the service is running, explore! Run the Redfish-Protocol-Validator (it
passes 100% of the tests), submit requests with `curl` to turn the systems on
or off, etc.

To turn the system on:

```
$ curl -k -u administrator:administrator https://localhost:3001/redfish/v1/Systems/1 | json_pp
{
   "@odata.id" : "/redfish/v1/Systems/1",
   "@odata.type" : "#ComputerSystem.v1_20_1.ComputerSystem",
   "Actions" : {
      "#ComputerSystem.Reset" : {
         "target" : "/redfish/v1/Systems/1/Actions/ComputerSystem.Reset"
      }
   },
   "Id" : "1",
   "Name" : "SimpleSystem-1",
   "PowerState" : "Off"
}
$ curl -H 'Content-Type: application/json' -d '{"ResetType":"On"}' -u administrator:administrator -k \
  https://localhost:3001/redfish/v1/Systems/1/Actions/ComputerSystem.Reset
$ curl -k -u administrator:administrator https://localhost:3001/redfish/v1/Systems/1 | json_pp
{
   "@odata.id" : "/redfish/v1/Systems/1",
   "@odata.type" : "#ComputerSystem.v1_20_1.ComputerSystem",
   "Actions" : {
      "#ComputerSystem.Reset" : {
         "target" : "/redfish/v1/Systems/1/Actions/ComputerSystem.Reset"
      }
   },
   "Id" : "1",
   "Name" : "SimpleSystem-1",
   "PowerState" : "On"
}
```
