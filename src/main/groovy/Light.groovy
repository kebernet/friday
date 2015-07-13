/**
 *  Friday X10 Light
 *
 *  Copyright 2015 Robert "kebernet" Cooper
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

import groovy.json.JsonSlurper

preferences {
    input("ip", "string", title:"IP Address", description: "IP Address of your Friday server. (192.168.16.82)", required: true, displayDuringSetup: true)
    input("x10id", "string", title:"X10 Device", description: "X10 Device ID (A1)", defaultValue: "A1" , required: true, displayDuringSetup: true)
}


metadata {
    definition (name: "Friday X10 Light", namespace: "kebernet", author: "Robert \"kebernet\" Cooper") {
        capability "Switch"
        capability "Switch Level"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles {
        standardTile("button", "device.switch", width: 1, height: 1, canChangeIcon: true) {
            state "off", label: 'Off', action: "on",  icon: "st.Lighting.light4", backgroundColor: "#ffffff", nextState: "on"
            state "on", label: 'On', action: "off", icon: "st.Lighting.light4", backgroundColor: "#79b821", nextState: "off"
        }
        controlTile("levelSliderControl", "device.level", "slider", height: 1,
                width: 2, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"switch level.setLevel"
        }
        main(["button", "levelSliderControl"])
        details(["button", "levelSliderControl" ])
    }
}

def parse(String description) {
    log.debug "Parsing '${description}'"
    def lanMessage = parseLanMessage(description);
    def slurper = new JsonSlurper()
    def result = slurper.parseText(lanMessage.body);
    log.debug(lanMessage.body)
    if(result.brightness == 0 || !result.on) {
        sendEvent(name: "switch", value: "off")
    } else {
        sendEvent(name: "switch", value: "on")
    }
    sendEvent(name: "level", value: result.brightness)

}

private setDeviceNetworkId(ip,port){
    def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    device.deviceNetworkId = "$iphex:$porthex"
    log.debug "Device Network Id set to ${iphex}:${porthex}"
}

def sendState(body) {
    def path = "/state";
    log.debug "Executing 'on' "+x10id.toUpperCase()+ " at "+getHostAddress()+path;
    def result = new physicalgraph.device.HubAction(
            method: "POST",
            path: path,
            body: body,
            headers: [
                    HOST: getHostAddress(),
                    "Content-Type":"application/json",
                    "Accept":"application/json"
            ]
    )
    return result
}

// handle commands
def on() {
    sendState('{"on":true, "brightness":100}')
}

def off() {
    sendState('{"on":true, "brightness":0}')
}

def setLevel(Double level) {
    sendState('{"on":true, "brightness":'+Math.abs(level)+'}')
}

private getHostAddress() {
    int port = 33000;
    port += "ABCDEFGHIJKLMNOP".indexOf(""+x10id.toUpperCase().charAt(0)) * 16;
    port += Integer.valueOf(x10id.substring(1));
    log.debug x10id.substring(1) +" == " +port;
    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }
    setDeviceNetworkId(ip, port)
    return ip+":"+port

}

private String convertIPtoHex(ipAddress) {
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}


